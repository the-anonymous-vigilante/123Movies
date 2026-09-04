package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

enum class AppDestination {
    NONE,
    PROFILE,
    UPDATES,
    SETTINGS,
    FEEDBACK
}

@Composable
fun Floating3DotMenu(
    isVisible: Boolean,
    hasUpdateBadge: Boolean,
    onNavigate: (AppDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    var isMenuOpen by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(spring(stiffness = Spring.StiffnessMedium)) + scaleIn(),
        exit = fadeOut(spring(stiffness = Spring.StiffnessMedium)) + scaleOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .padding(bottom = 24.dp, end = 20.dp)
                .navigationBarsPadding(),
            contentAlignment = Alignment.BottomEnd
        ) {
            FloatingActionButton(
                onClick = { isMenuOpen = true },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "123Movies Options Menu",
                        modifier = Modifier.size(28.dp)
                    )

                    if (hasUpdateBadge) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFE50914),
                            modifier = Modifier
                                .size(12.dp)
                                .align(Alignment.TopEnd)
                        ) {}
                    }
                }
            }
        }
    }

    // Mini Popup Window matching 123Movies streaming styling
    if (isMenuOpen) {
        Dialog(
            onDismissRequest = { isMenuOpen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { isMenuOpen = false }
                    .padding(horizontal = 24.dp, vertical = 40.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    shadowElevation = 16.dp,
                    modifier = Modifier
                        .widthIn(max = 300.dp)
                        .clickable(enabled = false) {}
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            RoundedCornerShape(24.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(8.dp)
                                ) {}
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "123Movies Menu",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(
                                onClick = { isMenuOpen = false },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Filled.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                            }
                        }

                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        // 1. Profile
                        MiniMenuItem(
                            icon = Icons.Filled.Person,
                            title = "Profile",
                            subtitle = "Name & Avatar settings",
                            badge = null,
                            onClick = {
                                isMenuOpen = false
                                onNavigate(AppDestination.PROFILE)
                            }
                        )

                        // 2. App Updates
                        MiniMenuItem(
                            icon = Icons.Filled.SystemUpdate,
                            title = "App Updates",
                            subtitle = "GitHub releases & history",
                            badge = if (hasUpdateBadge) "NEW" else null,
                            badgeColor = Color(0xFFE50914),
                            onClick = {
                                isMenuOpen = false
                                onNavigate(AppDestination.UPDATES)
                            }
                        )

                        // 3. Settings
                        MiniMenuItem(
                            icon = Icons.Filled.Settings,
                            title = "Settings",
                            subtitle = "HDR, Proxy & AdBlock",
                            badge = null,
                            onClick = {
                                isMenuOpen = false
                                onNavigate(AppDestination.SETTINGS)
                            }
                        )

                        // 4. Feedback & Reports
                        MiniMenuItem(
                            icon = Icons.Filled.RateReview,
                            title = "Feedback & Reports",
                            subtitle = "Bugs & Feature requests",
                            badge = null,
                            onClick = {
                                isMenuOpen = false
                                onNavigate(AppDestination.FEEDBACK)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badge: String? = null,
    badgeColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }

            if (badge != null) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = badgeColor,
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Text(
                        text = badge,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
