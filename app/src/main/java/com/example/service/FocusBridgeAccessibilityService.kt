package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.domain.repository.BlockerRepository
import com.example.ui.screens.FocusLockActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Custom Accessibility Service to perform real-time app blocking and website URL monitoring.
 * Activates rules automatically only when a scheduled study session is active in the timetable database.
 */
@AndroidEntryPoint
class FocusBridgeAccessibilityService : AccessibilityService() {

    @Inject
    lateinit var blockerRepository: BlockerRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: return

        serviceScope.launch {
            // Check if study session is active
            if (blockerRepository.isStudySessionActiveNow()) {
                
                // 1. App Blocking Execution
                val isAllowed = blockerRepository.isAppAllowed(packageName)
                if (!isAllowed) {
                    lockScreen()
                    return@launch
                }

                // 2. Website Blocking Execution (Monitor popular browsers)
                if (isBrowserApp(packageName)) {
                    val rootNode = rootInActiveWindow
                    if (rootNode != null) {
                        val currentUrl = extractUrl(rootNode)
                        if (currentUrl != null && blockerRepository.isWebsiteBlocked(currentUrl)) {
                            lockScreen()
                        }
                    }
                }
            }
        }
    }

    override fun onInterrupt() {
        // Accessibility service interrupted
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun lockScreen() {
        val intent = Intent(this, FocusLockActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }

    private fun isBrowserApp(packageName: String): Boolean {
        return packageName.contains("chrome") ||
                packageName.contains("browser") ||
                packageName.contains("firefox") ||
                packageName.contains("opera") ||
                packageName.contains("microsoft.emmx")
    }

    /**
     * Traverses the node tree recursively to extract URL or address text from browser apps.
     */
    private fun extractUrl(node: AccessibilityNodeInfo): String? {
        val viewId = node.viewIdResourceName
        if (viewId != null && (viewId.contains("url_bar") || viewId.contains("url_edit_text") || viewId.contains("address_bar"))) {
            node.text?.toString()?.let { return sanitizeUrl(it) }
        }

        val text = node.text?.toString()
        if (text != null && (text.contains("www.") || text.contains(".com") || text.contains(".org") || text.contains(".net") || text.contains(".edu"))) {
            return sanitizeUrl(text)
        }

        // Recurse children
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = extractUrl(child)
            if (result != null) {
                return result
            }
        }
        return null
    }

    private fun sanitizeUrl(input: String): String {
        var clean = input.trim().lowercase()
        if (clean.startsWith("http://")) clean = clean.substring(7)
        if (clean.startsWith("https://")) clean = clean.substring(8)
        if (clean.startsWith("www.")) clean = clean.substring(4)
        
        // Remove trailing query parameters or paths
        val slashIndex = clean.indexOf('/')
        if (slashIndex != -1) {
            clean = clean.substring(0, slashIndex)
        }
        return clean
    }
}
