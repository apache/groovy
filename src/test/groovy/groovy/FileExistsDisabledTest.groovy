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
package groovy

import groovy.junit6.plugin.ForkedJvm
import org.junit.jupiter.api.Test

import java.nio.file.Files
import java.nio.file.Path

/**
 * Tests that when {@code groovy.truth.file.exists.enabled=false}, GROOVY-11996
 * restores the pre-Groovy-5 behavior where any non-null {@link File} or
 * {@link Path} is truthy regardless of whether the underlying file exists.
 * <p>
 * Each test runs in a freshly forked JVM with the property set, so that
 * {@code ResourceGroovyMethods.FILE_EXISTS_ENABLED} is initialised to
 * {@code false} at class load.
 */
@ForkedJvm(systemProperties = ['groovy.truth.file.exists.enabled=false'])
final class FileExistsDisabledTest {

    @Test
    void existingFileIsTruthy() {
        File f = File.createTempFile('present', '.txt')
        f.deleteOnExit()
        assert f.exists()
        assert f
    }

    @Test
    void missingFileIsTruthy() {
        File f = new File(System.getProperty('java.io.tmpdir'), "missing-${UUID.randomUUID()}.txt")
        assert !f.exists()
        assert f
    }

    @Test
    void nullFileIsFalsy() {
        File f = null
        assert !f
    }

    @Test
    void existingPathIsTruthy() {
        Path p = Files.createTempFile('present', '.txt')
        p.toFile().deleteOnExit()
        assert Files.exists(p)
        assert p
    }

    @Test
    void missingPathIsTruthy() {
        Path p = Path.of(System.getProperty('java.io.tmpdir'), "missing-${UUID.randomUUID()}.txt")
        assert !Files.exists(p)
        assert p
    }

    @Test
    void nullPathIsFalsy() {
        Path p = null
        assert !p
    }
}
