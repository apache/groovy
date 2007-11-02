package groovy.io;

import java.io.BufferedWriter;
import java.io.Writer;
import java.io.IOException;

/**
 * A buffered writer that gobbles any \r characters
 * and replaces every \n with a platform specific newline.
 * In many places Groovy normalises streams to only have \n
 * characters but when creating files that must be used
 * by other platform-aware tools, you sometimes want the
 * newlines to match what the platform expects.
 *
 * @author Paul King
 */
public class PlatformLineWriter extends Writer {
    private BufferedWriter writer;

    public PlatformLineWriter(Writer out) {
        writer = new BufferedWriter(out);
    }

    public PlatformLineWriter(Writer out, int sz) {
        writer = new BufferedWriter(out, sz);
    }

    public void write(char cbuf[], int off, int len) throws IOException {
        for (; len > 0; len--) {
            char c = cbuf[off++];
            if (c == '\n') {
                writer.newLine();
            } else if (c != '\r') {
                writer.write(c);
            }
        }
    }

    public void flush() throws IOException {
        writer.flush();
    }

    public void close() throws IOException {
        writer.close();
    }
}
