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
package org.apache.groovy.nio.extensions

import groovy.io.FileType
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Files
import java.nio.file.StandardCopyOption

class NioExtensionsTest extends Specification {

    @TempDir
    private File tempDir

    private File tempFile

    def setup() {
        tempFile = File.createTempFile('temp', '.ext')
        tempFile.deleteOnExit()
    }

    def testShouldAppendByteArrayToFile() {
        when:
        def path = tempFile.toPath()
        path.write('Hello')
        byte[] bytes = ' World'.bytes
        path.append(bytes)

        then:
        path.text == 'Hello World'
    }

    def testShouldAppendStringToFileUsingDefaultEncoding() {
        when:
        def path = tempFile.toPath()
        path.write('Hello')
        path.append(' World')

        then:
        path.text == 'Hello World'
    }

    def testShouldAppendTextSuppliedByReaderToFileUsingDefaultEncoding() {
        when:
        def path = tempFile.toPath()
        path.write('Hello')
        Reader reader = new StringReader(' World')
        path.append(reader)

        then:
        path.text == 'Hello World'
    }

    def testShouldAppendTextSuppliedByWriterToFileUsingDefaultEncoding() {
        when:
        def path = tempFile.toPath()
        path.write('Hello')
        Writer writer = new StringWriter()
        writer.append(' World')
        path.append(writer)

        then:
        path.text == 'Hello World'
    }

    def testShouldAppendStringToFileUsingSpecifiedEncoding() {
        when:
        def path = tempFile.toPath()
        String encoding = 'UTF-8'
        path.write('؁', encoding)
        path.append(' ؁', encoding)

        then:
        path.getText(encoding) == '؁ ؁'
    }

    def testShouldAppendTextSuppliedByReaderToFileUsingSpecifiedEncoding() {
        when:
        def path = tempFile.toPath()
        String encoding = 'UTF-8'
        path.write('؁', encoding)
        Reader reader = new CharArrayReader([' ', '؁'] as char[])
        path.append(reader, encoding)

        then:
        path.getText(encoding) == '؁ ؁'
    }

    def testShouldAppendTextSuppliedByWriterToFileUsingSpecifiedEncoding() {
        when:
        def path = tempFile.toPath()
        String encoding = 'UTF-8'
        path.write('؁', encoding)
        Writer writer = new CharArrayWriter()
        writer.append(' ')
        writer.append('؁')
        path.append(writer, encoding)

        then:
        path.getText(encoding) == '؁ ؁'
    }

    def testShouldAppendStringToFileUsingSpecifiedEncodingWithBom() {
        when:
        def path = tempFile.toPath()
        Files.delete(path)
        String encoding = 'UTF-16LE'
        path.write('؁', encoding, true)
        path.append(' ؁', encoding, true)

        then:
        byte[] bytes = NioExtensions.getBytes(path)
        bytes[0] == -1 as byte
        bytes[1] == -2 as byte
        String string = path.getText(encoding)
        string.substring(1, string.size()) == '؁ ؁'
    }

    def testShouldAppendTextSuppliedByReaderToFileUsingSpecifiedEncodingWithBom() {
        when:
        def path = tempFile.toPath()
        Files.delete(path)
        String encoding = 'UTF-16LE'
        path.write('؁', encoding, true)
        Reader reader = new CharArrayReader([' ', '؁'] as char[])
        path.append(reader, encoding, true)

        then:
        byte[] bytes = NioExtensions.getBytes(path)
        bytes[0] == -1 as byte
        bytes[1] == -2 as byte
        String string = path.getText(encoding)
        string.substring(1, string.size()) == '؁ ؁'
    }

    def testShouldAppendTextSuppliedByWriterToFileUsingSpecifiedEncodingWithBom() {
        when:
        def path = tempFile.toPath()
        Files.delete(path)
        String encoding = 'UTF-16LE'
        path.write('؁', encoding, true)
        Writer writer = new CharArrayWriter()
        writer.append(' ')
        writer.append('؁')
        path.append(writer, encoding, true)

        then:
        byte[] bytes = NioExtensions.getBytes(path)
        bytes[0] == -1 as byte
        bytes[1] == -2 as byte
        String string = path.getText(encoding)
        string.substring(1, string.size()) == '؁ ؁'
    }

