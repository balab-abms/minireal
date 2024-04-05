package org.balab.minireal.views.components;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

import java.util.LinkedHashMap;

public class ParamView extends VerticalLayout
{
    private LinkedHashMap<String, TextField> textField_map;
    public ParamView(String metaData_str)
    {
        setSizeFull();
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        textField_map = new LinkedHashMap<>();

        JsonObject metaData_json = JsonParser.parseString(metaData_str).getAsJsonObject();
        JsonArray params_json = metaData_json.get("paramDTOList").getAsJsonArray();

        // add param fields
        add(new Label("Parameters"));
        addParamFields(params_json);
    }

    private void addParamFields(JsonArray json_array)
    {
        for(JsonElement json_elt: json_array)
        {
            JsonObject temp_json_obj = json_elt.getAsJsonObject();
            String param_name = temp_json_obj.get("name").getAsString();
            String param_value = temp_json_obj.get("value").getAsString();
            TextField temp_textField = new TextField(param_name);
            temp_textField.setValue(param_value);
            add(temp_textField);
            textField_map.put(param_name, temp_textField);
        }
    }

    public String getParamsValue()
    {
        JsonObject param_values_json = new JsonObject();
        for(String params_key: textField_map.keySet())
        {
            param_values_json.addProperty(params_key, textField_map.get(params_key).getValue());
        }
        return param_values_json.toString();
    }
}
