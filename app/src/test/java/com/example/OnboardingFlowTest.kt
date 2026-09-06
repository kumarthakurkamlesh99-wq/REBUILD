package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class OnboardingFlowTest {

    private lateinit var context: Context

    @Before
    fun setUp() = runBlocking {
        context = ApplicationProvider.getApplicationContext()
        val userPrefsRepo = UserPreferencesRepository(context)
        userPrefsRepo.clearOnboardingState()
    }

    @Test
    fun test1_freshInstall_showsOnboarding() = runBlocking {
        // Given: Fresh Install (no preferences set)
        val userPrefsRepo = UserPreferencesRepository(context)

        // Then: onboarding_completed must be false
        val isCompletedSync = userPrefsRepo.isOnboardingCompletedSync()
        val isCompletedFlow = userPrefsRepo.isOnboardingCompleted.first()

        assertFalse("Fresh install must have onboarding_completed = false (sync)", isCompletedSync)
        assertFalse("Fresh install must have onboarding_completed = false (flow)", isCompletedFlow)

        val navigationTarget = if (isCompletedSync) "Dashboard" else "Onboarding"
        assertEquals("Onboarding", navigationTarget)
    }

    @Test
    fun test2_completeOnboarding_opensDashboard() = runBlocking {
        val userPrefsRepo = UserPreferencesRepository(context)

        // When: User completes onboarding
        userPrefsRepo.setOnboardingCompleted(true)

        // Then: Status is permanently saved as true
        assertTrue("Completed onboarding must have onboarding_completed = true (sync)", userPrefsRepo.isOnboardingCompletedSync())
        assertTrue("Completed onboarding must have onboarding_completed = true (flow)", userPrefsRepo.isOnboardingCompleted.first())

        val navigationTarget = if (userPrefsRepo.isOnboardingCompletedSync()) "Dashboard" else "Onboarding"
        assertEquals("Dashboard", navigationTarget)
    }

    @Test
    fun test3_closeAppAndReopen_skipsOnboarding() = runBlocking {
        // App session 1: complete onboarding
        val firstSessionRepo = UserPreferencesRepository(context)
        firstSessionRepo.setOnboardingCompleted(true)

        // App session 2 (simulate process close and re-launch):
        val secondSessionRepo = UserPreferencesRepository(context)

        assertTrue("After restart, onboarding must remain completed", secondSessionRepo.isOnboardingCompletedSync())
        val navigationTarget = if (secondSessionRepo.isOnboardingCompletedSync()) "Dashboard" else "Onboarding"
        assertEquals("Dashboard", navigationTarget)
    }

    @Test
    fun test4_restartPhone_skipsOnboarding() = runBlocking {
        // Phone session 1: completed
        val repo1 = UserPreferencesRepository(context)
        repo1.setOnboardingCompleted(true)

        // Simulate phone reboot (new instance loading from persistent disk)
        val repo2 = UserPreferencesRepository(context)
        assertTrue("Persistent storage must survive phone restart", repo2.isOnboardingCompletedSync())
        val navigationTarget = if (repo2.isOnboardingCompletedSync()) "Dashboard" else "Onboarding"
        assertEquals("Dashboard", navigationTarget)
    }

    @Test
    fun test5_clearAppData_showsOnboardingAgain() = runBlocking {
        // Given: user completed onboarding earlier
        val repo = UserPreferencesRepository(context)
        repo.setOnboardingCompleted(true)
        assertTrue(repo.isOnboardingCompletedSync())

        // When: user clears app data (wipes persistent storage)
        repo.clearOnboardingState()

        // Then: onboarding appears again
        val resetRepo = UserPreferencesRepository(context)
        assertFalse("Clearing app data must reset onboarding_completed to false", resetRepo.isOnboardingCompletedSync())
        val navigationTarget = if (resetRepo.isOnboardingCompletedSync()) "Dashboard" else "Onboarding"
        assertEquals("Onboarding", navigationTarget)
    }
}
