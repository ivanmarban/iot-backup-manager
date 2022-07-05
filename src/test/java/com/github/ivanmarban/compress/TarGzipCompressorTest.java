package com.github.ivanmarban.compress;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TarGzipCompressorTest {

    private static final Path FILES_TAR_GZ = Path.of("./target/dummy-file.tar.gz");

    private static final Path FOLDER_TAR_GZ = Path.of("./target/dummy-directory.tar.gz");

    private static final Path DUMMY_FILE = Path.of("./src/test/resources/dummy-file.txt");

    private static final Path DUMMY_FOLDER = Path.of("./src/test/resources/dummy-dir");

    private static final Path TARGET_FOLDER = Path.of("./target");

    private final TarGzipCompressor tarGzipCompressor = new TarGzipCompressor();

    @Test
    @DisplayName("Should create a tar.gz from files")
    public void testCompressFiles() {
        tarGzipCompressor.compressFiles(List.of(DUMMY_FILE), FILES_TAR_GZ);
        assertTrue(FILES_TAR_GZ.toFile().exists());
        assertTrue(FILES_TAR_GZ.toFile().length() > 0);
    }

    @Test
    @DisplayName("Compressing files should throw RuntimeException while compressing folder ")
    public void testCompressFilesThrowsRuntimeExceptionWhileCompressingFolder() {
        Exception exception = assertThrows(RuntimeException.class,
                () -> tarGzipCompressor.compressFiles(List.of(TARGET_FOLDER), FILES_TAR_GZ));
        assertEquals("Error compressing files [./target]", exception.getMessage());
        assertEquals("Supports only files!", exception.getCause().getMessage());
    }

    @Test
    @DisplayName("Should create a tar.gz from folder")
    public void testCompressFolder() {
        tarGzipCompressor.compressFolder(DUMMY_FOLDER, FOLDER_TAR_GZ);
        assertTrue(FOLDER_TAR_GZ.toFile().exists());
        assertTrue(FOLDER_TAR_GZ.toFile().length() > 0);
    }

    @Test
    @DisplayName("Compressing folder should trow RuntimeException while compressing files")
    public void testCompressFolderThrowsRuntimeExceptionWhileCompressingFiles() {
        Exception exception = assertThrows(RuntimeException.class,
                () -> tarGzipCompressor.compressFolder(DUMMY_FILE, FOLDER_TAR_GZ));
        assertEquals("Error compressing folder [./src/test/resources/dummy-file.txt]", exception.getMessage());
        assertEquals("Supports only folder!", exception.getCause().getMessage());
    }

}
