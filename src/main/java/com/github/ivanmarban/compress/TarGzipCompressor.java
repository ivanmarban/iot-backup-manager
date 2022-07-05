package com.github.ivanmarban.compress;

import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

@Singleton
@Slf4j
public class TarGzipCompressor implements Compressor {

    @Override
    public void compressFiles(List<Path> inputFiles, Path outputFile) {
        try (OutputStream outputStream = Files.newOutputStream(outputFile);
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
                GzipCompressorOutputStream gzipCompressorOutputStream = new GzipCompressorOutputStream(
                        bufferedOutputStream);
                TarArchiveOutputStream tarArchiveOutputStream = new TarArchiveOutputStream(
                        gzipCompressorOutputStream)) {
            for (Path file : inputFiles) {
                if (!Files.isRegularFile(file)) {
                    throw new IOException("Supports only files!");
                }
                log.info("Processing file: {}", file);
                TarArchiveEntry tarArchiveEntry = new TarArchiveEntry(file.toFile(), file.getFileName().toString());
                tarArchiveOutputStream.putArchiveEntry(tarArchiveEntry);
                Files.copy(file, tarArchiveOutputStream);
                tarArchiveOutputStream.closeArchiveEntry();
            }
            tarArchiveOutputStream.finish();
        }
        catch (IOException e) {
            throw new RuntimeException("Error compressing files " + inputFiles, e);
        }
    }

    @Override
    public void compressFolder(Path inputFolder, Path outputFile) {
        try (OutputStream outputStream = Files.newOutputStream(outputFile);
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
                GzipCompressorOutputStream gzipCompressorOutputStream = new GzipCompressorOutputStream(
                        bufferedOutputStream);
                TarArchiveOutputStream tarArchiveOutputStream = new TarArchiveOutputStream(
                        gzipCompressorOutputStream)) {
            if (!Files.isDirectory(inputFolder)) {
                throw new IOException("Supports only folder!");
            }
            Files.walkFileTree(inputFolder, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                    log.info("Processing file: {}", file);
                    Path targetFile = inputFolder.relativize(file);
                    TarArchiveEntry tarEntry = new TarArchiveEntry(file.toFile(), targetFile.toString());
                    tarArchiveOutputStream.putArchiveEntry(tarEntry);
                    Files.copy(file, tarArchiveOutputStream);
                    tarArchiveOutputStream.closeArchiveEntry();
                    return FileVisitResult.CONTINUE;
                }
            });
            tarArchiveOutputStream.finish();
        }
        catch (IOException e) {
            throw new RuntimeException("Error compressing folder [" + inputFolder.toString() + "]", e);
        }
    }

}
