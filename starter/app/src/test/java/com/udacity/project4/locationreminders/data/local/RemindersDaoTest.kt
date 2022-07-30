package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import org.junit.After
import org.junit.Test
import org.koin.core.context.GlobalContext

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    private lateinit var database: RemindersDatabase

    @ExperimentalCoroutinesApi
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun closeDb() = database.close()

    @After
    fun stopKoinContext(){
        GlobalContext.stopKoin()
    }

    @Test
    fun saveReminder_getReminderById() {
        val reminder = ReminderDTO("Alte Pinakothek", "Museum", "Alte Pinakothek", 48.14881 ,11.57142)
        database.reminderDao().saveReminder(reminder)
        val loadedReminder = database.reminderDao().getReminderById(reminder.id)
        assertThat(loadedReminder).isEqualTo(reminder)
    }

    @Test
    fun saveReminder_replaces() {
        val reminder1 = ReminderDTO("Alte Pinakothek", "Museum", "Alte Pinakothek", 48.14881 ,11.57142)
        val reminder2 = ReminderDTO("Botanischer Garten", "Garten", "Botanischer Garten", 48.14348 ,11.56767, reminder1.id)
        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)
        val loadedReminder = database.reminderDao().getReminderById(reminder1.id)
        assertThat(loadedReminder?.title).isEqualTo("Botanischer Garten")
    }

    @Test
    fun saveReminders_getReminderList() {
        val reminder1 = ReminderDTO("Alte Pinakothek", "Museum", "Alte Pinakothek", 48.14881 ,11.57142)
        val reminder2 = ReminderDTO("Botanischer Garten", "Garten", "Botanischer Garten", 48.14348 ,11.56767)
        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)
        val loadedReminders = database.reminderDao().getReminders()
        assertThat(loadedReminders.size).isEqualTo(2)
    }

    @Test
    fun deleteAllRemindersInDB() {
        val reminder = ReminderDTO("Alte Pinakothek", "Museum", "Alte Pinakothek", 48.14881 ,11.57142)
        database.reminderDao().saveReminder(reminder)

        val loadedReminder = database.reminderDao().getReminderById(reminder.id)
        assertThat(loadedReminder).isEqualTo(reminder)

        database.reminderDao().deleteAllReminders()
        assertThat(database.reminderDao().getReminders()).isEmpty()

    }





}