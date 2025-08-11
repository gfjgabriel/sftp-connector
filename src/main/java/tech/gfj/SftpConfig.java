package tech.gfj;

public class SftpConfig {
    public final String host;
    public final int port;
    public final String user;
    public final String password;
    public final String baseDir;

    public SftpConfig(String host, int port, String user, String password, String baseDir) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        this.baseDir = (baseDir == null || baseDir.isBlank()) ? "/" :
                (baseDir.endsWith("/") ? baseDir : baseDir + "/");
    }
}
