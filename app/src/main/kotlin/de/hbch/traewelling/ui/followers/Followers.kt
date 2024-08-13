package de.hbch.traewelling.ui.followers

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.hbch.traewelling.R
import de.hbch.traewelling.api.models.user.User
import de.hbch.traewelling.theme.AppTypography
import de.hbch.traewelling.ui.composables.OutlinedButtonWithIconAndText
import de.hbch.traewelling.ui.composables.ProfilePicture
import de.hbch.traewelling.util.OnBottomReached
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageFollowers(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        PrimaryTabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(text = stringResource(id = R.string.followers))
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(text = stringResource(id = R.string.followings))
                }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = {
                    Text(text = stringResource(id = R.string.follow_requests))
                }
            )
        }
        when (selectedTab) {
            0 -> {
                Followers(
                    snackbarHostState = snackbarHostState,
                )
            }
            1 -> {

            }
            2 -> {

            }
        }
    }
}

@Composable
private fun Followers(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val manageFollowersViewModel: ManageFollowersViewModel = viewModel()
    val coroutineScope = rememberCoroutineScope()
    var currentPage by rememberSaveable { mutableIntStateOf(0) }
    val followers = remember { mutableStateListOf<User>() }
    val columnState = rememberLazyListState()
    columnState.OnBottomReached {
        currentPage++
    }

    LaunchedEffect(currentPage) {
        val page = currentPage
        if (page == 0) {
            followers.clear()
        }
        val users = manageFollowersViewModel.getFollowers(currentPage)
        followers.addAll(users)
    }

    if (followers.isNotEmpty()) {
        LazyColumn(
            state = columnState,
            modifier = modifier
                .fillMaxWidth()
                //.verticalScroll(rememberScrollState())
        ) {
            items(followers, key = { it.id }) { user ->
                var isRemoving by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ProfilePicture(
                            user = user,
                            modifier = Modifier.size(48.dp)
                        )
                        Column {
                            Text(
                                text = user.name,
                                style = AppTypography.labelLarge,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.fillMaxWidth(0.25f)
                            )
                            Text(
                                text = "(@${user.username})",
                                style = AppTypography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    OutlinedButtonWithIconAndText(
                        stringId = R.string.remove,
                        drawableId = R.drawable.ic_person_remove,
                        onClick = {
                            isRemoving = true
                            coroutineScope.launch {
                                val success = manageFollowersViewModel.removeFollower(user.id)
                                @StringRes var stringId: Int = R.string.remove_follower_error
                                if (success) {
                                    followers.remove(user)
                                    stringId = R.string.remove_follower_success
                                }
                                isRemoving = false
                                snackbarHostState.showSnackbar(context.getString(stringId), duration = SnackbarDuration.Short)
                            }
                        },
                        isLoading = isRemoving
                    )
                }
            }
        }
    }
}
