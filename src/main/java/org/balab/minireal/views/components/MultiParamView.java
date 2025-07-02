package org.balab.minireal.views.components;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;

import java.util.*;

public class MultiParamView extends VerticalLayout
{
    private LinkedHashMap<String, MultiSelectComboBox> multiComboField_map;
    public MultiParamView(String metaData_str)
    {
        setSizeFull();
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        multiComboField_map = new LinkedHashMap<>();

        JsonObject metaData_json = JsonParser.parseString(metaData_str).getAsJsonObject();
        JsonArray params_json = metaData_json.get("paramDTOList").getAsJsonArray();

        // add param fields
        add(new NativeLabel("Parameters"));
        addParamFields(params_json);
    }

    private void addParamFields(JsonArray json_array)
    {
        for(JsonElement json_elt: json_array)
        {
            JsonObject temp_json_obj = json_elt.getAsJsonObject();
            String param_name = temp_json_obj.get("name").getAsString();
            String param_value = temp_json_obj.get("value").getAsString();
            MultiSelectComboBox<String> temp_multiComboField = new MultiSelectComboBox<>(param_name);
            temp_multiComboField.setAllowCustomValue(true);
            temp_multiComboField.setAutoExpand(MultiSelectComboBox.AutoExpandMode.VERTICAL);
            temp_multiComboField.setSelectedItemsOnTop(true);
            temp_multiComboField.setWidthFull();
            temp_multiComboField.setItems(param_value);
            temp_multiComboField.setValue(param_value);
            temp_multiComboField.addCustomValueSetListener(event -> {
                String new_value = event.getDetail();
                // Add the new value to the current selection
                Set<String> currentSelection = new HashSet<>(temp_multiComboField.getValue());
                currentSelection.add(new_value);
                ListDataProvider<String> multi_combo_dp = (ListDataProvider<String>) temp_multiComboField.getDataProvider();
                Collection<String> multi_combo_items = multi_combo_dp.getItems();
                multi_combo_items.add(new_value);
                temp_multiComboField.setItems(multi_combo_items);
                temp_multiComboField.setValue(currentSelection);
//                System.out.println(getParamsPermutation());
            });
            add(temp_multiComboField);
            multiComboField_map.put(param_name, temp_multiComboField);
        }
    }

    public String getParamsValue()
    {
        JsonObject paramValuesJson = new JsonObject();
        for (Map.Entry<String, MultiSelectComboBox> entry : multiComboField_map.entrySet()) {
            String key = entry.getKey();
            Set<String> selected = entry.getValue().getValue();
            // Build a JsonArray
            JsonArray arr = new JsonArray();
            for (String val : selected) {
                arr.add(val);
            }
            paramValuesJson.add(key, arr);
        }
        return paramValuesJson.toString();
    }

    /**
     * Returns a simple String[] where each element is one JSON object
     * representing one combination of your parameters.
     *
     * Example (for population=[100,200], wealth=[100], steps=[100]):
     * [
     *   {"population":"100","wealth":"100","steps":"100"},
     *   {"population":"200","wealth":"100","steps":"100"}
     * ]
     */
    public String[] getParamsPermutation() {
        // Pull out selected values into a LinkedHashMap to preserve order
        Map<String, Set<String>> params = new LinkedHashMap<>();
        for (Map.Entry<String, MultiSelectComboBox> e : multiComboField_map.entrySet()) {
            params.put(e.getKey(), e.getValue().getValue());
        }

        // Build the JSON array of all permutations
        JsonArray permutations = new JsonArray();
        buildPermutations(params, new ArrayList<>(params.keySet()), 0, new LinkedHashMap<>(), permutations);

        // Convert each JsonObject in the array to its String form
        String[] result = new String[permutations.size()];
        for (int i = 0; i < permutations.size(); i++) {
            result[i] = permutations.get(i).toString();
        }
        System.out.println(Arrays.toString(result));
        return result;
    }

    // Recursive helper—same as before
    private void buildPermutations(Map<String, Set<String>> params,
                                   List<String> keys,
                                   int depth,
                                   Map<String, String> current,
                                   JsonArray result) {
        if (depth == keys.size()) {
            JsonObject obj = new JsonObject();
            current.forEach(obj::addProperty);
            result.add(obj);
            return;
        }
        String key = keys.get(depth);
        for (String val : params.get(key)) {
            current.put(key, val);
            buildPermutations(params, keys, depth + 1, current, result);
            current.remove(key);
        }
    }
}
