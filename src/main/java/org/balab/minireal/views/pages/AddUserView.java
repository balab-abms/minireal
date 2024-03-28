package org.balab.minireal.views.pages;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import org.balab.minireal.data.entity.Role;
import org.balab.minireal.data.entity.User;
import org.balab.minireal.data.service.FileSystemService;
import org.balab.minireal.data.service.StorageProperties;
import org.balab.minireal.data.service.UserService;
import org.balab.minireal.security.AuthenticatedUser;
import org.balab.minireal.views.MainLayout;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Route(value = "adduser", layout = MainLayout.class)
@RolesAllowed({"ADMIN", "OWNER"})
public class AddUserView extends VerticalLayout
{
    // define services
    private final AuthenticatedUser authed_user;
    private final UserService user_service;
    private final FileSystemService fs_service;
    private final StorageProperties storage_properties;

    // define elements
    FlexLayout child_main_layout;
    TextField username_tf;
    TextField name_tf;
    PasswordField passwd_pf;
    PasswordField confirm_passwd_pf;
    ComboBox<Role> user_role_combo;

    // define members
    Binder<User> user_form_binder;

    public AddUserView(
            AuthenticatedUser authed_user,
            UserService user_service,
            FileSystemService fs_service,
            StorageProperties storage_properties
    ){
        // initialize services
        this.authed_user = authed_user;
        this.user_service = user_service;
        this.fs_service = fs_service;
        this.storage_properties = storage_properties;

        // setup layout
        setSizeFull();
        child_main_layout = new FlexLayout();
        child_main_layout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        child_main_layout.addClassName(LumoUtility.Gap.LARGE);
        add(child_main_layout);

        // setup binder
        user_form_binder = new Binder<>(User.class);

        // create header
        VerticalLayout title_layout = new VerticalLayout(new H3("Add User"));
        title_layout.setJustifyContentMode(JustifyContentMode.START);
        title_layout.getStyle().set("padding", "12px");

        // create fields
        username_tf = new TextField();
        username_tf.setWidthFull();
        name_tf = new TextField();
        name_tf.setWidthFull();
        passwd_pf = new PasswordField();
        passwd_pf.setWidthFull();
        confirm_passwd_pf = new PasswordField();
        confirm_passwd_pf.setWidthFull();
        user_role_combo = new ComboBox<>();
        user_role_combo.setWidthFull();
        ArrayList<Role> roles_array = new ArrayList<>();
        roles_array.add(Role.USER);
        if(authed_user.get().get().getRoles().contains(Role.OWNER)){
            roles_array.add(Role.ADMIN);
            roles_array.add(Role.OWNER);
        }
        user_role_combo.setItems(roles_array);

        // create form add fields
        FormLayout user_form = new FormLayout();
        user_form.addFormItem(username_tf, "Username");
        user_form.addFormItem(name_tf, "Name");
        user_form.addFormItem(passwd_pf, "Password");
        user_form.addFormItem(confirm_passwd_pf, "Confirm Password");
        user_form.addFormItem(user_role_combo, "Role");
        user_form.setResponsiveSteps(
                // Use one column by default
                new FormLayout.ResponsiveStep("0", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP)
        );
        user_form.getStyle().set("padding", "12px");

        // bind model binder to related form elements
        user_form_binder.forField(username_tf).bind(User::getUsername, User::setUsername);
        user_form_binder.forField(name_tf).bind(User::getName, User::setName);

        // created buttons layout and buttons
        Button cancel_btn = new Button("Cancel", evnt -> UI.getCurrent().navigate(SamplesView.class));
        cancel_btn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancel_btn.getStyle().set("margin-inline-end", "auto");

        Button save_user_btn = new Button("Save");
        save_user_btn.addClickListener(event -> {
            try
            {
                // get the field values
                String username = username_tf.getValue();
                String name = name_tf.getValue().trim();
                String passwd = passwd_pf.getValue();
                String confirm_passwd = confirm_passwd_pf.getValue();

                if(!isFilledFormElements()){
                    Notification.show("Please fill all fields").addThemeVariants(NotificationVariant.LUMO_ERROR);
                    throw new RuntimeException("Error: all fields not filled.");
                }
                if(!passwd.equals(confirm_passwd)) {
                    Notification.show("Password & Confirm Password don't match.").addThemeVariants(NotificationVariant.LUMO_ERROR);
                    throw new RuntimeException("Passwords don't match");
                }

                User new_user = new User();
                new_user.setUsername(username);
                new_user.setName(name);
                BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
                new_user.setHashedPassword(passwordEncoder.encode(passwd));
                Set<Role> user_roles = new HashSet<>();
                user_roles.add(user_role_combo.getValue());
                new_user.setRoles(user_roles);

                // save user
                user_service.update(new_user);
                Notification.show("User successfully created.").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                UI.getCurrent().navigate(UsersListView.class);
            } catch (Exception exp) {
                Notification.show("User creation failed.").addThemeVariants(NotificationVariant.LUMO_ERROR);
                throw new RuntimeException("Error in user creation");
            }

        });
        save_user_btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);


        HorizontalLayout button_layout = new HorizontalLayout(cancel_btn, save_user_btn);
        button_layout.getStyle().set("flex-wrap", "wrap");
        button_layout.setJustifyContentMode(JustifyContentMode.END);
        button_layout.getStyle().set("padding", "12px");
        button_layout.setDefaultVerticalComponentAlignment(Alignment.CENTER);

        // add items to vertical Layout and set alignment
        child_main_layout.add(title_layout, user_form, button_layout);
        setAlignSelf(Alignment.CENTER, user_form);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
    }

    // a helper method to check if forms are filled
    private boolean isFilledFormElements()
    {
        return !username_tf.isEmpty() &&
                !name_tf.isEmpty() &&
                !passwd_pf.isEmpty() &&
                !confirm_passwd_pf.isEmpty() &&
                !user_role_combo.isEmpty();
    }
}