    def testPathSize() {
        when:
        def str = 'Hello world!'
        def path = tempFile.toPath()
        Files.copy(new ByteArrayInputStream(str.getBytes()), path, StandardCopyOption.REPLACE_EXISTING)

        then:
        path.size() == str.size()
    }

    def testPathName() {
        when:
        def file = new File(tempFile, 'gradle.properties')
        def dir = new File(tempFile, 'properties')

        then:
        file.name == file.toPath().name
        dir.name == dir.toPath().name
    }

    def testPathExtension() {
        when:
        def filePath = new File(tempFile, 'gradle.properties').toPath()
        def dirPath = new File(tempFile, 'properties').toPath()

        then:
        filePath.extension == 'properties'
        dirPath.extension == ""
    }

    def testPathBaseName() {
        when:
        def filePath = new File(tempFile, 'gradle.properties').toPath()
        def dirPath = new File(tempFile, 'properties').toPath()

        then:
        filePath.baseName == 'gradle'
        dirPath.baseName == 'properties'
    }

    def testNewObjectOutputStream() {
        setup:
        def str = 'Hello world!'
        def path = tempFile.toPath()

        when:
        def outputStream = path.newObjectOutputStream()
        outputStream.writeObject(str)
        outputStream.close()
        def inputStream = new ObjectInputStream(new FileInputStream(path.toFile()))

        then:
        inputStream.readObject() == str

        cleanup:
        inputStream?.close()
    }

    def testNewObjectInputStream() {
        setup:
        def str = 'Hello world!'
        def path = tempFile.toPath()
        def outputStream = new ObjectOutputStream(new FileOutputStream(path.toFile()))
        outputStream.writeObject(str)
        outputStream.close()

        when:
        def inputStream = path.newObjectInputStream()

        then:
        inputStream.readObject() == str

        cleanup:
        inputStream.close()
    }

    def testEachObject() {
        setup:
        def str1 = 'alpha'
        def str2 = 'beta'
        def str3 = 'delta'
        def path = tempFile.toPath()
        def stream = new ObjectOutputStream(new FileOutputStream(path.toFile()))
        stream.writeObject(str1)
        stream.writeObject(str2)
        stream.writeObject(str3)
        stream.close()

        when:
        def list = []
        path.eachObject { list << it }

        then:
        list == [str1, str2, str3]
    }

    def testReadLines() {
        setup:
        tempFile.text = 'alpha\nbeta\ndelta'

        when:
        def lines = tempFile.toPath().readLines()

        then:
        lines == ['alpha', 'beta', 'delta']
    }

    def testNewReader() {
        setup:
        def str = 'Hello world!'
        tempFile.text = str

        when:
        def reader = tempFile.toPath().newReader()
        def line = reader.readLine()

        then:
        line == str
        reader.readLine() == null
    }

    def testGetBytes() {
        when:
        tempFile.text = 'Hello world!'

        then:
        tempFile.toPath().getBytes() == 'Hello world!'.getBytes()
    }

    def testSetBytes() {
        when:
        tempFile.text = 'Hello world!'
        tempFile.toPath().setBytes('Ciao mundo!'.getBytes())

        then:
        tempFile.text == 'Ciao mundo!'
    }

    def testSetText() {
        when:
        tempFile.toPath().setText('Ciao mundo!')
        // invoke twice to make sure that the content is truncated by the setText method
        tempFile.toPath().setText('Hello world!')

        then:
        tempFile.text == 'Hello world!'
    }

    def testWrite() {
        when:
        def str = 'Hello world!'
        tempFile.toPath().write(str)

        then:
        tempFile.text == 'Hello world!'
    }

    def testWriteWithEncoding() {
        when:
        def str = 'Hello world!'
        tempFile.toPath().write('Ciao mundo!')
        tempFile.toPath().write(str, 'UTF-8')

        then:
        tempFile.text == str
    }

    def testWriteWithEncodingAndBom() {
        when:
        def str = '؁'
        tempFile.toPath().write(str, 'UTF-16LE', true)

        then:
        assert tempFile.getBytes() == [-1, -2, 1, 6] as byte[]
    }

    def testAppendObject() {
        setup:
        tempFile.text = 'alpha'

        when:
        tempFile.toPath().append('-gamma')

        then:
        tempFile.text == 'alpha-gamma'
    }

    def testAppendBytes() {
        setup:
        tempFile.text = 'alpha'

        when:
        tempFile.toPath().append('-beta'.getBytes())

        then:
        tempFile.text == 'alpha-beta'
    }

