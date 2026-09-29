package com.example.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(
    tableName = "study_days",
    indices = [Index(value = ["date"], unique = true)]
)
data class StudyDayEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // e.g. "1405-07-04"
    val weekday: String, // e.g. "شنبه"
    val title: String, // e.g. "شنبه ۴ مهر"
    val createdTimestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "study_items",
    foreignKeys = [
        ForeignKey(
            entity = StudyDayEntity::class,
            parentColumns = ["id"],
            childColumns = ["dayId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("dayId")]
)
data class StudyItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayId: Long,
    val subject: String, // e.g. "هندسه", "حسابان", "فیزیک"
    val activity: String, // "video", "note_taking", "reading", "exercise", "test", "analysis", "review", "exam", "other"
    val resource: String? = null,
    val teacher: String? = null,
    val targetKind: String? = null, // "tests", "video", "pages"
    val targetValue: Int? = null,
    val instructions: String? = null,
    val askedToReport: Boolean = false,
    val needsReview: Boolean = false,
    val reviewNote: String? = null,
    val isCompleted: Boolean = false,
    val actualDurationMinutes: Int = 0,
    val isEstimated: Boolean = false,
    val userNotes: String? = null,
    val consultantReport: String? = null,
    val orderIndex: Int = 0
)

@Entity(
    tableName = "study_slots",
    foreignKeys = [
        ForeignKey(
            entity = StudyItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("itemId")]
)
data class StudySlotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemId: Long,
    val startTime: String, // "16:00"
    val endTime: String, // "17:15"
    val durationMinutes: Int = 0
)

@Entity(
    tableName = "subtasks",
    foreignKeys = [
        ForeignKey(
            entity = StudyItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("itemId")]
)
data class SubtaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemId: Long,
    val title: String,
    val isCompleted: Boolean = false
)

@Entity(
    tableName = "test_results",
    foreignKeys = [
        ForeignKey(
            entity = StudyItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("itemId")]
)
data class TestResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemId: Long,
    val subject: String,
    val topic: String = "",
    val totalQuestions: Int = 0,
    val correctAnswers: Int = 0,
    val wrongAnswers: Int = 0,
    val blankAnswers: Int = 0,
    val percentage: Double = 0.0,
    val recordedAt: Long = System.currentTimeMillis()
)

data class FullStudyItem(
    @Embedded val item: StudyItemEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "itemId"
    )
    val slots: List<StudySlotEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "itemId"
    )
    val subtasks: List<SubtaskEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "itemId"
    )
    val testResult: TestResultEntity? = null
) {
    val totalPlannedMinutes: Int
        get() = slots.sumOf { it.durationMinutes }.let { if (it > 0) it else 60 }
}

data class FullStudyDay(
    @Embedded val day: StudyDayEntity,
    @Relation(
        entity = StudyItemEntity::class,
        parentColumn = "id",
        entityColumn = "dayId"
    )
    val items: List<FullStudyItem>
) {
    val totalPlannedMinutes: Int
        get() = items.sumOf { it.totalPlannedMinutes }

    val completedMinutes: Int
        get() = items.filter { it.item.isCompleted }.sumOf {
            if (it.item.actualDurationMinutes > 0) it.item.actualDurationMinutes else it.totalPlannedMinutes
        }

    val progressFraction: Float
        get() {
            if (items.isEmpty()) return 0f
            val totalWeight = totalPlannedMinutes
            if (totalWeight <= 0) return 0f
            val completedWeight = items.filter { it.item.isCompleted }.sumOf { it.totalPlannedMinutes }
            return (completedWeight.toFloat() / totalWeight.toFloat()).coerceIn(0f, 1f)
        }
}
