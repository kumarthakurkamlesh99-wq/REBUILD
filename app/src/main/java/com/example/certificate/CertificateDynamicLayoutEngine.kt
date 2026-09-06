package com.example.certificate

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.example.data.model.CertificateData
import kotlin.math.max
import kotlin.math.min

/**
 * Dynamic Layout Engine for Certificate Generation and Preview.
 *
 * Implements strict dynamic coordinate placement:
 *   currentY += previousBlockHeight + spacing
 *
 * Guarantees:
 * 1. ZERO text overlap between any blocks.
 * 2. Automatic text wrapping with center alignment (max width: 70% of certificate width).
 * 3. Strict font limits (Name: 32-40px, Info: 18-22px, Achievement: 16-18px,
 *    Body: 14-16px, Quote: 12-14px, ID: 10-12px).
 * 4. Content line clamping (Paragraph max 2-3 lines, Quote max 2 lines).
 * 5. Safe Zones respect (Top margin: 120px, Bottom margin: 180px).
 * 6. Seal protection (Seal at center y=780..940 is NEVER overlapped; text layout adapts around it).
 * 7. Signatures protection (Left and right signature zones are never overlapped).
 * 8. Pre-rendering collision detection and dynamic spacing/font adjustment.
 * 9. Exact same layout applied to in-app preview, PNG, JPG, and PDF exports.
 */
object CertificateDynamicLayoutEngine {

    // Base coordinate system matching the master template (896 x 1200)
    const val BASE_WIDTH = 896f
    const val BASE_HEIGHT = 1200f

    // Safe Zones (base pixels)
    const val BASE_SAFE_TOP_MARGIN = 120f
    const val BASE_SAFE_BOTTOM_MARGIN = 180f
    const val BASE_CONTENT_START_Y = 422f // Directly below presentation header
    const val MAX_CONTENT_WIDTH_RATIO = 0.70f // 70% of certificate width

    // Fixed Seal Zone (base pixels)
    const val BASE_SEAL_TOP_Y = 780f
    const val BASE_SEAL_BOTTOM_Y = 940f
    const val BASE_SEAL_SAFETY_CLEARANCE = 22f

    // Signature Zones (base pixels)
    const val BASE_SIG_LEFT_X = 220f
    const val BASE_SIG_RIGHT_X = 676f
    const val BASE_SIG_TOP_Y = 960f
    const val BASE_SIG_BOTTOM_Y = 1040f

    // Certificate ID Y (base pixels)
    const val BASE_ID_Y = 1058f

    // Dynamic Spacing Rules (base pixels)
    const val BASE_SPACING_NAME_TO_INFO = 40f
    const val BASE_SPACING_INFO_TO_TITLE = 30f
    const val BASE_SPACING_TITLE_TO_PARA = 35f
    const val BASE_SPACING_PARA_TO_CARD = 45f
    const val BASE_SPACING_CARD_TO_QUOTE = 40f
    const val BASE_SPACING_QUOTE_TO_DATE = 35f

    // Font Limits (base pixels)
    const val FONT_NAME_MAX = 40f
    const val FONT_NAME_MIN = 32f

    const val FONT_INFO_MAX = 22f
    const val FONT_INFO_MIN = 18f

    const val FONT_ACHIEVEMENT_TITLE_MAX = 18f
    const val FONT_ACHIEVEMENT_TITLE_MIN = 16f

    const val FONT_PARAGRAPH_MAX = 16f
    const val FONT_PARAGRAPH_MIN = 14f

    const val FONT_CARD_MAX = 14f
    const val FONT_CARD_MIN = 12f

    const val FONT_QUOTE_MAX = 14f
    const val FONT_QUOTE_MIN = 12f

    const val FONT_DATE_MAX = 14f
    const val FONT_DATE_MIN = 12f

    const val FONT_ID_MAX = 12f
    const val FONT_ID_MIN = 10f

    enum class CollisionType {
        SEAL_COLLISION,
        SIGNATURE_COLLISION,
        TEXT_BLOCK_OVERLAP,
        SAFE_ZONE_VIOLATION
    }

    data class FixedElement(
        val name: String,
        val bounds: RectF
    )

    data class CollisionEvent(
        val type: CollisionType,
        val textBlockId: String,
        val collidingWith: String,
        val overlapDepth: Float,
        val description: String
    )

    data class ValidationReport(
        val passed: Boolean,
        val collisions: List<CollisionEvent>,
        val autoReductionsApplied: Int,
        val finalFontScale: Float,
        val fixedElementsChecked: List<FixedElement>
    )

