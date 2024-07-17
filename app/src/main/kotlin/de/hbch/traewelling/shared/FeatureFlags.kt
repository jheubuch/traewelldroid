package de.hbch.traewelling.shared

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.getunleash.UnleashClient

class FeatureFlags private constructor() {
    companion object {
        private var instance: FeatureFlags? = null

        fun getInstance() =
            instance ?: FeatureFlags().also { instance = it }
    }

    private var unleashClient: UnleashClient? = null

    fun init(client: UnleashClient) {
        unleashClient = client
        unleashClient?.startPolling()
    }

    fun flagsUpdated() {
        unleashClient?.let {
            _statusReports.postValue(it.isEnabled("StatusReports", false))
        }
    }

    // Add feature flags as LiveData so they can be state-subscribed in Compose
    private val _statusReports = MutableLiveData(false)
    val statusReports: LiveData<Boolean> get() = _statusReports
}
