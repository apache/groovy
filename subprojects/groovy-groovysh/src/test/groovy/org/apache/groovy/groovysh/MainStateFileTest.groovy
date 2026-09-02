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
package org.apache.groovy.groovysh

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermissions

import static org.junit.jupiter.api.Assumptions.assumeTrue

/**
 * groovysh keeps its history and saved session under the user state directory. Those record what was
 * typed and what it evaluated to, so on a shared host they must not be left at the ambient umask.
 */
class MainStateFileTest {

    private static boolean posix() {
        FileSystems.default.supportedFileAttributeViews().contains('posix')
    }

    private static String modeOf(Path p) {
        PosixFilePermissions.toString(Files.getPosixFilePermissions(p))
    }

    // GROOVY-12335
    @Test
    void newStateFileIsReadableOnlyByItsOwner(@TempDir Path dir) {
        assumeTrue(posix())
        def file = Main.createOwnerOnlyStateFile(dir.resolve('groovysh_history'))

        assert Files.exists(file)
        assert modeOf(file) == 'rw-------'
    }

    // GROOVY-12335
    @Test
    void missingParentDirectoryIsCreatedOwnerOnly(@TempDir Path dir) {
        assumeTrue(posix())
        def file = Main.createOwnerOnlyStateFile(dir.resolve('state').resolve('groovysh.ser'))

        assert Files.exists(file)
        assert modeOf(file.parent) == 'rwx------'
    }

    // GROOVY-12335
    @Test
    void existingFileLeftTooOpenIsTightened(@TempDir Path dir) {
        assumeTrue(posix())
        def file = Files.createFile(dir.resolve('groovysh_history'))
        Files.setPosixFilePermissions(file, PosixFilePermissions.fromString('rw-r--r--'))

        Main.createOwnerOnlyStateFile(file)

        assert modeOf(file) == 'rw-------'
    }

    // GROOVY-12335
    @Test
    void alreadyRestrictiveModeIsLeftAlone(@TempDir Path dir) {
        assumeTrue(posix())
        // a user who chose something stricter than owner-only keeps it
        def file = Files.createFile(dir.resolve('groovysh.ser'))
        Files.setPosixFilePermissions(file, PosixFilePermissions.fromString('r--------'))

        Main.createOwnerOnlyStateFile(file)

        assert modeOf(file) == 'r--------'
    }

    // GROOVY-12335
    @Test
    void existingContentIsPreserved(@TempDir Path dir) {
        def file = dir.resolve('groovysh_history')
        file.text = 'previous session\n'

        Main.createOwnerOnlyStateFile(file)

        assert file.text == 'previous session\n'
    }

    // GROOVY-12335
    @Test
    void unwritableLocationDoesNotStopTheShellStarting(@TempDir Path dir) {
        assumeTrue(posix())
        def locked = Files.createDirectory(dir.resolve('locked'))
        Files.setPosixFilePermissions(locked, PosixFilePermissions.fromString('r-x------'))
        try {
            // best effort: returns the path rather than throwing, so startup continues
            assert Main.createOwnerOnlyStateFile(locked.resolve('groovysh_history')) != null
        } finally {
            Files.setPosixFilePermissions(locked, PosixFilePermissions.fromString('rwx------'))
        }
    }
}
