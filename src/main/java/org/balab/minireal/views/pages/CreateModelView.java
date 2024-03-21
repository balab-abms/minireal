package org.balab.minireal.views.pages;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.PropertyId;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import org.balab.minireal.data.entity.SimForm;
import org.balab.minireal.data.service.StorageProperties;
import org.balab.minireal.security.AuthenticatedUser;
import org.balab.minireal.views.MainLayout;
import sim.field.continuous.Continuous2D;
import sim.field.grid.Grid2D;
import sim.util.Bag;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Route(value = "create", layout = MainLayout.class)
@PermitAll
public class CreateModelView extends VerticalLayout
{
    // define services
    private StorageProperties storage_properties;
    private final AuthenticatedUser authenticated_user;

    // define elements
    FlexLayout child_main_layout;
    @PropertyId("name")
    TextField model_name;
    TextField field_name;
    ComboBox<Class> field_type;
    @PropertyId("name")
    TextField agent_name;
    NumberField agent_popln;
    Binder<SimForm> form_binder;

    // define members
    private UI form_ui;
    private String user_saved_dir;
    public CreateModelView(
            StorageProperties storage_properties,
            AuthenticatedUser authenticated_user
    ) {
        // initialize services
        this.storage_properties =storage_properties;
        this.authenticated_user = authenticated_user;

        // setup layout
//        setJustifyContentMode(JustifyContentMode.START);
//        setAlignItems(Alignment.CENTER);
        setSizeFull();
        child_main_layout = new FlexLayout();
        child_main_layout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        child_main_layout.addClassName(LumoUtility.Gap.LARGE);
        add(child_main_layout);


        // save the instance of the UI
        addAttachListener(event -> this.form_ui = event.getUI());
        // think about merging the ui and filesystem services
        user_saved_dir = storage_properties.getPath() + File.separator + authenticated_user.get().get().getId();

        // setup binder
        form_binder = new Binder<>(SimForm.class);

        // create header
        VerticalLayout title_layout = new VerticalLayout(new H3("Create Model"));
        title_layout.setJustifyContentMode(JustifyContentMode.START);
        title_layout.getStyle().set("padding", "12px");
        // create fields
        model_name = new TextField("Model Name");
        agent_name = new TextField("Agent Name");
        field_name = new TextField("Field Name");
        field_type = new ComboBox<>("Field Type");
        HashMap<Class, String> field_type_map = new HashMap<>();
        // correct this for the future ... retrieve from database and filter by user typed letters
        String[] field_types_array = {"Bag", "Grid2D", "Continuous2D"};
        ArrayList<Class> field_type_arrayList = new ArrayList<>(List.of(Bag.class, Grid2D.class, Continuous2D.class));
        for (int idx=0; idx<field_type_arrayList.size(); idx++)
        {
            String mmbr = field_types_array[idx];
            Class mmbr_path = field_type_arrayList.get(idx);
            field_type_arrayList.set(idx, mmbr_path);
            field_type_map.put(mmbr_path, mmbr);
        }
        field_type.setItems(field_type_arrayList);
        field_type.setItemLabelGenerator(itm -> field_type_map.get(itm));
        agent_popln = new NumberField("Agent Initial Population");

        // create form add fields
        FormLayout sim_form = new FormLayout();
        sim_form.add(model_name, agent_name, field_name, field_type, agent_popln);
        sim_form.setResponsiveSteps(
                // Use one column by default
                new FormLayout.ResponsiveStep("0", 1)
        );
        sim_form.getStyle().set("padding", "12px");

        // bind model binder to related form elements
        form_binder.forField(model_name).bind(SimForm::getModel_name, SimForm::setModel_name);
        form_binder.forField(agent_name).bind(SimForm::getAgent_name, SimForm::setAgent_name);
        form_binder.forField(field_name).bind(SimForm::getField_name, SimForm::setField_name);
        form_binder.forField(field_type).bind(SimForm::getField_type, SimForm::setField_type);
        form_binder.forField(agent_popln).bind(SimForm::getPopln, SimForm::setPopln);

        // created buttons layout and buttons
        Button cancel_btn = new Button("Cancel", evnt -> UI.getCurrent().navigate(SamplesView.class));
        cancel_btn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancel_btn.getStyle().set("margin-inline-end", "auto");

        Button generate_sim_btn = new Button("Generate");
        generate_sim_btn.addClickListener(event -> {
            if(isFilledFormElements())
            {
                // setup dialog
//                setupDialog();
//                zip_dialog.open();
//                download_thread = new Thread(this::downloadSim);
//                download_thread.start();
            } else {
                Notification.show("Please fill all form fields.").addThemeVariants(NotificationVariant.LUMO_ERROR);
            }

        });
        generate_sim_btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout button_layout = new HorizontalLayout(cancel_btn, generate_sim_btn);
        button_layout.getStyle().set("flex-wrap", "wrap");
        button_layout.setJustifyContentMode(JustifyContentMode.END);
        button_layout.getStyle().set("padding", "12px");
        button_layout.setDefaultVerticalComponentAlignment(Alignment.CENTER);

        // add items to vertical Layout and set alignment
        child_main_layout.add(title_layout, sim_form, button_layout);
        setAlignSelf(Alignment.CENTER, sim_form);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
    }

    // a helper method to check if forms are filled
    private boolean isFilledFormElements()
    {
        if(model_name.isEmpty() || field_name.isEmpty() || field_type.isEmpty() || agent_name.isEmpty() || agent_popln.isEmpty()) {
            return false;
        }
        return true;
    }
}
