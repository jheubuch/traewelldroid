package de.hbch.traewelling.ui.user

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.canopas.lib.showcase.IntroShowcase
import com.canopas.lib.showcase.component.ShowcaseStyle
import com.canopas.lib.showcase.component.rememberIntroShowcaseState
import com.jcloquell.androidsecurestorage.SecureStorage
import de.hbch.traewelling.R
import de.hbch.traewelling.api.models.user.User
import de.hbch.traewelling.shared.LoggedInUserViewModel
import de.hbch.traewelling.shared.SharedValues
import de.hbch.traewelling.theme.LocalColorScheme
import de.hbch.traewelling.theme.LocalFont
import de.hbch.traewelling.theme.MainTheme
import de.hbch.traewelling.ui.composables.ButtonWithIconAndText
import de.hbch.traewelling.ui.composables.Dialog
import de.hbch.traewelling.ui.composables.ProfilePicture
import de.hbch.traewelling.ui.followers.ManageFollowersViewModel
import de.hbch.traewelling.util.openLink
import kotlinx.coroutines.launch

@Composable
fun UserCard(
    modifier: Modifier = Modifier,
    userViewModel: UserStatusViewModel,
    loggedInUserViewModel: LoggedInUserViewModel,
    editProfile: () -> Unit = { },
    manageFollowerAction: () -> Unit = { }
) {
    val stateUser by userViewModel.user.observeAsState()
    val stateLoggedInUser by loggedInUserViewModel.user.observeAsState()

    stateUser?.let { displayedUser ->
        stateLoggedInUser?.let { loggedInUser ->
            UserCardContent(
                user = displayedUser,
                loggedInUser = loggedInUser,
                modifier = modifier,
                followAction = { userViewModel.handleFollowButton() },
                muteAction = { userViewModel.handleMuteButton() },
                manageFollowerAction = manageFollowerAction,
                editProfile = editProfile
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserCardContent(
    user: User,
    loggedInUser: User,
    modifier: Modifier = Modifier,
    followAction: () -> Unit = { },
    muteAction: () -> Unit = { },
    manageFollowerAction: () -> Unit = { },
    editProfile: () -> Unit = { }
) {
    val context = LocalContext.current
    val secureStorage = remember { SecureStorage(context) }
    val coroutineScope = rememberCoroutineScope()
    val manageFollowersViewModel: ManageFollowersViewModel = viewModel()

    var introduceProfileEdit by remember { mutableStateOf(
        !(secureStorage.getObject(SharedValues.SS_EDIT_PROFILE_SHOWCASE, Boolean::class.java) ?: false)
    ) }

    var unfollowDialogVisible by remember { mutableStateOf(false) }
    var followedBy by remember { mutableStateOf(user.followedBy) }

    val isOwnProfile = loggedInUser.id == user.id
    val showCaseState = rememberIntroShowcaseState()

    if (unfollowDialogVisible) {
        var isRemoving by remember { mutableStateOf(false) }
        Dialog(
            onDismissRequest = { unfollowDialogVisible = false },
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.remove_follower),
                    style = LocalFont.current.titleLarge
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    ButtonWithIconAndText(
                        stringId = R.string.remove,
                        drawableId = R.drawable.ic_person_remove,
                        isLoading = isRemoving,
                        onClick = {
                            try {
                                isRemoving = true
                                coroutineScope.launch {
                                    val response = manageFollowersViewModel.removeFollower(user.id)
                                    if (response) {
                                        isRemoving = false
                                        followedBy = false
                                        unfollowDialogVisible = false
                                    }
                                }
                            } catch (_: Exception) { }
                        }
                    )
                }
            }
        }
    }

    ElevatedCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Edit profile button
            if (isOwnProfile) {
                IntroShowcase(
                    showIntroShowCase = introduceProfileEdit,
                    onShowCaseCompleted = {
                        secureStorage.storeObject(SharedValues.SS_EDIT_PROFILE_SHOWCASE, true)
                        introduceProfileEdit = false
                    },
                    dismissOnClickOutside = true,
                    state = showCaseState
                ) {
                    IconButton(
                        onClick = editProfile,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
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
                                        text = stringResource(id = R.string.edit_profile),
                                        style = LocalFont.current.titleLarge,
                                        color = LocalColorScheme.current.onPrimary
                                    )
                                    Text(
                                        text = stringResource(id = R.string.edit_profile_description),
                                        color = LocalColorScheme.current.onPrimary
                                    )
                                }
                            }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_edit),
                            contentDescription = null,
                            tint = LocalColorScheme.current.primary
                        )
                    }
                }
            }

            if (!isOwnProfile && followedBy) {
                AssistChip(
                    onClick = { unfollowDialogVisible = true },
                    label = {
                        Text(
                            text = stringResource(id = R.string.follows_you)
                        )
                    },
                    modifier = Modifier.padding(end = 8.dp).align(Alignment.TopEnd)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ProfilePicture(
                    user = user,
                    modifier = Modifier
                        .width(150.dp)
                        .height(150.dp)
                )
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            style = LocalFont.current.titleLarge,
                            text = user.name
                        )
                        if (user.privateProfile) {
                            Icon(
                                painterResource(id = R.drawable.ic_lock),
                                contentDescription = stringResource(id = R.string.private_profile),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        if (user.mastodonUrl != null) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_mastodon),
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .clickable {
                                        context.openLink(user.mastodonUrl)
                                    },
                                tint = LocalColorScheme.current.primary
                            )
                        }
                    }
                    Text(
                        style = LocalFont.current.titleMedium,
                        text = "@${user.username}"
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painterResource(id = R.drawable.ic_navigation),
                            contentDescription = null
                        )
                        Text(
                            modifier = Modifier.padding(start = 8.dp),
                            text = stringResource(
                                id = R.string.format_distance_kilometers,
                                user.distance / 1000
                            )
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier.padding(end = 8.dp),
                            text = stringResource(id = R.string.display_points, user.points)
                        )
                        Icon(
                            painterResource(id = R.drawable.ic_score),
                            contentDescription = null
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painterResource(id = R.drawable.ic_travel_time),
                            contentDescription = null
                        )
                        Text(
                            modifier = Modifier.padding(start = 8.dp),
                            text = getDurationString(user.duration)
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier.padding(end = 8.dp),
                            text = stringResource(
                                id = R.string.display_average_speed,
                                user.averageSpeed
                            )
                        )
                        Icon(
                            painterResource(id = R.drawable.ic_speed),
                            contentDescription = null
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val buttonModifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                    if (isOwnProfile) {
                        ButtonWithIconAndText(
                            stringId = R.string.followers,
                            drawableId = R.drawable.ic_group,
                            onClick = manageFollowerAction,
                            modifier = buttonModifier
                        )
                    } else {
                        FollowButton(user = user, onClick = followAction, modifier = buttonModifier)
                        MuteButton(user = user, onClick = muteAction, modifier = buttonModifier)
                    }
                }
            }
        }
    }
}

