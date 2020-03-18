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
package groovy.text;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import groovy.util.CharsetToolkit;
import org.codehaus.groovy.control.CompilationFailedException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * A template engine is a factory for creating a Template instance for a given text input.
 */
public abstract class TemplateEngine {
    /**
     * Creates a template by reading content from the Reader.
     */
    public abstract Template createTemplate(Reader reader) throws CompilationFailedException, ClassNotFoundException, IOException;

    /**
     * Creates a template from the String contents.
     */
    public Template createTemplate(String templateText) throws CompilationFailedException, ClassNotFoundException, IOException {
        return createTemplate(new StringReader(templateText));
    }

    /**
     * Creates a template from the File contents.
     * If the encoding for the file can be determined, that encoding will be used, otherwise the default encoding will be used.
     * Consider using {@link #createTemplate(File, Charset)} if you need to explicitly set the encoding.
     */
    public Template createTemplate(File file) throws CompilationFailedException, ClassNotFoundException, IOException {
        CharsetToolkit toolkit = new CharsetToolkit(file);
        try (Reader reader = toolkit.getReader()) {
            return createTemplate(reader);
        }
    }

    /**
     * Creates a template from the File contents using the given charset encoding.
     */
    public Template createTemplate(File file, Charset cs) throws CompilationFailedException, ClassNotFoundException, IOException {
        try (Reader reader = new InputStreamReader(new FileInputStream(file), cs)) {
            return createTemplate(reader);
        }
    }

    /**
     * Creates a template from the content found at the URL using the default encoding.
     * Please consider using {@link #createTemplate(URL, Charset)}.
     */
    @SuppressFBWarnings(value = "DM_DEFAULT_ENCODING", justification = "left for legacy reasons but users expected to heed warning")
    public Template createTemplate(URL url) throws CompilationFailedException, ClassNotFoundException, IOException {
        try (Reader reader = new InputStreamReader(url.openStream())) {
            return createTemplate(reader);
        }
    }

    /**
     * Creates a template from the content found at the URL using the given charset encoding.
     */
    public Template createTemplate(URL url, Charset cs) throws CompilationFailedException, ClassNotFoundException, IOException {
        try (Reader reader = new InputStreamReader(url.openStream(), cs)) {
            return createTemplate(reader);
        }
    }
}
