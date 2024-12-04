package de.hbch.traewelling.ui.wrapped

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.hbch.traewelling.api.TraewellingApi
import de.hbch.traewelling.api.models.wrapped.YearInReviewData
import de.hbch.traewelling.theme.MainTheme
import kotlinx.coroutines.launch
import de.hbch.traewelling.R
import de.hbch.traewelling.util.shareImage

class WrappedActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainTheme {
                val coroutineScope = rememberCoroutineScope()
                val graphicsLayer = rememberGraphicsLayer()

                var initialized by remember { mutableStateOf(false) }
                var currentStep by remember { mutableIntStateOf(0) }
                var yearInReview by remember { mutableStateOf<YearInReviewData?>(null) }

                LaunchedEffect(initialized) {
                    if (!initialized) {
                        coroutineScope.launch {
                            val response = TraewellingApi.wrappedService.getYearInReview()
                            if (response.isSuccessful) {
                                yearInReview = response.body()
                            }
                            initialized = true
                        }
                    }
                }



                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(stringResource(R.string.wrapped_title))
                            },
                            navigationIcon = {
                                IconButton(
                                    onClick = {
                                        finish()
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_close),
                                        contentDescription = null
                                    )
                                }
                            }
                        )
                    },
                    content = { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            if (!initialized) {
                                WrappedIsBeingPrepared()
                            } else {
                                val wrappedModifier = Modifier.fillMaxSize()

                                if (yearInReview != null) {
                                    when (currentStep) {
                                        0 -> WrappedGreeting(
                                            graphicsLayer = graphicsLayer,
                                            yearInReviewData = yearInReview!!,
                                            modifier = wrappedModifier
                                        )
                                        1 -> WrappedTotalJourneys(
                                            graphicsLayer = graphicsLayer,
                                            yearInReviewData = yearInReview!!,
                                            modifier = wrappedModifier
                                        )
                                        2 -> WrappedOperatorDistance(
                                            graphicsLayer = graphicsLayer,
                                            yearInReviewData = yearInReview!!,
                                            modifier = wrappedModifier
                                        )
                                        3 -> WrappedOperatorDuration(
                                            graphicsLayer = graphicsLayer,
                                            yearInReviewData = yearInReview!!,
                                            modifier = wrappedModifier
                                        )
                                        4 -> WrappedLines(
                                            graphicsLayer = graphicsLayer,
                                            yearInReviewData = yearInReview!!,
                                            modifier = wrappedModifier
                                        )
                                        5 -> WrappedLongestDistanceTrip(
                                            graphicsLayer = graphicsLayer,
                                            yearInReviewData = yearInReview!!,
                                            modifier = wrappedModifier
                                        )
                                        6 -> WrappedLongestDurationTrip(
                                            graphicsLayer = graphicsLayer,
                                            yearInReviewData = yearInReview!!,
                                            modifier = wrappedModifier
                                        )
                                        7 -> WrappedFastestTrip(
                                            graphicsLayer = graphicsLayer,
                                            yearInReviewData = yearInReview!!,
                                            modifier = wrappedModifier
                                        )
                                        8 -> WrappedSlowestTrip(
                                            graphicsLayer = graphicsLayer,
                                            yearInReviewData = yearInReview!!,
                                            modifier = wrappedModifier
                                        )
                                        9 -> WrappedMostUnpunctualTrip(
                                            graphicsLayer = graphicsLayer,
                                            yearInReviewData = yearInReview!!,
                                            modifier = wrappedModifier
                                        )
                                        10 -> WrappedMostLikedTrip(
                                            graphicsLayer = graphicsLayer,
                                            yearInReviewData = yearInReview!!,
                                            modifier = wrappedModifier
                                        )
                                    }
                                }
                            }
                        }
                    },
                    floatingActionButton = {
                        // Share button
                        if (currentStep != 0) {
                            ExtendedFloatingActionButton(
                                onClick = {
                                    coroutineScope.launch {
                                        shareImage("", graphicsLayer.toImageBitmap())
                                    }
                                }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_share),
                                    contentDescription = null
                                )
                                Text(
                                    modifier = Modifier.padding(start = 4.dp),
                                    text = stringResource(id = R.string.title_share)
                                )
                            }
                        }
                    },
                    bottomBar = {
                        BottomAppBar {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        currentStep--
                                    },
                                    enabled = currentStep > 0
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_previous),
                                        contentDescription = null
                                    )
                                }
                                Text(
                                    text = "${currentStep + 1} / X",
                                    modifier = Modifier.padding(8.dp)
                                )
                                IconButton(
                                    onClick = {
                                        currentStep++
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_next),
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}