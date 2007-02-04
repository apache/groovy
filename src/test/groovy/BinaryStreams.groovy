/**
 * Test case for DefaultGroovyMethods involving Object streams and data streams.
 *
 * @author Martin C. Martin
 */
class BinaryStreams extends GroovyTestCase {

    def tempFile = new File("BinaryStreamsTestFile")

   void testNewObjectStream() {
       def oos = tempFile.newObjectOutputStream()

       // For fun, lets try storing & restoring a circular list.
       def writeFirst = [55, null]
       def writeSecond = [78, writeFirst]
       writeFirst[1] = writeSecond

       oos.writeObject(writeFirst)
       oos.close()

       def ois = tempFile.newObjectInputStream()
       def readFirst = ois.readObject()
       assert readFirst.getClass() == java.util.ArrayList
       assert readFirst[0] == 55
       assert readFirst[1][0] == 78
       assert readFirst[1][1] == readFirst
       ois.close()

       assert tempFile.delete()
   }

   void testWithObjectStream() {
      tempFile.withObjectOutputStream { oos ->
         oos.writeInt(12345)
         oos.writeObject("Yoinks!")
         oos.writeObject(new Date(1170466550755))
      }

      tempFile.withObjectInputStream { ois ->
        assert ois.readInt() == 12345;
        assert ois.readObject() == "Yoinks!"
        assert ois.readObject() == new Date(1170466550755)
      }

      // For some reason, this returns false.  Weird.
      tempFile.delete()
   }

   void testNewDataStream() {
      def dos = tempFile.newDataOutputStream()
      dos.writeInt(0x77654321)
      dos.writeChars("Miles")
      dos.close()

      tempFile.withInputStream { is ->
         def data = new byte[4+5*2]
         is.read(data)
         byte[] expected = [0x77, 0x65, 0x43, 0x21, 0, 'M', 0, 'i', 0, 'l', 0, 'e', 0, 's']
         assert data as List == expected as List
         assert is.read() == -1
      }

      def dis = tempFile.newDataInputStream()
      assert dis.readInt() == 0x77654321
      "Miles".each { assert dis.readChar() == it }
      dis.close()

      assert tempFile.delete()
   }

   void testWithDataStream() {
      tempFile.withDataOutputStream { dos ->
         dos.writeInt(0x12345678)
         dos.writeChars("Bubba")
      }

      tempFile.withInputStream { is ->
         def data = new byte[4+5*2]
         is.read(data)
         byte[] expected = [0x12, 0x34, 0x56, 0x78, 0, 'B', 0, 'u', 0, 'b', 0, 'b', 0, 'a']
         assert data as List == expected as List
         assert is.read() == -1
      }

      tempFile.withDataInputStream { dis ->
         assert dis.readInt() == 0x12345678
         "Bubba".each { assert dis.readChar() == it }
      }

      assert tempFile.delete()
   }
}
