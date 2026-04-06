package edu.cit.lariosa.quickparcel.utils

import android.graphics.Color
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter

object EarningsChartHelper {

    /**
     * Call this from your Activity/Fragment after the view is ready.
     *
     * @param chart      The BarChart view from your layout
     * @param earnings   A map of day label -> earnings amount, e.g.
     *                   mapOf("Mon" to 18f, "Tue" to 25f, ..., "Sun" to 42f)
     *                   Pass the last 7 days in chronological order.
     */
    fun setup(chart: BarChart, earnings: Map<String, Float>) {

        val days   = earnings.keys.toList()
        val values = earnings.values.toList()

        // Build entries
        val entries = values.mapIndexed { index, amount ->
            BarEntry(index.toFloat(), amount)
        }

        // Dataset styled to match the blue header
        val dataSet = BarDataSet(entries, "Earnings").apply {
            color              = Color.parseColor("#60A5FA")   // light blue bars
            highLightColor     = Color.parseColor("#FFFFFF")
            setDrawValues(false)                               // hide value labels on bars
        }
        // Highlight today's bar (last entry) in brighter white-blue
        dataSet.colors = List(values.size) { i ->
            if (i == values.lastIndex) Color.parseColor("#FFFFFF")
            else Color.parseColor("#3B82F6")
        }

        val barData = BarData(dataSet).apply {
            barWidth = 0.55f
        }

        chart.apply {
            data = barData

            // Remove all decorations so it looks embedded in the header
            description.isEnabled  = false
            legend.isEnabled       = false
            setTouchEnabled(false)
            setDrawGridBackground(false)
            setDrawBorders(false)
            setBackgroundColor(Color.TRANSPARENT)
            extraBottomOffset = 0f
            extraTopOffset    = 4f

            // X axis — show day labels at bottom
            xAxis.apply {
                position          = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                setDrawAxisLine(false)
                granularity       = 1f
                valueFormatter    = IndexAxisValueFormatter(days)
                textColor         = Color.parseColor("#BFDBFE")  // pale blue text
                textSize          = 9f
                labelCount        = days.size
            }

            // Left Y axis — hidden
            axisLeft.apply {
                setDrawGridLines(false)
                setDrawAxisLine(false)
                setDrawLabels(false)
                axisMinimum = 0f
            }

            // Right Y axis — hidden
            axisRight.isEnabled = false

            animateY(600)
            invalidate()
        }
    }
}