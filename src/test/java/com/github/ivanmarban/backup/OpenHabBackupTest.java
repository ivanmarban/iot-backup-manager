package com.github.ivanmarban.backup;

import com.github.ivanmarban.compress.TarGzipCompressor;
import com.github.ivanmarban.config.AppConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OpenHabBackupTest {

    private static final Path TARGET_FOLDER = Path.of("./target/");

    private static final String DUMMY_FOLDER = "./src/test/resources/dummy-dir";

    @Mock
    private AppConfig appConfig;

    @Mock
    private TarGzipCompressor tarGzipCompressor;

    @InjectMocks
    private OpenHabBackup openHabBackup;

    @Test
    @DisplayName("Should compress OpenHab directory")
    public void testOpenHabBackup() {
        when(appConfig.getOpenhabFolder()).thenReturn(DUMMY_FOLDER);
        openHabBackup.create(TARGET_FOLDER);
        verify(tarGzipCompressor, times(1)).compressFolder(eq(Path.of(appConfig.getOpenhabFolder())),
                eq(TARGET_FOLDER.resolve(OpenHabBackup.TAR_GZ_FILENAME)));
    }

}
