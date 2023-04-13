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

import org.junit.After
import org.junit.Before
import org.junit.Test

final class FileTreeBuilderTest {

    private File tmpDir
    private FileTreeBuilder root

    @Before
    void setUp() {
        tmpDir = File.createTempDir()
        root = new FileTreeBuilder(tmpDir)
    }

    @After
    void tearDown() {
        tmpDir.deleteDir()
    }

    @Test
    void testFileWithText() {
        def file = root.file('foo.txt','foo')
        assert file.exists()
        assert file.text == 'foo'
    }

    @Test
    void testFileWithBytes() {
        def file = root.file('foo.txt','foo'.getBytes('utf-8'))
        assert file.exists()
        assert file.getText('utf-8') == 'foo'
    }

    @Test
    void testFileWithFile() {
        def f1 = root.file('foo.txt', 'foo')
        def f2 = root.file('bar.txt', f1)
        assert f2.exists()
        assert f2.text == 'foo'
    }

    @Test
    void testFileWithClosureSpec() {
        def file = root.file('foo.txt') { foo_txt ->
            foo_txt << 'foo'
        }
        assert file.exists()
        assert file.text == 'foo'

        def file2 = root.file('foo.txt') {
            withWriter('utf-8') {
                it.write('foo')
            }
        }
        assert file2.exists()
        assert file2.text == 'foo'
    }

    @Test
    void testDir() {
        def dir = root.dir('sub')
        assert dir.isDirectory()
        assert dir.name == 'sub'
        assert dir.parentFile == root.baseDir
    }

    @Test
    void testDirWithClosure() {
        File f = null
        def dir = root.dir('sub') {
            f = file('foo.txt','foo')
        }
        assert dir.isDirectory()
        assert dir.parentFile == root.baseDir
        assert f.text == 'foo'
        assert f.parentFile == dir
    }

    @Test
    void testCall() {
        File s1, s2, f1, f2
        root {
            s1 = dir('sub1') {
                f1 = file('foo.txt','foo')
            }
            s2 = dir('sub2') {
                f2 = file('bar.txt', 'bar')
            }
        }
        assert f1.text == 'foo'
        assert f2.text == 'bar'
        assert f1.parentFile == s1
        assert f2.parentFile == s2
        assert s1.parentFile == root.baseDir
        assert s2.parentFile == root.baseDir
    }

    @Test
    void testCreateDirWithMethodMissing() {
        File dir = root.dir({}) // not dir(String) or dir(String,Closure)
        assert dir.isDirectory()
        assert dir.name == 'dir'
        assert dir.parentFile == root.baseDir
    }

    @Test
    void testCreateFileWithMethodMissing() {
        File f1 = root.'foo.txt'('foo')
        File f2 = root.'bar.txt'('foo'.getBytes('utf-8'))
        File f3 = root.'baz.txt'(f2)
        [f1, f2, f3].each {
            assert it.exists()
            assert it.getText('utf-8') == 'foo'
        }
    }
}
