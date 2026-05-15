package com.example.fixmyroad.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fixmyroad.domain.model.Report
import com.example.fixmyroad.ui.theme.*
import com.example.fixmyroad.ui.viewmodel.ReportViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToReportIssue: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSupport: () -> Unit,
    onNavigateToDetails: (String) -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {

    val reports by viewModel.allReports.collectAsStateWithLifecycle()

    val currentUser = FirebaseAuth.getInstance().currentUser

    val userName = currentUser?.displayName ?: "Citizen"

    val pendingCount = reports.count { it.status == "Pending" }
    val resolvedCount = reports.count { it.status == "Resolved" }
    val progressCount = reports.count { it.status == "In Progress" }

    Scaffold(

        containerColor = MaterialTheme.colorScheme.background,

        floatingActionButton = {

            FloatingActionButton(
                onClick = onNavigateToReportIssue,
                containerColor = BrandPrimary,
                contentColor = Color.White,
                shape = CircleShape
            ) {

                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Report Issue",
                    modifier = Modifier.size(30.dp)
                )
            }
        },

        floatingActionButtonPosition = FabPosition.End

    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),

            contentPadding = PaddingValues(
                bottom = 120.dp
            )
        ) {

            item {

                DashboardHeader(
                    userName = userName
                )
            }

            item {

                Spacer(modifier = Modifier.height(28.dp))
            }

            item {

                StatsSection(
                    pending = pendingCount,
                    resolved = resolvedCount,
                    progress = progressCount
                )
            }

            item {

                Spacer(modifier = Modifier.height(32.dp))
            }

            item {

                SectionHeader(
                    title = "Quick Actions",
                    icon = Icons.Rounded.GridView
                )
            }

            item {

                Spacer(modifier = Modifier.height(16.dp))
            }

            item {

                QuickActionsRow(
                    onNavigateToMap = onNavigateToMap,
                    onNavigateToHistory = onNavigateToHistory,
                    onNavigateToSupport = onNavigateToSupport,
                    onNavigateToProfile = onNavigateToProfile
                )
            }

            item {

                Spacer(modifier = Modifier.height(36.dp))
            }

            item {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "Recent Reports",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    TextButton(
                        onClick = onNavigateToHistory
                    ) {

                        Text(
                            text = "See All",
                            color = BrandPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (reports.isEmpty()) {

                item {

                    EmptyReportsCard()
                }

            } else {

                items(reports.take(6)) { report ->

                    DashboardReportCard(
                        report = report,
                        onClick = {
                            onNavigateToDetails(
                                report.ticketId
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardHeader(
    userName: String
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.82f)
                .clip(
                    RoundedCornerShape(
                        bottomStart = 40.dp,
                        bottomEnd = 40.dp
                    )
                )
                .background(
                    Brush.verticalGradient(
                        colors = PrimaryGradient
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 22.dp,
                    vertical = 24.dp
                )
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column {

                    Text(
                        text = "FixMyRoad",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Welcome back, $userName 👋",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.82f)
                    )
                }

                Surface(
                    modifier = Modifier.size(54.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.18f)
                ) {

                    Box(
                        contentAlignment = Alignment.Center
                    ) {

                        Icon(
                            Icons.Rounded.Person,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.14f)
                )
            ) {

                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(CircleShape)
                            .background(
                                Color.White.copy(alpha = 0.16f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {

                        Icon(
                            Icons.Rounded.Groups,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {

                        Text(
                            text = "Community Impact",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Together we improve roads, public safety and infrastructure.",
                            color = Color.White.copy(alpha = 0.84f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatsSection(
    pending: Int,
    resolved: Int,
    progress: Int
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp),

        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        StatCard(
            title = "Pending",
            value = pending.toString(),
            icon = Icons.Rounded.PendingActions,
            gradient = listOf(
                WarningAmber,
                Color(0xFFFF9800)
            ),
            modifier = Modifier.weight(1f)
        )

        StatCard(
            title = "Resolved",
            value = resolved.toString(),
            icon = Icons.Rounded.Verified,
            gradient = SuccessGradient,
            modifier = Modifier.weight(1f)
        )

        StatCard(
            title = "Progress",
            value = progress.toString(),
            icon = Icons.Rounded.Engineering,
            gradient = listOf(
                BrandPrimary,
                Color(0xFF5E35B1)
            ),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    gradient: List<Color>,
    modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier.height(130.dp),
        shape = RoundedCornerShape(26.dp)
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(gradient)
                )
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.18f)
                ) {

                    Box(
                        modifier = Modifier.padding(10.dp)
                    ) {

                        Icon(
                            icon,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }

                Column {

                    Text(
                        text = value,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp
                    )

                    Text(
                        text = title,
                        color = Color.White.copy(alpha = 0.82f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
@Composable
fun QuickActionsRow(
    onNavigateToMap: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSupport: () -> Unit,
    onNavigateToProfile: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        DashboardActionButton(
            icon = Icons.Rounded.Map,
            label = "Map",
            onClick = onNavigateToMap
        )

        DashboardActionButton(
            icon = Icons.Rounded.History,
            label = "Reports",
            onClick = onNavigateToHistory
        )

        DashboardActionButton(
            icon = Icons.Rounded.SupportAgent,
            label = "Support",
            onClick = onNavigateToSupport
        )

        DashboardActionButton(
            icon = Icons.Rounded.Person,
            label = "Profile",
            onClick = onNavigateToProfile
        )
    }
}
@Composable
fun EmptyReportsCard() {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp),
        shape = RoundedCornerShape(24.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Icon(
                Icons.Rounded.Assignment,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Gray400
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No Reports Yet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tap the + button to report your first issue.",
                color = Gray600
            )
        }
    }
}
@Composable
fun DashboardReportCard(
    report: Report,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 8.dp)
            .clickable { onClick() },

        shape = RoundedCornerShape(24.dp)
    ) {

        Column(
            modifier = Modifier.padding(20.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = report.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = report.category,
                        color = Gray600
                    )
                }

                Icon(
                    Icons.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Gray500
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                shape = RoundedCornerShape(50),
                color = when (report.status) {
                    "Resolved" -> SuccessGreen.copy(alpha = 0.15f)
                    "In Progress" -> BrandPrimary.copy(alpha = 0.15f)
                    else -> WarningAmber.copy(alpha = 0.15f)
                }
            ) {

                Text(
                    text = report.status,
                    modifier = Modifier.padding(
                        horizontal = 14.dp,
                        vertical = 6.dp
                    ),
                    color = when (report.status) {
                        "Resolved" -> SuccessGreen
                        "In Progress" -> BrandPrimary
                        else -> WarningAmber
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
@Composable
fun SectionHeader(
    title: String,
    icon: ImageVector
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = BrandPrimary,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}
@Composable
fun DashboardActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {

        Surface(
            modifier = Modifier.size(74.dp),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {

            Box(
                contentAlignment = Alignment.Center
            ) {

                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = BrandPrimary,
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}