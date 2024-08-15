package de.hbch.traewelling.ui.user

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.hbch.traewelling.R
import de.hbch.traewelling.api.models.user.TrustedUser
import de.hbch.traewelling.api.models.user.User
import de.hbch.traewelling.theme.AppTypography
import de.hbch.traewelling.ui.composables.ButtonWithIconAndText
import de.hbch.traewelling.ui.composables.DataLoading
import de.hbch.traewelling.ui.composables.DateTimeSelection
import de.hbch.traewelling.ui.composables.Dialog
import de.hbch.traewelling.ui.composables.ProfilePicture
import de.hbch.traewelling.ui.composables.SwitchWithIconAndText
import de.hbch.traewelling.ui.search.SearchViewModel
import de.hbch.traewelling.util.getLocalDateTimeString
import de.hbch.traewelling.util.useDebounce
import kotlinx.coroutines.launch
import java.time.ZonedDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrustedUsers(
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val viewModel: TrustedUsersViewModel = viewModel()
    val trustedUsers = remember { mutableStateListOf<TrustedUser>() }
    var loadData by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    var addDialogVisible by remember { mutableStateOf(false) }

    LaunchedEffect(loadData) {
        loadData = false
        if (trustedUsers.isEmpty()) {
            isLoading = true
            val response = viewModel.getTrustedUsers()
            if (response != null) {
                trustedUsers.addAll(response)
            }
            isLoading = false
        }
    }

    if (addDialogVisible) {
        Dialog(
            onDismissRequest = {
                addDialogVisible = false
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            AddTrustedUser(
                onAddedUser = {
                    addDialogVisible = false
                    trustedUsers.clear()
                    loadData = true
                }
            )
        }
    }

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (isLoading) {
            DataLoading()
        } else {
            ButtonWithIconAndText(
                modifier = Modifier.fillMaxWidth(),
                stringId = R.string.add_trusted_user,
                drawableId = R.drawable.ic_add_person,
                onClick = {
                    addDialogVisible = true
                }
            )
            if (trustedUsers.isNotEmpty()) {
                trustedUsers.forEach { user ->
                    var isRemoving by remember { mutableStateOf(false) }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProfilePicture(
                                name = user.user.name,
                                url = user.user.avatarUrl,
                                modifier = Modifier.size(48.dp)
                            )
                            Column {
                                Text(
                                    text = "@${user.user.username}"
                                )
                                Text(
                                    text = stringResource(
                                        id = R.string.expires_at,
                                        if (user.expiresAt != null)
                                            getLocalDateTimeString(user.expiresAt)
                                        else
                                            stringResource(id = R.string.never)
                                    )
                                )
                            }
                        }
                        IconButton(
                            enabled = !isRemoving,
                            onClick = {
                                isRemoving = true
                                coroutineScope.launch {
                                    val removed = viewModel.removeTrustedUser(user.user.id)
                                    if (removed) {
                                        trustedUsers.remove(user)
                                    }
                                    isRemoving = false
                                }
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_person_remove),
                                contentDescription = null
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddTrustedUser(
    modifier: Modifier = Modifier,
    onAddedUser: () -> Unit = { }
) {
    val coroutineScope = rememberCoroutineScope()
    val searchViewModel: SearchViewModel = viewModel()
    val trustedUsersViewModel: TrustedUsersViewModel = viewModel()
    val foundUsers = remember { mutableStateListOf<User>() }
    var isSearching by remember { mutableStateOf(false) }
    var userSearch by remember { mutableStateOf(TextFieldValue()) }
    userSearch.useDebounce(500L, coroutineScope) {
        isSearching = true
        foundUsers.clear()
        val users = searchViewModel.searchUsers(it.text)
        if (users != null) {
            foundUsers.addAll(users)
        }
        isSearching = false
    }
    var user by remember { mutableStateOf<User?>(null) }
    var trustedUnlimitedTime by remember { mutableStateOf(true) }
    var selectedExpiration by remember { mutableStateOf<ZonedDateTime?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.padding(8.dp)
    ) {
        Text(
            text = stringResource(id = R.string.add_trusted_user),
            style = AppTypography.titleLarge
        )
        OutlinedTextField(
            value = userSearch,
            onValueChange = {
                userSearch = it
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_search),
                    contentDescription = null
                )
            },
            label = {
                Text(
                    text = stringResource(id = R.string.search_users)
                )
            },
            placeholder = {
                Text(
                    text = stringResource(id = R.string.search_users)
                )
            },
            trailingIcon = {
                if (isSearching) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 4.dp
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        AnimatedVisibility(foundUsers.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                foundUsers.forEach {
                    FilterChip(
                        selected = user == it,
                        onClick = {
                            user = it
                        },
                        label = {
                            Text(
                                text = "@${it.username}"
                            )
                        },
                        leadingIcon = {
                            if (user == it) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_check),
                                    contentDescription = null
                                )
                            } else {
                                ProfilePicture(user = it, modifier = Modifier.size(24.dp))
                            }
                        }
                    )
                }
            }
        }
        AnimatedVisibility(user != null) {
            SwitchWithIconAndText(
                checked = trustedUnlimitedTime,
                onCheckedChange = { trustedUnlimitedTime = it },
                drawableId = R.drawable.ic_time,
                stringId = R.string.trusted_expires_never
            )
        }
        AnimatedVisibility(!trustedUnlimitedTime) {
            DateTimeSelection(
                initDate = null,
                plannedDate = null,
                label = R.string.select_expiration,
                modifier = Modifier.fillMaxWidth(),
                dateSelected = {
                    selectedExpiration = it
                }
            )
        }
        AnimatedVisibility(user != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                ButtonWithIconAndText(
                    stringId = R.string.save,
                    drawableId = R.drawable.ic_check_in,
                    isLoading = isSaving,
                    onClick = {
                        isSaving = true
                        coroutineScope.launch {
                            val response = trustedUsersViewModel.addTrustedUser(user!!.id, selectedExpiration)
                            if (response) {
                                onAddedUser()
                            }
                            isSaving = false
                        }
                    },
                    isEnabled = (user != null) && (trustedUnlimitedTime || selectedExpiration != null)
                )
            }
        }
    }
}
