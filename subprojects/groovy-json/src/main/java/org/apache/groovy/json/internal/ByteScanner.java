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
package org.apache.groovy.json.internal;

import static org.apache.groovy.json.internal.Exceptions.die;

public class ByteScanner {

    /**
     * Turns a single nibble into an ascii HEX digit.
     *
     * @param nibble the nibble to serializeObject.
     * @return the encoded nibble (1/2 byte).
     */
    protected static int encodeNibbleToHexAsciiCharByte(final int nibble) {
        return switch (nibble) {
            case 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09 -> nibble + 0x30; // 0x30('0') - 0x39('9')
            case 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F -> nibble + 0x57; // 0x41('a') - 0x46('f')
            default -> {
                die("illegal nibble: " + nibble);
                yield -1;
            }
        };
    }

    /**
     * Turn a single bytes into two hex character representation.
     *
     * @param decoded the byte to serializeObject.
     * @param encoded the array to which each encoded nibbles are now ascii hex representations.
     */
    public static void encodeByteIntoTwoAsciiCharBytes(final int decoded, final byte[] encoded) {
        encoded[0] = (byte) encodeNibbleToHexAsciiCharByte((decoded >> 4) & 0x0F);
        encoded[1] = (byte) encodeNibbleToHexAsciiCharByte(decoded & 0x0F);
    }
}
