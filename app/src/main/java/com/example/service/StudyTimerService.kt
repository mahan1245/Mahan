package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.util.PersianUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ActiveStudySession(
    val itemId: Long = 0,
    val subject: String = "",
    val resource: String = "",
    val slotText: String = "",
    val targetDurationMinutes: Int = 60,
    val elapsedSeconds: Long = 0,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false
)

object TimerManager {
    private val _sessionState = MutableStateFlow<ActiveStudySession?>(null)
    val sessionState: StateFlow<ActiveStudySession?> = _sessionState.asStateFlow()

    fun updateSession(session: ActiveStudySession?) {
        _sessionState.value = session
    }

    fun tick() {
        val current = _sessionState.value ?: return
        if (current.isRunning && !current.isPaused) {
            _sessionState.value = current.copy(elapsedSeconds = current.elapsedSeconds + 1)
        }
    }
}

class StudyTimerService : Service() {

    companion object {
        const val CHANNEL_ID = "konkur_study_timer_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_FINISH = "ACTION_FINISH"

        const val EXTRA_ITEM_ID = "EXTRA_ITEM_ID"
        const val EXTRA_SUBJECT = "EXTRA_SUBJECT"
        const val EXTRA_RESOURCE = "EXTRA_RESOURCE"
        const val EXTRA_SLOT_TEXT = "EXTRA_SLOT_TEXT"
        const val EXTRA_TARGET_MINUTES = "EXTRA_TARGET_MINUTES"

        fun startTimer(
            context: Context,
            itemId: Long,
            subject: String,
            resource: String,
            slotText: String,
            targetMinutes: Int
        ) {
            val intent = Intent(context, StudyTimerService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_ITEM_ID, itemId)
                putExtra(EXTRA_SUBJECT, subject)
                putExtra(EXTRA_RESOURCE, resource)
                putExtra(EXTRA_SLOT_TEXT, slotText)
                putExtra(EXTRA_TARGET_MINUTES, targetMinutes)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun pauseTimer(context: Context) {
            val intent = Intent(context, StudyTimerService::class.java).apply {
                action = ACTION_PAUSE
            }
            context.startService(intent)
        }

        fun resumeTimer(context: Context) {
            val intent = Intent(context, StudyTimerService::class.java).apply {
                action = ACTION_RESUME
            }
            context.startService(intent)
        }

        fun stopTimer(context: Context) {
            val intent = Intent(context, StudyTimerService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var tickerJob: Job? = null
    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val itemId = intent.getLongExtra(EXTRA_ITEM_ID, 0)
                val subject = intent.getStringExtra(EXTRA_SUBJECT) ?: "مطالعه کنکور"
                val resource = intent.getStringExtra(EXTRA_RESOURCE) ?: ""
                val slotText = intent.getStringExtra(EXTRA_SLOT_TEXT) ?: ""
                val targetMinutes = intent.getIntExtra(EXTRA_TARGET_MINUTES, 60)

                val newSession = ActiveStudySession(
                    itemId = itemId,
                    subject = subject,
                    resource = resource,
                    slotText = slotText,
                    targetDurationMinutes = targetMinutes,
                    elapsedSeconds = 0,
                    isRunning = true,
                    isPaused = false
                )
                TimerManager.updateSession(newSession)
                startForeground(NOTIFICATION_ID, buildNotification(newSession))
                startTicker()
            }
            ACTION_PAUSE -> {
                val current = TimerManager.sessionState.value
                if (current != null) {
                    val updated = current.copy(isPaused = true)
                    TimerManager.updateSession(updated)
                    notificationManager.notify(NOTIFICATION_ID, buildNotification(updated))
                }
            }
            ACTION_RESUME -> {
                val current = TimerManager.sessionState.value
                if (current != null) {
                    val updated = current.copy(isPaused = false)
                    TimerManager.updateSession(updated)
                    notificationManager.notify(NOTIFICATION_ID, buildNotification(updated))
                }
            }
            ACTION_STOP, ACTION_FINISH -> {
                stopTicker()
                TimerManager.updateSession(null)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = serviceScope.launch {
            while (true) {
                delay(1000)
                val session = TimerManager.sessionState.value
                if (session == null || !session.isRunning) {
                    break
                }
                if (!session.isPaused) {
                    TimerManager.tick()
                    val updated = TimerManager.sessionState.value
                    if (updated != null) {
                        notificationManager.notify(NOTIFICATION_ID, buildNotification(updated))
                    }
                }
            }
        }
    }

    private fun stopTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "بازه مطالعه کنکور",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "نمایش زمان زنده مطالعه بازه جاری حتی هنگام قفل بودن صفحه"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(session: ActiveStudySession): Notification {
        val appIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseResumeIntent = Intent(this, StudyTimerService::class.java).apply {
            action = if (session.isPaused) ACTION_RESUME else ACTION_PAUSE
        }
        val pauseResumePendingIntent = PendingIntent.getService(
            this,
            1,
            pauseResumeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val finishIntent = Intent(this, StudyTimerService::class.java).apply {
            action = ACTION_FINISH
        }
        val finishPendingIntent = PendingIntent.getService(
            this,
            2,
            finishIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val timeString = PersianUtils.formatSecondsToTime(session.elapsedSeconds)
        val targetString = PersianUtils.toPersianDigits("${session.targetDurationMinutes} دقیقه")
        val statusText = if (session.isPaused) "(متوقف شده)" else ""

        val contentText = if (session.resource.isNotEmpty()) {
            "${session.resource} | زمان: $timeString ($targetString) $statusText"
        } else {
            "زمان سپری شده: $timeString از $targetString $statusText"
        }

        val pauseResumeLabel = if (session.isPaused) "ادامه مطالعه ▶" else "توقف موقت ⏸"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("مطالعه فعال: ${session.subject} ${session.slotText}".trim())
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(contentPendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(0, pauseResumeLabel, pauseResumePendingIntent)
            .addAction(0, "پایان بازه ✔", finishPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    override fun onDestroy() {
        stopTicker()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
