package com.example

import android.content.Context
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
    fun `test text safe zones top margin 120px and bottom margin 180px`() {
        val data = CertificateData()
        val layout = CertificateDynamicLayoutEngine.computeLayout(896, 1200, data)

        val firstBlock = layout.blocks.first()
        assertTrue(
            "First content block top (${firstBlock.topY}) must be >= Top Safe Margin (120px)",
            firstBlock.topY >= 120f
        )

        // Safe zone bottom margin = 180px on 1200px height -> content must be <= 1020px (or bottom footer ID <= 1070px)
        val dateBlock = layout.blocks[6]
        assertTrue(
            "Main text flow must end well within safe zone (Date bottom ${dateBlock.bottomY} < 1020px)",
            dateBlock.bottomY < 1020f
        )
    }

    @Test
    fun `test seal protection text never collides with or overlaps the seal`() {
        val data = CertificateData()
        val layout = CertificateDynamicLayoutEngine.computeLayout(896, 1200, data)

        val sealTop = CertificateDynamicLayoutEngine.BASE_SEAL_TOP_Y // 780px
        val dateBlock = layout.blocks[6] // Last block before seal

        assertTrue(
            "Text block bottom (${dateBlock.bottomY}) must be strictly above seal top ($sealTop)",
            dateBlock.bottomY <= sealTop
        )
    }

    @Test
    fun `test text wrapping within 70 percent max width`() {
        val data = CertificateData(
            studentName = "Very Long Student Name That Should Be Tested For Dynamic Wrapping Across The Width Limit"
        )
        val layout = CertificateDynamicLayoutEngine.computeLayout(896, 1200, data)

        val maxAllowedWidth = 896f * 0.70f
        assertEquals(maxAllowedWidth, layout.maxSafeWidth, 0.5f)
        assertTrue(
            "Name layout width (${layout.nameLayout.width}) must be <= maxAllowedWidth ($maxAllowedWidth)",
            layout.nameLayout.width <= maxAllowedWidth
        )
    }

    @Test
    fun `test certificate bitmap generation is high resolution 1792x2400`() {
        val data = CertificateData(studentName = "Kamlesh Kumar Thakur")
        val bitmap = CertificateGeneratorEngine.generateCertificateBitmap(context, data)

        assertNotNull("Bitmap must not be null", bitmap)
        assertEquals(1792, bitmap.width)
        assertEquals(2400, bitmap.height)
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
    fun `test validation layer detects fixed elements and reports zero collisions on valid layout`() {
        val data = CertificateData(studentName = "Kamlesh Kumar Thakur")
        val layout = CertificateDynamicLayoutEngine.computeLayout(896, 1200, data)

        val report = layout.validationReport
        assertTrue("Validation report must pass", report.passed)
        assertTrue("Collisions list must be empty", report.collisions.isEmpty())

        val fixedElementNames = report.fixedElementsChecked.map { it.name }
        assertTrue("Seal must be in checked fixed elements", fixedElementNames.contains("Seal"))
        assertTrue("Left Signature must be in checked fixed elements", fixedElementNames.contains("Left Signature"))
        assertTrue("Right Signature must be in checked fixed elements", fixedElementNames.contains("Right Signature"))
    }

    @Test
    fun `test validation layer automatically reduces font size when long text threatens overlap`() {
        val veryLongData = CertificateData(
            studentName = "Alexander Montgomery Bartholomew III of Hohenzollern-Sigmaringen",
            studentClass = "Master of Advanced Theoretical Cybernetics & High-Performance Distributed Systems",
            achievementDescription = "IN RECOGNITION OF UNSURPASSED DEDICATION, PERSISTENT DISCIPLINE AND RELENTLESS DRIVE",
            achievementParagraph = "For completely unlocking and triumphantly conquering all challenging benchmarks within the protocol with flawless execution, resilience, and exemplary leadership.",
            aiEvaluation = "\"The protocol strictly rewards focused execution,\nand unwavering adherence to discipline.\""
        )
        val layout = CertificateDynamicLayoutEngine.computeLayout(896, 1200, veryLongData)

        val report = layout.validationReport
        assertTrue("Validation report must pass after auto-reduction", report.passed)
        assertTrue("Collisions must be fully resolved", report.collisions.isEmpty())
        assertTrue("Auto-reductions should be applied to resolve potential collisions", report.autoReductionsApplied >= 1)
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
