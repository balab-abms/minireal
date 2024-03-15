package org.balab.minireal.views;


import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.Lumo;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.theme.lumo.LumoUtility.FontSize;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;
import org.balab.minireal.data.service.FileSystemService;

public class MainLayout extends AppLayout {
    // define services
    private final FileSystemService fileSystem_service;

    // define layouts and components
    Div child_content;
    VerticalLayout footer;
    public MainLayout(
            FileSystemService fileSystem_service
    ) {
        this.fileSystem_service = fileSystem_service;

        // setup layouts
        child_content = new Div();
        child_content.setSizeFull();
        footer = new VerticalLayout();
        footer.setWidthFull();
        footer.setHeight("80px");
        footer.getStyle().set("padding", "12px 40px 12px 40px");

        VerticalLayout body = new VerticalLayout(child_content, footer);
        body.setFlexGrow(1, child_content);
        body.setSizeFull();
        setContent(body);

        setDrawerOpened(false);
        setupHeader();
        setupFooter();
    }

    private void setupHeader(){
        HorizontalLayout header_layout = new HorizontalLayout();
        header_layout.setSizeFull();
        header_layout.getStyle().set("background-color", "white");
        header_layout.getStyle().set("padding", "12px 40px 0px 40px");

        Image miniReal_logo = new Image(fileSystem_service.getImageResource("images/Logo.png"), "MiniReal Logo");
        miniReal_logo.setHeight("55px");
        header_layout.add(miniReal_logo);

        Hr horizontal_line = new Hr();
        horizontal_line.getStyle().set("background", "var(--lumo-contrast-10pct)");
        horizontal_line.getStyle().set("margin", "0px 80px 12px 80px");

        VerticalLayout nav_layout = new VerticalLayout(header_layout, horizontal_line);
        nav_layout.setSizeFull();
        nav_layout.getStyle().set("background-color", "white");

        addToNavbar(nav_layout);
    }

    // a helper method to set up the footer
    private void setupFooter()
    {
        Hr horizontal_line = new Hr();
        horizontal_line.getStyle().set("background", "var(--lumo-contrast-10pct)");
        horizontal_line.getStyle().set("margin", "0px 40px 0px 40px");

        Span copyright_footer = new Span("Copyright © 2024 BaLab");
        Span about_footer = new Span("About");
        HorizontalLayout footer_elts_layout = new HorizontalLayout(copyright_footer, about_footer);
        footer_elts_layout.setWidthFull();
        footer_elts_layout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        footer.add(horizontal_line, footer_elts_layout);
    }

    @Override
    public void showRouterLayoutContent(HasElement content)
    {
        // super.showRouterLayoutContent(content);

        /*
        if(content instanceof ModelListView) {
            menu_tabs.setSelectedTab(dashboard_tab);
        } else if (content instanceof ModelCreationView) {
            menu_tabs.setSelectedTab(create_tab);
        } else if (content instanceof MultipleChart) {
            menu_tabs.setSelectedTab(run_tab);
        }
        */


        // set the content part to the routed view
        child_content.removeAll();
        child_content.add((Component) content);
    }

}
