package com.udacity.project4.locationreminders.data


import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result


//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {

    var reminderData:LinkedHashMap<String, ReminderDTO> = LinkedHashMap()
    var shouldReturnError:Boolean = false

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError) {
            shouldReturnError = false
            return Result.Error("Error")
        } else {
            reminderData.let { return Result.Success(reminderData.values.toList()) }
        }

    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminderData[reminder.id] = reminder
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (!shouldReturnError) {
            reminderData[id]?.let {
                return Result.Success(it)
            }
        }
        shouldReturnError = false
        return Result.Error("Error")

    }

    override suspend fun deleteAllReminders() {
        reminderData.clear()
    }


}