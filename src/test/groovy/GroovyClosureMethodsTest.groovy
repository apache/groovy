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

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Test case for the eachObject method on a file containing
 * zero, one or more objects (object stream).  Also test cases
 * for eachDir, eachFileMatch and runAfter methods.
 *
 * @author Hein Meling
 */
class GroovyClosureMethodsTest extends GroovyTestCase {

    private String dirname_target = "target"
    private String dirname_source = "src/test/groovy"

    private String filename = "${dirname_target}/GroovyClosureMethodsTest.each.object"

    void testEachObjectMany() {
        def file = new File(filename)
        def oos = new ObjectOutputStream(new FileOutputStream(file))
        def list = [1, 2, 3, "foo", 9, "bar", 191, file, 9129]
        list.each {
            oos.writeObject(it)
        }

        println("Contents of file with multiple objects: " + file)
        int c = 0
        file.eachObject {
            print "${it} "
            c++
        }
        assert list.size() == c
        println ""
        //ensure to remove the created file
        file.delete()
    }

    void testEachObjectOne() {
        def file = new File(filename)
        def oos = new ObjectOutputStream(new FileOutputStream(file))
        oos.writeObject(file)

        println("Contents of file with one object: " + file)
        int c = 0
        file.eachObject {
            print "${it} "
            c++
        }
        assert c == 1
        println ""
        //ensure to remove the created file
        file.delete()
    }

    void testEachObjectEmptyFile() {
        def file = new File(filename)
        def oos = new ObjectOutputStream(new FileOutputStream(file))

        println("Contents of empty file: " + file)
        int c = 0
        file.eachObject {
            print "${it} "
            c++
        }
        assert c == 0
        println ""
        //ensure to remove the created file
        file.delete()
    }

    void testEachObjectNullFile() {
        def file = new File(filename)
        def oos = new ObjectOutputStream(new FileOutputStream(file))
        oos.writeObject(null)
        oos.writeObject("foo")
        oos.writeObject(null)

        println("Contents of null file: " + file)
        int c = 0
        file.eachObject {
            print "${it} "
            c++
        }
        assert c == 3
        println ""
        //ensure to remove the created file
        file.delete()
    }

    void testEachDir() {
        def dir = new File(dirname_source)

        println("Directories in: " + dir)
        int c = 0
        dir.eachDir {
            print "${it} "
            c++
        }
        println ""
        assert c > 0
    }

    void testEachFileMatch() {
        def file = new File(dirname_source)

        print "Files with the text Groovy: "
        file.eachFileMatch(~"^Groovy.*") {
            print "${it} "
        }
        println ""

        print "Files with the text Closure: "
        file.eachFileMatch(~"^Closure.*") {
            print "${it} "
        }
        println ""

        print "This file is here: "
        int c = 0
        file.eachFileMatch(~"^GroovyClosureMethodsTest.groovy") {
            print "${it} "
            c++
        }
        assert c == 1
        println ""
    }

    void testEachFileOnNonExistingDir() {
        shouldFail {
            File dir = new File("SomeNonExistingDir")
            dir.eachFile {
                println "${it} "
            }
        }
    }

    void testEachFileOnNonDirFile() {
        shouldFail {
            File dir = new File("${dirname_source}/GroovyClosureMethodsTest.groovy")
            dir.eachFile {
                println "${it} "
            }
        }
    }

    void testRunAfter() {
        CountDownLatch latch = new CountDownLatch(1)
        new Timer().runAfter(50) {
            latch.countDown()
        }
        assert latch.getCount() == 1
        latch.await(100L, TimeUnit.MILLISECONDS)
        assert latch.getCount() == 0
    }

    void testSplitEachLine() {
        String s = """A B C D
E F G H
1 2 3 4
"""
        Reader reader = new StringReader(s)
        def all_lines = []
        reader.splitEachLine(" ") { list ->
            all_lines << list
        }
        assert all_lines == [["A", "B", "C", "D"], ["E", "F", "G", "H"], ["1", "2", "3", "4"]]
    }

    void testSplitEachLinePattern() {
        String s = """A B C D
E F G H
1 2 3 4
"""
        def all_lines = []
        s.splitEachLine(~" ") { list ->
            all_lines << list
        }
        assert all_lines == [["A", "B", "C", "D"], ["E", "F", "G", "H"], ["1", "2", "3", "4"]]
    }

}
