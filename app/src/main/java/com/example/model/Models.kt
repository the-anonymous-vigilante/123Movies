package com.example.model

data class UserProfile(
    val name: String = "Movie Fan",
    val avatarUri: String? = null,
    val avatarEmoji: String = "🎬",
    val memberStatus: String = "VIP Member"
)

data class ProxyConfig(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String = "Default Proxy",
    val type: String = "HTTP", // HTTP, HTTPS, SOCKS5
    val host: String = "",
    val port: Int = 8080,
    val username: String = "",
    val password: String = "",
    val isEnabled: Boolean = false
)

data class AppUpdateInfo(
    val version: String,
    val title: String,
    val releaseDate: String,
    val isNewer: Boolean,
    val downloadUrl: String,
    val changelog: String,
    val newFeatures: List<String> = emptyList(),
    val bugFixes: List<String> = emptyList(),
    val improvements: List<String> = emptyList()
)

data class AdBlockConfig(
    val isAdBlockEnabled: Boolean = true,
    val isTrackerBlockEnabled: Boolean = true,
    val isPopupBlockEnabled: Boolean = true,
    val isRedirectProtectEnabled: Boolean = true,
    val customBlockedDomains: List<String> = emptyList()
)

data class VideoDisplayConfig(
    val isHdrEnabled: Boolean = false,
    val isDolbyVisionEnabled: Boolean = false,
    val isSmartEnhanceEnabled: Boolean = true
)
