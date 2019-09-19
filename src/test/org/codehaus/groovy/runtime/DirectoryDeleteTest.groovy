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

import groovy.test.GroovyTestCase;

/**
 * Test File.deleteDir() method in Groovy
 */
class DirectoryDeleteTest extends GroovyTestCase {

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
}
