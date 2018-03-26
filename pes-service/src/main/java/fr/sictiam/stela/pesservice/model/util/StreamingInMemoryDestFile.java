package fr.sictiam.stela.pesservice.model.util;

import net.schmizz.sshj.xfer.InMemoryDestFile;

import java.io.IOException;
import java.io.OutputStream;

public class StreamingInMemoryDestFile extends InMemoryDestFile {
    private OutputStream os;

    public StreamingInMemoryDestFile(OutputStream os) {
        super();
        this.os = os;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return os;
    }

    @Override
    public void setLastAccessedTime(long t) throws IOException {
    }

    @Override
    public void setLastModifiedTime(long t) throws IOException {
    }

    @Override
    public void setPermissions(int perms) throws IOException {
    }
}
