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
 * Unit tests for static type checking : closure parameter type inference for {@link org.codehaus.groovy.runtime.IOGroovyMethods}.
 */
class IOGMClosureParamTypeInferenceSTCTest extends StaticTypeCheckingTestCase {

    void testEachByteOnInputStream() {
        assertScript '''
            byte[] array = 'Groovy'.getBytes('utf-8')
            new ByteArrayInputStream(array).eachByte { b -> assert b > ((byte) 70) && b < ((byte)122) }
        '''
    }

    void testEachByteOnInputStreamWithBufferLen() {
        assertScript '''
            byte[] array = 'Groovy'.getBytes('utf-8')
            new ByteArrayInputStream(array).eachByte(4) { buf,len -> assert buf.length==4 }
        '''
    }

    void testEachLineOnInputStream() {
        assertScript '''
            int wc(InputStream is) {
                int sum = 0
                is.eachLine { line -> sum += line.length() }
            }
            String text = """foo
bar
baz"""
            assert wc(new ByteArrayInputStream(text.bytes)) > 0
        '''
        assertScript '''
            int wc(InputStream is) {
                int sum = 0
                is.eachLine { line, lineNumber -> sum += line.length() }
            }
            String text = """foo
bar
baz"""
            assert wc(new ByteArrayInputStream(text.bytes)) > 0
        '''
        assertScript '''
            int wc(InputStream is) {
                int sum = 0
                is.eachLine(45) { line, lineNumber -> sum += line.length() }
            }
            String text = """foo
bar
baz"""
            assert wc(new ByteArrayInputStream(text.bytes)) > 0
        '''
        assertScript '''
            int wc(InputStream is) {
                int sum = 0
                is.eachLine('utf-8') { line, lineNumber -> sum += line.length() }
            }
            String text = """foo
bar
baz"""
            assert wc(new ByteArrayInputStream(text.bytes)) > 0
        '''
        assertScript '''
            int wc(InputStream is) {
                int sum = 0
                is.eachLine('utf-8',45) { line, lineNumber -> sum += line.length() }
            }
            String text = """foo
bar
baz"""
            assert wc(new ByteArrayInputStream(text.bytes)) > 0
        '''
    }

    void testEachLineOnReader() {
        assertScript '''
            int wc(Reader is) {
                int sum = 0
                is.eachLine { line -> sum += line.length() }
            }
            String text = """foo
bar
baz"""
            assert wc(new ByteArrayInputStream(text.bytes).newReader()) > 0
        '''
        assertScript '''
            int wc(Reader is) {
                int sum = 0
                is.eachLine { line, lineNumber -> sum += line.length() }
            }
            String text = """foo
bar
baz"""
            assert wc(new ByteArrayInputStream(text.bytes).newReader()) > 0
        '''
        assertScript '''
            int wc(Reader is) {
                int sum = 0
                is.eachLine(45) { line, lineNumber -> sum += line.length() }
            }
            String text = """foo
bar
baz"""
            assert wc(new ByteArrayInputStream(text.bytes).newReader()) > 0
        '''
    }

