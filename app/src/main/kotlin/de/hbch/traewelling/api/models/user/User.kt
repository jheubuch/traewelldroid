package de.hbch.traewelling.api.models.user

import com.google.gson.annotations.SerializedName
import de.hbch.traewelling.api.models.station.Station
import de.hbch.traewelling.api.models.status.StatusVisibility
import java.time.ZonedDateTime

data class User(
    @SerializedName("id") val id: Int,
    @SerializedName("displayName") val name: String,
    @SerializedName("username") val username: String,
    @SerializedName("profilePicture") val avatarUrl: String,
    @SerializedName("totalDistance") val distance: Int,
    @SerializedName("totalDuration") val duration: Int,
    @SerializedName("points") val points: Int,
    @SerializedName("mastodonUrl") val mastodonUrl: String?,
    @SerializedName("privateProfile") val privateProfile: Boolean,
    @SerializedName("home") var home: Station?,
    @SerializedName("language") val language: String?,
    @SerializedName("following") val following: Boolean,
    @SerializedName("followPending") val followRequestPending: Boolean,
    @SerializedName("muted") val muted: Boolean,
    val defaultStatusVisibility: StatusVisibility?,
    val followedBy: Boolean
) {
    val averageSpeed: Double get() = (distance / 1000.0) / (duration / 60.0)
}

data class LightUser(
    @SerializedName("id") val id: Int,
    @SerializedName("displayName", alternate = ["name"]) val name: String,
    @SerializedName("username") val username: String,
    @SerializedName("profilePicture") val avatarUrl: String,
    @SerializedName("mastodonUrl") val mastodonUrl: String?
)

data class TrustedUser(
    val user: LightUser,
    val expiresAt: ZonedDateTime?
)

data class CreateTrustedUser(
    val userId: Int,
    val expiresAt: ZonedDateTime?
)
