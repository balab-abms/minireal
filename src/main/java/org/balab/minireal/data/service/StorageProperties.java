package org.balab.minireal.data.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "data")
@Getter
@Setter
public class StorageProperties {
    private String users;
    private String system;

}
