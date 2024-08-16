package de.hbch.traewelling.api.models.notifications

import com.google.gson.annotations.SerializedName
import de.hbch.traewelling.api.models.meta.PaginationData
import java.time.ZonedDateTime

data class NotificationPage(
    val data: List<Notification>,
    val meta: PaginationData
)

data class Notification(
    val id: String,
    val type: NotificationType?,
    var readAt: ZonedDateTime?,
    val createdAt: ZonedDateTime?,
    val data: Any
) {
    val safeType get() = type ?: NotificationType.Unknown
    val safeCreatedAt: ZonedDateTime get() = createdAt ?: ZonedDateTime.now()
}

data class NotificationStation(
    val name: String
)
data class NotificationUser(
    val id: Int,
    val username: String,
    val name: String
)
data class NotificationTrip<T>(
    val origin: T,
    val destination: T,
    @SerializedName("lineName", alternate = [ "linename", "line" ]) val lineName: String
)
data class NotificationStatus(
    val id: Int
)

data class StatusLikedNotificationData(
    val trip: NotificationTrip<NotificationStation>,
    val liker: NotificationUser,
    val status: NotificationStatus
)

data class EventSuggestionProcessedData(
    val accepted: Boolean,
    val suggestedName: String
)

data class UserFollowedData(
    val follower: NotificationUser
)

data class UserJoinedConnectionData(
    val checkin: NotificationTrip<String>,
    val user: NotificationUser,
    val status: NotificationStatus
)

data class FollowRequestData(
    val user: NotificationUser
)

data class MastodonNotSentData(
    val status: NotificationStatus,
    val httpResponseCode: Int
)

data class UserMentionedData(
    val status: NotificationStatus,
    val creator: NotificationUser
)

data class YouHaveBeenCheckedInData(
    val status: NotificationStatus,
    @SerializedName("checkin") val checkIn: NotificationTrip<String>,
    val user: NotificationUser
)