    def testAppendInputStream() {
        setup:
        tempFile.text = 'alpha'

        when:
        tempFile.toPath().append(new ByteArrayInputStream('-delta'.getBytes()))

        then:
        tempFile.text == 'alpha-delta'
    }

    def testLeftShift() {
        setup:
        def path = tempFile.toPath()

        when:
        path << 'Hello '
        path << 'world!'

        then:
        path.text == 'Hello world!'
    }

    def testEachFile() {
        setup:
        def folder = tempDir.toPath()
        def file1 = Files.createTempFile(folder, 'file_1_', null)
        def file2 = Files.createTempFile(folder, 'file_2_', null)
        def file3 = Files.createTempFile(folder, 'file_X_', null)
        def sub1 = Files.createTempDirectory(folder, 'sub1_')
        def file4 = Files.createTempFile(sub1, 'file_4_', null)
        def file5 = Files.createTempFile(sub1, 'file_5_', null)
        def sub2 = Files.createTempDirectory(sub1, 'sub2_')
        def file6 = Files.createTempFile(sub2, 'file_6_', null)

        when:
        def result1 = []
        folder.eachFile { result1 << it }

        then:
        result1.sort() == [file1, file2, file3, sub1].sort()

        when:
        def result2 = []
        folder.eachFile(FileType.FILES) { result2 << it }

        then:
        result2.sort() == [file1, file2, file3].sort()

        when:
        def result3 = []
        folder.eachFile(FileType.DIRECTORIES) { result3 << it }

        then:
        result3 == [sub1]

        when:
        def result4 = []
        folder.eachFileMatch(FileType.FILES, ~/file_\d_.*/) { result4 << it }

        then:
        result4.sort() == [file1, file2].sort()

        when:
        def result5 = []
        folder.eachFileMatch(FileType.DIRECTORIES, ~/sub\d_.*/) { result5 << it }

        then:
        result5 == [sub1]

        when:
        def result6 = []
        folder.eachFileRecurse(FileType.FILES) { result6 << it }

        then:
        result6.sort() == [file1, file2, file3, file4, file5, file6].sort()

        when:
        def result7 = []
        folder.eachFileRecurse(FileType.DIRECTORIES) { result7 << it }

        then:
        result7.sort() == [sub1, sub2].sort()
    }

    def testEachDir() {
        setup:
        def folder = tempDir.toPath()
        def sub1 = Files.createTempDirectory(folder, 'sub_1_')
        def sub2 = Files.createTempDirectory(folder, 'sub_2_')
        def sub3 = Files.createTempDirectory(folder, 'sub_X_')
        def sub4 = Files.createTempDirectory(sub2, 'sub_2_4_')
        def sub5 = Files.createTempDirectory(sub2, 'sub_2_5_')
        Files.createTempFile(folder, 'file1', null)
        Files.createTempFile(folder, 'file2', null)

        when:
        def result1 = []
        folder.eachDir { result1 << it }

        then:
        result1.sort() == [sub1, sub2, sub3].sort()

        when:
        def result2 = []
        folder.eachDirMatch(~/sub_\d_.*+/) { result2 << it }

        then:
        result2.sort() == [sub1, sub2].sort()

        when:
        def result3 = []
        folder.eachDirRecurse { result3 << it }

        then:
        result3.sort() == [sub1, sub2, sub3, sub4, sub5].sort()
    }

    def testAppendUTF16LE() {
        setup:
        def path = tempFile.toPath()
        Files.delete(path)
        def file = File.createTempFile('temp2', '.ext')
        file.delete()
        file.deleteOnExit()
        def str = 'Hello world!'

        // save using a File, thus uses ResourcesGroovyMethods
        when:
        file.append(str, 'UTF-16LE')

        then:
        file.getText('UTF-16LE') == str

        // now test append method using the Path
        when:
        path.append(str, 'UTF-16LE')

        then:
        path.getText('UTF-16LE') == str
        file.toPath().getText('UTF-16LE') == str

        // append the content of a reader, thus using the 'appendBuffered' version
        when:
        path.append(new StringReader(' - Hola Mundo!'), 'UTF-16LE')

        then:
        path.getText('UTF-16LE') == 'Hello world! - Hola Mundo!'
    }

    def testWriteUTF16LE() {
        setup:
        def path = tempFile.toPath()
        def str = 'Hello world!'

        when:
        path.write(str, 'UTF-16LE')

        then:
        path.getText('UTF-16LE') == str
    }

