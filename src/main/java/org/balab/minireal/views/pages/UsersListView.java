package org.balab.minireal.views.pages;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.balab.minireal.data.entity.Role;
import org.balab.minireal.views.MainLayout;

@Route(value = "users", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class UsersListView extends VerticalLayout
{
}
