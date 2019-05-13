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

import java.nio.file.Files
import java.nio.file.StandardCopyOption

import groovy.io.FileType
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class NioExtensionsTest extends Specification {

    @Rule
    TemporaryFolder temporaryFolder

    def testShouldAppendByteArrayToFile() {
        when:
        def path = temporaryFolder.newFile().toPath()
        path.write('Hello')
        byte[] bytes = ' World'.bytes
        path.append(bytes)

        then:
        path.text == 'Hello World'
    }

    def testShouldAppendStringToFileUsingDefaultEncoding() {
        when:
        def path = temporaryFolder.newFile().toPath()
        path.write('Hello')
        path.append(' World')

        then:
        path.text == 'Hello World'
    }

    def testShouldAppendTextSuppliedByReaderToFileUsingDefaultEncoding() {
        when:
        def path = temporaryFolder.newFile().toPath()
        path.write('Hello')
        Reader reader = new StringReader(' World')
        path.append(reader)

        then:
        path.text == 'Hello World'
    }

    def testShouldAppendTextSuppliedByWriterToFileUsingDefaultEncoding() {
        when:
        def path = temporaryFolder.newFile().toPath()
        path.write('Hello')
        Writer writer = new StringWriter()
        writer.append(' World')
        path.append(writer)

        then:
        path.text == 'Hello World'
    }

    def testShouldAppendStringToFileUsingSpecifiedEncoding() {
        when:
        def path = temporaryFolder.newFile().toPath()
        String encoding = 'UTF-8'
        path.write('؁', encoding)
        path.append(' ؁', encoding)

        then:
        path.getText(encoding) == '؁ ؁'
    }

    def testShouldAppendTextSuppliedByReaderToFileUsingSpecifiedEncoding() {
        when:
        def path = temporaryFolder.newFile().toPath()
        String encoding = 'UTF-8'
        path.write('؁', encoding)
        Reader reader = new CharArrayReader([' ','؁'] as char[])
        path.append(reader, encoding)

        then:
        path.getText(encoding) == '؁ ؁'
    }

    def testShouldAppendTextSuppliedByWriterToFileUsingSpecifiedEncoding() {
        when:
        def path = temporaryFolder.newFile().toPath()
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
        def path = temporaryFolder.newFile().toPath()
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
        def path = temporaryFolder.newFile().toPath()
        Files.delete(path)
        String encoding = 'UTF-16LE'
        path.write('؁', encoding, true)
        Reader reader = new CharArrayReader([' ','؁'] as char[])
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
        def path = temporaryFolder.newFile().toPath()
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
        def path = temporaryFolder.newFile().toPath()
        Files.copy(new ByteArrayInputStream(str.getBytes()), path, StandardCopyOption.REPLACE_EXISTING)

        then:
        path.size() == str.size()
    }

    def testNewObjectOutputStream() {
        setup:
        def str = 'Hello world!'
        def path = temporaryFolder.newFile().toPath()

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
        def path = temporaryFolder.newFile().toPath()
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
        def path = temporaryFolder.newFile().toPath()
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
        def file = temporaryFolder.newFile()
        file.text = 'alpha\nbeta\ndelta'

        when:
        def lines = file.toPath().readLines()

        then:
        lines == ['alpha', 'beta', 'delta']
    }

    def testNewReader() {
        setup:
        def str = 'Hello world!'
        def file = temporaryFolder.newFile()
        file.text = str

        when:
        def reader = file.toPath().newReader()
        def line = reader.readLine()

        then:
        line == str
        reader.readLine() == null
    }

    def testGetBytes() {
        when:
        def file = temporaryFolder.newFile()
        file.text = 'Hello world!'

        then:
        file.toPath().getBytes() == 'Hello world!'.getBytes()
    }

    def testSetBytes() {
        when:
        def file = temporaryFolder.newFile()
        file.text = 'Hello world!'
        file.toPath().setBytes('Ciao mundo!'.getBytes())

        then:
        file.text == 'Ciao mundo!'
    }

    def testSetText() {
        when:
        def file = temporaryFolder.newFile()
        file.toPath().setText('Ciao mundo!')
        // invoke twice to make sure that the content is truncated by the setText method
        file.toPath().setText('Hello world!')

        then:
        file.text == 'Hello world!'
    }

    def testWrite()  {
        when:
        def str = 'Hello world!'
        def file = temporaryFolder.newFile()
        file.toPath().write(str)

        then:
        file.text == 'Hello world!'
    }

    def testWriteWithEncoding()  {
        when:
        def str = 'Hello world!'
        def file = temporaryFolder.newFile()
        file.toPath().write('Ciao mundo!')
        file.toPath().write(str, 'UTF-8')

        then:
        file.text == str
    }

    def testWriteWithEncodingAndBom() {
        when:
        def str = '؁'
        def file = temporaryFolder.newFile()
        file.toPath().write(str, 'UTF-16LE', true)

        then:
        assert file.getBytes() == [-1, -2, 1, 6] as byte[]
    }

    def testAppendObject() {
        setup:
        def file = temporaryFolder.newFile()
        file.text = 'alpha'

        when:
        file.toPath().append('-gamma')

        then:
        file.text == 'alpha-gamma'
    }

    def testAppendBytes() {
        setup:
        def file = temporaryFolder.newFile()
        file.text = 'alpha'

        when:
        file.toPath().append('-beta'.getBytes())

        then:
        file.text == 'alpha-beta'
    }

    def testAppendInputStream() {
        setup:
        def file = temporaryFolder.newFile()
        file.text = 'alpha'

        when:
        file.toPath().append(new ByteArrayInputStream('-delta'.getBytes()))

        then:
        file.text == 'alpha-delta'
    }

    def testLeftShift() {
        setup:
        def path = temporaryFolder.newFile().toPath()

        when:
        path << 'Hello '
        path << 'world!'

        then:
        path.text == 'Hello world!'
    }

    def testEachFile() {
        setup:
        def folder = temporaryFolder.newFolder('test').toPath()
        def file1 = Files.createTempFile(folder, 'file_1_', null)
        def file2 = Files.createTempFile(folder, 'file_2_', null)
        def file3 = Files.createTempFile(folder, 'file_X_', null)
        def sub1 = Files.createTempDirectory(folder, 'sub1_')
        def file4  = Files.createTempFile(sub1, 'file_4_', null)
        def file5  = Files.createTempFile(sub1, 'file_5_', null)
        def sub2 = Files.createTempDirectory(sub1, 'sub2_')
        def file6  = Files.createTempFile(sub2, 'file_6_', null)

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
        def folder = temporaryFolder.newFolder('test').toPath()
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
        def path = temporaryFolder.newFile().toPath()
        Files.delete(path)
        def file = temporaryFolder.newFile()
        file.delete()
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
        def path = temporaryFolder.newFile().toPath()
        def str = 'Hello world!'

        when:
        path.write(str, 'UTF-16LE')

        then:
        path.getText('UTF-16LE') == str
    }

    def testWriteUTF16LEWithBom() {
        setup:
        def path = temporaryFolder.newFile().toPath()
        def str = 'Hello world!'

        when:
        path.write(str, 'UTF-16LE', true)

        then:
        assert NioExtensions.getBytes(path) == [-1, -2, 72, 0, 101, 0, 108, 0, 108, 0, 111, 0, 32, 0, 119, 0, 111, 0, 114, 0, 108, 0, 100, 0, 33, 0] as byte[]
    }

    def testWriteUTF16BE() {
        setup:
        def path = temporaryFolder.newFile().toPath()
        def str = 'Hello world!'

        when:
        path.write(str, 'UTF-16BE')

        then:
        path.getText('UTF-16BE') == str
    }

    def testWriteUTF16BEWithBom() {
        setup:
        def path = temporaryFolder.newFile().toPath()
        def str = 'Hello world!'

        when:
        path.write(str, 'UTF-16BE', true)

        then:
        assert NioExtensions.getBytes(path) == [-2, -1, 0, 72, 0, 101, 0, 108, 0, 108, 0, 111, 0, 32, 0, 119, 0, 111, 0, 114, 0, 108, 0, 100, 0, 33] as byte[]
    }

}
