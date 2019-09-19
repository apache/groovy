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

import groovy.test.GroovyTestCase

import static groovy.io.FileType.*
import static groovy.io.FileVisitResult.*

/**
 * Unit test for File GDK methods
 */
class FileTest extends GroovyTestCase {

    def baseDir = new File("target/test-resources/filetest")

    void setUp() {
        createFolder "emptyFolder"
        createFile "folder1/Readme"
        createFile "folder1/build.xml"
        createFile "folder2/myDoc.doc"
        createFile "folder2/myDoc.odt"
        createFile "folder2/subfolder/file1.groovy"
        createFile "folder2/subfolder/file2.groovy"
        createFile "folder3/subfolder/file3.groovy"
        createFile "folder3/subfolder2/file4.groovy"
        createFile "foo"
        createFile "foo.txt"
    }

    void testEachFile() {
        def names = []
        baseDir.eachFile {it -> names << it.name }
        names.sort() // needs to sort as there is no guarantee on the order within a folder
        def expected = ["emptyFolder", "folder1", "folder2", "folder3", "foo", "foo.txt"]
        assert names == expected
    }

    void testEachFileOnlyFiles() {
        def names = []
        baseDir.eachFile FILES, {it -> names << it.name }
        names.sort() // needs to sort as there is no guarantee on the order within a folder
        def expected = ["foo", "foo.txt"]
        assert names == expected

        names = []
        new File(baseDir, 'folder2').eachFile(FILES) {it -> names << it.name }
        names.sort() // needs to sort as there is no guarantee on the order within a folder
        expected = ['myDoc.doc', 'myDoc.odt']
        assert names == expected
    }

    void testEachDir() {
        def names = []
        baseDir.eachDir {it -> names << it.name }
        names.sort() // needs to sort as there is no guarantee on the order within a folder
        def expected = ["emptyFolder", "folder1", "folder2", "folder3"]
        assert names == expected
    }

    void testEachFileMatch() {
        def names = []
        baseDir.eachFileMatch ~/fo.*/, {it -> names << it.name }
        names.sort() // needs to sort as there is no guarantee on the order within a folder
        def expected = ["folder1", "folder2", "folder3", "foo", "foo.txt"]
        assert names == expected
    }

    void testEachFileMatchOnlyFiles() {
        def names = []
        baseDir.eachFileMatch FILES, ~/fo.*/, {it -> names << it.name }
        names.sort() // needs to sort as there is no guarantee on the order within a folder
        def expected = ["foo", "foo.txt"]
        assert names == expected
    }

    void testEachDirMatch() {
        def names = []
        baseDir.eachDirMatch ~/fo.*/, {it -> names << it.name }
        names.sort() // needs to sort as there is no guarantee on the order within a folder
        def expected = ["folder1", "folder2", "folder3"]
        assert names == expected
    }

    void testEachFileRecurse() {
        def names = []
        baseDir.eachFileRecurse {it -> names << it.name }
        names.sort() // needs to sort as there is no guarantee on the order within a folder
        def expected = ["Readme", "build.xml", "emptyFolder",
                "file1.groovy", "file2.groovy", "file3.groovy", "file4.groovy", "folder1", "folder2", "folder3",
                "foo", "foo.txt", "myDoc.doc", "myDoc.odt", "subfolder", "subfolder", "subfolder2"]
        assert names == expected
    }

    void testEachFileRecurseFilesOnly() {
        def names = []
        baseDir.eachFileRecurse(FILES) {it -> names << it.name }
        names.sort() // needs to sort as there is no guarantee on the order within a folder
        def expected = ["Readme", "build.xml",
                "file1.groovy", "file2.groovy", "file3.groovy", "file4.groovy",
                "foo", "foo.txt",
                "myDoc.doc", "myDoc.odt"]
        assert names == expected
    }

    void testEachDirRecurse() {
        def names = []
        baseDir.eachDirRecurse {it -> names << it.name }
        names.sort() // needs to sort as there is no guarantee on the order within a folder
        def expected = ["emptyFolder", "folder1", "folder2", "folder3", "subfolder", "subfolder", "subfolder2",]
        assert names == expected
    }

