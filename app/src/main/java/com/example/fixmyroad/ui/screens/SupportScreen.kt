package com.example.fixmyroad.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fixmyroad.ui.components.PremiumCard
import com.example.fixmyroad.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Support Center", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Emergency Section
            item {
                Text(
                    text = "Emergency Helplines",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                EmergencyCard(
                    title = "City Helpline",
                    number = "100",
                    description = "24/7 dedicated city corporation support",
                    icon = Icons.Rounded.PhoneInTalk
                )
            }
            
            item {
                EmergencyCard(
                    title = "Road Safety",
                    number = "101",
                    description = "Accident and traffic hazard reporting",
                    icon = Icons.Rounded.Traffic
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // FAQ Section
            item {
                Text(
                    text = "Common Questions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            items(faqs) { faq ->
                FAQItem(faq.question, faq.answer)
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // Feedback Section
            item {
                PremiumCard(containerColor = BrandPrimary) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.Feedback, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Send App Feedback",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Help us improve FixMyRoad for everyone.",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}

@Composable
fun EmergencyCard(
    title: String,
    number: String,
    description: String,
    icon: ImageVector
) {
    PremiumCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(BrandPrimary.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = BrandPrimary)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = Gray600)
            }
            Text(
                text = number,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = BrandPrimary
            )
        }
    }
}

@Composable
fun FAQItem(question: String, answer: String) {
    PremiumCard(containerColor = Gray100) {
        Column {
            Text(text = question, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = answer, style = MaterialTheme.typography.bodyMedium, color = Gray700)
        }
    }
}

data class FAQ(val question: String, val answer: String)

val faqs = listOf(
    FAQ("How long does a report take?", "Most issues are inspected within 48-72 hours by the city officials."),
    FAQ("Can I track my report?", "Yes, you can check the status of all your reports in the Activity tab."),
    FAQ("How to upload a photo?", "Click on 'Report Issue' and tap on the camera icon to take or upload a photo.")
)
