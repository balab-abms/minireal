package org.balab.minireal.data.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinirealConfigGetter {
    @Value("${project.version}")
    private String projectVersion;

    @Value("${project.name}")
    private String projectName;

    public String getProjectVersion() {
        return projectVersion;
    }

    public String getProjectName() {
        return projectName;
    }
}


