package com.github.ivanmarban.gdrive;

import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GoogleDriveServiceTest {

    private static final String FILE_NAME = "dummy-file.txt";

    private static final String FILE_ID = "1uG6PqmklsDiKDnwvsobC80KyqbDpxByu";

    private static final long FILE_VERSION = 1;

    private static final Path PATH = Path.of("./src/test/resources/dummy-file.txt");

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Drive drive;

    @Mock
    private FileList fileListA;

    @Mock
    private FileList fileListB;

    @InjectMocks
    private GoogleDriveService googleDriveService;

    @Test
    @DisplayName("Should return fileId")
    public void testGetFileId() throws IOException {
        List<File> aFiles = new ArrayList<>();
        aFiles.add(getFile());
        List<File> bFiles = new ArrayList<>();
        bFiles.add(new File().setName("foo").setId(FILE_ID).setVersion(FILE_VERSION));
        when(fileListB.getFiles()).thenReturn(bFiles);
        when(fileListB.getNextPageToken()).thenReturn("foo");
        when(fileListA.getFiles()).thenReturn(aFiles);
        when(fileListA.getNextPageToken()).thenReturn(null);
        when(drive.files().list().setQ(anyString()).setSpaces(anyString()).setFields(anyString()).setPageToken(any())
                .execute()).thenReturn(fileListB, fileListA);
        Optional<String> fileId = googleDriveService.getFileId(FILE_NAME);
        assertTrue(fileId.isPresent());
        assertEquals(FILE_ID, fileId.get());
    }

    @Test
    @DisplayName("Should throw RuntimeException while getting fileId ")
    public void testGetFileIdThrowsRuntimeException() {
        doAnswer(invocation -> {
            throw new IOException();
        }).when(drive).files();
        Exception exception = assertThrows(RuntimeException.class, () -> googleDriveService.getFileId(FILE_NAME));
        assertEquals("Unable to get fileId [name=" + FILE_NAME + "]", exception.getMessage());
    }

    @Test
    @DisplayName("Should upload file to drive")
    public void testUploadFile() throws IOException {
        when(drive.files().create(any(File.class), any(AbstractInputStreamContent.class)).setFields(anyString())
                .execute()).thenReturn(getFile());
        File file = googleDriveService.uploadFile(PATH);
        assertEquals(FILE_ID, file.getId());
        assertEquals(FILE_NAME, file.getName());
        assertEquals(FILE_VERSION, file.getVersion());
    }

    @Test
    @DisplayName("Should throw RuntimeException when uploading file")
    public void testUploadThrowsRuntimeException() {
        doAnswer(invocation -> {
            throw new IOException();
        }).when(drive).files();
        Exception exception = assertThrows(RuntimeException.class, () -> googleDriveService.uploadFile(PATH));
        assertEquals("Unable to upload file [name=" + FILE_NAME + "]", exception.getMessage());
    }

    @Test
    @DisplayName("Should update file on drive")
    public void testUpdateFile() throws IOException {
        when(drive.files().update(anyString(), any(), any(AbstractInputStreamContent.class)).setFields(anyString())
                .execute()).thenReturn(getFile());
        File file = googleDriveService.updateFile(FILE_NAME, PATH);
        assertEquals(FILE_ID, file.getId());
        assertEquals(FILE_NAME, file.getName());
        assertEquals(FILE_VERSION, file.getVersion());
    }

    @Test
    @DisplayName("Should throw RuntimeException when updating file")
    public void testUpdateFileTrowsRuntimeException() {
        doAnswer(invocation -> {
            throw new IOException();
        }).when(drive).files();
        Exception exception = assertThrows(RuntimeException.class, () -> googleDriveService.updateFile(FILE_ID, PATH));
        assertEquals("Unable to update file [id=" + FILE_ID + ", name=" + FILE_NAME + "]", exception.getMessage());
    }

    private File getFile() {
        return new File().setName(FILE_NAME).setId(FILE_ID).setVersion(FILE_VERSION);
    }

}
