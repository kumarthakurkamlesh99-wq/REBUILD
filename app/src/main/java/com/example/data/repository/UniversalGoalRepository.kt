package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.CustomTrackerEntity
import com.example.data.local.entity.RoadmapEntity
import com.example.data.local.entity.RoadmapMilestoneEntity
import com.example.data.local.entity.SkillNodeEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class UniversalGoalRepository(
    private val db: AppDatabase
) {
    // --- SKILL TREE ---
    fun getAllSkillNodes(): Flow<List<SkillNodeEntity>> = db.universalGoalDao().getAllSkillNodes()

    fun getSkillNodesByCategory(category: String): Flow<List<SkillNodeEntity>> =
        db.universalGoalDao().getSkillNodesByCategory(category)

    suspend fun updateNodeProgress(id: Long, isMastered: Boolean, percentage: Int) = withContext(Dispatchers.IO) {
        db.universalGoalDao().updateNodeProgress(id, isMastered, percentage)
    }

    suspend fun initializeDefaultSkillTrees() = withContext(Dispatchers.IO) {
        // Seed standard skill progression trees if empty
        val codingNodes = listOf(
            SkillNodeEntity(category = "CODING", treeTitle = "Full-Stack Development", nodeName = "HTML & CSS Foundation", tierLevel = 1, isUnlocked = true, isMastered = false, masteryPercentage = 40, xpReward = 50, description = "Semantic markup, flexbox, grid, and responsiveness"),
            SkillNodeEntity(category = "CODING", treeTitle = "Full-Stack Development", nodeName = "JavaScript & ES6 Core", tierLevel = 2, isUnlocked = true, isMastered = false, masteryPercentage = 20, xpReward = 100, description = "Async/await, DOM, closures, prototypes"),
            SkillNodeEntity(category = "CODING", treeTitle = "Full-Stack Development", nodeName = "React & Modern UI", tierLevel = 3, isUnlocked = false, isMastered = false, masteryPercentage = 0, xpReward = 150, description = "Components, hooks, state machines, and memoization"),
            SkillNodeEntity(category = "CODING", treeTitle = "Full-Stack Development", nodeName = "Backend & Databases", tierLevel = 4, isUnlocked = false, isMastered = false, masteryPercentage = 0, xpReward = 200, description = "Node/Kotlin, REST APIs, SQL, schema design"),
            SkillNodeEntity(category = "CODING", treeTitle = "Full-Stack Development", nodeName = "Production Projects & DSA", tierLevel = 5, isUnlocked = false, isMastered = false, masteryPercentage = 0, xpReward = 300, description = "LeetCode patterns, deployment, Docker, system design")
        )

        val fitnessNodes = listOf(
            SkillNodeEntity(category = "FITNESS", treeTitle = "Calisthenics & Strength", nodeName = "Beginner Form & Core", tierLevel = 1, isUnlocked = true, isMastered = false, masteryPercentage = 50, xpReward = 50, description = "Standard pushups, hollow body hold, squats"),
            SkillNodeEntity(category = "FITNESS", treeTitle = "Calisthenics & Strength", nodeName = "Intermediate Mastery", tierLevel = 2, isUnlocked = true, isMastered = false, masteryPercentage = 10, xpReward = 100, description = "Pull-ups, dips, pistol squats, 5km pacing"),
            SkillNodeEntity(category = "FITNESS", treeTitle = "Calisthenics & Strength", nodeName = "Advanced Calisthenics", tierLevel = 3, isUnlocked = false, isMastered = false, masteryPercentage = 0, xpReward = 150, description = "Muscle-up, handstand balance, weighted pullups"),
            SkillNodeEntity(category = "FITNESS", treeTitle = "Calisthenics & Strength", nodeName = "Elite Static Holds", tierLevel = 4, isUnlocked = false, isMastered = false, masteryPercentage = 0, xpReward = 200, description = "Front lever, planche progressions, human flag"),
            SkillNodeEntity(category = "FITNESS", treeTitle = "Calisthenics & Strength", nodeName = "Supreme Athlete", tierLevel = 5, isUnlocked = false, isMastered = false, masteryPercentage = 0, xpReward = 300, description = "Elite strength-to-weight ratio and peak endurance")
        )

        val examNodes = listOf(
            SkillNodeEntity(category = "EXAMS", treeTitle = "Board / Competitive Mastery", nodeName = "Concept Blueprinting", tierLevel = 1, isUnlocked = true, isMastered = false, masteryPercentage = 60, xpReward = 50, description = "NCERT/Textbook reading, formula sheets, theory notes"),
            SkillNodeEntity(category = "EXAMS", treeTitle = "Board / Competitive Mastery", nodeName = "Chapter Exercise Drills", tierLevel = 2, isUnlocked = true, isMastered = false, masteryPercentage = 25, xpReward = 100, description = "In-text examples and standard chapter problem sets"),
            SkillNodeEntity(category = "EXAMS", treeTitle = "Board / Competitive Mastery", nodeName = "PYQ (Past 10 Years)", tierLevel = 3, isUnlocked = false, isMastered = false, masteryPercentage = 0, xpReward = 150, description = "Previous year question speed and accuracy solving"),
            SkillNodeEntity(category = "EXAMS", treeTitle = "Board / Competitive Mastery", nodeName = "Timed Full Mock Tests", tierLevel = 4, isUnlocked = false, isMastered = false, masteryPercentage = 0, xpReward = 200, description = "3-hour exam simulation under strict time limits"),
            SkillNodeEntity(category = "EXAMS", treeTitle = "Board / Competitive Mastery", nodeName = "Rank-1 Exam Domination", tierLevel = 5, isUnlocked = false, isMastered = false, masteryPercentage = 0, xpReward = 300, description = "Flawless presentation, zero silly errors, 95%+ precision")
        )

        db.universalGoalDao().insertSkillNodes(codingNodes + fitnessNodes + examNodes)
    }

    // --- ROADMAPS ---
    fun getAllRoadmaps(): Flow<List<RoadmapEntity>> = db.universalGoalDao().getAllRoadmaps()

    fun getMilestonesForRoadmap(roadmapId: Long): Flow<List<RoadmapMilestoneEntity>> =
        db.universalGoalDao().getMilestonesForRoadmap(roadmapId)

    suspend fun createRoadmapWithMilestones(
        goalTitle: String,
        category: String,
        horizonDays: Int,
        summary: String,
        milestones: List<Pair<String, String>> // Title to description
    ): Long = withContext(Dispatchers.IO) {
        val roadmap = RoadmapEntity(
            goalTitle = goalTitle,
            category = category,
            horizonDays = horizonDays,
            totalMilestones = milestones.size,
            completedMilestones = 0,
            summary = summary
        )
        val roadmapId = db.universalGoalDao().insertRoadmap(roadmap)
        val entities = milestones.mapIndexed { index, (mTitle, mDesc) ->
            RoadmapMilestoneEntity(
                roadmapId = roadmapId,
                milestoneTitle = mTitle,
                weekNumber = (index / 2) + 1,
                description = mDesc,
                isCompleted = false
            )
        }
        db.universalGoalDao().insertMilestones(entities)
        roadmapId
    }

    suspend fun toggleMilestone(milestoneId: Long, completed: Boolean) = withContext(Dispatchers.IO) {
        db.universalGoalDao().toggleMilestone(milestoneId, completed)
    }

    suspend fun deleteRoadmap(roadmapId: Long) = withContext(Dispatchers.IO) {
        db.universalGoalDao().deleteMilestonesForRoadmap(roadmapId)
        db.universalGoalDao().deleteRoadmap(roadmapId)
    }

    // --- CUSTOM TRACKERS ---
    fun getAllCustomTrackers(): Flow<List<CustomTrackerEntity>> = db.universalGoalDao().getAllCustomTrackers()

    suspend fun createCustomTracker(
        title: String,
        category: String,
        targetCount: Int,
        unit: String
    ): Long = withContext(Dispatchers.IO) {
        val tracker = CustomTrackerEntity(
            title = title,
            category = category,
            currentCount = 0,
            targetCount = targetCount,
            unit = unit
        )
        db.universalGoalDao().insertTracker(tracker)
    }

    suspend fun updateTrackerCount(tracker: CustomTrackerEntity, newCount: Int) = withContext(Dispatchers.IO) {
        db.universalGoalDao().updateTracker(tracker.copy(currentCount = maxOf(0, newCount), lastUpdated = System.currentTimeMillis()))
    }

    suspend fun deleteTracker(id: Long) = withContext(Dispatchers.IO) {
        db.universalGoalDao().deleteTracker(id)
    }
}
