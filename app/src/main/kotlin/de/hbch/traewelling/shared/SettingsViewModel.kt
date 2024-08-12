package de.hbch.traewelling.shared

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jcloquell.androidsecurestorage.SecureStorage
import de.hbch.traewelling.api.TraewellingApi
import de.hbch.traewelling.api.models.user.SaveUserSettings
import de.hbch.traewelling.api.models.user.UserSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {

    private val _displayTagsInCard = MutableLiveData(true)
    val displayTagsInCard: LiveData<Boolean> get() = _displayTagsInCard

    private val _displayJourneyNumber = MutableLiveData(true)
    val displayJourneyNumber: LiveData<Boolean> get() = _displayJourneyNumber

    private val _displayDivergentStop = MutableLiveData(true)
    val displayDivergentStop: LiveData<Boolean> get() = _displayDivergentStop

    private val _userSettings = MutableLiveData<UserSettings?>(null)
    val userSettings: LiveData<UserSettings?> get() = _userSettings

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

        val coroutineScope = CoroutineScope(Dispatchers.IO)
        coroutineScope.launch {
            getUserSettings()
        }
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

    suspend fun getUserSettings() {
        val settings = try {
            val response = TraewellingApi.userService.getUserSettings()
            if (response.isSuccessful) {
                response.body()?.data
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
        _userSettings.postValue(settings)
    }

    suspend fun saveUserSettings(settings: SaveUserSettings): UserSettings? {
        val newSettings = try {
            val response = TraewellingApi.userService.saveUserSettings(settings)
            if (response.isSuccessful) {
                response.body()?.data
            } else {
                null
            }
        } catch (ex: Exception) {
            null
        }
        if (newSettings != null) {
            _userSettings.postValue(newSettings)
        }
        return newSettings
    }
}