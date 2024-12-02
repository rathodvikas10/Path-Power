package com.enmanuelbergling.pathpower

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.util.lerp
import coil.compose.AsyncImage
import com.enmanuelbergling.pathpower.ui.cars.model.CARS
import com.enmanuelbergling.pathpower.ui.wallpaper.Wallpaper
import kotlin.math.absoluteValue
import androidx.compose.ui.unit.lerp as dpInterpolation

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.CardStack(list: List<Wallpaper>, modifier: Modifier = Modifier) {
    val state = rememberLazyListState()

    var listSize by remember {
        mutableStateOf(Size.Zero)
    }

    var selectedWallpaper by remember {
        mutableStateOf<Wallpaper?>(null)
    }

    Column(modifier) {
        AnimatedVisibility(
            visible = selectedWallpaper != null,
            modifier = Modifier.weight(.4f)
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                selectedWallpaper?.let { model ->
                    WallCard(
                        model = model,
                        modifier = Modifier.height(240.dp),
                        animatedVisibilityScope = this@AnimatedVisibility,
                    ) { selectedWallpaper = null }
                }
            }
        }

        AnimatedContent(
            targetState = selectedWallpaper != null,
            modifier = Modifier
                .weight(.6f)
                .fillMaxWidth()
                .onSizeChanged {
                    listSize = it.toSize()
                },
            label = "stack content switch",
        ) { selected ->
            if (selected) {
                selectedWallpaper?.let { WallHeap(it, list, Modifier.fillMaxSize()) }
            } else {
                val itemHeight = 180.dp
                val maxPaddingItem = 80.dp

                LazyColumn(
                    state = state,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(-itemHeight),
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {

                    items(list, key = { it.key }) { wallpaper ->
                        val fraction by remember {
                            derivedStateOf {
                                val itemInfo = state.layoutInfo.visibleItemsInfo.find { it.key == wallpaper.key }
                                val result = itemInfo?.let {
                                    it.offset / listSize.height
                                } ?: 0f

                                result.coerceAtMost(1f)
                            }
                        }

                        val transition = updateTransition(fraction, "fraction transition")

                        val fartherSection = .3f

                        val animatedRotation by transition.animateFloat(
                            label = "rotation animation",
                            transitionSpec = { tween(50, easing = LinearEasing) },
                        ) { value ->
                            //to slightly increase rotation in the last ones
                            if (value <= fartherSection) {
                                val newFraction = value.coerceAtLeast(0f) / fartherSection
                                lerp(0f, 10f, newFraction)
                            } else {
                                val newFraction = (value - fartherSection) / (1f - fartherSection)
                                lerp(10f, 80f, newFraction)
                            }
                        }

                        val animatedScale by transition.animateFloat(label = "") { value ->
                            if (value <= fartherSection) {
                                val newFraction = value / fartherSection
                                lerp(.85f, 1.2f, newFraction)
                            } else 1.2f
                        }

                        val topPadding by transition.animateDp(
                            label = "y translation",
                            transitionSpec = { tween(50, easing = LinearEasing) },
                        ) { value ->
                            if (value < 0f) {
                                val maxValue = .35f
                                val newFraction = value.absoluteValue / maxValue
                                dpInterpolation(
                                    start = maxPaddingItem,
                                    stop = maxPaddingItem.times(3.7f),
                                    fraction = newFraction
                                )
                            } else if (value <= fartherSection) {
                                val newFraction = value / fartherSection
                                dpInterpolation(maxPaddingItem, 0.dp, newFraction)
                            } else {
                                val newFraction = (value - fartherSection) / (1f - fartherSection)
                                dpInterpolation(0.dp, maxPaddingItem, newFraction)
                            }
                        }

                        Column(
                            modifier = Modifier
                                .height(maxPaddingItem + itemHeight)
                        ) {
                            WallCard(
                                model = wallpaper,
                                modifier = Modifier
                                    .height(itemHeight)
                                    .graphicsLayer {
                                        translationY = topPadding.toPx()
                                    }
                                    .graphicsLayer {
                                        transformOrigin = TransformOrigin(.5f, .35f)

                                        rotationX = -animatedRotation

                                        scaleX = animatedScale
                                        scaleY = animatedScale
                                    },
                                animatedVisibilityScope = this@AnimatedContent
                            ) { selectedWallpaper = wallpaper }

                        }
                    }
                }
            }
        }
    }

}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.WallCard(
    model: Wallpaper,
    modifier: Modifier = Modifier,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onClick: () -> Unit,
) {
    ElevatedCard(onClick, shape = RoundedCornerShape(4), modifier = modifier
        .aspectRatio(7f / 5f)
        .sharedElement(
            state = rememberSharedContentState(key = model.key),
            animatedVisibilityScope = animatedVisibilityScope,
            boundsTransform = { _, _ ->
                spring(
                    Spring.DampingRatioLowBouncy,
                    Spring.StiffnessLow
                )
            }
        )) {
        AsyncImage(
            model.image,
            contentDescription = "car image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun WallCard(
    model: Wallpaper,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    ElevatedCard(
        onClick, shape = RoundedCornerShape(4),
        modifier = modifier.aspectRatio(7f / 5f)
    ) {
        AsyncImage(
            model.image,
            contentDescription = "wallpaper image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun WallHeap(selected: Wallpaper, list: List<Wallpaper>, modifier: Modifier = Modifier) {

    Column(
        verticalArrangement = Arrangement.spacedBy((-175).dp),
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        list.filterNot { it == selected }.forEachIndexed { index, it ->
            val fraction = remember { (index + 1) / CARS.size.toFloat() - 1f }
            val scale = remember { lerp(1f, 1.1f, fraction) }
            WallCard(it,
                Modifier
                    .height(200.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }) { }
        }
    }
}