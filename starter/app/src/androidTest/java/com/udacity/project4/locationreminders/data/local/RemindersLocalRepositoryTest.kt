package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.MyApp
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.Koin
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest :KoinTest{

    private val reminderLocalTestRepository: RemindersLocalRepository by inject()

    private val database: RemindersDatabase by inject()

    private  val koinTestModules = module {

        single {
             Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                RemindersDatabase::class.java
            ).allowMainThreadQueries().build()
        }
        single{ RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)}
    }

    @Before
    fun init() {
        stopKoin()
        startKoin {
            androidContext(ApplicationProvider.getApplicationContext<MyApp>())
            modules(listOf(koinTestModules))
        }
    }

    @After
    fun finish() {
        stopKoin()
    }

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()


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
    fun retrieveReminder_WithWrongID() = runTest {
        assertThat(reminderLocalTestRepository.getReminder("wrongID")).isEqualTo(Result.Error("Reminder not found!"))
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