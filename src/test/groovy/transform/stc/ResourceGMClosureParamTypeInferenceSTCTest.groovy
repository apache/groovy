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

package groovy.transform.stc
/**
 * Unit tests for static type checking : closure parameter type inference for {@link org.codehaus.groovy.runtime.ResourceGroovyMethods}.
 */
class ResourceGMClosureParamTypeInferenceSTCTest extends StaticTypeCheckingTestCase {
    void testEachByte() {
        assertScript '''
            File tmp = File.createTempFile('foo','tmp')
            tmp.deleteOnExit()
            byte b
            try {
                tmp << 'Groovy'
                tmp.eachByte {
                  b = it
                }
                assert b>0
            } finally{
                tmp.delete()
            }
        '''
        assertScript '''
            File tmp = File.createTempFile('foo','tmp')
            tmp.deleteOnExit()
            byte b
            try {
                tmp << 'Groovy'
                tmp.toURI().toURL().eachByte {
                  b = it
                }
                assert b>0
            } finally{
                tmp.delete()
            }
        '''
        assertScript '''
            File tmp = File.createTempFile('foo','tmp')
            tmp.deleteOnExit()
            byte b
            try {
                tmp << 'Groovy'
                tmp.eachByte(4) { buf, len ->
                  b = buf[0]
                }
                assert b>0
            } finally{
                tmp.delete()
            }
        '''
        assertScript '''
            File tmp = File.createTempFile('foo','tmp')
            tmp.deleteOnExit()
            byte b
            try {
                tmp << 'Groovy'
                tmp.toURI().toURL().eachByte(4) { buf, len ->
                  b = buf[0]
                }
                assert b>0
            } finally{
                tmp.delete()
            }
        '''
    }

    void testEachDirXYZ() {
        assertScript '''
            File tmp = File.createTempDir()
            try {
                tmp.eachDir {
                    println it.name
                }
                tmp.eachDirRecurse {
                    println it.name
                }
                tmp.eachDirMatch('foo') {
                    println it.name
                }

            } finally{
                tmp.deleteDir()
            }

        '''
    }
    void testEachFileXYZ() {
        assertScript '''import groovy.io.FileType
            File tmp = File.createTempDir()
            try {
                tmp.eachFile {
                    println it.name
                }
                tmp.eachFile(FileType.ANY) {
                    println it.name
                }
                tmp.eachFileMatch('foo') {
                    println it.name
                }
                tmp.eachFileMatch(FileType.ANY, 'foo') {
                    println it.name
                }
                tmp.eachFileRecurse {
                    println it.name
                }
                tmp.eachFileRecurse(FileType.ANY) {
                    println it.name
                }
            } finally{
                tmp.deleteDir()
            }

        '''
    }

    void testEachLineWithFile() {
        assertScript '''
        File src = File.createTempFile("test","tmp")
        try {
            src.deleteOnExit()
            src << """line1
line2
line3"""
            src.eachLine {
                assert it.startsWith('line')
            }
            src.eachLine { line, i ->
                assert line.toLowerCase() == "line${i}"
            }
            src.eachLine(1) { line ->
                assert line.startsWith('line')
            }
            src.eachLine(1) { line,i ->
                assert line.toLowerCase() == "line${i}"
            }
            src.eachLine('utf-8') {
                assert it.startsWith('line')
            }
            src.eachLine('utf-8') { line, i ->
                assert line.toLowerCase() == "line${i}"
            }
            src.eachLine('utf-8',1) { line ->
                assert line.startsWith('line')
            }
            src.eachLine('utf-8',1) { line,i ->
                assert line.toLowerCase() == "line${i}"
            }
        } finally {
            src.delete()
        }'''
    }

    void testEachLineWithURL() {
        assertScript '''
        File src = File.createTempFile("test","tmp")
        try {
            src.deleteOnExit()
            src << """line1
line2
line3"""
            src.toURI().toURL().eachLine {
                assert it.startsWith('line')
            }
            src.toURI().toURL().eachLine { line, i ->
                assert line.toLowerCase() == "line${i}"
            }
            src.toURI().toURL().eachLine(1) { line ->
                assert line.startsWith('line')
            }
            src.toURI().toURL().eachLine(1) { line,i ->
                assert line.toLowerCase() == "line${i}"
            }
            src.toURI().toURL().eachLine('utf-8') {
                assert it.startsWith('line')
            }
            src.toURI().toURL().eachLine('utf-8') { line, i ->
                assert line.toLowerCase() == "line${i}"
            }
            src.toURI().toURL().eachLine('utf-8',1) { line ->
                assert line.startsWith('line')
            }
            src.toURI().toURL().eachLine('utf-8',1) { line,i ->
                assert line.toLowerCase() == "line${i}"
            }
        } finally {
            src.delete()
        }'''
    }