    void testTraverseDirRecurse() {
        def names = []
        baseDir.traverse(type:DIRECTORIES) {it -> names << it.name }
        names.sort() // needs to sort as there is no guarantee on the order within a folder
        def expected = ["emptyFolder", "folder1", "folder2", "folder3", "subfolder", "subfolder", "subfolder2",]
        assert names == expected

        names = []
        baseDir.traverse(maxDepth:0, type:DIRECTORIES) {it -> names << it.name }
        names.sort() // needs to sort as there is no guarantee on the order within a folder
        expected = ["emptyFolder", "folder1", "folder2", "folder3"]
        assert names == expected

        names = []
        baseDir.traverse(maxDepth: 1, type:DIRECTORIES) {it -> names << it.name }
        names.sort() // needs to sort as there is no guarantee on the order within a folder
        expected = ["emptyFolder", "folder1", "folder2", "folder3", "subfolder", "subfolder", "subfolder2",]
        assert names == expected

        names = []
        baseDir.traverse(maxDepth: 2, type:DIRECTORIES) {it -> names << it.name }
        names.sort() // needs to sort as there is no guarantee on the order within a folder
        expected = ["emptyFolder", "folder1", "folder2", "folder3", "subfolder", "subfolder", "subfolder2",]
        assert names == expected
    }

    void testTraverseFilesAndDirectoriesRecurse() {
        def names = []
        baseDir.traverse(type:ANY) {it -> names << it.name }
        names.sort() // needs to sort as there is no guarantee on the order within a folder
        def expected = ["Readme", "build.xml", "emptyFolder", "file1.groovy", "file2.groovy", "file3.groovy", "file4.groovy",
                "folder1", "folder2", "folder3", "foo", "foo.txt", "myDoc.doc", "myDoc.odt", "subfolder", "subfolder", "subfolder2"]
        assert names == expected

        names = []
        baseDir.traverse {it -> names << it.name }
        names.sort() // needs to sort as there is no guarantee on the order within a folder
        expected = ["Readme", "build.xml", "emptyFolder", "file1.groovy", "file2.groovy", "file3.groovy", "file4.groovy",
                "folder1", "folder2", "folder3", "foo", "foo.txt", "myDoc.doc", "myDoc.odt", "subfolder", "subfolder", "subfolder2"]
        assert names == expected
    }

    void testTraverseFileRecurse() {
        def names = []
        baseDir.traverse(type:FILES) {it -> names << it.name }
        names.sort() // needs to sort as there is no guarantee on the order within a folder
        def expected = ["Readme", "build.xml", "file1.groovy", "file2.groovy", "file3.groovy", "file4.groovy",
                "foo", "foo.txt", "myDoc.doc", "myDoc.odt"]
        assert names == expected

        names = []
        baseDir.traverse(type:FILES, visit:{it -> names << it.name })
        names.sort() // needs to sort as there is no guarantee on the order within a folder
        expected = ["Readme", "build.xml", "file1.groovy", "file2.groovy", "file3.groovy", "file4.groovy",
                "foo", "foo.txt", "myDoc.doc", "myDoc.odt"]
        assert names == expected

        names = []
        baseDir.traverse(maxDepth:0, type:FILES) {it -> names << it.name }
        names.sort() // needs to sort as there is no guarantee on the order within a folder
        expected = ["foo", "foo.txt"]
        assert names == expected

        names = []
        baseDir.traverse(maxDepth: 1, type:FILES) {it -> names << it.name }
        names.sort() // needs to sort as there is no guarantee on the order within a folder
        expected = ["Readme", "build.xml", "foo", "foo.txt", "myDoc.doc", "myDoc.odt"]
        assert names == expected

        names = []
        baseDir.traverse(maxDepth: 2, type:FILES) {it -> names << it.name }
        names.sort() // needs to sort as there is no guarantee on the order within a folder
        expected = ["Readme", "build.xml", "file1.groovy", "file2.groovy", "file3.groovy", "file4.groovy",
                "foo", "foo.txt", "myDoc.doc", "myDoc.odt"]
        assert names == expected
    }

