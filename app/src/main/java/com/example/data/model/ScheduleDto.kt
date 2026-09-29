package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ScheduleResponseDto(
    @Json(name = "days") val days: List<DayDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class DayDto(
    @Json(name = "date") val date: String = "",
    @Json(name = "weekday") val weekday: String = "",
    @Json(name = "items") val items: List<ItemDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ItemDto(
    @Json(name = "subject") val subject: String = "",
    @Json(name = "activity") val activity: String = "reading", // video|note_taking|reading|exercise|test|analysis|review|exam|other
    @Json(name = "slots") val slots: List<SlotDto> = emptyList(),
    @Json(name = "resource") val resource: String? = null,
    @Json(name = "teacher") val teacher: String? = null,
    @Json(name = "target") val target: TargetDto? = null,
    @Json(name = "subtasks") val subtasks: List<String> = emptyList(),
    @Json(name = "instructions") val instructions: String? = null,
    @Json(name = "askedToReport") val askedToReport: Boolean = false,
    @Json(name = "needsReview") val needsReview: Boolean = false,
    @Json(name = "reviewNote") val reviewNote: String? = null
)

@JsonClass(generateAdapter = true)
data class SlotDto(
    @Json(name = "start") val start: String = "",
    @Json(name = "end") val end: String = ""
)

@JsonClass(generateAdapter = true)
data class TargetDto(
    @Json(name = "kind") val kind: String = "tests", // tests|video|pages
    @Json(name = "value") val value: Int = 0
)

data class UserProfile(
    val name: String = "دانش‌آموز دوازدهم",
    val grade: String = "پایه دوازدهم",
    val major: String = "ریاضی و فیزیک",
    val consultantName: String = "استاد حیدری",
    val dailyTargetHours: Float = 8.0f,
    val targetExam: String = "کنکور سراسری ریاضی",
    val konkurDateShamsi: String = "۱۴۰۶/۰۴/۰۵" // Optional Shamsi Konkur date
)
