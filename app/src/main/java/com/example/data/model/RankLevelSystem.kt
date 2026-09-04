package com.example.data.model

data class RankLevel(
    val level: Int,
    val title: String,
    val minXp: Int,
    val maxXp: Int,
    val brutalReality: String,
    val baseDirective: String,
    val nextRankGoal: String,
    val teaserLine: String = "Discipline protocol tier. Unlock this level to reveal full details.",
    val unlockXpCost: Int = if (level == 1) 0 else minXp,
    val celebrationMessage: String = "After weeks of excuses, you finally stopped acting like a spectator. You earned this rank. Now prove you deserve it.",
    val certificateCost: Int = if (level == 1) 100 else (level * 150)
)

data class XpSummaryData(
    val dailyXp: Int = 0,
    val weeklyXp: Int = 0,
    val monthlyXp: Int = 0,
    val totalXp: Int = 0
)

object RankLevelSystem {

    val RANKS = listOf(
        RankLevel(
            level = 1,
            title = "Absolute Clown",
            minXp = 0,
            maxXp = 250,
            brutalReality = "You're still operating like a distracted civilian.\nToo many excuses.\nToo little output.\nYou want results but your actions don't match your ambitions.",
            baseDirective = "Start stacking study sessions and consistency.",
            nextRankGoal = "Level 2 (Lazy Ladka) is waiting."
        ),
        RankLevel(
            level = 2,
            title = "Lazy Ladka",
            minXp = 250,
            maxXp = 600,
            brutalReality = "You took the first step, but you still negotiate with your morning alarm.\nComfort is your silent assassin.\nYou do the bare minimum and wonder why confidence feels fake.",
            baseDirective = "Eliminate zero days immediately. Treat your daily tasks as non-negotiable contracts.",
            nextRankGoal = "Level 3 (Noob) is waiting."
        ),
        RankLevel(
            level = 3,
            title = "Noob",
            minXp = 600,
            maxXp = 1100,
            brutalReality = "You've proven you can show up, but only when you 'feel like it'.\nMomentum dies the moment friction appears.\nTalent without endurance is completely useless.",
            baseDirective = "Build unbreakable study blocks. Keep your phone in another room during deep work.",
            nextRankGoal = "Level 4 (Casual) is waiting."
        ),
        RankLevel(
            level = 4,
            title = "Casual",
            minXp = 1100,
            maxXp = 1750,
            brutalReality = "You study in bursts, then disappear for three days.\nConsistency is your biggest bottleneck.\nMediocrity loves erratic effort.",
            baseDirective = "Lock down your wake time and workout routine. Protect your evening study hours.",
            nextRankGoal = "Level 5 (Average) is waiting."
        ),
        RankLevel(
            level = 5,
            title = "Average",
            minXp = 1750,
            maxXp = 2600,
            brutalReality = "You're doing what everyone else is doing, expecting exceptional results.\nAverage effort produces average scores and average lives.\nYou haven't tapped your true mental engine yet.",
            baseDirective = "Double your problem-solving volume. Push past mental fatigue instead of quitting at the first yawn.",
            nextRankGoal = "Level 6 (Tryhard) is waiting."
        ),
        RankLevel(
            level = 6,
            title = "Tryhard",
            minXp = 2600,
            maxXp = 3650,
            brutalReality = "You put in effort, but you still major in minor things.\nYou spend too much time organizing and too little time solving hard problems.\nAction isn't progress unless it's targeted.",
            baseDirective = "Attack your weakest subjects head-on. Stop hiding behind easy chapters.",
            nextRankGoal = "Level 7 (Grinder) is waiting."
        ),
        RankLevel(
            level = 7,
            title = "Grinder",
            minXp = 3650,
            maxXp = 4900,
            brutalReality = "Work ethic is kicking in, but your recovery is sloppy.\nIrregular sleep and skipped workouts will sabotage your long-term retention.\nDiscipline is a 24-hour cycle.",
            baseDirective = "Standardize your sleep schedule. Combine intense focus with rigorous physical training.",
            nextRankGoal = "Level 8 (Hustler) is waiting."
        ),
        RankLevel(
            level = 8,
            title = "Hustler",
            minXp = 4900,
            maxXp = 6400,
            brutalReality = "You're clocking hours, but passive reading is not deep work.\nIf you're not actively retrieving formulas and testing yourself, you're fooling yourself.",
            baseDirective = "Shift completely to active recall and timed PYQs. Make your brain sweat during study blocks.",
            nextRankGoal = "Level 9 (Warrior) is waiting."
        ),
        RankLevel(
            level = 9,
            title = "Warrior",
            minXp = 6400,
            maxXp = 8150,
            brutalReality = "You fight through distractions, but self-doubt still whispers in your ear.\nYou worry about the syllabus scale instead of executing the day.\nThe war is won in 50-minute blocks.",
            baseDirective = "Silence the noise. Execute today's mission with military precision.",
            nextRankGoal = "Level 10 (Beast) is waiting."
        ),
        RankLevel(
            level = 10,
            title = "Beast",
            minXp = 8150,
            maxXp = 10200,
            brutalReality = "Double digits reached. Most people quit long before here.\nHowever, arrogance is your new danger.\nComplacency at this stage will wipe out weeks of gains.",
            baseDirective = "Stay paranoid about weak chapters. Raise your daily standard by 10%.",
            nextRankGoal = "Level 11 (Savage) is waiting."
        ),
        RankLevel(
            level = 11,
            title = "Savage",
            minXp = 10200,
            maxXp = 12600,
            brutalReality = "You don't negotiate with feelings anymore.\nWhen tired, you still finish the reps and the derivations.\nNow you must master precision under timed pressure.",
            baseDirective = "Time yourself on full-length mock tests. Eliminate careless calculation errors.",
            nextRankGoal = "Level 12 (Alpha) is waiting."
        ),
        RankLevel(
            level = 12,
            title = "Alpha",
            minXp = 12600,
            maxXp = 15400,
            brutalReality = "You lead your own routine without needing external reminders.\nYour peers wonder how you stay locked in.\nDon't look sideways—look forward.",
            baseDirective = "Master the highest-weightage topics. Turn your good subjects into guaranteed 100% scores.",
            nextRankGoal = "Level 13 (Titan) is waiting."
        ),
        RankLevel(
            level = 13,
            title = "Titan",
            minXp = 15400,
            maxXp = 18600,
            brutalReality = "You are becoming immovable.\nDistractions bounce off you.\nYour daily study output now surpasses what you used to do in a full week.",
            baseDirective = "Polish your speed and answer presentation. Precision is what separates 90% from 98%.",
            nextRankGoal = "Level 14 (Overlord) is waiting."
        ),
        RankLevel(
            level = 14,
            title = "Overlord",
            minXp = 18600,
            maxXp = 22300,
            brutalReality = "You rule your schedule. Chaos has been banished from your day.\nEvery hour is accounted for, from calisthenics to deep revision.\nGuard this fortress.",
            baseDirective = "Target perfection in mock papers. Never let an unreviewed mistake slip past your audit.",
            nextRankGoal = "Level 15 (Legend) is waiting."
        ),
        RankLevel(
            level = 15,
            title = "Legend",
            minXp = 22300,
            maxXp = 26500,
            brutalReality = "You have separated yourself from 99% of your peers.\nDiscipline is no longer an effort; it is your baseline identity.\nLegendary status requires zero complacency.",
            baseDirective = "Accelerate revision cycles. Re-solve previous 10-year question banks under strict exam conditions.",
            nextRankGoal = "Level 16 (Phoenix) is waiting."
        ),
        RankLevel(
            level = 16,
            title = "Phoenix",
            minXp = 26500,
            maxXp = 31300,
            brutalReality = "You burned down your old undisciplined self and rose from the ashes.\nThe person who started this journey wouldn't recognize you today.\nFinish what you started.",
            baseDirective = "Keep the fire burning. Every morning routine must feel as sharp as day one.",
            nextRankGoal = "Level 17 (Titanium Mind) is waiting."
        ),
        RankLevel(
            level = 17,
            title = "Titanium Mind",
            minXp = 31300,
            maxXp = 36700,
            brutalReality = "Mental fatigue cannot penetrate your focus.\nYou sit down to study and the world simply vanishes.\nYour endurance is weaponized.",
            baseDirective = "Refine every micro-concept in Physics, Chemistry, and Math. Leave zero doubts unanswered.",
            nextRankGoal = "Level 18 (Shadow) is waiting."
        ),
        RankLevel(
            level = 18,
            title = "Shadow",
            minXp = 36700,
            maxXp = 42800,
            brutalReality = "You move in absolute silence.\nNo bragging, no social media updates, just ruthless execution in the dark.\nThe results will do all the speaking.",
            baseDirective = "Stay in the shadows of intense preparation. Your academic breakthrough is inevitable.",
            nextRankGoal = "Level 19 (Mastermind) is waiting."
        ),
        RankLevel(
            level = 19,
            title = "Mastermind",
            minXp = 42800,
            maxXp = 49600,
            brutalReality = "You understand the entire architecture of your exams and habits.\nYou predict question patterns and anticipate exam pressure.\nYou are in total command.",
            baseDirective = "Master exam-hall psychology and speed pacing. Execute with cool-headed mastery.",
            nextRankGoal = "Level 20 (Gladiator) is waiting."
        ),
        RankLevel(
            level = 20,
            title = "Gladiator",
            minXp = 49600,
            maxXp = 57200,
            brutalReality = "You are battle-tested.\nYou've survived exam pressure, exhaustion, and difficult chapters without breaking.\nThe arena belongs to you.",
            baseDirective = "Sharpen your execution to razor blade precision. Dominate every test series.",
            nextRankGoal = "Level 21 (Monk Mode) is waiting."
        ),
        RankLevel(
            level = 21,
            title = "Monk Mode",
            minXp = 57200,
            maxXp = 65700,
            brutalReality = "Complete detachment from worldly distractions.\nPure devotion to the craft of academic and physical excellence.\nYou are operating in rare air.",
            baseDirective = "Sustain this monastic purity of purpose. Do not compromise for a single day.",
            nextRankGoal = "Level 22 (Sigma) is waiting."
        ),
        RankLevel(
            level = 22,
            title = "Sigma",
            minXp = 65700,
            maxXp = 75200,
            brutalReality = "You walk your own path without needing outside validation.\nYour self-worth is rooted entirely in your work rate and integrity.\nUnshakable. Unbreakable.",
            baseDirective = "Channel this autonomy into academic supremacy. Turn every chapter into second nature.",
            nextRankGoal = "Level 23 (Chad) is waiting."
        ),
        RankLevel(
            level = 23,
            title = "Chad",
            minXp = 75200,
            maxXp = 85800,
            brutalReality = "Physical power combined with intellectual dominance.\nYou conquer heavy workouts and deep study sessions back-to-back with a smile.",
            baseDirective = "Maintain peak physical energy and mental clarity. The final summit is within reach.",
            nextRankGoal = "Level 24 (Mega Chad) is waiting."
        ),
        RankLevel(
            level = 24,
            title = "Mega Chad",
            minXp = 85800,
            maxXp = 97600,
            brutalReality = "Colossal discipline. You are the embodiment of the anti-mediocrity doctrine.\nYou do not flinch in the face of 12-hour study blocks or tough mock papers.",
            baseDirective = "One final push separates you from the pinnacle. Stack the last remaining protocol milestones.",
            nextRankGoal = "Level 25 (Supreme Chad) is waiting."
        ),
        RankLevel(
            level = 25,
            title = "Supreme Chad",
            minXp = 97600,
            maxXp = 120000,
            brutalReality = "You have conquered yourself. The ultimate transformation is complete.\nYou hold the pinnacle rank of the REBUILD Operating System.\nMediocrity has been permanently exterminated.",
            baseDirective = "Defend your throne at all costs. Maintain this supreme standard for the rest of your life.",
            nextRankGoal = "Maximum Apex Rank Achieved."
        )
    )

