package tech.gfj;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        SftpUploader sftpUploader = new SftpUploader();
        sftpUploader.uploadFile();
    }

    public static class SftpUploader {

        public void uploadFile() {

            String host = "localhost";
            int port = 2222;
            String user = "testuser";
            String password = "testpass";
            String remoteDir = "/upload/";

            InputStream resourceStream = this.getInputStream();
            Path tempFile = this.getTempFile(resourceStream);

            Session session = this.getSession(user, host, port, password);

            ChannelSftp channelSftp = this.getChannelSftp(session);

            this.upload(channelSftp, tempFile, remoteDir);

            this.disconnect(channelSftp, session);
        }

        private InputStream getInputStream() {
            InputStream resourceStream = SftpUploader.class.getResourceAsStream("/sales_report.csv");
            if (resourceStream == null) {
                throw new RuntimeException("Arquivo sales_report.csv não encontrado em resources!");
            }
            return resourceStream;
        }

        private Path getTempFile(InputStream resourceStream) {
            try {
                Path tempFile = Files.createTempFile("sales_report", ".csv");
                Files.copy(resourceStream, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                return tempFile;
            } catch (IOException e) {
                throw  new RuntimeException(e);
            }

        }

        private void upload(ChannelSftp channelSftp, Path tempFile, String remoteDir) {
            try {
                channelSftp.put(Files.newInputStream(tempFile), remoteDir + "sales_report.csv");
                System.out.println("Arquivo enviado com sucesso a partir do resources!");
            } catch (SftpException | IOException e) {
                throw new RuntimeException(e);
            }

        }

        private ChannelSftp getChannelSftp(Session session) {
            try {
                ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
                channelSftp.connect();
                return channelSftp;
            } catch (JSchException e) {
                throw  new RuntimeException(e);
            }
        }

        private Session getSession(String user, String host, int port, String password) {
            try {
                JSch jsch = new JSch();
                Session session = jsch.getSession(user, host, port);
                session.setPassword(password);
                session.setConfig("StrictHostKeyChecking", "no");
                session.connect();
                return session;
            } catch (JSchException e) {
                throw  new RuntimeException(e);
            }
        }

        private void disconnect(ChannelSftp channelSftp, Session session) {
            channelSftp.disconnect();
            session.disconnect();
        }
    }
}