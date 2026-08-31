package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.BoardExamConfigEntity
import com.example.data.local.entity.DailyDisciplineEntity
import com.example.data.local.entity.HolidayEntity
import com.example.data.local.entity.WinterArcStateEntity
import com.example.data.local.entity.WorkoutLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workout_logs WHERE date = :date ORDER BY id ASC")
    fun getWorkoutsForDate(date: String): Flow<List<WorkoutLogEntity>>

    @Query("SELECT * FROM workout_logs WHERE date = :date")
    suspend fun getWorkoutsForDateDirect(date: String): List<WorkoutLogEntity>

    @Query("SELECT * FROM workout_logs ORDER BY date DESC, id DESC")
    fun getAllWorkouts(): Flow<List<WorkoutLogEntity>>

    @Query("SELECT COUNT(*) FROM workout_logs WHERE isCompleted = 1")
    fun getTotalCompletedWorkouts(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkouts(workouts: List<WorkoutLogEntity>)

    @Update
    suspend fun updateWorkout(workout: WorkoutLogEntity)

    @Delete
    suspend fun deleteWorkout(workout: WorkoutLogEntity)

    @Query("DELETE FROM workout_logs")
    suspend fun clearAll()
}

@Dao
interface HolidayDao {
    @Query("SELECT * FROM holidays ORDER BY id ASC")
    fun getAllHolidays(): Flow<List<HolidayEntity>>

    @Query("SELECT * FROM holidays WHERE date = :date OR date = :monthDay LIMIT 1")
    suspend fun getHolidayForDate(date: String, monthDay: String): HolidayEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHoliday(holiday: HolidayEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHolidays(holidays: List<HolidayEntity>)

    @Delete
    suspend fun deleteHoliday(holiday: HolidayEntity)

    @Query("DELETE FROM holidays")
    suspend fun clearAll()
}

@Dao
interface DisciplineDao {
    @Query("SELECT * FROM daily_discipline_scores WHERE date = :date LIMIT 1")
    fun getDisciplineForDate(date: String): Flow<DailyDisciplineEntity?>

    @Query("SELECT * FROM daily_discipline_scores WHERE date = :date LIMIT 1")
    suspend fun getDisciplineForDateDirect(date: String): DailyDisciplineEntity?

    @Query("SELECT * FROM daily_discipline_scores ORDER BY date DESC LIMIT 30")
    fun getRecentDisciplineScores(): Flow<List<DailyDisciplineEntity>>

    @Query("SELECT AVG(totalScore) FROM daily_discipline_scores")
    fun getAverageDisciplineScore(): Flow<Float?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(discipline: DailyDisciplineEntity)

    @Query("DELETE FROM daily_discipline_scores")
    suspend fun clearAll()
}

@Dao
interface WinterArcDao {
    @Query("SELECT * FROM winter_arc_state WHERE id = 1 LIMIT 1")
    fun getWinterArcState(): Flow<WinterArcStateEntity?>

    @Query("SELECT * FROM winter_arc_state WHERE id = 1 LIMIT 1")
    suspend fun getWinterArcStateDirect(): WinterArcStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(state: WinterArcStateEntity)

    @Query("DELETE FROM winter_arc_state")
    suspend fun clearAll()
}

@Dao
interface BoardExamDao {
    @Query("SELECT * FROM board_exam_config WHERE id = 1 LIMIT 1")
    fun getBoardExamConfig(): Flow<BoardExamConfigEntity?>

    @Query("SELECT * FROM board_exam_config WHERE id = 1 LIMIT 1")
    suspend fun getBoardExamConfigDirect(): BoardExamConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(config: BoardExamConfigEntity)

    @Query("DELETE FROM board_exam_config")
    suspend fun clearAll()
}
