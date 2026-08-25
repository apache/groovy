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
package org.codehaus.groovy.ant;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Owner-only argument-file creation used by forked {@link Groovyc}.
 */
final class GroovycTempFileTest {

    @Test
    void createOwnerOnlyTempFile_isARegularFileUnderTheGivenDirectory() throws Exception {
        Path dir = Files.createTempDirectory("groovyc-owner-");
        File created = null;
        try {
            created = invokeCreateOwnerOnlyTempFile(dir);
            assertTrue(created.isFile());
            assertEquals(dir.toRealPath(), created.toPath().getParent().toRealPath());
            assertTrue(created.getName().startsWith("groovyc-files-"));
            assertTrue(created.getName().endsWith(".txt"));
            assertTrue(created.canRead());
            assertTrue(created.canWrite());
            try {
                Set<PosixFilePermission> perms = Files.getPosixFilePermissions(created.toPath());
                assertEquals(Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE), perms);
            } catch (UnsupportedOperationException ignore) {
                // non-POSIX filesystems use the best-effort restrictToOwner path
            }
        } finally {
            if (created != null) {
                created.delete();
            }
            Files.deleteIfExists(dir);
        }
    }

    @Test
    void restrictToOwner_onAnExistingFileKeepsOwnerReadWrite() throws Exception {
        File file = File.createTempFile("groovyc-restrict-", ".txt");
        try {
            invokeRestrictToOwner(file);
            assertTrue(file.canRead());
            assertTrue(file.canWrite());
        } finally {
            file.delete();
        }
    }

    @Test
    void restrictToOwner_onAMissingFileDoesNotThrow() throws Exception {
        File missing = new File("build/tmp/groovyc-restrict-missing-" + System.nanoTime() + ".txt");
        assertFalse(missing.exists());
        invokeRestrictToOwner(missing);
        assertFalse(missing.exists());
    }

    private static File invokeCreateOwnerOnlyTempFile(Path directory) throws Exception {
        Method method = Groovyc.class.getDeclaredMethod("createOwnerOnlyTempFile", Path.class, String.class, String.class);
        method.setAccessible(true);
        return (File) method.invoke(null, directory, "groovyc-files-", ".txt");
    }

    private static void invokeRestrictToOwner(File file) throws Exception {
        Method method = Groovyc.class.getDeclaredMethod("restrictToOwner", File.class);
        method.setAccessible(true);
        method.invoke(null, file);
    }
}
