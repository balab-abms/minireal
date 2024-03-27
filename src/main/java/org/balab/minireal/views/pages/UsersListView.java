package org.balab.minireal.views.pages;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
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
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import org.balab.minireal.data.entity.Role;
import org.balab.minireal.data.entity.User;
import org.balab.minireal.data.service.UserService;
import org.balab.minireal.security.AuthenticatedUser;
import org.balab.minireal.views.MainLayout;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Route(value = "users", layout = MainLayout.class)
@RolesAllowed({"ADMIN", "SUPER"})
public class UsersListView extends VerticalLayout
{
    // define services
    private final UserService user_service;
    private final AuthenticatedUser authed_user;
    // define elements
    FlexLayout child_main_layout;
    private Grid<User> grid;

    // define members
    User current_user;
    public UsersListView(
            AuthenticatedUser authed_user,
            UserService user_service
    )
    {
        // initialize services
        this.user_service = user_service;
        this.authed_user = authed_user;

        // setup layout
        setSizeFull();
        child_main_layout = new FlexLayout();
        child_main_layout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        child_main_layout.addClassName(LumoUtility.Gap.LARGE);
        child_main_layout.setMinWidth("50%");
        add(child_main_layout);

        // create header
        VerticalLayout title_layout = new VerticalLayout(new H3("Users List"));
        title_layout.setJustifyContentMode(JustifyContentMode.START);
        title_layout.getStyle().set("padding", "12px");

        if(authed_user.get().isPresent())
            current_user = authed_user.get().get();
        else
            authed_user.logout();

        // add items to vertical Layout and set alignment
        child_main_layout.add(title_layout);
//        setAlignSelf(Alignment.CENTER, );
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);

        setupGrid();
    }

    private void setupGrid() {
        grid = new Grid<>(User.class, false);
        grid.setAllRowsVisible(true);
        grid.addColumn(User::getUsername).setHeader("Username").setAutoWidth(true);
        grid.addColumn(User::getName).setHeader("Name").setAutoWidth(true);
        grid.addColumn(User::getRoles).setHeader("Roles").setAutoWidth(true);
        grid.addColumn(
                new ComponentRenderer<>(Button::new, (button, user) -> {
                    button.addThemeVariants(ButtonVariant.LUMO_ICON,
                            ButtonVariant.LUMO_TERTIARY);
                    button.addClickListener(e -> {
                        setupResetConfirmGrid(user);
                    });
                    button.setIcon(new Icon(VaadinIcon.REFRESH));
                    // disable the button if user does not have enough role
                    if(
                            (user.getRoles().contains(Role.ADMIN) ||
                                    (user.getRoles().contains(Role.SUPER))) &&
                                    !current_user.getRoles().contains(Role.SUPER)
                    ){
                        button.setEnabled(false);
                    }

                })).setHeader("Password Reset").setAutoWidth(true);
        grid.addColumn(
                new ComponentRenderer<>(Button::new, (button, user) -> {
                    button.addThemeVariants(ButtonVariant.LUMO_ICON,
                            ButtonVariant.LUMO_ERROR,
                            ButtonVariant.LUMO_TERTIARY);
                    button.addClickListener(e -> {
                        setupDeleteConfirmGrid(user);
                    });
                    button.setIcon(new Icon(VaadinIcon.TRASH));
                    // disable the button if user does not have enough role
                    if(
                            (user.getRoles().contains(Role.ADMIN) ||
                            (user.getRoles().contains(Role.SUPER))) &&
                            !current_user.getRoles().contains(Role.SUPER)
                    ){
                        button.setEnabled(false);
                    }

                })).setHeader("Delete").setAutoWidth(true);

        grid.setItems(user_service.getAll());
        grid.setWidthFull();

        child_main_layout.add(grid);
    }

    private ConfirmDialog setupDeleteConfirmGrid(User user) {
        ConfirmDialog delete_grid_dialog = new ConfirmDialog();
        delete_grid_dialog.setHeader("Delete User");
        delete_grid_dialog.setText(String.format("Do you want to delete user '%s'?", user.getUsername()));

        delete_grid_dialog.setCancelable(true);
        delete_grid_dialog.addCancelListener(event -> delete_grid_dialog.close());

        delete_grid_dialog.setRejectable(false);

        Button delete_user_btn = new Button("Delete");
        delete_user_btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        delete_user_btn.addClickListener(event -> {
            user_service.delete(user.getId());
            Notification.show("User Deleted successfully.").addThemeVariants(NotificationVariant.LUMO_PRIMARY);
            delete_grid_dialog.close();
            UI.getCurrent().getPage().reload();
        });
        delete_grid_dialog.setConfirmButton(delete_user_btn);
        delete_grid_dialog.open();

        return delete_grid_dialog;
    }

    private Dialog setupResetConfirmGrid(User user) {
        Dialog reset_grid_dialog = new Dialog();
        reset_grid_dialog.setWidth("400px");

        H4 dialog_title = new H4("Reset Password");
        NativeLabel reset_text_label = new NativeLabel(String.format("Reset password for User '%s'", user.getUsername()));
        PasswordField new_pwd_tf = new PasswordField("New Password");
        PasswordField new_pwd_confirm_tf = new PasswordField("Confirm Password");

        FormLayout reset_form = new FormLayout(reset_text_label, new_pwd_tf, new_pwd_confirm_tf);

        Button reset_close_btn  = new Button("Close", e -> reset_grid_dialog.close());
        reset_close_btn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        reset_close_btn.getStyle().set("margin-inline-end", "auto");

        Button reset_confirm_btn  = new Button("Reset");
        reset_confirm_btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        reset_confirm_btn.addClickListener(event -> {
            String new_passwd = new_pwd_tf.getValue();
            String new_passwd_confirm = new_pwd_confirm_tf.getValue();
            if(!new_passwd.isEmpty() && new_passwd.equals(new_passwd_confirm)){
                BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
                user.setHashedPassword(passwordEncoder.encode(new_passwd));
                Notification.show("User Password Updated successfully.").addThemeVariants(NotificationVariant.LUMO_PRIMARY);
                reset_grid_dialog.close();
            } else {
                Notification.show("Password & Confirm Password don't match.").addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        HorizontalLayout reset_buttons_layout = new HorizontalLayout(reset_close_btn, reset_confirm_btn);
        reset_buttons_layout.setSizeFull();
        reset_buttons_layout.getStyle().set("flex-wrap", "wrap");
        reset_buttons_layout.setJustifyContentMode(JustifyContentMode.END);
        reset_buttons_layout.setDefaultVerticalComponentAlignment(Alignment.CENTER);

        VerticalLayout reset_dialog_layout = new VerticalLayout(dialog_title, reset_form, reset_buttons_layout);
        reset_dialog_layout.setSizeFull();
        reset_buttons_layout.setSpacing(true);

        reset_grid_dialog.add(reset_dialog_layout);
        reset_grid_dialog.open();

        return reset_grid_dialog;
    }
}
