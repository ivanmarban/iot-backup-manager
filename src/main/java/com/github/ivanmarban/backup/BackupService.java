package com.github.ivanmarban.backup;

import com.github.ivanmarban.gdrive.GoogleDriveService;
import com.google.api.services.drive.model.File;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

@Singleton
@Slf4j
public class BackupService {

    private static final List<String> FILES = Arrays.asList(OpenHabBackup.TAR_GZ_FILENAME,
            TasmotaBackup.TAR_GZ_FILENAME);

    @Inject
    private TasmotaBackup tasmotaBackup;

    @Inject
    private OpenHabBackup openHabBackup;

    @Inject
    private GoogleDriveService googleDriveService;

    public void createBackup() throws IOException {
        Path tempFolder = Files.createTempDirectory("iot-backup-manager");
        tasmotaBackup.create(tempFolder);
        openHabBackup.create(tempFolder);
        FILES.forEach(file -> uploadBackupFile(tempFolder, file));
    }

    private void uploadBackupFile(Path tempFolder, String fileName) {
        googleDriveService.getFileId(fileName).ifPresentOrElse(fileId -> {
            File file = googleDriveService.updateFile(fileId, tempFolder.resolve(fileName));
            log.info("File updated successfully [id={}, name={}, version={}]", file.getId(), file.getName(),
                    file.getVersion());
        }, () -> {
            File file = googleDriveService.uploadFile(tempFolder.resolve(fileName));
            log.info("File uploaded successfully [id={}, name={}]", file.getId(), file.getName());
        });
    }

}
