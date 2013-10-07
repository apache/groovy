/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
package org.codehaus.groovy.runtime
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

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

}
