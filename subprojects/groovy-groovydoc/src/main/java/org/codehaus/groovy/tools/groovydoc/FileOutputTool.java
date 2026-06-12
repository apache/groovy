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

import org.codehaus.groovy.runtime.ResourceGroovyMethods;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static java.lang.System.Logger.Level.WARNING;

/**
 * Writes generated documentation and copied resources to the file system.
 */
public class FileOutputTool implements OutputTool {

    private static final System.Logger LOGGER = System.getLogger(FileOutputTool.class.getName());
    /** {@inheritDoc} */
    @Override
    public void makeOutputArea(String filename) {
        Path path = Paths.get(filename);
        if (Files.exists(path)) {
            return;
        }
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            LOGGER.log(WARNING, "Unable to create directory ''{0}'' due to ''{1}''; attempting to continue...", filename, e.getMessage());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void writeToOutput(String fileName, String text, String charset) throws Exception {
        File file = new File(fileName);
        Path path = file.getParentFile().toPath();
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                LOGGER.log(WARNING, "Unable to create parent directory ''{0}'' due to ''{1}''; attempting to continue...", path, e.getMessage());
            }
        }
        ResourceGroovyMethods.write(file, text, charset, true);
    }

    /** {@inheritDoc} */
    @Override
    public void copyResource(String srcPath, String dstPath) throws IOException {
        Path dst = Paths.get(dstPath);
        Path parent = dst.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.copy(Paths.get(srcPath), dst, StandardCopyOption.REPLACE_EXISTING);
    }
}
