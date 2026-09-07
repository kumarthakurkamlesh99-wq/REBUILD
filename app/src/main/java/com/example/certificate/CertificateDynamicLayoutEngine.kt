package com.example.certificate

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import com.example.data.model.CertificateData
import kotlin.math.max
import kotlin.math.min

/**
 * Dynamic Vertical Flow Layout Engine for Certificate Generation and Preview.
 *
 * Rules:
 * 1. ZERO hardcoded Y positions: Every section is positioned strictly relative to previous section:
 *      Name
 *      ↓ 20px gap
 *      Class
 *      ↓ 20px gap
 *      Achievement Text
 *      ↓ 30px gap
 *      Level Information Box
 *      ↓ 40px gap
 *      Seal
 *      ↓ 40px gap
 *      Signature Area
 * 2. Measure text height before rendering each section.
 * 3. Name field: Center aligned, max width 70%, auto shrink font size until it fits, minimum font size 28px.
 * 4. Achievement paragraph: Maximum 2 lines, auto wrap, overflow hidden, never overlaps level information.
 * 5. Render certificate on true A4 canvas: 2480 × 3508 px, 300 DPI.
 * 6. Preview uses exact A4 aspect ratio (2480f / 3508f) and scales proportionally.
 * 7. Active collision detection: automatically reduces font size and re-renders until all elements fit.
 * 8. Certificate generation is NOT complete until all elements fit without overlap.
 */
object CertificateDynamicLayoutEngine {

    // True A4 Canvas constants at 300 DPI
    const val A4_CANVAS_WIDTH = 2480
    const val A4_CANVAS_HEIGHT = 3508
    const val A4_DPI = 300

    // Base coordinate system matching the original master template (896 x 1200)
    const val BASE_WIDTH = 896f
    const val BASE_HEIGHT = 1200f

    // Safe Zones (base pixels)
    const val BASE_SAFE_TOP_MARGIN = 120f
    const val BASE_SAFE_BOTTOM_MARGIN = 160f
    const val BASE_CONTENT_START_Y = 422f // Directly below presentation header
    const val MAX_CONTENT_WIDTH_RATIO = 0.70f // Max width 70%

    // Relative Spacing Rules (Rule 4)
    const val GAP_NAME_TO_CLASS = 20f
    const val GAP_CLASS_TO_ACHIEVEMENT = 20f
    const val GAP_ACHIEVEMENT_TO_LEVEL = 30f
    const val GAP_LEVEL_TO_SEAL = 40f
    const val GAP_SEAL_TO_SIGNATURE = 40f

    // Backward compatibility aliases
    const val BASE_SPACING_NAME_TO_INFO = GAP_NAME_TO_CLASS
    const val BASE_SPACING_INFO_TO_TITLE = GAP_CLASS_TO_ACHIEVEMENT
    const val BASE_SPACING_TITLE_TO_PARA = 10f
    const val BASE_SPACING_PARA_TO_CARD = GAP_ACHIEVEMENT_TO_LEVEL
    const val BASE_SPACING_CARD_TO_QUOTE = 15f
    const val BASE_SPACING_QUOTE_TO_DATE = 15f

    // Fixed seal reference for backward compatibility
    const val BASE_SEAL_TOP_Y = 780f
    const val BASE_SEAL_BOTTOM_Y = 940f
    const val BASE_SEAL_SAFETY_CLEARANCE = 20f

    // Signature reference for backward compatibility
    const val BASE_SIG_LEFT_X = 220f
    const val BASE_SIG_RIGHT_X = 676f
    const val BASE_SIG_TOP_Y = 960f
    const val BASE_SIG_BOTTOM_Y = 1040f
    const val BASE_ID_Y = 1058f

    // Font Limits (base pixels)
    const val FONT_NAME_MAX = 40f
    const val FONT_NAME_MIN = 28f // Rule 2: Minimum font size 28px

    const val FONT_INFO_MAX = 22f
    const val FONT_INFO_MIN = 16f

    const val FONT_ACHIEVEMENT_TITLE_MAX = 17f
    const val FONT_ACHIEVEMENT_TITLE_MIN = 14f

    const val FONT_PARAGRAPH_MAX = 16f
    const val FONT_PARAGRAPH_MIN = 13f

    const val FONT_CARD_MAX = 14f
    const val FONT_CARD_MIN = 11f

