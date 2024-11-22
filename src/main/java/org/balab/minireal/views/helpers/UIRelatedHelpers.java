package org.balab.minireal.views.helpers;

import com.squareup.javapoet.JavaFile;
import com.storedobject.chart.*;
import com.vaadin.flow.server.StreamResource;
import org.balab.minireal.data.entity.SimForm;
import org.balab.minireal.data.service.CodeGenerationService;
import org.balab.minireal.data.service.OsService;
import org.balab.minireal.data.service.ZipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vaadin.addons.chartjs.config.LineChartConfig;
import org.vaadin.addons.chartjs.data.LineDataset;
import org.vaadin.addons.chartjs.options.Position;
import org.vaadin.addons.chartjs.options.zoom.XYMode;
import oshi.util.tuples.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@Service
public class UIRelatedHelpers
{
    @Autowired
    OsService os_service;
    @Autowired
    CodeGenerationService codeGen_service;
    @Autowired
    ZipService zip_service;

    public LineChartConfig getChartConfig(String chart_name)
    {
        LineChartConfig config = new LineChartConfig();
        config.data()
                .labels()
                .and();

        config.options()
                .responsive(true)
                .responsiveAnimationDuration(0)
                .title()
                .display(true)
                .position(Position.TOP)
                .text("Simulation Data Chart")
                .and()
                .animation() // Add this line
                .duration(0) // Set animation duration to 0
                .and()
                .done();
        config.
                options()
                .maintainAspectRatio(false)
                .scales()
                .and()
                .tooltips()
                .bodySpacing(10)
                .and()
                .done();

        return  config;
    }

    // a helper method to set up a SoChart LineChart instance
    public Pair<Data, Data> SoLineChartConfig(String chart_name, SOChart soChart, RectangularCoordinate rc){
        Data xValues = new Data(), yValues = new Data();

        LineChart lineChart = new LineChart(xValues, yValues);
        lineChart.setName(chart_name);
        PointSymbol ps = lineChart.getPointSymbol(true);
        ps.setType(PointSymbolType.NONE);
        lineChart.setSmoothness(true);
        lineChart.setAnimation(null);

        lineChart.plotOn(rc);

        soChart.add(lineChart);

        // Create a data channel to push the data
//        DataChannel dataChannel = new DataChannel(soChart, xValues, yValues);

        return new Pair<>(xValues, yValues) ;
    }

    public File generateModelJar(SimForm sim_data, File model_file) throws FileNotFoundException, IOException, Exception
    {
        // delete directory if it exists
        if (model_file.exists()){
            String remove_dir_cmd = "rm -r " + model_file.getName();
            String remove_zip_cmd = "rm " + model_file.getName() + ".zip";
            os_service.commandRunner(new File(model_file.getParent()).getPath(), remove_dir_cmd, 0);
            os_service.commandRunner(new File(model_file.getParent()).getPath(), remove_zip_cmd, 0);
            System.out.println("Deleted existing dir for model generation");
        }

        boolean isDirectoryCreated = model_file.mkdirs();
        // check if model directory creation failed
        if(!isDirectoryCreated)
        {
            System.out.println("Error in creating model directory");
            throw new RuntimeException("Model generation failed. Please try again later.");
        }

        // next: implement gradle project generation (check about best practises)
        String templates_path = "minireal_data" + File.separator + "template_files";
        String copy_temp_cmd = "cp -a " + templates_path + File.separator + ". " + model_file.getPath();
        Pair<Integer, String> copy_output = os_service.commandRunner(null, copy_temp_cmd, 0);
        System.out.println(copy_output.getB());
        // write to settings.gradle file
        String stg_gdl_code = "rootProject.name = '" + sim_data.getModel_name() + "'";
        codeGen_service.createSourceFile(model_file, "settings.gradle", stg_gdl_code);
        // proceed, only if the copying of the template files is successful
        if (copy_output.getA() != 0)
        {
            System.out.println("Error in placing template files.");
            throw new RuntimeException("Model generation failed. Please try again later.");
        }

        // using gradle wrapper to install gradle locally and generate required files
        String wrapper_init_cmd = "source /etc/profile.d/gradle.sh && gradle wrapper --gradle-version 8.4";
        Pair<Integer, String> wrapper_init_pair = os_service.commandRunner(model_file.getPath(), wrapper_init_cmd, 1);
        if (wrapper_init_pair.getA() != 0) {
            System.out.println("Error: Gradle wrapper initialization failed.");
            throw new RuntimeException("Model generation failed. Please try again later.");
        }

        // generate and save model & agent codes
        String source_path = model_file.getPath() + File.separator + "src" + File.separator + "main" + File.separator + "java";
        File source_dir = new File(source_path);
        JavaFile model_code = codeGen_service.generateBaseModel(sim_data);
        model_code.writeTo(source_dir);
        JavaFile agent_code = codeGen_service.generateBaseAgent(sim_data);
        agent_code.writeTo(source_dir);

        // zip model gradle project
        String zip_path = zip_service.createSimZip(model_file.getPath());
        File zip_file = new File(zip_path);
        FileInputStream zip_stream = new FileInputStream(zip_file);
        StreamResource zip_stream_resource = new StreamResource(zip_file.getName(), () -> zip_stream);

        return zip_file;
    }

    public String stringToRGB(String str) {
        StringBuilder hex = new StringBuilder();
        byte[] bytes = str.getBytes();
        for (byte b : bytes) {
            hex.append(String.format("%02X", b));
        }
//        System.out.println(hex.toString());

        int r = Integer.parseInt(hex.substring(1, 3), 16);
        int g = Integer.parseInt(hex.substring(3, 5), 16);
        int b = Integer.parseInt(hex.substring(5, 7), 16);
        String rgb_string =  "rgb(" + r + "," + g + "," + b + ")";
//        System.out.println(rgb_string);
        return rgb_string;
    }
}
