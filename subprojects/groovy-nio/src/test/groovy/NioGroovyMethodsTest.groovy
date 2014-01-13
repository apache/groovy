/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
package org.codehaus.groovy.runtime
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

import groovy.io.FileType
import spock.lang.Specification

class NioGroovyMethodsTest extends Specification {

    def testPathSize() {

        when:
        def str = 'Hello world!'
        Path path = Files.createTempFile('test-size',null)
        Files.copy( new ByteArrayInputStream(str.getBytes()), path, StandardCopyOption.REPLACE_EXISTING )
        then:
        path.size() == str.size()
        cleanup:
        Files.deleteIfExists(path)
    }


    def testNewObjectOutputStream() {

        setup:
        def str = 'Hello world!'
        Path path = Paths.get('new_obj_out_stream')
        when:
        def out = path.newObjectOutputStream()
        out.writeObject(str)
        out.flush()
        def stream = new ObjectInputStream(new FileInputStream(path.toFile()))

        then:
        stream.readObject() == str

        cleanup:
        Files.deleteIfExists(path)

    }

    def testNewObjectInputStream() {

        setup:
        def str = 'Hello world!'
        def path = Paths.get('new_obj_in_stream')
        def stream = new ObjectOutputStream(new FileOutputStream(path.toFile()))
        stream.writeObject(str)
        stream.close()

        when:
        def obj = path.newObjectInputStream()
        then:
        obj.readObject() == str
        cleanup:
        Files.deleteIfExists(path)

    }

    def testEachObject() {

        setup:
        def str1 = 'alpha'
        def str2 = 'beta'
        def str3 = 'delta'
        def path = Paths.get('new_obj_in_stream')
        def stream = new ObjectOutputStream(new FileOutputStream(path.toFile()))
        stream.writeObject(str1)
        stream.writeObject(str2)
        stream.writeObject(str3)
        stream.close()

        when:
        def list = []
        path.eachObject() { list << it }

        then:
        list.size()==3
        list[0]==str1
        list[1]==str2
        list[2]==str3

        cleanup:
        Files.deleteIfExists(path)
    }

    def testEachLine() {

        setup:
        def file = new File('test_each_file')
        file.text = 'alpha\nbeta\ndelta';
        when:
        def lines = file.toPath().readLines()
        then:
        lines.size()==3
        lines[0]=='alpha'
        lines[1]=='beta'
        lines[2]=='delta'

        cleanup:
        file?.delete()
    }

    def testNewReader() {

        setup:
        final str = 'Hello world!'
        def file = new File('test_new_reader')
        file.text = str
        when:
        def reader = file.toPath().newReader()
        def line = reader.readLine()
        then:
        line == str
        reader.readLine() == null

        cleanup:
        file?.delete()

    }

    def testGetBytes() {

        when:
        def file = new File('test_getBytes')
        file.text = 'Hello world!'
        def path = file.toPath()
        then:
        path.getBytes() == 'Hello world!'.getBytes()

        cleanup:
        file?.delete()

    }

    def testSetBytes() {


        when:
        def file = new File('test_setBytes')
        file.toPath().setBytes( 'Ciao mundo!'.getBytes() )
        then:
        file.text == 'Ciao mundo!'
        cleanup:
        file?.delete()

    }

    def testWrite( )  {
        when:
        String str = 'Hello there!'
        def file = new File('test_write');
        file.toPath().write(str)
        then:
        file.text == 'Hello there!'
        cleanup:
        file?.delete()
    }


    def testAppendObject() {

        setup:
        def file = new File('test_appendObject')
        file.text = 'alpha'
        when:
        file.toPath().append( '-gamma' )
        then:
        file.text == 'alpha-gamma'
        cleanup:
        file.delete()
    }

    def testAppendBytes() {
        setup:
        def file = new File('test_appendBytes')
        file.text = 'alpha'
        when:
        file.toPath().append( '-beta'.getBytes() )
        then:
        file.text == 'alpha-beta'
        cleanup:
        file.delete()
    }


    def testAppendInputStream() {

        setup:
        def file = new File('test_appendStream')
        file.text = 'alpha'
        when:
        file.toPath().append( new ByteArrayInputStream('-delta'.getBytes()) )
        then:
        file.text == 'alpha-delta'
        cleanup:
        file.delete()

    }

    def testEachFile() {
        setup:
        def folder = Files.createTempDirectory('test')
        def file1 = Files.createTempFile(folder, 'file_1_', null)
        def file2 = Files.createTempFile(folder, 'file_2_', null)
        def file3 = Files.createTempFile(folder, 'file_X_', null)
        // sub-folder with two files
        def sub1 = Files.createTempDirectory(folder, 'sub1_')
        def file4  = Files.createTempFile(sub1, 'file_4_', null)
        def file5  = Files.createTempFile(sub1, 'file_5_', null)
        // sub-sub-folder with one file
        def sub2 = Files.createTempDirectory(sub1, 'sub2_')
        def file6  = Files.createTempFile(sub2, 'file_6_', null)

        when:
        def result = []
        folder.eachFile() { result << it }
        then:
        result == [file1, file2, file3, sub1]

        when:
        def result2 = []
        folder.eachFile(FileType.FILES) { result2 << it }
        then:
        result2 == [file1, file2, file3]

        when:
        def result3 = []
        folder.eachFile(FileType.DIRECTORIES) { result3 << it }
        then:
        result3 == [sub1]

        when:
        def result4 = []
        folder.eachFileMatch(FileType.FILES, ~/file_\d_.*/) { result4 << it }
        then:
        result4 == [file1, file2]

        when:
        def result5 = []
        folder.eachFileMatch(FileType.DIRECTORIES, ~/sub\d_.*/) { result5 << it }
        then:
        result5 == [sub1]

        when:
        def result6 = []
        folder.eachFileRecurse(FileType.FILES) { result6 << it }
        then:
        result6 == [file1, file2, file3, file4, file5, file6]

        when:
        def result7 = []
        folder.eachFileRecurse(FileType.DIRECTORIES) { result7 << it }
        then:
        result7 == [sub1, sub2]

        cleanup:
        folder?.toFile()?.deleteDir()
    }

    
    def testEachDir() {

        setup:
        def folder = Files.createTempDirectory('test')
        def sub1 = Files.createTempDirectory(folder, 'sub_1_')
        def sub2 = Files.createTempDirectory(folder, 'sub_2_')
        def sub3 = Files.createTempDirectory(folder, 'sub_X_')
        def sub4 = Files.createTempDirectory(sub2, 'sub_2_4_')
        def sub5 = Files.createTempDirectory(sub2, 'sub_2_5_')
        def file1 = Files.createTempFile(folder, 'file1', null)
        def file2 = Files.createTempFile(folder, 'file2', null)

        // test *eachDir*
        when:
        def result = []
        folder.eachDir { result << it }
        then:
        result.sort() == [ sub1, sub2, sub3 ]

        // test *eachMatchDir*
        when:
        def result2 = []
        folder.eachDirMatch( ~/sub_\d_.*+/ ) { result2 << it }
        then:
        result2.sort() == [ sub1, sub2 ]

        when:
        def result3 = []
        folder.eachDirRecurse { result3 << it }
        then:
        result3 == [ sub1, sub2, sub4, sub5, sub3 ]


        cleanup:
        folder?.deleteDir()

    }

}
