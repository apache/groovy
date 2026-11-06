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
package org.codehaus.groovy.tools.javac;

import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Map;
import java.util.Objects;

/**
 * In-memory class file produced by javac during joint compilation.
 * <p>
 * Bytes are retained on the heap. Nothing is written to the filesystem.
 *
 * @since 7.0.0
 */
public class MemClassFileObject extends SimpleJavaFileObject {

    private final String binaryName;
    private final String resourceName;
    private final Map<String, byte[]> resourceSink;
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    /**
     * Creates an in-memory class file for {@code binaryName}.
     *
     * @param binaryName the JVMS binary name, for example {@code com.example.Outer$Inner}
     * @param resourceSink optional map, keyed by resource name
     *        ({@code com/example/Outer$Inner.class}), that receives a copy of the
     *        bytes when the class file is published; may be {@code null}
     */
    public MemClassFileObject(final String binaryName, final Map<String, byte[]> resourceSink) {
        super(URI.create("mem:///" + InMemoryJavaFileManager.resourceName(binaryName)), Kind.CLASS);
        this.binaryName = binaryName;
        this.resourceName = InMemoryJavaFileManager.resourceName(binaryName);
        this.resourceSink = resourceSink;
    }

    /**
     * Returns the binary name of this class file.
     *
     * @return the binary name
     */
    public String getBinaryName() {
        return binaryName;
    }

    /**
     * Returns a copy of the bytes written so far.
     *
     * @return the class bytes, possibly empty if javac has not written any yet
     */
    public byte[] getBytes() {
        return buffer.toByteArray();
    }

    /**
     * Opens a stream that replaces any bytes previously stored for this class.
     *
     * @return a stream javac writes the class file to
     */
    @Override
    public OutputStream openOutputStream() {
        buffer.reset();
        return new OutputStream() {
            @Override
            public void write(final int b) {
                buffer.write(b);
            }

            @Override
            public void write(final byte[] b, final int off, final int len) {
                buffer.write(b, off, len);
            }

            @Override
            public void close() {
                publish();
            }
        };
    }

    /**
     * Opens a stream over the bytes written so far.
     *
     * @return the class bytes
     * @throws IOException if javac has not written this class file
     */
    @Override
    public InputStream openInputStream() throws IOException {
        byte[] bytes = buffer.toByteArray();
        if (bytes.length == 0) {
            throw new IOException("No class bytes for " + binaryName);
        }
        return new ByteArrayInputStream(bytes);
    }

    /**
     * Discards the bytes held for this class file.
     *
     * @return {@code true}
     */
    @Override
    public boolean delete() {
        buffer.reset();
        if (resourceSink != null) {
            resourceSink.remove(resourceName);
        }
        return true;
    }

    /**
     * Copies the current bytes into the resource sink, if one was supplied.
     */
    void publish() {
        if (resourceSink == null) {
            return;
        }
        byte[] written = buffer.toByteArray();
        if (written.length == 0) {
            resourceSink.remove(resourceName);
        } else {
            resourceSink.put(resourceName, written);
        }
    }

    /**
     * Compares this file object by its binary name.
     *
     * @param o the object to compare against
     * @return {@code true} if the other object represents the same class
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof MemClassFileObject that)) return false;
        return Objects.equals(binaryName, that.binaryName);
    }

    /**
     * Returns the hash code derived from the binary name.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(binaryName);
    }

    /**
     * Returns a diagnostic string for this in-memory class file.
     *
     * @return a string representation of this file object
     */
    @Override
    public String toString() {
        return "MemClassFileObject{binaryName=" + binaryName + '}';
    }
}
