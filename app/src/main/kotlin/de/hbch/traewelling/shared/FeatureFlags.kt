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

    private var _wrappedActive = MutableLiveData(false)
    val wrappedActive: LiveData<Boolean> get() = _wrappedActive

    private var _trwlDown = MutableLiveData(false)
    val trwlDown: LiveData<Boolean> get() = _trwlDown

    private var _nearbyActive = MutableLiveData(false)
    val nearbyActive: LiveData<Boolean> get() = _nearbyActive

    fun init(client: UnleashClient) {
        unleashClient = client
        unleashClient?.startPolling()
    }

    fun flagsUpdated() {
        unleashClient?.let {
            _wrappedActive.postValue(it.isEnabled("WrappedActive", false))
            _trwlDown.postValue(it.isEnabled("TrwlDown", false))
            _nearbyActive.postValue(it.isEnabled("NearbyActive", false))
        }
    }
}
