/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.groovy.mcp.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

/**
 * Newline-delimited MCP frames. A frame that exceeds the byte cap is
 * discarded through the next newline so the following frame stays aligned.
 */
public final class StdioFrames {

    private StdioFrames() {
    }

    /**
     * One frame read from stdio.
     *
     * @param text decoded text, or {@code null} when there is none
     * @param kind how the read ended
     */
    public record Frame(String text, Kind kind) {

        /** What {@link StdioFrames#read} found. */
        public enum Kind {
            /** A decoded line, without the newline. */
            LINE,
            /** The line exceeded the byte cap and was discarded. */
            TOO_LARGE,
            /** The line was not valid UTF-8. */
            MALFORMED,
            /** The stream ended before another line. */
            EOF
        }

        static Frame eof() {
            return new Frame(null, Kind.EOF);
        }

        static Frame tooLarge() {
            return new Frame(null, Kind.TOO_LARGE);
        }

        static Frame malformed() {
            return new Frame(null, Kind.MALFORMED);
        }

        static Frame line(final String text) {
            return new Frame(text, Kind.LINE);
        }
    }

    /**
     * Reads one frame. A final line without a newline is still returned.
     * The next call then reports {@link Frame.Kind#EOF}. A CR immediately
     * before the newline, or at EOF, is the line ending and is not counted
     * toward {@code maxBytes}. A CR anywhere else is payload.
     *
     * @param in stream positioned at the next frame
     * @param maxBytes maximum payload bytes, not counting the newline
     * @return the frame
     * @throws IOException when the stream fails
     */
    public static Frame read(final PushbackInputStream in, final int maxBytes) throws IOException {
        if (in == null) {
            throw new IllegalArgumentException("in");
        }
        if (maxBytes < 1) {
            throw new IllegalArgumentException("maxBytes");
        }
        int read = in.read();
        if (read < 0) {
            return Frame.eof();
        }
        ByteArrayOutputStream raw = new ByteArrayOutputStream();
        boolean tooLarge = false;
        int count = 0;
        while (read >= 0 && read != '\n') {
            if (read == '\r') {
                int next = in.read();
                if (next < 0 || next == '\n') {
                    read = next;
                    break;
                }
                if (!tooLarge) {
                    count = append(raw, read, count, maxBytes);
                    tooLarge = count < 0;
                }
                read = next;
                continue;
            }
            if (!tooLarge) {
                count = append(raw, read, count, maxBytes);
                tooLarge = count < 0;
            }
            read = in.read();
        }
        if (tooLarge) {
            return Frame.tooLarge();
        }
        try {
            return Frame.line(decode(raw.toByteArray()));
        } catch (CharacterCodingException ex) {
            return Frame.malformed();
        }
    }

    /**
     * @return the new payload count, or {@code -1} when the cap is already full
     */
    private static int append(final ByteArrayOutputStream raw, final int value, final int count, final int maxBytes) {
        if (count >= maxBytes) {
            return -1;
        }
        raw.write(value);
        return count + 1;
    }

    private static String decode(final byte[] bytes) throws CharacterCodingException {
        CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);
        return decoder.decode(ByteBuffer.wrap(bytes)).toString();
    }
}
