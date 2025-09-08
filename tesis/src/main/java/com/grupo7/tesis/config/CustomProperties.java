package com.grupo7.tesis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "my.custom")
public class CustomProperties {

    private int combinaciones;

    public int getCombinaciones() {
        return combinaciones;
    }

    public void setCombinaciones(int combinaciones) {
        this.combinaciones = combinaciones;
    }
}