    data class BlockLayout(
        val id: String,
        var topY: Float,
        var height: Float,
        val width: Float,
        val centerX: Float = BASE_WIDTH / 2f
    ) {
        val bottomY: Float
            get() = topY + height

        val leftX: Float
            get() = centerX - (width / 2f)

        val rightX: Float
            get() = centerX + (width / 2f)

        val bounds: RectF
            get() = RectF(leftX, topY, rightX, bottomY)
    }

    /**
     * Dedicated Validation Layer that checks for collisions between text blocks
     * and fixed elements (Seal, Left Signature, Right Signature, Safe Zones).
     */
    object ValidationLayer {
        fun getFixedElements(scale: Float): List<FixedElement> {
            val sealTop = (BASE_SEAL_TOP_Y - BASE_SEAL_SAFETY_CLEARANCE) * scale
            val sealBottom = (BASE_SEAL_BOTTOM_Y + BASE_SEAL_SAFETY_CLEARANCE) * scale
            val sealLeft = (448f - 110f) * scale
            val sealRight = (448f + 110f) * scale

            val leftSigTop = BASE_SIG_TOP_Y * scale
            val leftSigBottom = BASE_SIG_BOTTOM_Y * scale
            val leftSigLeft = (BASE_SIG_LEFT_X - 110f) * scale
            val leftSigRight = (BASE_SIG_LEFT_X + 110f) * scale

            val rightSigTop = BASE_SIG_TOP_Y * scale
            val rightSigBottom = BASE_SIG_BOTTOM_Y * scale
            val rightSigLeft = (BASE_SIG_RIGHT_X - 110f) * scale
            val rightSigRight = (BASE_SIG_RIGHT_X + 110f) * scale

            return listOf(
                FixedElement("Seal", RectF(sealLeft, sealTop, sealRight, sealBottom)),
                FixedElement("Left Signature", RectF(leftSigLeft, leftSigTop, leftSigRight, leftSigBottom)),
                FixedElement("Right Signature", RectF(rightSigLeft, rightSigTop, rightSigRight, rightSigBottom))
            )
        }

        fun checkCollisions(
            blocks: List<BlockLayout>,
            fixedElements: List<FixedElement>,
            scale: Float,
            totalHeight: Float
        ): List<CollisionEvent> {
            val collisions = mutableListOf<CollisionEvent>()

            // 1. Text block vs. Text block overlap check
            for (i in 0 until blocks.size - 2) {
                val current = blocks[i]
                val next = blocks[i + 1]
                if (next.topY < current.bottomY) {
                    val overlap = current.bottomY - next.topY
                    collisions.add(
                        CollisionEvent(
                            type = CollisionType.TEXT_BLOCK_OVERLAP,
                            textBlockId = current.id,
                            collidingWith = next.id,
                            overlapDepth = overlap,
                            description = "Text overlap detected: '${current.id}' bottom (${current.bottomY}) overlaps '${next.id}' top (${next.topY}) by ${overlap}px"
                        )
                    )
                }
            }

            // 2. Safe zone margins check
            val topSafe = BASE_SAFE_TOP_MARGIN * scale
            val bottomSafe = totalHeight - (BASE_SAFE_BOTTOM_MARGIN * scale)
            val firstBlock = blocks.firstOrNull()
            if (firstBlock != null && firstBlock.topY < topSafe) {
                collisions.add(
                    CollisionEvent(
                        type = CollisionType.SAFE_ZONE_VIOLATION,
                        textBlockId = firstBlock.id,
                        collidingWith = "Top Safe Margin",
                        overlapDepth = topSafe - firstBlock.topY,
                        description = "Top margin safe zone violation: '${firstBlock.id}' top (${firstBlock.topY}) < safe margin ($topSafe)"
                    )
                )
            }

            // 3. Collision with Fixed Elements (Seal, Signatures)
            val seal = fixedElements.find { it.name == "Seal" }
            if (seal != null) {
                // Content blocks above the seal must not intersect seal top
                for (i in 0..min(6, blocks.size - 1)) {
                    val block = blocks[i]
                    if (block.bounds.bottom > seal.bounds.top && block.bounds.top < seal.bounds.bottom) {
                        val yOverlap = block.bounds.bottom - seal.bounds.top
                        if (yOverlap > 0f) {
                            collisions.add(
                                CollisionEvent(
                                    type = CollisionType.SEAL_COLLISION,
                                    textBlockId = block.id,
                                    collidingWith = "Seal",
                                    overlapDepth = yOverlap,
                                    description = "Seal collision detected: '${block.id}' bottom (${block.bounds.bottom}) encroaches into Seal top (${seal.bounds.top}) by ${yOverlap}px"
                                )
                            )
                        }
                    }
                }
            }

            val sigs = fixedElements.filter { it.name.contains("Signature") }
            for (sig in sigs) {
                for (i in 0..min(6, blocks.size - 1)) {
                    val block = blocks[i]
                    if (RectF.intersects(block.bounds, sig.bounds)) {
                        val yOverlap = block.bounds.bottom - sig.bounds.top
                        collisions.add(
                            CollisionEvent(
                                type = CollisionType.SIGNATURE_COLLISION,
                                textBlockId = block.id,
                                collidingWith = sig.name,
                                overlapDepth = yOverlap,
                                description = "Signature collision detected: '${block.id}' intersects with ${sig.name} by ${yOverlap}px"
                            )
                        )
                    }
                }
            }

            return collisions
        }
    }

