package org.balab.minireal.views.pages;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.DataProviderListener;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import org.balab.minireal.data.entity.Role;
import org.balab.minireal.data.entity.SampleModel;
import org.balab.minireal.data.entity.User;
import org.balab.minireal.data.service.SampleModelService;
import org.balab.minireal.data.service.UserService;
import org.balab.minireal.security.AuthenticatedUser;
import org.balab.minireal.views.MainLayout;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.vaadin.olli.FileDownloadWrapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;


@Route(value = "sample", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@PageTitle("Sample Models")
@PermitAll
@CssImport("./styles/grid-message-when-empty.css")
public class SamplesView extends VerticalLayout
{
    // define services
    private final UserService user_service;
    private final AuthenticatedUser authed_user;
    private final SampleModelService samples_service;
    // define elements
    FlexLayout child_main_layout;
    private Grid<SampleModel> grid;

    public SamplesView(
            AuthenticatedUser authed_user,
            UserService user_service,
            SampleModelService samples_service
    )
    {
        // initialize services
        this.user_service = user_service;
        this.authed_user = authed_user;
        this.samples_service = samples_service;

        // setup layout
        setSizeFull();
        addClassName("grid-message");
        child_main_layout = new FlexLayout();
        child_main_layout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        child_main_layout.addClassName(LumoUtility.Gap.LARGE);
        child_main_layout.setMinWidth("50%");
        add(child_main_layout);

        // create header
        H3 page_title = new H3("Sample Models");
        HorizontalLayout title_layout = new HorizontalLayout(page_title);
        title_layout.setJustifyContentMode(JustifyContentMode.START);
        title_layout.getStyle().set("padding", "12px");
        title_layout.getStyle().set("flex-wrap", "wrap");
        title_layout.setAlignItems(Alignment.CENTER);

        // add sample button
        if(authed_user.get().get().getRoles().contains(Role.OWNER) ||
            authed_user.get().get().getRoles().contains(Role.ADMIN)){
            Button add_sample_btn = new Button("Add Sample", e -> UI.getCurrent().navigate(AddSampleView.class));
            add_sample_btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            add_sample_btn.getStyle().set("margin-inline-start", "auto");
            title_layout.add(add_sample_btn);
        }

        setupGrid();
        updateGrid();

        // if grid is empty ... show message
        Div gridRoot = new Div();
        gridRoot.addClassName("grid-root");
        Div warning = new Div(new Text("There are no sample models to display."));
        warning.addClassName("warning");
        gridRoot.setWidthFull();
        gridRoot.add(grid, warning);

        DataProvider dataProvider = grid.getDataProvider();
        DataProviderListener<SampleModel> listener = (
                e -> {
                    if (dataProvider.size(new Query<>()) == 0) {
                        warning.removeClassName("hidden");
                    } else {
                        warning.addClassName("hidden");
                    }
                }
        );

        dataProvider.addDataProviderListener(listener);
        // Initial run of the listener, as there is no event fired for the initial state
        // of the data set that might be empty or not.
        listener.onDataChange(null);

        // add items to vertical Layout and set alignment
        child_main_layout.add(title_layout, gridRoot);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
    }

    private void setupGrid() {
        grid = new Grid<>(SampleModel.class, false);
        grid.addClassName("grid");
        grid.setAllRowsVisible(true);
        grid.addColumn(SampleModel::getModel).setHeader("Model").setAutoWidth(true);
        grid.addColumn(sampleModel -> {
            String agents_str = Arrays.toString(sampleModel.getAgents());
            return agents_str.substring(1, agents_str.length()-1);
        }).setHeader("Agents").setAutoWidth(true);
        grid.addColumn(sampleModel -> {
            String fields_str = Arrays.toString(sampleModel.getFields());
            return fields_str.substring(1, fields_str.length()-1);
        }).setHeader("Fields").setAutoWidth(true);
        grid.addColumn(
                new ComponentRenderer<>(sample -> {
                    Button download_btn = new Button();
                    download_btn.addThemeVariants(ButtonVariant.LUMO_ICON);
                    download_btn.setIcon(new Icon(VaadinIcon.DOWNLOAD));

                    File model_file = new File(sample.getPath());
                    FileDownloadWrapper sim_download_wrapper = new FileDownloadWrapper(model_file.getName(), model_file);
                    sim_download_wrapper.wrapComponent(download_btn);

                    return sim_download_wrapper;
                })).setHeader("Download").setAutoWidth(true);
        if(authed_user.get().get().getRoles().contains(Role.OWNER) ||
                authed_user.get().get().getRoles().contains(Role.ADMIN)){
            grid.addColumn(
                    new ComponentRenderer<>(sample -> {
                        Button delete_btn = new Button();
                        delete_btn.addThemeVariants(ButtonVariant.LUMO_ICON,
                                ButtonVariant.LUMO_ERROR);
                        delete_btn.addClickListener(e -> {
                            setupDeleteConfirmGrid(sample);
                        });
                        delete_btn.setIcon(new Icon(VaadinIcon.TRASH));
                        return delete_btn;

                    })).setHeader("Delete").setAutoWidth(true);
        }

        grid.setWidthFull();
    }

    public void updateGrid(){
        grid.setItems(samples_service.getAllSampleModels());
    }

    private ConfirmDialog setupDeleteConfirmGrid(SampleModel sample_model) {
        ConfirmDialog delete_grid_dialog = new ConfirmDialog();
        delete_grid_dialog.setHeader("Delete Sample Model");
        delete_grid_dialog.setText(String.format("Do you want to delete model '%s'?", sample_model.getModel()));

        delete_grid_dialog.setCancelable(true);
        delete_grid_dialog.addCancelListener(event -> delete_grid_dialog.close());

        delete_grid_dialog.setRejectable(false);

        Button delete_user_btn = new Button("Delete");
        delete_user_btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        delete_user_btn.addClickListener(event -> {
            try
            {
                samples_service.deleteSampleModel(sample_model.getId());
                Notification.show("Sample Model Deleted successfully.").addThemeVariants(NotificationVariant.LUMO_PRIMARY);
                delete_grid_dialog.close();
                UI.getCurrent().getPage().reload();
            } catch (IOException e)
            {
                Notification.show("Error in deleting User.").addThemeVariants(NotificationVariant.LUMO_ERROR);
                throw new RuntimeException(e);
            }
        });
        delete_grid_dialog.setConfirmButton(delete_user_btn);
        delete_grid_dialog.open();

        return delete_grid_dialog;
    }
}
