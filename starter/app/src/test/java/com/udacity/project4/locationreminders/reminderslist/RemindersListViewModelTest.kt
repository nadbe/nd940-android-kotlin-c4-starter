package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
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
import org.koin.core.context.GlobalContext

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var reminderRepository: FakeDataSource
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupViewModel() {
        reminderRepository = FakeDataSource()
        remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), reminderRepository)
    }

    @After
    fun stopKoinContext(){
        GlobalContext.stopKoin()
    }

    @Test
    fun loadReminders_withSuccess() = runTest {

        saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), reminderRepository)
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
        reminderRepository.withError = true
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue()).isTrue()
        advanceUntilIdle()
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue()).isFalse()
        assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue()).isEqualTo("Error")
    }

    @Test
    fun noReminders_updateShowNoData() = runTest {

        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue()).isTrue()
    }

    @Test
    fun remindersAvailable_updateShowNoData() = runTest {

        saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), reminderRepository)
        val reminder = ReminderDataItem("Alte Pinakothek", "Museum", "Alte Pinakothek", 48.14881 ,11.57142)
        saveReminderViewModel.validateAndSaveReminder(reminder)

        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue()).isFalse()
    }


}