package com.example.certificate

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
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

        // 1. Subtitle: "THIS CERTIFICATE IS PROUDLY PRESENTED TO"
        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1B365D")
            textSize = 15f * scale
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.18f
        }
        canvas.drawText("THIS CERTIFICATE IS PROUDLY PRESENTED TO", centerX, 445f * scale, subtitlePaint)

        // 2. Student Name: {{student_name}}
        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0A192F")
            textSize = 40f * scale
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.04f
        }
        canvas.drawText(data.studentName, centerX, 505f * scale, namePaint)

        // 3. Ornate Gold Divider Under Name
        val goldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#C69214")
            strokeWidth = 2.2f * scale
            style = Paint.Style.STROKE
        }
        val lineStart = centerX - (250f * scale)
        val lineEnd = centerX + (250f * scale)
        val lineY = 524f * scale
        canvas.drawLine(lineStart, lineY, lineEnd, lineY, goldPaint)

        // Small diamond accent in center of gold line
        val diamondPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#C69214")
            style = Paint.Style.FILL
        }
        val diamondSize = 4f * scale
        val diamondPath = android.graphics.Path().apply {
            moveTo(centerX, lineY - diamondSize)
            lineTo(centerX + diamondSize, lineY)
            lineTo(centerX, lineY + diamondSize)
            lineTo(centerX - diamondSize, lineY)
            close()
        }
        canvas.drawPath(diamondPath, diamondPaint)

        // 4. Class / Cohort line
        val classPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#203A63")
            textSize = 18f * scale
            typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.06f
        }
        canvas.drawText(data.studentClass, centerX, 552f * scale, classPaint)

        // 5. Official Achievement Text:
        // "This certificate is proudly awarded to {{student_name}} for successfully achieving Level {{level}} – {{rank_title}} through demonstrated discipline, consistency, commitment, and continuous self-improvement within the REBUILD protocol."
        val achievementText = data.getAchievementText()
        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1B2A4A")
            textSize = 15.5f * scale
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        val maxTextWidth = (590f * scale).toInt()
        val staticLayout = StaticLayout.Builder.obtain(
            achievementText,
            0,
            achievementText.length,
            textPaint,
            maxTextWidth
        )
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(3f * scale, 1.15f)
            .setIncludePad(false)
            .build()

        canvas.save()
        canvas.translate(centerX, 580f * scale)
        staticLayout.draw(canvas)
        canvas.restore()

        // 6. Protocol Metrics Badge Strip:
        // Level {{level}} • {{rank_title}}  |  {{xp}} XP  |  {{streak}}D Streak  |  Winter Arc Day {{winter_arc_day}}
        val badgeY = 672f * scale
        val badgeRect = RectF(
            centerX - (290f * scale),
            badgeY - (16f * scale),
            centerX + (290f * scale),
            badgeY + (16f * scale)
        )
        val badgeBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0DF0F4F8") // 5% tint
            style = Paint.Style.FILL
        }
        val badgeBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#40C69214")
            strokeWidth = 1f * scale
            style = Paint.Style.STROKE
        }
        canvas.drawRoundRect(badgeRect, 8f * scale, 8f * scale, badgeBgPaint)
        canvas.drawRoundRect(badgeRect, 8f * scale, 8f * scale, badgeBorderPaint)

        val metricsText = "Level ${data.level} (${data.rankTitle})   •   ${String.format("%,d", data.xp)} XP   •   ${data.streak}D Streak   •   Arc Day ${data.winterArcDay}"
        val metricsPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0B2545")
            textSize = 13.5f * scale
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.04f
        }
        canvas.drawText(metricsText, centerX, badgeY + (5f * scale), metricsPaint)

        // 7. AI Evaluation (Italicized Quote)
        val evalHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#C69214")
            textSize = 11.5f * scale
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.12f
        }
        canvas.drawText("AI PROTOCOL EVALUATION", centerX, 712f * scale, evalHeaderPaint)

        val evalText = "\"${data.aiEvaluation}\""
        val evalPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#2D3748")
            textSize = 13.5f * scale
            typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
            textAlign = Paint.Align.CENTER
        }
        val evalLayout = StaticLayout.Builder.obtain(
            evalText,
            0,
            evalText.length,
            evalPaint,
            (560f * scale).toInt()
        )
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(2f * scale, 1.15f)
            .setIncludePad(false)
            .build()

        canvas.save()
        canvas.translate(centerX, 726f * scale)
        evalLayout.draw(canvas)
        canvas.restore()

        // 8. Date Achieved
        val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1B365D")
            textSize = 13f * scale
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.05f
        }
        canvas.drawText("DATE OF ISSUANCE: ${data.dateAchieved.uppercase()}", centerX, 792f * scale, datePaint)

        // 9. Signatures Area
        // The original template has printed lines for "President Director" (Left) and "General Manager" (Right).
        // We print "REBUILD Neural Engine" above President Director, and "REBUILD Achievement Authority" above General Manager.
        val signatureLeftX = width * 0.285f
        val signatureRightX = width * 0.715f
        val signatureTextY = 1018f * scale

        val signAuthorityPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0A192F")
            textSize = 14.5f * scale
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.05f
        }
        canvas.drawText("REBUILD Neural Engine", signatureLeftX, signatureTextY, signAuthorityPaint)
        canvas.drawText("REBUILD Achievement Authority", signatureRightX, signatureTextY, signAuthorityPaint)

        val signSubPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#718096")
            textSize = 10.5f * scale
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("[DIGITALLY SIGNED]", signatureLeftX, signatureTextY + (13f * scale), signSubPaint)
        canvas.drawText("[VERIFIED AUTHORITY]", signatureRightX, signatureTextY + (13f * scale), signSubPaint)

        // 10. Footer Security & Verification Data
        val footerY1 = 1098f * scale
        val footerY2 = 1114f * scale
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#4A5568")
            textSize = 10.5f * scale
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }

        val idLine = "CERTIFICATE ID: ${data.certificateId}   •   VERIFICATION HASH: ${data.verificationHash.take(16)}..."
        canvas.drawText(idLine, centerX, footerY1, footerPaint)

        val verifyLine = "Issued By: REBUILD Neural Engine   |   Verification: REBUILD Achievement Authority"
        val authorityFooterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#718096")
            textSize = 9.5f * scale
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.05f
        }
        canvas.drawText(verifyLine, centerX, footerY2, authorityFooterPaint)
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
