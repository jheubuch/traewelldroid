package de.hbch.traewelling.ui.wrapped

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import de.hbch.traewelling.R
import de.hbch.traewelling.api.models.status.Status
import de.hbch.traewelling.api.models.wrapped.YearInReviewData
import de.hbch.traewelling.theme.AppTypography
import de.hbch.traewelling.theme.BTModern
import de.hbch.traewelling.theme.LocalColorScheme
import de.hbch.traewelling.theme.LocalFont
import de.hbch.traewelling.ui.composables.ButtonWithIconAndText
import de.hbch.traewelling.ui.composables.DataLoading
import de.hbch.traewelling.ui.composables.SharePic
import de.hbch.traewelling.ui.include.status.getFormattedDistance
import de.hbch.traewelling.ui.user.getDurationString
import de.hbch.traewelling.util.getLocalDateString

@Composable
fun WrappedTeaser(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    ElevatedCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.year_is_over, 2024),
                    fontFamily = BTModern,
                    style = LocalFont.current.headlineSmall,
                    color = LocalColorScheme.current.primary
                )
                Text(
                    text = stringResource(R.string.discover_recap, 2024),
                    style = LocalFont.current.bodyMedium
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                ButtonWithIconAndText(
                    stringId = R.string.take_me_back,
                    drawableId = R.drawable.ic_arrow_right,
                    onClick = {
                        context.startActivity(Intent(context, WrappedActivity::class.java))
                    }
                )
            }
        }
    }
}

