package de.hbch.traewelling.ui.report

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.hbch.traewelling.R
import de.hbch.traewelling.api.models.report.Report
import de.hbch.traewelling.api.models.report.ReportReason
import de.hbch.traewelling.api.models.report.ReportSubjectType
import de.hbch.traewelling.theme.LocalFont
import de.hbch.traewelling.ui.composables.ButtonWithIconAndText
import de.hbch.traewelling.ui.composables.OutlinedButtonWithIconAndText
import kotlinx.coroutines.launch

@Composable
fun Report(
    statusId: Int,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val reportViewModel: ReportViewModel = viewModel()
    var reportReasonSelectionVisible by remember { mutableStateOf(false) }
    var reportReason by remember { mutableStateOf(ReportReason.INAPPROPRIATE) }
    var reportText by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(
        TextFieldValue(text = "")
    ) }
    var reportLoading by remember { mutableStateOf(false) }
    var reportState by remember { mutableStateOf<Boolean?>(null) }
    val formModifier = Modifier.padding(horizontal = 4.dp)

    AnimatedVisibility(reportState == null) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_report),
                    contentDescription = null
                )
                Text(
                    text = stringResource(id = R.string.create_report),
                    style = LocalFont.current.titleLarge
                )
            }

            HorizontalDivider(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .fillMaxWidth()
            )

            Text(
                text = stringResource(id = R.string.report_reason_description),
                modifier = formModifier.fillMaxWidth()
            )

            Box(
                modifier = formModifier.fillMaxWidth()
            ) {
                OutlinedButtonWithIconAndText(
                    stringId = reportReason.title,
                    modifier = Modifier.fillMaxWidth(),
                    drawableId = reportReason.icon,
                    onClick = { reportReasonSelectionVisible = true }
                )
                DropdownMenu(
                    expanded = reportReasonSelectionVisible,
                    onDismissRequest = { reportReasonSelectionVisible = false }
                ) {
                    ReportReason.entries.forEach {
                        DropdownMenuItem(
                            onClick = {
                                reportReason = it
                                reportReasonSelectionVisible = false
                            },
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
                            }
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .fillMaxWidth()
            )

            OutlinedTextField(
                value = reportText,
                onValueChange = { reportText = it },
                modifier = formModifier.fillMaxWidth(),
                label = {
                    Text(
                        text = stringResource(id = R.string.additional_information)
                    )
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                ButtonWithIconAndText(
                    stringId = R.string.title_report,
                    drawableId = R.drawable.ic_check_in,
                    isLoading = reportLoading,
                    onClick = {
                        reportLoading = true
                        coroutineScope.launch {
                            val state = reportViewModel.createReport(
                                Report(
                                    ReportSubjectType.STATUS,
                                    statusId,
                                    reportReason,
                                    reportText.text
                                )
                            )
                            reportLoading = false
                            reportState = state
                        }
                    }
                )
            }
        }
    }

    AnimatedVisibility(reportState != null) {
        var icon = R.drawable.ic_check_in
        var color = Color.Green
        var text = R.string.report_success

        if (reportState == false) {
            icon = R.drawable.ic_error
            color = Color.Red
            text = R.string.report_error
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp).fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = color
                )
            }
            Text(
                text = stringResource(id = text),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}
