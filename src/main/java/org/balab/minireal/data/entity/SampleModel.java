package org.balab.minireal.data.entity;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class SampleModel extends AbstractEntity
{
    String model_name;
    String[] agent_names;
    String[] field_names;
    String file_path;
}