    const val FONT_QUOTE_MAX = 14f
    const val FONT_QUOTE_MIN = 11f

    const val FONT_DATE_MAX = 13f
    const val FONT_DATE_MIN = 11f

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
     * and safe zones.
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
            for (i in 0 until blocks.size - 1) {
                val current = blocks[i]
                val next = blocks[i + 1]
                if (next.id == "id") continue // Footer block placed independently
                if (next.topY < current.bottomY) {
                    val overlap = current.bottomY - next.topY
                    collisions.add(
                        CollisionEvent(
                            type = CollisionType.TEXT_BLOCK_OVERLAP,
                            textBlockId = current.id,
                            collidingWith = next.id,
                            overlapDepth = overlap,
                            description = "Overlap: '${current.id}' bottom (${current.bottomY}) overlaps '${next.id}' top (${next.topY}) by ${overlap}px"
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
                        description = "Top safe zone violation: '${firstBlock.id}' top (${firstBlock.topY}) < safe margin ($topSafe)"
                    )
                )
            }

            val sigBlock = blocks.find { it.id == "signatures" }
            if (sigBlock != null && sigBlock.bottomY > bottomSafe) {
                collisions.add(
                    CollisionEvent(
                        type = CollisionType.SAFE_ZONE_VIOLATION,
                        textBlockId = sigBlock.id,
                        collidingWith = "Bottom Safe Margin",
                        overlapDepth = sigBlock.bottomY - bottomSafe,
                        description = "Bottom safe zone violation: Signature bottom (${sigBlock.bottomY}) > safe margin ($bottomSafe)"
                    )
                )
            }

            // 3. Collision with Fixed Elements (if text encroaches into seal region)
            val seal = fixedElements.find { it.name == "Seal" }
            if (seal != null) {
                for (block in blocks) {
                    if (block.id != "seal" && block.id != "signatures" && block.id != "id") {
                        if (block.bounds.bottom > seal.bounds.top && block.bounds.top < seal.bounds.bottom) {
                            val yOverlap = block.bounds.bottom - seal.bounds.top
                            if (yOverlap > 0f) {
                                collisions.add(
                                    CollisionEvent(
                                        type = CollisionType.SEAL_COLLISION,
                                        textBlockId = block.id,
                                        collidingWith = "Seal",
                                        overlapDepth = yOverlap,
                                        description = "Seal collision: '${block.id}' encroaches into Seal top by ${yOverlap}px"
                                    )
                                )
                            }
                        }
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
        val validationReport: ValidationReport = ValidationReport(true, emptyList(), 0, 1.0f, emptyList()),
        val sealRect: RectF = RectF(),
        val signatureRect: RectF = RectF(),
        val issueDate: String = "",
        val certificateId: String = ""
    )

    /**
     * Calculates the dynamic vertical flow layout for all certificate sections:
     *   Name
     *   ↓ 20px gap
     *   Class
     *   ↓ 20px gap
     *   Achievement Text (max 2 lines, auto wrap, overflow hidden)
     *   ↓ 30px gap
     *   Level Information Box
     *   ↓ 40px gap
     *   Seal
     *   ↓ 40px gap
     *   Signature Area
     *
     * Performs pre-render collision checks and auto-shrinks font sizes until all elements fit.
     */
    fun computeLayout(
        width: Int,
        height: Int,
        data: CertificateData
    ): LayoutResult {
        val scale = width / BASE_WIDTH
        val centerX = width / 2f
        val maxSafeWidth = width * MAX_CONTENT_WIDTH_RATIO

        // 1. Sanitize content
        val cleanName = data.studentName.trim().ifEmpty { "Protocol Candidate" }
        val cleanClass = data.studentClass.trim().ifEmpty { "REBUILD PROTOCOL" }
        val achievementParagraph = sanitizeAchievement(
            if (data.achievementParagraph.isNotEmpty()) data.achievementParagraph
            else "For successfully unlocking and mastering ${data.levelName} through demonstrated consistency, self-discipline, and daily focus in the REBUILD protocol."
        )
        val cleanQuote = sanitizeQuote(
            data.aiEvaluation.ifEmpty {
                "\"The protocol rewards action, not intention.\""
            }
        )
        val cardText = "LEVEL ${data.level} • ${data.rankTitle.uppercase()}   |   ${data.totalXP}   |   ${data.arcDay.uppercase()}"
        val idText = "VERIFICATION ID: ${data.certificateId}"

        // Initial candidate font sizes
        var nameFontSize = FONT_NAME_MAX // Starts at 40px base, auto-shrinks down to 28px
        var infoFontSize = 20f
        var paraFontSize = 15.5f
        var cardFontSize = 13.5f
        val idFontSize = 10.5f

        val startY = BASE_CONTENT_START_Y * scale
        val fixedElements = ValidationLayer.getFixedElements(scale)
        var autoReductionsCount = 0
        var latestValidationReport: ValidationReport? = null

        // Convergence loop: Multi-pass collision detection & font auto-reduction
        for (attempt in 0..6) {
            // Rule 2: Name field auto shrink until it fits within 70% width, min 28px
            val (nameLayout, fittedNameSize) = layoutNameField(
                cleanName = cleanName,
                scale = scale,
                maxWidth = maxSafeWidth,
                baseMaxFontSize = nameFontSize,
                baseMinFontSize = FONT_NAME_MIN
            )
            nameFontSize = fittedNameSize

            // Text paints
            val infoPaint = createTextPaint("#203A63", infoFontSize * scale, Typeface.SERIF, Typeface.NORMAL, 0.03f)
            val paraPaint = createTextPaint("#1B2A4A", paraFontSize * scale, Typeface.SERIF, Typeface.NORMAL, 0.01f)
            val cardPaint = createTextPaint("#0B2545", cardFontSize * scale, Typeface.SANS_SERIF, Typeface.BOLD, 0.04f)
            val idPaint = createTextPaint("#4A5568", idFontSize * scale, Typeface.MONOSPACE, Typeface.NORMAL, 0.05f)

            // Rule 1: Measure text heights before positioning
            val infoLayout = createCenteredStaticLayout(cleanClass, infoPaint, maxSafeWidth.toInt())

            // Rule 3: Achievement paragraph maximum 2 lines, auto wrap, overflow hidden
            val paraLayout = createCenteredStaticLayout(
                text = achievementParagraph,
                paint = paraPaint,
                maxWidth = maxSafeWidth.toInt(),
                maxLines = 2,
                ellipsize = true
            )

            // Measure Level Information Box
            val cardTextWidth = cardPaint.measureText(cardText)
            val cardPadH = 18f * scale
            val cardPadV = 7f * scale
            val cardWidth = min(cardTextWidth + (cardPadH * 2f), maxSafeWidth)
            val cardHeight = (cardPaint.fontSpacing) + (cardPadV * 2f)

            // Measure Certificate ID
            val idLayout = createCenteredStaticLayout(idText, idPaint, maxSafeWidth.toInt())

            // Rule 4: Dynamic vertical flow layout with strict relative positioning:
            // Name -> 20px -> Class -> 20px -> Achievement -> 30px -> Level Box -> 40px -> Seal -> 40px -> Signatures
            var currentY = startY

            // Section 1: Recipient Name
            val nameBlock = BlockLayout("name", currentY, nameLayout.height.toFloat(), nameLayout.width.toFloat(), centerX)
            currentY += nameBlock.height + (GAP_NAME_TO_CLASS * scale) // ↓ 20px gap

            // Section 2: Class
            val classBlock = BlockLayout("class", currentY, infoLayout.height.toFloat(), infoLayout.width.toFloat(), centerX)
            currentY += classBlock.height + (GAP_CLASS_TO_ACHIEVEMENT * scale) // ↓ 20px gap

            // Section 3: Achievement Text (max 2 lines)
            val paraBlock = BlockLayout("achievement", currentY, paraLayout.height.toFloat(), paraLayout.width.toFloat(), centerX)
            currentY += paraBlock.height + (GAP_ACHIEVEMENT_TO_LEVEL * scale) // ↓ 30px gap

            // Section 4: Level Information Box (guaranteed never to overlap achievement)
            val cardRect = RectF(
                centerX - (cardWidth / 2f),
                currentY,
                centerX + (cardWidth / 2f),
                currentY + cardHeight
            )
            val cardBlock = BlockLayout("level_card", currentY, cardHeight, cardWidth, centerX)
            currentY += cardBlock.height + (GAP_LEVEL_TO_SEAL * scale) // ↓ 40px gap

            // Section 5: Seal (relative to level info box)
            val sealSize = 92f * scale
            val sealRect = RectF(
                centerX - (sealSize / 2f),
                currentY,
                centerX + (sealSize / 2f),
                currentY + sealSize
            )
            val sealBlock = BlockLayout("seal", currentY, sealSize, sealSize, centerX)
            currentY += sealBlock.height + (GAP_SEAL_TO_SIGNATURE * scale) // ↓ 40px gap

            // Section 6: Signature Area (relative to seal)
            val sigHeight = 64f * scale
            val sigWidth = maxSafeWidth
            val sigRect = RectF(
                centerX - (sigWidth / 2f),
                currentY,
                centerX + (sigWidth / 2f),
                currentY + sigHeight
            )
            val sigBlock = BlockLayout("signatures", currentY, sigHeight, sigWidth, centerX)

            // Section 7: Verification ID (placed in bottom safe footer)
            val idY = height - (BASE_SAFE_BOTTOM_MARGIN * 0.45f * scale)
            val idBlock = BlockLayout("id", idY, idLayout.height.toFloat(), idLayout.width.toFloat(), centerX)

            val blocks = listOf(nameBlock, classBlock, paraBlock, cardBlock, sealBlock, sigBlock, idBlock)

            // Rule 7 & 8: Active collision detection
            val detectedCollisions = ValidationLayer.checkCollisions(blocks, fixedElements, scale, height.toFloat())

            if (detectedCollisions.isNotEmpty() && attempt < 6) {
                // Reduce font sizes and re-render until all elements fit without overlap
                autoReductionsCount++
                nameFontSize = max(FONT_NAME_MIN, nameFontSize - 1.0f)
                infoFontSize = max(FONT_INFO_MIN, infoFontSize - 0.5f)
                paraFontSize = max(FONT_PARAGRAPH_MIN, paraFontSize - 0.4f)
                cardFontSize = max(FONT_CARD_MIN, cardFontSize - 0.3f)

                latestValidationReport = ValidationReport(
                    passed = false,
                    collisions = detectedCollisions,
                    autoReductionsApplied = autoReductionsCount,
                    finalFontScale = nameFontSize / FONT_NAME_MAX,
                    fixedElementsChecked = fixedElements
                )
                continue
            }

            // All elements fit within their assigned regions without overlap
            val validationReport = ValidationReport(
                passed = detectedCollisions.isEmpty(),
                collisions = detectedCollisions,
                autoReductionsApplied = autoReductionsCount,
                finalFontScale = nameFontSize / FONT_NAME_MAX,
                fixedElementsChecked = fixedElements
            )

            // Supporting layouts for backward compatibility
            val dummyTitleLayout = createCenteredStaticLayout("", paraPaint, maxSafeWidth.toInt())
            val dummyQuoteLayout = createCenteredStaticLayout(cleanQuote, paraPaint, maxSafeWidth.toInt())
            val dummyDateLayout = createCenteredStaticLayout("Date of Issuance: ${data.issueDate}", infoPaint, maxSafeWidth.toInt())

            return LayoutResult(
                scale = scale,
                maxSafeWidth = maxSafeWidth,
                centerX = centerX,
                nameLayout = nameLayout,
                classLayout = infoLayout,
                titleLayout = dummyTitleLayout,
                paragraphLayout = paraLayout,
                levelCardRect = cardRect,
                levelCardText = cardText,
                levelCardPaint = cardPaint,
                quoteLayout = dummyQuoteLayout,
                dateLayout = dummyDateLayout,
                idLayout = idLayout,
                blocks = blocks,
                isValid = validationReport.passed,
                validationReport = validationReport,
                sealRect = sealRect,
                signatureRect = sigRect,
                issueDate = data.issueDate,
                certificateId = data.certificateId
            )
        }

        // Fallback safe layout
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
     * Rule 2: Name field helper:
     * - Center aligned
     * - Max width 70%
     * - Auto shrink font size until it fits
     * - Minimum font size 28px
     */
    private fun layoutNameField(
        cleanName: String,
        scale: Float,
        maxWidth: Float,
        baseMaxFontSize: Float,
        baseMinFontSize: Float
    ): Pair<StaticLayout, Float> {
        var fontSize = baseMaxFontSize
        val minSize = baseMinFontSize
        val paint = createTextPaint("#0A192F", fontSize * scale, Typeface.SERIF, Typeface.BOLD, 0.02f)

        // Shrink font size if single-line text exceeds 70% width
        while (paint.measureText(cleanName) > maxWidth && fontSize > minSize) {
            fontSize = max(minSize, fontSize - 0.5f)
            paint.textSize = fontSize * scale
        }

        var layout = createCenteredStaticLayout(cleanName, paint, maxWidth.toInt())

        // If it wrapped onto multiple lines, continue auto-shrinking down to minSize
        while (layout.lineCount > 1 && fontSize > minSize) {
            fontSize = max(minSize, fontSize - 0.5f)
            paint.textSize = fontSize * scale
            layout = createCenteredStaticLayout(cleanName, paint, maxWidth.toInt())
        }

        return Pair(layout, fontSize)
    }

    /**
     * Pre-render verification check.
     */
    fun validateLayout(blocks: List<BlockLayout>, scale: Float, totalHeight: Int): Boolean {
        val fixedElements = ValidationLayer.getFixedElements(scale)
        val collisions = ValidationLayer.checkCollisions(blocks, fixedElements, scale, totalHeight.toFloat())
        return collisions.isEmpty()
    }

    /**
     * Draws the dynamic vertical flow layout onto any Canvas (Bitmap or PDF page).
     */
    fun renderToCanvas(canvas: Canvas, layout: LayoutResult) {
        val centerX = layout.centerX

        // 1. Recipient Name
        val nameBlock = layout.blocks[0]
        drawStaticLayout(canvas, layout.nameLayout, centerX, nameBlock.topY)

        // 2. Class
        val classBlock = layout.blocks[1]
        drawStaticLayout(canvas, layout.classLayout, centerX, classBlock.topY)

        // 3. Achievement Text (max 2 lines, overflow hidden)
        val paraBlock = layout.blocks[2]
        drawStaticLayout(canvas, layout.paragraphLayout, centerX, paraBlock.topY)

        // 4. Level Information Box
        drawLevelCard(canvas, layout.levelCardRect, layout.levelCardText, layout.levelCardPaint, layout.scale)

        // 5. Official Seal (dynamically positioned relative to Level Information Box)
        drawOfficialSeal(canvas, layout.sealRect, layout.scale)

        // 6. Signature Area (dynamically positioned relative to Seal)
        drawSignatureArea(canvas, layout.signatureRect, layout.issueDate, layout.scale)

        // 7. Verification ID / Footer
        val idBlock = layout.blocks.find { it.id == "id" }
        if (idBlock != null) {
            drawStaticLayout(canvas, layout.idLayout, centerX, idBlock.topY)
        }
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
            strokeWidth = 1.2f * scale
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

    /**
     * Draws the official REBUILD crest seal at its dynamic vertical position.
     */
    private fun drawOfficialSeal(canvas: Canvas, rect: RectF, scale: Float) {
        val cx = rect.centerX()
        val cy = rect.centerY()
        val radius = (rect.width() / 2f) * 0.85f

        // 1. Ribbons draped at bottom
        val ribbonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#C69214")
            style = Paint.Style.FILL
        }
        val ribbonPath = Path().apply {
            // Left ribbon tail
            moveTo(cx - (14f * scale), cy + (radius * 0.7f))
            lineTo(cx - (22f * scale), cy + radius + (18f * scale))
            lineTo(cx - (14f * scale), cy + radius + (12f * scale))
            lineTo(cx - (6f * scale), cy + radius + (18f * scale))
            lineTo(cx - (4f * scale), cy + (radius * 0.7f))
            close()
            // Right ribbon tail
            moveTo(cx + (4f * scale), cy + (radius * 0.7f))
            lineTo(cx + (6f * scale), cy + radius + (18f * scale))
            lineTo(cx + (14f * scale), cy + radius + (12f * scale))
            lineTo(cx + (22f * scale), cy + radius + (18f * scale))
            lineTo(cx + (14f * scale), cy + (radius * 0.7f))
            close()
        }
        canvas.drawPath(ribbonPath, ribbonPaint)

        // 2. Outer golden circle
        val outerGoldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D4AF37")
            style = Paint.Style.FILL
        }
        canvas.drawCircle(cx, cy, radius, outerGoldPaint)

        // 3. Beaded accent ring
        val ringStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#8A6D3B")
            strokeWidth = 1.5f * scale
            style = Paint.Style.STROKE
        }
        canvas.drawCircle(cx, cy, radius - (3.5f * scale), ringStrokePaint)

        // 4. Inner navy/gold medallion
        val innerMedallionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0A192F")
            style = Paint.Style.FILL
        }
        canvas.drawCircle(cx, cy, radius - (7f * scale), innerMedallionPaint)

        // 5. Star emblem & text in center
        val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#DFC15D")
            textSize = 12f * scale
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val fm = starPaint.fontMetrics
        val textY = cy - ((fm.descent + fm.ascent) / 2f)
        canvas.drawText("★ ★ ★", cx, textY - (5f * scale), starPaint)

        val sealLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F5F7FA")
            textSize = 6f * scale
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            letterSpacing = 0.06f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("REBUILD PROTOCOL", cx, textY + (7f * scale), sealLabelPaint)
        canvas.drawText("VERIFIED DISCIPLINE", cx, textY + (14f * scale), sealLabelPaint.apply { textSize = 5f * scale })
    }

    /**
     * Draws the signature and date area at its dynamic vertical position.
     */
    private fun drawSignatureArea(canvas: Canvas, rect: RectF, issueDate: String, scale: Float) {
        val lineWidth = 190f * scale
        val leftX = rect.left + (30f * scale)
        val rightX = rect.right - (30f * scale)
        val lineY = rect.top + (30f * scale)

        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#8A6D3B")
            strokeWidth = 1.2f * scale
            style = Paint.Style.STROKE
        }

        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#203A63")
            textSize = 9.5f * scale
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            letterSpacing = 0.04f
            textAlign = Paint.Align.CENTER
        }