    def testWriteUTF16LEWithBom() {
        setup:
        def path = tempFile.toPath()
        def str = 'Hello world!'

        when:
        path.write(str, 'UTF-16LE', true)

        then:
        assert NioExtensions.getBytes(path) == [-1, -2, 72, 0, 101, 0, 108, 0, 108, 0, 111, 0, 32, 0, 119, 0, 111, 0, 114, 0, 108, 0, 100, 0, 33, 0] as byte[]
    }

    def testWriteUTF16BE() {
        setup:
        def path = tempFile.toPath()
        def str = 'Hello world!'

        when:
        path.write(str, 'UTF-16BE')

        then:
        path.getText('UTF-16BE') == str
    }

    def testWriteUTF16BEWithBom() {
        setup:
        def path = tempFile.toPath()
        def str = 'Hello world!'

        when:
        path.write(str, 'UTF-16BE', true)

        then:
        assert NioExtensions.getBytes(path) == [-2, -1, 0, 72, 0, 101, 0, 108, 0, 108, 0, 111, 0, 32, 0, 119, 0, 111, 0, 114, 0, 108, 0, 100, 0, 33] as byte[]
    }

    def testAsBoolean() {
        setup:
        def path = tempFile.toPath()

        when:
        path.write('Some text')

        then:
        assert path

        when:
        Files.delete(path)

        then:
        assert !path
    }

    def testExists() {
        setup:
        def path = tempFile.toPath()

        when:
        path.write('Some text')

        then:
        NioExtensions.exists(path)

        when:
        Files.delete(path)

        then:
        !NioExtensions.exists(path)
    }

    def testTraverse() {
        setup:
        def folder = tempDir.toPath()
        def sub1 = Files.createTempDirectory(folder, 'sub1_')
        def file1 = Files.createTempFile(folder, 'file1_', '.txt')
        def file2 = Files.createTempFile(sub1, 'file2_', '.txt')
        def sub2 = Files.createTempDirectory(sub1, 'sub2_')
        def file3 = Files.createTempFile(sub2, 'file3_', '.txt')

        when:
        def visited = []
        folder.traverse { visited << it }

        then:
        visited.containsAll([file1, sub1, file2, sub2, file3])
    }

    def testTraverseWithOptions() {
        setup:
        def folder = tempDir.toPath()
        def sub1 = Files.createTempDirectory(folder, 'sub1_')
        def file1 = Files.createTempFile(folder, 'file1_', '.txt')
        def file2 = Files.createTempFile(sub1, 'file2_', '.txt')
        Files.createTempFile(sub1, 'other_', '.dat')

        when:
        def visited = []
        folder.traverse(type: FileType.FILES, nameFilter: ~/.*\.txt/) { visited << it }

        then:
        visited.sort() == [file1, file2].sort()
    }

    def testTraverseWithMaxDepth() {
        setup:
        def folder = tempDir.toPath()
        def sub1 = Files.createTempDirectory(folder, 'sub1_')
        def file1 = Files.createTempFile(folder, 'file1_', '.txt')
        def sub2 = Files.createTempDirectory(sub1, 'sub2_')
        def file2 = Files.createTempFile(sub2, 'file2_', '.txt')

        when:
        def visited = []
        folder.traverse(maxDepth: 0) { visited << it }

        then:
        visited.containsAll([file1, sub1])
        !visited.contains(file2)
    }

    def testFilterLine() {
        setup:
        tempFile.text = 'alpha\nbeta\ngamma\ndelta'
        def path = tempFile.toPath()

        when:
        def writable = path.filterLine { it.startsWith('a') || it.startsWith('g') || it.startsWith('d') }
        def sw = new StringWriter()
        writable.writeTo(sw)

        then:
        sw.toString() == ['alpha','gamma','delta',''].join(System.lineSeparator())
    }

    def testFilterLineWithCharset() {
        setup:
        def path = tempFile.toPath()
        path.write('alpha\nbeta\ngamma', 'UTF-8')

        when:
        def writable = path.filterLine('UTF-8') { it.startsWith('a') || it.startsWith('g') }
        def sw = new StringWriter()
        writable.writeTo(sw)

        then:
        sw.toString() == ['alpha','gamma',''].join(System.lineSeparator())
    }

