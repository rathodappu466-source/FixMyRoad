package com.example.fixmyroad.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Help
import androidx.compose.material.icons.automirrored.rounded.Logout
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.fixmyroad.ui.components.PremiumCard
import com.example.fixmyroad.ui.theme.*
import com.example.fixmyroad.ui.viewmodel.ReportViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {

    val reports by viewModel.allReports.collectAsStateWithLifecycle()

    val currentUser = FirebaseAuth.getInstance().currentUser

    val userName = currentUser?.displayName ?: "FixMyRoad User"
    val userEmail = currentUser?.email ?: "No email available"
    val profilePhoto = currentUser?.photoUrl?.toString()

    val userReports = reports.filter {
        it.userId == currentUser?.uid
    }

    val resolvedCount = userReports.count {
        it.status == "Resolved"
    }

    val pendingCount = userReports.count {
        it.status == "Pending"
    }

    var showLogoutDialog by remember {
        mutableStateOf(false)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Profile",
                        fontWeight = FontWeight.Bold
                    )
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
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(20.dp))

            // PROFILE HEADER
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                )
            ) {

                Box(
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(PrimaryGradient)
                        )
                        .padding(28.dp)
                ) {

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        // PROFILE IMAGE
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(
                                    Color.White.copy(alpha = 0.2f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {

                            if (profilePhoto != null) {

                                AsyncImage(
                                    model = profilePhoto,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )

                            } else {

                                Icon(
                                    Icons.Rounded.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = userName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = userEmail,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f)
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(50)
                        ) {

                            Row(
                                modifier = Modifier.padding(
                                    horizontal = 16.dp,
                                    vertical = 8.dp
                                ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Icon(
                                    Icons.Rounded.Verified,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "Verified Citizen",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // STATS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                ProfileStatCard(
                    label = "Reports",
                    value = userReports.size.toString(),
                    modifier = Modifier.weight(1f)
                )

                ProfileStatCard(
                    label = "Resolved",
                    value = resolvedCount.toString(),
                    modifier = Modifier.weight(1f)
                )

                ProfileStatCard(
                    label = "Pending",
                    value = pendingCount.toString(),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ACHIEVEMENT CARD
            PremiumCard(
                containerColor = BrandPrimary.copy(alpha = 0.06f)
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                WarningAmber.copy(alpha = 0.15f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {

                        Icon(
                            Icons.Rounded.EmojiEvents,
                            contentDescription = null,
                            tint = WarningAmber,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(18.dp))

                    Column {

                        Text(
                            text = "Community Contributor",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "You've successfully contributed to improving your city.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray600
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // MENU ITEMS
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                ProfileMenuItem(
                    icon = Icons.Rounded.Edit,
                    label = "Edit Profile"
                )

                ProfileMenuItem(
                    icon = Icons.Rounded.Settings,
                    label = "Settings"
                )

                ProfileMenuItem(
                    icon = Icons.AutoMirrored.Rounded.Help,
                    label = "Help & Support"
                )

                ProfileMenuItem(
                    icon = Icons.Rounded.History,
                    label = "Report History"
                )

                ProfileMenuItem(
                    icon = Icons.AutoMirrored.Rounded.Logout,
                    label = "Logout",
                    color = ErrorRed,
                    onClick = {
                        showLogoutDialog = true
                    }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // LOGOUT DIALOG
    if (showLogoutDialog) {

        AlertDialog(
            onDismissRequest = {
                showLogoutDialog = false
            },
            title = {
                Text("Logout")
            },
            text = {
                Text("Are you sure you want to logout from FixMyRoad?")
            },
            confirmButton = {

                Button(
                    onClick = {

                        FirebaseAuth.getInstance().signOut()

                        showLogoutDialog = false

                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorRed
                    )
                ) {

                    Text("Logout")
                }
            },
            dismissButton = {

                TextButton(
                    onClick = {
                        showLogoutDialog = false
                    }
                ) {

                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ProfileStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {

    PremiumCard(
        modifier = modifier
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = BrandPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Gray500
            )
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    label: String,
    color: Color = Gray800,
    onClick: () -> Unit = {}
) {

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = Gray100,
        tonalElevation = 2.dp
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(
                        Color.White,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {

                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = color
            )

            Spacer(modifier = Modifier.weight(1f))

            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = Gray400
            )
        }
    }
}