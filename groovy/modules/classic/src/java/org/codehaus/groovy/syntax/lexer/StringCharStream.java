package org.codehaus.groovy.syntax.lexer;

import org.codehaus.groovy.syntax.ReadException;

public class StringCharStream extends AbstractCharStream {
    private int cur;
    private String text;

    public StringCharStream(String text) {
        this.text = text;
        this.cur = 0;
    }

    public StringCharStream(String text, String description) {
        super(description);
        this.text = text;
        this.cur = 0;
    }

    public char consume() throws ReadException {
        if (this.cur >= this.text.length()) {
            return CharStream.EOS;
        }

        char c = this.text.charAt(this.cur);

        ++this.cur;

        return c;
    }

    public void close() throws ReadException {
        // do nothing
    }
}
