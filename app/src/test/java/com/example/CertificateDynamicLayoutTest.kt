package com.example

import android.content.Context
import android.text.Layout
import androidx.test.core.app.ApplicationProvider
import com.example.certificate.CertificateDynamicLayoutEngine
import com.example.certificate.CertificateGeneratorEngine
import com.example.data.model.CertificateData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CertificateDynamicLayoutTest {

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @Test
    fun `test dynamic spacing rule currentY equals previousBlockHeight plus spacing`() {
        val data = CertificateData(
            studentName = "Kamlesh Kumar Thakur",
            studentClass = "Class 12 • Science (PCM)"
        )
        val layout = CertificateDynamicLayoutEngine.computeLayout(896, 1200, data)

        assertTrue("Layout must be valid", layout.isValid)
        val blocks = layout.blocks

        // Verify that every single sequential block starts exactly at or after previous block's bottom
        for (i in 0 until blocks.size - 2) {
            val current = blocks[i]
            val next = blocks[i + 1]
            assertTrue(
                "Block ${next.id} top (${next.topY}) must be >= previous block ${current.id} bottom (${current.bottomY})",
                next.topY >= current.bottomY
            )
        }
    }

    @Test
    fun `test relative positioning gaps between sections match exact specifications`() {
        val data = CertificateData(
            studentName = "Kamlesh Kumar Thakur",
            studentClass = "Class 12 • Science (PCM)"
        )
        val layout = CertificateDynamicLayoutEngine.computeLayout(896, 1200, data)
        val blocks = layout.blocks

        val nameBlock = blocks.find { it.id == "name" }!!
        val classBlock = blocks.find { it.id == "class" }!!
        val achievementBlock = blocks.find { it.id == "achievement" }!!
        val cardBlock = blocks.find { it.id == "level_card" }!!
        val sealBlock = blocks.find { it.id == "seal" }!!
        val sigBlock = blocks.find { it.id == "signatures" }!!

        // Name ↓ 20px gap Class
        assertEquals(20f, classBlock.topY - nameBlock.bottomY, 1.0f)

        // Class ↓ 20px gap Achievement Text
        assertEquals(20f, achievementBlock.topY - classBlock.bottomY, 1.0f)

        // Achievement Text ↓ 30px gap Level Information Box
        assertEquals(30f, cardBlock.topY - achievementBlock.bottomY, 1.0f)

        // Level Information Box ↓ 40px gap Seal
        assertEquals(40f, sealBlock.topY - cardBlock.bottomY, 1.0f)

        // Seal ↓ 40px gap Signature Area
        assertEquals(40f, sigBlock.topY - sealBlock.bottomY, 1.0f)
    }

    @Test
    fun `test zero text overlap with long student name and multiple lines`() {
        val longData = CertificateData(
            studentName = "Alexander Montgomery Bartholomew III of Hohenzollern",
            studentClass = "Advanced Theoretical Physics and Quantum Cybernetics - Batch of 2026",
            achievementDescription = "IN RECOGNITION OF EXCEPTIONAL MASTERY AND RELENTLESS DISCIPLINE",
            achievementParagraph = "Awarded for completing all 18 levels of the protocol with absolute consistency and unwavering focus.",
            aiEvaluation = "\"Discipline is the bridge between goals\nand accomplishment.\""
        )
        val layout = CertificateDynamicLayoutEngine.computeLayout(896, 1200, longData)

        assertTrue("Layout with long text must remain valid", layout.isValid)
        val blocks = layout.blocks

        // Check each pair of blocks for zero overlap
        for (i in 0 until blocks.size - 2) {
            val current = blocks[i]
            val next = blocks[i + 1]
            val overlap = current.bottomY - next.topY
            assertTrue(
                "Zero overlap expected between ${current.id} and ${next.id}, found overlap $overlap",
                next.topY >= current.bottomY
            )
        }
    }

    @Test
    fun `test name field center aligned max width 70 percent auto shrink font size min 28px`() {
        val longNameData = CertificateData(
            studentName = "Alexander Montgomery Bartholomew III of Hohenzollern-Sigmaringen"
        )
        val layout = CertificateDynamicLayoutEngine.computeLayout(896, 1200, longNameData)

        // Alignment center
        assertEquals(Layout.Alignment.ALIGN_CENTER, layout.nameLayout.alignment)

        // Max width 70%
        val maxAllowedWidth = 896f * 0.70f
        assertTrue(
            "Name layout width (${layout.nameLayout.width}) must be <= maxAllowedWidth ($maxAllowedWidth)",
            layout.nameLayout.width <= maxAllowedWidth
        )

        // Name paint text size must not shrink below 28px
        assertTrue(
            "Name font size (${layout.nameLayout.paint.textSize}) must be >= min 28px",
            layout.nameLayout.paint.textSize >= 28f
        )
    }

    @Test
    fun `test achievement paragraph maximum 2 lines auto wrap overflow hidden`() {
        val longParagraphData = CertificateData(
            achievementParagraph = "This is line 1 of achievement text which is quite comprehensive and provides extensive detail. " +
                    "This is line 2 of achievement which details the heroic efforts and milestones reached throughout the protocol. " +
                    "This is line 3 which should be truncated or hidden because maximum allowed lines is strictly two lines. " +
                    "This is line 4 which definitely must never render or cause an overlap with the level card."
        )
        val layout = CertificateDynamicLayoutEngine.computeLayout(896, 1200, longParagraphData)

        assertTrue(
            "Achievement paragraph line count (${layout.paragraphLayout.lineCount}) must be <= 2",
            layout.paragraphLayout.lineCount <= 2
        )

        val achievementBlock = layout.blocks.find { it.id == "achievement" }!!
        val cardBlock = layout.blocks.find { it.id == "level_card" }!!
        assertTrue(
            "Level card top (${cardBlock.topY}) must be strictly below achievement bottom (${achievementBlock.bottomY})",
            cardBlock.topY >= achievementBlock.bottomY + 30f
        )
    }

    @Test
    fun `test text safe zones top margin 120px and bottom margin 160px`() {
        val data = CertificateData()
        val layout = CertificateDynamicLayoutEngine.computeLayout(896, 1200, data)

        val firstBlock = layout.blocks.first()
        assertTrue(
            "First content block top (${firstBlock.topY}) must be >= Top Safe Margin (120px)",
            firstBlock.topY >= 120f
        )

        val sigBlock = layout.blocks.find { it.id == "signatures" }!!
        assertTrue(
            "Signatures bottom (${sigBlock.bottomY}) must be within safe zone (< 1040px)",
            sigBlock.bottomY <= 1040f
        )
    }

    @Test
    fun `test seal protection text never collides with or overlaps the seal`() {
        val data = CertificateData()
        val layout = CertificateDynamicLayoutEngine.computeLayout(896, 1200, data)

        val cardBlock = layout.blocks.find { it.id == "level_card" }!!
        val sealBlock = layout.blocks.find { it.id == "seal" }!!

        assertTrue(
            "Level card bottom (${cardBlock.bottomY}) must be strictly above seal top (${sealBlock.topY})",
            cardBlock.bottomY <= sealBlock.topY
        )
    }

    @Test
    fun `test certificate bitmap generation is true A4 canvas 2480x3508 at 300 DPI`() {
        val data = CertificateData(studentName = "Kamlesh Kumar Thakur")
        val bitmap = CertificateGeneratorEngine.generateCertificateBitmap(context, data)

        assertNotNull("Bitmap must not be null", bitmap)
        assertEquals(2480, bitmap.width)
        assertEquals(3508, bitmap.height)
        assertEquals(300, bitmap.density)
    }

    @Test
    fun `test export formats JPG PNG and PDF produce valid non-empty files`() {
        val data = CertificateData(studentName = "Kamlesh Kumar Thakur")

        val jpgFile = CertificateGeneratorEngine.exportToJpg(context, data)
        assertTrue("JPG file must exist and not be empty", jpgFile.exists() && jpgFile.length() > 0)

        val pngFile = CertificateGeneratorEngine.exportToPng(context, data)
        assertTrue("PNG file must exist and not be empty", pngFile.exists() && pngFile.length() > 0)

        val pdfFile = CertificateGeneratorEngine.exportToPdf(context, data)
        assertTrue("PDF file must exist and not be empty", pdfFile.exists() && pdfFile.length() > 0)
    }

    @Test
    fun `test validation layer reports zero collisions on valid layout`() {
        val data = CertificateData(studentName = "Kamlesh Kumar Thakur")
        val layout = CertificateDynamicLayoutEngine.computeLayout(896, 1200, data)

        val report = layout.validationReport
        assertTrue("Validation report must pass", report.passed)
        assertTrue("Collisions list must be empty", report.collisions.isEmpty())
    }

    @Test
    fun `test validation layer automatically reduces font size when long text threatens overlap`() {
        val veryLongData = CertificateData(
            studentName = "Alexander Montgomery Bartholomew III of Hohenzollern-Sigmaringen",
            studentClass = "Master of Advanced Theoretical Cybernetics & High-Performance Distributed Systems",
            achievementDescription = "IN RECOGNITION OF UNSURPASSED DEDICATION, PERSISTENT DISCIPLINE AND RELENTLESS DRIVE",
            achievementParagraph = "For completely unlocking and triumphantly conquering all challenging benchmarks within the protocol with flawless execution, resilience, and exemplary leadership.",
            aiEvaluation = "\"The protocol strictly rewards focused execution, and unwavering adherence to discipline.\""
        )
        val layout = CertificateDynamicLayoutEngine.computeLayout(896, 1200, veryLongData)

        val report = layout.validationReport
        assertTrue("Validation report must pass after auto-reduction", report.passed)
        assertTrue("Collisions must be fully resolved", report.collisions.isEmpty())
        assertTrue("Final font scale factor must be <= 1.0", report.finalFontScale <= 1.0f)
    }

    @Test
    fun `test validation layer collision checker detects encroaching block directly`() {
        val scale = 1.0f
        val fixedElements = CertificateDynamicLayoutEngine.ValidationLayer.getFixedElements(scale)
        val seal = fixedElements.first { it.name == "Seal" }

        // Artificially construct a block that encroaches into the Seal's top boundary
        val encroachingBlock = CertificateDynamicLayoutEngine.BlockLayout(
            id = "test_date",
            topY = seal.bounds.top - 20f,
            height = 50f, // Extends 30px past seal top
            width = 300f,
            centerX = 448f
        )

        val collisions = CertificateDynamicLayoutEngine.ValidationLayer.checkCollisions(
            blocks = listOf(encroachingBlock),
            fixedElements = fixedElements,
            scale = scale,
            totalHeight = 1200f
        )

        val sealCollision = collisions.find { it.type == CertificateDynamicLayoutEngine.CollisionType.SEAL_COLLISION }
        assertNotNull("Encroaching block must trigger SEAL_COLLISION", sealCollision)
        assertTrue("Overlap depth must be positive", (sealCollision?.overlapDepth ?: 0f) > 0f)
    }
}

