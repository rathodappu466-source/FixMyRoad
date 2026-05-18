@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.example.fixmyroad.ui.screens

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.fixmyroad.domain.model.Report
import com.example.fixmyroad.ui.theme.*
import com.example.fixmyroad.ui.viewmodel.ReportViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onLogout: () -> Unit,
    onNavigateToDetails: (String) -> Unit,
    viewModel: ReportViewModel = hiltViewModel()
) {

    val reports by viewModel.allReports.collectAsStateWithLifecycle()
    val updatingTicketId by viewModel.updatingTicketId.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var selectedFilter by remember { mutableStateOf("Open") }
    var followUpReport by remember { mutableStateOf<Report?>(null) }
    var responseText by remember { mutableStateOf("") }

    val pendingReports = reports.filter { it.status == "Pending" }
    val workingReports = reports.filter {
        it.status == "Working" || it.status == "In Progress"
    }
    val resolvedReports = reports.filter { it.status == "Resolved" }

    val filteredReports = remember(reports, selectedFilter) {
        when (selectedFilter) {
            "Pending" -> reports.filter { it.status == "Pending" }
            "Working" -> reports.filter {
                it.status == "Working" || it.status == "In Progress"
            }
            "Resolved" -> reports.filter { it.status == "Resolved" }
            else -> reports.filter { it.status != "Resolved" }
        }
    }

    fun showMessage(message: String) {
        coroutineScope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }

    fun updateStatus(report: Report, status: String, response: String? = null) {
        viewModel.updateReportStatus(
            report = report,
            status = status,
            response = response,
            onSuccess = {
                showMessage("${report.ticketId} marked $status")
            },
            onError = {
                showMessage(it)
            }
        )
    }

    if (followUpReport != null) {
        AlertDialog(
            onDismissRequest = {
                followUpReport = null
                responseText = ""
            },
            icon = {
                Icon(
                    Icons.Rounded.Forum,
                    contentDescription = null,
                    tint = BrandPrimary
                )
            },
            title = {
                Text("Add Follow-up")
            },
            text = {
                Column {
                    Text(
                        text = followUpReport?.ticketId.orEmpty(),
                        color = Gray600,
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = responseText,
                        onValueChange = {
                            responseText = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Response")
                        },
                        placeholder = {
                            Text("Example: Crew assigned for inspection today")
                        },
                        minLines = 4,
                        shape = RoundedCornerShape(18.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val report = followUpReport ?: return@Button
                        updateStatus(report, "Working", responseText)
                        followUpReport = null
                        responseText = ""
                    },
                    enabled = responseText.isNotBlank()
                ) {
                    Text("Send")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        followUpReport = null
                        responseText = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            item {
                AdminHeader(
                    pending = pendingReports.size,
                    working = workingReports.size,
                    resolved = resolvedReports.size,
                    onLogout = onLogout
                )
            }

            item {
                AdminQuickTools()
            }

            item {
                Column(
                    modifier = Modifier.padding(horizontal = 22.dp)
                ) {
                    Text(
                        text = "Issue Queue",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Open", "Pending", "Working", "Resolved").forEach { filter ->
                            FilterChip(
                                selected = selectedFilter == filter,
                                onClick = {
                                    selectedFilter = filter
                                },
                                label = {
                                    Text(filter)
                                }
                            )
                        }
                    }
                }
            }

            if (filteredReports.isEmpty()) {
                item {
                    EmptyAdminQueue()
                }
            } else {
                items(
                    items = filteredReports,
                    key = { it.ticketId }
                ) { report ->
                    AdminReportCard(
                        report = report,
                        isUpdating = updatingTicketId == report.ticketId,
                        onOpen = {
                            onNavigateToDetails(report.ticketId)
                        },
                        onFollowUp = {
                            followUpReport = report
                            responseText = report.adminResponse.orEmpty()
                        },
                        onMarkWorking = {
                            updateStatus(report, "Working")
                        },
                        onResolve = {
                            updateStatus(report, "Resolved")
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminHeader(
    pending: Int,
    working: Int,
    resolved: Int,
    onLogout: () -> Unit
) {

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 300.dp)
            .background(
                Brush.verticalGradient(PrimaryGradient)
            )
            .padding(22.dp)
    ) {
        val isCompact = maxWidth < 380.dp

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(22.dp)
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
                        text = "Administrator",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Manage civic reports and response status",
                        color = Color.White.copy(alpha = 0.84f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                OutlinedButton(
                    onClick = onLogout,
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.72f),
                                Color.White.copy(alpha = 0.34f)
                            )
                        )
                    )
                ) {
                    Icon(
                        Icons.Rounded.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    if (!isCompact) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Logout",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (isCompact) {
                AdminMetricCard(
                    label = "Pending",
                    value = pending.toString(),
                    icon = Icons.Rounded.PendingActions,
                    modifier = Modifier.fillMaxWidth()
                )

                AdminMetricCard(
                    label = "Working",
                    value = working.toString(),
                    icon = Icons.Rounded.Engineering,
                    modifier = Modifier.fillMaxWidth()
                )

                AdminMetricCard(
                    label = "Resolved",
                    value = resolved.toString(),
                    icon = Icons.Rounded.Verified,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AdminMetricCard(
                        label = "Pending",
                        value = pending.toString(),
                        icon = Icons.Rounded.PendingActions,
                        modifier = Modifier.weight(1f)
                    )

                    AdminMetricCard(
                        label = "Working",
                        value = working.toString(),
                        icon = Icons.Rounded.Engineering,
                        modifier = Modifier.weight(1f)
                    )

                    AdminMetricCard(
                        label = "Resolved",
                        value = resolved.toString(),
                        icon = Icons.Rounded.Verified,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminMetricCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {

    Surface(
        modifier = modifier.height(96.dp),
        shape = RoundedCornerShape(22.dp),
        color = Color.White.copy(alpha = 0.16f)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color.White
            )

            Column {
                Text(
                    text = value,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = label,
                    color = Color.White.copy(alpha = 0.82f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun AdminQuickTools() {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
    ) {
        Text(
            text = "Admin Tools",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AdminToolCard(
                icon = Icons.Rounded.Forum,
                title = "Follow-ups",
                subtitle = "Respond and mark working",
                modifier = Modifier
                    .weight(1f)
                    .widthIn(min = 150.dp)
            )

            AdminToolCard(
                icon = Icons.Rounded.TaskAlt,
                title = "Resolution",
                subtitle = "Close completed issues",
                modifier = Modifier
                    .weight(1f)
                    .widthIn(min = 150.dp)
            )
        }
    }
}

@Composable
private fun AdminToolCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {

    Surface(
        modifier = modifier.height(118.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = BrandPrimary
            )

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )

                Text(
                    text = subtitle,
                    color = Gray600,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun AdminReportCard(
    report: Report,
    isUpdating: Boolean,
    onOpen: () -> Unit,
    onFollowUp: () -> Unit,
    onMarkWorking: () -> Unit,
    onResolve: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .clickable { onOpen() },
        shape = RoundedCornerShape(26.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            BoxWithConstraints {
                val isCompact = maxWidth < 360.dp
                if (isCompact) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AdminReportImage(report)
                        AdminReportSummary(report)
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.Top
                    ) {
                        AdminReportImage(report)

                        Spacer(modifier = Modifier.width(14.dp))

                        AdminReportSummary(
                            report = report,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (!report.adminResponse.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(0.82f),
                        shape = RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = 18.dp,
                            bottomEnd = 4.dp
                        ),
                        color = BrandPrimary
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "Admin follow-up",
                                color = Color.White.copy(alpha = 0.78f),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = report.adminResponse,
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onFollowUp,
                    modifier = Modifier
                        .weight(1f)
                        .widthIn(min = 120.dp),
                    enabled = !isUpdating
                ) {
                    Icon(
                        Icons.Rounded.Forum,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Follow up")
                }

                Button(
                    onClick = onMarkWorking,
                    modifier = Modifier
                        .weight(1f)
                        .widthIn(min = 110.dp),
                    enabled = !isUpdating && report.status != "Working",
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WarningAmber
                    )
                ) {
                    Text("Working")
                }

                Button(
                    onClick = onResolve,
                    modifier = Modifier
                        .weight(1f)
                        .widthIn(min = 110.dp),
                    enabled = !isUpdating && report.status != "Resolved",
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SuccessGreen
                    )
                ) {
                    if (isUpdating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Resolve")
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminReportImage(report: Report) {
    if (!report.imageUri.isNullOrEmpty()) {
        AsyncImage(
            model = report.imageUri,
            contentDescription = null,
            modifier = Modifier
                .size(88.dp)
                .clip(RoundedCornerShape(20.dp)),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Gray100),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.ImageNotSupported,
                contentDescription = null,
                tint = Gray400
            )
        }
    }
}

@Composable
private fun AdminReportSummary(
    report: Report,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AdminStatusBadge(report.status)

            Text(
                text = formatDate(report.timestamp),
                color = Gray500,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = report.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "${report.category} - ${report.severity}",
            color = Gray600,
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = report.address ?: "Address unavailable",
            color = Gray600,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun AdminStatusBadge(status: String) {

    val color = when (status) {
        "Pending" -> ErrorRed
        "Working", "In Progress" -> WarningAmber
        "Resolved" -> SuccessGreen
        else -> BrandPrimary
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.14f)
    ) {
        Text(
            text = if (status == "In Progress") "Working" else status,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = color,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun EmptyAdminQueue() {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(42.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(Gray100),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.TaskAlt,
                contentDescription = null,
                tint = Gray400,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "No Issues Here",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Reports matching this queue will appear here.",
            color = Gray600,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
