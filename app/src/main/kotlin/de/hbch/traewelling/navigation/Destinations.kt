package de.hbch.traewelling.navigation

import androidx.navigation.NavDeepLink
import androidx.navigation.navDeepLink
import de.hbch.traewelling.R
import kotlinx.serialization.Serializable

const val TRWL_BASE_URI = "https://traewelling.de"
const val TRAEWELLDROID_BASE_URI = "traewelldroid://app.traewelldroid.de"

interface Destination {
    val label: Int
    val route: String
}

interface MainDestination : Destination {
    val icon: Int
}

@Serializable
object Dashboard : MainDestination {
    override val icon = R.drawable.ic_dashboard
    override val label = R.string.title_dashboard
    override val route = "dashboard"
}

@Serializable
object EnRoute : MainDestination {
    override val icon = R.drawable.ic_train
    override val label = R.string.title_active_checkins
    override val route = "en-route"
}

@Serializable
object Notifications : MainDestination {
    override val icon = R.drawable.ic_notification
    override val label = R.string.title_notifications
    override val route = "notifications"
}

@Serializable
object Statistics : MainDestination {
    override val icon = R.drawable.ic_statistics
    override val label = R.string.title_statistics
    override val route = "statistics"
}

fun List<String>.toNavDeepLinks(): List<NavDeepLink> {
    return this.map {
        navDeepLink {
            uriPattern = it
        }
    }
}

@Serializable
data class PersonalProfile(
    val username: String? = null,
    val isPrivateProfile: Boolean = false,
    val isFollowing: Boolean = false
) : MainDestination {
    override val icon = R.drawable.ic_account
    override val label = R.string.title_user
    override val route = "personal-profile/?username={username}"
    companion object {
        val deepLinks = listOf(
            "$TRWL_BASE_URI/@{username}",
            "$TRAEWELLDROID_BASE_URI/@{username}"
        )
    }
}

@Serializable
data class DailyStatistics(
    val date: String
): Destination {
    override val route = "daily-statistics/{date}"
    override val label = R.string.daily_overview
    companion object {
        val deepLinks = listOf(
            "$TRWL_BASE_URI/stats/daily/{date}",
            "$TRAEWELLDROID_BASE_URI/stats/daily/{date}"
        )
    }
}

@Serializable
data class SearchConnection(
    val station: Int,
    val date: String? = null
) : Destination {
    override val label = R.string.title_search_connection
    override val route = "search-connection/?station={station}&date={date}"
    companion object {
        val deepLinks = listOf(
            "$TRWL_BASE_URI/trains/stationboard?station={station}",
            "$TRAEWELLDROID_BASE_URI/trains/stationboard?station={station}"
        )
    }
}

@Serializable
data class SelectDestination(
    val editMode: Boolean = false
) : Destination {
    override val label = R.string.title_select_destination
    override val route = "select-destination/?editMode={editMode}"
}

@Serializable
data class CheckIn(
    val editMode: Boolean = false
) : Destination {
    override val label = R.string.check_in
    override val route = "check-in/?editMode={editMode}"
}

@Serializable
object CheckInResult: Destination {
    override val label = R.string.check_in
    override val route = "check-in-result"
}

@Serializable
data class StatusDetails(
    val statusId: Int
) : Destination {
    override val label = R.string.status_details
    override val route = "status-details/{statusId}"
    companion object {
        val deepLinks = listOf(
            "$TRWL_BASE_URI/status/{statusId}",
            "$TRAEWELLDROID_BASE_URI/status/{statusId}"
        )
    }
}

@Serializable
object Settings : Destination {
    override val label = R.string.settings
    override val route = "settings"
}

@Serializable
object ProfileEdit : Destination {
    override val label = R.string.edit_profile
    override val route = "edit-profile"
}

@Serializable
data class ManageFollowers(
    val followRequests: Boolean = false
): Destination {
    override val label = R.string.manage_followers
    override val route = "manage-followers/?followRequests={followRequests}"
    companion object {
        val deepLinks = listOf(
            "$TRAEWELLDROID_BASE_URI/manage-followers/?followRequests={followRequests}"
        )
    }
}

@Serializable
object TrustedUsers : Destination {
    override val label = R.string.trusted
    override val route = "trusted-users"
}

val SCREENS = listOf(
    Dashboard,
    EnRoute,
    Notifications,
    Statistics,
    PersonalProfile(),
    DailyStatistics(""),
    SearchConnection(0),
    SelectDestination(),
    CheckIn(),
    CheckInResult,
    StatusDetails(0),
    Settings,
    ProfileEdit,
    ManageFollowers(),
    ProfileEdit,
    TrustedUsers
)

val BOTTOM_NAVIGATION = listOf(
    Dashboard,
    EnRoute,
    Notifications,
    Statistics,
    PersonalProfile()
)

data class ComposeMenuItem(
    val label: Int,
    val icon: Int,
    val badge: String? = null,
    val onClick: () -> Unit = { }
)
