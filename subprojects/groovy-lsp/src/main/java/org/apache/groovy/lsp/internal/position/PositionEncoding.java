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
package org.apache.groovy.lsp.internal.position;

import java.util.List;
import java.util.Locale;

/**
 * Negotiated LSP position encoding. Groovy AST columns are Unicode code
 * points; the protocol default is UTF-16 code units.
 */
public enum PositionEncoding {
    /** UTF-8 code units (bytes). */
    UTF8("utf-8"),
    /** UTF-16 code units. Always supported. */
    UTF16("utf-16"),
    /** Unicode code points. Matches Groovy AST columns. */
    UTF32("utf-32");

    private final String protocolName;

    PositionEncoding(final String protocolName) {
        this.protocolName = protocolName;
    }

    /**
     * Returns the LSP {@code PositionEncodingKind} string.
     *
     * @return the protocol name
     */
    public String protocolName() {
        return protocolName;
    }

    /**
     * Parses a protocol encoding name.
     *
     * @param name protocol name, possibly {@code null}
     * @return the encoding, or {@code null} when unrecognized
     */
    public static PositionEncoding fromProtocol(final String name) {
        if (name == null) {
            return null;
        }
        String n = name.toLowerCase(Locale.ROOT);
        for (PositionEncoding encoding : values()) {
            if (encoding.protocolName.equals(n)) {
                return encoding;
            }
        }
        return null;
    }

    /**
     * Picks the first encoding the server supports from the client's
     * preference list. Defaults to {@link #UTF16} when the client omits
     * the capability, matching LSP 3.17.
     *
     * @param clientEncodings client preference, most-preferred first
     * @return the encoding to advertise in {@code InitializeResult}
     */
    public static PositionEncoding negotiate(final List<String> clientEncodings) {
        if (clientEncodings == null || clientEncodings.isEmpty()) {
            return UTF16;
        }
        for (String name : clientEncodings) {
            PositionEncoding encoding = fromProtocol(name);
            if (encoding != null) {
                return encoding;
            }
        }
        return UTF16;
    }
}
