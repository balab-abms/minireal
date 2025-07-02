package org.balab.minireal.views.pages;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.storedobject.chart.*;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.dom.DomEventListener;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import org.apache.commons.io.IOUtils;
import org.balab.minireal.data.entity.SimSession;
import org.balab.minireal.data.service.FileSystemService;
import org.balab.minireal.data.service.SimSessionService;
import org.balab.minireal.data.service.SimulationService;
import org.balab.minireal.data.service.StorageProperties;
import org.balab.minireal.middleware.kafka.KafkaTopicDeleter;
import org.balab.minireal.middleware.kafka.listener.ChartListener;
import org.balab.minireal.middleware.kafka.listener.MultiTickListener;
import org.balab.minireal.security.AuthenticatedUser;
import org.balab.minireal.views.MainLayout;
import org.balab.minireal.views.components.DBView;
import org.balab.minireal.views.components.MultiParamView;
import org.balab.minireal.views.helpers.SImRelatedHelpers;
import org.balab.minireal.views.helpers.SimulationResult;
import org.balab.minireal.views.helpers.UIRelatedHelpers;
import org.springframework.beans.factory.annotation.Value;
import oshi.util.tuples.Pair;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@PageTitle("Batch Run Simulations")
@Route(value = "batch", layout = MainLayout.class)
@PermitAll
@CssImport("./styles/upload-center-style.css")
public class BatchRunView extends VerticalLayout
{
    // define services
    private final AuthenticatedUser authed_user;
    private final FileSystemService fs_service;
    private final StorageProperties storage_properties;
    private final SimSessionService sim_session_service;
    private final UIRelatedHelpers ui_helper_service;
    private final SimulationService sim_service;
    private final SImRelatedHelpers sim_helper_service;
    private final KafkaTopicDeleter kafka_topic_deleter_service;
    private final Flux<String> sim_session_del_subscriber;



    // define elements
    FlexLayout child_main_layout;
    VerticalLayout model_params_layout;
    VerticalLayout model_chart_settings_layout;
//    private LineChartConfig config;
//    private ChartJs chartJs;
    private SOChart soChart;
    private HashMap<String, Pair<Data, Data>> sochart_datachannels_list;
    Upload model_upload;
    H5 model_name_label, tick_label;
    TextField tick_tf;
    Button start_btn, stop_btn;
    public UI run_ui;

    // define members
    private SimSession sim_session;
    private MemoryBuffer file_buffer;
    byte[] model_data;
    private String model_uploaded_path, model_metaData, user_saved_dir;

    private Boolean is_model_ran;
    private MultiParamView param_view;
    private DBView db_view;
    // property values
    @Value("${spring.kafka.bootstrap-servers}")
    private String kafka_broker;
    private ArrayList<Boolean> sims_is_success_array;
    private ArrayList<Thread> sims_thread_array;

    public BatchRunView(
            AuthenticatedUser authed_user,
            FileSystemService fs_service,
            StorageProperties storage_properties,
            UIRelatedHelpers ui_helper_service,
            SImRelatedHelpers sim_helper_service,
            SimSessionService sim_session_service,
            SimulationService sim_service,
            KafkaTopicDeleter kafka_topic_deleter_service,
            Flux<String> sim_session_del_subscriber
    ){
        // initialize services
        this.authed_user = authed_user;
        this.fs_service = fs_service;
        this.storage_properties = storage_properties;
        this.ui_helper_service = ui_helper_service;
        this.sim_session_service = sim_session_service;
        this.sim_service = sim_service;
        this.sim_helper_service = sim_helper_service;
        this.kafka_topic_deleter_service = kafka_topic_deleter_service;
        this.sim_session_del_subscriber = sim_session_del_subscriber;

        // setup layout
        setSizeFull();
        child_main_layout = new FlexLayout();
        child_main_layout.setSizeFull();
        child_main_layout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        child_main_layout.addClassName(LumoUtility.Gap.LARGE);
        add(child_main_layout);

        // save the instance of the UI
        addAttachListener(event -> this.run_ui = event.getUI());

        if(authed_user.get().isPresent()){
            user_saved_dir = storage_properties.getUsers() + File.separator + authed_user.get().get().getId() +
                    File.separator + "models";
            // create sim simsession instance
            sim_session = sim_session_service.createSimSession(
                    authed_user.get().get(),
                    model_uploaded_path
            );
        } else {
            authed_user.logout();
        }

        // add title and main body components
        setupComponents();
        System.out.println(sim_session.getToken());

        // subscribe to sim-session delete channel
        sim_session_del_subscriber.subscribe(del_sim_session_token -> {
            if(sim_session.getToken().equals(del_sim_session_token)){
                run_ui.access(() -> {
                    Notification.show("Simulation run session token expired.").addThemeVariants(NotificationVariant.LUMO_ERROR);
                    UI.getCurrent().navigate(SamplesView.class);
                });

            }
        });

    }

