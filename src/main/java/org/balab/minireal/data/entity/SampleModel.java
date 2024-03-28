package org.balab.minireal.data.entity;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class SampleModel extends AbstractEntity
{
    String model;
    String[] agents;
    String[] fields;
    String path;
}
