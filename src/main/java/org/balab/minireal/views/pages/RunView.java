package org.balab.minireal.views.pages;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import org.balab.minireal.data.service.FileSystemService;
import org.balab.minireal.data.service.StorageProperties;
import org.balab.minireal.security.AuthenticatedUser;
import org.balab.minireal.views.MainLayout;
import org.balab.minireal.views.helpers.UIRelatedHelpers;
import org.vaadin.addons.chartjs.ChartJs;
import org.vaadin.addons.chartjs.config.LineChartConfig;

@Route(value = "run", layout = MainLayout.class)
@PermitAll
@CssImport("./styles/upload-center-style.css")
public class RunView extends VerticalLayout
{
    // define services
    private final AuthenticatedUser authed_user;
    private final FileSystemService fs_service;
    private final StorageProperties storage_properties;
    private final UIRelatedHelpers ui_helper_service;


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

    // define members
    private MemoryBuffer file_buffer;
    byte[] model_data;
    private String model_uploaded_path, model_metaData, user_saved_dir;

    private Boolean is_model_ran;

    public RunView(
            AuthenticatedUser authed_user,
            FileSystemService fs_service,
            StorageProperties storage_properties,
            UIRelatedHelpers ui_helper_service
    ){
        // initialize services
        this.authed_user = authed_user;
        this.fs_service = fs_service;
        this.storage_properties = storage_properties;
        this.ui_helper_service = ui_helper_service;

        // setup layout
        setSizeFull();
        child_main_layout = new FlexLayout();
        child_main_layout.setSizeFull();
        child_main_layout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        child_main_layout.addClassName(LumoUtility.Gap.LARGE);
        add(child_main_layout);

        // create header
        VerticalLayout title_layout = new VerticalLayout(new H3("Run Model"));
        title_layout.setJustifyContentMode(JustifyContentMode.START);
        title_layout.getStyle().set("padding", "12px");
        // add title and main body components
        child_main_layout.add(title_layout, setupComponents());



    }

    public HorizontalLayout setupComponents(){
        file_buffer = new MemoryBuffer();
        model_upload = new Upload(file_buffer);
        model_upload.setAcceptedFileTypes("application/java-archive", ".jar");
        model_upload.setMinHeight("150px");
        model_upload.setWidthFull();
        model_upload.getStyle().set("display", "flex");
        model_upload.getStyle().set("justify-content", "center");
        model_upload.getStyle().set("align-items", "center");
        model_upload.addSucceededListener(event -> {
//            modelUploadSuccess(event.getFileName());
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

        });
        stop_btn = new Button("Stop");
        stop_btn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        stop_btn.addClickListener(event -> {

        });
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

        return run_components_layout;

    }
}
