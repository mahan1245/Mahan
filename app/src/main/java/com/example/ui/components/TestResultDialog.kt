package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SuccessGreen
import com.example.util.PersianUtils

@Composable
fun TestResultDialog(
    subject: String,
    initialTotal: Int = 20,
    initialCorrect: Int = 0,
    initialWrong: Int = 0,
    onDismiss: () -> Unit,
    onSave: (total: Int, correct: Int, wrong: Int, blank: Int) -> Unit
) {
    var totalText by remember { mutableStateOf(if (initialTotal > 0) initialTotal.toString() else "20") }
    var correctText by remember { mutableStateOf(if (initialCorrect > 0) initialCorrect.toString() else "") }
    var wrongText by remember { mutableStateOf(if (initialWrong > 0) initialWrong.toString() else "") }
    var topicText by remember { mutableStateOf("") }

    val total = PersianUtils.toEnglishDigits(totalText).toIntOrNull() ?: 0
    val correct = PersianUtils.toEnglishDigits(correctText).toIntOrNull() ?: 0
    val wrong = PersianUtils.toEnglishDigits(wrongText).toIntOrNull() ?: 0
    val blank = (total - correct - wrong).coerceAtLeast(0)

    val percentage = PersianUtils.calculateKonkurPercentage(correct, wrong, total)

    val percentColor = when {
        percentage >= 70.0 -> SuccessGreen
        percentage >= 45.0 -> PrimaryBlue
        else -> ErrorRed
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "ثبت درصد و کارنامه: $subject",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "محاسبه بر اساس فرمول نمره منفی کنکور سراسری",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("test_result_dialog")
            ) {
                // Live Percentage Preview Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(percentColor.copy(alpha = 0.12f))
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "درصد کنکوری محاسبه‌شده",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = PersianUtils.toPersianDigits("$percentage٪"),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 28.sp
                            ),
                            color = percentColor
                        )
                        Text(
                            text = PersianUtils.toPersianDigits("نزده: $blank سوال"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = totalText,
                    onValueChange = { totalText = it },
                    label = { Text("تعداد کل سوالات") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = correctText,
                        onValueChange = { correctText = it },
                        label = { Text("درست (صحیح)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = wrongText,
                        onValueChange = { wrongText = it },
                        label = { Text("غلط (منفی)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(total, correct, wrong, blank)
                },
                enabled = total > 0 && (correct + wrong) <= total
            ) {
                Text("ثبت کارنامه")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("انصراف")
            }
        }
    )
}
