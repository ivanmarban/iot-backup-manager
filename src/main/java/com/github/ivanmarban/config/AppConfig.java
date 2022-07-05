package com.github.ivanmarban.config;

import io.micronaut.context.annotation.ConfigurationProperties;
import lombok.Data;

import java.util.List;

@Data
@ConfigurationProperties("application")
public class AppConfig {

    private String googleDriveCredentialsFolder;

    private List<String> tasmotaDevices;

    private String openhabFolder;

}
