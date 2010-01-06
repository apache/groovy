/*
 * Copyright 2003-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy

import static groovy.io.FileType.*

/**
 * Unit test for File GDK methods
 *
 * @author Marc Guillemot
 * @author Paul King
 * @version $Revision: 4996 $
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
        createFile "foo"
        createFile "foo.txt"
    }

    void testEachFile() {
        def collectedFiles = []
        baseDir.eachFile {it -> collectedFiles << it.name }
        collectedFiles.sort() // needs to sort as there is no guarantee on the order within a folder

        def expected = ["emptyFolder", "folder1", "folder2", "folder3", "foo", "foo.txt"]

        assertEquals expected, collectedFiles
    }

    void testEachFileOnlyFiles() {
        def collectedFiles = []
        baseDir.eachFile FilesOnly, {it -> collectedFiles << it.name }
        collectedFiles.sort() // needs to sort as there is no guarantee on the order within a folder
        def expected = ["foo", "foo.txt"]
        assertEquals expected, collectedFiles
        collectedFiles = []
        new File(baseDir, 'folder2').eachFile(FilesOnly) {it -> collectedFiles << it.name }
        collectedFiles.sort() // needs to sort as there is no guarantee on the order within a folder
        expected = ['myDoc.doc', 'myDoc.odt']
        assertEquals expected, collectedFiles
    }

    void testEachDir() {
        def collectedFiles = []
        baseDir.eachDir {it -> collectedFiles << it.name }
        collectedFiles.sort() // needs to sort as there is no guarantee on the order within a folder

        def expected = ["emptyFolder", "folder1", "folder2", "folder3"]

        assertEquals expected, collectedFiles
    }

    void testEachFileMatch() {
        def collectedFiles = []
        baseDir.eachFileMatch ~/fo.*/, {it -> collectedFiles << it.name }
        collectedFiles.sort() // needs to sort as there is no guarantee on the order within a folder

        def expected = ["folder1", "folder2", "folder3", "foo", "foo.txt"]

        assertEquals expected, collectedFiles
    }

    void testEachFileMatchOnlyFiles() {
        def collectedFiles = []
        baseDir.eachFileMatch FilesOnly, ~/fo.*/, {it -> collectedFiles << it.name }
        collectedFiles.sort() // needs to sort as there is no guarantee on the order within a folder

        def expected = ["foo", "foo.txt"]

        assertEquals expected, collectedFiles
    }

    void testEachDirMatch() {
        def collectedFiles = []
        baseDir.eachDirMatch ~/fo.*/, {it -> collectedFiles << it.name }
        collectedFiles.sort() // needs to sort as there is no guarantee on the order within a folder

        def expected = ["folder1", "folder2", "folder3"]

        assertEquals expected, collectedFiles
    }

    void testEachFileRecurse() {
        def collectedFiles = []
        baseDir.eachFileRecurse {it -> collectedFiles << it.name }
        collectedFiles.sort() // needs to sort as there is no guarantee on the order within a folder

        def expected = ["Readme", "build.xml", "emptyFolder",
                "file1.groovy", "file2.groovy", "file3.groovy", "folder1", "folder2", "folder3",
                "foo", "foo.txt",
                "myDoc.doc", "myDoc.odt",
                "subfolder", "subfolder"]

        assertEquals expected, collectedFiles
    }

    void testEachFileRecurseFilesOnly() {
        def collectedFiles = []
        baseDir.eachFileRecurse(FilesOnly) {it -> collectedFiles << it.name }
        collectedFiles.sort() // needs to sort as there is no guarantee on the order within a folder

        def expected = ["Readme", "build.xml",
                "file1.groovy", "file2.groovy", "file3.groovy",
                "foo", "foo.txt",
                "myDoc.doc", "myDoc.odt"]

        assertEquals expected, collectedFiles
    }

    void testEachDirRecurse() {
        def collectedFiles = []
        baseDir.eachDirRecurse {it -> collectedFiles << it.name }
        collectedFiles.sort() // needs to sort as there is no guarantee on the order within a folder

        def expected = ["emptyFolder", "folder1", "folder2", "folder3", "subfolder", "subfolder",]

        assertEquals expected, collectedFiles
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
