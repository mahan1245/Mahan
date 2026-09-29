package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FullStudyItem
import com.example.ui.theme.CalculusBlue
import com.example.ui.theme.ChemistryAmber
import com.example.ui.theme.DiscreteIndigo
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GeneralGreen
import com.example.ui.theme.GeometryTeal
import com.example.ui.theme.PhysicsPurple
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningOrange
import com.example.util.PersianUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudyItemCard(
    fullItem: FullStudyItem,
    isTimerRunningForThisItem: Boolean,
    onToggleCompleted: () -> Unit,
    onToggleSubtask: (subtaskId: Long, current: Boolean) -> Unit,
    onStartTimer: () -> Unit,
    onOpenTestResult: () -> Unit,
    onOpenReport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val item = fullItem.item
    val subjectColor = getSubjectColor(item.subject)

    val cardBg by animateColorAsState(
        targetValue = if (item.isCompleted) {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        label = "CardBgAnimation"
    )

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = if (item.isCompleted) 1.dp else 3.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = if (isTimerRunningForThisItem) 2.dp else 1.dp,
                color = if (isTimerRunningForThisItem) PrimaryBlue else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(18.dp)
            )
            .testTag("study_item_card_${item.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Subject, Activity Tag, Completion Checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Subject tag pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(subjectColor.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = item.subject,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            color = subjectColor
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Activity pill
                    val activityLabel = getActivityLabel(item.activity)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = activityLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (item.teacher != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "استاد ${item.teacher}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Completion status icon / toggle button
                IconButton(
                    onClick = onToggleCompleted,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (item.isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                        contentDescription = if (item.isCompleted) "انجام شده" else "انجام نشده",
                        tint = if (item.isCompleted) SuccessGreen else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Resource title if available
            if (!item.resource.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = item.resource,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (item.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                )
            }

            // Time Slots Badge Row
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                for (slot in fullItem.slots) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            val slotTimeStr = PersianUtils.toPersianDigits("${slot.startTime} تا ${slot.endTime}")
                            val slotDurationStr = PersianUtils.formatDurationPersian(slot.durationMinutes)
                            Text(
                                text = "$slotTimeStr ($slotDurationStr)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Target badge
                if (item.targetValue != null && item.targetValue > 0) {
                    val targetUnit = when (item.targetKind) {
                        "tests" -> "تست"
                        "video" -> "قسمت"
                        "pages" -> "صفحه"
                        else -> "مورد"
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AccentGoldLight.copy(alpha = 0.35f)
                    ) {
                        Text(
                            text = "هدف: ${PersianUtils.toPersianDigits(item.targetValue)} $targetUnit",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = ChemistryAmber,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Completion duration badge
                if (item.isCompleted) {
                    val durationLabel = if (item.isEstimated) {
                        "تخمینی: ${PersianUtils.formatDurationPersian(item.actualDurationMinutes)}"
                    } else {
                        "زمان واقعی: ${PersianUtils.formatDurationPersian(item.actualDurationMinutes)}"
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SuccessGreen.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = durationLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = SuccessGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Verbatim Consultant Instructions
            if (!item.instructions.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Column {
                        Text(
                            text = "دستور مشاور:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = item.instructions,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Ambiguity warning (needsReview)
            if (item.needsReview) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(WarningOrange.copy(alpha = 0.15f))
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = "نیازمند بازبینی",
                        tint = WarningOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.reviewNote ?: "این آیتم نیازمند بازبینی است",
                        style = MaterialTheme.typography.labelSmall,
                        color = WarningOrange
                    )
                }
            }

            // Subtasks Checklist
            if (fullItem.subtasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "ریزاقدامات و مراحل:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Column(modifier = Modifier.padding(top = 2.dp)) {
                    for (subtask in fullItem.subtasks) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggleSubtask(subtask.id, subtask.isCompleted) }
                                .padding(vertical = 2.dp)
                        ) {
                            Checkbox(
                                checked = subtask.isCompleted,
                                onCheckedChange = { onToggleSubtask(subtask.id, subtask.isCompleted) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = SuccessGreen
                                ),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = subtask.title,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    textDecoration = if (subtask.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                ),
                                color = if (subtask.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Test Result Summary if recorded
            if (fullItem.testResult != null) {
                Spacer(modifier = Modifier.height(8.dp))
                val tr = fullItem.testResult
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = PrimaryBlue.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Assignment,
                                contentDescription = "نتیجه تست",
                                tint = PrimaryBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            val resultStr = PersianUtils.toPersianDigits("${tr.totalQuestions} سوال (درست: ${tr.correctAnswers} | غلط: ${tr.wrongAnswers})")
                            Text(
                                text = resultStr,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = PersianUtils.toPersianDigits("${tr.percentage}٪"),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                            color = PrimaryBlue
                        )
                    }
                }
            }

            // Consultant Report note if answered
            if (!item.consultantReport.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "گزارش ثبت شده برای مشاور: ${item.consultantReport}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 2
                )
            }

            // Bottom Action Buttons: Timer, Test, Report
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Timer Button
                OutlinedButton(
                    onClick = onStartTimer,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "شروع بازه",
                        modifier = Modifier.size(16.dp),
                        tint = PrimaryBlue
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isTimerRunningForThisItem) "در حال ثبت" else "تایمر بازه",
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                // Test score button for test/exam/analysis items
                if (item.activity in listOf("test", "exam", "analysis") || item.targetKind == "tests") {
                    OutlinedButton(
                        onClick = onOpenTestResult,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = "کارنامه تست",
                            modifier = Modifier.size(16.dp),
                            tint = ChemistryAmber
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (fullItem.testResult != null) "ویرایش درصد" else "ثبت درصد",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                // Consultant Report button
                OutlinedButton(
                    onClick = onOpenReport,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(if (item.activity in listOf("test", "exam", "analysis")) 1f else 1.2f)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "گزارش مشاور",
                        modifier = Modifier.size(16.dp),
                        tint = if (item.askedToReport) WarningOrange else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (item.askedToReport) "گزارش مشاور ❗" else "یادداشت/گزارش",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

private fun getSubjectColor(subject: String): Color {
    return when {
        subject.contains("هندسه") -> GeometryTeal
        subject.contains("حسابان") || subject.contains("ریاضی") -> CalculusBlue
        subject.contains("فیزیک") -> PhysicsPurple
        subject.contains("شیمی") -> ChemistryAmber
        subject.contains("گسسته") || subject.contains("آمار") -> DiscreteIndigo
        else -> GeneralGreen
    }
}

private fun getActivityLabel(activity: String): String {
    return when (activity) {
        "video" -> "تماشای فیلم"
        "test" -> "حل تست"
        "exam" -> "آزمون"
        "analysis" -> "تحلیل آزمون"
        "reading" -> "مطالعه و جزوه"
        "exercise" -> "تمرین تشریحی"
        "review" -> "مرور سریع"
        else -> "مطالعه"
    }
}
val AccentGoldLight = Color(0xFFFDE68A)
