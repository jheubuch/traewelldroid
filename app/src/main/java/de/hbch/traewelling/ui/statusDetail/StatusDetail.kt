package de.hbch.traewelling.ui.statusDetail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.hbch.traewelling.R
import de.hbch.traewelling.api.dtos.Status
import de.hbch.traewelling.theme.LocalColorScheme
import de.hbch.traewelling.theme.MainTheme
import de.hbch.traewelling.ui.composables.OpenRailwayMapView
import de.hbch.traewelling.ui.composables.getPolylinesFromFeatureCollection
import de.hbch.traewelling.ui.include.status.CheckInCard
import de.hbch.traewelling.ui.include.status.CheckInCardViewModel
import org.osmdroid.views.overlay.Polyline

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun StatusDetail(
    modifier: Modifier = Modifier,
    statusId: Int,
    statusDetailViewModel: StatusDetailViewModel,
    checkInCardViewModel: CheckInCardViewModel,
    statusLoaded: (Status) -> Unit = { }
) {
    var mapExpanded by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<Status?>(null) }

    LaunchedEffect(status == null) {
        statusDetailViewModel.getStatusById(statusId, {
            val statusDto = it.toStatusDto()
            status = statusDto
            statusLoaded(statusDto)
        }, { })
    }

    Column(
        modifier = modifier
            .padding(if (mapExpanded) 0.dp else 12.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val mapModifier = modifier
            .fillMaxHeight(if (mapExpanded) 1.0f else 0.5f)
            .animateContentSize()
        Box(
            modifier = modifier
        ) {
            StatusDetailMap(
                modifier = mapModifier.align(Alignment.TopCenter),
                statusId = statusId,
                statusDetailViewModel = statusDetailViewModel
            )
            IconToggleButton(
                modifier = Modifier.align(Alignment.TopEnd),
                checked = mapExpanded,
                onCheckedChange = {
                    mapExpanded = it
                },
                colors = IconButtonDefaults.filledIconToggleButtonColors()
            ) {
                AnimatedContent(mapExpanded) {
                    val iconSource =
                        if (it) R.drawable.ic_fullscreen_exit else R.drawable.ic_fullscreen
                    Icon(
                        painter = painterResource(id = iconSource),
                        contentDescription = null
                    )
                }
            }
        }
        AnimatedVisibility (!mapExpanded) {
            CheckInCard(
                checkInCardViewModel = checkInCardViewModel,
                status = status
            )
        }
    }
}

@Composable
private fun StatusDetailMap(
    modifier: Modifier = Modifier,
    statusId: Int,
    statusDetailViewModel: StatusDetailViewModel
) {
    val color = LocalColorScheme.current.primary.toArgb()
    var polylines: List<Polyline> = listOf()
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        OpenRailwayMapView(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            onLoad = {
                if (polylines.isEmpty()) {
                    statusDetailViewModel.getPolylineForStatus(statusId, { collection ->
                        polylines = getPolylinesFromFeatureCollection(collection, color)
                        it.overlays.addAll(polylines)
                        it.zoomToBoundingBox(polylines[0].bounds.increaseByScale(1.1f), false)
                    }, { })
                }
            }
        )
    }
}

@Preview
@Composable
private fun StatusDetailPreview() {
    val statusDetailViewModel = StatusDetailViewModel()
    val checkInCardViewModel = CheckInCardViewModel()
    MainTheme {
        StatusDetail(
            modifier = Modifier.fillMaxWidth(),
            statusDetailViewModel = statusDetailViewModel,
            statusId = 1117900,
            checkInCardViewModel = checkInCardViewModel
        )
    }
}