package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

enum class PaymentMethodTab {
    REDOT_PAY,
    NSAVE
}

const val REDOT_PAY_ID = "1965421414"
const val REDOT_PAY_URL = "https://url.redotpay.com/pay?id=1965421414"

const val NSAVE_NAME = "Md Arafath Rahman"
const val NSAVE_HANDLE = "@arafath_rahman9"
const val NSAVE_URL = "https://nsave.com/pay/@arafath_rahman9"

@Composable
fun BuyMeACoffeeSection(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedMethod by remember { mutableStateOf(PaymentMethodTab.REDOT_PAY) }
    var fullScreenQrData by remember { mutableStateOf<Pair<String, CenterLogoType>?>(null) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(
                listOf(
                    Color(0xFFE50914).copy(alpha = 0.5f),
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                )
            )
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFFFFB300), Color(0xFFE50914))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocalCafe,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Buy Me a Coffee / Support",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Support 123Movies development & servers",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "If you enjoy streaming on 123Movies without ads, help us maintain high-speed servers and ongoing development with a small tip via RedotPay or Nsave.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Start,
                lineHeight = 18.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Method Selector Tabs (RedotPay vs Nsave)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Tab 1: RedotPay
                Surface(
                    onClick = { selectedMethod = PaymentMethodTab.REDOT_PAY },
                    shape = RoundedCornerShape(10.dp),
                    color = if (selectedMethod == PaymentMethodTab.REDOT_PAY)
                        Color(0xFFE50914)
                    else
                        Color.Transparent,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (selectedMethod == PaymentMethodTab.REDOT_PAY) Color.White else Color(0xFFE50914),
                            modifier = Modifier.size(16.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "P",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (selectedMethod == PaymentMethodTab.REDOT_PAY) Color(0xFFE50914) else Color.White
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "RedotPay",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (selectedMethod == PaymentMethodTab.REDOT_PAY) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Tab 2: Nsave
                Surface(
                    onClick = { selectedMethod = PaymentMethodTab.NSAVE },
                    shape = RoundedCornerShape(10.dp),
                    color = if (selectedMethod == PaymentMethodTab.NSAVE)
                        Color(0xFFE50914)
                    else
                        Color.Transparent,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccountBalanceWallet,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (selectedMethod == PaymentMethodTab.NSAVE) Color.White else Color(0xFFE50914)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Nsave",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (selectedMethod == PaymentMethodTab.NSAVE) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Animated Card Content
            AnimatedContent(
                targetState = selectedMethod,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "PaymentCardSwitch"
            ) { method ->
                when (method) {
                    PaymentMethodTab.REDOT_PAY -> {
                        RedotPayCardView(
                            onEnlargeQr = {
                                fullScreenQrData = Pair(REDOT_PAY_URL, CenterLogoType.REDOT_PAY)
                            }
                        )
                    }
                    PaymentMethodTab.NSAVE -> {
                        NsaveCardView(
                            onEnlargeQr = {
                                fullScreenQrData = Pair(NSAVE_URL, CenterLogoType.NSAVE)
                            }
                        )
                    }
                }
            }
        }
    }

    // Full Screen QR Dialog on Tap
    if (fullScreenQrData != null) {
        Dialog(onDismissRequest = { fullScreenQrData = null }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (fullScreenQrData?.second == CenterLogoType.REDOT_PAY) "RedotPay QR Code" else "Nsave QR Code",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Point any camera or scanner app to pay",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(18.dp))

                    QrCodeView(
                        data = fullScreenQrData!!.first,
                        centerLogo = fullScreenQrData!!.second,
                        modifier = Modifier.size(260.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = { fullScreenQrData = null },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

/**
 * Recreates the RedotPay payment card from Redotpay.jpg:
 * - RedotPay Logo + "RedotPay"
 * - "Scan with the RedotPay app to pay"
 * - High-Resolution scannable QR with center red 'P'
 * - "RedotPay ID: 1965421414"
 * - Action buttons
 */
@Composable
private fun RedotPayCardView(
    onEnlargeQr: () -> Unit
) {
    val context = LocalContext.current

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF14151B), // Dark sleek theme matching Redotpay.jpg
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF2A2B36), RoundedCornerShape(18.dp))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: RedotPay Logo + Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFE50914),
                    modifier = Modifier.size(26.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "P",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "RedotPay",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Scan with the RedotPay app to pay",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFE0E0E0),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Central QR Code with RedotPay Center Logo (Scannable!)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onEnlargeQr() }
            ) {
                QrCodeView(
                    data = REDOT_PAY_URL,
                    centerLogo = CenterLogoType.REDOT_PAY,
                    modifier = Modifier.size(210.dp),
                    contentPadding = 10.dp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // RedotPay ID display
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF1E2029),
                modifier = Modifier.clickable {
                    copyToClipboard(context, REDOT_PAY_ID, "RedotPay ID copied!")
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RedotPay ID: $REDOT_PAY_ID",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Filled.ContentCopy,
                        contentDescription = "Copy ID",
                        tint = Color(0xFFE50914),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        copyToClipboard(context, REDOT_PAY_ID, "RedotPay ID ($REDOT_PAY_ID) copied!")
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE50914)
                    )
                ) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy ID", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(REDOT_PAY_URL))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            copyToClipboard(context, REDOT_PAY_ID, "RedotPay ID copied: $REDOT_PAY_ID")
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open Pay")
                }
            }
        }
    }
}

