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
package org.codehaus.groovy.control;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.EnumSet;
import java.util.Set;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.WARNING;

/**
 * Serializes AST structures to XML for debugging.
 */
public abstract class XStreamUtils {

    private static final System.Logger LOGGER = System.getLogger(XStreamUtils.class.getName());

    /**
     * Serializes the supplied AST object to an XML file next to the named source. The file is
     * given the source file's permissions, so it exposes no more than the source already does;
     * where there is no source file to match, or the filesystem has no POSIX permissions, the
     * file is owner-only or left to the default respectively.
     *
     * @param name the source name or URI used to derive the XML file name
     * @param ast the AST object to serialize
     */
    public static void serialize(final String name, final Object ast) {
        if (name == null || name.isEmpty()) return;

        XStream xstream = new XStream(new StaxDriver());
        FileWriter astFileWriter = null;
        try {
            File astFile = astFile(name);
            if (astFile == null) {
                LOGGER.log(WARNING, "File-name for writing {0} AST could not be determined!", name);
                return;
            }
            // the dump is a serialization of the source's AST, so it is no less sensitive than the
            // source; give it the source file's permissions rather than the process umask, so it
            // does not expose to others what the source kept to its owner
            createWithPermissions(astFile.toPath(), astDumpPermissions(name));
            astFileWriter = new FileWriter(astFile, false);
            xstream.toXML(ast, astFileWriter);
            LOGGER.log(DEBUG, "Written AST to {0}.xml", name);

        } catch (Exception e) {
            LOGGER.log(WARNING, "Couldn''t write to " + name + ".xml", e);
        } finally {
            DefaultGroovyMethods.closeQuietly(astFileWriter);
        }
    }

    /**
     * The permissions to give the AST dump: the source file's own, so the dump exposes no more
     * than the source already does. When there is no source file to match — a source compiled
     * from a string, say — a debug dump defaults to owner-only rather than the process umask.
     *
     * @param name the source name or URI the dump sits beside
     * @return the permissions, or {@code null} where POSIX permissions do not apply
     */
    private static Set<PosixFilePermission> astDumpPermissions(final String name) {
        try {
            File sourceFile = name.startsWith("file:") ? new File(URI.create(name)) : new File(name);
            if (sourceFile.exists()) {
                return Files.getPosixFilePermissions(sourceFile.toPath());
            }
            return EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE);
        } catch (IOException | UnsupportedOperationException | IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Creates the dump file carrying the given permissions, so its contents are never briefly
     * readable by anyone those permissions exclude. Where they do not apply, creation is left to
     * the writer as before.
     *
     * @param path the file to create
     * @param permissions the permissions to give it, or {@code null} to leave creation to the writer
     */
    private static void createWithPermissions(final Path path, final Set<PosixFilePermission> permissions) {
        if (permissions == null) {
            return;
        }
        try {
            Files.createFile(path, PosixFilePermissions.asFileAttribute(permissions));
        } catch (FileAlreadyExistsException e) {
            try {
                Files.setPosixFilePermissions(path, permissions);
            } catch (IOException | UnsupportedOperationException ignored) {
                // best effort; the writer overwrites the existing file
            }
        } catch (IOException | UnsupportedOperationException e) {
            // no POSIX support, or the file cannot be pre-created; the writer creates it
        }
    }

    /**
     * Takes the incoming file-name and checks whether this is a URI using the <tt>file:</tt> protocol or a non-URI and treats
     * it accordingly.
     *
     * @return a file-name {@link File} representation or <tt>null</tt> if the file-name was in an invalid URI format
     */
    private static File astFile(final String uriOrFileName) {
        try {
            final String astFileName = uriOrFileName + ".xml";
            return uriOrFileName.startsWith("file:") ? new File(URI.create(astFileName)) : new File(astFileName);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
