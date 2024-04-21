package com.enmanuelbergling.pathpower.ui.chart

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.enmanuelbergling.pathpower.ui.theme.PathPowerTheme
import com.enmanuelbergling.pathpower.util.styled
import kotlin.random.Random


@Preview
@Composable
fun ChartLinePlaceholder(modifier: Modifier = Modifier) {
    val color = LocalContentColor.current

    val percents by remember {
        mutableStateOf(
            (1..8).map { Random.nextFloat() }
        )
    }

    Canvas(
        modifier = modifier.sizeIn(
            minHeight = 100.dp,
            minWidth = 125.dp,
            maxHeight = 160.dp,
            maxWidth = 200.dp
        )
    ) {
        val chartPath = getChartLinePath(percents)

        drawPath(chartPath, color, style = Stroke(2.dp.toPx()))

        val chartOnGround = chartPath.apply {
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
        }

        drawPath(
            chartOnGround,
            Brush.verticalGradient(listOf(color, Color.Transparent)),
            style = Fill
        )
    }
}

@Preview
@Composable
fun ChartBarPlaceholder(modifier: Modifier = Modifier) {

    val percents by remember {
        mutableStateOf(
            (1..8).map { Random.nextFloat() }
        )
    }

    Canvas(
        modifier = modifier.sizeIn(
            minHeight = 100.dp,
            minWidth = 125.dp,
            maxHeight = 160.dp,
            maxWidth = 200.dp
        )
    ) {
        drawChartBar(percents)
    }
}

@Composable
fun ChartContainer(
    chartData: ChartUiModel,
    modifier: Modifier = Modifier,
    style: ChartStyle = ChartStyle.Line,
    colors: ChartColors = ChartDefaults.colors(),
    horizontalSpacing: Dp = 6.dp,
    verticalSpacing: Dp = 8.dp,
) {

    val minValue = chartData.values.minOf { it.value }

    val steps = (0..chartData.steps).reversed().map {
        minValue + it * chartData.steps
    }

    ConstraintLayout(modifier) {
        val (stepsRef, chartRef, valuesRef) = createRefs()

        ChartSteps(
            steps = steps,
            modifier = Modifier.constrainAs(stepsRef) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
                bottom.linkTo(anchor = valuesRef.top, margin = verticalSpacing)
                height = Dimension.fillToConstraints
            })

        Chart(
            stepValues = steps,
            style = style,
            colors = colors,
            chartData = chartData,
            modifier = Modifier.constrainAs(chartRef) {
                top.linkTo(parent.top)
                start.linkTo(stepsRef.end, margin = horizontalSpacing)
                end.linkTo(parent.end)
                bottom.linkTo(valuesRef.top, margin = verticalSpacing)
                width = Dimension.fillToConstraints
                height = Dimension.fillToConstraints
            })

        ChartValues(
            chartValues = chartData.values,
            chartStyle = style,
            modifier = Modifier.constrainAs(valuesRef) {
                start.linkTo(anchor = stepsRef.end, margin = horizontalSpacing)
                end.linkTo(parent.end)
                bottom.linkTo(parent.bottom)
                width = Dimension.fillToConstraints
            })
    }


}

@Composable
private fun ChartValues(
    chartValues: List<ChartValue>,
    chartStyle: ChartStyle,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = when (chartStyle) {
            ChartStyle.Line -> Arrangement.SpaceBetween
            ChartStyle.Bar -> Arrangement.SpaceBetween
        }
    ) {

        chartValues
            .map { it.label }
            .forEach { label ->
                Text(text = label, style = MaterialTheme.typography.labelSmall)
            }
    }
}

