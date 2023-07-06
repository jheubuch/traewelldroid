package de.hbch.traewelling.navigation

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import de.hbch.traewelling.R

interface Destination {
    val label: Int
    val route: String
}

interface ArgumentDestination : Destination {
    val arguments: List<NamedNavArgument>
}

interface MainDestination : Destination {
    val icon: Int
}

object Dashboard : MainDestination {
    override val icon = R.drawable.ic_dashboard
    override val label = R.string.title_dashboard
    override val route = "dashboard"
}

object EnRoute : MainDestination {
    override val icon = R.drawable.ic_train
    override val label = R.string.title_active_checkins
    override val route = "en-route"
}

object Statistics : MainDestination {
    override val icon = R.drawable.ic_statistics
    override val label = R.string.title_statistics
    override val route = "statistics"
}

object PersonalProfile : MainDestination {
    override val icon = R.drawable.ic_account
    override val label = R.string.title_user
    override val route = "personal-profile"
}

object SearchConnection : Destination {
    override val label = R.string.title_search_connection
    override val route = "search-connection"
}

object SelectDestination : Destination {
    override val label = R.string.title_select_destination
    override val route = "select-destination"
}

object CheckIn : Destination {
    override val label = R.string.check_in
    override val route = "check-in"
}

object StatusDetails : ArgumentDestination {
    override val label = R.string.status_details
    override val route = "status-details/{statusId}"
    override val arguments = listOf(
        navArgument("statusId") {
            type = NavType.IntType
        }
    )
}

object Settings : Destination {
    override val label = R.string.settings
    override val route = "settings"
}

val SCREENS = listOf(
    Dashboard,
    EnRoute,
    Statistics,
    PersonalProfile,
    SearchConnection,
    SelectDestination,
    CheckIn,
    StatusDetails,
    Settings
)

val BOTTOM_NAVIGATION = listOf(
    Dashboard,
    EnRoute,
    Statistics,
    PersonalProfile
)
