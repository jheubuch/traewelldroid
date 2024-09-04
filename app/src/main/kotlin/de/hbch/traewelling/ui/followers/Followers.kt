package de.hbch.traewelling.ui.followers

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
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
import androidx.compose.ui.Alignment
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
import de.hbch.traewelling.ui.composables.ProfilePicture
import de.hbch.traewelling.util.OnBottomReached
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageFollowers(
    snackbarHostState: SnackbarHostState,
    showFollowRequests: Boolean,
    modifier: Modifier = Modifier
) {
    val manageFollowersViewModel: ManageFollowersViewModel = viewModel()
    var selectedTab by remember { mutableIntStateOf(if (showFollowRequests) 2 else 0) }

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
                    nextPageAction = { manageFollowersViewModel.getFollowers(it) },
                    removeAction = { manageFollowersViewModel.removeFollower(it) },
                    removeSuccessString = R.string.remove_follower_success,
                    removeErrorString = R.string.remove_follower_error
                )
            }
            1 -> {
                Followers(
                    snackbarHostState = snackbarHostState,
                    nextPageAction = { manageFollowersViewModel.getFollowings(it) },
                    removeAction = { manageFollowersViewModel.unfollowUser(it) },
                    removeSuccessString = R.string.remove_follower_success,
                    removeErrorString = R.string.remove_follower_error
                )
            }
            2 -> {
                FollowRequests(
                    snackbarHostState = snackbarHostState
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun Followers(
    snackbarHostState: SnackbarHostState,
    nextPageAction: suspend (Int) -> List<User>,
    removeAction: suspend (Int) -> Boolean,
    @StringRes removeSuccessString: Int,
    @StringRes removeErrorString: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var currentPage by rememberSaveable { mutableIntStateOf(0) }
    val users = remember { mutableStateListOf<User>() }
    val columnState = rememberLazyListState()
    columnState.OnBottomReached {
        currentPage++
    }
    var isLoading by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isLoading,
        onRefresh = {
            currentPage = 0
        }
    )

    LaunchedEffect(currentPage) {
        isLoading = true
        val page = currentPage
        if (page == 0) {
            users.clear()
        }
        val nextPage = nextPageAction(page)
        users.addAll(nextPage)
        isLoading = false
    }

    Box(
        modifier = Modifier.fillMaxWidth().pullRefresh(pullRefreshState)
    ) {
        if (users.isNotEmpty()) {
            LazyColumn(
                state = columnState,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                items(users, key = { it.id }) { user ->
                    var isRemoving by remember { mutableStateOf(false) }
                    UserRow(
                        user = user,
                        additionalActions = {
                            IconButton(
                                onClick = {
                                    isRemoving = true
                                    coroutineScope.launch {
                                        val success = removeAction(user.id)
                                        @StringRes var stringId: Int = removeErrorString
                                        if (success) {
                                            users.remove(user)
                                            stringId = removeSuccessString
                                        }
                                        isRemoving = false
                                        snackbarHostState.showSnackbar(
                                            context.getString(stringId),
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                },
                                enabled = !isRemoving
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_person_remove),
                                    contentDescription = stringResource(R.string.remove)
                                )
                            }
                        }
                    )
                }
            }
        }

        PullRefreshIndicator(
            refreshing = isLoading,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun FollowRequests(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val viewModel: ManageFollowersViewModel = viewModel()

    var currentPage by rememberSaveable { mutableIntStateOf(0) }
    val users = remember { mutableStateListOf<User>() }
    val columnState = rememberLazyListState()
    columnState.OnBottomReached {
        currentPage++
    }
    var isLoading by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isLoading,
        onRefresh = {
            currentPage = 0
        }
    )

    LaunchedEffect(currentPage) {
        isLoading = true
        val page = currentPage
        if (page == 0) {
            users.clear()
        }
        val nextPage = viewModel.getFollowRequests(page)
        users.addAll(nextPage)
        isLoading = false
    }

    Box(
        modifier = Modifier.fillMaxWidth().pullRefresh(pullRefreshState)
    ) {
        if (users.isNotEmpty()) {
            LazyColumn(
                state = columnState,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                items(users, key = { it.id }) { user ->
                    var isRemoving by remember { mutableStateOf(false) }
                    UserRow(
                        user = user,
                        additionalActions = {
                            IconButton(
                                onClick = {
                                    isRemoving = true
                                    coroutineScope.launch {
                                        val success = viewModel.acceptFollowRequest(user.id)
                                        @StringRes var stringId: Int = R.string.error_occurred
                                        if (success) {
                                            users.remove(user)
                                            stringId = R.string.follow_request_accepted
                                        }
                                        isRemoving = false
                                        snackbarHostState.showSnackbar(
                                            context.getString(stringId),
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                },
                                enabled = !isRemoving
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_check),
                                    contentDescription = stringResource(R.string.accept)
                                )
                            }
                            IconButton(
                                onClick = {
                                    isRemoving = true
                                    coroutineScope.launch {
                                        val success = viewModel.declineFollowRequest(user.id)
                                        @StringRes var stringId: Int = R.string.error_occurred
                                        if (success) {
                                            users.remove(user)
                                            stringId = R.string.follow_request_declined
                                        }
                                        isRemoving = false
                                        snackbarHostState.showSnackbar(
                                            context.getString(stringId),
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                },
                                enabled = !isRemoving
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_remove),
                                    contentDescription = stringResource(R.string.remove)
                                )
                            }
                        }
                    )
                }
            }
        }

        PullRefreshIndicator(
            refreshing = isLoading,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
private fun UserRow(
    user: User,
    modifier: Modifier = Modifier,
    additionalActions: @Composable () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(end = 8.dp).weight(1f)
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
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "(@${user.username})",
                    style = AppTypography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        additionalActions()
    }
}
