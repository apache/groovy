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

import org.codehaus.groovy.control.CompilerConfiguration;

import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * Represents a Java source file in file to compile
 * @since 3.0.0
 */
public class RawJavaFileObject extends SimpleJavaFileObject {
    private static final Charset DEFAULT_CHARSET = Charset.forName(CompilerConfiguration.DEFAULT.getSourceEncoding());
    private final Path javaFilePath;
    private String src;

    /**
     * Construct a RawJavaFileObject of the given kind and with the
     * given URI.
     *
     * @param uri  the URI for this file object
     */
    public RawJavaFileObject(URI uri) {
        super(uri, JavaFileObject.Kind.SOURCE);
        this.javaFilePath = Paths.get(uri);
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        return null != src ? src : (src = new String(Files.readAllBytes(javaFilePath), DEFAULT_CHARSET));
    }

    /**
     * delete the Java source file
     * @return <code>true</code> if deleted successfully
     */
    @Override
    public boolean delete() {
        return new File(uri).delete();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RawJavaFileObject)) return false;
        RawJavaFileObject that = (RawJavaFileObject) o;
        return Objects.equals(uri, that.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri);
    }

    @Override
    public String toString() {
        return "RawJavaFileObject{" +
                "uri=" + uri +
                '}';
    }
}