@Composable
fun WrappedIsBeingPrepared(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DataLoading()
        Text(
            text = stringResource(R.string.wrapped_is_being_prepared),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun WrappedGreeting(
    graphicsLayer: GraphicsLayer,
    yearInReviewData: YearInReviewData,
    modifier: Modifier = Modifier
) {
    WrappedScaffold(
        graphicsLayer = graphicsLayer,
        modifier = modifier,
        header = {
            Text(
                modifier = it,
                text = stringResource(R.string.wrapped_hey, yearInReviewData.user.name),
                style = AppTypography.headlineLarge
            )
        },
        status = null
    ) {
        Text(
            modifier = it,
            text = stringResource(R.string.wrapped_intro, 2024),
            style = AppTypography.titleLarge,
            textAlign = TextAlign.Justify
        )
    }
}

@Composable
fun WrappedTotalJourneys(
    graphicsLayer: GraphicsLayer,
    yearInReviewData: YearInReviewData,
    modifier: Modifier = Modifier
) {
    val primaryColor = LocalColorScheme.current.primary
    val primarySpanStyle = SpanStyle(color = primaryColor)
    val largeSpanStyle = SpanStyle(fontSize = AppTypography.headlineLarge.fontSize)
    WrappedScaffold(
        graphicsLayer = graphicsLayer,
        modifier = modifier,
        header = {
            val annotatedString = buildAnnotatedString {
                withStyle(primarySpanStyle) {
                    append(yearInReviewData.count.toString())
                    append(' ')
                }
                append(stringResource(R.string.wrapped_times))
            }
            Text(
                modifier = it,
                text = annotatedString,
                style = AppTypography.headlineLarge
            )
        },
        status = null
    ) {
        val annotatedString = buildAnnotatedString {
            appendLine(stringResource(R.string.wrapped_times_checked_in))
            appendLine()
            withStyle(primarySpanStyle.merge(largeSpanStyle)) {
                appendLine(getFormattedDistance(yearInReviewData.distance.total.toInt()))
            }
            appendLine()
            appendLine(stringResource(R.string.and))
            appendLine()
            withStyle(primarySpanStyle.merge(largeSpanStyle)) {
                appendLine(getDurationString(yearInReviewData.duration.total.toInt()))
            }
            appendLine()
            append(stringResource(R.string.of_travel))
        }
        Text(
            modifier = it,
            text = annotatedString,
            style = AppTypography.titleLarge,
            textAlign = TextAlign.Justify
        )
    }
}

@Composable
fun WrappedOperatorDistance(
    graphicsLayer: GraphicsLayer,
    yearInReviewData: YearInReviewData,
    modifier: Modifier = Modifier
) {
    WrappedScaffold(
        graphicsLayer = graphicsLayer,
        modifier = modifier,
        status = null,
        header = {
            Text(
                modifier = it,
                text = stringResource(R.string.wrapped_favorite_operator),
                style = AppTypography.headlineLarge
            )
        }
    ) {
        val primaryColor = LocalColorScheme.current.primary
        val primarySpanStyle = SpanStyle(color = primaryColor)
        val largeSpanStyle = SpanStyle(fontSize = AppTypography.headlineLarge.fontSize)
        val centerAlignedStyle = ParagraphStyle(textAlign = TextAlign.Center)

        val annotatedString = buildAnnotatedString {
            appendLine(stringResource(R.string.wrapped_farest_travels))
            appendLine()
            withStyle(primarySpanStyle.merge(largeSpanStyle)) {
                withStyle(centerAlignedStyle) {
                    appendLine(yearInReviewData.operators.topByDistance.operator)
                }
            }
            appendLine()
            appendLine(stringResource(R.string.wrapped_in_a_total))
            appendLine()
            withStyle(primarySpanStyle.merge(largeSpanStyle)) {
                appendLine(getFormattedDistance(yearInReviewData.operators.topByDistance.distance.toInt()))
            }
        }
        Text(
            modifier = it,
            text = annotatedString,
            style = AppTypography.titleLarge,
            textAlign = TextAlign.Justify
        )
    }
}

@Composable
fun WrappedOperatorDuration(
    graphicsLayer: GraphicsLayer,
    yearInReviewData: YearInReviewData,
    modifier: Modifier = Modifier
) {
    WrappedScaffold(
        graphicsLayer = graphicsLayer,
        modifier = modifier,
        status = null,
        header = {
            Text(
                modifier = it,
                text = stringResource(R.string.wrapped_times_travels),
                style = AppTypography.headlineLarge
            )
        }
    ) {
        val primaryColor = LocalColorScheme.current.primary
        val primarySpanStyle = SpanStyle(color = primaryColor)
        val largeSpanStyle = SpanStyle(fontSize = AppTypography.headlineLarge.fontSize)
        val centerAlignedStyle = ParagraphStyle(textAlign = TextAlign.Center)

        val annotatedString = buildAnnotatedString {
            appendLine(stringResource(R.string.wrapped_longest_travels))
            withStyle(primarySpanStyle.merge(largeSpanStyle)) {
                withStyle(centerAlignedStyle) {
                    append(yearInReviewData.operators.topByDuration.operator)
                }
            }
            appendLine()
            appendLine(stringResource(R.string.wrapped_in_a_total))
            appendLine()
            withStyle(primarySpanStyle.merge(largeSpanStyle)) {
                appendLine(getDurationString(yearInReviewData.operators.topByDuration.duration.toInt()))
            }
            appendLine()
            appendLine(stringResource(R.string.wrapped_like_trains))
        }
        Text(
            modifier = it,
            text = annotatedString,
            style = AppTypography.titleLarge,
            textAlign = TextAlign.Justify
        )
    }
}

@Composable
fun WrappedLines(
    graphicsLayer: GraphicsLayer,
    yearInReviewData: YearInReviewData,
    modifier: Modifier = Modifier
) {
    WrappedScaffold(
        graphicsLayer = graphicsLayer,
        modifier = modifier,
        status = null,
        header = {
            Text(
                modifier = it,
                text = stringResource(R.string.wrapped_favorite_lines),
                style = AppTypography.headlineLarge
            )
        }
    ) {
        val primaryColor = LocalColorScheme.current.primary
        val primarySpanStyle = SpanStyle(color = primaryColor)
        val largeSpanStyle = SpanStyle(fontSize = AppTypography.headlineLarge.fontSize)

        val annotatedString = buildAnnotatedString {
            appendLine(stringResource(R.string.wrapped_line_distance))
            withStyle(primarySpanStyle.merge(largeSpanStyle)) {
                append(yearInReviewData.lines.topByDistance.line)
            }
            append(' ')
            append(stringResource(R.string.operated_by))
            append(' ')
            withStyle(primarySpanStyle) {
                append(yearInReviewData.lines.topByDistance.operator)
                append(' ')
                append("(${getFormattedDistance(yearInReviewData.lines.topByDistance.distance.toInt())})")
            }
            appendLine()
            appendLine()
            appendLine(stringResource(R.string.wrapped_line_duration))
            withStyle(primarySpanStyle.merge(largeSpanStyle)) {
                append(yearInReviewData.lines.topByDuration.line)
            }
            append(' ')
            append(stringResource(R.string.operated_by))
            append(' ')
            withStyle(primarySpanStyle) {
                append(yearInReviewData.lines.topByDuration.operator)
                append(' ')
                append("(${getDurationString(yearInReviewData.lines.topByDuration.duration.toInt())})")
            }
            appendLine()
        }
        Text(
            modifier = it,
            text = annotatedString,
            style = AppTypography.titleLarge,
            textAlign = TextAlign.Justify
        )
    }
}

@Composable
fun WrappedLongestDistanceTrip(
    graphicsLayer: GraphicsLayer,
    yearInReviewData: YearInReviewData,
    modifier: Modifier = Modifier
) {
    val status = yearInReviewData.longestTrips.distance
    WrappedScaffold(
        graphicsLayer = graphicsLayer,
        modifier = modifier,
        status = status,
        header = {
            Text(
                modifier = it,
                text = stringResource(R.string.wrapped_longest_distance_trip),
                style = AppTypography.headlineLarge
            )
        }
    ) {
        Text(
            modifier = it,
            text = stringResource(R.string.wrapped_your_furthest_trip, getLocalDateString(status.journey.origin.departurePlanned)),
            style = AppTypography.titleLarge,
            textAlign = TextAlign.Justify
        )
    }
}

@Composable
fun WrappedLongestDurationTrip(
    graphicsLayer: GraphicsLayer,
    yearInReviewData: YearInReviewData,
    modifier: Modifier = Modifier
) {
    val status = yearInReviewData.longestTrips.duration
    WrappedScaffold(
        graphicsLayer = graphicsLayer,
        modifier = modifier,
        status = status,
        header = {
            Text(
                modifier = it,
                text = stringResource(R.string.wrapped_longest_duration_trip),
                style = AppTypography.headlineLarge
            )
        }
    ) {
        Text(
            modifier = it,
            text = stringResource(R.string.wrapped_your_longest_trip, getLocalDateString(status.journey.origin.departurePlanned)),
            style = AppTypography.titleLarge,
            textAlign = TextAlign.Justify
        )
    }
}

@Composable
fun WrappedFastestTrip(
    graphicsLayer: GraphicsLayer,
    yearInReviewData: YearInReviewData,
    modifier: Modifier = Modifier
) {
    val status = yearInReviewData.fastestTrip
    WrappedScaffold(
        graphicsLayer = graphicsLayer,
        modifier = modifier,
        status = status,
        header = {
            Text(
                modifier = it,
                text = stringResource(R.string.wrapped_fastest_trip),
                style = AppTypography.headlineLarge
            )
        }
    ) {
        Text(
            modifier = it,
            text = stringResource(R.string.wrapped_your_fastest_trip, getLocalDateString(status.journey.origin.departurePlanned)),
            style = AppTypography.titleLarge,
            textAlign = TextAlign.Justify
        )
    }
}

@Composable
fun WrappedSlowestTrip(
    graphicsLayer: GraphicsLayer,
    yearInReviewData: YearInReviewData,
    modifier: Modifier = Modifier
) {
    val status = yearInReviewData.slowestTrip
    WrappedScaffold(
        graphicsLayer = graphicsLayer,
        modifier = modifier,
        status = status,
        header = {
            Text(
                modifier = it,
                text = stringResource(R.string.wrapped_slowest_trip),
                style = AppTypography.headlineLarge
            )
        }
    ) {
        Text(
            modifier = it,
            text = stringResource(R.string.wrapped_your_slowest_trip, getLocalDateString(status.journey.origin.departurePlanned)),
            style = AppTypography.titleLarge,
            textAlign = TextAlign.Justify
        )
    }
}

@Composable
fun WrappedMostUnpunctualTrip(
    graphicsLayer: GraphicsLayer,
    yearInReviewData: YearInReviewData,
    modifier: Modifier = Modifier
) {
    val status = yearInReviewData.mostDelayedArrival
    WrappedScaffold(
        graphicsLayer = graphicsLayer,
        modifier = modifier,
        status = status,
        header = {
            Text(
                modifier = it,
                text = stringResource(R.string.wrapped_unpunctual_trip),
                style = AppTypography.headlineLarge
            )
        }
    ) {
        Text(
            modifier = it,
            text = stringResource(R.string.wrapped_your_unpunctual_trip, getLocalDateString(status.journey.origin.departurePlanned)),
            style = AppTypography.titleLarge,
            textAlign = TextAlign.Justify
        )
    }
}

@Composable
fun WrappedMostLikedTrip(
    graphicsLayer: GraphicsLayer,
    yearInReviewData: YearInReviewData,
    modifier: Modifier = Modifier
) {
    val status = yearInReviewData.mostLikedStatuses.first().status
    WrappedScaffold(
        graphicsLayer = graphicsLayer,
        modifier = modifier,
        status = status,
        header = {
            Text(
                modifier = it,
                text = stringResource(R.string.wrapped_most_liked),
                style = AppTypography.headlineLarge
            )
        }
    ) {
        Text(
            modifier = it,
            text = stringResource(id = R.string.wrapped_your_most_liked_trip, status.likes ?: 0, getLocalDateString(status.journey.origin.departurePlanned)),
            style = AppTypography.titleLarge,
            textAlign = TextAlign.Justify
        )
    }
}

@Composable
private fun WrappedScaffold(
    graphicsLayer: GraphicsLayer,
    modifier: Modifier = Modifier,
    status: Status? = null,
    header: @Composable (Modifier) -> Unit = { },
    content: @Composable (Modifier) -> Unit = { },
) {
    val defaultModifier = Modifier.fillMaxWidth()
    val primaryColor = LocalColorScheme.current.primary

    Box(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .drawWithContent {
                    graphicsLayer.record {
                        this@drawWithContent.drawContent()
                    }
                    drawLayer(graphicsLayer)
                }
                .background(LocalColorScheme.current.surface)
                .padding(8.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Headline
                header(defaultModifier)

                // Description
                content(defaultModifier)

                // Status
                if (status != null) {
                    SharePic(
                        status = status,
                        modifier = defaultModifier
                    )
                }
            }
            if (status == null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_logo),
                        contentDescription = null,
                        tint = primaryColor
                    )
                    Text(
                        text = stringResource(id = R.string.app_name),
                        color = primaryColor,
                        style = AppTypography.bodySmall
                    )
                }
            }
        }
    }
}
