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
package bugs

import groovy.ui.GroovyMain
import org.junit.jupiter.api.Test
import java.nio.file.FileStore
import java.nio.file.Files
import java.nio.file.Path

import static org.junit.jupiter.api.Assumptions.assumeTrue

/**
 * GROOVY-2681: in-place edit ({@code -i}) used to move the source aside
 * to {@code java.io.tmpdir} and then fail with
 * {@code java.io.IOException: unable to rename ... to /tmp/groovy_*.tmp}
 * when the source lived on a different filesystem than the system temp
 * directory (a common Unix layout).
 *
 * The rewrite must stay in the source directory so the rename/move does
 * not cross filesystems. This test lives under {@code bugs/} so the ticket
 * has a named regression; the same in-place helpers are also covered in
 * {@code groovy.ui.GroovyMainTest}.
 */
final class Groovy2681 {

    @Test
    void testInPlaceEditAcrossFileSystemsWithoutBackupExtension() {
        File foreignDir = foreignTempDir()
        assumeTrue(foreignDir != null,
            'no writable directory on a different filesystem than java.io.tmpdir')

        def dir = new File(foreignDir, "groovy-2681-${UUID.randomUUID()}")
        assert dir.mkdirs()
        def file = new File(dir, 'foo')
        file.text = 'aaa\n'
        try {
            GroovyMain.main('-i', '-p', '-e', "(line =~ 'a').replaceAll('b')", file.absolutePath)
            assert file.text.normalize() == 'bbb\n'
            def leftovers = dir.listFiles()?.findAll { it.name.startsWith('groovy_') && it.name.endsWith('.tmp') } ?: []
            assert leftovers.isEmpty() : leftovers
        } finally {
            dir.listFiles()?.each { it.delete() }
            dir.delete()
        }
    }

    @Test
    void testInPlaceEditAcrossFileSystemsWithBackupExtension() {
        File foreignDir = foreignTempDir()
        assumeTrue(foreignDir != null,
            'no writable directory on a different filesystem than java.io.tmpdir')

        def dir = new File(foreignDir, "groovy-2681-bak-${UUID.randomUUID()}")
        assert dir.mkdirs()
        def file = new File(dir, 'foo')
        file.text = 'aaa\n'
        try {
            GroovyMain.main('-i', '.bak', '-p', '-e', "(line =~ 'a').replaceAll('b')", file.absolutePath)
            assert file.text.normalize() == 'bbb\n'
            def backup = new File(dir, 'foo.bak')
            assert backup.exists()
            assert backup.text == 'aaa\n'
        } finally {
            dir.listFiles()?.each { it.delete() }
            dir.delete()
        }
    }

    /**
     * A writable directory whose {@link FileStore} is not the same as
     * {@code java.io.tmpdir}. On Linux, {@code /dev/shm} is typically tmpfs
     * while {@code /tmp} is the root filesystem.
     */
    private static File foreignTempDir() {
        Path tmp = Path.of(System.getProperty('java.io.tmpdir')).toAbsolutePath()
        FileStore tmpStore
        try {
            tmpStore = Files.getFileStore(tmp)
        } catch (IOException ignored) {
            return null
        }
        for (String candidate : ['/dev/shm', '/run/shm']) {
            Path path = Path.of(candidate)
            if (!Files.isDirectory(path) || !Files.isWritable(path)) continue
            try {
                if (!Files.getFileStore(path).equals(tmpStore)
                        && path.toFile().canonicalFile != tmp.toFile().canonicalFile) {
                    return path.toFile()
                }
            } catch (IOException ignored) {
                // try the next candidate
            }
        }
        return null
    }
}
