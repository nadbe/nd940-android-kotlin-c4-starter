package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var reminderLocalTestRepository: RemindersLocalRepository

    private lateinit var database: RemindersDatabase

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun createDbAndRepository() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()

        reminderLocalTestRepository = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }

    @After
    fun closeDB() {
        database.close()
        GlobalContext.stopKoin()
    }

    @Test
    fun saveReminder_retrieveReminder() = runTest {
        val reminder = ReminderDTO("Alte Pinakothek", "Museum", "Alte Pinakothek", 48.14881 ,11.57142)
        reminderLocalTestRepository.saveReminder(reminder)
        assertThat(reminderLocalTestRepository.getReminder(reminder.id)).isEqualTo(Result.Success(reminder))
    }

    @Test
    fun saveReminders_retrieveReminders() = runTest {
        val reminder1 = ReminderDTO("Alte Pinakothek", "Museum", "Alte Pinakothek", 48.14881 ,11.57142)
        val reminder2 = ReminderDTO("Botanischer Garten", "Garten", "Botanischer Garten", 48.14348 ,11.56767)
        reminderLocalTestRepository.saveReminder(reminder1)
        reminderLocalTestRepository.saveReminder(reminder2)
        val resultList = reminderLocalTestRepository.getReminders() as Result.Success<List<ReminderDTO>>
        assertThat(resultList.data.size).isEqualTo(2)
    }

    @Test
    fun saveReminders_deleteAllAndRetrieveNoReminders() = runTest {
        val reminder1 = ReminderDTO("Alte Pinakothek", "Museum", "Alte Pinakothek", 48.14881 ,11.57142)
        val reminder2 = ReminderDTO("Botanischer Garten", "Garten", "Botanischer Garten", 48.14348 ,11.56767)
        reminderLocalTestRepository.saveReminder(reminder1)
        reminderLocalTestRepository.saveReminder(reminder2)
        val resultList = reminderLocalTestRepository.getReminders() as Result.Success<List<ReminderDTO>>
        assertThat(resultList.data.size).isEqualTo(2)

        reminderLocalTestRepository.deleteAllReminders()
        val emptyResultList = reminderLocalTestRepository.getReminders() as Result.Success<List<ReminderDTO>>
        assertThat(emptyResultList.data).isEmpty()
    }
}