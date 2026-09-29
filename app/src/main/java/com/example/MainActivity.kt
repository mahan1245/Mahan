package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.DailyChecklistScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.UploadScheduleScreen
import com.example.ui.theme.AccentGold
import com.example.ui.theme.KonkurYarTheme
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.Slate900
import com.example.ui.viewmodel.StudyViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: StudyViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            KonkurYarTheme {
                // Request Notification Permission for Study Timer on Android 13+
                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { _ -> }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                var selectedNavIndex by rememberSaveable { mutableIntStateOf(0) }

                BackHandler(enabled = selectedNavIndex != 0) {
                    selectedNavIndex = 0
                }

                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(PrimaryBlue),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "ک",
                                            fontWeight = FontWeight.Black,
                                            color = Color.White,
                                            fontSize = 15.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "کنکور یار",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Black,
                                                letterSpacing = 0.5.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "برنامه‌ریز دوازدهم ریاضی",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.testTag("bottom_nav_bar")
                        ) {
                            NavigationBarItem(
                                selected = selectedNavIndex == 0,
                                onClick = { selectedNavIndex = 0 },
                                icon = {
                                    Icon(
                                        imageVector = if (selectedNavIndex == 0) Icons.Filled.Checklist else Icons.Outlined.Checklist,
                                        contentDescription = "برنامه روزانه"
                                    )
                                },
                                label = { Text("چک‌لیست") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PrimaryBlue,
                                    selectedTextColor = PrimaryBlue,
                                    indicatorColor = PrimaryBlue.copy(alpha = 0.15f)
                                ),
                                modifier = Modifier.testTag("nav_checklist")
                            )

                            NavigationBarItem(
                                selected = selectedNavIndex == 1,
                                onClick = { selectedNavIndex = 1 },
                                icon = {
                                    Icon(
                                        imageVector = if (selectedNavIndex == 1) Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome,
                                        contentDescription = "تحلیل هوشمند"
                                    )
                                },
                                label = { Text("تبدیل برنامه") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PrimaryBlue,
                                    selectedTextColor = PrimaryBlue,
                                    indicatorColor = PrimaryBlue.copy(alpha = 0.15f)
                                ),
                                modifier = Modifier.testTag("nav_upload")
                            )

                            NavigationBarItem(
                                selected = selectedNavIndex == 2,
                                onClick = { selectedNavIndex = 2 },
                                icon = {
                                    Icon(
                                        imageVector = if (selectedNavIndex == 2) Icons.Filled.Insights else Icons.Outlined.Insights,
                                        contentDescription = "آنالیز و آمار"
                                    )
                                },
                                label = { Text("آنالیز") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PrimaryBlue,
                                    selectedTextColor = PrimaryBlue,
                                    indicatorColor = PrimaryBlue.copy(alpha = 0.15f)
                                ),
                                modifier = Modifier.testTag("nav_analytics")
                            )

                            NavigationBarItem(
                                selected = selectedNavIndex == 3,
                                onClick = { selectedNavIndex = 3 },
                                icon = {
                                    Icon(
                                        imageVector = if (selectedNavIndex == 3) Icons.Filled.Person else Icons.Outlined.Person,
                                        contentDescription = "پروفایل"
                                    )
                                },
                                label = { Text("پروفایل") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PrimaryBlue,
                                    selectedTextColor = PrimaryBlue,
                                    indicatorColor = PrimaryBlue.copy(alpha = 0.15f)
                                ),
                                modifier = Modifier.testTag("nav_profile")
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (selectedNavIndex) {
                            0 -> DailyChecklistScreen(
                                viewModel = viewModel,
                                onNavigateToUpload = { selectedNavIndex = 1 }
                            )
                            1 -> UploadScheduleScreen(
                                viewModel = viewModel,
                                onSavedSuccessfully = { selectedNavIndex = 0 }
                            )
                            2 -> AnalyticsScreen(
                                viewModel = viewModel
                            )
                            3 -> ProfileScreen(
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }
}
