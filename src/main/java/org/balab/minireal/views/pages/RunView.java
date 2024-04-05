package org.balab.minireal.views.pages;

import com.google.gson.JsonParser;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.dom.DomEventListener;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import org.apache.commons.io.IOUtils;
import org.balab.minireal.data.entity.SimSession;
import org.balab.minireal.data.service.FileSystemService;
import org.balab.minireal.data.service.SimSessionService;
import org.balab.minireal.data.service.SimulationService;
import org.balab.minireal.data.service.StorageProperties;
import org.balab.minireal.security.AuthenticatedUser;
import org.balab.minireal.views.MainLayout;
import org.balab.minireal.views.components.DBView;
import org.balab.minireal.views.components.ParamView;
import org.balab.minireal.views.helpers.UIRelatedHelpers;
import org.vaadin.addons.chartjs.ChartJs;
import org.vaadin.addons.chartjs.config.LineChartConfig;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Route(value = "run", layout = MainLayout.class)
@PermitAll
@CssImport("./styles/upload-center-style.css")
public class RunView extends VerticalLayout
{
    // define services
    private final AuthenticatedUser authed_user;
    private final FileSystemService fs_service;
    private final StorageProperties storage_properties;
    private final SimSessionService sim_session_service;
    private final UIRelatedHelpers ui_helper_service;
    private final SimulationService sim_service;


    // define elements
    FlexLayout child_main_layout;
    VerticalLayout model_params_layout;
    VerticalLayout model_chart_settings_layout;
    private LineChartConfig config;
    private ChartJs chartJs;
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
    private ParamView param_view;
    private DBView db_view;

    public RunView(
            AuthenticatedUser authed_user,
            FileSystemService fs_service,
            StorageProperties storage_properties,
            UIRelatedHelpers ui_helper_service,
            SimSessionService sim_session_service,
            SimulationService sim_service
    ){
        // initialize services
        this.authed_user = authed_user;
        this.fs_service = fs_service;
        this.storage_properties = storage_properties;
        this.ui_helper_service = ui_helper_service;
        this.sim_session_service = sim_session_service;
        this.sim_service = sim_service;

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


    }

    public void setupComponents(){
        // create header
        VerticalLayout title_layout = new VerticalLayout(new H3("Run Model"));
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
        HorizontalLayout tick_layout = new HorizontalLayout(tick_label, tick_tf);
        tick_layout.setJustifyContentMode(JustifyContentMode.CENTER);
        tick_layout.setAlignItems(Alignment.CENTER);
        // setup buttons
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

        config = ui_helper_service.getChartConfig("Chart");
        chartJs = new ChartJs(config);
        chartJs.setWidthFull();
        chartJs.setHeight("75%");


        model_chart_settings_layout = new VerticalLayout(sim_settings_layout, chartJs);
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
            param_view = new ParamView(model_metaData);
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

        } catch (IOException e){
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
                // get param values and database checkbox value
                String param_json = param_view.getParamsValue();

                // todo: create sim ui data listener
//            ManualKafkaListener listener = new ManualKafkaListener(kafka_broker, "tick", tick_publisher);
//            Thread thread = new Thread(listener, sim_session.getToken());
//            thread.start();


                // run the simulation in a new thread
                new Thread(() -> {
                    try {
                        boolean is_sim_run = sim_service.runSimulation(model_uploaded_path, param_json, sim_session);

                        // UI updates should be run on the UI thread
                        getUI().ifPresent(ui -> ui.access(() -> {
                            if (is_sim_run) {
                                Notification.show("Simulation run successful").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                            }

                            // reset the chart
                            if (config.data().getDatasets() != null) {
                                config.data().getDatasets().clear();
                                config.data().getLabels().clear();
                            }
                        }));
                    } catch (IOException | InterruptedException e) {
                        getUI().ifPresent(ui -> ui.access(() -> {
                            Notification.show("Simulation failed").addThemeVariants(NotificationVariant.LUMO_ERROR);
                        }));
                        throw new RuntimeException(e);
                    }
                }).start();
            }
        } catch (Exception e) {
            Notification.show("Simulation failed").addThemeVariants(NotificationVariant.LUMO_ERROR);
            throw new RuntimeException(e);
        }
    }



}
