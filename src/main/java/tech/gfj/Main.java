package tech.gfj;

import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        SftpConfig config = new SftpConfig("localhost", 2222, "testuser", "testpass", "/upload/");

        SftpUploader uploader = new SftpUploader(config);
        uploader.uploadFromResources("/sales_report.csv", "sales_report.csv");

        SftpDownloader downloader = new SftpDownloader(config);
        downloader.downloadTo("sales_report.csv", Path.of("downloads", "sales_report.csv"));
    }
}