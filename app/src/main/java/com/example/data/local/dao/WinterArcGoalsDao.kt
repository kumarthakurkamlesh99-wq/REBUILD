package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ArcGoalPlanItemEntity
import com.example.data.local.entity.WinterArcObjectiveEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WinterArcObjectivesDao {
    @Query("SELECT * FROM winter_arc_objectives ORDER BY orderIndex ASC, id ASC")
    fun getAllObjectives(): Flow<List<WinterArcObjectiveEntity>>

    @Query("SELECT * FROM winter_arc_objectives ORDER BY orderIndex ASC, id ASC")
    suspend fun getAllObjectivesDirect(): List<WinterArcObjectiveEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObjective(objective: WinterArcObjectiveEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObjectives(objectives: List<WinterArcObjectiveEntity>)

    @Update
    suspend fun updateObjective(objective: WinterArcObjectiveEntity)

    @Delete
    suspend fun deleteObjective(objective: WinterArcObjectiveEntity)

    @Query("DELETE FROM winter_arc_objectives")
    suspend fun clearAllObjectives()

    // Goals Plan (Daily, Weekly, Monthly)
    @Query("SELECT * FROM winter_arc_goals_plan ORDER BY orderIndex ASC, id ASC")
    fun getAllArcGoals(): Flow<List<ArcGoalPlanItemEntity>>

    @Query("SELECT * FROM winter_arc_goals_plan WHERE timeHorizon = :horizon ORDER BY orderIndex ASC, id ASC")
    fun getArcGoalsByHorizon(horizon: String): Flow<List<ArcGoalPlanItemEntity>>

    @Query("SELECT * FROM winter_arc_goals_plan ORDER BY orderIndex ASC, id ASC")
    suspend fun getAllArcGoalsDirect(): List<ArcGoalPlanItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArcGoal(goal: ArcGoalPlanItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArcGoals(goals: List<ArcGoalPlanItemEntity>)

    @Update
    suspend fun updateArcGoal(goal: ArcGoalPlanItemEntity)

    @Delete
    suspend fun deleteArcGoal(goal: ArcGoalPlanItemEntity)

    @Query("DELETE FROM winter_arc_goals_plan")
    suspend fun clearAllArcGoals()
}
