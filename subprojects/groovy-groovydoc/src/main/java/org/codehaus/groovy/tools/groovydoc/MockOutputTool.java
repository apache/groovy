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
package org.codehaus.groovy.tools.groovydoc;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * In-memory {@link OutputTool} used by tests. Nothing is written to disk
 * by default, so test runs leave no filesystem artifacts.
 *
 * <p><b>Debugging:</b> pass the {@code groovydoc.mockOutputTool.dumpDir}
 * system property to also mirror every output call to that directory.
 * Example (from the groovy-groovydoc module):
 *
 * <pre>
 * ./gradlew :groovy-groovydoc:test -Dgroovydoc.mockOutputTool.dumpDir=build/mock-dump
 * </pre>
 *
 * When the property is unset, all calls are captured only in the in-memory
 * {@link #output} and {@link #outputAreas} maps; inspect them via
 * {@link #getText(String)} or via a debugger breakpoint.
 */
public class MockOutputTool implements OutputTool {
    private static final String DUMP_DIR_PROP = "groovydoc.mockOutputTool.dumpDir";

    private final Set<String> outputAreas = new LinkedHashSet<>();
    private final Map<String, String> output = new LinkedHashMap<>();
    private final String dumpDir = System.getProperty(DUMP_DIR_PROP);

    /**
     * {@inheritDoc}
     */
    @Override
    public void makeOutputArea(String filename) {
        outputAreas.add(filename);
        if (dumpDir != null) {
            try {
                Files.createDirectories(Paths.get(dumpDir, filename));
            } catch (IOException ignore) {
                // Debugging aid only; don't fail tests on a mirror-write issue.
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeToOutput(String fileName, String text, String charset) throws Exception {
        output.put(fileName, text);
        if (dumpDir != null) {
            Path dst = Paths.get(dumpDir, fileName);
            Path parent = dst.getParent();
            if (parent != null) Files.createDirectories(parent);
            Charset cs = charset == null || charset.isEmpty()
                    ? StandardCharsets.UTF_8 : Charset.forName(charset);
            Files.writeString(dst, text, cs);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void copyResource(String srcPath, String dstPath) throws IOException {
        // Read the source file's contents into the in-memory map so tests
        // can assert on copied-file content without touching disk.
        output.put(dstPath, Files.readString(Paths.get(srcPath)));
        if (dumpDir != null) {
            Path dst = Paths.get(dumpDir, dstPath);
            Path parent = dst.getParent();
            if (parent != null) Files.createDirectories(parent);
            Files.copy(Paths.get(srcPath), dst, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Returns {@code true} if {@link #makeOutputArea} was previously called with the given filename.
     */
    public boolean isValidOutputArea(String fileName) {
        return outputAreas.contains(fileName);
    }

    /**
     * Returns the content written to the given filename by {@link #writeToOutput}, or {@code null} if no content has been written.
     */
    public String getText(String fileName) {
        return output.get(fileName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "dirs:" + outputAreas + ", files:" + output.keySet();
    }
}
