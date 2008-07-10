/*
 * Copyright 2003-2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
