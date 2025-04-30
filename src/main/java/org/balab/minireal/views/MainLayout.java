package org.balab.minireal.views;


import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.balab.minireal.data.entity.Role;
import org.balab.minireal.data.entity.User;
import org.balab.minireal.data.service.MinirealConfigGetter;
import org.balab.minireal.data.service.FileSystemService;
import org.balab.minireal.security.AuthenticatedUser;
import org.balab.minireal.views.components.AvatarUpdateToken;
import org.balab.minireal.views.pages.*;
import org.vaadin.lineawesome.LineAwesomeIcon;


public class MainLayout extends AppLayout
{
    // define services
    private final FileSystemService fileSystem_service;
    private final AuthenticatedUser authenticatedUser;
    private final MinirealConfigGetter minirealConfigGetter;


    // define layouts and components
    FlexLayout child_content;
    VerticalLayout footer;
    private H1 viewTitle;
    private MenuBar avatar_menu;

    public MainLayout(
            FileSystemService fileSystem_service,
            AuthenticatedUser authenticatedUser,
            MinirealConfigGetter minirealConfigGetter
    ) {
        this.fileSystem_service = fileSystem_service;
        this.authenticatedUser = authenticatedUser;
        this.minirealConfigGetter = minirealConfigGetter;

        // setup layouts
        child_content = new FlexLayout();
//        child_content.setSizeFull();
        child_content.setWidthFull();
        child_content.setHeight("min-content");
        child_content.getStyle().set("overflow-y", "auto");
        footer = new VerticalLayout();
        footer.setAlignItems(FlexComponent.Alignment.CENTER);
        footer.setWidthFull();
        footer.setHeight("80px");
        footer.getStyle().set("padding", "12px 40px 4px 40px");

        VerticalLayout body = new VerticalLayout(child_content, footer);
        body.setFlexGrow(1, child_content);
        body.setSizeFull();
        body.addClassName(LumoUtility.Gap.SMALL);
        setContent(body);

//        setDrawerOpened(true);
        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        setupHeader();
//        setupFooter();
    }

    private void addDrawerContent() {
        Image miniReal_logo = new Image(fileSystem_service.getImageResource("/META-INF/resources/images/minireal_logo_updated(2x).png"), "MiniReal Logo");
        miniReal_logo.setHeight("50px");
        miniReal_logo.getStyle().set("margin-bottom", "16px");


        H2 appName = new H2("WSim4ABM");
        appName.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.FontSize.MEDIUM);
        appName.getStyle().set("margin-bottom", "24px");

        VerticalLayout header_layout = new VerticalLayout(miniReal_logo, appName);
        header_layout.setWidthFull();
        header_layout.setAlignItems(FlexComponent.Alignment.CENTER);
        header_layout.setSpacing(false);
        header_layout.setPadding(false);

        VerticalLayout drawer_layout = new VerticalLayout(header_layout, createNavigation());
        drawer_layout.setSpacing(true);
        drawer_layout.getStyle().set("gap", "24px");
        drawer_layout.setSizeFull();
        drawer_layout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        drawer_layout.setAlignItems(FlexComponent.Alignment.CENTER);
        drawer_layout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        Scroller scroller = new Scroller(drawer_layout);

        String version_text = "Version: " + this.minirealConfigGetter.getProjectVersion();
        Span copyright_footer = new Span(version_text);
        copyright_footer.getElement().getStyle().set("display", "flex");
        copyright_footer.getElement().getStyle().set("align-items", "center");

        VerticalLayout parent_drawer_layout = new VerticalLayout(scroller, copyright_footer);
        parent_drawer_layout.setSizeFull();
        parent_drawer_layout.setAlignItems(FlexComponent.Alignment.CENTER);
        parent_drawer_layout.setFlexGrow(1, scroller);

