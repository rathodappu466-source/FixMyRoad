package com.example.fixmyroad.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.fixmyroad.domain.model.Report
import com.example.fixmyroad.ui.components.PremiumCard
import com.example.fixmyroad.ui.theme.BrandPrimary
import com.example.fixmyroad.ui.viewmodel.EditReportViewModel
import com.example.fixmyroad.utils.CameraUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditReportScreen(
    onBack: () -> Unit,
    onUpdateSuccess: () -> Unit,
    viewModel: EditReportViewModel = hiltViewModel()
) {

    val report by viewModel.report.collectAsStateWithLifecycle()
    val title by viewModel.title.collectAsStateWithLifecycle()
    val description by viewModel.description.collectAsStateWithLifecycle()
    val category by viewModel.category.collectAsStateWithLifecycle()
    val severity by viewModel.severity.collectAsStateWithLifecycle()
    val selectedImageUri by viewModel.selectedImageUri.collectAsStateWithLifecycle()
    val isUpdating by viewModel.isUpdating.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val updateSuccess by viewModel.updateSuccess.collectAsStateWithLifecycle()

    var showErrorDialog by remember {
        mutableStateOf(false)
    }

    val context = LocalContext.current

    var cameraImageUri by remember {
        mutableStateOf<Uri?>(null)
    }

    // CAMERA
    val cameraLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture()
        ) { success ->

            if (success && cameraImageUri != null) {
                viewModel.updateImageUri(cameraImageUri!!)
            }
        }

    // GALLERY
    val galleryLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri ->

            uri?.let {
                viewModel.updateImageUri(it)
            }
        }

    // SUCCESS
    LaunchedEffect(updateSuccess) {

        if (updateSuccess) {
            onUpdateSuccess()
            viewModel.resetUpdateSuccess()
        }
    }

    // ERROR
    LaunchedEffect(error) {

        if (error != null) {
            showErrorDialog = true
        }
    }

    if (showErrorDialog && error != null) {

        AlertDialog(
            onDismissRequest = {
                showErrorDialog = false
                viewModel.clearError()
            },
            title = {
                Text("Error")
            },
            text = {
                Text(error ?: "Unknown error")
            },
            confirmButton = {

                Button(
                    onClick = {
                        showErrorDialog = false
                        viewModel.clearError()
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {

            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Edit Report",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {

                    IconButton(
                        onClick = onBack
                    ) {

                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->

        if (report == null) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {

                CircularProgressIndicator()
            }

        } else {

            EditReportContent(
                report = report!!,
                title = title,
                description = description,
                category = category,
                severity = severity,
                selectedImageUri = selectedImageUri,
                isUpdating = isUpdating,
                padding = padding,

                onTitleChange = {
                    viewModel.updateTitle(it)
                },

                onDescriptionChange = {
                    viewModel.updateDescription(it)
                },

                onCategoryChange = {
                    viewModel.updateCategory(it)
                },

                onSeverityChange = {
                    viewModel.updateSeverity(it)
                },

                onCameraClick = {

                    val uri =
                        CameraUtils.createImageUri(context)

                    if (uri != null) {

                        cameraImageUri = uri
                        cameraLauncher.launch(uri)
                    }
                },

                onGalleryClick = {
                    galleryLauncher.launch("image/*")
                },

                onSubmit = {

                    viewModel.submitUpdate(
                        onSuccess = {
                            onUpdateSuccess()
                        },
                        onError = {
                            showErrorDialog = true
                        }
                    )
                }
            )
        }
    }
}

@Composable
private fun EditReportContent(
    report: Report,
    title: String,
    description: String,
    category: String,
    severity: String,
    selectedImageUri: Uri?,
    isUpdating: Boolean,
    padding: PaddingValues,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onSeverityChange: (String) -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onSubmit: () -> Unit
) {

    val displayImageUri =
        selectedImageUri?.toString()
            ?: report.imageUri

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item {

            ImageEditSection(
                imageUri = displayImageUri,
                onCameraClick = onCameraClick,
                onGalleryClick = onGalleryClick
            )
        }

        item {

            FormFieldCard(
                label = "Issue Title"
            ) {

                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("Enter issue title")
                    },
                    singleLine = true
                )
            }
        }

        item {

            FormFieldCard(
                label = "Description"
            ) {

                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = {
                        Text("Describe issue")
                    }
                )
            }
        }

        item {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                FormFieldCard(
                    label = "Category",
                    modifier = Modifier.weight(1f)
                ) {

                    OutlinedTextField(
                        value = category,
                        onValueChange = onCategoryChange,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                FormFieldCard(
                    label = "Severity",
                    modifier = Modifier.weight(1f)
                ) {

                    OutlinedTextField(
                        value = severity,
                        onValueChange = onSeverityChange,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }

        item {

            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                enabled = !isUpdating,
                shape = RoundedCornerShape(14.dp)
            ) {

                if (isUpdating) {

                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                }

                Text(
                    if (isUpdating)
                        "Updating..."
                    else
                        "Update Report"
                )
            }
        }
    }
}

@Composable
private fun ImageEditSection(
    imageUri: String?,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {

    PremiumCard {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.LightGray.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {

                if (!imageUri.isNullOrEmpty()) {

                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Report Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                } else {

                    Icon(
                        Icons.Rounded.Image,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Button(
                    onClick = onCameraClick,
                    modifier = Modifier.weight(1f)
                ) {

                    Icon(
                        Icons.Rounded.PhotoCamera,
                        contentDescription = null
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text("Camera")
                }

                Button(
                    onClick = onGalleryClick,
                    modifier = Modifier.weight(1f)
                ) {

                    Icon(
                        Icons.Rounded.PhotoLibrary,
                        contentDescription = null
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text("Gallery")
                }
            }
        }
    }
}

@Composable
private fun FormFieldCard(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {

    Column(
        modifier = modifier.fillMaxWidth()
    ) {

        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        content()
    }
}