    void testFilterLineOnInputStream() {
        assertScript '''
            def ls = String.format('%n')
            def is = new ByteArrayInputStream("foo\\nbar\\nbaz".bytes)
            String res = is.filterLine { line -> line.contains('ba') }
            assert res == "bar" + ls + "baz" + ls
        '''
        assertScript '''
            def ls = String.format('%n')
            def is = new ByteArrayInputStream("foo\\nbar\\nbaz".getBytes('utf-8'))
            String res = is.filterLine('utf-8') { line -> line.contains('ba') }
            assert res == "bar" + ls + "baz" + ls
        '''
        assertScript '''
            def ls = String.format('%n')
            def wrt = new StringWriter()
            def is = new ByteArrayInputStream("foo\\nbar\\nbaz".bytes)
            is.filterLine(wrt) { line -> line.contains('ba') }
            assert wrt.toString() == "bar" + ls + "baz" + ls
        '''
        assertScript '''
            def ls = String.format('%n')
            def wrt = new StringWriter()
            def is = new ByteArrayInputStream("foo\\nbar\\nbaz".getBytes('utf-8'))
            is.filterLine(wrt,'utf-8') { line -> line.contains('ba') }
            assert wrt.toString() == "bar" + ls + "baz" + ls
        '''
        assertScript '''
            def ls = String.format('%n')
            def is = new ByteArrayInputStream("foo\\nbar\\nbaz".bytes)
            String res = is.newReader().filterLine { line -> line.contains('ba') }
            assert res == "bar" + ls + "baz" + ls
        '''
        assertScript '''
            def ls = String.format('%n')
            def wrt = new StringWriter()
            def is = new ByteArrayInputStream("foo\\nbar\\nbaz".bytes)
            is.newReader().filterLine(wrt) { line -> line.contains('ba') }
            assert wrt.toString() == "bar" + ls + "baz" + ls
        '''
    }

    void testSplitEachLineOnInputStream() {
        assertScript '''def is = new ByteArrayInputStream("""a,b,c
d,e,f""".bytes)
            is.splitEachLine(',') { assert it.size() == 3 }
        '''
        assertScript '''def is = new ByteArrayInputStream("""a,b,c
d,e,f""".bytes)
            is.splitEachLine(~',') { assert it.size() == 3 }
        '''
        assertScript '''def is = new ByteArrayInputStream("""a,b,c
d,e,f""".bytes)
            is.splitEachLine(',', 'utf-8') { assert it.size() == 3 }
        '''
        assertScript '''def is = new ByteArrayInputStream("""a,b,c
d,e,f""".bytes)
            is.splitEachLine(~',', 'utf-8') { assert it.size() == 3 }
        '''
    }

    void testSplitEachLineOnReader() {
        assertScript '''def reader = new ByteArrayInputStream("""a,b,c
d,e,f""".bytes).newReader()
            reader.splitEachLine(',') { assert it.size() == 3 }
        '''
        assertScript '''def reader = new ByteArrayInputStream("""a,b,c
d,e,f""".bytes).newReader()
            reader.splitEachLine(',') { List it -> assert it.size() == 3 }
        '''
        assertScript '''def reader = new ByteArrayInputStream("""a,b,c
d,e,f""".bytes).newReader()
            reader.splitEachLine(',') { List<String> it -> assert it.size() == 3 }
        '''
        assertScript '''def reader = new ByteArrayInputStream("""a,b,c
d,e,f""".bytes).newReader()
            reader.splitEachLine(',') { a, b, c -> assert [a.size(), b.size(), c.size()] == [1, 1, 1] }
// TODO replace above with below once GROOVY-7442 is fixed
//            reader.splitEachLine(',') { a, b, c -> assert [a, b, c]*.size() == [1, 1, 1] }
        '''
        assertScript '''def reader = new ByteArrayInputStream("""a,b,c
d,e,f""".bytes).newReader()
            reader.splitEachLine(',') { String a, String b, String c -> assert [a.size(), b.size(), c.size()] == [1, 1, 1] }
// TODO replace above with below once GROOVY-7442 is fixed
//            reader.splitEachLine(',') { String a, String b, String c -> assert [a, b, c]*.size() == [1, 1, 1] }
        '''
        assertScript '''def reader = new ByteArrayInputStream("""a,b,c
d,e,f""".bytes).newReader()
            reader.splitEachLine(',') { String it -> assert it instanceof String && it.size() == 1 }
        '''
        assertScript '''def reader = new ByteArrayInputStream("""a,b,c
d,e,f""".bytes).newReader()
            reader.splitEachLine(~',') { assert it.size() == 3 }
        '''
    }

    void testTransformCharWithReader() {
        assertScript '''
            def reader = new ByteArrayInputStream("Groovy".bytes).newReader()
            def writer = new StringWriter()
            reader.transformChar(writer) { c ->
                assert c.length() == 1
                c.toUpperCase()
            }
            assert writer.toString() == 'GROOVY'
'''
    }

