package me.ramazanenescik04.diken.tools;

import java.io.*;

public class CountingInputStream extends FilterInputStream {

    private long position;

    public CountingInputStream(InputStream in) {
        super(in);
    }

    @Override
    public int read() throws IOException {
        int b = super.read();
        if (b != -1) position++;
        return b;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int count = super.read(b, off, len);
        if (count > 0) position += count;
        return count;
    }

    public long getPosition() {
        return position;
    }
}
