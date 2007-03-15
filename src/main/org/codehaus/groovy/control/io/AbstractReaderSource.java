/*
 $Id$

 Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.

 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.

 3. The name "groovy" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Codehaus.  For written permission,
    please contact info@codehaus.org.

 4. Products derived from this Software may not be called "groovy"
    nor may "groovy" appear in their names without prior written
    permission of The Codehaus. "groovy" is a registered
    trademark of The Codehaus.

 5. Due credit should be given to The Codehaus -
    http://groovy.codehaus.org/

 THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 */
package org.codehaus.groovy.control.io;

import java.io.BufferedReader;
import java.io.IOException;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.Janitor;

/**
 * For ReaderSources that can choose a parent class, a base that
 * provides common functionality.
 *
 * @author <a href="mailto:cpoirier@dreaming.org">Chris Poirier</a>
 * @version $Id$
 */

public abstract class AbstractReaderSource implements ReaderSource {
    protected CompilerConfiguration configuration;   // Configuration data

    public AbstractReaderSource(CompilerConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("Compiler configuration must not be null!");
            // ... or more relaxed?
            // configuration = CompilerConfiguration.DEFAULT;
        }
        this.configuration = configuration;
    }

    /**
     * Returns true if the source can be restarted (ie. if getReader()
     * will return non-null on subsequent calls.
     */
    public boolean canReopenSource() {
        return true;
    }

    private BufferedReader lineSource = null;    // If set, a reader on the current source file
    private String line = null;    // The last line read from the current source file
    private int number = 0;       // The last line number read

    /**
     * Returns a line from the source, or null, if unavailable.  If
     * you supply a Janitor, resources will be cached.
     */
    public String getLine(int lineNumber, Janitor janitor) {
        // If the source is already open and is passed the line we
        // want, close it.
        if (lineSource != null && number > lineNumber) {
            cleanup();
        }

        // If the line source is closed, try to open it.
        if (lineSource == null) {
            try {
                lineSource = new BufferedReader(getReader());
            } catch (Exception e) {
            }
            number = 0;
        }

        // Read until the appropriate line number.
        if (lineSource != null) {
            while (number < lineNumber) {
                try {
                    line = lineSource.readLine();
                    number++;
                }
                catch (IOException e) {
                    cleanup();
                }
            }

            if (janitor == null) {
                cleanup();
            } else {
                janitor.register(this);
            }
        }

        return line;
    }

    /**
     * Cleans up any cached resources used by getLine().
     */
    public void cleanup() {
        if (lineSource != null) {
            try {
                lineSource.close();
            } catch (Exception e) {
            }
        }

        lineSource = null;
        line = null;
        number = 0;
    }

}
