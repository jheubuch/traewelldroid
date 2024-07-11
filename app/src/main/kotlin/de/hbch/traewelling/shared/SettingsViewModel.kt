package de.hbch.traewelling.shared

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jcloquell.androidsecurestorage.SecureStorage

class SettingsViewModel : ViewModel() {

    private val _displayTagsInCard = MutableLiveData(true)
    val displayTagsInCard: LiveData<Boolean> get() = _displayTagsInCard

    private val _displayJourneyNumber = MutableLiveData(true)
    val displayJourneyNumber: LiveData<Boolean> get() = _displayJourneyNumber

    private val _displayDivergentStop = MutableLiveData(true)
    val displayDivergentStop: LiveData<Boolean> get() = _displayDivergentStop

    fun loadSettings(context: Context) {
        val secureStorage = SecureStorage(context)

        _displayTagsInCard.postValue(
            secureStorage.getObject(SharedValues.SS_DISPLAY_TAGS_IN_CARD, Boolean::class.java) ?: true
        )
        _displayJourneyNumber.postValue(
            secureStorage.getObject(SharedValues.SS_DISPLAY_JOURNEY_NUMBER, Boolean::class.java) ?: true
        )
        _displayDivergentStop.postValue(
            secureStorage.getObject(SharedValues.SS_DISPLAY_DIVERGENT_STOP, Boolean::class.java) ?: true
        )
    }

    fun updateDisplayTagsInCard(context: Context, state: Boolean) {
        val secureStorage = SecureStorage(context)
        secureStorage.storeObject(SharedValues.SS_DISPLAY_TAGS_IN_CARD, state)
        _displayTagsInCard.postValue(state)
    }

    fun updateDisplayJourneyNumber(context: Context, state: Boolean) {
        val secureStorage = SecureStorage(context)
        secureStorage.storeObject(SharedValues.SS_DISPLAY_JOURNEY_NUMBER, state)
        _displayJourneyNumber.postValue(state)
    }

    fun updateDisplayDivergentStop(context: Context, state: Boolean) {
        val secureStorage = SecureStorage(context)
        secureStorage.storeObject(SharedValues.SS_DISPLAY_DIVERGENT_STOP, state)
        _displayDivergentStop.postValue(state)
    }
}