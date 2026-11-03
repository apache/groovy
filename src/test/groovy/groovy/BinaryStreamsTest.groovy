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

import org.junit.jupiter.api.Test

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.lang.reflect.Proxy
import java.util.ArrayList

import static groovy.test.GroovyAssert.shouldFail


/**
 * Test case for DefaultGroovyMethods involving Object streams and data streams.
 */
final class BinaryStreamsTest {

    @Test
    void testNewObjectStream() {
        def temp1 = tempFile
        def oos = temp1.newObjectOutputStream()

        // For fun, let's try storing & restoring a circular list.
        def writeFirst = [55, null]
        def writeSecond = [78, writeFirst]
        writeFirst[1] = writeSecond

        oos.writeObject(writeFirst)
        oos.close()

        def ois = temp1.newObjectInputStream()
        def readFirst = ois.readObject()
        assert readFirst.getClass() == ArrayList
        assert readFirst[0] == 55
        assert readFirst[1][0] == 78
        assert readFirst[1][1] == readFirst
        ois.close()
    }

    @Test
    void testWithObjectStream() {
        def temp2 = tempFile
        temp2.withObjectOutputStream { oos ->
            oos.writeInt(12345)
            oos.writeObject("Yoinks!")
            oos.writeObject(new Date(1170466550755))
        }

        temp2.withObjectInputStream { ois ->
            assert ois.readInt() == 12345;
            assert ois.readObject() == "Yoinks!"
            assert ois.readObject() == new Date(1170466550755)
        }
    }

    // GROOVY-12676
    @Test
    void testWithObjectStreamAndClassLoaderReadsPrimitiveClasses() {
        def types = [boolean, byte, char, short, int, long, float, double, void, int[], String]
        def bytes = new ByteArrayOutputStream()
        bytes.withObjectOutputStream { oos -> oos.writeObject(types) }

        def result = new ByteArrayInputStream(bytes.toByteArray()).withObjectInputStream(getClass().classLoader) { ois ->
            ois.readObject()
        }
        assert result == types
    }

    // GROOVY-12676
    @Test
    void testWithObjectStreamAndClassLoaderReadsDehydratedClosureWithPrimitiveParameters() {
        def temp = tempFile
        temp.withObjectOutputStream { oos -> oos.writeObject({ int i, long l -> i + l }.dehydrate()) }

        def closure = temp.withObjectInputStream(getClass().classLoader) { ois -> ois.readObject() }
        assert closure.parameterTypes == [int, long] as Class[]
        assert closure(1, 2L) == 3L

        def ois = temp.newObjectInputStream(getClass().classLoader)
        try {
            assert ois.readObject()(3, 4L) == 7L
        } finally {
            ois.close()
        }
    }

    // GROOVY-12676
    @Test
    void testWithObjectStreamAndClassLoaderStillResolvesClassesThroughThatLoader() {
        def bytes = new ByteArrayOutputStream()
        bytes.withObjectOutputStream { oos -> oos.writeObject([int, IntRange]) }
        def bootstrapOnly = new URLClassLoader(new URL[0], (ClassLoader) null)

        def e = shouldFail(ClassNotFoundException) {
            new ByteArrayInputStream(bytes.toByteArray()).withObjectInputStream(bootstrapOnly) { ois -> ois.readObject() }
        }
        assert e.message == 'groovy.lang.IntRange'
    }

    @Test
    void testNewDataStream() {
        def temp3 = tempFile
        def dos = temp3.newDataOutputStream()
        dos.writeInt(0x77654321)
        dos.writeChars("Miles")
        dos.close()

        temp3.withInputStream { is ->
            def data = new byte[4+5*2]
            is.read(data)
            byte[] expected = [0x77, 0x65, 0x43, 0x21, 0, 'M', 0, 'i', 0, 'l', 0, 'e', 0, 's']
            assert data as List == expected as List
            assert is.read() == -1
        }

        def dis = temp3.newDataInputStream()
        assert dis.readInt() == 0x77654321
        "Miles".each { assert dis.readChar() == it }
        dis.close()
    }

    @Test
    void testWithDataStream() {
        def temp4 = tempFile
        temp4.withDataOutputStream { dos ->
            dos.writeInt(0x12345678)
            dos.writeChars("Bubba")
        }

        temp4.withInputStream { is ->
            def data = new byte[4+5*2]
            is.read(data)
            byte[] expected = [0x12, 0x34, 0x56, 0x78, 0, 'B', 0, 'u', 0, 'b', 0, 'b', 0, 'a']
            assert data as List == expected as List
            assert is.read() == -1
        }

        temp4.withDataInputStream { dis ->
            assert dis.readInt() == 0x12345678
            "Bubba".each { assert dis.readChar() == it }
        }
    }

