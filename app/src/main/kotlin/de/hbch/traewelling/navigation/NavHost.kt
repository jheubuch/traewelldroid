package de.hbch.traewelling.navigation

import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ShortcutManager
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.jcloquell.androidsecurestorage.SecureStorage
import de.hbch.traewelling.R
import de.hbch.traewelling.api.models.status.Status
import de.hbch.traewelling.shared.CheckInViewModel
import de.hbch.traewelling.shared.EventViewModel
import de.hbch.traewelling.shared.LoggedInUserViewModel
import de.hbch.traewelling.shared.SharedValues
import de.hbch.traewelling.ui.activeCheckins.EnRoute
import de.hbch.traewelling.ui.checkIn.CheckIn
import de.hbch.traewelling.ui.checkInResult.CheckInResultView
import de.hbch.traewelling.ui.dashboard.Dashboard
import de.hbch.traewelling.ui.followers.ManageFollowers
import de.hbch.traewelling.ui.info.InfoActivity
import de.hbch.traewelling.ui.main.MainActivity
import de.hbch.traewelling.ui.notifications.Notifications
import de.hbch.traewelling.ui.notifications.NotificationsViewModel
import de.hbch.traewelling.ui.searchConnection.SearchConnection
import de.hbch.traewelling.ui.selectDestination.SelectDestination
import de.hbch.traewelling.ui.settings.Settings
import de.hbch.traewelling.ui.statistics.DailyStatistics
import de.hbch.traewelling.ui.statistics.Statistics
import de.hbch.traewelling.ui.statusDetail.StatusDetail
import de.hbch.traewelling.ui.user.EditProfile
import de.hbch.traewelling.ui.user.Profile
import de.hbch.traewelling.ui.user.TrustedUsers
import de.hbch.traewelling.util.HOME
import de.hbch.traewelling.util.popBackStackAndNavigate
import de.hbch.traewelling.util.shareStatus
import de.hbch.traewelling.util.toShortcut
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Composable
fun TraewelldroidNavHost(
    navController: NavHostController,
    loggedInUserViewModel: LoggedInUserViewModel,
    eventViewModel: EventViewModel,
    checkInViewModel: CheckInViewModel,
    notificationsViewModel: NotificationsViewModel,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    onFloatingActionButtonChange: (Int, Int, () -> Unit) -> Unit = { _, _, _ -> },
    onResetFloatingActionButton: () -> Unit = { },
    onMenuChange: (List<ComposeMenuItem>) -> Unit = { },
    onNotificationCountChange: () -> Unit = { }
) {
    val context = LocalContext.current
    val secureStorage = SecureStorage(context)

    val navToSearchConnections: (Int, ZonedDateTime?) -> Unit = { station, date ->
        val formattedDate =
            if (date == null)
                ""
            else 
                DateTimeFormatter.ISO_DATE_TIME.format(date)

        navController.navigate(
            SearchConnection(station, formattedDate)
        )
    }
    val navToStatusDetails: (Int) -> Unit = { statusId ->
        navController.navigate(
            StatusDetails(statusId)
        )
    }
    val navToUserProfile: (String) -> Unit = { username ->
        navController.navigate(
            PersonalProfile(username)
        )
    }

    val navToEditCheckIn: (Status) -> Unit = {
        checkInViewModel.lineName = it.journey.line
        checkInViewModel.lineId = it.journey.lineId
        checkInViewModel.operatorCode = it.journey.operator?.id
        checkInViewModel.message.postValue(it.body)
        checkInViewModel.statusVisibility.postValue(it.visibility)
        checkInViewModel.statusBusiness.postValue(it.business)
        checkInViewModel.destination = it.journey.destination.name
        checkInViewModel.destinationStationId = it.journey.destination.id
        checkInViewModel.departureTime = it.journey.origin.departurePlanned
        checkInViewModel.manualDepartureTime = it.journey.departureManual
        checkInViewModel.arrivalTime = it.journey.destination.arrivalPlanned
        checkInViewModel.manualArrivalTime = it.journey.arrivalManual
        checkInViewModel.startStationId = it.journey.origin.id
        checkInViewModel.tripId = it.journey.hafasTripId
        checkInViewModel.editStatusId = it.id
        checkInViewModel.category = it.journey.safeProductType

        navController.navigate(
            CheckIn(true)
        )
    }

    val navToJoinConnection: (Status) -> Unit = { status ->
        checkInViewModel.lineName = status.journey.line
        checkInViewModel.operatorCode = status.journey.operator?.id
        checkInViewModel.lineId = status.journey.lineId
        checkInViewModel.tripId = status.journey.hafasTripId
        checkInViewModel.startStationId = status.journey.origin.id
        checkInViewModel.departureTime = status.journey.origin.departurePlanned
        checkInViewModel.destinationStationId = status.journey.destination.id
        checkInViewModel.arrivalTime = status.journey.destination.arrivalPlanned
        checkInViewModel.category = status.journey.safeProductType
        checkInViewModel.destination = status.journey.destination.name

        navController.navigate(
            CheckIn()
        )
    }

    NavHost(
        navController = navController,
        startDestination = Dashboard,
        modifier = modifier
    ) {
        composable<Dashboard> {
            var initialized by remember { mutableStateOf(false) }
            if (!initialized) {
                onMenuChange(listOf())
                initialized = true
            }

            Dashboard(
                loggedInUserViewModel = loggedInUserViewModel,
                searchConnectionsAction = navToSearchConnections,
                statusSelectedAction = navToStatusDetails,
                userSelectedAction = navToUserProfile,
                statusEditAction = navToEditCheckIn,
                joinConnection = navToJoinConnection
            )
            onResetFloatingActionButton()
        }

        composable<EnRoute> {
            var initialized by remember { mutableStateOf(false) }
            if (!initialized) {
                onMenuChange(listOf())
                initialized = true
            }
            EnRoute(
                loggedInUserViewModel = loggedInUserViewModel,
                statusSelectedAction = navToStatusDetails,
                userSelectedAction = navToUserProfile,
                statusEditAction = navToEditCheckIn,
                joinConnection = navToJoinConnection
            )
        }
        composable<Notifications> {
            var initialized by remember { mutableStateOf(false) }
            if (!initialized) {
                onMenuChange(listOf(
                    ComposeMenuItem(
                        R.string.mark_all_as_read,
                        R.drawable.ic_mark_all_as_read
                    ) {
                        notificationsViewModel.markAllAsRead {
                            onNotificationCountChange()
                            navController.popBackStack()
                            navController.navigate(Notifications) {
                                navController.graph.startDestinationRoute?.let { screenRoute ->
                                    popUpTo(screenRoute) {
                                        inclusive = true
                                    }
                                }
                                launchSingleTop = true
                            }
                        }
                    }
                ))
                initialized = true
            }
            Notifications(
                notificationsViewModel = notificationsViewModel,
                navHostController = navController,
                unreadNotificationsChanged = onNotificationCountChange
            )
        }
        composable<Statistics> {
            var initialized by remember { mutableStateOf(false) }
            if (!initialized) {
                onMenuChange(listOf())
                initialized = true
            }
            Statistics(
                modifier = Modifier.padding(bottom = 8.dp)
            )
            onResetFloatingActionButton()
        }
        composable<PersonalProfile>(
            deepLinks = PersonalProfile.deepLinks.toNavDeepLinks()
        ) {
            val profile: PersonalProfile = it.toRoute()
            var initialized by remember { mutableStateOf(false) }
            if (!initialized) {
                val menuItems = mutableListOf<ComposeMenuItem>()
                if (profile.username == null) {
                    menuItems.addAll(
                        listOf(
                            ComposeMenuItem(
                                R.string.information,
                                R.drawable.ic_privacy
                            ) {
                                context.startActivity(Intent(context, InfoActivity::class.java))
                            },
                            ComposeMenuItem(
                                R.string.settings,
                                R.drawable.ic_settings
                            ) {
                                navController.navigate(Settings) {
                                    launchSingleTop = true
                                }
                            }
                        )
                    )
                }
                onMenuChange(menuItems)
                initialized = true
            }

            Profile(
                username = profile.username,
                loggedInUserViewModel = loggedInUserViewModel,
                stationSelectedAction = navToSearchConnections,
                statusSelectedAction = navToStatusDetails,
                statusEditAction = navToEditCheckIn,
                dailyStatisticsSelectedAction = { date ->
                    val formatted = DateTimeFormatter.ISO_DATE.format(date)
                    navController.navigate(DailyStatistics(formatted))
                },
                userSelectedAction = navToUserProfile,
                joinConnection = navToJoinConnection,
                editProfile = {
                    navController.navigate(ProfileEdit) {
                        launchSingleTop = true
                    }
                },
                manageFollowerAction = {
                    navController.navigate(ManageFollowers()) {
                        launchSingleTop = true
                    }
                }
            )

            onResetFloatingActionButton()
        }
        composable<ProfileEdit> {
            EditProfile(
                snackbarHostState = snackbarHostState,
                manageTrustedUsers = {
                    navController.navigate(TrustedUsers) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable<ManageFollowers>(deepLinks = ManageFollowers.deepLinks.toNavDeepLinks()) {
            val data: ManageFollowers = it.toRoute()
            ManageFollowers(
                snackbarHostState = snackbarHostState,
                showFollowRequests = data.followRequests
            )
        }
        composable<TrustedUsers> {
            TrustedUsers()
        }
        composable<DailyStatistics>(
            deepLinks = DailyStatistics.deepLinks.toNavDeepLinks()
        ) {
            var initialized by remember { mutableStateOf(false) }
            if (!initialized) {
                onMenuChange(listOf())
                initialized = true
            }
            val data: DailyStatistics = it.toRoute()
            val date = data.date
            var localDate = LocalDate.now()
            if (date.isNotEmpty()) {
                localDate = LocalDate.from(DateTimeFormatter.ISO_DATE.parse(date))
            }
            DailyStatistics(
                date = localDate,
                loggedInUserViewModel = loggedInUserViewModel,
                statusSelectedAction = navToStatusDetails,
                statusEditAction = navToEditCheckIn
            )
            onResetFloatingActionButton()
        }
        composable<Settings> {
            var initialized by remember { mutableStateOf(false) }
            if (!initialized) {
                onMenuChange(listOf())
                initialized = true
            }
            Settings(
                snackbarHostState = snackbarHostState,
                loggedInUserViewModel = loggedInUserViewModel,
                emojiPackItemAdapter = (context as? MainActivity)?.emojiPackItemAdapter
            )
            onResetFloatingActionButton()
        }
        composable<StatusDetails>(
            deepLinks = StatusDetails.deepLinks.toNavDeepLinks()
        ) {
            val statusDetails: StatusDetails = it.toRoute()
            val statusId = statusDetails.statusId
            if (statusId == 0) {
                navController.popBackStack()
                return@composable
            }
            var initialized by remember { mutableStateOf(false) }
            if (!initialized) {
                onMenuChange(listOf())
                initialized = true
            }

            StatusDetail(
                statusId = statusId,
                joinConnection = navToJoinConnection,
                loggedInUserViewModel = loggedInUserViewModel,
                statusLoaded = { status ->
                    val menuItems = mutableListOf<ComposeMenuItem>()
                    if (loggedInUserViewModel.loggedInUser.value != null) {
                        if (status.user.id == loggedInUserViewModel.loggedInUser.value?.id) {
                            menuItems.add(
                                ComposeMenuItem(
                                    R.string.title_share,
                                    R.drawable.ic_share
                                ) {
                                    context.shareStatus(status)
                                }
                            )
                        } else {
                            menuItems.add(
                                ComposeMenuItem(
                                    R.string.title_also_check_in,
                                    R.drawable.ic_also_check_in
                                ) {
                                    navToJoinConnection(status)
                                }
                            )
                        }
                    }
                    onMenuChange(menuItems)
                    onResetFloatingActionButton()
                },
                statusEdit = navToEditCheckIn,
                statusDeleted = {
                    navController.popBackStack()
                },
                userSelected = navToUserProfile
            )
        }
        composable<SearchConnection>(
            deepLinks = SearchConnection.deepLinks.toNavDeepLinks()
        ) {
            val data: SearchConnection = it.toRoute()
            // if specific date is passed, take it. if not, search from now -5min
            var zonedDateTime = ZonedDateTime.now().minusMinutes(5)
            val searchDate = data.date
            if (!searchDate.isNullOrEmpty()) {
                zonedDateTime = ZonedDateTime.from(DateTimeFormatter.ISO_DATE_TIME.parse(searchDate))
            }

            var initialized by remember { mutableStateOf(false) }
            if (!initialized) {
                onMenuChange(listOf())
                initialized = true
            }

            SearchConnection(
                loggedInUserViewModel = loggedInUserViewModel,
                station = data.station,
                currentSearchDate = zonedDateTime,
                checkInViewModel = checkInViewModel,
                onTripSelected = {
                    navController.navigate(
                        SelectDestination(false)
                    )
                },
                onHomelandSelected = { station ->
                    val shortcutManager: ShortcutManager?
                        = context.getSystemService(ShortcutManager::class.java)
                    if (shortcutManager != null) {
                        val shortcut = station.toShortcut(context, HOME, true)
                        val intent = shortcutManager.createShortcutResultIntent(shortcut)
                        val successCallback = PendingIntent.getBroadcast(
                            context,
                            0,
                            intent,
                            PendingIntent.FLAG_IMMUTABLE
                        )
                        shortcutManager.requestPinShortcut(shortcut, successCallback.intentSender)
                    }
                }
            )
            onResetFloatingActionButton()
        }
        composable<SelectDestination> {
            val data: SelectDestination = it.toRoute()
            val editMode = data.editMode
            var initialized by remember { mutableStateOf(false) }
            if (!initialized) {
                onMenuChange(listOf())
                initialized = true
            }
            SelectDestination(
                checkInViewModel = checkInViewModel,
                onStationSelected = {
                    navController.navigate(
                        CheckIn(editMode)
                    ) {
                        launchSingleTop = true
                    }
                }
            )
            onResetFloatingActionButton()
        }
        composable<CheckIn> {
            val data: CheckIn = it.toRoute()
            val editMode = data.editMode
            val initText =
                if (editMode) {
                    checkInViewModel.message.value ?: ""
                } else {
                    // Only set default visibility when a new check-in is created!
                    checkInViewModel.statusVisibility.postValue(
                        loggedInUserViewModel.defaultStatusVisibility
                    )

                    val hashtag = secureStorage.getObject(SharedValues.SS_HASHTAG, String::class.java)
                    if (hashtag == null || hashtag == "")
                        ""
                    else
                        "\n#$hashtag"
                }
            var initialized by remember { mutableStateOf(false) }
            if (!initialized) {
                onMenuChange(listOf())
                initialized = true
            }

            val coroutineScope = rememberCoroutineScope()

            CheckIn(
                checkInViewModel = checkInViewModel,
                loggedInUserViewModel = loggedInUserViewModel,
                eventViewModel = eventViewModel,
                initText = initText,
                checkInAction = { trwl, travelynx ->
                    if (editMode) {
                        checkInViewModel.updateCheckIn { status ->
                            navController.navigate(
                                StatusDetails(status.id)
                            ) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        }
                    } else {
                        val checkInCount = secureStorage.getObject(SharedValues.SS_CHECK_IN_COUNT, Long::class.java) ?: 0L

                        coroutineScope.launch {
                            checkInViewModel.checkIn(trwl, travelynx) { succeeded ->
                                navController.navigate(
                                    CheckInResult
                                ) {
                                    if (succeeded) {
                                        secureStorage.storeObject(
                                            SharedValues.SS_CHECK_IN_COUNT,
                                            checkInCount + 1
                                        )
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            inclusive = false
                                        }
                                    }

                                    launchSingleTop = true
                                }
                            }
                        }
                    }
                },
                isEditMode = editMode,
                changeDestinationAction = {
                    navController.navigate(
                        SelectDestination(true)
                    ) {
                        launchSingleTop = true
                    }
                }
            )
            onResetFloatingActionButton()
        }
        composable<CheckInResult> {
            var initialized by remember { mutableStateOf(false) }
            if (!initialized) {
                onMenuChange(listOf())
                initialized = true
            }
            val coroutineScope = rememberCoroutineScope()

            CheckInResultView(
                checkInViewModel = checkInViewModel,
                loggedInUserViewModel = loggedInUserViewModel,
                onStatusSelected = navToStatusDetails,
                onFloatingActionButtonChange = { icon, label ->
                    onFloatingActionButtonChange(icon, label) {
                        checkInViewModel.reset()
                        navController.popBackStackAndNavigate(Dashboard)
                    }
                },
                onCheckInForced = {
                    coroutineScope.launch {
                        checkInViewModel.forceCheckIn {
                            navController.navigate(
                                CheckInResult
                            ) {
                                launchSingleTop = true
                            }
                        }
                    }
                }
            )
        }
    }
}
