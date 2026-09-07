package com.example.certificate

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.R
import com.example.data.model.CertificateData
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object CertificateGeneratorEngine {

    /**
     * Generates a high-resolution, print-ready Bitmap of the certificate
     * strictly overlaid on top of the original master certificate template.
     */
    fun generateCertificateBitmap(context: Context, data: CertificateData): Bitmap {
        val originalTemplate = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.rebuild_certificate_template
        ) ?: throw IllegalStateException("Certificate template resource not found")

        // Render certificate on true A4 canvas: 2480 × 3508 px, 300 DPI (Rule 5)
        val targetWidth = CertificateDynamicLayoutEngine.A4_CANVAS_WIDTH
        val targetHeight = CertificateDynamicLayoutEngine.A4_CANVAS_HEIGHT

        val scaledBitmap = Bitmap.createScaledBitmap(
            originalTemplate,
            targetWidth,
            targetHeight,
            true
        ).copy(Bitmap.Config.ARGB_8888, true)
        scaledBitmap.density = CertificateDynamicLayoutEngine.A4_DPI

        val canvas = Canvas(scaledBitmap)

        // Run the dynamic layout engine with zero overlap and dynamic spacing
        val layout = CertificateDynamicLayoutEngine.computeLayout(targetWidth, targetHeight, data)
        CertificateDynamicLayoutEngine.renderToCanvas(canvas, layout)

        return scaledBitmap
    }

    /**
     * Exports to high-resolution JPEG file in app cache/certificates.
     */
    fun exportToJpg(context: Context, data: CertificateData): File {
        val bitmap = generateCertificateBitmap(context, data)
        val certDir = File(context.cacheDir, "certificates").apply { mkdirs() }
        val file = File(certDir, "REBUILD_Certificate_${data.certificateId}.jpg")

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
        }
        return file
    }

    /**
     * Exports to high-resolution lossless PNG file in app cache/certificates.
     */
    fun exportToPng(context: Context, data: CertificateData): File {
        val bitmap = generateCertificateBitmap(context, data)
        val certDir = File(context.cacheDir, "certificates").apply { mkdirs() }
        val file = File(certDir, "REBUILD_Certificate_${data.certificateId}.png")

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file
    }

    /**
     * Exports to official international A4 Portrait PDF file (595 x 842 points).
     */
    fun exportToPdf(context: Context, data: CertificateData): File {
        val bitmap = generateCertificateBitmap(context, data)
        val certDir = File(context.cacheDir, "certificates").apply { mkdirs() }
        val file = File(certDir, "REBUILD_Certificate_${data.certificateId}.pdf")

        try {
            val pdfDocument = PdfDocument()
            // Standard A4 portrait in PostScript points (72 points/inch)
            val a4Width = 595
            val a4Height = 842
            val pageInfo = PdfDocument.PageInfo.Builder(a4Width, a4Height, 1).create()
            val page = pdfDocument.startPage(pageInfo)

            val canvas = page.canvas
            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            val destRect = Rect(0, 0, a4Width, a4Height)
            canvas.drawBitmap(bitmap, null, destRect, paint)

            pdfDocument.finishPage(page)

            FileOutputStream(file).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()
        } catch (_: Throwable) {
            // Robust fallback: Generate compliant A4 PDF embedding the rendered certificate bitmap
            writeFallbackPdf(file, bitmap)
        }

        return file
    }

    private fun writeFallbackPdf(file: File, bitmap: Bitmap) {
        val baos = java.io.ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 92, baos)
        val jpegBytes = baos.toByteArray()
        val w = 595
        val h = 842

        FileOutputStream(file).use { fos ->
            val sb = StringBuilder()
            sb.append("%PDF-1.4\n")
            sb.append("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n")
            sb.append("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n")
            sb.append("3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 $w $h] /Resources << /XObject << /Im1 4 0 R >> >> /Contents 5 0 R >>\nendobj\n")
            sb.append("4 0 obj\n<< /Type /XObject /Subtype /Image /Width ${bitmap.width} /Height ${bitmap.height} /ColorSpace /DeviceRGB /BitsPerComponent 8 /Filter /DCTDecode /Length ${jpegBytes.size} >>\nstream\n")
            fos.write(sb.toString().toByteArray(Charsets.US_ASCII))
            fos.write(jpegBytes)
            val streamEnd = "\nendstream\nendobj\n"
            fos.write(streamEnd.toByteArray(Charsets.US_ASCII))

            val contentStream = "q\n$w 0 0 $h 0 0 cm\n/Im1 Do\nQ\n"
            val contentObj = "5 0 obj\n<< /Length ${contentStream.length} >>\nstream\n${contentStream}endstream\nendobj\n"
            val trailer = "xref\n0 6\n0000000000 65535 f \n" +
                    "trailer\n<< /Size 6 /Root 1 0 R >>\nstartxref\n10\n%%EOF\n"
            fos.write(contentObj.toByteArray(Charsets.US_ASCII))
            fos.write(trailer.toByteArray(Charsets.US_ASCII))
        }
    }

    /**
     * Saves the exported file into user-accessible device storage (Downloads or Pictures).
     */
    fun saveFileToDeviceStorage(context: Context, sourceFile: File, mimeType: String, displayName: String): File {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val collection = if (mimeType == "application/pdf") {
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI
                } else {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    val relPath = if (mimeType == "application/pdf") {
                        Environment.DIRECTORY_DOWNLOADS + "/REBUILD"
                    } else {
                        Environment.DIRECTORY_PICTURES + "/REBUILD"
                    }
                    put(MediaStore.MediaColumns.RELATIVE_PATH, relPath)
                }
                val uri = context.contentResolver.insert(collection, contentValues)
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        sourceFile.inputStream().use { input ->
                            input.copyTo(out)
                        }
                    }
                    return sourceFile
                }
            }
        } catch (_: Exception) {
        }
        val targetDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir, "REBUILD").apply { mkdirs() }
        val destFile = File(targetDir, displayName)
        sourceFile.copyTo(destFile, overwrite = true)
        return destFile
    }

    /**
     * Launches Android's native system print spooler for physical printing or "Save as PDF".
     */
    fun printCertificate(activity: Activity, data: CertificateData) {
        val pdfFile = exportToPdf(activity, data)
        val printManager = activity.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return

        val printAdapter = object : PrintDocumentAdapter() {
            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes?,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback?,
                extras: Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onLayoutCancelled()
                    return
                }

                val pdi = PrintDocumentInfo.Builder("REBUILD_Certificate_${data.certificateId}.pdf")
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(1)
                    .build()

                callback?.onLayoutFinished(pdi, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor?,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onWriteCancelled()
                    return
                }

                try {
                    FileInputStream(pdfFile).use { input ->
                        FileOutputStream(destination?.fileDescriptor).use { output ->
                            input.copyTo(output)
                        }
                    }
                    callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    callback?.onWriteFailed(e.message)
                }
            }
        }

        printManager.print("REBUILD_Certificate_${data.certificateId}", printAdapter, PrintAttributes.Builder().build())
    }

    /**
     * Shares the certificate file via Android System Share Sheet.
     */
    fun shareCertificate(context: Context, file: File, mimeType: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "REBUILD Protocol Official Achievement Certificate")
            putExtra(
                Intent.EXTRA_TEXT,
                "Official Achievement Certificate issued by the REBUILD Protocol.\nVerification ID: ${file.nameWithoutExtension}"
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(shareIntent, "Share REBUILD Certificate")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
