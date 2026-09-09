package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.CustomTrackerEntity
import com.example.data.local.entity.RoadmapEntity
import com.example.data.local.entity.RoadmapMilestoneEntity
import com.example.data.local.entity.SkillNodeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UniversalGoalDao {

    // --- SKILL NODES ---
    @Query("SELECT * FROM skill_nodes ORDER BY tierLevel ASC, id ASC")
    fun getAllSkillNodes(): Flow<List<SkillNodeEntity>>

    @Query("SELECT * FROM skill_nodes WHERE category = :category ORDER BY tierLevel ASC, id ASC")
    fun getSkillNodesByCategory(category: String): Flow<List<SkillNodeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkillNodes(nodes: List<SkillNodeEntity>)

    @Update
    suspend fun updateSkillNode(node: SkillNodeEntity)

    @Query("UPDATE skill_nodes SET isMastered = :isMastered, masteryPercentage = :percentage WHERE id = :id")
    suspend fun updateNodeProgress(id: Long, isMastered: Boolean, percentage: Int)

    // --- ROADMAPS ---
    @Query("SELECT * FROM roadmaps ORDER BY createdAt DESC")
    fun getAllRoadmaps(): Flow<List<RoadmapEntity>>

    @Query("SELECT * FROM roadmaps WHERE id = :id")
    suspend fun getRoadmapById(id: Long): RoadmapEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoadmap(roadmap: RoadmapEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestones(milestones: List<RoadmapMilestoneEntity>)

    @Query("SELECT * FROM roadmap_milestones WHERE roadmapId = :roadmapId ORDER BY weekNumber ASC")
    fun getMilestonesForRoadmap(roadmapId: Long): Flow<List<RoadmapMilestoneEntity>>

    @Query("UPDATE roadmap_milestones SET isCompleted = :completed WHERE id = :id")
    suspend fun toggleMilestone(id: Long, completed: Boolean)

    @Query("DELETE FROM roadmaps WHERE id = :roadmapId")
    suspend fun deleteRoadmap(roadmapId: Long)

    @Query("DELETE FROM roadmap_milestones WHERE roadmapId = :roadmapId")
    suspend fun deleteMilestonesForRoadmap(roadmapId: Long)

    // --- CUSTOM TRACKERS ---
    @Query("SELECT * FROM custom_trackers ORDER BY id ASC")
    fun getAllCustomTrackers(): Flow<List<CustomTrackerEntity>>

    @Query("SELECT * FROM custom_trackers ORDER BY id ASC")
    fun getAllTrackersDirect(): List<CustomTrackerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracker(tracker: CustomTrackerEntity): Long

    @Update
    suspend fun updateTracker(tracker: CustomTrackerEntity)

    @Query("DELETE FROM custom_trackers WHERE id = :id")
    suspend fun deleteTracker(id: Long)
}