    void testTraverseFileRecurseWithFilter() {
        def byName = { it.name }

        def names = []
        baseDir.traverse(type:FILES, nameFilter:~/.*\..*/, sort:byName) {it -> names << it.name }
        def expected = ["build.xml", "myDoc.doc", "myDoc.odt", "file1.groovy",
                "file2.groovy", "file3.groovy", "file4.groovy", "foo.txt"]
        assert names == expected

        names = []
        baseDir.traverse(type:FILES, excludeNameFilter:~/file\d\.groovy/, sort:byName) {it -> names << it.name }
        expected = ["Readme", "build.xml", "myDoc.doc", "myDoc.odt", "foo", "foo.txt"]
        assert names == expected

        names = []
        baseDir.traverse(type:FILES, filter: { it.name ==~ /file\d\.groovy/}, sort:byName) {it -> names << it.name }
        expected = ["file1.groovy", "file2.groovy", "file3.groovy", "file4.groovy"]
        assert names == expected

        names = []
        baseDir.traverse(type:FILES, nameFilter: ~/file\d\.groovy/, excludeNameFilter: ~/file[24]\.groovy/, sort:byName) {it -> names << it.name }
        expected = ["file1.groovy", "file3.groovy"]
        assert names == expected
    }

    void testTraverseFileRecurseWithPrePost() {
        def names = []
        def pre = { names << "pre($it.name)" }
        def post = { names << "post($it.name)" }
        baseDir.traverse(type:FILES, preDir:pre, postDir:post) { names << it.name }
        names.sort() // needs to sort as there is no guarantee on the order within a folder
        def expected = ["Readme", "build.xml", "file1.groovy", "file2.groovy", "file3.groovy", "file4.groovy",
                "foo", "foo.txt", "myDoc.doc", "myDoc.odt", "post(emptyFolder)", "post(folder1)",
                "post(folder2)", "post(folder3)", "post(subfolder)", "post(subfolder)", "post(subfolder2)", "pre(emptyFolder)",
                "pre(folder1)", "pre(folder2)", "pre(folder3)", "pre(subfolder)", "pre(subfolder)", "pre(subfolder2)"]
        assert names == expected

        names = []
        baseDir.traverse(maxDepth: 0, type:FILES, preRoot:true, preDir:pre, postDir:post) { names << it.name }
        names.sort() // needs to sort as there is no guarantee on the order within a folder
        expected = ["foo", "foo.txt", "pre(filetest)"]
        assert names == expected

        names = []
        baseDir.traverse(maxDepth: 0, type:FILES, postRoot:true, preDir:pre, postDir:post) { names << it.name }
        names.sort() // needs to sort as there is no guarantee on the order within a folder
        expected = ["foo", "foo.txt", "post(filetest)"]
        assert names == expected

        names = []
        baseDir.traverse(maxDepth: 1, type:FILES, preRoot:true, postRoot:true, preDir:pre, postDir:post) { names << it.name }
        names.sort() // needs to sort as there is no guarantee on the order within a folder
        expected = ["Readme", "build.xml", "foo", "foo.txt", "myDoc.doc", "myDoc.odt", "post(emptyFolder)", "post(filetest)", "post(folder1)",
                "post(folder2)", "post(folder3)", "pre(emptyFolder)", "pre(filetest)", "pre(folder1)", "pre(folder2)", "pre(folder3)"]
        assert names == expected

        names = []
        baseDir.traverse(maxDepth: 2, type:FILES, preDir:pre, postDir:post) { names << it.name }
        names.sort() // needs to sort as there is no guarantee on the order within a folder
        expected = ["Readme", "build.xml", "file1.groovy", "file2.groovy", "file3.groovy", "file4.groovy",
                "foo", "foo.txt", "myDoc.doc", "myDoc.odt", "post(emptyFolder)", "post(folder1)",
                "post(folder2)", "post(folder3)", "post(subfolder)", "post(subfolder)", "post(subfolder2)", "pre(emptyFolder)",
                "pre(folder1)", "pre(folder2)", "pre(folder3)", "pre(subfolder)", "pre(subfolder)", "pre(subfolder2)"]
        assert names == expected
    }

