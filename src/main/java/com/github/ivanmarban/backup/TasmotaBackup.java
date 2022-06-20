package com.github.ivanmarban.backup;

import com.github.ivanmarban.config.AppConfig;
import com.github.ivanmarban.exception.HttpHeaderValueException;
import io.micronaut.http.HttpHeaders;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
@Slf4j
public class TasmotaBackup {

	private static final String FILENAME = "filename";

	private static final String URL_TEMPLATE = "http://{host}/dl";

	private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{(.*?)\\}");

	private final AppConfig appConfig;

	public TasmotaBackup(AppConfig appConfig) {
		this.appConfig = appConfig;
	}

	public void runBackup(String destinationDirectory) {
		appConfig.getTasmotaIpAddresses().forEach(ip -> {
			downloadBackupConfiguration(ip, destinationDirectory);
			log.info("Downloaded backup configuration for device [{}] ", ip);
		});
	}

	private void downloadBackupConfiguration(String host, String destinationDirectory) {
		try {
			URL url = new URL(Objects.requireNonNull(templateUrl(Map.of("host", host))));
			URLConnection urlConnection = url.openConnection();
			String fileName = getFileName(urlConnection);
			File file = new File(destinationDirectory, fileName);
			ReadableByteChannel readableByteChannel = Channels.newChannel(urlConnection.getInputStream());
			FileOutputStream fileOutputStream = new FileOutputStream(file);
			fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
			fileOutputStream.close();
		}
		catch (IOException | HttpHeaderValueException | NullPointerException e) {
			log.error("Error downloading backup for device [" + host + "] ", e);
			throw new RuntimeException(e);
		}
	}

	private String getFileName(URLConnection urlConnection) {
		String httpHeaderValue = urlConnection.getHeaderField(HttpHeaders.CONTENT_DISPOSITION);
		if (httpHeaderValue == null || !httpHeaderValue.contains(FILENAME)) {
			throw new HttpHeaderValueException(
					"Unable to determinate filename from " + HttpHeaders.CONTENT_DISPOSITION + " Header");
		}
		return httpHeaderValue.split("=")[1];
	}

	private String templateUrl(Map<String, Object> params) {
		String target = TasmotaBackup.URL_TEMPLATE;
		try {
			Matcher matcher = PLACEHOLDER_PATTERN.matcher(target);
			while (matcher.find()) {
				String key = matcher.group();
				String keyclone = key.substring(1, key.length() - 1).trim();
				Object value = params.get(keyclone);
				if (value != null) {
					target = target.replace(key, value.toString());
				}
			}
		}
		catch (Exception e) {
			log.error("Error templating url with params: " + params, e);
			return null;
		}
		return target;
	}

}
