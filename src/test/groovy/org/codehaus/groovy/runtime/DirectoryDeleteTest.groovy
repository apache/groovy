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
package org.codehaus.groovy.runtime

import org.junit.jupiter.api.Test

import java.nio.file.Files
import java.nio.file.LinkOption

import static org.junit.jupiter.api.Assumptions.assumeTrue


/**
 * Test File.deleteDir() method in Groovy
 */
class DirectoryDeleteTest {

    @Test
    void testDeleteDir(){
        def file = File.createTempFile("deleteDirTest", "")

        // deleteDir for existing file should return false
        assert !file.deleteDir()

        // deleteDir for non existing file should return true
        file.delete();
        assert file.deleteDir()

        // create and delete empty directory
        def dir = new File(file.getPath())
        assert dir.mkdir()
        assert dir.deleteDir()

        // create and delete directory with file
        dir = new File(file.getPath())
        assert dir.mkdir()
        new File(dir, "test.txt").write("Test")
        assert dir.deleteDir()

        // create and delete directory with subdirectory and file
        dir = new File(file.getPath())
        assert dir.mkdir()
        new File(dir, "test.txt").write("Test")
        def subdir = new File(dir, "subdir")
        subdir.mkdir()
        new File(subdir, "testsubdir.txt").write("Test")
        assert dir.deleteDir()
    }

    @Test
    void testDeleteDirDoesNotFollowSymlink() {
        def base = Files.createTempDirectory("deleteDirSymlink").toFile()

        // a directory outside the tree being deleted, holding a file that must survive
        def outside = new File(base, "outside")
        outside.mkdir()
        def survivor = new File(outside, "survivor.txt")
        survivor.write("keep")

        // the tree we delete, containing a symlink pointing at the outside directory
        def tree = new File(base, "tree")
        tree.mkdir()
        def link = new File(tree, "link")
        try {
            Files.createSymbolicLink(link.toPath(), outside.toPath())
        } catch (IOException | UnsupportedOperationException e) {
            assumeTrue(false, "symbolic links not supported on this platform: $e")
        }

        assert tree.deleteDir()

        // the link and its parent are gone, but the target's contents are untouched
        assert !tree.exists()
        assert survivor.exists()
        assert survivor.text == "keep"

        outside.deleteDir()
        base.deleteDir()
    }

    @Test
    void testDeleteDirOnSymlinkDoesNotFollowIntoTarget() {
        def base = Files.createTempDirectory("deleteDirSymlinkSelf").toFile()

        def outside = new File(base, "outside")
        outside.mkdir()
        def survivor = new File(outside, "survivor.txt")
        survivor.write("keep")

        // call deleteDir directly on a symlink pointing at the outside directory
        def link = new File(base, "link")
        try {
            Files.createSymbolicLink(link.toPath(), outside.toPath())
        } catch (IOException | UnsupportedOperationException e) {
            assumeTrue(false, "symbolic links not supported on this platform: $e")
        }

        assert link.deleteDir()

        // the link itself is removed, but the target and its contents survive
        assert !Files.exists(link.toPath(), LinkOption.NOFOLLOW_LINKS)
        assert outside.exists()
        assert survivor.exists()
        assert survivor.text == "keep"

        base.deleteDir()
    }
}