    def testFilterLineToWriter() {
        setup:
        tempFile.text = 'line1\nline2\nline3\nline4'
        def path = tempFile.toPath()
        def sw = new StringWriter()

        when:
        path.filterLine(sw) { it.contains('2') || it.contains('4') }

        then:
        sw.toString() == ['line2','line4',''].join(System.lineSeparator())
    }

    def testCreateParentDirectories() {
        setup:
        def nestedPath = tempDir.toPath().resolve('a/b/c/file.txt')

        expect:
        !Files.exists(nestedPath.parent)

        when:
        nestedPath.createParentDirectories()

        then:
        Files.exists(nestedPath.parent)
        Files.isDirectory(nestedPath.parent)
    }

    def testDeleteDir() {
        setup:
        def folder = Files.createTempDirectory(tempDir.toPath(), 'toDelete_')
        def sub = Files.createTempDirectory(folder, 'sub_')
        Files.createTempFile(folder, 'file1_', '.txt')
        Files.createTempFile(sub, 'file2_', '.txt')

        expect:
        Files.exists(folder)

        when:
        def result = folder.deleteDir()

        then:
        result
        !Files.exists(folder)
    }

    def testWithWriter() {
        setup:
        def path = tempFile.toPath()

        when:
        def result = path.withWriter { writer ->
            writer.write('Hello from withWriter')
            'return value'
        }

        then:
        path.text == 'Hello from withWriter'
        result == 'return value'
    }

    def testWithWriterAndCharset() {
        setup:
        def path = tempFile.toPath()

        when:
        path.withWriter('UTF-8') { writer ->
            writer.write('Hello UTF-8')
        }

        then:
        path.getText('UTF-8') == 'Hello UTF-8'
    }

    def testWithWriterAppend() {
        setup:
        def path = tempFile.toPath()
        path.write('Hello')

        when:
        path.withWriterAppend { writer ->
            writer.write(' World')
        }

        then:
        path.text == 'Hello World'
    }

    def testWithWriterAppendAndCharset() {
        setup:
        def path = tempFile.toPath()
        path.write('Hello', 'UTF-8')

        when:
        path.withWriterAppend('UTF-8') { writer ->
            writer.write(' World')
        }

        then:
        path.getText('UTF-8') == 'Hello World'
    }

    def testWithPrintWriter() {
        setup:
        def path = tempFile.toPath()

        when:
        def result = path.withPrintWriter { pw ->
            pw.println('Line 1')
            pw.println('Line 2')
            42
        }

        then:
        path.readLines() == ['Line 1', 'Line 2']
        result == 42
    }

    def testWithPrintWriterAndCharset() {
        setup:
        def path = tempFile.toPath()

        when:
        path.withPrintWriter('UTF-8') { pw ->
            pw.println('UTF-8 Line')
        }

        then:
        path.getText('UTF-8').trim() == 'UTF-8 Line'
    }

    def testNewPrintWriter() {
        setup:
        def path = tempFile.toPath()

        when:
        def pw = path.newPrintWriter()
        pw.println('Test line')
        pw.close()

        then:
        path.readLines() == ['Test line']
    }

    def testNewPrintWriterWithCharset() {
        setup:
        def path = tempFile.toPath()

        when:
        def pw = path.newPrintWriter('UTF-8')
        pw.println('UTF-8 test')
        pw.close()

        then:
        path.getText('UTF-8').trim() == 'UTF-8 test'
    }

    def testEachByte() {
        setup:
        def path = tempFile.toPath()
        path.setBytes([65, 66, 67] as byte[])

        when:
        def bytes = []
        path.eachByte { bytes << it }

        then:
        bytes == [65, 66, 67]
    }

    def testEachByteWithBufferLen() {
        setup:
        def path = tempFile.toPath()
        path.setBytes([1, 2, 3, 4, 5, 6, 7, 8, 9, 10] as byte[])

        when:
        def chunks = []
        path.eachByte(4) { buffer, len ->
            def bytes = new byte[len]
            System.arraycopy(buffer, 0, bytes, 0, len)
            chunks << [bytes: bytes as List, len: len]
        }

        then:
        chunks.size() == 3
        chunks[0].len == 4
        chunks[1].len == 4
        chunks[2].len == 2
    }

    def testNewDataInputStream() {
        setup:
        def path = tempFile.toPath()
        def dos = new DataOutputStream(new FileOutputStream(tempFile))
        dos.writeInt(42)
        dos.writeUTF('Hello')
        dos.close()

        when:
        def dis = path.newDataInputStream()

        then:
        dis.readInt() == 42
        dis.readUTF() == 'Hello'

        cleanup:
        dis?.close()
    }

