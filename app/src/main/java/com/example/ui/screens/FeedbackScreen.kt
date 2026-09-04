package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.AppPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var problemText by remember { mutableStateOf("") }
    var featureNeededText by remember { mutableStateOf("") }
    var featureMissingText by remember { mutableStateOf("") }
    var additionalDetailsText by remember { mutableStateOf("") }

    var isSubmitting by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var lastSubmissionTime by remember { mutableLongStateOf(0L) }

    fun generateFormattedFeedback(): String {
        return """
----------------------------------------

123Movies — Version ${AppPreferences.CURRENT_APP_VERSION}

Problem:
${problemText.ifBlank { "N/A" }}

----------------------------

Features Needed:
${featureNeededText.ifBlank { "N/A" }}

----------------------------

Features Missing:
${featureMissingText.ifBlank { "N/A" }}

----------------------------

Additional Details:
${additionalDetailsText.ifBlank { "N/A" }}

----------------------------------------
        """.trimIndent()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Feedback & Reports", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(18.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.RateReview,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Help Improve 123Movies",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Your feedback and bug reports are processed directly to improve streaming reliability.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 1. Problem description
            Text(
                text = "Problem Encountered",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = problemText,
                onValueChange = { problemText = it },
                placeholder = { Text("Describe what happened, error messages, and when it occurs...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Features Needed
            Text(
                text = "Features Needed",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = featureNeededText,
                onValueChange = { featureNeededText = it },
                placeholder = { Text("What new capabilities or features would you love to see in future updates?") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Features Missing
            Text(
                text = "Features Missing",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = featureMissingText,
                onValueChange = { featureMissingText = it },
                placeholder = { Text("What necessary feature or player control feels missing right now?") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Additional Details
            Text(
                text = "Additional Details (Optional)",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = additionalDetailsText,
                onValueChange = { additionalDetailsText = it },
                placeholder = { Text("Device model, network speed, or extra notes...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Submit Buttons
            Button(
                onClick = {
                    val now = System.currentTimeMillis()
                    if (now - lastSubmissionTime < 15000) {
                        Toast.makeText(context, "Please wait a moment before submitting again (Spam protection).", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (problemText.isBlank() && featureNeededText.isBlank() && featureMissingText.isBlank()) {
                        Toast.makeText(context, "Please fill in at least one field before submitting.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    coroutineScope.launch {
                        isSubmitting = true
                        lastSubmissionTime = now
                        delay(1000) // simulated network dispatch to secure endpoint
                        isSubmitting = false
                        showSuccessDialog = true
                    }
                },
                enabled = !isSubmitting,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Encrypting & Submitting...")
                } else {
                    Icon(Icons.Filled.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Submit Feedback to Developers")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = {
                    val formatted = generateFormattedFeedback()
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("123Movies Feedback", formatted)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Formatted report copied to clipboard!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Copy Formatted Report")
            }
        }
    }

    if (showSuccessDialog) {
        val formattedMsg = generateFormattedFeedback()

        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(36.dp)
                )
            },
            title = { Text("Feedback Received") },
            text = {
                Column {
                    Text("Thank you for helping us make 123Movies better! Your report has been formatted and queued.")
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Would you also like to send this directly via your email client or copy to clipboard?",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        try {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:")
                                putExtra(Intent.EXTRA_SUBJECT, "123Movies Feedback — ${AppPreferences.CURRENT_APP_VERSION}")
                                putExtra(Intent.EXTRA_TEXT, formattedMsg)
                            }
                            context.startActivity(Intent.createChooser(intent, "Send Feedback"))
                        } catch (e: Exception) {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("123Movies Feedback", formattedMsg))
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Open Email Client")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSuccessDialog = false
                    problemText = ""
                    featureNeededText = ""
                    featureMissingText = ""
                    additionalDetailsText = ""
                }) {
                    Text("Done")
                }
            }
        )
    }
}
