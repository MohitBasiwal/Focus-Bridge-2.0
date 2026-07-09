package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.domain.repository.SecurityRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * BroadcastReceiver triggered on system boot. Logs reboot events and restores
 * the Focus Bridge active blocking rules.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var securityRepository: SecurityRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val config = securityRepository.getSecurityConfigSnapshot()
                    val isHealthy = securityRepository.isProtectionActive()
                    
                    if (isHealthy) {
                        if (config != null && config.isProtectionPaused) {
                            // If it was manually paused or marked paused, attempt to unpause it if permissions are fully green
                            securityRepository.saveSecurityConfig(config.copy(isProtectionPaused = false))
                        }
                        securityRepository.logSecurityEvent(
                            eventType = "BOOT_COMPLETED",
                            details = "Device reboot detected. Protection is fully active and verified.",
                            severity = "INFO"
                        )
                    } else {
                        // Permissions were lost or not fully restored on reboot
                        securityRepository.logSecurityEvent(
                            eventType = "BOOT_COMPLETED",
                            details = "Device reboot detected. Protection was PAUSED because some required permissions are not granted.",
                            severity = "WARNING"
                        )
                    }
                } catch (e: Exception) {
                    // Fail-safe
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
