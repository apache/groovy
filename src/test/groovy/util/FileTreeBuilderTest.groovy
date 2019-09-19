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
package groovy.util

import groovy.test.GroovyTestCase

class FileTreeBuilderTest extends GroovyTestCase {
    File tmpDir
    FileTreeBuilder builder

    void setUp() {
        super.setUp()
        tmpDir = File.createTempDir()
        builder = new FileTreeBuilder(tmpDir)
    }

    void tearDown() {
        tmpDir.deleteDir()
        builder = null
        tmpDir = null
    }

    void testFileWithText() {
        def file = builder.file('foo.txt','foo')
        assert file.exists()
        assert file.text == 'foo'
    }

    void testFileWithBytes() {
        def file = builder.file('foo.txt','foo'.getBytes('utf-8'))
        assert file.exists()
        assert file.getText('utf-8') == 'foo'
    }

    void testFileWithFile() {
        def f1 = builder.file('foo.txt', 'foo')
        def f2 = builder.file('bar.txt', f1)
        assert f2.exists()
        assert f2.text == 'foo'
    }

    void testFileWithClosureSpec() {
        def file = builder.file('foo.txt') { file ->
            file << 'foo'
        }
        assert file.exists()
        assert file.text == 'foo'
        def file2 = builder.file('foo.txt') {
            withWriter('utf-8') {
                it.write('foo')
            }
        }
        assert file2.exists()
        assert file2.text == 'foo'
    }

    void testDir() {
        def dir = builder.dir('sub')
        assert dir.directory
        assert dir.parentFile == builder.baseDir
    }

    void testDirWithClosure() {
        File f = null
        def dir = builder.dir('sub') {
            f = file('foo.txt','foo')
        }
        assert dir.directory
        assert dir.parentFile == builder.baseDir
        assert f.text == 'foo'
        assert f.parentFile == dir
    }

    void testCall() {
        File s1=null,s2=null,f1=null,f2=null
        builder {
            s1=dir('sub1') {
                f1=file('foo.txt','foo')
            }
            s2=dir('sub2') {
                f2=file('bar.txt', 'bar')
            }
        }
        assert f1.text == 'foo'
        assert f2.text == 'bar'
        assert f1.parentFile == s1
        assert f2.parentFile == s2
        assert s1.parentFile == builder.baseDir
        assert s2.parentFile == builder.baseDir
    }

    void testCreateDirWithMethodMissing() {
        File dir = builder.dir {}
        assert dir.directory
        assert dir.name == 'dir'
        assert dir.parentFile == builder.baseDir
    }

    void testCreateFileWithMethodMissing() {
        File f1 = builder.'foo.txt'('foo')
        File f2 = builder.'bar.txt'('foo'.getBytes('utf-8'))
        File f3 = builder.'baz.txt'(f2)
        [f1,f2,f3].each {
            assert it.exists()
            assert it.getText('utf-8') == 'foo'
        }
    }
}
