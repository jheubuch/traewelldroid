package de.hbch.traewelling.api.models.wrapped

import com.google.gson.annotations.SerializedName
import de.hbch.traewelling.api.models.station.Station
import de.hbch.traewelling.api.models.status.Status
import de.hbch.traewelling.api.models.user.LightUser

data class YearInReviewData(
    val user: LightUser,
    val count: Int,
    val distance: YearInReviewSumStats,
    val duration: YearInReviewSumStats,
    val totalDelay: Int,
    val operators: TopBy,
    val lines: TopBy,
    val longestTrips: LongestTrips,
    @SerializedName("fastestTrips") val fastestTrip: Status,
    @SerializedName("slowestTrips") val slowestTrip: Status,
    @SerializedName("mostDelayedArrivals") val mostDelayedArrival: Status,
    val topDestinations: List<TopStation>,
    val lonelyStations: List<TopStation>,
    val mostLikedStatuses: List<MostLikedStatus>
)

data class YearInReviewSumStats(
    val total: Long,
    val averagePerDay: Float
)

data class TopByDistance(
    val operator: String,
    val distance: Long,
    val line: String?
)

data class TopByDuration(
    val operator: String,
    val duration: Long,
    val line: String?
)

data class TopBy(
    val count: Int?,
    val topByDistance: TopByDistance,
    val topByDuration: TopByDuration
)

data class LongestTrips(
    val distance: Status,
    val duration: Status
)

data class TopStation(
    val count: Int,
    val station: Station
)

data class MostLikedStatus(
    val likeCount: Int,
    val status: Status
)
