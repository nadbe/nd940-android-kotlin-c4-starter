package com.udacity.project4.locationreminders.data

import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Tasks
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.runBlocking
import java.lang.Exception

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {

    var reminderData:LinkedHashMap<String, ReminderDTO> = LinkedHashMap()
    var withError:Boolean = false

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (withError) {
            withError = false
            return Result.Error("Error")
        } else {
            reminderData.let { return Result.Success(reminderData.values.toList()) }
        }

    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminderData[reminder.id] = reminder
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        reminderData[id]?.let {
            return Result.Success(it)
        }
        return Result.Error("Error")
    }

    override suspend fun deleteAllReminders() {
        reminderData.clear()
    }


}