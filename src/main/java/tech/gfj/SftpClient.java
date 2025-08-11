package tech.gfj;

import com.jcraft.jsch.*;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class SftpClient implements AutoCloseable {
    private final SftpConfig config;
    private Session session;
    private ChannelSftp channelSftp;

    public SftpClient(SftpConfig config) {
        this.config = config;
    }

    public SftpClient connect() {
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(config.user, config.host, config.port);
            session.setPassword(config.password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(15000);
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect(10000);
            return this;
        } catch (JSchException e) {
            throw new RuntimeException(e);
        }
    }

    public void upload(InputStream inputStream, String relativePath) {
        String remotePath = resolveRemotePath(relativePath);
        createRemoteDirectoriesIfMissing(getParentDirectory(remotePath));
        try {
            channelSftp.put(inputStream, remotePath);
        } catch (SftpException e) {
            throw new RuntimeException(e);
        }
    }

    public void download(String relativePath, Path localTarget) {
        String remotePath = resolveRemotePath(relativePath);
        try {
            Files.createDirectories(localTarget.getParent());
            channelSftp.get(remotePath, localTarget.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> listFiles(String relativeDir) {
        String remoteDir = resolveRemoteDirectory(relativeDir);
        try {
            Vector<?> entries = channelSftp.ls(remoteDir);
            List<String> fileNames = new ArrayList<>();
            for (Object entry : entries) {
                fileNames.add(((ChannelSftp.LsEntry) entry).getFilename());
            }
            return fileNames;
        } catch (SftpException e) {
            throw new RuntimeException(e);
        }
    }

    private void createRemoteDirectoriesIfMissing(String absoluteDir) {
        if (absoluteDir == null || absoluteDir.isBlank() || "/".equals(absoluteDir)) return;
        String[] parts = absoluteDir.split("/");
        String path = "/";
        try {
            for (String part : parts) {
                if (part.isEmpty()) continue;
                path += part + "/";
                try {
                    channelSftp.stat(path);
                } catch (SftpException e) {
                    if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                        channelSftp.mkdir(path);
                    } else {
                        throw e;
                    }
                }
            }
        } catch (SftpException e) {
            throw new RuntimeException(e);
        }
    }

    private String resolveRemotePath(String relativePath) {
        String cleanPath = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
        return config.baseDir + cleanPath;
    }

    private String resolveRemoteDirectory(String relativeDir) {
        String dir = resolveRemotePath(relativeDir);
        return dir.endsWith("/") ? dir : dir + "/";
    }

    private String getParentDirectory(String fullPath) {
        int index = fullPath.lastIndexOf('/');
        return (index <= 0) ? "/" : fullPath.substring(0, index);
    }

    @Override
    public void close() {
        if (channelSftp != null && channelSftp.isConnected()) channelSftp.disconnect();
        if (session != null && session.isConnected()) session.disconnect();
    }
}