    void testTransformLineWithReader() {
        assertScript '''
            def ls = String.format('%n')
            def reader = new ByteArrayInputStream("Groovy".bytes).newReader()
            def writer = new StringWriter()
            reader.transformLine(writer) { line ->
                assert line.length() == 6
                line.toUpperCase()
            }
            assert writer.toString() == "GROOVY" + ls
'''
    }

    void testWithFormatter() {
        assertScript '''
            def sb = new StringBuilder()
            sb.withFormatter {
                it.format('%4$2s %3$2s %2$2s %1$2s', "a", "b", "c", "d")
            }
            assert sb.toString() == ' d  c  b  a'
        '''
        assertScript '''
            def sb = new StringBuilder()
            sb.withFormatter(Locale.ENGLISH) {
                it.format('%4$2s %3$2s %2$2s %1$2s', "a", "b", "c", "d")
            }
            assert sb.toString() == ' d  c  b  a'
        '''
    }

    void testWithObjectOutputInputStream() {
        assertScript '''
            def baos = new ByteArrayOutputStream()
            baos.withObjectOutputStream { oos ->
                oos.writeByte 123
            }
            def is = new ByteArrayInputStream(baos.toByteArray())
            byte b = -1
            is.withObjectInputStream { oos ->
                b = oos.readByte()
            }
            assert b == 123
        '''
        assertScript '''
            def baos = new ByteArrayOutputStream()
            baos.withObjectOutputStream { oos ->
                oos.writeByte 123
            }
            def is = new ByteArrayInputStream(baos.toByteArray())
            byte b = -1
            is.withObjectInputStream(this.class.classLoader) { oos ->
                b = oos.readByte()
            }
            assert b == 123
        '''
    }

    void testWithPrintWriter() {
        assertScript '''
            def baos = new ByteArrayOutputStream()
            baos.withPrintWriter { it.print 'Groovy' }
            assert new String(baos.toByteArray()) == 'Groovy'
        '''
        assertScript '''
            def wrt = new StringWriter()
            wrt.withPrintWriter { it.print 'Groovy' }
            assert wrt.toString() == 'Groovy'
        '''
    }

    void testWithReader() {
        assertScript '''
            def is = new ByteArrayInputStream("Groovy".bytes)
            char c = 'x'
            is.withReader {
                c = (char) it.read()
            }
            assert c == 'G'
        '''
        assertScript '''
            def is = new ByteArrayInputStream("Groovy".bytes)
            char c = 'x'
            is.withReader('UTF-8') {
                c = (char) it.read()
            }
            assert c == 'G'
        '''
        assertScript '''
            def is = new ByteArrayInputStream("Groovy".bytes)
            char c = 'x'
            is.newReader().withReader {
                c = (char) it.read()
            }
            assert c == 'G'
        '''
    }

    void testWithStream() {
        assertScript '''
            InputStream is = [read: {123}] as InputStream
            int x
            is.withStream { x = it.read() }
            assert x == 123
        '''
        assertScript '''
            int x
            OutputStream os = [write: { int it -> x = it }] as OutputStream
            os.withStream { it.write 123 }
            assert x == 123
        '''
    }

    void testWithWriter() {
        assertScript '''
            def baos = new ByteArrayOutputStream()
            baos.withWriter {
                it.write 'Groovy'
            }
            assert new String(baos.toByteArray()) == 'Groovy'
        '''
        assertScript '''
            def baos = new ByteArrayOutputStream()
            baos.withWriter('utf-8') {
                it.write 'Groovy'
            }
            assert new String(baos.toByteArray()) == 'Groovy'
        '''
        assertScript '''
            def baos = new ByteArrayOutputStream()
            baos.newWriter().withWriter {
                it.write 'Groovy'
            }
            assert new String(baos.toByteArray()) == 'Groovy'
        '''
    }
}
