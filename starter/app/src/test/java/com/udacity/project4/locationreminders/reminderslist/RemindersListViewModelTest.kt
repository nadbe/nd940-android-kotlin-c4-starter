package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest : KoinTest {

    private val remindersListViewModel: RemindersListViewModel by inject()
    private val reminderRepository: FakeDataSource by inject()
    private val saveReminderViewModel: SaveReminderViewModel by inject()


    private  val koinTestModules = module {

        viewModel {
            RemindersListViewModel(
                ApplicationProvider.getApplicationContext(),
                get() as FakeDataSource
            )
        }
        viewModel {
            SaveReminderViewModel(
                ApplicationProvider.getApplicationContext(),
                get() as FakeDataSource
            )
        }
        single{ FakeDataSource() }
    }

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupViewModel() {
        stopKoin()
        startKoin {
            modules(listOf(koinTestModules))
        }
    }

    @After
    fun stopKoinContext(){
        stopKoin()
    }

    @Test
    fun loadReminders_withSuccess() = runTest {

        val reminder = ReminderDataItem("Alte Pinakothek", "Museum", "Alte Pinakothek", 48.14881 ,11.57142)
        saveReminderViewModel.validateAndSaveReminder(reminder)

        Dispatchers.setMain(StandardTestDispatcher())
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue()).isTrue()
        advanceUntilIdle()
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue()).isFalse()
        assertThat(remindersListViewModel.remindersList.getOrAwaitValue()).contains(reminder)
    }

    @Test
    fun loadReminders_withError() = runTest {

        Dispatchers.setMain(StandardTestDispatcher())
        reminderRepository.shouldReturnError = true
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue()).isTrue()
        advanceUntilIdle()
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue()).isFalse()
        assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue()).isEqualTo("Error")
    }

    @Test
    fun loadReminder_withError() = runTest {
        reminderRepository.shouldReturnError = true
        reminderRepository.getReminder("wrongid")
        assertThat(reminderRepository.getReminder("wrongID")).isEqualTo(Result.Error("Error"))
    }

    @Test
    fun noReminders_updateShowNoData() = runTest {

        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue()).isTrue()
    }

    @Test
    fun remindersAvailable_updateShowNoData() = runTest {

        val reminder = ReminderDataItem("Alte Pinakothek", "Museum", "Alte Pinakothek", 48.14881 ,11.57142)
        saveReminderViewModel.validateAndSaveReminder(reminder)

        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue()).isFalse()
    }


}