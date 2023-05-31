package de.hbch.traewelling.shared

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.hbch.traewelling.api.TraewellingApi
import de.hbch.traewelling.api.dtos.Trip
import de.hbch.traewelling.api.dtos.TripStation
import de.hbch.traewelling.api.models.Data
import de.hbch.traewelling.api.models.event.Event
import de.hbch.traewelling.api.models.status.CheckInRequest
import de.hbch.traewelling.api.models.status.CheckInResponse
import de.hbch.traewelling.api.models.status.StatusBusiness
import de.hbch.traewelling.api.models.status.StatusVisibility
import io.sentry.Sentry
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class CheckInViewModel : ViewModel() {

    val trip: MutableLiveData<Trip?> = MutableLiveData(null)
    val destinationTripStation: MutableLiveData<TripStation?> = MutableLiveData(null)
    var lineName: String = ""
    var tripId: String = ""
    var startStationId: Int = 0
    var departureTime: Date? = null
    val message = MutableLiveData<String>()
    val toot = MutableLiveData(false)
    val chainToot = MutableLiveData(false)
    val statusVisibility = MutableLiveData(StatusVisibility.PUBLIC)
    val statusBusiness = MutableLiveData(StatusBusiness.PRIVATE)
    val event = MutableLiveData<Event?>()

    init {
        reset()
    }

    fun reset() {
        trip.postValue(null)
        destinationTripStation.postValue(null)
        tripId = ""
        lineName = ""
        startStationId = 0
        departureTime = null
        message.value = ""
        toot.value = false
        chainToot.value = false
        statusVisibility.postValue(StatusVisibility.PUBLIC)
        statusBusiness.postValue(StatusBusiness.PRIVATE)
        event.postValue(null)
    }

    fun checkIn(
        successCallback: (CheckInResponse?) -> Unit,
        failureCallback: (Int) -> Unit
    ) {
        val checkInRequest = CheckInRequest(
            message.value ?: "",
            statusBusiness.value ?: StatusBusiness.PRIVATE,
            statusVisibility.value ?: StatusVisibility.PUBLIC,
            event.value?.id,
            toot.value ?: false,
            chainToot.value ?: false,
            tripId,
            trip.value?.lineName ?: "",
            startStationId,
            destinationTripStation.value?.id ?: 0,
            departureTime ?: Date(),
             destinationTripStation.value?.arrivalReal ?: Date()
        )
        TraewellingApi.checkInService.checkIn(checkInRequest)
            .enqueue(object: Callback<Data<CheckInResponse>> {
                override fun onResponse(
                    call: Call<Data<CheckInResponse>>,
                    response: Response<Data<CheckInResponse>>
                ) {
                    if (response.isSuccessful) {
                        successCallback(response.body()?.data)
                    } else {
                        failureCallback(response.code())
                        Sentry.captureMessage(response.errorBody()?.charStream()?.readText()!!)
                    }
                }
                override fun onFailure(call: Call<Data<CheckInResponse>>, t: Throwable) {
                    failureCallback(-1)
                    Sentry.captureException(t)
                }
            })
    }
}