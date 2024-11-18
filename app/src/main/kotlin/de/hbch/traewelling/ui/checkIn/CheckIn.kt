package de.hbch.traewelling.ui.checkIn

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.canopas.lib.showcase.IntroShowcase
import com.canopas.lib.showcase.component.ShowcaseStyle
import com.canopas.lib.showcase.component.rememberIntroShowcaseState
import com.jcloquell.androidsecurestorage.SecureStorage
import de.hbch.traewelling.R
import de.hbch.traewelling.api.models.event.Event
import de.hbch.traewelling.api.models.mastodon.CustomEmoji
import de.hbch.traewelling.api.models.status.StatusBusiness
import de.hbch.traewelling.api.models.status.StatusVisibility
import de.hbch.traewelling.api.models.user.TrustedUser
import de.hbch.traewelling.api.models.user.User
import de.hbch.traewelling.shared.BottomSearchViewModel
import de.hbch.traewelling.shared.CheckInViewModel
import de.hbch.traewelling.shared.EventViewModel
import de.hbch.traewelling.shared.LoggedInUserViewModel
import de.hbch.traewelling.shared.MastodonEmojis
import de.hbch.traewelling.shared.SharedValues
import de.hbch.traewelling.theme.LocalColorScheme
import de.hbch.traewelling.theme.LocalFont
import de.hbch.traewelling.theme.MainTheme
import de.hbch.traewelling.ui.composables.ButtonWithIconAndText
import de.hbch.traewelling.ui.composables.DataLoading
import de.hbch.traewelling.ui.composables.DateTimeSelection
import de.hbch.traewelling.ui.composables.ContentDialog
import de.hbch.traewelling.ui.composables.OutlinedButtonWithIconAndText
import de.hbch.traewelling.ui.composables.ProfilePicture
import de.hbch.traewelling.ui.composables.SwitchWithIconAndText
import de.hbch.traewelling.ui.selectDestination.FromToTextRow
import de.hbch.traewelling.ui.user.TrustedUsersViewModel
import de.hbch.traewelling.util.checkAnyUsernames
import de.hbch.traewelling.util.checkCustomEmojis
import de.hbch.traewelling.util.getLocalDateString
import de.hbch.traewelling.util.useDebounce
import kotlinx.coroutines.launch
import java.net.URL
import java.time.ZonedDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckIn(
    modifier: Modifier = Modifier,
    checkInViewModel: CheckInViewModel,
    loggedInUserViewModel: LoggedInUserViewModel,
    eventViewModel: EventViewModel,
    checkInAction: (Boolean, Boolean) -> Unit = { _, _ -> },
    initText: String = "",
    isEditMode: Boolean = false,
    changeDestinationAction: () -> Unit = { }
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val secureStorage = remember { SecureStorage(context) }
    val loggedInUser by loggedInUserViewModel.loggedInUser.observeAsState()
    var introduceEmoji by remember { mutableStateOf(
        !(secureStorage.getObject(SharedValues.SS_EMOJI_SHOWCASE, Boolean::class.java) ?: false) &&
        loggedInUser?.mastodonUrl != null
    ) }
    var introduceCoTravels by remember {
        mutableStateOf(!(secureStorage.getObject(SharedValues.SS_CO_TRAVELLER_SHOWCASE, Boolean::class.java) ?: false))
    }
    val introShowcaseState = rememberIntroShowcaseState()

    val mastodonEmojis = remember { MastodonEmojis.getInstance(context) }
    val instanceEmojis by remember { derivedStateOf {
        if (loggedInUser?.mastodonUrl?.isNotBlank() == true) {
            mastodonEmojis.emojis[URL(loggedInUser?.mastodonUrl).host] ?: listOf()
        } else {
            listOf()
        }
    } }
    val bottomSearchViewModel: BottomSearchViewModel = viewModel()

    var enableTrwlCheckIn by rememberSaveable { mutableStateOf(secureStorage.getObject(SharedValues.SS_TRWL_AUTO_LOGIN, Boolean::class.java) ?: true) }
    val travelynxConfigured = secureStorage.getObject(SharedValues.SS_TRAVELYNX_TOKEN, String::class.java)?.isNotBlank() ?: false
    var enableTravelynxCheckIn by rememberSaveable { mutableStateOf(secureStorage.getObject(SharedValues.SS_TRAVELYNX_AUTO_CHECKIN, Boolean::class.java) ?: false) }

    var businessSelectionVisible by remember { mutableStateOf(false) }
    var visibilitySelectionVisible by remember { mutableStateOf(false) }
    var eventSelectionVisible by remember { mutableStateOf(false) }
    var coTravellerSelectionVisible by remember { mutableStateOf(false) }

    var statusText by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue(text = initText)) }
    val userSearchQuery by remember { derivedStateOf {
        val matches = statusText.text.checkAnyUsernames()
        matches.firstOrNull { it.range.contains(statusText.selection.min - 1) || it.range.contains(statusText.selection.max + 1) }?.value?.replace("@", "")
    } }
    val customEmojiQuery by remember { derivedStateOf {
        val matches = statusText.text.checkCustomEmojis()
        matches.firstOrNull { it.range.contains(statusText.selection.min - 1) || it.range.contains(statusText.selection.max + 1) }?.value?.replace(":", "")
    } }

    val userResults = remember { mutableStateListOf<User>() }
    val customEmojiResults = remember { mutableStateListOf<CustomEmoji>() }
    var usersQuerying by remember { mutableStateOf(false) }
    var displayUserResults by remember { mutableStateOf(false) }
    userSearchQuery.useDebounce(
        onChange = { query ->
            if (query == null) {
                usersQuerying = false
                displayUserResults = false
            } else {
                usersQuerying = true
                displayUserResults = true
                coroutineScope.launch {
                    val users = bottomSearchViewModel.searchUsers(query)
                    usersQuerying = false
                    userResults.clear()
                    userResults.addAll(users)
                }
            }
        },
        delayMillis = 500L
    )
    customEmojiQuery.useDebounce(
        onChange = { query ->
            if (query == null) {
                customEmojiResults.clear()
            } else {
                customEmojiResults.clear()
                coroutineScope.launch {
                    customEmojiResults.addAll(instanceEmojis.filter { it.shortcode.contains(query, ignoreCase = true) })
                }
            }
        },
        delayMillis = 250L
    )

    val selectedVisibility by checkInViewModel.statusVisibility.observeAsState()
    val selectedBusiness by checkInViewModel.statusBusiness.observeAsState()
    val activeEvents by eventViewModel.activeEvents.observeAsState()
    val selectedEvent by checkInViewModel.event.observeAsState()
    val selectedCoTravellers by checkInViewModel.coTravellers.observeAsState()
    val dialogModifier = Modifier.fillMaxWidth(0.99f)

    if (businessSelectionVisible) {
        ContentDialog(
            modifier = dialogModifier,
            onDismissRequest = {
                businessSelectionVisible = false
            }
        ) {
            SelectStatusBusinessDialog(
                businessSelectedAction = {
                    businessSelectionVisible = false
                    checkInViewModel.statusBusiness.postValue(it)
                }
            )
        }
    }

    if (visibilitySelectionVisible) {
        ContentDialog(
            modifier = dialogModifier,
            onDismissRequest = {
                visibilitySelectionVisible = false
            }
        ) {
            SelectStatusVisibilityDialog(
                visibilitySelectedAction = {
                    visibilitySelectionVisible = false
                    checkInViewModel.statusVisibility.postValue(it)
                }
            )
        }
    }

    if (eventSelectionVisible && activeEvents !== null) {
        ContentDialog(
            modifier = dialogModifier,
            onDismissRequest = {
                eventSelectionVisible = false
            }
        ) {
            SelectEventDialog(
                activeEvents = activeEvents!!,
                eventSelectedAction = {
                    checkInViewModel.event.postValue(it)
                    eventSelectionVisible = false
                }
            )
        }
    }

    if (coTravellerSelectionVisible) {
        ContentDialog(
            modifier = dialogModifier,
            onDismissRequest = {
                coTravellerSelectionVisible = false
                checkInViewModel.coTravellers.postValue(listOf())
            }
        ) {
            SelectCoTravellers(
                onUsersSelected = {
                    checkInViewModel.coTravellers.postValue(it)
                    coTravellerSelectionVisible = false
                }
            )
        }
    }

    IntroShowcase(
        showIntroShowCase = introduceEmoji || introduceCoTravels,
        onShowCaseCompleted = {
            introduceEmoji = false
            introduceCoTravels = false
            secureStorage.storeObject(SharedValues.SS_EMOJI_SHOWCASE, true)
            secureStorage.storeObject(SharedValues.SS_CO_TRAVELLER_SHOWCASE, true)
        },
        state = introShowcaseState,
        dismissOnClickOutside = true
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            ElevatedCard(
                modifier = modifier
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    FromToTextRow(
                        modifier = Modifier.fillMaxWidth(),
                        category = checkInViewModel.category,
                        lineName = checkInViewModel.lineName,
                        lineId = checkInViewModel.lineId,
                        operatorCode = checkInViewModel.operatorCode,
                        destination = checkInViewModel.destination
                    )

                    // Text field
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        var textFieldModifier: Modifier = Modifier
                        if (introduceEmoji) {
                            textFieldModifier = Modifier
                                .introShowCaseTarget(
                                    index = 0,
                                    style = ShowcaseStyle.Default.copy(
                                        backgroundColor = LocalColorScheme.current.primary,
                                        backgroundAlpha = 0.95f,
                                        targetCircleColor = LocalColorScheme.current.onPrimary
                                    )
                                ) {
                                    Column {
                                        Text(
                                            text = stringResource(id = R.string.mastodon_emoji),
                                            style = LocalFont.current.titleLarge,
                                            color = LocalColorScheme.current.onPrimary
                                        )
                                        Text(
                                            text = stringResource(id = R.string.mastodon_emoji_description),
                                            color = LocalColorScheme.current.onPrimary
                                        )
                                    }
                                }
                        }
                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(
                                    min = 72.dp,
                                    max = Dp.Unspecified
                                ),
                            value = statusText,
                            onValueChange = {
                                if (it.text.count() > 280)
                                    return@OutlinedTextField
                                statusText = it
                                checkInViewModel.message.postValue(it.text)
                            },
                            label = {
                                Text(
                                    text = stringResource(id = R.string.status_message),
                                    modifier = textFieldModifier
                                )
                            }
                        )
                        Text(
                            modifier = Modifier.padding(4.dp),
                            text = "${statusText.text.count()}/280",
                            style = LocalFont.current.labelSmall
                        )
                        AnimatedVisibility(displayUserResults) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (usersQuerying) {
                                    Text(
                                        text = stringResource(id = R.string.data_loading)
                                    )
                                } else {
                                    if (userResults.isEmpty()) {
                                        Text(
                                            text = stringResource(id = R.string.no_results_found)
                                        )
                                    } else {
                                        userResults.forEach {
                                            val username = "@${it.username}"
                                            AssistChip(
                                                onClick = {
                                                    val firstMatch =
                                                        statusText.text.checkAnyUsernames()
                                                            .first {
                                                                it.range.contains(statusText.selection.min - 1) || it.range.contains(
                                                                    statusText.selection.max + 1
                                                                )
                                                            }
                                                    statusText = statusText.copy(
                                                        text = statusText.text.replaceRange(
                                                            firstMatch.range.first,
                                                            firstMatch.range.last + 1,
                                                            "@${it.username} "
                                                        ),
                                                        selection = TextRange(firstMatch.range.first + it.username.length + 2)
                                                    )
                                                },
                                                label = {
                                                    Text(
                                                        text = username
                                                    )
                                                },
                                                leadingIcon = {
                                                    ProfilePicture(
                                                        user = it,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        AnimatedVisibility(customEmojiResults.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                customEmojiResults.forEach { emoji ->
                                    AssistChip(
                                        onClick = {
                                            val firstMatch = statusText.text.checkCustomEmojis()
                                                .first {
                                                    it.range.contains(statusText.selection.min - 1) || it.range.contains(
                                                        statusText.selection.max + 1
                                                    )
                                                }
                                            statusText = statusText.copy(
                                                text = statusText.text.replaceRange(
                                                    firstMatch.range.first,
                                                    firstMatch.range.last + 1,
                                                    ":${emoji.shortcode}: "
                                                ),
                                                selection = TextRange(firstMatch.range.first + emoji.shortcode.length + 3)
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = emoji.shortcode
                                            )
                                        },
                                        leadingIcon = {
                                            AsyncImage(
                                                model = emoji.url,
                                                contentDescription = emoji.shortcode,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (travelynxConfigured && !isEditMode) {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                SwitchWithIconAndText(
                                    checked = enableTrwlCheckIn,
                                    onCheckedChange = {
                                        enableTrwlCheckIn = it
                                    },
                                    drawableId = R.drawable.ic_trwl,
                                    stringId = R.string.check_in_trwl
                                )
                                SwitchWithIconAndText(
                                    checked = enableTravelynxCheckIn,
                                    onCheckedChange = {
                                        enableTravelynxCheckIn = it
                                    },
                                    drawableId = R.drawable.ic_travelynx,
                                    stringId = R.string.check_in_travelynx
                                )
                            }
                        }
                    }

                    AnimatedVisibility(enableTrwlCheckIn) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            // Co-travellers
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                var coTravellerButtonModifier = Modifier.fillMaxWidth()
                                if (introduceCoTravels) {
                                    coTravellerButtonModifier = coTravellerButtonModifier
                                        .introShowCaseTarget(
                                            index = 1,
                                            style = ShowcaseStyle.Default.copy(
                                                backgroundColor = LocalColorScheme.current.primary,
                                                backgroundAlpha = 0.95f,
                                                targetCircleColor = LocalColorScheme.current.onPrimary
                                            )
                                        ) {
                                            Column {
                                                Text(
                                                    text = stringResource(id = R.string.select_co_travellers),
                                                    style = LocalFont.current.titleLarge,
                                                    color = LocalColorScheme.current.onPrimary
                                                )
                                                Text(
                                                    text = stringResource(id = R.string.select_co_travellers_description),
                                                    color = LocalColorScheme.current.onPrimary
                                                )
                                                Text(
                                                    text = stringResource(id = R.string.only_check_in_persons),
                                                    color = LocalColorScheme.current.onPrimary,
                                                    style = LocalFont.current.labelMedium
                                                )
                                            }
                                        }
                                }
                                OutlinedButtonWithIconAndText(
                                    stringId = R.string.select_co_travellers,
                                    drawableId = R.drawable.ic_also_check_in,
                                    onClick = {
                                        coTravellerSelectionVisible = true
                                    },
                                    modifier = coTravellerButtonModifier
                                )
                                if (selectedCoTravellers?.isNotEmpty() == true) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AssistChip(
                                            onClick = {
                                                checkInViewModel.coTravellers.postValue(listOf())
                                            },
                                            label = {
                                                Text(
                                                    text = stringResource(id = R.string.remove)
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.ic_remove),
                                                    contentDescription = null
                                                )
                                            }
                                        )
                                        selectedCoTravellers?.forEach {
                                            AssistChip(
                                                onClick = {
                                                    coTravellerSelectionVisible = true
                                                },
                                                label = {
                                                    Text(
                                                        text = "@${it.user.username}"
                                                    )
                                                },
                                                leadingIcon = {
                                                    ProfilePicture(
                                                        name = it.user.name,
                                                        url = it.user.avatarUrl,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            // Option buttons
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val optionButtonModifier = Modifier
                                    .weight(1f)

                                if (selectedVisibility != null) {
                                    OutlinedButtonWithIconAndText(
                                        modifier = optionButtonModifier,
                                        stringId = selectedVisibility!!.title,
                                        drawableId = selectedVisibility!!.icon,
                                        onClick = {
                                            visibilitySelectionVisible = true
                                        }
                                    )
                                }
                                if (selectedBusiness != null) {
                                    OutlinedButtonWithIconAndText(
                                        modifier = optionButtonModifier,
                                        stringId = selectedBusiness!!.title,
                                        drawableId = selectedBusiness!!.icon,
                                        onClick = {
                                            businessSelectionVisible = true
                                        }
                                    )
                                }
                            }

                            // Event button
                            if (!isEditMode && activeEvents?.isNotEmpty() == true) {
                                OutlinedButtonWithIconAndText(
                                    modifier = Modifier.fillMaxWidth(),
                                    drawableId = if (selectedEvent == null)
                                        R.drawable.ic_calendar
                                    else
                                        R.drawable.ic_calendar_checked,
                                    text = selectedEvent?.name
                                        ?: stringResource(id = R.string.title_select_event),
                                    onClick = {
                                        eventSelectionVisible = true
                                    }
                                )
                            }

                            // Share options
                            if (!isEditMode && !loggedInUser?.mastodonUrl.isNullOrEmpty()) {
                                ShareOptions(
                                    modifier = Modifier.fillMaxWidth(),
                                    checkInViewModel = checkInViewModel
                                )
                            }
                        }
                    }

                    // Manual time overwrites
                    if (isEditMode) {
                        val currentDateTime = ZonedDateTime.now()
                        val plannedDeparture = checkInViewModel.departureTime
                        if (plannedDeparture != null && currentDateTime.isAfter(plannedDeparture)) {
                            DateTimeSelection(
                                initDate = checkInViewModel.manualDepartureTime,
                                plannedDate = checkInViewModel.departureTime,
                                label = R.string.manual_departure,
                                modifier = Modifier.fillMaxWidth(),
                                dateSelected = { checkInViewModel.manualDepartureTime = it }
                            )
                        }
                        val plannedArrival = checkInViewModel.arrivalTime
                        if (plannedArrival != null && currentDateTime.isAfter(plannedArrival)) {
                            DateTimeSelection(
                                initDate = checkInViewModel.manualArrivalTime,
                                plannedDate = checkInViewModel.arrivalTime,
                                label = R.string.manual_arrival,
                                modifier = Modifier.fillMaxWidth(),
                                dateSelected = { checkInViewModel.manualArrivalTime = it }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (isEditMode) {
                            ButtonWithIconAndText(
                                stringId = R.string.change_destination,
                                drawableId = R.drawable.ic_edit,
                                onClick = changeDestinationAction
                            )
                        } else {
                            Box {}
                        }
                        var isCheckingIn by remember { mutableStateOf(false) }
                        ButtonWithIconAndText(
                            stringId = if (isEditMode) R.string.save else R.string.check_in,
                            drawableId = R.drawable.ic_check_in,
                            onClick = {
                                checkInViewModel.message.value = statusText.text
                                checkInAction(
                                    enableTrwlCheckIn,
                                    (travelynxConfigured && enableTravelynxCheckIn)
                                )
                                isCheckingIn = true
                            },
                            isLoading = isCheckingIn,
                            isEnabled = (enableTrwlCheckIn || (travelynxConfigured && enableTravelynxCheckIn))
                        )
                    }
                }
            }
            Box(modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun SelectStatusVisibilityDialog(
    visibilitySelectedAction: (StatusVisibility) -> Unit = { }
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            modifier = Modifier.padding(bottom = 8.dp),
            text = stringResource(id = R.string.title_select_visibility),
            style = LocalFont.current.titleLarge,
            color = LocalColorScheme.current.primary
        )
        StatusVisibility.entries.forEach { visibility ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        visibilitySelectedAction(visibility)
                    }
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = visibility.icon),
                    contentDescription = null
                )
                Text(
                    text = stringResource(id = visibility.title),
                    style = LocalFont.current.titleLarge
                )
            }
        }
    }
}

@Composable
private fun SelectStatusBusinessDialog(
    businessSelectedAction: (StatusBusiness) -> Unit = { }
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            text = stringResource(id = R.string.title_select_business),
            style = LocalFont.current.titleLarge,
            color = LocalColorScheme.current.primary
        )
        StatusBusiness.entries.forEach { business ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        businessSelectedAction(business)
                    }
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = business.icon),
                    contentDescription = null
                )
                Text(
                    text = stringResource(id = business.title),
                    style = LocalFont.current.titleLarge
                )
            }
        }
    }
}

