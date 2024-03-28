package org.balab.minireal.views.pages;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import org.balab.minireal.data.entity.Role;
import org.balab.minireal.data.entity.SampleModel;
import org.balab.minireal.data.service.UserService;
import org.balab.minireal.security.AuthenticatedUser;
import org.balab.minireal.views.MainLayout;

@Route(value = "sample", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@PermitAll
public class SamplesView extends VerticalLayout
{
    // define services
    private final UserService user_service;
    private final AuthenticatedUser authed_user;
    // define elements
    FlexLayout child_main_layout;
    private Grid<SampleModel> grid;

    public SamplesView(
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
        H3 page_title = new H3("Sample Models");
        Button add_sample_btn = new Button("Add Sample");
        add_sample_btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        add_sample_btn.getStyle().set("margin-inline-start", "auto");
        HorizontalLayout title_layout = new HorizontalLayout(page_title, add_sample_btn);
        title_layout.setJustifyContentMode(JustifyContentMode.START);
        title_layout.getStyle().set("padding", "12px");
        title_layout.getStyle().set("flex-wrap", "wrap");
        title_layout.setAlignItems(Alignment.CENTER);

        // add items to vertical Layout and set alignment
        child_main_layout.add(title_layout);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);

        setupGrid();
    }

    private void setupGrid() {
        grid = new Grid<>(SampleModel.class, false);
        grid.setAllRowsVisible(true);
        grid.addColumn(SampleModel::getModel).setHeader("Model").setAutoWidth(true);
        grid.addColumn(SampleModel::getAgents).setHeader("Agents").setAutoWidth(true);
        grid.addColumn(SampleModel::getFields).setHeader("Fields").setAutoWidth(true);
        grid.addColumn(
                new ComponentRenderer<>(Button::new, (button, user) -> {
                    button.addThemeVariants(ButtonVariant.LUMO_ICON);
                    button.addClickListener(e -> {
//                        setupResetConfirmGrid(user);
                    });
                    button.setIcon(new Icon(VaadinIcon.DOWNLOAD));

                })).setHeader("Download").setAutoWidth(true);
        if(authed_user.get().get().getRoles().contains(Role.OWNER) ||
                authed_user.get().get().getRoles().contains(Role.ADMIN)){
            grid.addColumn(
                    new ComponentRenderer<>(Button::new, (button, user) -> {
                        button.addThemeVariants(ButtonVariant.LUMO_ICON,
                                ButtonVariant.LUMO_ERROR);
                        button.addClickListener(e -> {
//                        setupDeleteConfirmGrid(user);
                        });
                        button.setIcon(new Icon(VaadinIcon.TRASH));

                    })).setHeader("Delete").setAutoWidth(true);
        }


//        grid.setItems(user_service.getAll());
        grid.setWidthFull();

        child_main_layout.add(grid);
    }
}
