package org.balab.minireal.views.pages;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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
import jakarta.annotation.security.PermitAll;
import org.apache.commons.io.IOUtils;
import org.balab.minireal.data.entity.User;
import org.balab.minireal.data.service.FileSystemService;
import org.balab.minireal.data.service.StorageProperties;
import org.balab.minireal.data.service.UserService;
import org.balab.minireal.security.AuthenticatedUser;
import org.balab.minireal.views.MainLayout;
import org.balab.minireal.views.components.AvatarUpdateToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Route(value = "profile", layout = MainLayout.class)
@PermitAll
public class ProfileView extends VerticalLayout
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

    Upload profile_pic_upload;

    // define members
    Binder<User> user_form_binder;
    private MemoryBuffer file_buffer;
    byte[] profile_pic_data;
    String pic_file_name, user_saved_dir;

    public ProfileView(
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

        if(authed_user.get().isEmpty()){
            authed_user.logout();
        }

        // setup layout
        setSizeFull();
        child_main_layout = new FlexLayout();
        child_main_layout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        child_main_layout.addClassName(LumoUtility.Gap.LARGE);
        add(child_main_layout);

        // setup binder
        user_form_binder = new Binder<>(User.class);

        user_saved_dir = storage_properties.getUsers() + File.separator + authed_user.get().get().getId();
        profile_pic_data = null;
        pic_file_name = null;

        // create header
        VerticalLayout title_layout = new VerticalLayout(new H3("User Profile"));
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
        file_buffer = new MemoryBuffer();
        profile_pic_upload = new Upload(file_buffer);
        profile_pic_upload.setAcceptedFileTypes("image/jpeg", "image/png", ".jpeg", ".jpg", ".png");
        profile_pic_upload.addSucceededListener(event -> {
            try
            {
                InputStream uploader_inputStream = file_buffer.getInputStream();
                profile_pic_data = IOUtils.toByteArray(uploader_inputStream);
                pic_file_name = event.getFileName();
            } catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        });
        int maxFileSizeInBytes = 100 * 1024 * 1024; // 10MB
        profile_pic_upload.setMaxFileSize(maxFileSizeInBytes);

        profile_pic_upload.addFileRejectedListener(event -> {
            String errorMessage = event.getErrorMessage();

            Notification notification = Notification.show(errorMessage);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        });

        // create form add fields
        FormLayout user_form = new FormLayout();
        user_form.addFormItem(username_tf, "Username");
        user_form.addFormItem(name_tf, "Name");
        user_form.addFormItem(passwd_pf, "Password");
        user_form.addFormItem(confirm_passwd_pf, "Confirm Password");
        user_form.addFormItem(profile_pic_upload, "Profile Picture");
        user_form.setResponsiveSteps(
                // Use one column by default
                new FormLayout.ResponsiveStep("0", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP)
        );
        user_form.getStyle().set("padding", "12px");

        // bind model binder to related form elements
        user_form_binder.forField(username_tf).bind(User::getUsername, User::setUsername);
        user_form_binder.forField(name_tf).bind(User::getName, User::setName);

        // update the form fields (username, name)
        user_form_binder.readBean(authed_user.get().get());

        // created buttons layout and buttons
        Button cancel_btn = new Button("Cancel", evnt -> UI.getCurrent().navigate(SamplesView.class));
        cancel_btn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancel_btn.getStyle().set("margin-inline-end", "auto");

        Button update_sim_btn = new Button("Update");
        update_sim_btn.addClickListener(event -> {
            // get the field values
            String username = username_tf.getValue();
            String name = name_tf.getValue().trim();
            String passwd = passwd_pf.getValue();
            String confirm_passwd = confirm_passwd_pf.getValue();

            User current_user = authed_user.get().get();
            // update the user data
            if(isFilledFormElements()){
                current_user.setUsername(username);
                current_user.setName(name);
            } else {
                Notification.show("Please enter Username and Name fields").addThemeVariants(NotificationVariant.LUMO_ERROR);
                throw new RuntimeException("Please enter Username and Name fields");
            }
            // update pasword
            if(!passwd.isEmpty() && passwd.equals(confirm_passwd)) {
                BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
                current_user.setHashedPassword(passwordEncoder.encode(passwd));
            } else if(!passwd.equals(confirm_passwd)) {
                Notification.show("Password & Confirm Password don't match.").addThemeVariants(NotificationVariant.LUMO_ERROR);
                throw new RuntimeException("Passwords don't match");
            }
            // update profile pic
            if(profile_pic_data != null){
                Long user_id = authed_user.get().get().getId();
                String pic_path = user_saved_dir + File.separator + "profile_pics" + File.separator + pic_file_name;
                boolean pic_saved = fs_service.saveFile(pic_path, profile_pic_data);
                if(pic_saved)
                {
                    current_user.setProfilePath(pic_path);
                    UI.getCurrent().getSession().setAttribute(AvatarUpdateToken.class, new AvatarUpdateToken(this.toString()));
                }
            }
            // update user
            user_service.update(current_user);
            Notification.show("User data successfully updated.").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            if(authed_user.get().isPresent()){
                UI.getCurrent().navigate("/");
            } else {
                authed_user.logout();
            }

        });
        update_sim_btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout button_layout = new HorizontalLayout(cancel_btn, update_sim_btn);
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
        return !username_tf.isEmpty() && !name_tf.isEmpty();
    }
}