    fun getRankForLevel(level: Int): RankLevel {
        val clamped = level.coerceIn(1, 25)
        return RANKS[clamped - 1]
    }

    fun getRankForXp(xp: Int): RankLevel {
        for (i in RANKS.indices.reversed()) {
            if (xp >= RANKS[i].minXp) {
                return RANKS[i]
            }
        }
        return RANKS[0]
    }

    fun calculateProgress(xp: Int, rank: RankLevel): Float {
        if (rank.level >= 25) return 1.0f
        val range = (rank.maxXp - rank.minXp).toFloat().coerceAtLeast(1f)
        val progress = (xp - rank.minXp) / range
        return progress.coerceIn(0f, 1f)
    }

    fun getXpRequiredForNextLevel(xp: Int, rank: RankLevel): Int {
        if (rank.level >= 25) return 0
        return (rank.maxXp - xp).coerceAtLeast(0)
    }

    fun generateDynamicAnalysis(rank: RankLevel, daysLeft: Long): String {
        val sb = StringBuilder()
        sb.append(rank.brutalReality)
        sb.append("\n\n")
        sb.append("Good news:\n")
        if (daysLeft > 0) {
            sb.append("You have $daysLeft days left.\n")
        } else {
            sb.append("The target exam window is here.\n")
        }
        sb.append(rank.baseDirective)
        sb.append("\n")
        sb.append(rank.nextRankGoal)
        return sb.toString()
    }
}
