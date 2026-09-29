package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.FullStudyDay
import com.example.data.model.TestResultEntity
import com.example.data.model.UserProfile
import com.example.ui.theme.AccentGold
import com.example.ui.theme.CalculusBlue
import com.example.ui.theme.ChemistryAmber
import com.example.ui.theme.DiscreteIndigo
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GeometryTeal
import com.example.ui.theme.PhysicsPurple
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.Slate900
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.StudyViewModel
import com.example.util.PersianUtils

@Composable
fun AnalyticsScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val days by viewModel.allDays.collectAsStateWithLifecycle()
    val testResults by viewModel.allTestResults.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("analytics_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Konkur Countdown Card
        item {
            KonkurCountdownCard(profile = userProfile)
        }

        // Overview Summary Cards
        item {
            StudyOverviewRow(days = days, profile = userProfile)
        }

        // Daily Study Hours Bars
        item {
            DailyStudyBarsCard(days = days, targetDailyHours = userProfile.dailyTargetHours)
        }

        // Subject Breakdown
        item {
            SubjectDistributionCard(days = days)
        }

        // Test Performance Analytics
        item {
            TestPerformanceCard(testResults = testResults)
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun KonkurCountdownCard(profile: UserProfile) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(AccentGold.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassTop,
                            contentDescription = null,
                            tint = AccentGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "شمارش معکوس ${profile.targetExam}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "تاریخ تخمینی: ${PersianUtils.toPersianDigits(profile.konkurDateShamsi)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Realistic estimation: approx ~270 days for typical 12th math session
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CountdownPill(value = "۲۷۵", label = "روز باقی‌مانده")
                CountdownPill(value = "۳۹", label = "هفته مطالعه")
                CountdownPill(value = "۶,۶۰۰", label = "ساعت تا آزمون")
            }
        }
    }
}

@Composable
private fun CountdownPill(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
            color = AccentGold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun StudyOverviewRow(days: List<FullStudyDay>, profile: UserProfile) {
    val totalStudiedMinutes = days.sumOf { it.completedMinutes }
    val totalPlannedMinutes = days.sumOf { it.totalPlannedMinutes }
    val totalStudiedHours = totalStudiedMinutes / 60f

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.weight(1f)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "کل ساعات مطالعه",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = PersianUtils.toPersianDigits(String.format("%.1f ساعت", totalStudiedHours)),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryBlue
                )
            }
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.weight(1f)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "هدف روزانه",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = PersianUtils.toPersianDigits("${profile.dailyTargetHours.toInt()} ساعت"),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = SuccessGreen
                )
            }
        }
    }
}

@Composable
private fun DailyStudyBarsCard(days: List<FullStudyDay>, targetDailyHours: Float) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "روند مطالعه روزانه در برنامه مشاور:",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(14.dp))

            for (day in days) {
                val studiedHours = day.completedMinutes / 60f
                val plannedHours = day.totalPlannedMinutes / 60f
                val fraction = if (plannedHours > 0) (studiedHours / plannedHours).coerceIn(0f, 1f) else 0f

                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = PersianUtils.toPersianDigits(day.day.title),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = PersianUtils.toPersianDigits(
                                "${String.format("%.1f", studiedHours)} از ${String.format("%.1f", plannedHours)} ساعت"
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    LinearProgressIndicator(
                        progress = { fraction },
                        color = if (fraction >= 0.9f) SuccessGreen else PrimaryBlue,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = StrokeCap.Round,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                }
            }
        }
    }
}

@Composable
private fun SubjectDistributionCard(days: List<FullStudyDay>) {
    val subjectMinutes = mutableMapOf<String, Int>()
    days.forEach { day ->
        day.items.forEach { fullItem ->
            val cur = subjectMinutes.getOrDefault(fullItem.item.subject, 0)
            subjectMinutes[fullItem.item.subject] = cur + fullItem.totalPlannedMinutes
        }
    }

    val totalMin = subjectMinutes.values.sum().coerceAtLeast(1)

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "توزیع موضوعی دروس (پایه دوازدهم):",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            subjectMinutes.entries.sortedByDescending { it.value }.forEach { (subject, mins) ->
                val fraction = mins.toFloat() / totalMin
                val hours = mins / 60f
                val color = when {
                    subject.contains("هندسه") -> GeometryTeal
                    subject.contains("حسابان") -> CalculusBlue
                    subject.contains("فیزیک") -> PhysicsPurple
                    subject.contains("شیمی") -> ChemistryAmber
                    subject.contains("گسسته") -> DiscreteIndigo
                    else -> SuccessGreen
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = subject,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = PersianUtils.toPersianDigits("${String.format("%.1f", hours)} ساعت (${(fraction * 100).toInt()}٪)"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun TestPerformanceCard(testResults: List<TestResultEntity>) {
    val totalTests = testResults.sumOf { it.totalQuestions }
    val totalCorrect = testResults.sumOf { it.correctAnswers }
    val totalWrong = testResults.sumOf { it.wrongAnswers }
    val avgPercentage = if (totalTests > 0) {
        PersianUtils.calculateKonkurPercentage(totalCorrect, totalWrong, totalTests)
    } else {
        0.0
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "آمار تست‌ها و آزمون‌های آزمایشی:",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = PersianUtils.toPersianDigits("میانگین درصد: $avgPercentage٪"),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                    color = if (avgPercentage >= 60) SuccessGreen else PrimaryBlue
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatBox(label = "کل تست‌ها", value = PersianUtils.toPersianDigits(totalTests), color = PrimaryBlue)
                StatBox(label = "صحیح", value = PersianUtils.toPersianDigits(totalCorrect), color = SuccessGreen)
                StatBox(label = "غلط", value = PersianUtils.toPersianDigits(totalWrong), color = ErrorRed)
            }
        }
    }
}

@Composable
private fun StatBox(label: String, value: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f),
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
