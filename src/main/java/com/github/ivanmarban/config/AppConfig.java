package com.github.ivanmarban.config;

import io.micronaut.context.annotation.ConfigurationProperties;
import lombok.Data;

import java.util.List;

@Data
@ConfigurationProperties("application")
public class AppConfig {

	private List<String> tasmotaIpAddresses;

	private List<String> backupDirectories;

	private String driveCredentialsDirectory;

	private String driveTargetDirectory;

}
