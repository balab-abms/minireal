package org.balab.minireal.data.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SimForm {
    String model_name;
    String agent_name;
    Class field_type;
    String field_name;
    Double popln;
}
