package com.github.ivanmarban.backup;

import com.github.ivanmarban.app.AppConfig;
import com.github.ivanmarban.compress.TarGzipCompressor;
import com.github.ivanmarban.exception.HttpHeaderValueException;
import io.micronaut.http.HttpHeaders;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
@Slf4j
public class TasmotaBackup implements Backup {

    private static final int KB = 1024;

    private static final String FILENAME = "filename";

    public static final String TAR_GZ_FILENAME = "tasmota-devices.tar.gz";

    private static final String URL_TEMPLATE = "http://{host}/dl";

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{(.*?)}");

    @Inject
    private AppConfig appConfig;

    @Inject
    private TarGzipCompressor tarGzipCompressor;

    @Override
    public void create(Path outputFolder) {
        log.info("Creating Tasmota devices backup");
        List<Path> paths = downloadDevicesConfiguration(outputFolder);
        if (paths.size() > 0) {
            tarGzipCompressor.compressFiles(paths, outputFolder.resolve(TAR_GZ_FILENAME));
        }
        else {
            log.warn("No Tasmota devices backup made. Skipping.");
        }
    }

    private List<Path> downloadDevicesConfiguration(Path outputFolder) {
        List<Path> paths = new ArrayList<>();
        appConfig.getTasmotaDevices().forEach(host -> {
            try {
                URL url = new URL(templateUrl(Map.of("host", host)));
                URLConnection urlConnection = url.openConnection();
                urlConnection.setConnectTimeout(5000);
                InputStream inputStream = urlConnection.getInputStream();
                String fileName = getFileName(urlConnection);
                Path downloadedFile = outputFolder.resolve(fileName);
                OutputStream outputStream = Files.newOutputStream(downloadedFile);
                copyStreamContent(inputStream, outputStream);
                paths.add(downloadedFile);
                log.info("Downloaded backup configuration for host [{}]", host);
            }
            catch (SocketTimeoutException | ConnectException | NoRouteToHostException e) {
                log.warn("Could not download backup for device [{}] [{}]", host, e.getMessage());
            }
            catch (IOException | HttpHeaderValueException e) {
                throw new RuntimeException("Error downloading backup for device [" + host + "]", e);
            }
        });
        return paths;
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
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(target);
        while (matcher.find()) {
            String key = matcher.group();
            String keyClone = key.substring(1, key.length() - 1).trim();
            Object value = params.get(keyClone);
            target = target.replace(key, value.toString());
        }
        return target;
    }

    private void copyStreamContent(InputStream inputStream, OutputStream outputStream) throws IOException {
        try (ReadableByteChannel readableByteChannel = Channels.newChannel(inputStream);
                WritableByteChannel writableByteChannel = Channels.newChannel(outputStream)) {
            ByteBuffer buffer = ByteBuffer.allocate(KB);
            int read;
            while ((read = readableByteChannel.read(buffer)) > 0) {
                buffer.rewind();
                buffer.limit(read);
                while (read > 0) {
                    read -= writableByteChannel.write(buffer);
                }
                buffer.clear();
            }
        }
    }

}
