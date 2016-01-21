package org.slavick.dailydozen.activity;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;

import org.slavick.dailydozen.R;
import org.slavick.dailydozen.model.Day;
import org.slavick.dailydozen.model.Servings;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ServingsHistoryActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servings_history);

        initChart();
    }

    private void initChart() {
        final CombinedChart chart = (CombinedChart) findViewById(R.id.daily_servings_chart);

        chart.setData(getChartData());

        // Draw bars behind lines
        chart.setDrawOrder(new CombinedChart.DrawOrder[]{
                CombinedChart.DrawOrder.BAR, CombinedChart.DrawOrder.LINE
        });

        // Only show 1 week at a time and start the chart with the latest day in view
        chart.setVisibleXRange(7, 7);
        chart.moveViewToX(chart.getXChartMax());

        chart.setDescription("");

        // Prevents the value for each bar from drawing over the labels at the top
        chart.setDrawValueAboveBar(false);

        // Even though we hide the left axis, we must set its max value so that full servings days reach the top
        chart.getAxisLeft().setAxisMaxValue(24);
        chart.getAxisLeft().setEnabled(false);

        chart.getAxisRight().setEnabled(false);

        chart.getLegend().setEnabled(false);

        // Disable all zooming and interacting with the chart
        chart.setScaleEnabled(false);
        chart.setPinchZoom(false);
        chart.setDoubleTapToZoomEnabled(false);
        chart.setHighlightPerDragEnabled(false);
        chart.setHighlightPerTapEnabled(false);
    }

    private CombinedData getChartData() {
        final List<Day> allDays = Day.getAllDays();

        final List<String> xVals = new ArrayList<>(allDays.size());
        final List<BarEntry> barEntries = new ArrayList<>(allDays.size());
        final List<Entry> lineEntries = new ArrayList<>(allDays.size());

        float previousTrend = 0;

        for (Day day : allDays) {
            xVals.add(day.getDayOfWeek());

            final int totalServingsOnDate = Servings.getTotalServingsOnDate(day);

            barEntries.add(new BarEntry(totalServingsOnDate, xVals.size()));

            previousTrend = calculateTrend(previousTrend, totalServingsOnDate);
            lineEntries.add(new Entry(previousTrend, xVals.size()));
        }

        CombinedData combinedData = new CombinedData(xVals);
        combinedData.setData(getBarData(xVals, barEntries));
        combinedData.setData(getLineData(xVals, lineEntries));
        return combinedData;
    }

    private float calculateTrend(float previousTrend, int currentValue) {
        if (previousTrend == 0) {
            return currentValue;
        } else {
            return previousTrend + 0.1f * (currentValue - previousTrend);
        }
    }

    private BarData getBarData(List<String> xVals, List<BarEntry> barEntries) {
        final BarDataSet dataSet = new BarDataSet(barEntries, "Servings");

        dataSet.setColor(ContextCompat.getColor(this, R.color.colorPrimary));
        dataSet.setValueTextColor(ContextCompat.getColor(this, android.R.color.white));
        dataSet.setValueTextSize(14);

        // We just want the value as an integer
        dataSet.setValueFormatter(new BarChartValueFormatter());

        return new BarData(xVals, dataSet);
    }

    private LineData getLineData(List<String> xVals, List<Entry> lineEntries) {
        final LineDataSet dataSet = new LineDataSet(lineEntries, "7-day moving average");

        final int color = ContextCompat.getColor(this, R.color.colorAccent);

        dataSet.setColor(color);
        dataSet.setLineWidth(2.5f);
        dataSet.setCircleColor(color);
        dataSet.setCircleRadius(5f);
        dataSet.setFillColor(color);
        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(12);
        dataSet.setValueTextColor(color);

        return new LineData(xVals, dataSet);
    }

    private class BarChartValueFormatter implements com.github.mikephil.charting.formatter.ValueFormatter {
        private DecimalFormat decimalFormat;

        public BarChartValueFormatter() {
            decimalFormat = new DecimalFormat("#");
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            return decimalFormat.format(value);
        }
    }
}
