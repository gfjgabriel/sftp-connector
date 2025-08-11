package tech.gfj;

import java.io.InputStream;

public class SftpUploader {
    private final SftpConfig config;

    public SftpUploader(SftpConfig config) {
        this.config = config;
    }

    public void uploadFromResources(String resourcePath, String remoteRelativePath) {
        try (InputStream inputStream = getResourceStream(resourcePath);
             SftpClient client = new SftpClient(config).connect()) {
            client.upload(inputStream, remoteRelativePath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private InputStream getResourceStream(String resourcePath) {
        InputStream inputStream = SftpUploader.class.getResourceAsStream(resourcePath);
        if (inputStream == null) throw new IllegalStateException("Resource not found: " + resourcePath);
        return inputStream;
    }
}
