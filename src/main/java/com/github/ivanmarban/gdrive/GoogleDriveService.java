package com.github.ivanmarban.gdrive;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Singleton
public class GoogleDriveService {

    private static final String APP_PROPERTIES_KEY = "app";

    private static final String APP_PROPERTIES_VALUE = "iot-backup-manager";

    private static final String FOLDER_MIME_TYPE = "application/vnd.google-apps.folder";

    private final Drive drive;

    public GoogleDriveService(Drive drive) {
        this.drive = drive;
    }

    public Optional<String> getFileId(String fileName) {
        try {
            List<File> files = new ArrayList<>();
            String query = "mimeType != '" + FOLDER_MIME_TYPE + "' and appProperties has { key = '" + APP_PROPERTIES_KEY
                    + "' and value ='" + APP_PROPERTIES_VALUE + "'} and trashed = false";
            String pageToken = null;
            do {
                FileList result = drive.files().list().setQ(query).setSpaces("drive")
                        .setFields("nextPageToken, files(id, name, createdTime)").setPageToken(pageToken).execute();
                files.addAll(result.getFiles());
                pageToken = result.getNextPageToken();
            }
            while (pageToken != null);
            return files.stream().filter(f -> f.getName().equals(fileName)).map(File::getId).findFirst();
        }
        catch (IOException e) {
            throw new RuntimeException("Unable to get fileId [name=" + fileName + "]", e);
        }
    }

    public File uploadFile(Path file) {
        try {
            String mimeType = Files.probeContentType(file);
            File fileMetadata = new File();
            fileMetadata.setName(file.toFile().getName());
            fileMetadata.setMimeType(mimeType);
            fileMetadata.setAppProperties(Map.of(APP_PROPERTIES_KEY, APP_PROPERTIES_VALUE));
            FileContent fileContent = new FileContent(mimeType, file.toFile());
            return drive.files().create(fileMetadata, fileContent).setFields("id, name").execute();
        }
        catch (IOException e) {
            throw new RuntimeException("Unable to upload file [name=" + file.getFileName().toString() + "]", e);
        }
    }

    public File updateFile(String fileId, Path file) {
        try {
            java.io.File newFile = new java.io.File(file.toString());
            String mimeType = Files.probeContentType(file);
            FileContent fileContent = new FileContent(mimeType, newFile);
            return drive.files().update(fileId, null, fileContent).setFields("id, name, version").execute();
        }
        catch (IOException e) {
            throw new RuntimeException(
                    "Unable to update file [id=" + fileId + ", name=" + file.getFileName().toString() + "]", e);
        }
    }

}