    def testNewDataOutputStream() {
        setup:
        def path = tempFile.toPath()

        when:
        def dos = path.newDataOutputStream()
        dos.writeInt(123)
        dos.writeUTF('Test')
        dos.close()
        def dis = new DataInputStream(new FileInputStream(tempFile))

        then:
        dis.readInt() == 123
        dis.readUTF() == 'Test'

        cleanup:
        dis?.close()
    }

    def testNewInputStream() {
        setup:
        def path = tempFile.toPath()
        path.write('Test content')

        when:
        def is = path.newInputStream()
        def content = is.text

        then:
        content == 'Test content'

        cleanup:
        is?.close()
    }

    def testNewOutputStream() {
        setup:
        def path = tempFile.toPath()

        when:
        def os = path.newOutputStream()
        os.write('Output stream test'.bytes)
        os.close()

        then:
        path.text == 'Output stream test'
    }

    def testNewWriter() {
        setup:
        def path = tempFile.toPath()

        when:
        def writer = path.newWriter()
        writer.write('Writer test')
        writer.close()

        then:
        path.text == 'Writer test'
    }

    def testNewWriterWithCharset() {
        setup:
        def path = tempFile.toPath()

        when:
        def writer = path.newWriter('UTF-8')
        writer.write('UTF-8 writer')
        writer.close()

        then:
        path.getText('UTF-8') == 'UTF-8 writer'
    }

    def testNewReader() {
        setup:
        def path = tempFile.toPath()
        path.write('Reader test content')

        when:
        def reader = path.newReader()
        def content = reader.text

        then:
        content == 'Reader test content'

        cleanup:
        reader?.close()
    }

    def testNewReaderWithCharset() {
        setup:
        def path = tempFile.toPath()
        path.write('UTF-8 content', 'UTF-8')

        when:
        def reader = path.newReader('UTF-8')
        def content = reader.text

        then:
        content == 'UTF-8 content'

        cleanup:
        reader?.close()
    }

    def testReadBytes() {
        setup:
        def path = tempFile.toPath()
        def expectedBytes = [72, 101, 108, 108, 111] as byte[]
        path.setBytes(expectedBytes)

        when:
        def readBytes = path.readBytes()

        then:
        readBytes == expectedBytes
    }

    def testEachLine() {
        setup:
        tempFile.text = 'line1\nline2\nline3'
        def path = tempFile.toPath()

        when:
        def lines = []
        path.eachLine { lines << it }

        then:
        lines == ['line1', 'line2', 'line3']
    }

    def testEachLineWithCharset() {
        setup:
        def path = tempFile.toPath()
        path.write('utf8-line1\nutf8-line2', 'UTF-8')

        when:
        def lines = []
        path.eachLine('UTF-8') { lines << it }

        then:
        lines == ['utf8-line1', 'utf8-line2']
    }

    def testEachLineWithLineNumber() {
        setup:
        tempFile.text = 'first\nsecond\nthird'
        def path = tempFile.toPath()

        when:
        def result = []
        path.eachLine { line, num -> result << [line: line, num: num] }

        then:
        result.size() == 3
        result[0] == [line: 'first', num: 1]
        result[1] == [line: 'second', num: 2]
        result[2] == [line: 'third', num: 3]
    }

    def testSplitEachLine() {
        setup:
        tempFile.text = 'a,b,c\n1,2,3\nx,y,z'
        def path = tempFile.toPath()

        when:
        def rows = []
        path.splitEachLine(',') { rows << it }

        then:
        rows == [['a', 'b', 'c'], ['1', '2', '3'], ['x', 'y', 'z']]
    }

    def testSplitEachLineWithPattern() {
        setup:
        tempFile.text = 'a:b:c\n1:2:3'
        def path = tempFile.toPath()

        when:
        def rows = []
        path.splitEachLine(~/\:/) { rows << it }

        then:
        rows == [['a', 'b', 'c'], ['1', '2', '3']]
    }

    def testWithObjectInputStream() {
        setup:
        def path = tempFile.toPath()
        def oos = new ObjectOutputStream(new FileOutputStream(tempFile))
        oos.writeObject('test object')
        oos.close()

        when:
        def result = path.withObjectInputStream { ois ->
            ois.readObject()
        }

        then:
        result == 'test object'
    }

