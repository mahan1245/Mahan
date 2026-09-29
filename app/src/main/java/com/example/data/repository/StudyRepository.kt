package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.database.StudyDao
import com.example.data.model.DayDto
import com.example.data.model.FullStudyDay
import com.example.data.model.FullStudyItem
import com.example.data.model.ItemDto
import com.example.data.model.ScheduleResponseDto
import com.example.data.model.SlotDto
import com.example.data.model.StudyDayEntity
import com.example.data.model.StudyItemEntity
import com.example.data.model.StudySlotEntity
import com.example.data.model.SubtaskEntity
import com.example.data.model.TargetDto
import com.example.data.model.TestResultEntity
import com.example.data.model.UserProfile
import com.example.util.PersianUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class StudyRepository(
    private val studyDao: StudyDao,
    context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("konkuryar_user_prefs", Context.MODE_PRIVATE)

    val allFullDays: Flow<List<FullStudyDay>> = studyDao.getAllFullDays()
    val allTestResults: Flow<List<TestResultEntity>> = studyDao.getAllTestResults()

    fun getFullDayById(dayId: Long): Flow<FullStudyDay?> = studyDao.getFullDayById(dayId)

    fun getFullDayByDate(date: String): Flow<FullStudyDay?> = studyDao.getFullDayByDate(date)

    suspend fun getFullItemById(itemId: Long): FullStudyItem? = studyDao.getFullItemById(itemId)

    suspend fun saveScheduleResponse(dto: ScheduleResponseDto) = withContext(Dispatchers.IO) {
        for (dayDto in dto.days) {
            saveDayDto(dayDto)
        }
    }

    suspend fun saveDayDto(dayDto: DayDto): Long = withContext(Dispatchers.IO) {
        val existingDayId = studyDao.getDayIdByDate(dayDto.date)
        val dayId = if (existingDayId != null) {
            existingDayId
        } else {
            val title = if (dayDto.weekday.isNotEmpty()) "${dayDto.weekday} ${dayDto.date}" else dayDto.date
            studyDao.insertDay(
                StudyDayEntity(
                    date = dayDto.date,
                    weekday = dayDto.weekday,
                    title = title
                )
            )
        }

        var orderIndex = 0
        for (itemDto in dayDto.items) {
            val itemEntity = StudyItemEntity(
                dayId = dayId,
                subject = itemDto.subject.ifEmpty { "درس عمومی/اختصاصی" },
                activity = itemDto.activity,
                resource = itemDto.resource,
                teacher = itemDto.teacher,
                targetKind = itemDto.target?.kind,
                targetValue = itemDto.target?.value,
                instructions = itemDto.instructions,
                askedToReport = itemDto.askedToReport,
                needsReview = itemDto.needsReview,
                reviewNote = itemDto.reviewNote,
                orderIndex = orderIndex++
            )
            val itemId = studyDao.insertItem(itemEntity)

            val slotEntities = itemDto.slots.map { slot ->
                val duration = calculateDurationMinutes(slot.start, slot.end)
                StudySlotEntity(
                    itemId = itemId,
                    startTime = PersianUtils.toEnglishDigits(slot.start),
                    endTime = PersianUtils.toEnglishDigits(slot.end),
                    durationMinutes = duration
                )
            }
            if (slotEntities.isNotEmpty()) {
                studyDao.insertSlots(slotEntities)
            }

            val subtaskEntities = itemDto.subtasks.map { subtaskTitle ->
                SubtaskEntity(
                    itemId = itemId,
                    title = subtaskTitle,
                    isCompleted = false
                )
            }
            if (subtaskEntities.isNotEmpty()) {
                studyDao.insertSubtasks(subtaskEntities)
            }
        }
        dayId
    }

    private fun calculateDurationMinutes(start: String, end: String): Int {
        try {
            val sEng = PersianUtils.toEnglishDigits(start).trim()
            val eEng = PersianUtils.toEnglishDigits(end).trim()
            val sParts = sEng.split(":").map { it.toInt() }
            val eParts = eEng.split(":").map { it.toInt() }
            val sMin = sParts[0] * 60 + (if (sParts.size > 1) sParts[1] else 0)
            val eMin = eParts[0] * 60 + (if (eParts.size > 1) eParts[1] else 0)
            val diff = eMin - sMin
            return if (diff > 0) diff else 60
        } catch (_: Exception) {
            return 60
        }
    }

    suspend fun setItemCompleted(
        itemId: Long,
        completed: Boolean,
        actualDuration: Int,
        isEstimated: Boolean
    ) = withContext(Dispatchers.IO) {
        studyDao.updateItemCompletion(itemId, completed, actualDuration, isEstimated)
    }

    suspend fun setSubtaskCompleted(subtaskId: Long, completed: Boolean) = withContext(Dispatchers.IO) {
        studyDao.updateSubtaskCompletion(subtaskId, completed)
    }

    suspend fun saveItemNotes(itemId: Long, notes: String?, report: String?) = withContext(Dispatchers.IO) {
        studyDao.updateItemNotes(itemId, notes, report)
    }

    suspend fun saveTestResult(testResult: TestResultEntity): Long = withContext(Dispatchers.IO) {
        studyDao.deleteTestResultByItemId(testResult.itemId)
        studyDao.insertTestResult(testResult)
    }

    suspend fun deleteDay(dayId: Long) = withContext(Dispatchers.IO) {
        studyDao.deleteDay(dayId)
    }

    suspend fun deleteItem(itemId: Long) = withContext(Dispatchers.IO) {
        studyDao.deleteItem(itemId)
    }

    fun getUserProfile(): UserProfile {
        return UserProfile(
            name = prefs.getString("user_name", "داوطلب دوازدهم ریاضی") ?: "داوطلب دوازدهم ریاضی",
            grade = prefs.getString("user_grade", "پایه دوازدهم") ?: "پایه دوازدهم",
            major = prefs.getString("user_major", "ریاضی و فیزیک") ?: "ریاضی و فیزیک",
            consultantName = prefs.getString("user_consultant", "مهندس حیدری") ?: "مهندس حیدری",
            dailyTargetHours = prefs.getFloat("daily_target_hours", 8.0f),
            targetExam = prefs.getString("target_exam", "کنکور سراسری ریاضی") ?: "کنکور سراسری ریاضی",
            konkurDateShamsi = prefs.getString("konkur_date", "۱۴۰۶/۰۴/۰۵") ?: "۱۴۰۶/۰۴/۰۵"
        )
    }

    fun saveUserProfile(profile: UserProfile) {
        prefs.edit()
            .putString("user_name", profile.name)
            .putString("user_grade", profile.grade)
            .putString("user_major", profile.major)
            .putString("user_consultant", profile.consultantName)
            .putFloat("daily_target_hours", profile.dailyTargetHours)
            .putString("target_exam", profile.targetExam)
            .putString("konkur_date", profile.konkurDateShamsi)
            .apply()
    }

    suspend fun seedInitialSampleDaysIfNeeded() = withContext(Dispatchers.IO) {
        val hasDays = prefs.getBoolean("has_seeded_initial_data", false)
        if (hasDays) return@withContext

        // Create sample 3-day schedule as specified in prompt
        val day1 = DayDto(
            date = "1405-07-04",
            weekday = "شنبه",
            items = listOf(
                ItemDto(
                    subject = "هندسه",
                    activity = "video",
                    slots = listOf(
                        com.example.data.model.SlotDto(start = "16:00", end = "17:15"),
                        com.example.data.model.SlotDto(start = "17:30", end = "19:00")
                    ),
                    resource = "فیلم ۱۷ از شروع فصل دوم",
                    teacher = "حیدری",
                    target = null,
                    subtasks = listOf(
                        "تماشای فیلم ۱۷ هندسه و جزوه‌نویسی",
                        "مطالعه جزوه معلم مدرسه",
                        "دسته‌بندی مطالب در ذهن"
                    ),
                    instructions = "فیلم ۱۷ از شروع فصل دوم هندسه رو ببین و جزوه نویسی کن و بعدش که تموم شد جزوه معلم مدرسه رو هم بخون و مطالب رو یکجا توی ذهنت دسته بندی کن",
                    askedToReport = false,
                    needsReview = false
                ),
                ItemDto(
                    subject = "زبان انگلیسی",
                    activity = "reading",
                    slots = listOf(
                        com.example.data.model.SlotDto(start = "19:15", end = "21:15")
                    ),
                    resource = "جزوه زبان تابستان",
                    teacher = null,
                    target = null,
                    subtasks = listOf(
                        "مطالعه کامل جزوه زبان تابستان",
                        "تسلط در حد مدرسه"
                    ),
                    instructions = "جزوه زبان رو برای تابستون کامل بخون و مطالب رو مسلط بشو فعلا در حد مدرسه",
                    askedToReport = false,
                    needsReview = false
                ),
                ItemDto(
                    subject = "گسسته و فیزیک",
                    activity = "analysis",
                    slots = listOf(
                        com.example.data.model.SlotDto(start = "22:00", end = "23:00")
                    ),
                    resource = "آزمون آزمایشی",
                    teacher = null,
                    target = com.example.data.model.TargetDto(kind = "tests", value = 15),
                    subtasks = listOf(
                        "تحلیل آزمون گسسته",
                        "تحلیل آزمون فیزیک",
                        "یادگیری دانه دانه غلط‌ها و نزده‌ها"
                    ),
                    instructions = "تحلیل آزمون گسسته و فیزیک و دونه دونه نزده ها و غلط ها رو یاد بگیر",
                    askedToReport = false,
                    needsReview = false
                )
            )
        )

        val day2 = DayDto(
            date = "1405-07-05",
            weekday = "یک‌شنبه",
            items = listOf(
                ItemDto(
                    subject = "شیمی",
                    activity = "exercise",
                    slots = listOf(
                        com.example.data.model.SlotDto(start = "15:45", end = "17:00")
                    ),
                    resource = "تمرینات آخر فصل شیمی",
                    teacher = null,
                    target = null,
                    subtasks = listOf(
                        "حل تمرینات آخر فصل به روش مشاور",
                        "بررسی نکات تستی"
                    ),
                    instructions = "از ساعت ۱۵:۴۵ تا ۱۷ تمرینات آخر فصل شیمی رو کار کن به روشی که گفتم",
                    askedToReport = false,
                    needsReview = false
                ),
                ItemDto(
                    subject = "شیمی",
                    activity = "analysis",
                    slots = listOf(
                        com.example.data.model.SlotDto(start = "17:00", end = "17:45")
                    ),
                    resource = "تحلیل شیمی",
                    teacher = null,
                    target = null,
                    subtasks = listOf(
                        "تحلیل کامل تمارین شیمی"
                    ),
                    instructions = "۱۷ تا ۱۷:۴۵ تحلیل شیمی رو انجام بده",
                    askedToReport = false,
                    needsReview = false
                ),
                ItemDto(
                    subject = "گسسته",
                    activity = "test",
                    slots = listOf(
                        com.example.data.model.SlotDto(start = "18:00", end = "19:30")
                    ),
                    resource = "تست گسسته",
                    teacher = null,
                    target = com.example.data.model.TargetDto(kind = "tests", value = 20),
                    subtasks = listOf(
                        "حل ۲۰ تست گسسته",
                        "تیپ‌بندی و یادگیری کامل روش‌ها"
                    ),
                    instructions = "تست گسسته ۲۰ تا اما همین رو کامل یاد بگیر و تیپ بندی کن",
                    askedToReport = false,
                    needsReview = false
                ),
                ItemDto(
                    subject = "حسابان",
                    activity = "video",
                    slots = listOf(
                        com.example.data.model.SlotDto(start = "19:45", end = "21:00"),
                        com.example.data.model.SlotDto(start = "21:30", end = "23:30")
                    ),
                    resource = "فیلم ۲۲ حیدری",
                    teacher = "حیدری",
                    target = null,
                    subtasks = listOf(
                        "تماشای بخش اول فیلم ۲۲",
                        "تماشای بخش دوم و یادگیری دانه‌دانه نکات",
                        "ارسال گزارش مقدار باقی‌مانده به مشاور"
                    ),
                    instructions = "فیلم ۲۲ حیدری هر چه قدر موند بهم بگو اما با کیفیت ببین و دونه دونه رو یاد بگیر",
                    askedToReport = true,
                    needsReview = false
                )
            )
        )

        val day3 = DayDto(
            date = "1405-07-06",
            weekday = "دوشنبه",
            items = listOf(
                ItemDto(
                    subject = "هندسه",
                    activity = "exam",
                    slots = listOf(
                        com.example.data.model.SlotDto(start = "15:45", end = "16:00")
                    ),
                    resource = "آزمون هندسه",
                    teacher = null,
                    target = com.example.data.model.TargetDto(kind = "tests", value = 10),
                    subtasks = listOf("شرکت در آزمون سریع هندسه"),
                    instructions = "آزمون هندسه",
                    askedToReport = false,
                    needsReview = false
                ),
                ItemDto(
                    subject = "حسابان",
                    activity = "video",
                    slots = listOf(
                        com.example.data.model.SlotDto(start = "16:00", end = "17:00")
                    ),
                    resource = "فیلم ۲۲ حیدری (تکمیل)",
                    teacher = "حیدری",
                    target = null,
                    subtasks = listOf("تکمیل و جمع‌بندی فیلم ۲۲ حیدری"),
                    instructions = "تکمیل فیلم ۲۲ حیدری",
                    askedToReport = false,
                    needsReview = false
                ),
                ItemDto(
                    subject = "شیمی",
                    activity = "test",
                    slots = listOf(
                        com.example.data.model.SlotDto(start = "17:15", end = "19:00"),
                        com.example.data.model.SlotDto(start = "19:15", end = "21:00")
                    ),
                    resource = "تست شیمی ۵۰ تا",
                    teacher = null,
                    target = com.example.data.model.TargetDto(kind = "tests", value = 50),
                    subtasks = listOf(
                        "پارت ۱: حل ۲۵ تست شیمی (۱۷:۱۵ تا ۱۹:۰۰)",
                        "پارت ۲: حل ۲۵ تست شیمی (۱۹:۱۵ تا ۲۱:۰۰)"
                    ),
                    instructions = "تست شیمی ۵۰ تا در هر پارت ۲۵ تا",
                    askedToReport = false,
                    needsReview = false
                )
            )
        )

        saveDayDto(day1)
        saveDayDto(day2)
        saveDayDto(day3)

        prefs.edit().putBoolean("has_seeded_initial_data", true).apply()
    }
}
