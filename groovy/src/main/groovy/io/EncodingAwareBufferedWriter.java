package groovy.io;

import java.io.*;
import java.nio.charset.Charset;

/**
 * A buffered writer only for OutputStreamWriter that is aware of
 * the encoding of the OutputStreamWriter.
 *
 * @author Paul King
 */

public class EncodingAwareBufferedWriter extends BufferedWriter {
    private OutputStreamWriter out;
    public EncodingAwareBufferedWriter(OutputStreamWriter out) {
        super(out);
        this.out = out;
    }

    /**
     * The encoding as returned by the underlying OutputStreamWriter. Can be the historical name.
     *
     * @return the encoding
     * @see java.io.OutputStreamWriter#getEncoding()
     */
    public String getEncoding() {
        return out.getEncoding();
    }

    /**
     * The encoding as returned by the underlying OutputStreamWriter. Will be the preferred name.
     *
     * @return the encoding
     * @see java.io.OutputStreamWriter#getEncoding()
     */
    public String getNormalizedEncoding() {
        return Charset.forName(getEncoding()).name();
    }
}