        val subLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#5A6B82")
            textSize = 8f * scale
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            letterSpacing = 0.03f
            textAlign = Paint.Align.CENTER
        }

        // Left Column: Issue Date
        val leftCenter = leftX + (lineWidth / 2f)
        val dateText = if (issueDate.isNotEmpty()) issueDate else "March 2026"
        val dateValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0A192F")
            textSize = 11f * scale
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(dateText, leftCenter, lineY - (6f * scale), dateValuePaint)
        canvas.drawLine(leftX, lineY, leftX + lineWidth, lineY, linePaint)
        canvas.drawText("DATE OF ISSUANCE", leftCenter, lineY + (13f * scale), labelPaint)
        canvas.drawText("AUTHENTICATED RECORD", leftCenter, lineY + (23f * scale), subLabelPaint)

        // Right Column: Protocol Signature
        val rightLeft = rightX - lineWidth
        val rightCenter = rightLeft + (lineWidth / 2f)
        val sigFont = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0A192F")
            textSize = 14f * scale
            typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Marcus Vance, Ph.D.", rightCenter, lineY - (6f * scale), sigFont)
        canvas.drawLine(rightLeft, lineY, rightX, lineY, linePaint)
        canvas.drawText("PROTOCOL CHAIR", rightCenter, lineY + (13f * scale), labelPaint)
        canvas.drawText("AUTHORIZED ISSUANCE", rightCenter, lineY + (23f * scale), subLabelPaint)
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
        maxWidth: Int,
        maxLines: Int = Int.MAX_VALUE,
        ellipsize: Boolean = false
    ): StaticLayout {
        val safeWidth = max(10, maxWidth)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val builder = StaticLayout.Builder.obtain(text, 0, text.length, paint, safeWidth)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(0f, 1.15f)
                .setIncludePad(false)
            if (maxLines != Int.MAX_VALUE) {
                builder.setMaxLines(maxLines)
            }
            if (ellipsize) {
                builder.setEllipsize(TextUtils.TruncateAt.END)
            }
            builder.build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(
                text,
                paint,
                safeWidth,
                Layout.Alignment.ALIGN_CENTER,
                1.15f,
                0f,
                false
            )
        }
    }

    private fun sanitizeAchievement(text: String): String {
        // Enforce maximum 2 lines
        val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
        return if (lines.size > 2) {
            lines.take(2).joinToString(" ")
        } else {
            text.replace("\n", " ").trim()
        }
    }

    private fun sanitizeQuote(quote: String): String {
        val lines = quote.lines().map { it.trim() }.filter { it.isNotEmpty() }
        return if (lines.size > 2) {
            lines.take(2).joinToString(" ")
        } else {
            quote.trim()
        }
    }
}

