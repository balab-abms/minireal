package org.balab.minireal.views.pages;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.annotation.security.PermitAll;
import org.balab.minireal.data.service.FileSystemService;
import org.balab.minireal.views.MainLayout;

@Route(value = "about", layout = MainLayout.class)
@AnonymousAllowed
public class AboutView extends VerticalLayout
{
    // define services
    private final FileSystemService file_system_service;
    public AboutView(FileSystemService file_system_service) {
        // initialize services
        this.file_system_service = file_system_service;

        // setup page layout
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setSpacing(true);

        Image plant_img = new Image(file_system_service.getImageResource("/META-INF/resources/images/empty-plant 1.png"), "plant image");
        plant_img.setWidth("240px");
        plant_img.setHeight("240px");
        plant_img.getStyle().set("margin-bottom", "36px");

        H2 title = new H2("About");

        Paragraph about_text = new Paragraph("SimReal is ... ");

        Button back_btn = new Button("Back", event -> {
            UI.getCurrent().getPage().getHistory().back();
        });

        add(plant_img, title, about_text, back_btn);
    }
}
