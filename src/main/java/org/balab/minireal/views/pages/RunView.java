package org.balab.minireal.views.pages;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.balab.minireal.views.MainLayout;

@Route(value = "run", layout = MainLayout.class)
@PermitAll
public class RunView extends VerticalLayout
{
}