    def testWithObjectInputStreamAndClassLoader() {
        setup:
        def path = tempFile.toPath()
        def oos = new ObjectOutputStream(new FileOutputStream(tempFile))
        oos.writeObject(['a', 'b', 'c'])
        oos.close()

        when:
        def result = path.withObjectInputStream(getClass().classLoader) { ois ->
            ois.readObject()
        }

        then:
        result == ['a', 'b', 'c']
    }

    def testWithInputStream() {
        setup:
        def path = tempFile.toPath()
        path.write('input stream content')

        when:
        def result = path.withInputStream { is ->
            is.text
        }

        then:
        result == 'input stream content'
    }

    def testWithDataInputStream() {
        setup:
        def path = tempFile.toPath()
        def dos = new DataOutputStream(new FileOutputStream(tempFile))
        dos.writeInt(999)
        dos.writeBoolean(true)
        dos.close()

        when:
        def results = []
        path.withDataInputStream { dis ->
            results << dis.readInt()
            results << dis.readBoolean()
        }

        then:
        results == [999, true]
    }

    def testWithDataOutputStream() {
        setup:
        def path = tempFile.toPath()

        when:
        path.withDataOutputStream { dos ->
            dos.writeDouble(3.14)
            dos.writeLong(123456789L)
        }
        def dis = new DataInputStream(new FileInputStream(tempFile))

        then:
        dis.readDouble() == 3.14
        dis.readLong() == 123456789L

        cleanup:
        dis?.close()
    }

    def testWithOutputStream() {
        setup:
        def path = tempFile.toPath()

        when:
        def result = path.withOutputStream { os ->
            os.write('output stream'.bytes)
            'done'
        }

        then:
        path.text == 'output stream'
        result == 'done'
    }

    def testWithReader() {
        setup:
        def path = tempFile.toPath()
        path.write('reader content')

        when:
        def result = path.withReader { reader ->
            reader.readLine()
        }

        then:
        result == 'reader content'
    }

    def testWithReaderAndCharset() {
        setup:
        def path = tempFile.toPath()
        path.write('UTF-8 reader', 'UTF-8')

        when:
        def result = path.withReader('UTF-8') { reader ->
            reader.text
        }

        then:
        result == 'UTF-8 reader'
    }

    def testAsWritable() {
        setup:
        tempFile.text = 'writable content'
        def path = tempFile.toPath()

        when:
        def writable = path.asWritable()

        then:
        writable != null
        writable instanceof org.apache.groovy.nio.runtime.WritablePath
    }

    def testAsWritableWithCharset() {
        setup:
        tempFile.text = 'UTF-8 writable'
        def path = tempFile.toPath()

        when:
        def writable = path.asWritable('UTF-8')

        then:
        writable != null
        writable instanceof org.apache.groovy.nio.runtime.WritablePath
    }

    def testReadLinesWithCharset() {
        setup:
        def path = tempFile.toPath()
        path.write('line1\nline2\nline3', 'UTF-8')

        when:
        def lines = path.readLines('UTF-8')

        then:
        lines == ['line1', 'line2', 'line3']
    }

    def testGetTextWithCharset() {
        setup:
        def path = tempFile.toPath()
        path.write('charset text', 'UTF-8')

        when:
        def text = path.getText('UTF-8')

        then:
        text == 'charset text'
    }

    def testRenameTo() {
        setup:
        def path = tempFile.toPath()
        path.write('content to rename')
        def destFile = new File(tempDir, 'renamed_file.txt')

        when:
        def result = path.renameTo(destFile.absolutePath)

        then:
        result
        destFile.exists()
        !Files.exists(path)
        destFile.text == 'content to rename'
    }

    def testRenameToWithString() {
        setup:
        def path = tempFile.toPath()
        path.write('rename by string')
        def destFile = new File(tempDir, 'renamed_string.txt')

        when:
        def result = path.renameTo(destFile.absolutePath)

        then:
        result
        destFile.exists()
        !Files.exists(path)
    }

    def testRenameToWithURI() {
        setup:
        def path = tempFile.toPath()
        path.write('rename by uri')
        def destPath = tempDir.toPath().resolve('renamed_uri.txt')

        when:
        def result = path.renameTo(destPath.toUri())

        then:
        result
        Files.exists(destPath)
        !Files.exists(path)
    }
}