    void testTraverseFileRecurseWithPrePostEarlyTermination() {
        def names = []
        def byName = { it.name }
        def pre = { names << "pre($it.name)" }
        def pre2 = {
            names << "pre($it.name)"
            if (it.name == 'subfolder') return SKIP_SUBTREE
        }
        def post = { names << "post($it.name)" }
        def post2 = {
            names << "post($it.name)"
            if (it.name == 'folder2') return TERMINATE
        }
        def post3 = {
            names << "post($it.name)"
            if (it.name == 'subfolder') return SKIP_SIBLINGS
        }
        baseDir.traverse(type:FILES, preDir:pre, postDir:post, sort:byName) { names << it.name }
        def expected = ["pre(emptyFolder)", "post(emptyFolder)",
                "pre(folder1)", "Readme", "build.xml", "post(folder1)",
                "pre(folder2)", "myDoc.doc", "myDoc.odt", "pre(subfolder)", "file1.groovy", "file2.groovy", "post(subfolder)", "post(folder2)",
                "pre(folder3)", "pre(subfolder)", "file3.groovy", "post(subfolder)", "pre(subfolder2)", "file4.groovy", "post(subfolder2)", "post(folder3)", "foo", "foo.txt"]
        assert names == expected

        names = []
        baseDir.traverse(maxDepth: 1, type:FILES, preDir:pre, postDir:post, sort:byName) { names << it.name }
        expected = ["pre(emptyFolder)", "post(emptyFolder)",
                "pre(folder1)", "Readme", "build.xml", "post(folder1)",
                "pre(folder2)", "myDoc.doc", "myDoc.odt", "post(folder2)",
                "pre(folder3)", "post(folder3)", "foo", "foo.txt"]
        assert names == expected

        names = []
        baseDir.traverse(maxDepth: 2, type:FILES, preDir:pre, postDir:post, sort:byName) { names << it.name }
        expected = ["pre(emptyFolder)", "post(emptyFolder)",
                "pre(folder1)", "Readme", "build.xml", "post(folder1)",
                "pre(folder2)", "myDoc.doc", "myDoc.odt", "pre(subfolder)", "file1.groovy", "file2.groovy", "post(subfolder)", "post(folder2)",
                "pre(folder3)", "pre(subfolder)", "file3.groovy", "post(subfolder)", "pre(subfolder2)", "file4.groovy", "post(subfolder2)", "post(folder3)", "foo", "foo.txt"]
        assert names == expected

        names = []
        baseDir.traverse(type:FILES, preDir:pre2, postDir:post, sort:byName) { names << it.name }
        expected = ["pre(emptyFolder)", "post(emptyFolder)",
                "pre(folder1)", "Readme", "build.xml", "post(folder1)",
                "pre(folder2)", "myDoc.doc", "myDoc.odt", "pre(subfolder)", "post(subfolder)", "post(folder2)",
                "pre(folder3)", "pre(subfolder)", "post(subfolder)", "pre(subfolder2)", "file4.groovy", "post(subfolder2)", "post(folder3)", "foo", "foo.txt"]
        assert names == expected

        names = []
        baseDir.traverse(type:FILES, preDir:pre, postDir:post2, sort:byName) { names << it.name }
        expected = ["pre(emptyFolder)", "post(emptyFolder)",
                "pre(folder1)", "Readme", "build.xml", "post(folder1)",
                "pre(folder2)", "myDoc.doc", "myDoc.odt", "pre(subfolder)", "file1.groovy", "file2.groovy", "post(subfolder)", "post(folder2)"]
        assert names == expected

        names = []
        baseDir.traverse(type:FILES, preDir:pre, postDir:post3, sort:byName) { names << it.name }
        expected = ["pre(emptyFolder)", "post(emptyFolder)",
                "pre(folder1)", "Readme", "build.xml", "post(folder1)",
                "pre(folder2)", "myDoc.doc", "myDoc.odt", "pre(subfolder)", "file1.groovy", "file2.groovy", "post(subfolder)", "post(folder2)",
                "pre(folder3)", "pre(subfolder)", "file3.groovy", "post(subfolder)", "post(folder3)", "foo", "foo.txt"]
        assert names == expected

        names = []
        baseDir.traverse(type:FILES, preDir:pre, postDir:post, sort:byName) { names << it.name; if (it.name == 'file1.groovy') return TERMINATE }
        expected = ["pre(emptyFolder)", "post(emptyFolder)",
                "pre(folder1)", "Readme", "build.xml", "post(folder1)",
                "pre(folder2)", "myDoc.doc", "myDoc.odt", "pre(subfolder)", "file1.groovy"]
        assert names == expected
    }

    def createFile(path) {
        def f = new File(baseDir, path)
        f.parentFile.mkdirs()
        f.createNewFile()
    }

    def createFolder(path) {
        def f = new File(baseDir, path)
        f.mkdirs()
    }
}