/**
 * Recreates the Nsave payment card from Nsave.jpg:
 * - Scannable Nsave QR code
 * - "Md Arafath Rahman"
 * - "@arafath_rahman9" (in red font)
 * - "Share your QR code with other nsavers. Receive payments instantly at zero fees from friends & family."
 * - Red pill action button: [ Share Link ]
 */
@Composable
private fun NsaveCardView(
    onEnlargeQr: () -> Unit
) {
    val context = LocalContext.current

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFFFFFFFF), // Crisp white background matching Nsave.jpg
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE2E4E9), RoundedCornerShape(18.dp))
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Nsave Central QR Code
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onEnlargeQr() }
            ) {
                QrCodeView(
                    data = NSAVE_URL,
                    centerLogo = CenterLogoType.NSAVE,
                    modifier = Modifier.size(200.dp),
                    backgroundColor = Color.White,
                    codeColor = Color.Black,
                    contentPadding = 8.dp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Name: Md Arafath Rahman
            Text(
                text = NSAVE_NAME,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111318)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Handle: @arafath_rahman9 (in vibrant red)
            Text(
                text = NSAVE_HANDLE,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE50914),
                modifier = Modifier.clickable {
                    copyToClipboard(context, NSAVE_HANDLE, "Nsave handle copied!")
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Nsave Description Text matching image
            Text(
                text = "Share your QR code with other nsavers.\nReceive payments instantly at zero fees\nfrom friends & family.",
                fontSize = 13.sp,
                color = Color(0xFF4B5565),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Red pill button matching Nsave.jpg: [ Share Link ]
            Button(
                onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "Pay $NSAVE_NAME on Nsave")
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "Support 123Movies developer $NSAVE_NAME on Nsave! Handle: $NSAVE_HANDLE\nPayment link: $NSAVE_URL"
                        )
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Nsave Payment Link"))
                },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE50914)
                ),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Share Link",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = {
                    copyToClipboard(context, NSAVE_HANDLE, "Copied handle: $NSAVE_HANDLE")
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.ContentCopy,
                    contentDescription = null,
                    tint = Color(0xFF4B5565),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Copy $NSAVE_HANDLE",
                    fontSize = 12.sp,
                    color = Color(0xFF4B5565)
                )
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String, message: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("123Movies Payment", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}
