package com.example.fixmyroad.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.fixmyroad.domain.model.Report
import com.example.fixmyroad.ui.components.PremiumCard
import com.example.fixmyroad.ui.theme.*
import com.example.fixmyroad.ui.viewmodel.ReportViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    onNavigateToDetails: (String) -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {

    val reports by viewModel.allReports.collectAsStateWithLifecycle()

    var searchQuery by remember {
        mutableStateOf("")
    }

    var selectedStatus by remember {
        mutableStateOf("All")
    }

    var selectedCategory by remember {
        mutableStateOf("All")
    }

    val filteredReports = remember(
        reports,
        searchQuery,
        selectedStatus,
        selectedCategory
    ) {

        reports.filter { report ->

            val matchesSearch =
                report.title.contains(searchQuery, true) ||
                        report.description.contains(searchQuery, true) ||
                        (report.address ?: "").contains(searchQuery, true)

            val matchesStatus =
                selectedStatus == "All" ||
                        report.status == selectedStatus

            val matchesCategory =
                selectedCategory == "All" ||
                        report.category == selectedCategory

            matchesSearch && matchesStatus && matchesCategory
        }
    }

    val listState = rememberLazyListState()

    Scaffold(
        topBar = {

            CenterAlignedTopAppBar(
                title = {

                    Text(
                        text = "Activity History",
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
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // SEARCH BAR
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp),
                placeholder = {

                    Text(
                        "Search reports, locations..."
                    )
                },
                leadingIcon = {

                    Icon(
                        Icons.Rounded.Search,
                        contentDescription = null,
                        tint = BrandPrimary
                    )
                },
                trailingIcon = {

                    if (searchQuery.isNotEmpty()) {

                        IconButton(
                            onClick = {
                                searchQuery = ""
                            }
                        ) {

                            Icon(
                                Icons.Rounded.Close,
                                contentDescription = null
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(20.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandPrimary,
                    focusedLabelColor = BrandPrimary,
                    cursorColor = BrandPrimary
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            // FILTERS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                FilterChip(
                    selected = selectedStatus == "All",
                    onClick = {
                        selectedStatus = "All"
                        selectedCategory = "All"
                    },
                    label = {
                        Text("All")
                    }
                )

                FilterChip(
                    selected = selectedStatus == "Pending",
                    onClick = {
                        selectedStatus = "Pending"
                    },
                    label = {
                        Text("Pending")
                    }
                )

                FilterChip(
                    selected = selectedStatus == "Resolved",
                    onClick = {
                        selectedStatus = "Resolved"
                    },
                    label = {
                        Text("Resolved")
                    }
                )

                FilterChip(
                    selected = selectedCategory == "Pothole",
                    onClick = {
                        selectedCategory = "Pothole"
                    },
                    label = {
                        Text("Pothole")
                    }
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // HEADER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column {

                    Text(
                        text = "Recent Contributions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${filteredReports.size} reports found",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = BrandPrimary.copy(alpha = 0.12f)
                ) {

                    Box(
                        modifier = Modifier.padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {

                        Icon(
                            Icons.Rounded.History,
                            contentDescription = null,
                            tint = BrandPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // CONTENT
            if (filteredReports.isEmpty()) {

                EmptyHistoryState()

            } else {

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    contentPadding = PaddingValues(
                        horizontal = 22.dp,
                        vertical = 8.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    items(filteredReports) { report ->

                        ModernHistoryCard(
                            report = report,
                            onClick = {
                                onNavigateToDetails(
                                    report.ticketId
                                )
                            }
                        )
                    }

                    item {

                        Spacer(
                            modifier = Modifier.height(30.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModernHistoryCard(
    report: Report,
    onClick: () -> Unit
) {

    PremiumCard(
        modifier = Modifier.clickable(
            interactionSource = remember {
                MutableInteractionSource()
            },
            indication = null
        ) {
            onClick()
        }
    ) {

        Column {

            Row(
                verticalAlignment = Alignment.Top
            ) {

                // IMAGE
                if (!report.imageUri.isNullOrEmpty()) {

                    AsyncImage(
                        model = report.imageUri,
                        contentDescription = null,
                        modifier = Modifier
                            .size(92.dp)
                            .clip(RoundedCornerShape(22.dp)),
                        contentScale = ContentScale.Crop
                    )

                } else {

                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(
                                Gray100
                            ),
                        contentAlignment = Alignment.Center
                    ) {

                        Icon(
                            Icons.Rounded.ImageNotSupported,
                            contentDescription = null,
                            tint = Gray400,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // CONTENT
                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            CategoryChip(
                                category = report.category
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            SeverityChip(
                                severity = report.severity
                            )
                        }

                        Text(
                            text = formatDate(
                                report.timestamp
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = Gray500
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

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = report.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        StatusChipModern(
                            status = report.status
                        )

                        Icon(
                            Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            tint = Gray400
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = !report.address.isNullOrEmpty()
            ) {

                Column {

                    Spacer(modifier = Modifier.height(18.dp))

                    HorizontalDivider(
                        color = Gray200.copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Icon(
                            Icons.Rounded.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Gray500
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text =
                            "${report.area ?: ""}, ${report.city ?: ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray600,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryChip(
    category: String
) {

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                BrandPrimary.copy(alpha = 0.12f)
            )
            .padding(
                horizontal = 10.dp,
                vertical = 5.dp
            )
    ) {

        Text(
            text = category,
            style = MaterialTheme.typography.labelSmall,
            color = BrandPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SeverityChip(
    severity: String
) {

    val color =
        when (severity) {
            "Critical" -> ErrorRed
            "High" -> WarningAmber
            "Medium" -> BrandPrimary
            else -> SuccessGreen
        }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                color.copy(alpha = 0.12f)
            )
            .padding(
                horizontal = 10.dp,
                vertical = 5.dp
            )
    ) {

        Text(
            text = severity,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun StatusChipModern(
    status: String
) {

    val color =
        when (status) {
            "Resolved" -> SuccessGreen
            "Working", "In Progress" -> WarningAmber
            "Pending" -> ErrorRed
            else -> BrandPrimary
        }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = if (status == "In Progress") "Working" else status,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun EmptyHistoryState() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(
                    Gray100
                ),
            contentAlignment = Alignment.Center
        ) {

            Icon(
                Icons.Rounded.History,
                contentDescription = null,
                modifier = Modifier.size(54.dp),
                tint = Gray300
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "No Reports Found",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Your submitted reports and civic activities will appear here.",
            style = MaterialTheme.typography.bodyMedium,
            color = Gray600
        )
    }
}

fun formatDate(
    timestamp: Long
): String {

    return try {

        SimpleDateFormat(
            "MMM dd",
            Locale.getDefault()
        ).format(Date(timestamp))

    } catch (_: Exception) {

        "Unknown"
    }
}
