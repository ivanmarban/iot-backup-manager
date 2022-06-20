package com.github.ivanmarban.backup;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.junit.jupiter.MockServerSettings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static com.google.common.net.HttpHeaders.CONTENT_DISPOSITION;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockserver.model.Header.header;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.HttpStatusCode.OK_200;

@MicronautTest(environments = "test")
@ExtendWith(MockServerExtension.class)
@MockServerSettings(ports = 80)
class TasmotaBackupTest {

	private static final String FILE_NAME = "dummy.txt";

	@Inject
	private TasmotaBackup tasmotaBackup;

	@Test
	void testDownloadBackupConfiguration(MockServerClient mockServerClient) throws IOException {

		byte[] pdfBytes = IOUtils
				.toByteArray(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(FILE_NAME)));

		mockServerClient.when(request().withPath("/dl"))
				.respond(response().withBody(pdfBytes).withStatusCode(OK_200.code())
						.withHeaders(header(CONTENT_DISPOSITION, "attachment; filename=" + FILE_NAME))
						.withBody(pdfBytes));

		tasmotaBackup.runBackup("./target");

		byte[] orig = Files.readAllBytes(Path.of("./src/test/resources/" + FILE_NAME));
		byte[] dest = Files.readAllBytes(Path.of("./target/" + FILE_NAME));

		assertArrayEquals(orig, dest);

	}

}