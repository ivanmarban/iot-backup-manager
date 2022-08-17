package com.github.ivanmarban.backup;

import com.github.ivanmarban.app.AppConfig;
import com.github.ivanmarban.compress.TarGzipCompressor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;

@Singleton
@Slf4j
public class OpenHabBackup implements Backup {

    public static final String TAR_GZ_FILENAME = "openhab.tar.gz";

    @Inject
    private AppConfig appConfig;

    @Inject
    private TarGzipCompressor tarGzipCompressor;

    @Override
    public void create(Path outputFolder) {
        log.info("Creating OpenHab backup");
        tarGzipCompressor.compressFolder(Path.of(appConfig.getOpenhabFolder()), outputFolder.resolve(TAR_GZ_FILENAME));
    }

}
