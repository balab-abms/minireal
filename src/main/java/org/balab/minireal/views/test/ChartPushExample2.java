package org.balab.minireal.views.test;

import com.storedobject.chart.*;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.balab.minireal.views.MainLayout;

import java.util.Random;
import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Route(value = "sotest2", layout = MainLayout.class)
@PermitAll
public class ChartPushExample2 extends VerticalLayout{

    private Timer timer;
    private final Random randomGen = new Random();
    private final TimeData seconds = new TimeData();
    private final Data random = new Data();

    public ChartPushExample2() {

        // Creating a chart display area
        SOChart soChart = new SOChart();
        soChart.setSize("800px", "500px");

        // Generating some random values for a LineChart
        Random random = new Random();
        Data xValues = new Data(), yValues = new Data();

        xValues.setName("X Values");
        yValues.setName("Random Values");

        // Line chart is initialized with the generated XY values
        LineChart lineChart = new LineChart(xValues, yValues);
        lineChart.setName("40 Random Values");
        PointSymbol ps = lineChart.getPointSymbol(true);
        ps.setType(PointSymbolType.NONE);
        lineChart.setSmoothness(true);

        // Line chart needs a coordinate system to plot on
        // We need Number-type for both X and Y axes in this case
        XAxis xAxis = new XAxis(DataType.NUMBER);
        xAxis.setName("Ticks");
        YAxis yAxis = new YAxis(DataType.NUMBER);
        RectangularCoordinate rc = new RectangularCoordinate(xAxis, yAxis);
        lineChart.plotOn(rc);

        LineChart lineChart2 = new LineChart(xValues, yValues);
        lineChart2.setName("20 Random Values");
        lineChart2.plotOn(rc);

        // Add to the chart display area with a simple title
        soChart.add(lineChart, new Title("Sample Line Chart"));
        soChart.add(lineChart2, new Title("Sample Line Chart 2"));

        // Set the component for the view
        add(soChart);

        for (int x = 0; x < 40; x++) {
            xValues.add(x);
            yValues.add(random.nextDouble());
        }

    }





}

