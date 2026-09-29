package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.FullStudyDay
import com.example.data.model.FullStudyItem
import com.example.ui.components.ActiveTimerBanner
import com.example.ui.components.ProgressRing
import com.example.ui.components.ReportDialog
import com.example.ui.components.StudyItemCard
import com.example.ui.components.TestResultDialog
import com.example.ui.theme.AccentGold
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.Slate900
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.StudyViewModel
import com.example.util.PersianUtils

@Composable
fun DailyChecklistScreen(
    viewModel: StudyViewModel,
    onNavigateToUpload: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val days by viewModel.allDays.collectAsStateWithLifecycle()
    val selectedDayIndex by viewModel.selectedDayIndex.collectAsStateWithLifecycle()
    val currentDay by viewModel.currentDay.collectAsStateWithLifecycle()
    val activeSession by viewModel.activeTimerSession.collectAsStateWithLifecycle()

    var testDialogItem by remember { mutableStateOf<FullStudyItem?>(null) }
    var reportDialogItem by remember { mutableStateOf<FullStudyItem?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToUpload,
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_schedule")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "افزودن برنامه جدید")
            }
        },
        bottomBar = {
            ActiveTimerBanner(
                session = activeSession,
                onPause = { viewModel.pauseTimer(context) },
                onResume = { viewModel.resumeTimer(context) },
                onFinish = { viewModel.finishActiveTimerAndComplete(context) },
                onCancel = { viewModel.cancelActiveTimer(context) }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Day selector tabs
            if (days.isNotEmpty()) {
                ScrollableTabRow(
                    selectedTabIndex = selectedDayIndex.coerceIn(0, days.size - 1),
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = PrimaryBlue,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    days.forEachIndexed { index, studyDay ->
                        val isSelected = index == selectedDayIndex
                        Tab(
                            selected = isSelected,
                            onClick = { viewModel.selectDay(index) },
                            text = {
                                Text(
                                    text = PersianUtils.toPersianDigits(studyDay.day.title),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp
                                )
                            }
                        )
                    }
                }
            }

            if (currentDay == null) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "هنوز برنامه‌ای ثبت نشده است",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "برای تبدیل خودکار برنامه مشاور با هوش مصنوعی دکمه زیر را لمس کنید",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onNavigateToUpload,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Text("ثبت و تحلیل برنامه مشاور")
                        }
                    }
                }
            } else {
                val day = currentDay!!
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        // Daily Progress Summary Card
                        DailyProgressHeaderCard(
                            day = day,
                            onShareConsultantReport = {
                                val reportText = viewModel.generateConsultantReportText(day)
                                shareReport(context, reportText)
                            }
                        )
                    }

                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "چک‌لیست بازه‌های درسی:",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = PersianUtils.toPersianDigits("${day.items.count { it.item.isCompleted }} از ${day.items.size} انجام شده"),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    items(day.items, key = { it.item.id }) { fullItem ->
                        val isItemTimerActive = activeSession?.itemId == fullItem.item.id
                        StudyItemCard(
                            fullItem = fullItem,
                            isTimerRunningForThisItem = isItemTimerActive,
                            onToggleCompleted = {
                                viewModel.toggleItemCompleted(fullItem.item, fullItem.totalPlannedMinutes)
                            },
                            onToggleSubtask = { subtaskId, cur ->
                                viewModel.toggleSubtask(subtaskId, cur)
                            },
                            onStartTimer = {
                                viewModel.startTimer(context, fullItem)
                            },
                            onOpenTestResult = {
                                testDialogItem = fullItem
                            },
                            onOpenReport = {
                                reportDialogItem = fullItem
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp)) // Space for FAB & Bottom bar
                    }
                }
            }
        }
    }

    // Test Dialog
    if (testDialogItem != null) {
        val item = testDialogItem!!
        TestResultDialog(
            subject = item.item.subject,
            initialTotal = item.item.targetValue ?: 20,
            initialCorrect = item.testResult?.correctAnswers ?: 0,
            initialWrong = item.testResult?.wrongAnswers ?: 0,
            onDismiss = { testDialogItem = null },
            onSave = { total, correct, wrong, blank ->
                viewModel.saveTestResult(
                    itemId = item.item.id,
                    subject = item.item.subject,
                    topic = item.item.resource ?: "",
                    total = total,
                    correct = correct,
                    wrong = wrong,
                    blank = blank
                )
                testDialogItem = null
            }
        )
    }

    // Report Dialog
    if (reportDialogItem != null) {
        val item = reportDialogItem!!
        ReportDialog(
            fullItem = item,
            onDismiss = { reportDialogItem = null },
            onSaveNotes = { notes, report ->
                viewModel.saveItemNotes(item.item.id, notes, report)
            }
        )
    }
}

@Composable
private fun DailyProgressHeaderCard(
    day: FullStudyDay,
    onShareConsultantReport: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Left side: Progress Ring
                ProgressRing(
                    progress = day.progressFraction,
                    size = 92.dp,
                    strokeWidth = 9.dp
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Right side: Statistics
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = PersianUtils.toPersianDigits(day.day.title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    val completedDuration = PersianUtils.formatDurationPersian(day.completedMinutes)
                    val totalDuration = PersianUtils.formatDurationPersian(day.totalPlannedMinutes)

                    Text(
                        text = "زمان اجرا: $completedDuration",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = SuccessGreen
                    )
                    Text(
                        text = "کل برنامه: $totalDuration",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val remainingMins = (day.totalPlannedMinutes - day.completedMinutes).coerceAtLeast(0)
                    if (remainingMins > 0) {
                        Text(
                            text = "باقی‌مانده: ${PersianUtils.formatDurationPersian(remainingMins)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = AccentGold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action button to export / send daily report to consultant
            Button(
                onClick = onShareConsultantReport,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Slate900,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "تولید و ارسال گزارش روزانه برای مشاور",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

private fun shareReport(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "ارسال گزارش به مشاور"))
}
