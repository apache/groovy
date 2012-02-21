/*
 * Copyright 2003-2010 the original author or authors.
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
package org.codehaus.groovy.runtime;

import groovy.lang.StringWriterIOException;
import groovy.lang.Writable;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

/**
 * This class defines all the encoding/decoding groovy methods which enhance
 * the normal JDK classes when inside the Groovy environment.
 * Static methods are used with the first parameter the destination class.
 */
public class EncodingGroovyMethods {

    private static final char[] T_TABLE = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=".toCharArray();

    private static final String CHUNK_SEPARATOR = "\r\n";

    /**
     * Produce a Writable object which writes the Base64 encoding of the byte array.
     * Calling toString() on the result returns the encoding as a String. For more
     * information on Base64 encoding and chunking see <code>RFC 4648</code>.
     *
     * @param data Byte array to be encoded
     * @param chunked whether or not the Base64 encoded data should be MIME chunked
     * @return object which will write the Base64 encoding of the byte array
     * @since 1.5.1
     */
    public static Writable encodeBase64(Byte[] data, final boolean chunked) {
        return encodeBase64(DefaultTypeTransformation.convertToByteArray(data), chunked);
    }

    /**
     * Produce a Writable object which writes the Base64 encoding of the byte array.
     * Calling toString() on the result returns the encoding as a String. For more
     * information on Base64 encoding and chunking see <code>RFC 4648</code>.
     *
     * @param data Byte array to be encoded
     * @return object which will write the Base64 encoding of the byte array
     * @since 1.0
     */
    public static Writable encodeBase64(Byte[] data) {
        return encodeBase64(DefaultTypeTransformation.convertToByteArray(data), false);
    }

    /**
     * Produce a Writable object which writes the Base64 encoding of the byte array.
     * Calling toString() on the result returns the encoding as a String. For more
     * information on Base64 encoding and chunking see <code>RFC 4648</code>.
     *
     * @param data byte array to be encoded
     * @param chunked whether or not the Base64 encoded data should be MIME chunked
     * @return object which will write the Base64 encoding of the byte array
     * @since 1.5.7
     */
    public static Writable encodeBase64(final byte[] data, final boolean chunked) {
        return new Writable() {
            public Writer writeTo(final Writer writer) throws IOException {
                int charCount = 0;
                final int dLimit = (data.length / 3) * 3;

                for (int dIndex = 0; dIndex != dLimit; dIndex += 3) {
                    int d = ((data[dIndex] & 0XFF) << 16) | ((data[dIndex + 1] & 0XFF) << 8) | (data[dIndex + 2] & 0XFF);

                    writer.write(T_TABLE[d >> 18]);
                    writer.write(T_TABLE[(d >> 12) & 0X3F]);
                    writer.write(T_TABLE[(d >> 6) & 0X3F]);
                    writer.write(T_TABLE[d & 0X3F]);

                    if (chunked && ++charCount == 19) {
                        writer.write(CHUNK_SEPARATOR);
                        charCount = 0;
                    }
                }

                if (dLimit != data.length) {
                    int d = (data[dLimit] & 0XFF) << 16;

                    if (dLimit + 1 != data.length) {
                        d |= (data[dLimit + 1] & 0XFF) << 8;
                    }

                    writer.write(T_TABLE[d >> 18]);
                    writer.write(T_TABLE[(d >> 12) & 0X3F]);
                    writer.write((dLimit + 1 < data.length) ? T_TABLE[(d >> 6) & 0X3F] : '=');
                    writer.write('=');
                    if (chunked && charCount != 0) {
                        writer.write(CHUNK_SEPARATOR);
                    }
                }

                return writer;
            }

            public String toString() {
                StringWriter buffer = new StringWriter();

                try {
                    writeTo(buffer);
                } catch (IOException e) {
                    throw new StringWriterIOException(e);
                }

                return buffer.toString();
            }
        };
    }