    void testFilterLineOnFile() {
        assertScript '''
        File src = File.createTempFile("test","tmp")
        try {
            src.deleteOnExit()
            src << """line1
line2
line3"""
            def wrt = src.filterLine {
                it.toUpperCase() == 'LINE2'
            }
            assert wrt.toString().startsWith('line2')
            wrt = src.filterLine('utf-8') {
                it.toUpperCase() == 'LINE2'
            }
            assert wrt.toString().startsWith('line2')
            wrt = new StringWriter()
            src.filterLine(wrt) {
                it.toUpperCase() == 'LINE2'
            }
            assert wrt.toString().startsWith('line2')
            wrt = new StringWriter()
            src.filterLine(wrt,'utf-8') {
                it.toUpperCase() == 'LINE2'
            }
            assert wrt.toString().startsWith('line2')
        } finally {
            src.delete()
        }'''

    }

    void testFilterLineOnURL() {
        assertScript '''
        File src = File.createTempFile("test","tmp")
        try {
            src.deleteOnExit()
            src << """line1
line2
line3"""
            def wrt = src.toURI().toURL().filterLine {
                it.toUpperCase() == 'LINE2'
            }
            assert wrt.toString().startsWith('line2')
            wrt = src.toURI().toURL().filterLine('utf-8') {
                it.toUpperCase() == 'LINE2'
            }
            assert wrt.toString().startsWith('line2')
            wrt = new StringWriter()
            src.toURI().toURL().filterLine(wrt) {
                it.toUpperCase() == 'LINE2'
            }
            assert wrt.toString().startsWith('line2')
            wrt = new StringWriter()
            src.toURI().toURL().filterLine(wrt,'utf-8') {
                it.toUpperCase() == 'LINE2'
            }
            assert wrt.toString().startsWith('line2')
        } finally {
            src.delete()
        }'''

    }

    void testSplitEachLineOnFile() {
        assertScript '''
        File src = File.createTempFile("test","tmp")
        try {
            src.deleteOnExit()
            src << """a;A
b;B
c;C"""
            src.splitEachLine(/;/) {
                assert it[0] == it[1].toLowerCase()
            }
            src.splitEachLine(/;/) { a,b ->
                assert a == b.toLowerCase()
            }
            src.splitEachLine(/;/,'utf-8') {
                assert it[0] == it[1].toLowerCase()
            }
            src.splitEachLine(/;/,'utf-8\') { a,b ->
                assert a == b.toLowerCase()
            }
            src.splitEachLine(~/;/) {
                assert it[0] == it[1].toLowerCase()
            }
            src.splitEachLine(~/;/) { a,b ->
                assert a == b.toLowerCase()
            }
            src.splitEachLine(~/;/,'utf-8') {
                assert it[0] == it[1].toLowerCase()
            }
            src.splitEachLine(~/;/,'utf-8\') { a,b ->
                assert a == b.toLowerCase()
            }

        } finally {
            src.delete()
        }'''

    }

    void testSplitEachLineOnURL() {
        assertScript '''
        File src = File.createTempFile("test","tmp")
        try {
            src.deleteOnExit()
            src << """a;A
b;B
c;C"""
            src.toURI().toURL().splitEachLine(/;/) {
                assert it[0] == it[1].toLowerCase()
            }
            src.toURI().toURL().splitEachLine(/;/) { a,b ->
                assert a == b.toLowerCase()
            }
            src.toURI().toURL().splitEachLine(/;/,'utf-8') {
                assert it[0] == it[1].toLowerCase()
            }
            src.toURI().toURL().splitEachLine(/;/,'utf-8\') { a,b ->
                assert a == b.toLowerCase()
            }
            src.toURI().toURL().splitEachLine(~/;/) {
                assert it[0] == it[1].toLowerCase()
            }
            src.toURI().toURL().splitEachLine(~/;/) { a,b ->
                assert a == b.toLowerCase()
            }
            src.toURI().toURL().splitEachLine(~/;/,'utf-8') {
                assert it[0] == it[1].toLowerCase()
            }
            src.toURI().toURL().splitEachLine(~/;/,'utf-8\') { a,b ->
                assert a == b.toLowerCase()
            }

        } finally {
            src.delete()
        }'''

    }

