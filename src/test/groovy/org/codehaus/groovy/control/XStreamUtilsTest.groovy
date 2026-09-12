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
package org.codehaus.groovy.control

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.PosixFileAttributeView
import java.nio.file.attribute.PosixFilePermission

import static org.junit.jupiter.api.Assumptions.assumeTrue

/**
 * The {@code groovy.ast=xml} AST dump is written beside its source; it should not expose to
 * others what the source kept to its owner.
 */
class XStreamUtilsTest {

    @TempDir
    Path dir

    private static boolean posixSupported(Path path) {
        Files.getFileAttributeView(path, PosixFileAttributeView) != null
    }

    @Test
    void dumpInheritsThePermissionsOfItsSource() {
        Path source = dir.resolve('Private.groovy')
        Files.writeString(source, 'println "hi"')
        assumeTrue(posixSupported(source), 'POSIX file permissions not supported here')
        Files.setPosixFilePermissions(source, EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE))

        XStreamUtils.serialize(source.toString(), 'placeholder AST content')

        Path dump = dir.resolve('Private.groovy.xml')
        assert Files.exists(dump)
        assert Files.getPosixFilePermissions(dump) == Files.getPosixFilePermissions(source)
        assert !Files.getPosixFilePermissions(dump).contains(PosixFilePermission.OTHERS_READ)
    }

    @Test
    void toXMLIsTheDocumentSerializeWrites() {
        String xml = XStreamUtils.toXML('placeholder AST content')
        assert xml.contains('placeholder AST content')
    }

    @Test
    void dumpForASourceWithoutAFileIsOwnerOnly() {
        // a source compiled from a string has a name but no file to match, so the dump defaults
        // to owner-only rather than the process umask
        Path dump = dir.resolve('script_from_string.xml')
        assumeTrue(posixSupported(dir), 'POSIX file permissions not supported here')

        XStreamUtils.serialize(dir.resolve('script_from_string').toString(), 'placeholder AST content')

        assert Files.exists(dump)
        def perms = Files.getPosixFilePermissions(dump)
        assert !perms.contains(PosixFilePermission.OTHERS_READ)
        assert !perms.contains(PosixFilePermission.GROUP_READ)
    }
}
