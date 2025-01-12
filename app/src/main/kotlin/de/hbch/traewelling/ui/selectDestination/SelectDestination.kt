package de.hbch.traewelling.ui.selectDestination

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.viewmodel.compose.viewModel
import de.hbch.traewelling.R
import de.hbch.traewelling.api.models.trip.HafasTrainTrip
import de.hbch.traewelling.api.models.trip.HafasTrainTripStation
import de.hbch.traewelling.api.models.trip.ProductType
import de.hbch.traewelling.shared.CheckInViewModel
import de.hbch.traewelling.theme.LocalColorScheme
import de.hbch.traewelling.theme.LocalFont
import de.hbch.traewelling.ui.composables.DataLoading
import de.hbch.traewelling.ui.composables.LineIcon
import de.hbch.traewelling.util.getDelayColor
import de.hbch.traewelling.util.getLocalTimeString

@Composable
fun SelectDestination(
    checkInViewModel: CheckInViewModel,
    modifier: Modifier = Modifier,
    onStationSelected: (HafasTrainTripStation) -> Unit = { }
) {
    val selectDestinationViewModel: SelectDestinationViewModel = viewModel()
    var trip by remember { mutableStateOf<HafasTrainTrip?>(null) }
    var dataLoading by remember { mutableStateOf(false) }
    var dataError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(trip) {
        if (trip == null) {
            dataLoading = true
            selectDestinationViewModel.getTrip(
                checkInViewModel.tripId,
                checkInViewModel.lineName,
                checkInViewModel.startStationId,
                { tripData ->
                    dataLoading = false
                    val relevantStations = tripData.stopovers.subList(
                        tripData.stopovers.indexOf(
                            tripData.stopovers.find {
                                it.id == checkInViewModel.startStationId
                                        && it.departurePlanned.isEqual(checkInViewModel.departureTime)
                            }
                        ) + 1, tripData.stopovers.lastIndex + 1)

                    tripData.stopovers = relevantStations
                    trip = tripData
                },
                {
                    dataLoading = false
                    dataError = true
                    errorMessage = it ?: ""
                }
            )
        }
    }

    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ElevatedCard {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (dataLoading) {
                    DataLoading()
                } else {
                    if (dataError) {
                        Icon(
                            painter = painterResource(R.drawable.ic_error),
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = stringResource(R.string.timetable_api_error),
                            textAlign = TextAlign.Center,
                            style = LocalFont.current.bodyLarge
                        )
                        Text(
                            text = errorMessage,
                            textAlign = TextAlign.Center,
                            style = LocalFont.current.bodySmall,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    } else if (trip != null) {
                        FromToTextRow(
                            category = trip!!.safeProductType,
                            lineName = trip!!.lineName,
                            lineId = checkInViewModel.lineId,
                            operatorCode = checkInViewModel.operatorCode,
                            destination = trip!!.destination.name,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Column(
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            trip!!.stopovers.forEachIndexed { index, tripStation ->
                                TravelStopListItem(
                                    modifier = Modifier.clickable(onClick = {
                                        if (!tripStation.isCancelled) {
                                            checkInViewModel.arrivalTime =
                                                tripStation.arrivalPlanned
                                            checkInViewModel.destination = tripStation.name
                                            checkInViewModel.destinationStationId = tripStation.id
                                            onStationSelected(tripStation)
                                        }
                                    }),
                                    station = tripStation,
                                    isLastStop = index == trip!!.stopovers.size - 1
                                )
                            }
                        }
                    }
                }
            }
        }
        Box { }
    }
}

@Composable
fun FromToTextRow(
    modifier: Modifier = Modifier,
    category: ProductType?,
    lineName: String,
    lineId: String?,
    operatorCode: String?,
    destination: String
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (category != null) {
            Image(
                modifier = Modifier.size(24.dp),
                painter = painterResource(id = category.getIcon(operatorCode, lineId)),
                contentDescription = null
            )
        }
        LineIcon(
            lineName = lineName,
            lineId = lineId,
            operatorCode = operatorCode,
            modifier = Modifier.padding(start = 4.dp),
            defaultTextStyle = LocalFont.current.titleLarge,
            journeyNumber = null
        )
        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_right),
            contentDescription = null,
            modifier = Modifier.padding(start = 8.dp)
        )
        Text(
            modifier = Modifier.padding(start = 8.dp),
            text = destination,
            style = LocalFont.current.titleLarge,
            overflow = TextOverflow.Ellipsis,
            maxLines = 2
        )
    }
}

