package de.hbch.traewelling.ui.user

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import de.hbch.traewelling.R
import de.hbch.traewelling.api.models.status.AllowedPersonsToCheckIn
import de.hbch.traewelling.api.models.status.StatusVisibility
import de.hbch.traewelling.api.models.user.SaveUserSettings
import de.hbch.traewelling.shared.SettingsViewModel
import de.hbch.traewelling.ui.composables.ButtonWithIconAndText
import de.hbch.traewelling.ui.composables.SwitchWithIconAndText
import kotlinx.coroutines.launch

@Composable
fun EditProfile(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val settingsViewModel: SettingsViewModel = viewModel(
        viewModelStoreOwner = context as ViewModelStoreOwner
    )
    val userSettings by settingsViewModel.userSettings.observeAsState()

    var username by rememberSaveable { mutableStateOf("") }
    var displayName by rememberSaveable { mutableStateOf("") }
    var privateProfile by rememberSaveable { mutableStateOf(false) }
    var collectPoints by rememberSaveable { mutableStateOf(false) }
    var allowLikes by rememberSaveable { mutableStateOf(false) }
    var showHideCheckInsAfter by rememberSaveable { mutableStateOf(false) }
    var hideCheckInsAfter by rememberSaveable { mutableStateOf("") }
    var defaultStatusVisibility by rememberSaveable { mutableStateOf(StatusVisibility.PUBLIC) }
    var defaultStatusVisibilitySelectionVisible by remember { mutableStateOf(false) }
    var defaultMastodonVisibility by rememberSaveable { mutableStateOf(StatusVisibility.PUBLIC) }
    var defaultMastodonVisibilitySelectionVisible by remember { mutableStateOf(false) }
    var allowedPersonsToCheckIn by rememberSaveable { mutableStateOf(AllowedPersonsToCheckIn.FORBIDDEN) }
    var allowedPersonsToCheckInSelectionVisible by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var formErrorString by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userSettings) {
        if (userSettings != null) {
            username = userSettings!!.username
            displayName = userSettings!!.displayName
            privateProfile = userSettings!!.privateProfile
            collectPoints = userSettings!!.pointsEnabled
            allowLikes = userSettings!!.likesEnabled
            showHideCheckInsAfter = userSettings!!.privacyHideDays > 0
            hideCheckInsAfter = userSettings!!.privacyHideDays.toString()
            defaultStatusVisibility = userSettings!!.defaultStatusVisibility
            defaultMastodonVisibility = userSettings!!.mastodonVisibility ?: StatusVisibility.PUBLIC
            allowedPersonsToCheckIn = userSettings!!.allowedPersonsToCheckIn
        }
    }

    val formModifier = Modifier.fillMaxWidth()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            modifier = formModifier,
            singleLine = true,
            maxLines = 1,
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_user_mention),
                    contentDescription = null
                )
            },
            placeholder = {
                Text(
                    text = stringResource(id = R.string.input_username)
                )
            },
            label = {
                Text(
                    text = stringResource(id = R.string.input_username)
                )
            },
            isError = formErrorString?.contains("username") == true
        )
        OutlinedTextField(
            value = displayName,
            onValueChange = { displayName = it },
            modifier = formModifier,
            singleLine = true,
            maxLines = 1,
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_person),
                    contentDescription = null
                )
            },
            placeholder = {
                Text(
                    text = stringResource(id = R.string.display_name)
                )
            },
            label = {
                Text(
                    text = stringResource(id = R.string.display_name)
                )
            },
            isError = formErrorString?.contains("displayName") == true
        )
        SwitchWithIconAndText(
            modifier = formModifier,
            checked = privateProfile,
            onCheckedChange = { privateProfile = it },
            drawableId = R.drawable.ic_lock,
            stringId = R.string.private_profile
        )
        SwitchWithIconAndText(
            modifier = formModifier,
            checked = collectPoints,
            onCheckedChange = { collectPoints = it },
            drawableId = R.drawable.ic_score,
            stringId = R.string.allow_points
        )
        SwitchWithIconAndText(
            modifier = formModifier,
            checked = allowLikes,
            onCheckedChange = { allowLikes = it },
            drawableId = R.drawable.ic_heart_filled,
            stringId = R.string.allow_likes
        )
        SwitchWithIconAndText(
            modifier = formModifier,
            checked = showHideCheckInsAfter,
            onCheckedChange = {
                showHideCheckInsAfter = it
            },
            drawableId = R.drawable.ic_archive,
            stringId = R.string.auto_hide_check_ins_after
        )
        AnimatedVisibility(showHideCheckInsAfter) {
            OutlinedTextField(
                value = hideCheckInsAfter,
                onValueChange = { hideCheckInsAfter = it },
                modifier = formModifier,
                singleLine = true,
                maxLines = 1,
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_archive),
                        contentDescription = null
                    )
                },
                suffix = {
                    Text(
                        text = pluralStringResource(
                            id = R.plurals.days,
                            count = hideCheckInsAfter.toIntOrNull() ?: 0
                        )
                    )
                },
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.auto_hide_check_ins_after)
                    )
                },
                label = {
                    Text(
                        text = stringResource(id = R.string.auto_hide_check_ins_after)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = formErrorString?.contains("privacyHideDays") == true
            )
        }
        Box {
            val statusVisibilityInteractionSource = remember { MutableInteractionSource() }
            val statusFieldPressed by statusVisibilityInteractionSource.collectIsPressedAsState()
            if (statusFieldPressed) {
                defaultStatusVisibilitySelectionVisible = true
            }

            OutlinedTextField(
                value = stringResource(defaultStatusVisibility.title),
                onValueChange = { },
                modifier = formModifier.clickable(statusVisibilityInteractionSource, null) { },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_check_in),
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    Icon(
                        painter = painterResource(id = defaultStatusVisibility.icon),
                        contentDescription = null
                    )
                },
                label = {
                    Text(
                        text = stringResource(id = R.string.default_visibility_check_ins)
                    )
                },
                singleLine = true,
                interactionSource = statusVisibilityInteractionSource,
                readOnly = true
            )
            DropdownMenu(
                expanded = defaultStatusVisibilitySelectionVisible,
                onDismissRequest = {
                    defaultStatusVisibilitySelectionVisible = false
                }
            ) {
                StatusVisibility.entries.forEach {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(id = it.title)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = it.icon),
                                contentDescription = null
                            )
                        },
                        onClick = {
                            defaultStatusVisibility = it
                            defaultStatusVisibilitySelectionVisible = false
                        }
                    )
                }
            }
        }
        if (userSettings?.mastodonUrl != null) {
            Box {
                val mastodonVisibilityInteractionSource = remember { MutableInteractionSource() }
                val mastodonFieldPressed by mastodonVisibilityInteractionSource.collectIsPressedAsState()
                if (mastodonFieldPressed) {
                    defaultMastodonVisibilitySelectionVisible = true
                }

                OutlinedTextField(
                    value = stringResource(defaultMastodonVisibility.title),
                    onValueChange = { },
                    modifier = formModifier.clickable(
                        mastodonVisibilityInteractionSource,
                        null
                    ) { },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_mastodon),
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        Icon(
                            painter = painterResource(id = defaultMastodonVisibility.icon),
                            contentDescription = null
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(id = R.string.default_visibility_mastodon)
                        )
                    },
                    singleLine = true,
                    interactionSource = mastodonVisibilityInteractionSource,
                    readOnly = true
                )
                DropdownMenu(
                    expanded = defaultMastodonVisibilitySelectionVisible,
                    onDismissRequest = {
                        defaultMastodonVisibilitySelectionVisible = false
                    }
                ) {
                    StatusVisibility.entries.filter { it.isMastodonVisibility }.forEach {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(id = it.title)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = it.icon),
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                defaultMastodonVisibility = it
                                defaultMastodonVisibilitySelectionVisible = false
                            }
                        )
                    }
                }
            }
        }
        Box {
            val allowedPersonsInteractionSource = remember { MutableInteractionSource() }
            val allowedPersonsFieldPressed by allowedPersonsInteractionSource.collectIsPressedAsState()
            if (allowedPersonsFieldPressed) {
                allowedPersonsToCheckInSelectionVisible = true
            }

            OutlinedTextField(
                value = stringResource(allowedPersonsToCheckIn.title),
                onValueChange = { },
                modifier = formModifier.clickable(allowedPersonsInteractionSource, null) { },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_train),
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    Icon(
                        painter = painterResource(id = allowedPersonsToCheckIn.icon),
                        contentDescription = null
                    )
                },
                label = {
                    Text(
                        text = stringResource(id = R.string.allow_check_ins_from_others)
                    )
                },
                singleLine = true,
                interactionSource = allowedPersonsInteractionSource,
                readOnly = true
            )
            DropdownMenu(
                expanded = allowedPersonsToCheckInSelectionVisible,
                onDismissRequest = {
                    allowedPersonsToCheckInSelectionVisible = false
                }
            ) {
                AllowedPersonsToCheckIn.entries.forEach {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(id = it.title)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = it.icon),
                                contentDescription = null
                            )
                        },
                        onClick = {
                            allowedPersonsToCheckIn = it
                            allowedPersonsToCheckInSelectionVisible = false
                        }
                    )
                }
            }
        }
        ButtonWithIconAndText(
            stringId = R.string.save,
            drawableId = R.drawable.ic_check_in,
            onClick = {
                isSaving = true
                val hideDays = if (!showHideCheckInsAfter) null else hideCheckInsAfter.toIntOrNull()
                coroutineScope.launch {
                    formErrorString = null
                    val response = settingsViewModel.saveUserSettings(
                        SaveUserSettings(
                            username,
                            displayName,
                            privateProfile,
                            defaultStatusVisibility.ordinal,
                            hideDays,
                            defaultMastodonVisibility.ordinal,
                            allowedPersonsToCheckIn,
                            allowLikes,
                            collectPoints
                        )
                    )
                    if (response != null) {
                        if (response.isSuccessful) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    context.getString(R.string.changes_saved)
                                )
                            }
                        } else {
                            if (response.code() == 422) {
                                formErrorString = response.errorBody()?.string()
                            }
                        }
                    }
                    isSaving = false
                }
            },
            modifier = formModifier,
            isLoading = isSaving
        )
    }
}
