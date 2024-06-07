package de.hbch.traewelling.shared

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.hbch.traewelling.api.TraewellingApi
import de.hbch.traewelling.api.models.Data
import de.hbch.traewelling.api.models.event.Event
import de.hbch.traewelling.logging.Logger
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EventViewModel : ViewModel() {

    val activeEvents: MutableLiveData<List<Event>> = MutableLiveData(listOf())

    suspend fun activeEvents() {
        activeEvents.postValue(
            try {
                TraewellingApi
                    .checkInService
                    .getEvents()
                    .data
            } catch (_: Exception) {
                listOf()
            }
        )
    }
}
