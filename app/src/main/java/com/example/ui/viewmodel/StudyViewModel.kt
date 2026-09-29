package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiScheduleParser
import com.example.data.database.AppDatabase
import com.example.data.model.FullStudyDay
import com.example.data.model.FullStudyItem
import com.example.data.model.ScheduleResponseDto
import com.example.data.model.StudyItemEntity
import com.example.data.model.TestResultEntity
import com.example.data.model.UserProfile
import com.example.data.repository.StudyRepository
import com.example.service.ActiveStudySession
import com.example.service.StudyTimerService
import com.example.service.TimerManager
import com.example.util.PersianUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface AiParseState {
    data object Idle : AiParseState
    data object Parsing : AiParseState
    data class Preview(val scheduleDto: ScheduleResponseDto) : AiParseState
    data class Error(val message: String) : AiParseState
}

class StudyViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val repository = StudyRepository(database.studyDao(), application)
    private val geminiParser = GeminiScheduleParser()

    val allDays: StateFlow<List<FullStudyDay>> = repository.allFullDays
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTestResults: StateFlow<List<TestResultEntity>> = repository.allTestResults
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedDayIndex = MutableStateFlow(0)
    val selectedDayIndex: StateFlow<Int> = _selectedDayIndex.asStateFlow()

    val currentDay: StateFlow<FullStudyDay?> = combine(allDays, _selectedDayIndex) { days, index ->
        if (days.isNotEmpty()) {
            val safeIndex = index.coerceIn(0, days.size - 1)
            days[safeIndex]
        } else {
            null
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeTimerSession: StateFlow<ActiveStudySession?> = TimerManager.sessionState

    private val _aiParseState = MutableStateFlow<AiParseState>(AiParseState.Idle)
    val aiParseState: StateFlow<AiParseState> = _aiParseState.asStateFlow()

    private val _userProfile = MutableStateFlow(repository.getUserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialSampleDaysIfNeeded()
        }
    }

    fun selectDay(index: Int) {
        _selectedDayIndex.value = index
    }

    fun toggleItemCompleted(item: StudyItemEntity, defaultDuration: Int) {
        viewModelScope.launch {
            val newCompleted = !item.isCompleted
            // If user ticks without active timer, record planned duration as estimated
            val actualDuration = if (newCompleted) {
                if (item.actualDurationMinutes > 0) item.actualDurationMinutes else defaultDuration
            } else {
                0
            }
            val isEstimated = newCompleted && item.actualDurationMinutes == 0
            repository.setItemCompleted(item.id, newCompleted, actualDuration, isEstimated)
        }
    }

    fun toggleSubtask(subtaskId: Long, currentCompleted: Boolean) {
        viewModelScope.launch {
            repository.setSubtaskCompleted(subtaskId, !currentCompleted)
        }
    }

    fun saveItemNotes(itemId: Long, notes: String?, report: String?) {
        viewModelScope.launch {
            repository.saveItemNotes(itemId, notes, report)
        }
    }

    fun saveTestResult(
        itemId: Long,
        subject: String,
        topic: String,
        total: Int,
        correct: Int,
        wrong: Int,
        blank: Int
    ) {
        viewModelScope.launch {
            val percentage = PersianUtils.calculateKonkurPercentage(correct, wrong, total)
            val entity = TestResultEntity(
                itemId = itemId,
                subject = subject,
                topic = topic,
                totalQuestions = total,
                correctAnswers = correct,
                wrongAnswers = wrong,
                blankAnswers = blank,
                percentage = percentage
            )
            repository.saveTestResult(entity)
        }
    }

    fun startTimer(context: Context, fullItem: FullStudyItem) {
        val slotText = fullItem.slots.joinToString(" و ") {
            PersianUtils.toPersianDigits("${it.startTime} تا ${it.endTime}")
        }
        val targetMinutes = fullItem.totalPlannedMinutes
        StudyTimerService.startTimer(
            context = context,
            itemId = fullItem.item.id,
            subject = fullItem.item.subject,
            resource = fullItem.item.resource ?: "",
            slotText = slotText,
            targetMinutes = targetMinutes
        )
    }

    fun pauseTimer(context: Context) {
        StudyTimerService.pauseTimer(context)
    }

    fun resumeTimer(context: Context) {
        StudyTimerService.resumeTimer(context)
    }

    fun finishActiveTimerAndComplete(context: Context) {
        val currentSession = activeTimerSession.value ?: return
        val elapsedMinutes = ((currentSession.elapsedSeconds + 59) / 60).toInt().coerceAtLeast(1)
        val itemId = currentSession.itemId

        viewModelScope.launch {
            if (itemId > 0) {
                repository.setItemCompleted(
                    itemId = itemId,
                    completed = true,
                    actualDuration = elapsedMinutes,
                    isEstimated = false
                )
            }
            StudyTimerService.stopTimer(context)
        }
    }

    fun cancelActiveTimer(context: Context) {
        StudyTimerService.stopTimer(context)
    }

    fun parseScheduleText(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            _aiParseState.value = AiParseState.Parsing
            val result = geminiParser.parseScheduleText(text)
            result.onSuccess { dto ->
                if (dto.days.isEmpty()) {
                    _aiParseState.value = AiParseState.Error("برنامه‌ای در متن وارد شده شناسایی نشد.")
                } else {
                    _aiParseState.value = AiParseState.Preview(dto)
                }
            }.onFailure { err ->
                _aiParseState.value = AiParseState.Error(err.message ?: "خطا در تحلیل هوش مصنوعی")
            }
        }
    }

    fun parseScheduleImage(bitmap: Bitmap, notes: String = "") {
        viewModelScope.launch {
            _aiParseState.value = AiParseState.Parsing
            val result = geminiParser.parseScheduleImage(bitmap, notes)
            result.onSuccess { dto ->
                _aiParseState.value = AiParseState.Preview(dto)
            }.onFailure { err ->
                _aiParseState.value = AiParseState.Error(err.message ?: "خطا در پردازش تصویر برنامه")
            }
        }
    }

    fun saveParsedSchedule(dto: ScheduleResponseDto) {
        viewModelScope.launch {
            repository.saveScheduleResponse(dto)
            _aiParseState.value = AiParseState.Idle
            // select latest day
            _selectedDayIndex.value = (allDays.value.size - 1).coerceAtLeast(0)
        }
    }

    fun clearAiParseState() {
        _aiParseState.value = AiParseState.Idle
    }

    fun updateProfile(profile: UserProfile) {
        _userProfile.value = profile
        repository.saveUserProfile(profile)
    }

    fun generateConsultantReportText(day: FullStudyDay): String {
        val profile = _userProfile.value
        val sb = StringBuilder()
        sb.append("📋 گزارش مطالعه روزانه برای ${profile.consultantName}\n")
        sb.append("👤 دانش‌آموز: ${profile.name} (${profile.grade} - ${profile.major})\n")
        sb.append("📅 تاریخ: ${day.day.title}\n")
        sb.append("⏱ میزان پیشرفت: ${PersianUtils.toPersianDigits((day.progressFraction * 100).toInt())}٪\n")
        val studiedHours = (day.completedMinutes / 60f)
        val plannedHours = (day.totalPlannedMinutes / 60f)
        sb.append("⌛ زمان مطالعه: ${PersianUtils.toPersianDigits(String.format("%.1f", studiedHours))} ساعت از ${PersianUtils.toPersianDigits(String.format("%.1f", plannedHours))} ساعت برنامه‌ریزی‌شده\n")
        sb.append("─────────────────────\n")

        day.items.forEachIndexed { idx, fullItem ->
            val statusIcon = if (fullItem.item.isCompleted) "✅" else "⏳"
            val slotsStr = fullItem.slots.joinToString("، ") { "${it.startTime} تا ${it.endTime}" }
            sb.append("${idx + 1}. $statusIcon ${fullItem.item.subject} ($slotsStr)\n")
            if (!fullItem.item.resource.isNullOrEmpty()) {
                sb.append("   📖 منبع: ${fullItem.item.resource}\n")
            }
            if (fullItem.testResult != null) {
                val tr = fullItem.testResult
                sb.append("   🎯 آزمون/تست: ${tr.totalQuestions} سوال | درست: ${tr.correctAnswers} | غلط: ${tr.wrongAnswers} | درصد: ${tr.percentage}٪\n")
            }
            if (!fullItem.item.consultantReport.isNullOrEmpty()) {
                sb.append("   💬 پاسخ به درخواست مشاور: ${fullItem.item.consultantReport}\n")
            }
        }
        sb.append("─────────────────────\n")
        sb.append("ارسال شده از طریق اپلیکیشن «کنکور یار»")
        return PersianUtils.toPersianDigits(sb.toString())
    }
}
