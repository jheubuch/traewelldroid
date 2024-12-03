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

    fun init(client: UnleashClient) {
        unleashClient = client
        unleashClient?.startPolling()
    }

    fun flagsUpdated() {
        unleashClient?.let {
            _wrappedActive.postValue(it.isEnabled("WrappedActive", false))
        }
    }
}