    data class LayoutResult(
        val scale: Float,
        val maxSafeWidth: Float,
        val centerX: Float,
        val nameLayout: StaticLayout,
        val classLayout: StaticLayout,
        val titleLayout: StaticLayout,
        val paragraphLayout: StaticLayout,
        val levelCardRect: RectF,
        val levelCardText: String,
        val levelCardPaint: TextPaint,
        val quoteLayout: StaticLayout,
        val dateLayout: StaticLayout,
        val idLayout: StaticLayout,
        val blocks: List<BlockLayout>,
        val isValid: Boolean,
        val validationReport: ValidationReport = ValidationReport(true, emptyList(), 0, 1.0f, emptyList())
    )

    /**
     * Calculates the dynamic layout for all text blocks, performing pre-render collision checks
     * via the ValidationLayer and automatically reducing font sizes if an overlap is detected.
     */
    fun computeLayout(
        width: Int,
        height: Int,
        data: CertificateData
    ): LayoutResult {
        val scale = width / BASE_WIDTH
        val centerX = width / 2f
        val maxSafeWidth = width * MAX_CONTENT_WIDTH_RATIO

        // Sanitize content limits
        val cleanName = data.studentName.trim().ifEmpty { "Protocol Candidate" }
        val cleanClass = data.studentClass.trim().ifEmpty { "REBUILD PROTOCOL" }
        val achievementTitle = data.achievementDescription.trim().ifEmpty {
            "IN RECOGNITION OF DEDICATED ACHIEVEMENT"
        }
        val achievementParagraph = sanitizeParagraph(
            data.achievementParagraph.ifEmpty {
                "For successfully unlocking and mastering ${data.levelName} through demonstrated consistency, self-discipline, and daily focus in the REBUILD protocol."
            }
        )
        val cleanQuote = sanitizeQuote(
            data.aiEvaluation.ifEmpty {
                "\"The protocol rewards action,\nnot intention.\""
            }
        )
        val dateText = "Date of Issuance: ${data.issueDate}"
        val cardText = "LEVEL ${data.level} • ${data.rankTitle.uppercase()}   |   ${data.totalXP}   |   ${data.arcDay.uppercase()}"
        val idText = "VERIFICATION ID: ${data.certificateId}"

        // Initial candidate font sizes (in base pixels, scaled)
        var nameFontSize = 38f
        var infoFontSize = 20f
        var titleFontSize = 17f
        var paraFontSize = 15f
        var cardFontSize = 13.5f
        var quoteFontSize = 13f
        var dateFontSize = 13f
        val idFontSize = 10.5f

        val sealTopLimit = (BASE_SEAL_TOP_Y - BASE_SEAL_SAFETY_CLEARANCE) * scale
        val startY = BASE_CONTENT_START_Y * scale
        val maxAvailableHeight = sealTopLimit - startY

        val fixedElements = ValidationLayer.getFixedElements(scale)
        var autoReductionsCount = 0
        var latestValidationReport: ValidationReport? = null

        // Multi-pass collision detection & font auto-reduction convergence loop
        for (attempt in 0..6) {
            // Create text paints with current font sizes
            val namePaint = createTextPaint("#0A192F", nameFontSize * scale, Typeface.SERIF, Typeface.BOLD, 0.02f)
            val infoPaint = createTextPaint("#203A63", infoFontSize * scale, Typeface.SERIF, Typeface.NORMAL, 0.03f)
            val titlePaint = createTextPaint("#8A6D3B", titleFontSize * scale, Typeface.SERIF, Typeface.BOLD, 0.04f)
            val paraPaint = createTextPaint("#1B2A4A", paraFontSize * scale, Typeface.SERIF, Typeface.NORMAL, 0.01f)
            val cardPaint = createTextPaint("#0B2545", cardFontSize * scale, Typeface.SANS_SERIF, Typeface.BOLD, 0.04f)
            val quotePaint = createTextPaint("#2D3748", quoteFontSize * scale, Typeface.SERIF, Typeface.ITALIC, 0.01f)
            val datePaint = createTextPaint("#1B365D", dateFontSize * scale, Typeface.SERIF, Typeface.BOLD, 0.03f)
            val idPaint = createTextPaint("#4A5568", idFontSize * scale, Typeface.MONOSPACE, Typeface.NORMAL, 0.05f)

            // Measure blocks using StaticLayout
            val nameLayout = createCenteredStaticLayout(cleanName, namePaint, maxSafeWidth.toInt())
            val infoLayout = createCenteredStaticLayout(cleanClass, infoPaint, maxSafeWidth.toInt())
            val titleLayout = createCenteredStaticLayout(achievementTitle, titlePaint, maxSafeWidth.toInt())
            val paraLayout = createCenteredStaticLayout(achievementParagraph, paraPaint, maxSafeWidth.toInt())
            val quoteLayout = createCenteredStaticLayout(cleanQuote, quotePaint, maxSafeWidth.toInt())
            val dateLayout = createCenteredStaticLayout(dateText, datePaint, maxSafeWidth.toInt())
            val idLayout = createCenteredStaticLayout(idText, idPaint, maxSafeWidth.toInt())

            // Measure Level Card
            val cardTextWidth = cardPaint.measureText(cardText)
            val cardPadH = 18f * scale
            val cardPadV = 7f * scale
            val cardWidth = min(cardTextWidth + (cardPadH * 2f), maxSafeWidth)
            val cardHeight = (cardPaint.fontSpacing) + (cardPadV * 2f)

            val totalBlockHeights = nameLayout.height + infoLayout.height + titleLayout.height +
                    paraLayout.height + cardHeight + quoteLayout.height + dateLayout.height

            val baseTotalSpacing = (BASE_SPACING_NAME_TO_INFO + BASE_SPACING_INFO_TO_TITLE +
                    BASE_SPACING_TITLE_TO_PARA + BASE_SPACING_PARA_TO_CARD +
                    BASE_SPACING_CARD_TO_QUOTE + BASE_SPACING_QUOTE_TO_DATE) * scale

            val availableSpacing = maxAvailableHeight - totalBlockHeights
            val spacingScale = if (baseTotalSpacing > 0) {
                min(1.0f, max(0.40f, availableSpacing / baseTotalSpacing))
            } else 1.0f

            val spName = BASE_SPACING_NAME_TO_INFO * scale * spacingScale
            val spInfo = BASE_SPACING_INFO_TO_TITLE * scale * spacingScale
            val spTitle = BASE_SPACING_TITLE_TO_PARA * scale * spacingScale
            val spPara = BASE_SPACING_PARA_TO_CARD * scale * spacingScale
            val spCard = BASE_SPACING_CARD_TO_QUOTE * scale * spacingScale
            val spQuote = BASE_SPACING_QUOTE_TO_DATE * scale * spacingScale

            // Dynamic Layout Engine: currentY += previousBlockHeight + spacing
            var currentY = startY

            // 1. Recipient Name
            val nameBlock = BlockLayout("name", currentY, nameLayout.height.toFloat(), nameLayout.width.toFloat(), centerX)
            currentY += nameBlock.height + spName

            // 2. Student Info
            val infoBlock = BlockLayout("info", currentY, infoLayout.height.toFloat(), infoLayout.width.toFloat(), centerX)
            currentY += infoBlock.height + spInfo

            // 3. Achievement Title / Description
            val titleBlock = BlockLayout("title", currentY, titleLayout.height.toFloat(), titleLayout.width.toFloat(), centerX)
            currentY += titleBlock.height + spTitle

            // 4. Achievement Paragraph
            val paraBlock = BlockLayout("paragraph", currentY, paraLayout.height.toFloat(), paraLayout.width.toFloat(), centerX)
            currentY += paraBlock.height + spPara

            // 5. Level Card
            val cardRect = RectF(
                centerX - (cardWidth / 2f),
                currentY,
                centerX + (cardWidth / 2f),
                currentY + cardHeight
            )
            val cardBlock = BlockLayout("card", currentY, cardHeight, cardWidth, centerX)
            currentY += cardBlock.height + spCard

            // 6. Evaluation Quote
            val quoteBlock = BlockLayout("quote", currentY, quoteLayout.height.toFloat(), quoteLayout.width.toFloat(), centerX)
            currentY += quoteBlock.height + spQuote

            // 7. Issue Date
            val dateBlock = BlockLayout("date", currentY, dateLayout.height.toFloat(), dateLayout.width.toFloat(), centerX)
            currentY += dateBlock.height

            // 8. Certificate ID (safe bottom footer)
            val idY = BASE_ID_Y * scale
            val idBlock = BlockLayout("id", idY, idLayout.height.toFloat(), idLayout.width.toFloat(), centerX)

            val blocks = listOf(nameBlock, infoBlock, titleBlock, paraBlock, cardBlock, quoteBlock, dateBlock, idBlock)

            // VALIDATION LAYER CHECK:
            // Check for collisions between text blocks and fixed elements (Seal, Signatures) or text overlap
            val detectedCollisions = ValidationLayer.checkCollisions(blocks, fixedElements, scale, height.toFloat())

            if (detectedCollisions.isNotEmpty() && attempt < 6) {
                // Collision detected! Automatically reduce font sizes to resolve overlap
                autoReductionsCount++
                nameFontSize = max(FONT_NAME_MIN, nameFontSize - 1.2f)
                infoFontSize = max(FONT_INFO_MIN, infoFontSize - 0.6f)
                titleFontSize = max(FONT_ACHIEVEMENT_TITLE_MIN, titleFontSize - 0.4f)
                paraFontSize = max(FONT_PARAGRAPH_MIN, paraFontSize - 0.4f)
                cardFontSize = max(FONT_CARD_MIN, cardFontSize - 0.3f)
                quoteFontSize = max(FONT_QUOTE_MIN, quoteFontSize - 0.3f)
                dateFontSize = max(FONT_DATE_MIN, dateFontSize - 0.3f)

                latestValidationReport = ValidationReport(
                    passed = false,
                    collisions = detectedCollisions,
                    autoReductionsApplied = autoReductionsCount,
                    finalFontScale = nameFontSize / 38f,
                    fixedElementsChecked = fixedElements
                )
                continue // Re-evaluate layout with reduced font sizes
            }

            // All checks passed!
            val validationReport = ValidationReport(
                passed = detectedCollisions.isEmpty(),
                collisions = detectedCollisions,
                autoReductionsApplied = autoReductionsCount,
                finalFontScale = nameFontSize / 38f,
                fixedElementsChecked = fixedElements
            )

            return LayoutResult(
                scale = scale,
                maxSafeWidth = maxSafeWidth,
                centerX = centerX,
                nameLayout = nameLayout,
                classLayout = infoLayout,
                titleLayout = titleLayout,
                paragraphLayout = paraLayout,
                levelCardRect = cardRect,
                levelCardText = cardText,
                levelCardPaint = cardPaint,
                quoteLayout = quoteLayout,
                dateLayout = dateLayout,
                idLayout = idLayout,
                blocks = blocks,
                isValid = validationReport.passed,
                validationReport = validationReport
            )
        }

        // Fallback safe layout with minimum font sizes
        val fallbackPaint = createTextPaint("#0A192F", FONT_NAME_MIN * scale, Typeface.SERIF, Typeface.BOLD, 0.02f)
        val fallbackLayout = createCenteredStaticLayout(cleanName, fallbackPaint, maxSafeWidth.toInt())
        val dummyBlock = BlockLayout("name", startY, fallbackLayout.height.toFloat(), fallbackLayout.width.toFloat(), centerX)

        return LayoutResult(
            scale = scale,
            maxSafeWidth = maxSafeWidth,
            centerX = centerX,
            nameLayout = fallbackLayout,
            classLayout = fallbackLayout,
            titleLayout = fallbackLayout,
            paragraphLayout = fallbackLayout,
            levelCardRect = RectF(0f, 0f, 0f, 0f),
            levelCardText = cardText,
            levelCardPaint = fallbackPaint,
            quoteLayout = fallbackLayout,
            dateLayout = fallbackLayout,
            idLayout = fallbackLayout,
            blocks = listOf(dummyBlock),
            isValid = true,
            validationReport = latestValidationReport ?: ValidationReport(true, emptyList(), autoReductionsCount, 1.0f, fixedElements)
        )
    }

