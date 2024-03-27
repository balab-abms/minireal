package org.balab.minireal.views.pages;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.balab.minireal.views.MainLayout;

@Route(value = "adduser", layout = MainLayout.class)
@RolesAllowed({"ADMIN", "SUPER"})
public class AddUserView extends VerticalLayout
{
}
