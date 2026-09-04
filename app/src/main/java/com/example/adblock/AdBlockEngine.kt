package com.example.adblock

import android.content.Context
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import com.example.data.AppPreferences
import java.io.ByteArrayInputStream

class AdBlockEngine(private val context: Context) {
    private val prefs = AppPreferences.getInstance(context)

    // Layer 1: Network & Advertising Domain List
    private val defaultAdDomains = setOf(
        "doubleclick.net", "adsystem.com", "popads.net",
        "googlesyndication.com", "google-analytics.com", "adnxs.com",
        "popcash.net", "propellerads.com", "onclickads.net",
        "infolinks.com", "revcontent.com", "taboola.com",
        "outbrain.com", "amazon-adsystem.com", "scorecardresearch.com",
        "yadser", "coinhive", "exoclick", "juicyads", "adsterra",
        "fastclick", "adkeeper", "adservice.google.com", "pagead2.googlesyndication.com",
        "bet365", "1xbet", "parimatch", "mostbet", "vulkan",
        "trafficjunky", "adtrue", "hilltopads", "clickadu", "richpush",
        "monetag", "yllix", "adxad", "adcash", "evadav", "admaven",
        "adsupply", "bidvertiser", "popmyads", "a-ads.com"
    )

    // Layer 2: Ad URL Keyword / Pattern matching
    private val adUrlPatterns = listOf(
        "/ads/", "/ad/", "/advert/", "/banner/", "/banners/",
        "ad_type=", "popup=", "popunder", "pop_under", "clicktag=",
        "partner_id=", "/adclick", "/adserve", "/telemetry/ads",
        "doubleclick", "creativecdn", "adcolony", "applovin",
        "unityads", "vungle", "ironsource"
    )

    // Layer 3: Ad-blocker CSS & JS Injection to destroy ad overlays & prevent click traps
    val adShieldInjectionScript: String = """
        javascript:(function() {
            if (window._123MoviesShieldLoaded) return;
            window._123MoviesShieldLoaded = true;

            // 1. Override window.open to block popups
            var originalOpen = window.open;
            window.open = function(url, target, features) {
                console.log('[123Movies Shield] Blocked window.open popup: ' + url);
                return null;
            };

            // 2. Inject CSS to hide ad frames, overlays, and floating banners
            var style = document.createElement('style');
            style.innerHTML = `
                iframe[src*="ad"], iframe[id*="ad"], iframe[class*="ad"],
                div[id*="ad-"], div[class*="ad-"], div[id*="banner"], div[class*="banner"],
                div[class*="popup"], div[id*="popup"], .ad-overlay, .banner-overlay,
                [data-ad], [data-banner], .adbox, .ad-slot, #overlay-ad,
                a[href*="popads"], a[href*="onclick"], a[href*="track"] {
                    display: none !important;
                    visibility: hidden !important;
                    opacity: 0 !important;
                    pointer-events: none !important;
                    height: 0 !important;
                    width: 0 !important;
                }
            `;
            (document.head || document.documentElement).appendChild(style);

            // 3. Remove invisible click-hijack overlay divs
            setInterval(function() {
                var elements = document.querySelectorAll('div, a, span');
                for (var i = 0; i < elements.length; i++) {
                    var el = elements[i];
                    var zIndex = parseInt(window.getComputedStyle(el).zIndex) || 0;
                    var pos = window.getComputedStyle(el).position;
                    // Catch full-screen invisible clickjack overlays
                    if ((pos === 'fixed' || pos === 'absolute') && zIndex > 9999) {
                        var rect = el.getBoundingClientRect();
                        if (rect.width >= (window.innerWidth - 20) && rect.height >= (window.innerHeight - 20)) {
                            // Check if this is not the video player itself
                            if (!el.querySelector('video')) {
                                el.remove();
                                console.log('[123Movies Shield] Removed click-hijack overlay');
                            }
                        }
                    }
                }
            }, 1200);
        })();
    """.trimIndent()

    fun shouldBlockUrl(url: String, host: String?): Boolean {
        val config = prefs.getAdBlockConfig()
        if (!config.isAdBlockEnabled) return false

        val lowerUrl = url.lowercase()
        val lowerHost = (host ?: "").lowercase()

        // Check custom list first
        if (config.customBlockedDomains.any { lowerUrl.contains(it.lowercase()) }) {
            prefs.incrementBlockedCount()
            return true
        }

        // Layer 1: Domain matching
        if (defaultAdDomains.any { lowerHost.contains(it) || lowerUrl.contains(it) }) {
            prefs.incrementBlockedCount()
            return true
        }

        // Layer 2: Tracker Blocking
        if (config.isTrackerBlockEnabled) {
            if (lowerUrl.contains("google-analytics") || lowerUrl.contains("hotjar") ||
                lowerUrl.contains("statcounter") || lowerUrl.contains("yandex.ru/metrika") ||
                lowerUrl.contains("facebook.net/tr") || lowerUrl.contains("analytics.js")
            ) {
                prefs.incrementBlockedCount()
                return true
            }
        }

        // Layer 3: URL ad pattern matching
        if (adUrlPatterns.any { lowerUrl.contains(it) }) {
            prefs.incrementBlockedCount()
            return true
        }

        return false
    }

    fun handleInterceptRequest(request: WebResourceRequest?): WebResourceResponse? {
        if (request == null) return null
        val url = request.url?.toString() ?: return null
        val host = request.url?.host

        if (shouldBlockUrl(url, host)) {
            Log.d("AdBlockEngine", "Blocked ad resource: $url")
            return WebResourceResponse("text/plain", "UTF-8", ByteArrayInputStream("".toByteArray()))
        }
        return null
    }

    fun shouldBlockNavigation(url: String, isRedirect: Boolean): Boolean {
        val config = prefs.getAdBlockConfig()
        if (!config.isAdBlockEnabled) return false

        val host = try { android.net.Uri.parse(url).host ?: "" } catch (e: Exception) { "" }

        if (shouldBlockUrl(url, host)) {
            return true
        }

        // Redirect Protection
        if (config.isRedirectProtectEnabled && isRedirect) {
            val lowerHost = host.lowercase()
            // If redirecting to known spam/gambling/suspicious domain
            if (lowerHost.contains("bet") || lowerHost.contains("casino") ||
                lowerHost.contains("dating") || lowerHost.contains("survey") ||
                lowerHost.contains("gift") || lowerHost.contains("win-iphone")
            ) {
                prefs.incrementBlockedCount()
                return true
            }
        }

        return false
    }
}
