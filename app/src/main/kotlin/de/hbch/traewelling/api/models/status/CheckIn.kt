package de.hbch.traewelling.api.models.status

import com.google.gson.annotations.SerializedName
import de.hbch.traewelling.providers.checkin.CheckInRequest
import java.time.ZonedDateTime

data class TrwlCheckInRequest(
    val body: String,
    val business: Int,
    val visibility: Int,
    val eventId: Int?,
    @SerializedName("toot") val sendToot: Boolean,
    @SerializedName("chainPost") val shouldChainToot: Boolean,
    val tripId: String,
    val lineName: String,
    @SerializedName("start") val startStationId: Int,
    @SerializedName("destination") val destinationStationId: Int,
    @SerializedName("departure") val departureTime: ZonedDateTime,
    @SerializedName("arrival") val arrivalTime: ZonedDateTime,
    val force: Boolean = false
): CheckInRequest()

data class TrwlCheckInResponse(
    @SerializedName("status") val status: Status,
    @SerializedName("alsoOnThisConnection") val coTravellers: List<Status>,
    @SerializedName("points") val points: StatusPoints
)
