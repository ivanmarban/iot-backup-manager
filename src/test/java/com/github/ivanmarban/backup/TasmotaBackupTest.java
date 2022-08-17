package com.github.ivanmarban.backup;

import com.github.ivanmarban.compress.TarGzipCompressor;
import com.github.ivanmarban.app.AppConfig;
import io.micronaut.http.HttpHeaders;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.junit.jupiter.MockServerSettings;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static com.google.common.net.HttpHeaders.CONTENT_DISPOSITION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockserver.model.Header.header;
import static org.mockserver.model.HttpError.error;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.HttpStatusCode.OK_200;

@ExtendWith({ MockServerExtension.class, MockitoExtension.class })
@MockServerSettings(ports = 80)
class TasmotaBackupTest {

    private static final String FILE_NAME = "dummy-file.txt";

    private static final Path TARGET_DIR = Path.of("./target");

    @Mock
    private AppConfig appConfig;

    @Mock
    private TarGzipCompressor tarGzipCompressor;

    @InjectMocks
    private TasmotaBackup tasmotaBackup;

    @BeforeEach
    public void setUp() {
        when(appConfig.getTasmotaDevices()).thenReturn(List.of("127.0.0.1"));
    }

    @Test
    @DisplayName("Should download & compress Tasmota device configuration")
    void testDownloadBackupConfiguration(MockServerClient mockServerClient) throws IOException {
        byte[] fileBytes = IOUtils
                .toByteArray(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(FILE_NAME)));
        mockServerClient.when(request().withPath("/dl"))
                .respond(response().withBody(fileBytes).withStatusCode(OK_200.code())
                        .withHeaders(header(CONTENT_DISPOSITION, "attachment; filename=" + FILE_NAME)));
        tasmotaBackup.create(TARGET_DIR);
        verify(tarGzipCompressor, times(1)).compressFiles(anyList(), any(Path.class));
    }

    @Test
    @DisplayName("Should throw RuntimeException when response has no filename in header")
    void testDownloadBackupConfigurationThrowsExceptionWhileNoFilenameInHeader(MockServerClient mockServerClient) {
        mockServerClient.reset().when(request().withPath("/dl")).respond(response().withStatusCode(OK_200.code())
                .withHeaders(header(CONTENT_DISPOSITION, "bar; foo=" + FILE_NAME)));
        Exception exception = assertThrows(RuntimeException.class, () -> tasmotaBackup.create(TARGET_DIR));
        assertEquals("Error downloading backup for device [127.0.0.1]", exception.getMessage());
        assertEquals("Unable to determinate filename from " + HttpHeaders.CONTENT_DISPOSITION + " Header",
                exception.getCause().getMessage());
    }

    @Test
    @DisplayName("Should throw RuntimeException when response has no headers")
    void testDownloadBackupConfigurationThrowsExceptionWhileNoHeaders(MockServerClient mockServerClient) {
        mockServerClient.reset().when(request().withPath("/dl")).respond(response().withStatusCode(OK_200.code()));
        Exception exception = assertThrows(RuntimeException.class, () -> tasmotaBackup.create(TARGET_DIR));
        assertEquals("Error downloading backup for device [127.0.0.1]", exception.getMessage());
        assertEquals("Unable to determinate filename from " + HttpHeaders.CONTENT_DISPOSITION + " Header",
                exception.getCause().getMessage());
    }

    @Test
    @DisplayName("Should throw RuntimeException when error from server")
    void testDownloadBackupConfigurationThrowssExceptionWhileErrorFromServer(MockServerClient mockServerClient) {
        mockServerClient.reset().when(request().withPath("/dl")).error(error().withDropConnection(true));
        Exception exception = assertThrows(RuntimeException.class, () -> tasmotaBackup.create(TARGET_DIR));
        assertEquals("Error downloading backup for device [127.0.0.1]", exception.getMessage());
        assertEquals("Unexpected end of file from server", exception.getCause().getMessage());
    }

}
