package org.balab.minireal.views.pages;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
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

        Image minireal_header = new Image(file_system_service.getFileResource("minireal_data/assets/minireal-header-img.png"), "minireal header image");
        minireal_header.setWidth("800px");
        minireal_header.getStyle().set("margin-bottom", "36px");
        H2 title = new H2("WSim4ABM (MiniReal)");

        String simple_intro = "This is a WebService implementation for an Agent-based Modeling Simulator, shortly called WSim4ABM or MiniReal.\n" +
                "This opensource project houses remote access to High Performance Computing (HPC) resources through \n" +
                "browser based visualization for ABM simulations along with other services.";
        Paragraph about_text = new Paragraph(simple_intro);
        about_text.setWidth("600px");

        Button doc_link_btn = new Button("Documentation");
        doc_link_btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        doc_link_btn.addClickListener(event -> {
            UI.getCurrent().getPage().open("https://central.sonatype.com/artifact/io.github.panderior/minireal-annotation", "_blank");
        });
        Button repo_link_btn = new Button("Source Code");
        repo_link_btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        repo_link_btn.addClickListener(event -> {
            UI.getCurrent().getPage().open("https://central.sonatype.com/artifact/io.github.panderior/minireal-annotation", "_blank");
        });
        HorizontalLayout link_btns_layout = new HorizontalLayout(doc_link_btn, repo_link_btn);
        link_btns_layout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        link_btns_layout.setMinWidth("600px");
        link_btns_layout.getStyle().set("margin-bottom", "36px");

        Button back_btn = new Button("Back", event -> {
            UI.getCurrent().getPage().getHistory().back();
        });

        add(minireal_header, title, about_text, link_btns_layout, back_btn);
    }
}
