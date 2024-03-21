package org.balab.minireal.data.service;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "upload")
public class StorageProperties {
    private String path;

    public String getPath(){
        return  this.path;
    }

    public void setPath(String path){
        this.path = path;
    }
}