        addToDrawer(parent_drawer_layout);
    }

    private void setupHeader(){
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");

        viewTitle = new H1();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
        viewTitle.getStyle().set("text-align", "center");
        viewTitle.setWidthFull();

        HorizontalLayout header_layout = new HorizontalLayout();
        header_layout.setSizeFull();
        header_layout.getStyle().set("background-color", "white");
        header_layout.getStyle().set("padding", "4px 40px 0px 40px");
        header_layout.setAlignItems(FlexComponent.Alignment.CENTER);

        Image miniReal_logo = new Image(fileSystem_service.getImageResource("/META-INF/resources/images/minireal_logo_updated(2x).png"), "MiniReal Logo");
        miniReal_logo.setHeight("45px");
        header_layout.add(miniReal_logo);
        miniReal_logo.addClickListener(event -> {
            UI.getCurrent().navigate("/");
        });


        // display the rest of nav elements if the user is logged in
        if (authenticatedUser.get().isPresent()) {
            // setup the nav menu items
//            HorizontalLayout nav_menu_layout = new HorizontalLayout();
//            nav_menu_layout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
//            nav_menu_layout.setWidthFull();
//            nav_menu_layout.getStyle().set("background-color", "white"); // Set the background color
//            nav_menu_layout.getStyle().set("padding", "0px 40px 0px 40px");
//            nav_menu_layout.setSpacing(true);
//            header_layout.add(nav_menu_layout);
//            header_layout.setFlexGrow(1, nav_menu_layout);
//
//            MenuBar nav_menu = new MenuBar();
//            nav_menu.addThemeVariants(MenuBarVariant.LUMO_ICON, MenuBarVariant.LUMO_TERTIARY);
//
//            MenuItem starter_item = nav_menu.addItem(new H4("Starter"));
//            starter_item.getStyle().set("width", "160px");
//            starter_item.getStyle().set("color", "black");
//            starter_item.add(VaadinIcon.ANGLE_DOWN.create());
//            SubMenu starter_submenu = starter_item.getSubMenu();
//            starter_submenu.addItem("Samples", event -> {
//                UI.getCurrent().navigate(SamplesView.class);
//            });
//            starter_submenu.addItem("Create Model", event -> {
//                UI.getCurrent().navigate(CreateModelView.class);
//            });
//
//            MenuItem run_item = nav_menu.addItem(new H4("Run"), event -> {
//                UI.getCurrent().navigate(RunView.class);
//            });
//            run_item.getStyle().set("width", "160px");
//            run_item.getStyle().set("color", "black");
//
//            // add admin nav item based on user role
//            User currentUser = authenticatedUser.get().get();
//            if (currentUser.getRoles().contains(Role.ADMIN) || currentUser.getRoles().contains(Role.OWNER)) {
//                MenuItem admin_item = nav_menu.addItem(new H4("Admin"));
//                admin_item.getStyle().set("width", "160px");
//                admin_item.getStyle().set("color", "black");
//                admin_item.add(VaadinIcon.ANGLE_DOWN.create());
//                SubMenu admin_submenu = admin_item.getSubMenu();
//                admin_submenu.addItem("Users List", event -> {
//                    UI.getCurrent().navigate(UsersListView.class);
//                });
//                admin_submenu.addItem("Add User", event -> {
//                    UI.getCurrent().navigate(AddUserView.class);
//                });
//            }


//            nav_menu_layout.add(nav_menu);

            // add user avatar
            avatar_menu = new MenuBar();
            avatar_menu.addThemeVariants(MenuBarVariant.LUMO_LARGE, MenuBarVariant.LUMO_TERTIARY);
            Avatar user_avatar = new Avatar();
            user_avatar.setHeight("40px");
            user_avatar.setWidth("40px");
            updateAvatar(user_avatar);

            MenuItem avatar_menu_item = avatar_menu.addItem(user_avatar);
            avatar_menu_item.getStyle().set("width", "60px");
            avatar_menu_item.getStyle().set("height", "45px");
            SubMenu avatar_options = avatar_menu_item.getSubMenu();
            avatar_options.addItem("Profile", event -> {
                UI.getCurrent().navigate(ProfileView.class);
            });
            avatar_options.addItem("Logout", event -> {
                authenticatedUser.logout();
            });

            // subscribe to events for UI update for avatar
            UI.getCurrent().addAfterNavigationListener(event -> {
                AvatarUpdateToken avatar_update_event = UI.getCurrent().getSession().getAttribute(AvatarUpdateToken.class);
                if (avatar_update_event != null) {
                    updateAvatar(user_avatar);
                }
            });

            header_layout.add(avatar_menu);
        }


//        Hr horizontal_line = new Hr();
//        horizontal_line.getStyle().set("background", "var(--lumo-contrast-10pct)");
//        horizontal_line.getStyle().set("margin", "12px 80px 0px 80px");
//
//        VerticalLayout nav_layout = new VerticalLayout(header_layout, horizontal_line);
//        nav_layout.setSizeFull();
//        nav_layout.getStyle().set("background-color", "white");

//        addToNavbar(nav_layout);
        addToNavbar(true, toggle, viewTitle, avatar_menu);
    }

