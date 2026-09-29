package com.example.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.FullStudyItem

@Composable
fun ReportDialog(
    fullItem: FullStudyItem,
    onDismiss: () -> Unit,
    onSaveNotes: (notes: String?, report: String?) -> Unit
) {
    val context = LocalContext.current
    var notesText by remember { mutableStateOf(fullItem.item.userNotes ?: "") }
    var reportText by remember { mutableStateOf(fullItem.item.consultantReport ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "گزارش و یادداشت: ${fullItem.item.subject}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                if (fullItem.item.askedToReport) {
                    Text(
                        text = "مشاور خواسته: هر چه قدر انجام شد گزارش داده شود",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (fullItem.item.instructions != null) {
                    Text(
                        text = "دستور مشاور: ${fullItem.item.instructions}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = reportText,
                    onValueChange = { reportText = it },
                    label = { Text("متن گزارش برای مشاور") },
                    placeholder = { Text("مثلاً: فیلم کامل دیده شد و جزوه‌نویسی تا صفحه ۴۵ انجام شد...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("یادداشت شخصی دانش‌آموز") },
                    placeholder = { Text("نکات برای مرور بعدی، تست‌های نشان‌دار...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveNotes(
                        notesText.ifBlank { null },
                        reportText.ifBlank { null }
                    )
                    onDismiss()
                }
            ) {
                Text("ذخیره")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = {
                    if (reportText.isNotBlank()) {
                        shareText(context, "گزارش ${fullItem.item.subject}:\n$reportText")
                    }
                }
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = "اشتراک‌گذاری")
                Spacer(modifier = Modifier.height(4.dp))
                Text("ارسال به مشاور")
            }
        }
    )
}

private fun shareText(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "ارسال به مشاور"))
}
