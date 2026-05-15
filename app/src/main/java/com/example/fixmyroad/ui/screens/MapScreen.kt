package com.example.fixmyroad.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.fixmyroad.domain.model.Report
import com.example.fixmyroad.ui.theme.*
import com.example.fixmyroad.ui.viewmodel.ReportViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    onNavigateToDetails: (String) -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {

    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {

        if (permissionState.allPermissionsGranted) {

            GoogleMapView(
                viewModel = viewModel,
                onNavigateToDetails = onNavigateToDetails
            )

        } else {

            PermissionGateway {
                permissionState.launchMultiplePermissionRequest()
            }
        }
    }
}

@Composable
fun PermissionGateway(
    onGrant: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(
                    BrandPrimary.copy(alpha = 0.1f)
                ),
            contentAlignment = Alignment.Center
        ) {

            Icon(
                imageVector = Icons.Rounded.LocationOn,
                contentDescription = null,
                tint = BrandPrimary,
                modifier = Modifier.size(60.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Location Permission Required",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Enable location access to discover nearby road issues and civic reports in real time.",
            style = MaterialTheme.typography.bodyLarge,
            color = Gray600
        )

        Spacer(modifier = Modifier.height(36.dp))

        Button(
            onClick = onGrant,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandPrimary
            )
        ) {

            Icon(
                Icons.Rounded.MyLocation,
                contentDescription = null
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "Grant Permission",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleMapView(
    viewModel: ReportViewModel,
    onNavigateToDetails: (String) -> Unit
) {

    val context = LocalContext.current

    val reports by viewModel.allReports.collectAsStateWithLifecycle()
    val selectedLocation by viewModel.selectedLocation.collectAsStateWithLifecycle()

    var searchQuery by remember {
        mutableStateOf("")
    }

    var selectedFilter by remember {
        mutableStateOf("All")
    }

    var selectedReport by remember {
        mutableStateOf<Report?>(null)
    }

    var openBottomSheet by remember {
        mutableStateOf(false)
    }

    val coroutineScope = rememberCoroutineScope()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            selectedLocation ?: LatLng(12.9716, 77.5946),
            12f
        )
    }

    val filteredReports = reports.filter {

        val matchesSearch =
            it.title.contains(searchQuery, true) ||
                    it.description.contains(searchQuery, true) ||
                    (it.address ?: "").contains(searchQuery, true)

        val matchesFilter =
            selectedFilter == "All" ||
                    it.status == selectedFilter ||
                    it.category == selectedFilter

        matchesSearch && matchesFilter
    }

    val mapProperties = remember {

        MapProperties(
            isMyLocationEnabled = true,
            mapStyleOptions = MapStyleOptions(GoogleMapsStylePremium)
        )
    }

    val mapUiSettings = remember {

        MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false,
            mapToolbarEnabled = false,
            compassEnabled = true
        )
    }

    val sheetState = rememberModalBottomSheetState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = mapUiSettings
        ) {

            filteredReports.forEach { report ->

                val position = LatLng(
                    report.latitude,
                    report.longitude
                )

                Marker(
                    state = rememberMarkerState(position = position),
                    title = report.title,
                    snippet = report.description,
                    icon = BitmapDescriptorFactory.defaultMarker(
                        when (report.status) {
                            "Resolved" -> BitmapDescriptorFactory.HUE_GREEN
                            "In Review" -> BitmapDescriptorFactory.HUE_ORANGE
                            else -> BitmapDescriptorFactory.HUE_AZURE
                        }
                    ),
                    onClick = {

                        selectedReport = report
                        openBottomSheet = true

                        coroutineScope.launch {

                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(
                                    position,
                                    16f
                                )
                            )
                        }

                        true
                    }
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp)
                .align(Alignment.TopCenter),
            shape = RoundedCornerShape(30.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(12.dp)
        ) {

            Column {

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    placeholder = {
                        Text("Search reports, locations...")
                    },
                    leadingIcon = {

                        Icon(
                            Icons.Rounded.Search,
                            contentDescription = null
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
                    shape = RoundedCornerShape(22.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions()
                )

                LazyRow(
                    contentPadding = PaddingValues(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    items(
                        listOf(
                            "All",
                            "Pending",
                            "Resolved",
                            "Pothole",
                            "Garbage",
                            "Drainage"
                        )
                    ) { filter ->

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

        FloatingActionButton(
            onClick = {

                selectedLocation?.let {

                    coroutineScope.launch {

                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(
                                it,
                                15f
                            )
                        )
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(22.dp)
                .padding(bottom = 90.dp),
            containerColor = Color.White,
            contentColor = BrandPrimary,
            elevation = FloatingActionButtonDefaults.elevation(10.dp)
        ) {

            Icon(
                Icons.Rounded.MyLocation,
                contentDescription = null
            )
        }

        AnimatedVisibility(
            visible = filteredReports.isNotEmpty(),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp)
        ) {

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = BrandPrimary
                )
            ) {

                Row(
                    modifier = Modifier.padding(
                        horizontal = 18.dp,
                        vertical = 12.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        Icons.Rounded.Map,
                        contentDescription = null,
                        tint = Color.White
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "${filteredReports.size} reports nearby",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (openBottomSheet && selectedReport != null) {

            ModalBottomSheet(
                onDismissRequest = {
                    openBottomSheet = false
                },
                sheetState = sheetState,
                containerColor = Color.White
            ) {

                ReportDetailsModern(
                    report = selectedReport!!,
                    onViewDetails = {

                        openBottomSheet = false

                        onNavigateToDetails(
                            selectedReport!!.ticketId
                        )
                    },
                    onNavigate = {

                        val uri = Uri.parse(
                            "google.navigation:q=${selectedReport!!.latitude},${selectedReport!!.longitude}"
                        )

                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            uri
                        )

                        intent.setPackage("com.google.android.apps.maps")

                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
fun ReportDetailsModern(
    report: Report,
    onViewDetails: () -> Unit,
    onNavigate: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = report.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = report.category,
                    style = MaterialTheme.typography.titleMedium,
                    color = BrandPrimary
                )
            }

            StatusBadge(
                status = report.status
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        if (!report.imageUri.isNullOrEmpty()) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                shape = RoundedCornerShape(26.dp)
            ) {

                AsyncImage(
                    model = report.imageUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        Text(
            text = "Description",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = report.description,
            style = MaterialTheme.typography.bodyLarge,
            color = Gray700
        )

        if (!report.address.isNullOrEmpty()) {

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    Icons.Rounded.LocationOn,
                    contentDescription = null,
                    tint = BrandPrimary
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = report.address ?: "",
                    color = Gray600
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Button(
                onClick = onViewDetails,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandPrimary
                )
            ) {

                Icon(
                    Icons.Rounded.Info,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "View Details",
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedButton(
                onClick = onNavigate,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(18.dp)
            ) {

                Icon(
                    Icons.Rounded.Directions,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Directions",
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun StatusBadge(
    status: String
) {

    val backgroundColor =
        when (status) {
            "Resolved" -> SuccessGreen.copy(alpha = 0.15f)
            "In Review" -> WarningAmber.copy(alpha = 0.15f)
            else -> BrandPrimary.copy(alpha = 0.15f)
        }

    val textColor =
        when (status) {
            "Resolved" -> SuccessGreen
            "In Review" -> WarningAmber
            else -> BrandPrimary
        }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .padding(
                horizontal = 14.dp,
                vertical = 8.dp
            ),
        contentAlignment = Alignment.Center
    ) {

        Text(
            text = status,
            color = textColor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}