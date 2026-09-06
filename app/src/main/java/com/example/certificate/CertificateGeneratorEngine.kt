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

        // Supersample 2x for ultra-sharp A4 print rendering (1792 x 2400)
        val targetWidth = originalTemplate.width * 2
        val targetHeight = originalTemplate.height * 2

        val scaledBitmap = Bitmap.createScaledBitmap(
            originalTemplate,
            targetWidth,
            targetHeight,
            true
        ).copy(Bitmap.Config.ARGB_8888, true)

        val canvas = Canvas(scaledBitmap)
        val scale = targetWidth / 896f

        drawDynamicFields(canvas, targetWidth, targetHeight, scale, data)

        return scaledBitmap
    }

    private fun drawDynamicFields(
        canvas: Canvas,
        width: Int,
        height: Int,
        scale: Float,
        data: CertificateData
    ) {
        val centerX = width / 2f
        val maxSafeWidth = width * 0.74f

        // 1. Student Name
        // - Large bold serif font
        // - Center aligned
        // - Single line only
        // - Auto shrink if too long
        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0A192F")
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.02f
        }
        var nameSize = 39f * scale
        namePaint.textSize = nameSize
        while (namePaint.measureText(data.studentName) > maxSafeWidth && nameSize > 20f * scale) {
            nameSize -= 1f * scale
            namePaint.textSize = nameSize
        }
        val nameY = 442f * scale
        canvas.drawText(data.studentName, centerX, nameY, namePaint)

        // 2. Class Information
        // - Directly below name
        // - Medium size
        val classPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#203A63")
            textSize = 18f * scale
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.03f
        }
        while (classPaint.measureText(data.studentClass) > maxSafeWidth && classPaint.textSize > 13f * scale) {
            classPaint.textSize -= 0.5f * scale
        }
        val classY = 482f * scale
        canvas.drawText(data.studentClass, centerX, classY, classPaint)

        // 3. Achievement Statement
        // "This certificate is awarded to {studentName}
        // for successfully unlocking
        // {levelName}
        // through demonstrated discipline,
        // consistency and self-improvement."
        // - Maximum 4 lines
        // - Perfect line spacing
        // - No overlap
        val achievementPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1B2A4A")
            textSize = 15.5f * scale
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        val achievementLines = data.getAchievementLines().take(4)
        while (achievementLines.any { achievementPaint.measureText(it) > maxSafeWidth } && achievementPaint.textSize > 11.5f * scale) {
            achievementPaint.textSize -= 0.5f * scale
        }
        val lineSpacing = 24.5f * scale
        val achievementStartY = 534f * scale
        achievementLines.forEachIndexed { index, line ->
            canvas.drawText(line, centerX, achievementStartY + (index * lineSpacing), achievementPaint)
        }

        // 4. Level Information Bar
        // Level: {levelName}
        // XP: {totalXP}
        // Arc Day: {arcDay}
        // Single horizontal line.
        val levelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0B2545")
            textSize = 14f * scale
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.03f
        }
        val levelInfoText = data.levelInfoLine
        while (levelPaint.measureText(levelInfoText) > maxSafeWidth && levelPaint.textSize > 10.5f * scale) {
            levelPaint.textSize -= 0.5f * scale
        }
        val levelBarY = 660f * scale
        canvas.drawText(levelInfoText, centerX, levelBarY, levelPaint)

        // 5. Quote Section
        // Display exactly:
        // "The protocol rewards action,
        // not intention."
        // Center aligned.
        val quotePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#2D3748")
            textSize = 14.5f * scale
            typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
            textAlign = Paint.Align.CENTER
        }
        val quoteLineSpacing = 21f * scale
        val quoteStartY = 712f * scale
        val quoteLines = data.quoteLines
        quoteLines.forEachIndexed { index, line ->
            canvas.drawText(line, centerX, quoteStartY + (index * quoteLineSpacing), quotePaint)
        }

        // 6. Date Section
        // Issue Date:
        // {issueDate}
        val dateLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1B365D")
            textSize = 13f * scale
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.04f
        }
        val dateValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#203A63")
            textSize = 13.5f * scale
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        val dateLabelY = 988f * scale
        val dateValueY = 1008f * scale
        canvas.drawText("Issue Date:", centerX, dateLabelY, dateLabelPaint)
        canvas.drawText(data.issueDate, centerX, dateValueY, dateValuePaint)

        // 7. Certificate ID
        // ID: {certificateId}
        // Small text near bottom.
        val idPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#4A5568")
            textSize = 10.5f * scale
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.05f
        }
        val idY = 1058f * scale
        canvas.drawText("ID: ${data.certificateId}", centerX, idY, idPaint)
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

        return file
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