@Composable
private fun FollowButton(
    user: User,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    /*
    * Handle four states:
    * - Following
    * - Not following, public profile
    * - Not following, private profile
    * - Follow request pending
    */
    val iconDrawable =
        if (user.following) {
            R.drawable.ic_person_remove
        } else if (user.followRequestPending) {
            R.drawable.ic_hourglass
        } else {
            R.drawable.ic_add_person
        }

    val buttonText =
        if (user.following) {
            R.string.unfollow
        } else if (user.followRequestPending) {
            R.string.request_follow_pending
        } else if (user.privateProfile) {
            R.string.request_follow
        } else {
            R.string.follow
        }

    ButtonWithIconAndText(
        stringId = buttonText,
        drawableId = iconDrawable,
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
private fun MuteButton(
    user: User,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconDrawable =
        if (user.muted)
            R.drawable.ic_unmute
        else
            R.drawable.ic_mute

    val buttonText =
        if (user.muted)
            R.string.unmute
        else
            R.string.mute

    ButtonWithIconAndText(
        stringId = buttonText,
        drawableId = iconDrawable,
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
fun getDurationString(
    duration: Int
): String {
    val minutes = duration % 60
    var hours = duration / 60
    val days = hours / 24
    hours -= days * 24

    return if (days > 0) {
        stringResource(id = R.string.display_travel_time_days_hours_minutes, days, hours, minutes)
    } else {
        if (hours == 0) {
            stringResource(R.string.display_travel_time_minutes, minutes)
        } else {
            stringResource(id = R.string.display_travel_time_hours_minutes, hours, minutes)
        }
    }
}

@Preview
@Composable
private fun UserCardPreview() {
    val user1 = User(
        0,
        "Hildegard Test",
        "hildetest",
        "urlurl",
        1234567890,
        10241024,
        4711,
        null,
        false,
        null,
        null,
        false,
        false,
        false,
        null,
        false
    )
    val user2 = User(
        1,
        "Sebastian Sepp",
        "sebblsepp",
        "urlurl",
        56789,
        4568,
        42,
        null,
        true,
        null,
        null,
        false,
        false,
        false,
        null,
        true
    )

    MainTheme {
        Column {
            UserCardContent(user = user1, loggedInUser = user1)
            UserCardContent(user = user2, loggedInUser = user1, modifier = Modifier.padding(top = 16.dp))
        }
    }
}
