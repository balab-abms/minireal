package org.balab.minireal.views.components;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class DBView extends VerticalLayout
{
//    private Checkbox isDB_enabled_checkBox;
    public DBView(String metaData_str)
    {
        setSizeFull();
        setAlignItems(Alignment.CENTER);

        JsonObject metaData_json = JsonParser.parseString(metaData_str).getAsJsonObject();
        // String db_json_elt = metaData_json.get("table_name").getAsString();
        JsonArray dbs_json = metaData_json.get("dbDTOList").getAsJsonArray();

        add(new Label("Database"));
        for(JsonElement db_elt: dbs_json)
        {
            JsonObject temp_json_obj = db_elt.getAsJsonObject();
            String db_name = temp_json_obj.get("tableName").getAsString();
            Checkbox isDB_enabled_checkBox = new Checkbox(db_name);
            isDB_enabled_checkBox.setValue(true);
            add(isDB_enabled_checkBox);

        }


    }

//    private Boolean isDBEnabled()
//    {
//        return isDB_enabled_checkBox.getValue();
//    }

}