    // GROOVY-12681
    @Test
    void testWithObjectStreamAndClassLoaderReadsProxy() {
        def loader = new GroovyClassLoader(getClass().classLoader)
        def greeter = loader.parseClass('interface Greeter { String greet() }')
        def bytes = new ByteArrayOutputStream()
        bytes.withObjectOutputStream { oos -> oos.writeObject(newProxy(loader, greeter)) }

        def result = new ByteArrayInputStream(bytes.toByteArray()).withObjectInputStream(loader) { ois -> ois.readObject() }
        assert greeter.isInstance(result)
        assert result.getClass().classLoader.is(loader)
        assert result.greet() == 'greet'

        def ois = new ByteArrayInputStream(bytes.toByteArray()).newObjectInputStream(loader)
        try {
            assert ois.readObject().greet() == 'greet'
        } finally {
            ois.close()
        }
    }

    // GROOVY-12681
    @Test
    void testWithObjectStreamAndClassLoaderReadsProxyFromFile() {
        def loader = new GroovyClassLoader(getClass().classLoader)
        def greeter = loader.parseClass('interface Greeter { String greet() }')
        def temp = tempFile
        temp.withObjectOutputStream { oos -> oos.writeObject(newProxy(loader, greeter, Runnable)) }

        def result = temp.withObjectInputStream(loader) { ois -> ois.readObject() }
        assert greeter.isInstance(result) && result instanceof Runnable
        assert result.greet() == 'greet'

        def ois = temp.newObjectInputStream(loader)
        try {
            assert ois.readObject().greet() == 'greet'
        } finally {
            ois.close()
        }
    }

    // GROOVY-12681
    @Test
    void testWithObjectStreamAndClassLoaderReadsProxyOfNonPublicInterface() {
        def loader = new GroovyClassLoader(getClass().classLoader)
        def secret = loader.parseClass('''
            @groovy.transform.PackageScope interface Secret { String tell() }
            interface Greeter { String greet() }
        ''')
        def greeter = loader.loadClass('Greeter')
        assert !Modifier.isPublic(secret.modifiers)
        def bytes = new ByteArrayOutputStream()
        bytes.withObjectOutputStream { oos -> oos.writeObject(newProxy(secret.classLoader, greeter, secret)) }

        def result = new ByteArrayInputStream(bytes.toByteArray()).withObjectInputStream(loader) { ois -> ois.readObject() }
        assert secret.isInstance(result) && greeter.isInstance(result)
        // the proxy class is defined where its non-public interface lives
        assert result.getClass().classLoader.is(secret.classLoader)
        assert result.greet() == 'greet'
    }

    // GROOVY-12681
    @Test
    void testWithObjectStreamAndClassLoaderResolvesProxyInterfacesThroughThatLoader() {
        def bytes = new ByteArrayOutputStream()
        bytes.withObjectOutputStream { oos -> oos.writeObject(newProxy(getClass().classLoader, Greeting)) }
        def bootstrapOnly = new URLClassLoader(new URL[0], (ClassLoader) null)

        def e = shouldFail(ClassNotFoundException) {
            new ByteArrayInputStream(bytes.toByteArray()).withObjectInputStream(bootstrapOnly) { ois -> ois.readObject() }
        }
        assert e.message == Greeting.name
    }

    void manualTestRawSocketsProcessing() {
        def server
        def port = 999
        Thread.start{
            server = new ServerSocket(port)
            server.accept() { socket ->
                socket.withStreams { input, output ->
                    def ois = new ObjectInputStream(input)
                    def oos = new ObjectOutputStream(output)
                    def arg1 = ois.readObject()
                    def arg2 = ois.readObject()
                    oos << arg1 + arg2
                    ois.close()
                    oos.close()
                }
            }
        }

        def result
        def client = new Socket("localhost", port)
        client.withStreams{ input, output ->
            def oos = new ObjectOutputStream(output)
            def ois = new ObjectInputStream(input)
            oos << 1000
            oos << 24
            result = ois.readObject()
            ois.close()
            oos.close()
        }
        client.close()
        server.close()
        assert result == 1024
    }

    void manualTestObjectSocketsProcessing() {
        def server
        def port = 999
        Thread.start{
            server = new ServerSocket(port)
            server.accept() { socket ->
                socket.withObjectStreams { ois, oos ->
                    def arg1 = ois.readObject()
                    def arg2 = ois.readObject()
                    oos << arg1 + arg2
                }
            }
        }

        def result
        def client = new Socket("localhost", port)
        client.withObjectStreams{ ois, oos ->
            oos << 1000
            oos << 24
            result = ois.readObject()
        }
        client.close()
        server.close()
        assert result == 1024
    }

    private static Object newProxy(ClassLoader loader, Class<?>... interfaces) {
        Proxy.newProxyInstance(loader, interfaces, new MethodNameHandler())
    }

    interface Greeting {
        String greet()
    }

    static class MethodNameHandler implements InvocationHandler, Serializable {
        @Override
        Object invoke(Object proxy, Method method, Object[] args) {
            method.declaringClass == Object ? method.invoke(this, args) : method.name
        }
    }

    private File getTempFile() {
        def temp = File.createTempFile("BinaryStreamsTestFile", ".dat")
        temp.deleteOnExit()
        return temp
    }
}
