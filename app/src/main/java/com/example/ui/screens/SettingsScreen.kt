package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.view.Display
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppPreferences
import com.example.model.AdBlockConfig
import com.example.model.ProxyConfig
import com.example.model.VideoDisplayConfig
import com.example.ui.components.BuyMeACoffeeSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { AppPreferences.getInstance(context) }

    // Settings States loaded from persistent storage
    var videoConfig by remember { mutableStateOf(prefs.getVideoConfig()) }
    var adBlockConfig by remember { mutableStateOf(prefs.getAdBlockConfig()) }
    var blockedCount by remember { mutableLongStateOf(prefs.getBlockedCount()) }
    var proxies by remember { mutableStateOf(prefs.getProxies()) }

    // Device Hardware Capabilities Detection
    val (isDeviceHdrSupported, isDolbyVisionSupported) = remember {
        detectDeviceVideoCapabilities(context)
    }

    // Dialog States
    var showAddProxyDialog by remember { mutableStateOf(false) }
    var editingProxy by remember { mutableStateOf<ProxyConfig?>(null) }
    var showCustomFilterDialog by remember { mutableStateOf(false) }
    var newFilterDomain by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.SemiBold) },
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
            // Section 1: Display & Video Features
            SettingsSectionHeader(icon = Icons.Filled.Tv, title = "Display & Video Features")
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // HDR Feature
                    SettingSwitchItem(
                        title = "High Dynamic Range (HDR)",
                        subtitle = if (isDeviceHdrSupported)
                            "Enhance contrast and color richness for HDR10/HLG streams"
                        else
                            "Unavailable: Device display hardware does not support HDR",
                        checked = if (isDeviceHdrSupported) videoConfig.isHdrEnabled else false,
                        enabled = isDeviceHdrSupported,
                        onCheckedChange = { checked ->
                            if (isDeviceHdrSupported) {
                                val updated = videoConfig.copy(isHdrEnabled = checked)
                                videoConfig = updated
                                prefs.saveVideoConfig(updated)
                            }
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)

                    // Dolby Vision Feature
                    SettingSwitchItem(
                        title = "Dolby Vision Playback",
                        subtitle = if (isDolbyVisionSupported)
                            "Dynamic frame-by-frame metadata color grading"
                        else
                            "Unavailable: Dolby Vision decoder not licensed on this device",
                        checked = if (isDolbyVisionSupported) videoConfig.isDolbyVisionEnabled else false,
                        enabled = isDolbyVisionSupported,
                        onCheckedChange = { checked ->
                            if (isDolbyVisionSupported) {
                                val updated = videoConfig.copy(isDolbyVisionEnabled = checked)
                                videoConfig = updated
                                prefs.saveVideoConfig(updated)
                            }
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)

                    // Smart Enhance Feature
                    SettingSwitchItem(
                        title = "Smart Enhance Engine",
                        subtitle = "Adaptive hardware clarity, frame rate smoothing, and dark-scene enhancement",
                        checked = videoConfig.isSmartEnhanceEnabled,
                        enabled = true,
                        onCheckedChange = { checked ->
                            val updated = videoConfig.copy(isSmartEnhanceEnabled = checked)
                            videoConfig = updated
                            prefs.saveVideoConfig(updated)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Section 2: Advanced Ad Blocking System
            SettingsSectionHeader(icon = Icons.Filled.Shield, title = "Advanced Ad Blocking")
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Main Adblock switch
                    SettingSwitchItem(
                        title = "AdShield Multi-Layer Engine",
                        subtitle = "Block banner ads, video popups, and click-jack overlays",
                        checked = adBlockConfig.isAdBlockEnabled,
                        enabled = true,
                        onCheckedChange = { checked ->
                            val updated = adBlockConfig.copy(isAdBlockEnabled = checked)
                            adBlockConfig = updated
                            prefs.saveAdBlockConfig(updated)
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)

                    // Tracker blocking
                    SettingSwitchItem(
                        title = "Tracker & Telemetry Shield",
                        subtitle = "Filter invasive analytics scripts and behavioral trackers",
                        checked = adBlockConfig.isTrackerBlockEnabled,
                        enabled = adBlockConfig.isAdBlockEnabled,
                        onCheckedChange = { checked ->
                            val updated = adBlockConfig.copy(isTrackerBlockEnabled = checked)
                            adBlockConfig = updated
                            prefs.saveAdBlockConfig(updated)
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)

                    // Popup & Pop-Under Blocking
                    SettingSwitchItem(
                        title = "Popup & New Window Blocker",
                        subtitle = "Prevent unsolicited tab spawns and background pop-unders",
                        checked = adBlockConfig.isPopupBlockEnabled,
                        enabled = adBlockConfig.isAdBlockEnabled,
                        onCheckedChange = { checked ->
                            val updated = adBlockConfig.copy(isPopupBlockEnabled = checked)
                            adBlockConfig = updated
                            prefs.saveAdBlockConfig(updated)
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)

                    // Redirect Protection
                    SettingSwitchItem(
                        title = "Auto-Redirect Guard",
                        subtitle = "Stop malicious automatic domain rerouting to gambling or scam sites",
                        checked = adBlockConfig.isRedirectProtectEnabled,
                        enabled = adBlockConfig.isAdBlockEnabled,
                        onCheckedChange = { checked ->
                            val updated = adBlockConfig.copy(isRedirectProtectEnabled = checked)
                            adBlockConfig = updated
                            prefs.saveAdBlockConfig(updated)
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)

                    // Statistics & Filter list management
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Ads & Trackers Blocked",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$blockedCount items filtered",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        TextButton(onClick = {
                            prefs.resetBlockedCount()
                            blockedCount = 0
                            Toast.makeText(context, "Stats reset", Toast.LENGTH_SHORT).show()
                        }) {
                            Text("Reset Count")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { showCustomFilterDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Filled.FilterList, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Custom Block List (${adBlockConfig.customBlockedDomains.size} domains)")
                    }
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Section 3: Advanced Proxy System
            SettingsSectionHeader(icon = Icons.Filled.VpnKey, title = "Advanced Proxy System")
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Configured Tunnels",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = {
                            editingProxy = ProxyConfig(host = "", port = 8080)
                            showAddProxyDialog = true
                        }) {
                            Icon(Icons.Filled.Add, contentDescription = "Add Proxy")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    proxies.forEach { proxy ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (proxy.isEnabled)
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                else
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = proxy.name,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = proxy.type,
                                                fontSize = 10.sp,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = if (proxy.host.isNotEmpty()) "${proxy.host}:${proxy.port}" else "Direct Connection",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            editingProxy = proxy
                                            showAddProxyDialog = true
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Filled.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp))
                                    }

                                    Switch(
                                        checked = proxy.isEnabled,
                                        onCheckedChange = { isChecked ->
                                            val updatedList = proxies.map { p ->
                                                if (p.id == proxy.id) p.copy(isEnabled = isChecked)
                                                else if (isChecked) p.copy(isEnabled = false) // only 1 active
                                                else p
                                            }
                                            proxies = updatedList
                                            prefs.saveProxies(updatedList)
                                            Toast.makeText(
                                                context,
                                                if (isChecked) "Proxy enabled: ${proxy.name}" else "Proxy disabled",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Section 4: Support Developer / Buy Me A Coffee (RedotPay & Nsave)
            BuyMeACoffeeSection()

            Spacer(modifier = Modifier.height(36.dp))
        }
    }

    // Proxy Edit / Add Dialog
    if (showAddProxyDialog && editingProxy != null) {
        var name by remember { mutableStateOf(editingProxy!!.name) }
        var type by remember { mutableStateOf(editingProxy!!.type) }
        var host by remember { mutableStateOf(editingProxy!!.host) }
        var portStr by remember { mutableStateOf(editingProxy!!.port.toString()) }
        var username by remember { mutableStateOf(editingProxy!!.username) }
        var password by remember { mutableStateOf(editingProxy!!.password) }

        AlertDialog(
            onDismissRequest = { showAddProxyDialog = false },
            title = { Text(if (proxies.any { it.id == editingProxy!!.id }) "Edit Proxy" else "Add Proxy") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Profile Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Type selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("HTTP", "HTTPS", "SOCKS5").forEach { t ->
                            FilterChip(
                                selected = type == t,
                                onClick = { type = t },
                                label = { Text(t) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = host,
                        onValueChange = { host = it },
                        label = { Text("Server Host / IP") },
                        singleLine = true,
                        placeholder = { Text("e.g. 192.168.1.100") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = portStr,
                        onValueChange = { portStr = it },
                        label = { Text("Port") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password (Optional)") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsedPort = portStr.toIntOrNull() ?: 8080
                        val newProxy = editingProxy!!.copy(
                            name = name.ifEmpty { "Proxy Profile" },
                            type = type,
                            host = host.trim(),
                            port = parsedPort,
                            username = username.trim(),
                            password = password
                        )

                        val updatedList = if (proxies.any { it.id == newProxy.id }) {
                            proxies.map { if (it.id == newProxy.id) newProxy else it }
                        } else {
                            proxies + newProxy
                        }
                        proxies = updatedList
                        prefs.saveProxies(updatedList)
                        showAddProxyDialog = false
                        Toast.makeText(context, "Proxy configuration saved", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                Row {
                    if (proxies.any { it.id == editingProxy!!.id }) {
                        TextButton(
                            onClick = {
                                val updated = proxies.filter { it.id != editingProxy!!.id }
                                proxies = updated
                                prefs.saveProxies(updated)
                                showAddProxyDialog = false
                                Toast.makeText(context, "Proxy deleted", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Delete")
                        }
                    }
                    TextButton(onClick = { showAddProxyDialog = false }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }

    // Custom Blocked Domains Dialog
    if (showCustomFilterDialog) {
        AlertDialog(
            onDismissRequest = { showCustomFilterDialog = false },
            title = { Text("Custom Blocked Domains") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newFilterDomain,
                            onValueChange = { newFilterDomain = it },
                            label = { Text("Domain (e.g. badsite.com)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                val d = newFilterDomain.trim().lowercase()
                                if (d.isNotEmpty() && !adBlockConfig.customBlockedDomains.contains(d)) {
                                    val updated = adBlockConfig.copy(
                                        customBlockedDomains = adBlockConfig.customBlockedDomains + d
                                    )
                                    adBlockConfig = updated
                                    prefs.saveAdBlockConfig(updated)
                                    newFilterDomain = ""
                                }
                            }
                        ) {
                            Icon(Icons.Filled.AddCircle, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (adBlockConfig.customBlockedDomains.isEmpty()) {
                        Text(
                            text = "No custom domains added yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .heightIn(max = 200.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            adBlockConfig.customBlockedDomains.forEach { domain ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(domain, style = MaterialTheme.typography.bodyMedium)
                                    IconButton(
                                        onClick = {
                                            val updated = adBlockConfig.copy(
                                                customBlockedDomains = adBlockConfig.customBlockedDomains - domain
                                            )
                                            adBlockConfig = updated
                                            prefs.saveAdBlockConfig(updated)
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Filled.Close, contentDescription = "Remove", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showCustomFilterDialog = false }) {
                    Text("Done")
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(icon: ImageVector, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SettingSwitchItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

private fun detectDeviceVideoCapabilities(context: Context): Pair<Boolean, Boolean> {
    var hdrSupported = false
    var dolbyVisionSupported = false

    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
            val display: Display? = windowManager?.defaultDisplay

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                hdrSupported = display?.isHdr == true
            }

            val hdrCapabilities = display?.hdrCapabilities
            if (hdrCapabilities != null) {
                val types = hdrCapabilities.supportedHdrTypes
                if (types.isNotEmpty()) {
                    hdrSupported = true
                }
                if (types.contains(Display.HdrCapabilities.HDR_TYPE_DOLBY_VISION)) {
                    dolbyVisionSupported = true
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return Pair(hdrSupported, dolbyVisionSupported)
}
