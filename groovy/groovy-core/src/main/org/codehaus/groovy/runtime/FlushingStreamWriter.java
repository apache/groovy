package org.codehaus.groovy.runtime;

import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.io.IOException;

/**
 *
 *
 * <p>Création: 18 avr. 2004</p>
 *
 * @author Guillaume Laforge
 *
 * @since Release x.x.x
 * @cvs.revision $Revision$
 * @cvs.tag $Name$
 * @cvs.author $Author$
 * @cvs.date $Date$
 */
public class FlushingStreamWriter extends OutputStreamWriter {

    public FlushingStreamWriter(OutputStream out) {
        super(out);
    }

    public void write(char[] cbuf, int off, int len) throws IOException {
        super.write(cbuf, off, len);
        flush();
    }

    public void write(int c) throws IOException {
        super.write(c);
        flush();
    }

    public void write(String str, int off, int len) throws IOException {
        super.write(str, off, len);
        flush();
    }
}
