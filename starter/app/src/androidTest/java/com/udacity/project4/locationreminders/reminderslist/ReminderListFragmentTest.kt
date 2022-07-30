package com.udacity.project4.locationreminders.reminderslist

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.*

import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.MyApp
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.FakeAndroidDataSource
import com.udacity.project4.locationreminders.data.local.RemindersDatabase
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.test.inject
import org.koin.test.KoinTest
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.*

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : KoinTest{

    private lateinit var database: RemindersDatabase
    private val reminderDataSource:FakeAndroidDataSource by inject()

    private  val koinTestModules = module {
        viewModel {
            RemindersListViewModel(
                get(),
                get() as FakeAndroidDataSource
            )
        }
        viewModel {
            SaveReminderViewModel (
                get(),
                get() as FakeAndroidDataSource
            )
        }
        single {
            database = Room.inMemoryDatabaseBuilder(
                getApplicationContext(),
                RemindersDatabase::class.java
            ).allowMainThreadQueries().build()
        }
        single{ FakeAndroidDataSource()}
    }

    @Before
    fun init() {
        stopKoin()
        startKoin {
            androidContext(getApplicationContext<MyApp>())
            modules(listOf(koinTestModules))
        }
    }

    @After
    fun finish() {
        stopKoin()
    }

    @Test
    fun locationsAvailable_locationListIsShown() = runTest {
        //test the displayed data on the UI.
        val reminder1 = ReminderDTO("Alte Pinakothek", "Museum", "Alte Pinakothek", 48.14881 ,11.57142)
        val reminder2 = ReminderDTO("Botanischer Garten", "Garten", "Botanischer Garten", 48.14348 ,11.56767)
        reminderDataSource.saveReminder(reminder1)
        reminderDataSource.saveReminder(reminder2)

        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.reminderssRecyclerView)).perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
            hasDescendant(withText("Alte Pinakothek")), click()))
        onView(withId(R.id.reminderssRecyclerView)).perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
            hasDescendant(withText("Botanischer Garten")), click()))

    }

    @Test
    fun navigateToSaveReminderFragment() = runTest {
        //test the navigation of the fragments
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.addReminderFAB)).perform(click())
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }

    @Test
    fun noDataIconShown() {
        //add testing for the error messages.
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        onView(withId(R.id.noDataTextView)).check(ViewAssertions.matches(isDisplayed()))
    }




}