    public void setupComponents(){
        // create header
        VerticalLayout title_layout = new VerticalLayout(new H3("Run Batch Models"));
        title_layout.setJustifyContentMode(JustifyContentMode.START);
        title_layout.getStyle().set("padding", "12px");

        file_buffer = new MemoryBuffer();
        model_upload = new Upload(file_buffer);
        model_upload.setAcceptedFileTypes("application/java-archive", ".jar");
        model_upload.setMinHeight("150px");
        model_upload.setWidthFull();
        model_upload.getStyle().set("display", "flex");
        model_upload.getStyle().set("flex-direction", "column");
        model_upload.getStyle().set("justify-content", "center");
        // model_upload.getStyle().set("align-items", "stretch");
        model_upload.addSucceededListener(event -> {
            modelUploadSuccess(event.getFileName());
        });
        // add a listener for file upload abort by user
        model_upload.getElement().addEventListener("upload-abort", new DomEventListener()
        {
            @Override
            public void handleEvent(DomEvent domEvent)
            {
                System.out.println("File Upload Aborted by User.");
                child_main_layout.removeAll();
                setupComponents();
                setSimulationButtons(false);
                model_name_label.setText("Model");
            }
        });

        model_params_layout = new VerticalLayout(model_upload);
        model_params_layout.setWidth("30%");
        model_params_layout.setHeightFull();
        model_params_layout.setAlignItems(Alignment.CENTER);

        model_name_label = new H5("Model");
        tick_label = new H5("Tick");
        tick_tf = new TextField();
        tick_tf.setEnabled(false);
        tick_tf.setWidth("75px");
        tick_tf.addThemeVariants(TextFieldVariant.LUMO_ALIGN_CENTER);
        HorizontalLayout tick_layout = new HorizontalLayout(tick_label, tick_tf);
        tick_layout.setJustifyContentMode(JustifyContentMode.CENTER);
        tick_layout.setAlignItems(Alignment.CENTER);
        // setup buttonsl
        start_btn = new Button("Start");
        start_btn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        start_btn.addClickListener(event -> {
            // start the simulation
            this.setStartButtonListener();
        });
        stop_btn = new Button("Stop");
        stop_btn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        stop_btn.addClickListener(event -> {
            // run the simulation in a new thread
            new Thread(() -> {
                // stop the simulation
                boolean is_sim_stopped = sim_service.stopSimulation(sim_session);
                getUI().ifPresent(ui -> ui.access(() -> {
                    if (is_sim_stopped) {
                        Notification.show("Simulation stopped").addThemeVariants(NotificationVariant.LUMO_PRIMARY);
                    } else {
                        Notification.show("No simulation running for this token").addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                }));
            }).start();

        });
        this.setSimulationButtons(false);
        HorizontalLayout sim_settings_layout = new HorizontalLayout(model_name_label, tick_layout, start_btn, stop_btn);
        sim_settings_layout.setWidthFull();
        sim_settings_layout.setFlexGrow(1, tick_layout);
        sim_settings_layout.setAlignItems(Alignment.CENTER);

        soChart = new SOChart();
        soChart.setSize("100%", "90%");
        sochart_datachannels_list = new HashMap<>();

        model_chart_settings_layout = new VerticalLayout(sim_settings_layout, soChart);
        model_chart_settings_layout.setWidth("70%");
        model_chart_settings_layout.setHeightFull();

        HorizontalLayout run_components_layout = new HorizontalLayout(model_params_layout, model_chart_settings_layout);
        run_components_layout.setSizeFull();

        child_main_layout.add(title_layout, run_components_layout);
    }
    // a helper function to handle the processing of uploaded files
    public void modelUploadSuccess(String file_name)
    {
        try
        {
            InputStream uploader_inputStream = file_buffer.getInputStream();
            byte[] uploaded_file_byte = IOUtils.toByteArray(uploader_inputStream);

            model_uploaded_path = user_saved_dir + File.separator + file_name;

            boolean is_model_saved = fs_service.saveFile(model_uploaded_path, uploaded_file_byte);
            if(!is_model_saved){
                System.out.println("File upload failed.");
                Notification.show("File upload failed.").addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            model_metaData = fs_service.getMetaData(model_uploaded_path);
            this.setSimulationButtons(true);
            // place model parameters on setting side layout
            param_view = new MultiParamView(model_metaData);
//            db_view = new DBView(model_metaData);
            model_params_layout.add(param_view);
            // update model name and path in sim_session entity
            String model_name = JsonParser.parseString(model_metaData)
                    .getAsJsonObject().get("modelDTO")
                    .getAsJsonObject().get("name")
                    .getAsString();
            sim_session.setModel_name(model_name);
            sim_session.setFile_path(model_uploaded_path);
            sim_session = sim_session_service.updateSimSession(sim_session);
            // update model name on UI
            model_name_label.setText(model_name);

            // clear existing data and chart
            soChart.removeAll();
            sochart_datachannels_list.clear();

            JsonArray model_charts = JsonParser.parseString(model_metaData)
                    .getAsJsonObject().get("chartDTOList").getAsJsonArray();
            // create the coordinate system to draw on
            XAxis xAxis = new XAxis(DataType.NUMBER);
            xAxis.setMinAsMinData();
            xAxis.setMaxAsMaxData();
            xAxis.setName("Ticks");
            YAxis yAxis = new YAxis(DataType.NUMBER);
            yAxis.setMinAsMinData();
            yAxis.setMaxAsMaxData();
            RectangularCoordinate rc = new RectangularCoordinate(xAxis, yAxis);
            soChart.update();
        } catch (IOException|ChartException e){
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    // a helper function to activate or deactivate simulation buttons
    public void setSimulationButtons(Boolean status)
    {
        start_btn.setEnabled(status);
        stop_btn.setEnabled(status);
    }

    public void setStartButtonListener() {
        try {
            if (!model_uploaded_path.isEmpty()) {
                // clear existing data and chart
                soChart.removeAll();
                sochart_datachannels_list.clear();

                JsonArray model_charts = JsonParser.parseString(model_metaData)
                        .getAsJsonObject().get("chartDTOList").getAsJsonArray();
                // create the coordinate system to draw on
                XAxis xAxis = new XAxis(DataType.NUMBER);
                xAxis.setName("Ticks");
                YAxis yAxis = new YAxis(DataType.NUMBER);
                yAxis.setMinAsMinData();
                yAxis.setMaxAsMaxData();
                RectangularCoordinate rc = new RectangularCoordinate(xAxis, yAxis);
                DataZoom rc_zoom = new DataZoom(rc, yAxis);

                // start the tick listener
                MultiTickListener multi_tick_listener = new MultiTickListener(kafka_broker, this.sim_session.getToken(), this.run_ui, this.tick_tf);
                Thread multi_tick_thread = new Thread(multi_tick_listener, "tick" + sim_session.getToken());
                multi_tick_thread.start();

                // start the chart listener
                ChartListener multi_chart_listener = new ChartListener(
                        ui_helper_service,
                        kafka_broker,
                        this.sim_session.getToken(),
                        this.run_ui,
                        soChart,
                        sochart_datachannels_list
                );
                Thread chart_thread = new Thread(multi_chart_listener, "chart" + sim_session.getToken());
                chart_thread.start();

                sim_session.set_running(true);
                sim_session.set_completed(false);
                sim_session.set_failed(false);
                sim_session = sim_session_service.updateSimSession(sim_session);

                // get param values & permutation of parameters
                String [] params_permutations = param_view.getParamsPermutation();
                sims_is_success_array = new ArrayList<>();
                sims_thread_array = new ArrayList<>();
                for(int params_idx = 0; params_idx < params_permutations.length; params_idx++){
                    // add charts for each parameter combination
                    String temp_comb_name = "comb" + params_idx;
                    for(JsonElement chart_elt: model_charts){
                        String temp_chart_name = chart_elt.getAsJsonObject().get("chartName").getAsString();
                        String updated_chart_name = String.format("%s_%s", temp_comb_name, temp_chart_name);
                        Pair<Data, Data> temp_datas = ui_helper_service.SoLineChartConfig(updated_chart_name, soChart, rc);
                        sochart_datachannels_list.put(updated_chart_name, temp_datas);
                    }
                    // run simulation
                    boolean temp_is_sim_success = simRunningHelper(params_permutations[params_idx], temp_comb_name);
                    sims_is_success_array.add(temp_is_sim_success);
                }
                soChart.add(rc_zoom);
                soChart.update();

                // implement a thread that waits for all the sim threads and update the ui
                Thread sim_done_waiter_thread = new Thread(() -> {
                    try {
                        // Wait for each simulation thread to finish
                        for (Thread simThread : sims_thread_array) {
                            simThread.join();
                        }
                        // All simulations done: check aggregated success flags
                        boolean allSuccess = sims_is_success_array.stream().allMatch(Boolean::booleanValue);

                        // Update the UI on Vaadin’s UI thread
                        run_ui.access(() -> {
                            if (allSuccess) {
                                sim_session.set_running(false);
                                sim_session.set_completed(true);
                                sim_session = sim_session_service.updateSimSession(sim_session);
                                try {
                                    for(Pair<Data, Data> temp_chart_datas: sochart_datachannels_list.values()){
                                        soChart.updateData(temp_chart_datas.getA(), temp_chart_datas.getB());
                                    }
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }

                                Notification.show("All simulations completed successfully",
                                                10000,
                                                Notification.Position.BOTTOM_START)
                                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                                // delete listener threads and kafka topics
                                sim_helper_service.deleteThreadsTopics(sim_session.getToken());
                            } else {
                                Notification.show("Some simulations failed",
                                                10000,
                                                Notification.Position.BOTTOM_START)
                                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                            }
                        });
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
                sim_done_waiter_thread.start();

            }
        } catch (Exception e) {
            Notification.show("Simulation failed").addThemeVariants(NotificationVariant.LUMO_ERROR);
            throw new RuntimeException(e);
        }
    }

    private boolean simRunningHelper(String param_json, String comb_name){
        AtomicBoolean is_sim_success_temp = new AtomicBoolean(true);
        // run the simulation in a new thread
        Thread temp_thread = new Thread(() -> {
            try {
                SimulationResult sim_result_data = sim_service.runSimulation(model_uploaded_path, param_json, sim_session, comb_name);
                is_sim_success_temp.set(sim_result_data.isSuccess());
            } catch (IOException | InterruptedException e) {
                getUI().ifPresent(ui -> ui.access(() -> {
                    sim_session.set_failed(true);
                    sim_session = sim_session_service.updateSimSession(sim_session);

                    String temp_toast_text = String.format("Simulation failed for combination %s", comb_name);
                    Notification.show(temp_toast_text).addThemeVariants(NotificationVariant.LUMO_ERROR);
                }));
                throw new RuntimeException(e);
            }
        });
        temp_thread.start();
        sims_thread_array.add(temp_thread);

        return is_sim_success_temp.get();
    }

    @Override
    protected void onDetach(DetachEvent detachEvent)
    {
        // delete listener threads and kafka topics
        sim_helper_service.deleteThreadsTopics(sim_session.getToken());

        super.onDetach(detachEvent);
    }


}
