package com.github.ivanmarban.gdrive;

import com.github.ivanmarban.config.AppConfig;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.java6.auth.oauth2.VerificationCodeReceiver;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Factory
@Slf4j
public class GoogleDrive {

    private static final String ACCESS_TYPE = "offline";

    private static final String APPLICATION_NAME = "iot-backup-manager";

    private static final String CLIENT_SECRET_FILE_NAME = "credentials.json";

    private static final GsonFactory GSON_FACTORY = GsonFactory.getDefaultInstance();

    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);

    private static final String USER_ID = "user";

    @Singleton
    public Credential getCredential(GoogleAuthorizationCodeFlow googleAuthorizationCodeFlow,
            VerificationCodeReceiver verificationCodeReceiver) throws IOException {
        return new AuthorizationCodeInstalledApp(googleAuthorizationCodeFlow, verificationCodeReceiver)
                .authorize(USER_ID);
    }

    @Singleton
    public VerificationCodeReceiver getVerificationCodeReceiver() {
        return new LocalServerReceiver();
    }

    @Singleton
    public GoogleAuthorizationCodeFlow getGoogleAuthorizationCodeFlow(HttpTransport httpTransport,
            FileDataStoreFactory fileDataStoreFactory, GoogleClientSecrets clientSecrets) throws IOException {
        return new GoogleAuthorizationCodeFlow.Builder(httpTransport, GSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(fileDataStoreFactory).setAccessType(ACCESS_TYPE).build();
    }

    @Singleton
    public GoogleClientSecrets getGoogleClientSecrets(AppConfig appConfig) throws IOException {
        File file = new File(appConfig.getGoogleDriveCredentialsFolder(), CLIENT_SECRET_FILE_NAME);
        InputStream inputStream = new FileInputStream(file);
        return GoogleClientSecrets.load(GSON_FACTORY, new InputStreamReader(inputStream));
    }

    @Singleton
    public Drive getDrive(HttpTransport httpTransport, Credential credential) {
        return new Drive.Builder(httpTransport, GSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();
    }

    @Singleton
    public HttpTransport getHttpTransport() throws GeneralSecurityException, IOException {
        return GoogleNetHttpTransport.newTrustedTransport();
    }

    @Singleton
    public FileDataStoreFactory getFileDataStoreFactory(AppConfig appConfig) throws IOException {
        File credentialsFolder = new File(appConfig.getGoogleDriveCredentialsFolder());
        return new FileDataStoreFactory(credentialsFolder);
    }

}