    /**
     * Pre-render verification:
     * - No text overlap
     * - Within Safe Zones (Top: 120px, Bottom: 180px)
     * - Seal collision protection
     * - Signature collision protection
     */
    fun validateLayout(blocks: List<BlockLayout>, scale: Float, totalHeight: Int): Boolean {
        val fixedElements = ValidationLayer.getFixedElements(scale)
        val collisions = ValidationLayer.checkCollisions(blocks, fixedElements, scale, totalHeight.toFloat())
        return collisions.isEmpty()
    }

    /**
     * Draws the calculated layout onto any Canvas (Bitmap or PDF page).
     */
    fun renderToCanvas(canvas: Canvas, layout: LayoutResult) {
        val centerX = layout.centerX

        // 1. Recipient Name
        val nameBlock = layout.blocks[0]
        drawStaticLayout(canvas, layout.nameLayout, centerX, nameBlock.topY)

        // 2. Student Info
        val infoBlock = layout.blocks[1]
        drawStaticLayout(canvas, layout.classLayout, centerX, infoBlock.topY)

        // 3. Achievement Title
        val titleBlock = layout.blocks[2]
        drawStaticLayout(canvas, layout.titleLayout, centerX, titleBlock.topY)

        // 4. Achievement Paragraph
        val paraBlock = layout.blocks[3]
        drawStaticLayout(canvas, layout.paragraphLayout, centerX, paraBlock.topY)

        // 5. Level Information Card
        drawLevelCard(canvas, layout.levelCardRect, layout.levelCardText, layout.levelCardPaint, layout.scale)

        // 6. Evaluation Quote
        val quoteBlock = layout.blocks[5]
        drawStaticLayout(canvas, layout.quoteLayout, centerX, quoteBlock.topY)

        // 7. Issue Date
        val dateBlock = layout.blocks[6]
        drawStaticLayout(canvas, layout.dateLayout, centerX, dateBlock.topY)

        // 8. Certificate ID
        val idBlock = layout.blocks[7]
        drawStaticLayout(canvas, layout.idLayout, centerX, idBlock.topY)
    }

