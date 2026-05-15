package com.example.fixmyroad.ui.screens

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.fixmyroad.ui.theme.*
import com.example.fixmyroad.ui.viewmodel.ReportViewModel
import com.example.fixmyroad.utils.CameraUtils
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalPermissionsApi::class
)
@Composable
fun ReportIssueScreen(
    onBack: () -> Unit,
    onSubmitSuccess: () -> Unit,
    viewModel: ReportViewModel = hiltViewModel()
) {

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var title by remember {
        mutableStateOf("")
    }

    var description by remember {
        mutableStateOf("")
    }

    var selectedCategory by remember {
        mutableStateOf("Pothole")
    }

    var selectedSeverity by remember {
        mutableStateOf("Medium")
    }

    var imageUri by remember {
        mutableStateOf<Uri?>(null)
    }

    var searchQuery by remember {
        mutableStateOf("")
    }

    var showValidationError by remember {
        mutableStateOf(false)
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    val isSubmitting by viewModel.isSubmitting.collectAsStateWithLifecycle()

    val categories = listOf(
        "Pothole",
        "Street Light",
        "Drainage",
        "Debris",
        "Garbage",
        "Other"
    )

    val severities = listOf(
        "Low",
        "Medium",
        "High",
        "Critical"
    )

    val cameraPermissionState =
        rememberPermissionState(
            Manifest.permission.CAMERA
        )

    val locationPermissionState =
        rememberPermissionState(
            Manifest.permission.ACCESS_FINE_LOCATION
        )

    val cameraLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture()
        ) { success ->

            if (!success) {
                imageUri = null
            }
        }

    val galleryLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri ->

            imageUri = uri
        }

    Scaffold(
        topBar = {

            CenterAlignedTopAppBar(
                title = {

                    Text(
                        text = "Report Issue",
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
        ) {

            Spacer(modifier = Modifier.height(10.dp))

            // HERO HEADER
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                )
            ) {

                Box(
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(
                                PrimaryGradient
                            )
                        )
                        .padding(26.dp)
                ) {

                    Column {

                        Text(
                            text = "Make Your City Better",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Report civic problems with images, location, and details.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.82f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // EVIDENCE SECTION
            SectionHeader(
                title = "Evidence",
                icon = Icons.Rounded.CameraAlt
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Gray100
                )
            ) {

                if (imageUri != null) {

                    Box {

                        AsyncImage(
                            model = imageUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(14.dp)
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(
                                    Color.Black.copy(alpha = 0.45f)
                                )
                                .clickable {
                                    imageUri = null
                                },
                            contentAlignment = Alignment.Center
                        ) {

                            Icon(
                                Icons.Rounded.Close,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }

                } else {

                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        UploadOption(
                            icon = Icons.Rounded.Camera,
                            title = "Camera",
                            subtitle = "Capture Issue",
                            onClick = {

                                if (cameraPermissionState.status.isGranted) {

                                    try {

                                        val uri =
                                            CameraUtils.createImageUri(context)

                                        imageUri = uri

                                        cameraLauncher.launch(uri)

                                    } catch (_: Exception) {

                                        Toast.makeText(
                                            context,
                                            "Unable to open camera",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                } else {

                                    cameraPermissionState.launchPermissionRequest()
                                }
                            }
                        )

                        VerticalDivider(
                            modifier = Modifier.height(70.dp),
                            color = Gray300
                        )

                        UploadOption(
                            icon = Icons.Rounded.Collections,
                            title = "Gallery",
                            subtitle = "Upload Image",
                            onClick = {
                                galleryLauncher.launch("image/*")
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // LOCATION SECTION
            SectionHeader(
                title = "Issue Location",
                icon = Icons.Rounded.LocationOn
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("Search location...")
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
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {

                        viewModel.searchLocation(searchQuery)

                        focusManager.clearFocus()
                    }
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            LocationPickerCard(
                viewModel = viewModel,
                permissionGranted = locationPermissionState.status.isGranted,
                onRequestPermission = {
                    locationPermissionState.launchPermissionRequest()
                }
            )

            Spacer(modifier = Modifier.height(30.dp))

            // DETAILS SECTION
            SectionHeader(
                title = "Issue Details",
                icon = Icons.Rounded.Description
            )

            Spacer(modifier = Modifier.height(18.dp))

            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Issue Title")
                },
                placeholder = {
                    Text("Example: Large pothole near signal")
                },
                shape = RoundedCornerShape(18.dp),
                singleLine = true,
                isError = showValidationError && title.isBlank(),
                supportingText = {

                    if (showValidationError && title.isBlank()) {

                        Text(
                            text = "Title is required"
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Category",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                items(categories) { category ->

                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = {
                            selectedCategory = category
                        },
                        label = {
                            Text(category)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Severity",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                items(severities) { severity ->

                    FilterChip(
                        selected = selectedSeverity == severity,
                        onClick = {
                            selectedSeverity = severity
                        },
                        label = {
                            Text(severity)
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor =
                            when (severity) {
                                "Critical" -> ErrorRed.copy(alpha = 0.18f)
                                "High" -> WarningAmber.copy(alpha = 0.18f)
                                else -> BrandPrimary.copy(alpha = 0.12f)
                            }
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = description,
                onValueChange = {
                    description = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Description")
                },
                placeholder = {
                    Text("Describe the issue in detail...")
                },
                minLines = 5,
                shape = RoundedCornerShape(18.dp),
                isError = showValidationError && description.isBlank(),
                supportingText = {

                    if (showValidationError && description.isBlank()) {

                        Text(
                            text = "Description is required"
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(40.dp))

            // SUBMIT BUTTON
            Button(
                onClick = {

                    focusManager.clearFocus()

                    when {

                        title.isBlank() -> {

                            showValidationError = true

                            errorMessage = "Please enter issue title"
                        }

                        description.isBlank() -> {

                            showValidationError = true

                            errorMessage = "Please enter issue description"
                        }

                        imageUri == null -> {

                            errorMessage = "Please upload issue image"
                        }

                        else -> {

                            showValidationError = false

                            viewModel.submitReport(
                                title = title,
                                description = description,
                                category = selectedCategory,
                                severity = selectedSeverity,
                                imageUri = imageUri,
                                onSuccess = {

                                    Toast.makeText(
                                        context,
                                        "Report submitted successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    onSubmitSuccess()
                                },
                                onError = {

                                    errorMessage = it
                                }
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                enabled = !isSubmitting,
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandPrimary
                )
            ) {

                if (isSubmitting) {

                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )

                } else {

                    Icon(
                        Icons.Rounded.Send,
                        contentDescription = null
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "Submit Complaint",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

        // ERROR SNACKBAR
        AnimatedVisibility(
            visible = errorMessage.isNotEmpty(),
            modifier = Modifier
                .padding(16.dp)
        ) {

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = ErrorRed
                ),
                shape = RoundedCornerShape(18.dp)
            ) {

                Row(
                    modifier = Modifier.padding(
                        horizontal = 18.dp,
                        vertical = 14.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        Icons.Rounded.Error,
                        contentDescription = null,
                        tint = Color.White
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = errorMessage,
                        color = Color.White
                    )
                }
            }
        }
    }
}
@Composable
fun UploadOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(
            interactionSource = remember {
                MutableInteractionSource()
            },
            indication = null
        ) {
            onClick()
        }
    ) {

        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(
                    Color.White
                ),
            contentAlignment = Alignment.Center
        ) {

            Icon(
                icon,
                contentDescription = null,
                tint = BrandPrimary,
                modifier = Modifier.size(30.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = title,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = subtitle,
            color = Gray600,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun LocationPickerCard(
    viewModel: ReportViewModel,
    permissionGranted: Boolean,
    onRequestPermission: () -> Unit
) {

    val selectedLocation by viewModel.selectedLocation.collectAsStateWithLifecycle()
    val detailedAddress by viewModel.detailedAddress.collectAsStateWithLifecycle()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(330.dp),
        shape = RoundedCornerShape(28.dp)
    ) {

        if (permissionGranted) {

            val cameraPositionState =
                rememberCameraPositionState {

                    position = CameraPosition.fromLatLngZoom(
                        selectedLocation ?: LatLng(12.9716, 77.5946),
                        15f
                    )
                }

            LaunchedEffect(cameraPositionState.isMoving) {

                if (!cameraPositionState.isMoving) {

                    viewModel.updateSelectedLocation(
                        cameraPositionState.position.target
                    )
                }
            }

            Box {

                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        isMyLocationEnabled = true,
                        mapStyleOptions = MapStyleOptions(
                            GoogleMapsStylePremium
                        )
                    ),
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = false,
                        myLocationButtonEnabled = true
                    )
                )

                Icon(
                    Icons.Rounded.LocationOn,
                    contentDescription = null,
                    tint = BrandPrimary,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(42.dp)
                )

                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(18.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {

                        Text(
                            text = detailedAddress?.fullAddress
                                ?: "Fetching address...",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${detailedAddress?.area ?: ""}, ${detailedAddress?.city ?: ""}",
                            color = Gray600,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

        } else {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Icon(
                    Icons.Rounded.LocationOff,
                    contentDescription = null,
                    tint = Gray400,
                    modifier = Modifier.size(52.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Location Permission Required",
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Enable location permission to pin the issue accurately on the map.",
                    textAlign = TextAlign.Center,
                    color = Gray600
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onRequestPermission
                ) {

                    Text("Enable Location")
                }
            }
        }
    }
}