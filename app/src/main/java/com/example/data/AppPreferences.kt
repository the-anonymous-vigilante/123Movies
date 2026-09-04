package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AdBlockConfig
import com.example.model.ProxyConfig
import com.example.model.UserProfile
import com.example.model.VideoDisplayConfig
import org.json.JSONArray
import org.json.JSONObject

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("123movies_prefs", Context.MODE_PRIVATE)

    companion object {
        const val CURRENT_APP_VERSION = "v1.0.5"
        const val GITHUB_RELEASES_URL = "https://github.com/the-anonymous-vigilante/123Movies/releases"
        const val GITHUB_LATEST_RELEASE_URL = "https://github.com/the-anonymous-vigilante/123Movies/releases/latest"
        const val GITHUB_DIRECT_APK_DOWNLOAD_URL = "https://github.com/the-anonymous-vigilante/123Movies/releases/latest/download/123Movies.apk"
        const val GITHUB_API_RELEASES_URL = "https://api.github.com/repos/the-anonymous-vigilante/123Movies/releases"

        @Volatile
        private var INSTANCE: AppPreferences? = null

        fun getInstance(context: Context): AppPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppPreferences(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    // Profile
    fun getUserProfile(): UserProfile {
        val name = prefs.getString("profile_name", "Movie Fan") ?: "Movie Fan"
        val avatarUri = prefs.getString("profile_avatar_uri", null)
        val avatarEmoji = prefs.getString("profile_avatar_emoji", "🎬") ?: "🎬"
        val status = prefs.getString("profile_status", "VIP Member") ?: "VIP Member"
        return UserProfile(name, avatarUri, avatarEmoji, status)
    }

    fun saveUserProfile(profile: UserProfile) {
        prefs.edit()
            .putString("profile_name", profile.name)
            .putString("profile_avatar_uri", profile.avatarUri)
            .putString("profile_avatar_emoji", profile.avatarEmoji)
            .putString("profile_status", profile.memberStatus)
            .apply()
    }

    // Video Display
    fun getVideoConfig(): VideoDisplayConfig {
        return VideoDisplayConfig(
            isHdrEnabled = prefs.getBoolean("video_hdr", false),
            isDolbyVisionEnabled = prefs.getBoolean("video_dolby", false),
            isSmartEnhanceEnabled = prefs.getBoolean("video_smart_enhance", true)
        )
    }

    fun saveVideoConfig(config: VideoDisplayConfig) {
        prefs.edit()
            .putBoolean("video_hdr", config.isHdrEnabled)
            .putBoolean("video_dolby", config.isDolbyVisionEnabled)
            .putBoolean("video_smart_enhance", config.isSmartEnhanceEnabled)
            .apply()
    }

    // AdBlock Config
    fun getAdBlockConfig(): AdBlockConfig {
        val enabled = prefs.getBoolean("adblock_enabled", true)
        val tracker = prefs.getBoolean("adblock_tracker", true)
        val popup = prefs.getBoolean("adblock_popup", true)
        val redirect = prefs.getBoolean("adblock_redirect", true)
        val customListStr = prefs.getString("adblock_custom_list", "[]") ?: "[]"
        val customList = mutableListOf<String>()
        try {
            val jsonArray = JSONArray(customListStr)
            for (i in 0 until jsonArray.length()) {
                customList.add(jsonArray.getString(i))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return AdBlockConfig(enabled, tracker, popup, redirect, customList)
    }

    fun saveAdBlockConfig(config: AdBlockConfig) {
        val jsonArray = JSONArray()
        config.customBlockedDomains.forEach { jsonArray.put(it) }
        prefs.edit()
            .putBoolean("adblock_enabled", config.isAdBlockEnabled)
            .putBoolean("adblock_tracker", config.isTrackerBlockEnabled)
            .putBoolean("adblock_popup", config.isPopupBlockEnabled)
            .putBoolean("adblock_redirect", config.isRedirectProtectEnabled)
            .putString("adblock_custom_list", jsonArray.toString())
            .apply()
    }

    // Block statistics
    fun getBlockedCount(): Long {
        return prefs.getLong("adblock_total_blocked", 142)
    }

    fun incrementBlockedCount() {
        val current = prefs.getLong("adblock_total_blocked", 142)
        prefs.edit().putLong("adblock_total_blocked", current + 1).apply()
    }

    fun resetBlockedCount() {
        prefs.edit().putLong("adblock_total_blocked", 0).apply()
    }

    // Proxy Configs
    fun getProxies(): List<ProxyConfig> {
        val jsonStr = prefs.getString("proxy_list", null)
        if (jsonStr.isNullOrEmpty()) {
            return listOf(
                ProxyConfig(
                    id = "default-1",
                    name = "Direct Streaming (No Proxy)",
                    type = "HTTP",
                    host = "",
                    port = 8080,
                    isEnabled = false
                )
            )
        }
        val list = mutableListOf<ProxyConfig>()
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    ProxyConfig(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        type = obj.getString("type"),
                        host = obj.getString("host"),
                        port = obj.getInt("port"),
                        username = obj.optString("username", ""),
                        password = obj.optString("password", ""),
                        isEnabled = obj.optBoolean("isEnabled", false)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return if (list.isEmpty()) {
            listOf(ProxyConfig(id = "default-1", name = "Default Tunnel", host = "127.0.0.1", port = 8080))
        } else list
    }

    fun saveProxies(proxies: List<ProxyConfig>) {
        val arr = JSONArray()
        for (p in proxies) {
            val obj = JSONObject().apply {
                put("id", p.id)
                put("name", p.name)
                put("type", p.type)
                put("host", p.host)
                put("port", p.port)
                put("username", p.username)
                put("password", p.password)
                put("isEnabled", p.isEnabled)
            }
            arr.put(obj)
        }
        prefs.edit().putString("proxy_list", arr.toString()).apply()
    }

    // Last seen update version
    fun getLastPromptedUpdate(): String {
        return prefs.getString("last_prompted_update", "") ?: ""
    }

    fun setLastPromptedUpdate(version: String) {
        prefs.edit().putString("last_prompted_update", version).apply()
    }
}
