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
/**
 * Test case for DefaultGroovyMethods involving Object streams and data streams.
 */
class BinaryStreamsTest extends GroovyTestCase {

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
        assert readFirst.getClass() == java.util.ArrayList
        assert readFirst[0] == 55
        assert readFirst[1][0] == 78
        assert readFirst[1][1] == readFirst
        ois.close()
    }

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

    private File getTempFile() {
        def temp = File.createTempFile("BinaryStreamsTestFile", ".dat")
        temp.deleteOnExit()
        return temp
    }
}
