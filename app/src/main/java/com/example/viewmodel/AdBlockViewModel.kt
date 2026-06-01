package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.VpnService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.AdBlockVpnService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdBlockViewModel(application: Application) : AndroidViewModel(application) {
    
    private val prefs = application.getSharedPreferences("adblock_prefs", Context.MODE_PRIVATE)

    private val _isVpnActive = MutableStateFlow(AdBlockVpnService.isVpnActive)
    val isVpnActive: StateFlow<Boolean> = _isVpnActive.asStateFlow()

    private val _adsBlockedToday = MutableStateFlow(0)
    val adsBlockedToday: StateFlow<Int> = _adsBlockedToday.asStateFlow()

    private val _adsBlockedWeek = MutableStateFlow(0)
    val adsBlockedWeek: StateFlow<Int> = _adsBlockedWeek.asStateFlow()

    private val _totalAdsBlocked = MutableStateFlow(0)
    val totalAdsBlocked: StateFlow<Int> = _totalAdsBlocked.asStateFlow()
    
    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        updateStats()
    }
    
    init {
        prefs.registerOnSharedPreferenceChangeListener(prefsListener)
        updateStats()
    }

    private fun updateStats() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val todayKey = "ads_blocked_$today"
        _adsBlockedToday.value = prefs.getInt(todayKey, 0)
        _totalAdsBlocked.value = prefs.getInt("ads_blocked_total", 0)
        _adsBlockedWeek.value = prefs.getInt("ads_blocked_total", 0) // Approximation for week if needed
    }

    override fun onCleared() {
        super.onCleared()
        prefs.unregisterOnSharedPreferenceChangeListener(prefsListener)
    }

    fun toggleVpn(context: Context, onVpnPermissionRequired: (Intent) -> Unit) {
        if (_isVpnActive.value) {
            stopVpn(context)
        } else {
            startVpn(context, onVpnPermissionRequired)
        }
    }

    private fun startVpn(context: Context, onVpnPermissionRequired: (Intent) -> Unit) {
        val intent = VpnService.prepare(context)
        if (intent != null) {
            onVpnPermissionRequired(intent)
        } else {
            startVpnService(context)
        }
    }

    fun startVpnService(context: Context) {
        try {
            val vpnIntent = Intent(context, AdBlockVpnService::class.java).apply {
                action = AdBlockVpnService.ACTION_START
            }
            context.startService(vpnIntent)
            _isVpnActive.value = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopVpn(context: Context) {
        try {
            val vpnIntent = Intent(context, AdBlockVpnService::class.java).apply {
                action = AdBlockVpnService.ACTION_STOP
            }
            context.startService(vpnIntent)
            _isVpnActive.value = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun checkStatus() {
        _isVpnActive.value = AdBlockVpnService.isVpnActive
        updateStats()
    }
}
