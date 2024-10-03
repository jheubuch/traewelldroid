package de.hbch.traewelling.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import de.hbch.traewelling.R
import de.hbch.traewelling.api.models.status.Status
import de.hbch.traewelling.theme.LocalColorScheme
import de.hbch.traewelling.theme.LocalFont
import de.hbch.traewelling.ui.include.status.StationRow
import de.hbch.traewelling.ui.include.status.getFormattedDistance
import de.hbch.traewelling.ui.tag.StatusTag
import de.hbch.traewelling.ui.user.getDurationString
import de.hbch.traewelling.util.shareStatus
import kotlinx.coroutines.launch

@Composable
fun SharePicDialog(
    status: Status,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val graphicsLayer = rememberGraphicsLayer()
    val context = LocalContext.current

    var shareImage by remember { mutableStateOf(true) }
    var shareTags by remember { mutableStateOf(true) }

    Column(
        modifier = modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(id = R.string.title_share),
            style = LocalFont.current.titleLarge
        )
        SwitchWithIconAndText(
            checked = shareImage,
            onCheckedChange = {
                shareImage = it
            },
            drawableId = R.drawable.ic_image,
            stringId = R.string.share_as_image
        )
        if (shareImage) {
            SwitchWithIconAndText(
                checked = shareTags,
                onCheckedChange = {
                    shareTags = it
                },
                drawableId = R.drawable.ic_tag,
                stringId = R.string.share_tags_on_image
            )
            Box(
                modifier = Modifier
                    .drawWithContent {
                        graphicsLayer.record {
                            this@drawWithContent.drawContent()
                        }
                        drawLayer(graphicsLayer)
                    }
                    .background(LocalColorScheme.current.surface)
            ) {
                SharePic(
                    status = status,
                    shareTags = shareTags,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            ButtonWithIconAndText(
                stringId = R.string.title_share,
                drawableId = R.drawable.ic_share,
                onClick = {
                    coroutineScope.launch {
                        var bitmap: ImageBitmap? = null
                        if (shareImage) {
                            bitmap = graphicsLayer.toImageBitmap()
                        }
                        context.shareStatus(status, bitmap)
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SharePic(
    status: Status,
    modifier: Modifier = Modifier,
    shareTags: Boolean = true
) {
    val primaryColor = LocalColorScheme.current.primary
    val message = status.getStatusBody()

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            val (
                perlschnurTop,
                perlschnurConnection,
                perlschnurBottom,
                stationRowTop,
                stationRowBottom,
                content
            ) = createRefs()

            // Perlschnur
            Icon(
                modifier = Modifier
                    .constrainAs(perlschnurTop) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                    }
                    .size(20.dp),
                painter = painterResource(id = R.drawable.ic_perlschnur_main),
                contentDescription = null,
                tint = primaryColor
            )
            Image(
                modifier = Modifier.constrainAs(perlschnurConnection) {
                    start.linkTo(perlschnurTop.start)
                    end.linkTo(perlschnurTop.end)
                    top.linkTo(perlschnurTop.bottom)
                    bottom.linkTo(perlschnurBottom.top)
                    height = Dimension.fillToConstraints
                    width = Dimension.value(2.dp)
                },
                painter = painterResource(id = R.drawable.ic_perlschnur_connection),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Color.Gray),
                contentScale = ContentScale.Crop
            )
            Icon(
                modifier = Modifier
                    .constrainAs(perlschnurBottom) {
                        start.linkTo(parent.start)
                        bottom.linkTo(parent.bottom)
                    }
                    .size(20.dp),
                painter = painterResource(id = R.drawable.ic_perlschnur_main),
                contentDescription = null,
                tint = primaryColor
            )

            // Station row top
            StationRow(
                modifier = Modifier.constrainAs(stationRowTop) {
                    start.linkTo(perlschnurTop.end, 8.dp)
                    end.linkTo(parent.end)
                    top.linkTo(parent.top)
                    width = Dimension.fillToConstraints
                },
                station = status.journey.origin,
                timePlanned = status.journey.origin.departurePlanned,
                timeReal = status.journey.departureManual ?: status.journey.origin.departureReal
            )

            // Station row bottom
            StationRow(
                modifier = Modifier
                    .constrainAs(stationRowBottom) {
                        start.linkTo(perlschnurBottom.end, 8.dp)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                        width = Dimension.fillToConstraints
                    },
                station = status.journey.destination,
                timePlanned = status.journey.destination.arrivalPlanned,
                timeReal = status.journey.arrivalManual ?: status.journey.destination.arrivalReal,
                verticalAlignment = Alignment.Bottom
            )

            // Main content
            Column(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .constrainAs(content) {
                        top.linkTo(stationRowTop.bottom)
                        bottom.linkTo(stationRowBottom.top)
                        start.linkTo(stationRowTop.start)
                        end.linkTo(stationRowTop.end)
                        width = Dimension.fillToConstraints
                    },
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FlowRow(
                    modifier = modifier
                ) {
                    val alignmentModifier = Modifier.align(Alignment.CenterVertically)
                    Image(
                        modifier = alignmentModifier,
                        painter = painterResource(id = status.journey.safeProductType.getIcon()),
                        contentDescription = null
                    )
                    LineIconView(
                        lineName = status.journey.line,
                        modifier = alignmentModifier.padding(start = 4.dp),
                        operatorCode = status.journey.operator?.id,
                        lineId = status.journey.lineId,
                    )
                    Text(
                        modifier = alignmentModifier.padding(start = 12.dp),
                        text = getFormattedDistance(status.journey.distance),
                        style = LocalFont.current.bodySmall
                    )
                    Text(
                        modifier = alignmentModifier.padding(start = 8.dp),
                        text = getDurationString(duration = status.journey.duration),
                        style = LocalFont.current.bodySmall
                    )
                }
                if (message.first.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_quote),
                            contentDescription = null
                        )
                        CustomClickableText(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            text = message.first,
                            style = LocalTextStyle.current.copy(color = LocalContentColor.current),
                            inlineContent = message.second
                        )
                    }
                }
            }
        }
        if (shareTags) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy((-12).dp)
            ) {
                status.tags
                    .forEach {
                        StatusTag(tag = it)
                    }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProfilePicture(
                    name = status.user.name,
                    url = status.user.avatarUrl,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = status.user.name
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_logo),
                    contentDescription = null,
                    tint = primaryColor
                )
                Text(
                    text = stringResource(id = R.string.app_name),
                    color = primaryColor,
                    style = LocalFont.current.bodySmall
                )
            }
        }
    }
}