//    // a helper method to set up the footer
//    private void setupFooter()
//    {
//        Hr horizontal_line = new Hr();
//        horizontal_line.getStyle().set("background", "var(--lumo-contrast-10pct)");
//        horizontal_line.getStyle().set("margin", "0px 40px 0px 40px");
//
//
////        Span copyright_footer = new Span("Copyright © 2024 BaLab");
//        String version_text = "Version: " + this.minirealConfigGetter.getProjectVersion();
//        Span copyright_footer = new Span(version_text);
//        copyright_footer.getElement().getStyle().set("display", "flex");
//        copyright_footer.getElement().getStyle().set("align-items", "center");
//        Button about_footer = new Button("About", event -> {
//            UI.getCurrent().navigate(AboutView.class);
//        });
//        about_footer.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
//        about_footer.getStyle().set("color", "black");
//
//        HorizontalLayout footer_elts_layout = new HorizontalLayout(copyright_footer, about_footer);
//        footer_elts_layout.setWidthFull();
//        footer_elts_layout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
//
//        footer.add(horizontal_line, footer_elts_layout);
////        footer.add(footer_elts_layout);
//    }

    private SideNav createNavigation() {
        SideNav nav = new SideNav();

        if (authenticatedUser.get().isPresent())
        {
            nav.addItem(new SideNavItem("Sample Models", SamplesView.class, LineAwesomeIcon.FILE_DOWNLOAD_SOLID.create()));
            nav.addItem(new SideNavItem("Create Model", CreateModelView.class, LineAwesomeIcon.MAGIC_SOLID.create()));
            nav.addItem(new SideNavItem("Run Simulation", RunView.class, LineAwesomeIcon.PLAY_CIRCLE_SOLID.create()));
            nav.addItem(new SideNavItem("Batch Simulation", RunView.class, LineAwesomeIcon.COGS_SOLID.create()));
            nav.addItem(new SideNavItem("Model Calibration", RunView.class, LineAwesomeIcon.SLIDERS_H_SOLID.create()));

            User currentUser = authenticatedUser.get().get();
            if (currentUser.getRoles().contains(Role.ADMIN) || currentUser.getRoles().contains(Role.OWNER))
            {
                nav.addItem(new SideNavItem("Manage Users", UsersListView.class, LineAwesomeIcon.USERS_COG_SOLID.create()));
            }
        }
        nav.addItem(new SideNavItem("About", AboutView.class, LineAwesomeIcon.INFO_CIRCLE_SOLID.create()));

        return nav;
    }

    public void updateAvatar(Avatar user_avatar)
    {
        String pic_path = authenticatedUser.get().get().getProfilePath();
        if(pic_path != null){
            user_avatar.setImageResource(fileSystem_service.getFileResource(pic_path));
        }
    }

    @Override
    public void showRouterLayoutContent(HasElement content)
    {
        // super.showRouterLayoutContent(content);

        // set the content part to the routed view
        child_content.removeAll();
        child_content.add((Component) content);
        viewTitle.setText(getCurrentPageTitle());
        System.out.println("*** Page Title: " + getCurrentPageTitle());
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }
}
