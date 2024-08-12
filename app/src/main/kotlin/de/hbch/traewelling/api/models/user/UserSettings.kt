package de.hbch.traewelling.api.models.user

import com.google.gson.annotations.SerializedName
import de.hbch.traewelling.api.models.status.AllowedPersonsToCheckIn
import de.hbch.traewelling.api.models.status.StatusVisibility

data class UserSettings(
    val username: String,
    val displayName: String,
    val profilePicture: String,
    val privateProfile: Boolean,
    val defaultStatusVisibility: StatusVisibility,
    val privacyHideDays: Int,
    val email: String,
    @SerializedName("mastodon") val mastodonUrl: String?,
    val mastodonVisibility: StatusVisibility?,
    @SerializedName("friendCheckin") val allowedPersonsToCheckIn: AllowedPersonsToCheckIn,
    val likesEnabled: Boolean,
    val pointsEnabled: Boolean
)

data class SaveUserSettings(
    val username: String,
    val displayName: String,
    val privateProfile: Boolean,
    val defaultStatusVisibility: Int,
    val privacyHideDays: Int?,
    val mastodonVisibility: Int,
    @SerializedName("friendCheckin") val allowedPersonsToCheckIn: AllowedPersonsToCheckIn,
    val likesEnabled: Boolean,
    val pointsEnabled: Boolean
)