    void testTraverse() {
        assertScript '''import groovy.io.FileType
            File tmp = File.createTempDir()
            try {
                tmp.traverse {
                    if (it.name=='foo') {
                        println 'found'
                    }
                }
                tmp.traverse([:]) {
                    if (it.name=='foo') {
                        println 'found'
                    }
                }
            } finally{
                tmp.deleteDir()
            }

        '''
    }

    void testWithDataInputOutputStream() {
        assertScript '''
            def tmp = File.createTempFile('test','tmp')
            tmp.deleteOnExit()
            try {
                tmp.withDataOutputStream {
                    it.writeUTF('Groovy')
                }
                String read
                tmp.withDataInputStream {
                    read = it.readUTF()
                }
                assert read=='Groovy'
            } finally {
                tmp.delete()
            }
        '''
    }

    void testWithInputOutputStream() {
        assertScript '''
            def tmp = File.createTempFile('test','tmp')
            tmp.deleteOnExit()
            try {
                tmp.withOutputStream { it.write(123) }
                int read
                tmp.withInputStream {
                    read = it.read()
                }
                assert read==123
                tmp.toURI().toURL().withInputStream {
                    read = it.read()
                }
                assert read==123
            } finally {
                tmp.delete()
            }
        '''
    }

    void testWithObjectInputOutputStream() {
        assertScript '''
            def tmp = File.createTempFile('test','tmp')
            tmp.deleteOnExit()
            try {
                tmp.withObjectOutputStream { it.writeInt(123) }
                int read
                tmp.withObjectInputStream {
                    read = it.readInt()
                }
                assert read==123
                tmp.withObjectInputStream(this.class.classLoader) {
                    read = it.readInt()
                }
                assert read==123
            } finally {
                tmp.delete()
            }
        '''
    }

    void testWithPrintWriter() {
        assertScript '''
            def tmp = File.createTempFile('test','tmp')
            tmp.deleteOnExit()
            try {
                tmp.withPrintWriter {
                    it.write 'Groovy'
                }
                assert tmp.text == 'Groovy'
                tmp.withPrintWriter('utf-8') {
                    it.write 'Groovy'
                }
                assert tmp.text == 'Groovy'
            } finally {
                tmp.delete()
            }

'''
    }

    void testWithReaderWriter() {
        assertScript '''
            def tmp = File.createTempFile('test','tmp')
            tmp.deleteOnExit()
            try {
                tmp.withWriter {
                    it.write 'Groovy'
                }
                tmp.withWriter('utf-8') {
                    it.write 'Groovy'
                }
                tmp.withWriterAppend {
                    it.write 'Groovy'
                }
                tmp.withWriterAppend('utf-8') {
                    it.write 'Groovy'
                }
                int read = 0
                tmp.withReader {
                    read = it.read()
                }
                assert read as char == 'G'
                read = 0
                tmp.withReader('utf-8') {
                    read = it.read()
                }
                assert read as char == 'G'
                read = 0
                tmp.toURI().toURL().withReader('utf-8') {
                    read = it.read()
                }
                assert read as char == 'G'
                read = 0
                tmp.toURI().toURL().withReader('utf-8') {
                    read = it.read()
                }
                assert read as char == 'G'
            } finally {
                tmp.delete()
            }

'''

        assertScript '''
            def tmp = File.createTempFile('test','tmp')
            tmp.deleteOnExit()
            try {
                tmp.withWriter { BufferedWriter it ->
                    it.write 'Groovy'
                }
                tmp.withWriter('utf-8') { BufferedWriter it ->
                    it.write 'Groovy'
                }
                tmp.withWriterAppend { BufferedWriter it ->
                    it.write 'Groovy'
                }
                tmp.withWriterAppend('utf-8') { BufferedWriter it ->
                    it.write 'Groovy'
                }
                int read = 0
                tmp.withReader { BufferedReader it ->
                    read = it.read()
                }
                assert read as char == 'G'
                read = 0
                tmp.withReader('utf-8') { BufferedReader it ->
                    read = it.read()
                }
                assert read as char == 'G'
                read = 0
                tmp.toURI().toURL().withReader('utf-8') { Reader it ->
                    read = it.read()
                }
                assert read as char == 'G'
                read = 0
                tmp.toURI().toURL().withReader('utf-8') { Reader it ->
                    read = it.read()
                }
                assert read as char == 'G'
            } finally {
                tmp.delete()
            }

'''
    }

}