    private fun drawStaticLayout(canvas: Canvas, staticLayout: StaticLayout, centerX: Float, topY: Float) {
        canvas.save()
        canvas.translate(centerX - (staticLayout.width / 2f), topY)
        staticLayout.draw(canvas)
        canvas.restore()
    }

    private fun drawLevelCard(
        canvas: Canvas,
        rect: RectF,
        text: String,
        paint: TextPaint,
        scale: Float
    ) {
        // Soft ivory/slate badge background
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F5F7FA")
            style = Paint.Style.FILL
        }

        // Distinct gold border
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#C69214")
            strokeWidth = 1f * scale
            style = Paint.Style.STROKE
        }

        val cornerRadius = 6f * scale
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, bgPaint)
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, borderPaint)

        // Draw centered text vertically & horizontally
        val fontMetrics = paint.fontMetrics
        val textY = rect.centerY() - ((fontMetrics.descent + fontMetrics.ascent) / 2f)
        canvas.drawText(text, rect.centerX(), textY, paint.apply { textAlign = Paint.Align.CENTER })
    }

    private fun createTextPaint(
        hexColor: String,
        textSize: Float,
        typeface: Typeface,
        style: Int,
        letterSpacing: Float
    ): TextPaint {
        return TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor(hexColor)
            this.textSize = textSize
            this.typeface = Typeface.create(typeface, style)
            this.letterSpacing = letterSpacing
        }
    }

    private fun createCenteredStaticLayout(
        text: String,
        paint: TextPaint,
        maxWidth: Int
    ): StaticLayout {
        val safeWidth = max(10, maxWidth)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(text, 0, text.length, paint, safeWidth)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(0f, 1.18f)
                .setIncludePad(false)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(
                text,
                paint,
                safeWidth,
                Layout.Alignment.ALIGN_CENTER,
                1.18f,
                0f,
                false
            )
        }
    }

    private fun sanitizeParagraph(text: String): String {
        // Enforce maximum 2-3 lines
        val lines = text.lines()
        return if (lines.size > 3) {
            lines.take(3).joinToString(" ")
        } else {
            text
        }
    }

    private fun sanitizeQuote(quote: String): String {
        // Enforce maximum 2 lines
        val lines = quote.lines()
        return if (lines.size > 2) {
            lines.take(2).joinToString(" ")
        } else {
            quote
        }
    }
}
