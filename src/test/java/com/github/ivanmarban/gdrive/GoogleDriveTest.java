package com.github.ivanmarban.gdrive;

import com.github.ivanmarban.app.AppConfig;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.VerificationCodeReceiver;
import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.List;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GoogleDriveTest {

    @Mock
    private AppConfig appConfigMock;

    @Mock
    private HttpTransport httpTransportMock;

    @Mock
    private Credential credentialMock;

    @Mock
    private FileDataStoreFactory fileDataStoreFactoryMock;

    @Mock
    private GoogleAuthorizationCodeFlow googleAuthorizationCodeFlowMock;

    @Mock
    private VerificationCodeReceiver verificationCodeReceiverMock;

    private final GoogleDrive googleDrive = new GoogleDrive();

    @Test
    public void testGetFileDataStoreFactory() throws IOException {
        when(appConfigMock.getGoogleDriveCredentialsFolder()).thenReturn("./target");
        FileDataStoreFactory fileDataStoreFactory = googleDrive.getFileDataStoreFactory(appConfigMock);
        assertEquals("target", fileDataStoreFactory.getDataDirectory().getName());
    }

    @Test
    public void testGetHttpTransport() throws GeneralSecurityException, IOException {
        HttpTransport httpTransport = googleDrive.getHttpTransport();
        assertNotNull(httpTransport);
    }

    @Test
    public void testGetDrive() {
        Drive drive = googleDrive.getDrive(httpTransportMock, credentialMock);
        assertEquals("iot-backup-manager", drive.getApplicationName());
    }

    @Test
    public void testGetGoogleClientSecrets() throws IOException {
        when(appConfigMock.getGoogleDriveCredentialsFolder()).thenReturn("./src/test/resources");
        GoogleClientSecrets googleClientSecrets = googleDrive.getGoogleClientSecrets(appConfigMock);
        String credentialsJsonFile = new String(Files.readAllBytes(Paths.get("./src/test/resources/credentials.json")));
        assertThatJson(credentialsJsonFile).isEqualTo(googleClientSecrets.toString());
    }

    @Test
    public void testGetGoogleAuthorizationCodeFlow() throws IOException {
        when(appConfigMock.getGoogleDriveCredentialsFolder()).thenReturn("./src/test/resources");
        GoogleClientSecrets googleClientSecrets = googleDrive.getGoogleClientSecrets(appConfigMock);
        GoogleAuthorizationCodeFlow googleAuthorizationCodeFlow = googleDrive
                .getGoogleAuthorizationCodeFlow(httpTransportMock, fileDataStoreFactoryMock, googleClientSecrets);
        assertEquals("client-id", googleAuthorizationCodeFlow.getClientId());
        assertEquals("offline", googleAuthorizationCodeFlow.getAccessType());
    }

    @Test
    public void testGetVerificationCodeReceiver(){
        VerificationCodeReceiver verificationCodeReceiver = googleDrive.getVerificationCodeReceiver();
        assertNotNull(verificationCodeReceiver);
    }

    @Test
    public void testGetCredential() throws IOException {
        GoogleAuthorizationCodeRequestUrl googleAuthorizationCodeRequestUrl = new GoogleAuthorizationCodeRequestUrl(
                GoogleOAuthConstants.AUTHORIZATION_SERVER_URL, "client-id", "http://localhost", List.of("drive"));
        GoogleTokenResponse googleTokenResponse = mock(GoogleTokenResponse.class);
        GoogleAuthorizationCodeTokenRequest googleAuthorizationCodeTokenRequest = mock(
                GoogleAuthorizationCodeTokenRequest.class);
        Credential credentialMock = mock(Credential.class);
        when(verificationCodeReceiverMock.getRedirectUri()).thenReturn("http://localhost");
        when(verificationCodeReceiverMock.waitForCode()).thenReturn("foo");
        when(googleAuthorizationCodeFlowMock.newAuthorizationUrl()).thenReturn(googleAuthorizationCodeRequestUrl);
        when(googleAuthorizationCodeTokenRequest.setRedirectUri(anyString()))
                .thenReturn(googleAuthorizationCodeTokenRequest);
        when(googleAuthorizationCodeFlowMock.newTokenRequest(anyString()))
                .thenReturn(googleAuthorizationCodeTokenRequest);
        when(googleAuthorizationCodeFlowMock.newTokenRequest(anyString()).setRedirectUri(anyString()).execute())
                .thenReturn(googleTokenResponse);
        when(credentialMock.getAccessToken()).thenReturn("bar");
        when(googleAuthorizationCodeFlowMock.createAndStoreCredential(any(), anyString())).thenReturn(credentialMock);
        Credential credential = googleDrive.getCredential(googleAuthorizationCodeFlowMock,
                verificationCodeReceiverMock);
        assertEquals("bar", credential.getAccessToken());
    }

}
