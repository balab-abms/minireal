package org.balab.minireal.views.test;

import com.storedobject.chart.*;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.balab.minireal.views.MainLayout;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Route(value = "sotest", layout = MainLayout.class)
@PermitAll
public class ChartPushExample extends VerticalLayout{

    private Timer timer;
    private final Random randomGen = new Random();
    private final Data seconds = new Data();
    private final Data random = new Data();
    private final DataChannel dataChannel;
    private final UI a;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public ChartPushExample() {
        setSizeFull();

        // Creating a chart display area
        SOChart soChart = new SOChart();
        soChart.setSize("100%", "500px");

        // Axes
        XAxis xAxis = new XAxis(seconds);
        xAxis.setMinAsMinData();
        xAxis.setMaxAsMaxData();
//        xAxis.getLabel(true).setInterval(-1);
//        xAxis.getGridLines(true).setInterval(-1);
        // last stop here ... check how to set the min xaxis labels gap
        YAxis yAxis = new YAxis(random);
        yAxis.setMinAsMinData();
        yAxis.setMaxAsMaxData();

        // Line chart
        LineChart lc = new LineChart(seconds, random);
        lc.plotOn(new RectangularCoordinate(xAxis, yAxis));
        lc.setSmoothness(true);
        PointSymbol ps = lc.getPointSymbol(true);
        ps.setType(PointSymbolType.NONE);


        // Add the chart to the chart display
        soChart.add(lc);

        // Set the component for the view
        add(soChart);

        // Create a data channel to push the data
//        dataChannel = new DataChannel(soChart, seconds, random);
        dataChannel = new DataChannel(soChart, random);
        

        // A reference yo the Application is required for updating UI in the background
        a = UI.getCurrent();

        // Schedule the 'data' method to be called every 2 seconds
        scheduler.scheduleAtFixedRate(this::data, 0, 250, TimeUnit.MILLISECONDS);
    }


    private void data() {
        // Generate a new data point

        seconds.add(seconds.size());
        random.add(randomGen.nextInt(1000));
        int lastIndex = seconds.size() - 1;

        a.access(
                () -> { // Required to lock the UI

                    try
                    {
//                        System.out.println(lastIndex + "-" + seconds.get(lastIndex));
                        dataChannel.append(seconds.get(lastIndex), random.get(lastIndex));
                    } catch (ChartException e)
                    {
                        throw new RuntimeException(e);
                    }
//                    try {
//                        if (lastIndex < 60) {
//                            // Append data if data size is less than 60
//                            dataChannel.append(seconds.get(lastIndex), random.get(lastIndex));
//                        } else {
//                            // Push data if data size is more than 60 (tail-end will be trimmed)
//                            dataChannel.push(seconds.get(lastIndex), random.get(lastIndex));
//                        }
//                    } catch (Exception e) {
//                        System.out.println(e);
//                    }
                });
    }


    @Override
    protected void onDetach(DetachEvent detachEvent) {
        // Properly shut down the scheduler when the UI is detached
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
        super.onDetach(detachEvent);
    }
}

