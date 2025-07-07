package org.balab.minireal.views.components;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class PermutationGridView extends VerticalLayout
{
    public PermutationGridView(List<Map<String,String>> combos_map_list) {
        // Find all keys
        Set<String> allKeys = combos_map_list.stream()
                .flatMap(m -> m.keySet().stream())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // Build grid
        Grid<Map<String,String>> grid = new Grid<>();
        grid.setSizeFull();
        grid.setAllRowsVisible(true);
        for (String key : allKeys) {
            grid.addColumn(map -> map.get(key))
                    .setHeader(key)
                    .setSortable(true)
                    .setAutoWidth(true);
        }

        // Bind data
        grid.setItems(combos_map_list);

        // Layout
        NativeLabel param_grid_label = new NativeLabel("Parameters Permutations");
        add(param_grid_label, grid);
        setFlexGrow(1, grid);
        setMinHeight("250px");
        setAlignItems(Alignment.CENTER);
    }
}