@Composable
private fun TravelStopListItem(
    modifier: Modifier = Modifier,
    station: HafasTrainTripStation,
    isLastStop: Boolean = false
) {
    ConstraintLayout(
        modifier = modifier.fillMaxWidth()
    ) {
        val (
            perlschnurTop,
            perlschnurMain,
            perlschnurBottom,
            stationName,
            time
        ) = createRefs()

        // Perlschnur
        Image(
            modifier = Modifier.constrainAs(perlschnurTop) {
                top.linkTo(parent.top)
                bottom.linkTo(perlschnurMain.top)
                start.linkTo(perlschnurMain.start)
                end.linkTo(perlschnurMain.end)
                height = Dimension.fillToConstraints
                width = Dimension.value(2.dp)
            },
            painter = painterResource(id = R.drawable.ic_perlschnur_connection),
            contentDescription = null,
            colorFilter = ColorFilter.tint(Color.Gray),
            contentScale = ContentScale.Crop
        )
        Image(
            modifier = Modifier
                .size(20.dp)
                .constrainAs(perlschnurMain) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                },
            painter = painterResource(id = R.drawable.ic_perlschnur_main),
            contentDescription = null,
            colorFilter = ColorFilter.tint(LocalColorScheme.current.primary)
        )
        val bottomConstraint = Modifier.constrainAs(perlschnurBottom) {
            top.linkTo(perlschnurMain.bottom)
            bottom.linkTo(parent.bottom)
            end.linkTo(perlschnurMain.end)
            start.linkTo(perlschnurMain.start)
            height = Dimension.fillToConstraints
            width = Dimension.value(2.dp)
        }
        if (isLastStop) {
            Box(
                modifier = bottomConstraint
            )
        } else {
            Image(
                modifier = bottomConstraint,
                painter = painterResource(id = R.drawable.ic_perlschnur_connection),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Color.Gray),
                contentScale = ContentScale.Crop
            )
        }

        // Station description
        var stationNameText = station.name
        if (station.rilIdentifier != null)
            stationNameText = stationNameText.plus(" [${station.rilIdentifier}]")
        Text(
            modifier = Modifier
                .padding(vertical = 12.dp)
                .constrainAs(stationName) {
                    start.linkTo(perlschnurMain.end, margin = 8.dp)
                    top.linkTo(perlschnurTop.top)
                    bottom.linkTo(perlschnurBottom.bottom)
                    end.linkTo(time.start, margin = 8.dp)
                    width = Dimension.fillToConstraints
                },
            text = stationNameText,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = LocalFont.current.titleMedium
        )

        // Time/Cancelled
        Column(
            modifier = Modifier.constrainAs(time) {
                end.linkTo(parent.end)
                top.linkTo(perlschnurTop.top)
                bottom.linkTo(perlschnurBottom.bottom)
            },
            horizontalAlignment = Alignment.End
        ) {
            if (station.isCancelled) {
                Text(
                    text = stringResource(id = R.string.cancelled),
                    color = Color.Red,
                    style = LocalFont.current.titleMedium
                )
            } else {
                Text(
                    text = getLocalTimeString(
                        date = station.arrivalReal ?: station.arrivalPlanned
                    ),
                    color = getDelayColor(
                        real = station.arrivalReal,
                        planned = station.arrivalPlanned
                    ),
                    style = LocalFont.current.titleMedium
                )
            }
            if (station.isCancelled || (station.arrivalReal ?: station.arrivalPlanned) > station.arrivalPlanned) {
                Text(
                    text = getLocalTimeString(
                        date = station.arrivalPlanned
                    ),
                    textDecoration = TextDecoration.LineThrough,
                    style = LocalFont.current.labelMedium
                )
            }
        }
    }
}
