package org.balab.minireal.views.pages;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import org.apache.commons.io.IOUtils;
import org.balab.minireal.security.AuthenticatedUser;
import org.balab.minireal.views.MainLayout;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Route(value = "addsample", layout = MainLayout.class)
@RolesAllowed({"ADMIN", "OWNER"})
public class AddSampleView extends VerticalLayout {
    // define services
    private final AuthenticatedUser authed_user;

    // define elements
    FlexLayout child_main_layout;
    TextField model_tf;
    MultiSelectComboBox<String> agent_combo;
    MultiSelectComboBox<String> field_combo;
    Upload model_file_upload;

    // define members
    private MemoryBuffer file_buffer;
    byte[] model_data;
    String model_file_name;
    public AddSampleView(
            AuthenticatedUser authed_user
    ) {
        this.authed_user = authed_user;

        // setup layout
        setSizeFull();
        child_main_layout = new FlexLayout();
        child_main_layout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        child_main_layout.addClassName(LumoUtility.Gap.LARGE);
        add(child_main_layout);

        model_data = null;
        model_file_name = null;

        // create header
        VerticalLayout title_layout = new VerticalLayout(new H3("Add Sample Model"));
        title_layout.setJustifyContentMode(JustifyContentMode.START);
        title_layout.getStyle().set("padding", "12px");

        // create fields
        model_tf = new TextField();
        model_tf.setWidthFull();

        Set<String> agentValues = new HashSet<>();
        agentValues.add("Agent");
        agent_combo = new MultiSelectComboBox<>();
        agent_combo.setAllowCustomValue(true);
        agent_combo.setWidthFull();
        agent_combo.addCustomValueSetListener(event -> {
            // Add the new value to the current selection
            Set<String> currentSelection = new HashSet<>(agent_combo.getValue());
            currentSelection.add(event.getDetail());
            // Add the custom value to the items
            agentValues.add(event.getDetail());
            agent_combo.setItems(agentValues);
            agent_combo.setValue(currentSelection);
        });
        agent_combo.setItems(agentValues);

        String[] field_types_array = {"Bag", "Grid2D", "Continuous2D"};
        Set<String> fieldValues = new HashSet<>();
        fieldValues.addAll(Arrays.stream(field_types_array).toList());
        field_combo = new MultiSelectComboBox<>();
        field_combo.setAllowCustomValue(true);
        field_combo.setWidthFull();
        field_combo.addCustomValueSetListener(event -> {
            // Add the new value to the current selection
            Set<String> currentSelection = new HashSet<>(field_combo.getValue());
            currentSelection.add(event.getDetail());
            // Add the custom value to the items
            fieldValues.add(event.getDetail());
            field_combo.setItems(fieldValues);
            field_combo.setValue(currentSelection);
        });
        field_combo.setItems(fieldValues);

        file_buffer = new MemoryBuffer();
        model_file_upload = new Upload(file_buffer);
        model_file_upload.setAcceptedFileTypes(".zip");
        model_file_upload.addSucceededListener(event -> {
            try
            {
                InputStream uploader_inputStream = file_buffer.getInputStream();
                model_data = IOUtils.toByteArray(uploader_inputStream);
                model_file_name = event.getFileName();
            } catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        });

        // create form add fields
        FormLayout sample_form = new FormLayout();
        sample_form.addFormItem(model_tf, "Model Name");
        sample_form.addFormItem(agent_combo, "Agent Names");
        sample_form.addFormItem(field_combo, "Field Types");
        sample_form.addFormItem(model_file_upload, "Model File");
        sample_form.setResponsiveSteps(
                // Use one column by default
                new FormLayout.ResponsiveStep("0", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP)
        );
        sample_form.getStyle().set("padding", "12px");

        // created buttons layout and buttons
        Button cancel_btn = new Button("Cancel", evnt -> UI.getCurrent().navigate(SamplesView.class));
        cancel_btn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancel_btn.getStyle().set("margin-inline-end", "auto");

        Button save_model_btn = new Button("Save");
        save_model_btn.addClickListener(event -> {

        });
        save_model_btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout button_layout = new HorizontalLayout(cancel_btn, save_model_btn);
        button_layout.getStyle().set("flex-wrap", "wrap");
        button_layout.setJustifyContentMode(JustifyContentMode.END);
        button_layout.getStyle().set("padding", "12px");
        button_layout.setDefaultVerticalComponentAlignment(Alignment.CENTER);

        // add items to vertical Layout and set alignment
        child_main_layout.add(title_layout, sample_form, button_layout);
        child_main_layout.setAlignSelf(Alignment.CENTER, sample_form);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);

    }
}