@Composable
private fun Chart(
    chartData: ChartUiModel,
    stepValues: List<Float>,
    style: ChartStyle,
    colors: ChartColors,
    modifier: Modifier = Modifier,
) {
    val animationProgress = remember(chartData) {
        Animatable(0f)
    }

    LaunchedEffect(key1 = chartData) {
        animationProgress.animateTo(1f, tween(3_000))
    }

    val contentColor = contentColorFor(colors.containerColor)

    val minValue by remember(chartData) {
        mutableFloatStateOf(
            chartData.values.minOf { it.value }
        )
    }

    val maxValue by remember(chartData) {
        mutableFloatStateOf(
            chartData.values.maxOf { it.value }
        )
    }

    Canvas(
        modifier = modifier
    ) {

        drawChartGrid(
            stepsCount = stepValues.count(),
            valuesCount = chartData.values.count(),
            gridColor = colors.gridColor,
        )

        val percents = chartData.values.map { chartValue ->
            val range = maxValue - minValue

            (chartValue.value - minValue) / range
        }

        when (style) {
            ChartStyle.Line -> {
                val chartPath = getChartLinePath(percents)

                clipRect(right = size.width * animationProgress.value) {
                    drawPath(path = chartPath, color = contentColor, style = Stroke(2.dp.toPx()))
                }

                val chartOnGround = chartPath.apply {
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                }

                clipRect(right = size.width * animationProgress.value) {
                    drawPath(path = chartOnGround, brush = colors.contentBrush, style = Fill)
                }
            }

            ChartStyle.Bar -> {
                clipRect(top = size.height - (animationProgress.value * size.height)) {
                    drawChartBar(
                        percents = percents,
                        brush = colors.contentBrush,
                        widthPercent = .7f
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawChartGrid(
    stepsCount: Int,
    valuesCount: Int,
    gridColor: Color,
) {
    repeat(stepsCount) { index ->
        drawLine(
            gridColor,
            Offset(0f, index.toFloat() / (stepsCount - 1) * size.height),
            Offset(size.width, index.toFloat() / (stepsCount - 1) * size.height)
        )
    }

    repeat(valuesCount) { index ->
        drawLine(
            gridColor,
            Offset(index.toFloat() / (valuesCount - 1) * size.width, 0f),
            Offset(
                index.toFloat() / (valuesCount - 1) * size.width,
                size.height
            )
        )
    }
}

@Composable
private fun ChartSteps(
    steps: List<Float>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        steps.forEach {
            Text(text = it.styled(), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Preview
@Composable
private fun ChartLinePrev() {
    val earnings = listOf(15f, 45f, 18f, 20f, 15f, 35f, 25f)

    PathPowerTheme {
        ChartContainer(
            ChartUiModel(
                steps = 4,
                values = earnings.mapIndexed { index, value ->
                    ChartValue(
                        value = value,
                        label = index.toString(),
                    )
                }
            ),
            colors = ChartDefaults.colors(),
            modifier = Modifier
                .width(300.dp)
                .height(200.dp)
                .padding(8.dp)
        )
    }
}


private fun DrawScope.getChartLinePath(percents: List<Float>) =
    Path().apply {

        percents.forEachIndexed { index, yPercent ->
            val yPoint = size.height * (1f - yPercent)

            if (index == 0) {
                moveTo(x = 0f, y = yPoint)
            } else {
                val xPoint = size.width * index / percents.lastIndex

                val previousXPoint = size.width * (index - 1) / percents.lastIndex

                val previousYPoint = size.height * (1f - percents[index - 1])

                val firstPoint = Offset(
                    x = previousXPoint * 1.15f,
                    y = previousYPoint * 1f,
                )
                val secondPoint = Offset(
                    x = xPoint * .85f,
                    y = yPoint * 1f,
                )

                cubicTo(
                    x1 = firstPoint.x,
                    y1 = firstPoint.y,
                    x2 = secondPoint.x,
                    y2 = secondPoint.y,
                    x3 = xPoint,
                    y3 = yPoint
                )
            }
        }

    }

private fun DrawScope.drawChartBar(
    percents: List<Float>,
    brush: Brush = Brush.verticalGradient(
        listOf(
            Color.Green,
            Color.Transparent
        )
    ),
    widthPercent: Float = .5f,
) {
    percents.forEachIndexed { index, yPercent ->
        val yPoint = size.height * (1f - yPercent)

        val maxBarWidth = size.width / percents.lastIndex

        val xPoint = maxBarWidth * index

        val barWidth = maxBarWidth.times(widthPercent)

        val topLeft = Offset(
            x = xPoint - (barWidth / 2),
            y = yPoint,
        )

        drawRoundRect(
            brush = brush,
            topLeft = topLeft,
            size = Size(width = barWidth, height = size.height - yPoint),
            cornerRadius = CornerRadius(
                x = barWidth,
                y = barWidth
            )
        )
    }
}