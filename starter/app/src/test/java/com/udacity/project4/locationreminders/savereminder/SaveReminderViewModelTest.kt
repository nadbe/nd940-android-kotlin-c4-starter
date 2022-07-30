package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.MyApp
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext.stopKoin
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest : KoinTest{

    private val saveReminderViewModel: SaveReminderViewModel by inject()

    private  val koinTestModules = module {

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
    fun init() {
        org.koin.core.context.stopKoin()
        startKoin {
            androidContext(ApplicationProvider.getApplicationContext<MyApp>())
            modules(listOf(koinTestModules))
        }
    }

    @After
    fun finish() {
        org.koin.core.context.stopKoin()
    }

    @Test
    fun saveData_ShowLoadingIsUpdated() = runTest {

        Dispatchers.setMain(StandardTestDispatcher())

        val reminder = ReminderDataItem("Alte Pinakothek", "Museum", "Alte Pinakothek", 48.14881 ,11.57142)
        //When saving reminder
        saveReminderViewModel.validateAndSaveReminder(reminder)
        //Showloading is updated twice and toast is shown
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue()).isTrue()
        advanceUntilIdle()
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue()).isFalse()
        assertThat(saveReminderViewModel.showToast.getOrAwaitValue()).containsMatch(ApplicationProvider.getApplicationContext<MyApp>().getString(R.string.reminder_saved))
        assertThat(saveReminderViewModel.navigationCommand.getOrAwaitValue()).isEqualTo(NavigationCommand.Back)
    }

    @Test
    fun saveData_validateEnteredData() {
        //Given a ReminderDataItem does have an empty title
        val reminderNoTitle = ReminderDataItem("", "Botanischer Garten", "Botanischer Garten", 48.14348 ,11.56767)

        saveReminderViewModel.validateEnteredData(reminderNoTitle)
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue()).isEqualTo(R.string.err_enter_title)

        //Given a ReminderDataItem does have an empty location
        val reminderNoLocation = ReminderDataItem("Botanischer Garten", "Botanischer Garten", "", 48.14348 ,11.56767)
        saveReminderViewModel.validateEnteredData(reminderNoLocation)
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue()).isEqualTo(R.string.err_select_location)
    }

    @Test
    fun onClearData() {

        saveReminderViewModel.onClear()
        assertThat(saveReminderViewModel.reminderTitle.getOrAwaitValue()).isEqualTo(null)
        assertThat(saveReminderViewModel.reminderDescription.getOrAwaitValue()).isEqualTo(null)
        assertThat(saveReminderViewModel.reminderSelectedLocationStr.getOrAwaitValue()).isEqualTo(null)
        assertThat(saveReminderViewModel.selectedPOI.getOrAwaitValue()).isEqualTo(null)
        assertThat(saveReminderViewModel.latitude.getOrAwaitValue()).isEqualTo(null)
        assertThat(saveReminderViewModel.longitude.getOrAwaitValue()).isEqualTo(null)
    }






}