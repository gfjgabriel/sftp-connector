package tech.gfj;

import java.nio.file.Path;

public class SftpDownloader {
    private final SftpConfig config;

    public SftpDownloader(SftpConfig config) {
        this.config = config;
    }

    public void downloadTo(String remoteRelativePath, Path localTarget) {
        try (SftpClient client = new SftpClient(config).connect()) {
            client.download(remoteRelativePath, localTarget);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
