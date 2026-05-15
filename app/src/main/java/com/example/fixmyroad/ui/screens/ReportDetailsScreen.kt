package com.example.fixmyroad.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fixmyroad.domain.model.Report
import com.example.fixmyroad.ui.theme.*
import com.example.fixmyroad.ui.viewmodel.ReportDetailsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailsScreen(
    onBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onDeleteSuccess: () -> Unit,
    viewModel: ReportDetailsViewModel = hiltViewModel()
) {

    val report by viewModel.report.collectAsStateWithLifecycle()
    val isDeleting by viewModel.isDeleting.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val deleteSuccess by viewModel.deleteSuccess.collectAsStateWithLifecycle()

    var showDeleteDialog by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(deleteSuccess) {

        if (deleteSuccess) {

            onDeleteSuccess()

            viewModel.resetDeleteSuccess()
        }
    }

    if (showDeleteDialog) {

        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },

            title = {

                Text(
                    text = "Delete Report",
                    fontWeight = FontWeight.Bold
                )
            },

            text = {

                Text(
                    text = "Are you sure you want to permanently delete this report?"
                )
            },

            confirmButton = {

                Button(
                    onClick = {

                        showDeleteDialog = false

                        viewModel.deleteReport(
                            onSuccess = {},
                            onError = {}
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorRed
                    )
                ) {

                    Text("Delete")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {

                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(

        topBar = {

            CenterAlignedTopAppBar(

                title = {

                    Text(
                        text = "Report Details",
                        fontWeight = FontWeight.Bold
                    )
                },

                navigationIcon = {

                    IconButton(
                        onClick = onBack
                    ) {

                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = null
                        )
                    }
                },

                actions = {

                    report?.let { currentReport ->

                        IconButton(
                            onClick = {
                                onNavigateToEdit(
                                    currentReport.ticketId
                                )
                            }
                        ) {

                            Icon(
                                Icons.Rounded.Edit,
                                contentDescription = null
                            )
                        }

                        IconButton(
                            onClick = {
                                showDeleteDialog = true
                            }
                        ) {

                            Icon(
                                Icons.Rounded.Delete,
                                contentDescription = null,
                                tint = ErrorRed
                            )
                        }
                    }
                }
            )
        }

    ) { padding ->

        when {

            report == null && error == null -> {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {

                    CircularProgressIndicator()
                }
            }

            error != null -> {

                ErrorSection(
                    error = error ?: "Unknown error"
                )
            }

            report != null -> {

                ReportDetailsContent(
                    report = report!!,
                    padding = padding,
                    onDelete = {
                        showDeleteDialog = true
                    },
                    onEdit = {
                        onNavigateToEdit(
                            report!!.ticketId
                        )
                    },
                    isDeleting = isDeleting
                )
            }
        }
    }
}

@Composable
fun ReportDetailsContent(
    report: Report,
    padding: PaddingValues,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    isDeleting: Boolean
) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                shape = RoundedCornerShape(24.dp)
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Gray100),
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        Icons.Rounded.Image,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = Gray400
                    )
                }
            }
        }

        item {

            DetailCard(
                title = "Issue Title",
                value = report.title
            )
        }

        item {

            DescriptionCard(
                report = report
            )
        }

        item {

            DetailCard(
                title = "Category",
                value = report.category
            )
        }

        item {

            DetailCard(
                title = "Severity",
                value = report.severity
            )
        }

        item {

            DetailCard(
                title = "Status",
                value = report.status
            )
        }

        item {

            DetailCard(
                title = "Address",
                value = report.address ?: "Unavailable"
            )
        }

        item {

            DetailCard(
                title = "Latitude",
                value = report.latitude.toString()
            )
        }

        item {

            DetailCard(
                title = "Longitude",
                value = report.longitude.toString()
            )
        }

        item {

            DetailCard(
                title = "Submitted On",
                value = formatTimestamp(report.timestamp)
            )
        }

        item {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                Button(
                    onClick = onEdit,
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {

                    Icon(
                        Icons.Rounded.Edit,
                        contentDescription = null
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text("Edit")
                }

                Button(
                    onClick = onDelete,
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorRed
                    ),
                    enabled = !isDeleting
                ) {

                    if (isDeleting) {

                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )

                    } else {

                        Icon(
                            Icons.Rounded.Delete,
                            contentDescription = null
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text("Delete")
                }
            }
        }
    }
}

@Composable
fun DescriptionCard(
    report: Report
) {

    Card(
        shape = RoundedCornerShape(22.dp)
    ) {

        Column(
            modifier = Modifier.padding(20.dp)
        ) {

            Text(
                text = "Description",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = report.description,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 24.sp,
                color = Gray700
            )
        }
    }
}

@Composable
fun DetailCard(
    title: String,
    value: String
) {

    Card(
        shape = RoundedCornerShape(20.dp)
    ) {

        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            Text(
                text = title,
                color = Gray500,
                style = MaterialTheme.typography.labelLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ErrorSection(
    error: String
) {

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {

            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(
                        ErrorRed.copy(alpha = 0.12f)
                    ),
                contentAlignment = Alignment.Center
            ) {

                Icon(
                    Icons.Rounded.ErrorOutline,
                    contentDescription = null,
                    tint = ErrorRed,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Something went wrong",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = error,
                textAlign = TextAlign.Center,
                color = Gray600
            )
        }
    }
}

fun formatTimestamp(
    timestamp: Long
): String {

    return try {

        val sdf = SimpleDateFormat(
            "MMM dd, yyyy • hh:mm a",
            Locale.getDefault()
        )

        sdf.format(Date(timestamp))

    } catch (_: Exception) {

        "Unknown date"
    }
}