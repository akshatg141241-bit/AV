package com.aryaleelastudios.astroveda

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.aryaleelastudios.astroveda.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OnboardingLaunchTest {

    @get:Rule
    val tmpFolder = TemporaryFolder()

    @Test
    fun firstLaunchRequiresOnboarding_secondColdLaunchSkipsOnboarding() = runBlocking {
        val prefsFile = File(tmpFolder.newFolder("ds"), "astroveda_onboarding.preferences_pb")

        // First cold launch: onboardingComplete defaults to false, unused flag defaults to true
        val firstJob = SupervisorJob()
        val firstScope = CoroutineScope(firstJob + Dispatchers.IO)
        val firstDataStore = PreferenceDataStoreFactory.create(
            scope = firstScope,
            produceFile = { prefsFile }
        )
        val firstRepo = UserPreferencesRepository(firstDataStore)

        assertFalse(
            "Fresh install should start with onboardingComplete = false",
            firstRepo.onboardingCompleteFlow.first()
        )
        assertTrue(
            "Unused isPremiumUnlocked flag must default to true",
            firstRepo.isPremiumUnlockedFlow.first()
        )
        assertEquals(
            "Default notification time should be 6:00 AM",
            Pair(6, 0),
            firstRepo.notificationTimeFlow.first()
        )

        // Complete onboarding steps (language, name, onboardingComplete = true)
        firstRepo.setLanguage("hi")
        firstRepo.setUserName("Aarav")
        firstRepo.setOnboardingComplete(true)

        assertTrue(firstRepo.onboardingCompleteFlow.first())

        // Shut down first DataStore scope cleanly to simulate process exit
        firstScope.cancel()
        firstJob.join()

        // Second cold launch: fresh DataStore instance reading persisted preferences file
        val secondJob = SupervisorJob()
        val secondScope = CoroutineScope(secondJob + Dispatchers.IO)
        try {
            val secondDataStore = PreferenceDataStoreFactory.create(
                scope = secondScope,
                produceFile = { prefsFile }
            )
            val secondRepo = UserPreferencesRepository(secondDataStore)

            assertTrue(
                "Second cold launch must skip onboarding when onboardingComplete was persisted",
                secondRepo.onboardingCompleteFlow.first()
            )
            assertEquals("hi", secondRepo.languageFlow.first())
            assertEquals("Aarav", secondRepo.userNameFlow.first())
        } finally {
            secondScope.cancel()
            secondJob.join()
        }
    }
}