@Composable
private fun SelectEventDialog(
    activeEvents: List<Event?>,
    eventSelectedAction: (Event?) -> Unit = { }
) {
    val events = mutableListOf<Event?>(null)
    events.addAll(activeEvents)
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            text = stringResource(id = R.string.title_select_event),
            style = LocalFont.current.titleLarge,
            color = LocalColorScheme.current.primary
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            text = stringResource(id = R.string.hint_event_missing),
            style = LocalFont.current.labelLarge
        )
        events.forEach { event ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        eventSelectedAction(event)
                    }
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Icon(
                        painter = painterResource(id =
                            if (event == null)
                                R.drawable.ic_remove
                            else
                                R.drawable.ic_calendar
                        ),
                        contentDescription = null
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                ) {
                    Text(
                        text = event?.name ?: stringResource(id = R.string.reset_selection),
                        style = LocalFont.current.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (event == null) {
                            stringResource(R.string.no_event_check_in)
                        } else {
                            stringResource(
                                id = R.string.date_range,
                                getLocalDateString(event.begin),
                                getLocalDateString(event.end)
                            )
                        },
                        style = LocalFont.current.titleSmall
                    )
                }
                Icon(
                    painter = painterResource(id = R.drawable.ic_select),
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
private fun ShareOptions(
    modifier: Modifier = Modifier,
    checkInViewModel: CheckInViewModel
) {
    val shareOnMastodon by checkInViewModel.toot.observeAsState(false)
    val chainShareOnMastodon by checkInViewModel.chainToot.observeAsState(false)
    var toot by remember { mutableStateOf(shareOnMastodon) }
    var chainToot by remember { mutableStateOf(chainShareOnMastodon) }

    val chainTootAction: (Boolean) -> Unit = {
        chainToot = it
        checkInViewModel.chainToot.postValue(it)
    }
    val tootAction: (Boolean) -> Unit = {
        toot = it
        checkInViewModel.toot.postValue(it)

        if (!it) {
            chainTootAction(false)
        }
    }

    Column(
        modifier = modifier
    ) {
        SwitchWithIconAndText(
            modifier = Modifier.fillMaxWidth(),
            checked = toot,
            onCheckedChange = {
                tootAction(it)
            },
            drawableId = R.drawable.ic_mastodon,
            stringId = R.string.send_toot
        )
        AnimatedVisibility(shareOnMastodon) {
            SwitchWithIconAndText(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                checked = chainToot,
                onCheckedChange =  {
                    chainTootAction(it)
                },
                drawableId = R.drawable.ic_chain,
                stringId = R.string.chain_toot
            )
        }
    }
}

@Composable
fun SelectCoTravellers(
    onUsersSelected: (List<TrustedUser>) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: TrustedUsersViewModel = viewModel()
    val trustingPersons = remember { mutableStateListOf<TrustedUser>() }
    val selectedForCheckIn = remember { mutableStateListOf<TrustedUser>() }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(trustingPersons.toList()) {
        if (trustingPersons.isEmpty()) {
            isLoading = true
            val trusting = viewModel.getTrustingUsers()
            if (trusting != null) {
                trustingPersons.addAll(trusting)
            }
            isLoading = false
        }
    }

    Column(
        modifier = modifier
            .padding(8.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(id = R.string.select_co_travellers),
            style = LocalFont.current.titleLarge
        )
        Text(
            text = stringResource(id = R.string.only_check_in_persons),
            style = LocalFont.current.labelMedium
        )
        if (isLoading) {
            DataLoading()
        } else {
            if (trustingPersons.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.no_person_allowed_check_in),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            } else {
                trustingPersons.forEach { user ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = selectedForCheckIn.contains(user),
                            onCheckedChange = {
                                if (it) {
                                    selectedForCheckIn.add(user)
                                } else {
                                    selectedForCheckIn.remove(user)
                                }
                            }
                        )
                        ProfilePicture(
                            name = user.user.name,
                            url = user.user.avatarUrl,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "@${user.user.username}"
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    ButtonWithIconAndText(
                        stringId = R.string.ok,
                        drawableId = R.drawable.ic_check_in,
                        onClick = {
                            onUsersSelected(selectedForCheckIn)
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DialogPreviews() {
    MainTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SelectStatusBusinessDialog()
            SelectStatusVisibilityDialog()
            SelectEventDialog(activeEvents = listOf())
        }
    }
}
