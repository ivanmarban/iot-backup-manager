package com.github.ivanmarban.backup;

import com.github.ivanmarban.gdrive.GoogleDriveService;
import com.google.api.services.drive.model.File;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BackupServiceTest {

    private static final String FILE_NAME = "file.txt";

    private static final String FILE_ID = "1uG6PqmklsDiKDnwvsobC80KyqbDpxByu";

    private static final long FILE_VERSION = 1;

    @Mock
    private TasmotaBackup tasmotaBackup;

    @Mock
    private OpenHabBackup openHabBackup;

    @Mock
    private GoogleDriveService googleDriveService;

    @InjectMocks
    private BackupService backupService;

    @Test
    @DisplayName("Should upload backup files to Google Drive")
    public void testBackupServiceShouldUploadFiles() throws IOException {
        when(googleDriveService.uploadFile(any(Path.class))).thenReturn(mockFile());
        createFiles();
        backupService.createBackup();
        verify(tasmotaBackup, times(1)).create(any(Path.class));
        verify(openHabBackup, times(1)).create(any(Path.class));
        verify(googleDriveService, times(2)).getFileId(anyString());
        verify(googleDriveService, times(2)).uploadFile(any(Path.class));
    }

    @Test
    @DisplayName("Should not upload backup files to Google Drive")
    public void testBackupServiceShouldNotUploadFiles() throws IOException {
        backupService.createBackup();
        verify(tasmotaBackup, times(1)).create(any(Path.class));
        verify(openHabBackup, times(1)).create(any(Path.class));
        verify(googleDriveService, times(0)).getFileId(anyString());
        verify(googleDriveService, times(0)).uploadFile(any(Path.class));
    }

    @Test
    @DisplayName("Should update a file's metadata and/or content in Google Drive")
    public void testBackupServiceShouldUpdateFiles() throws IOException {
        createFiles();
        when(googleDriveService.updateFile(anyString(), any(Path.class))).thenReturn(mockFile());
        when(googleDriveService.getFileId(anyString())).thenReturn(Optional.of("foo"));
        backupService.createBackup();
        verify(tasmotaBackup, times(1)).create(any(Path.class));
        verify(openHabBackup, times(1)).create(any(Path.class));
        verify(googleDriveService, times(2)).getFileId(anyString());
        verify(googleDriveService, times(0)).uploadFile(any(Path.class));
        verify(googleDriveService, times(2)).updateFile(anyString(), any(Path.class));
    }

    private File mockFile() {
        return new File().setName(FILE_NAME).setId(FILE_ID).setVersion(FILE_VERSION);
    }

    private void createFiles() {
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            Path tempPath = (Path) args[0];
            tempPath.resolve(TasmotaBackup.TAR_GZ_FILENAME).toFile().createNewFile();
            return null;
        }).when(tasmotaBackup).create(any(Path.class));
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            Path tempPath = (Path) args[0];
            tempPath.resolve(OpenHabBackup.TAR_GZ_FILENAME).toFile().createNewFile();
            return null;
        }).when(openHabBackup).create(any(Path.class));
    }

}
