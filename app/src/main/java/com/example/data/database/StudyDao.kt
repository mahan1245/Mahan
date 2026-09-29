package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.FullStudyDay
import com.example.data.model.FullStudyItem
import com.example.data.model.StudyDayEntity
import com.example.data.model.StudyItemEntity
import com.example.data.model.StudySlotEntity
import com.example.data.model.SubtaskEntity
import com.example.data.model.TestResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyDao {

    @Query("SELECT * FROM study_days ORDER BY date ASC")
    fun getAllDays(): Flow<List<StudyDayEntity>>

    @Transaction
    @Query("SELECT * FROM study_days ORDER BY date ASC")
    fun getAllFullDays(): Flow<List<FullStudyDay>>

    @Transaction
    @Query("SELECT * FROM study_days WHERE id = :dayId LIMIT 1")
    fun getFullDayById(dayId: Long): Flow<FullStudyDay?>

    @Transaction
    @Query("SELECT * FROM study_days WHERE date = :date LIMIT 1")
    fun getFullDayByDate(date: String): Flow<FullStudyDay?>

    @Transaction
    @Query("SELECT * FROM study_items WHERE id = :itemId LIMIT 1")
    suspend fun getFullItemById(itemId: Long): FullStudyItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDay(day: StudyDayEntity): Long

    @Query("SELECT id FROM study_days WHERE date = :date LIMIT 1")
    suspend fun getDayIdByDate(date: String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: StudyItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<StudyItemEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlots(slots: List<StudySlotEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtasks(subtasks: List<SubtaskEntity>)

    @Update
    suspend fun updateItem(item: StudyItemEntity)

    @Query("UPDATE study_items SET isCompleted = :completed, actualDurationMinutes = :actualDuration, isEstimated = :isEstimated WHERE id = :itemId")
    suspend fun updateItemCompletion(itemId: Long, completed: Boolean, actualDuration: Int, isEstimated: Boolean)

    @Query("UPDATE study_items SET userNotes = :notes, consultantReport = :report WHERE id = :itemId")
    suspend fun updateItemNotes(itemId: Long, notes: String?, report: String?)

    @Query("UPDATE subtasks SET isCompleted = :completed WHERE id = :subtaskId")
    suspend fun updateSubtaskCompletion(subtaskId: Long, completed: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTestResult(testResult: TestResultEntity): Long

    @Query("DELETE FROM test_results WHERE itemId = :itemId")
    suspend fun deleteTestResultByItemId(itemId: Long)

    @Query("SELECT * FROM test_results ORDER BY recordedAt DESC")
    fun getAllTestResults(): Flow<List<TestResultEntity>>

    @Query("DELETE FROM study_days WHERE id = :dayId")
    suspend fun deleteDay(dayId: Long)

    @Query("DELETE FROM study_items WHERE id = :itemId")
    suspend fun deleteItem(itemId: Long)

    @Query("DELETE FROM study_days")
    suspend fun clearAllData()
}