    /**
     * Produce a Writable object which writes the Base64 encoding of the byte array.
     * Calling toString() on the result returns the encoding as a String. For more
     * information on Base64 encoding and chunking see <code>RFC 4648</code>.
     *
     * @param data byte array to be encoded
     * @return object which will write the Base64 encoding of the byte array
     * @since 1.0
     */
    public static Writable encodeBase64(final byte[] data) {
        return encodeBase64(data, false);
    }

    /**
     * Decode the String from Base64 into a byte array.
     *
     * @param value the string to be decoded
     * @return the decoded bytes as an array
     * @since 1.0
     */
    public static byte[] decodeBase64(String value) {
        int byteShift = 4;
        int tmp = 0;
        boolean done = false;
        final StringBuilder buffer = new StringBuilder();

        for (int i = 0; i != value.length(); i++) {
            final char c = value.charAt(i);
            final int sixBit = (c < 123) ? EncodingGroovyMethodsSupport.TRANSLATE_TABLE[c] : 66;

            if (sixBit < 64) {
                if (done)
                    throw new RuntimeException("= character not at end of base64 value"); // TODO: change this exception type

                tmp = (tmp << 6) | sixBit;

                if (byteShift-- != 4) {
                    buffer.append((char) ((tmp >> (byteShift * 2)) & 0XFF));
                }

            } else if (sixBit == 64) {

                byteShift--;
                done = true;

            } else if (sixBit == 66) {
                // RFC 2045 says that I'm allowed to take the presence of
                // these characters as evidence of data corruption
                // So I will
                throw new RuntimeException("bad character in base64 value"); // TODO: change this exception type
            }

            if (byteShift == 0) byteShift = 4;
        }

        try {
            return buffer.toString().getBytes("ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Base 64 decode produced byte values > 255"); // TODO: change this exception type
        }
    }

    /**
     * Produces a Writable that writes the hex encoding of the Byte[]. Calling
     * toString() on this Writable returns the hex encoding as a String. The hex
     * encoding includes two characters for each byte and all letters are lower case.
     *
     * @param data byte array to be encoded
     * @return object which will write the hex encoding of the byte array
     * @see Integer#toHexString(int)
     */
    public static Writable encodeHex(final Byte[] data) {
        return encodeHex(DefaultTypeTransformation.convertToByteArray(data));
    }

    /**
     * Produces a Writable that writes the hex encoding of the byte[]. Calling
     * toString() on this Writable returns the hex encoding as a String. The hex
     * encoding includes two characters for each byte and all letters are lower case.
     *
     * @param data byte array to be encoded
     * @return object which will write the hex encoding of the byte array
     * @see Integer#toHexString(int)
     */
    public static Writable encodeHex(final byte[] data) {
        return new Writable() {
            public Writer writeTo(Writer out) throws IOException {
                for (int i = 0; i < data.length; i++) {
                    // convert byte into unsigned hex string
                    String hexString = Integer.toHexString(data[i] & 0xFF);

                    // add leading zero if the length of the string is one
                    if (hexString.length() < 2) {
                        out.write("0");
                    }

                    // write hex string to writer
                    out.write(hexString);
                }
                return out;
            }

            public String toString() {
                StringWriter buffer = new StringWriter();

                try {
                    writeTo(buffer);
                } catch (IOException e) {
                    throw new StringWriterIOException(e);
                }

                return buffer.toString();
            }
        };
    }

    /**
     * Decodes a hex string to a byte array. The hex string can contain either upper
     * case or lower case letters.
     *
     * @param value string to be decoded
     * @return decoded byte array
     * @throws NumberFormatException If the string contains an odd number of characters
     *                               or if the characters are not valid hexadecimal values.
     */
    public static byte[] decodeHex(final String value) {
        // if string length is odd then throw exception
        if (value.length() % 2 != 0) {
            throw new NumberFormatException("odd number of characters in hex string");
        }

        byte[] bytes = new byte[value.length() / 2];
        for (int i = 0; i < value.length(); i += 2) {
            bytes[i / 2] = (byte) Integer.parseInt(value.substring(i, i + 2), 16);
        }

        return bytes;
    }
}