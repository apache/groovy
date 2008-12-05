package groovy.script

class StreamClassloaderInScriptTest extends GroovyShellTestCase {

    final static BLAH = """
class Blah implements Serializable {
def x, y
boolean equals(def other) { x == other.x && y == other.y }
}
"""

    void testSerializationToFileWithinScript() {
        evaluate BLAH + """
def file = new File('blahblah.dat')
file.deleteOnExit()
def data = [ new Blah(x:1, y:2), new Blah(x:'flob', y:'adob') ]
file.withObjectOutputStream { oos ->
    oos.writeObject ( data )
}
// in a script we must set the classloader
file.withObjectInputStream(getClass().classLoader){ ois ->
    def newData = ois.readObject ( )
    assert data == newData
}
        """
    }

    void testSerializationToBytesWithinScript() {
        evaluate BLAH + """
def baos = new ByteArrayOutputStream()
def data = [ new Blah(x:1, y:2), new Blah(x:'flob', y:'adob') ]
baos.withObjectOutputStream { oos ->
    oos.writeObject ( data )
}
def bais = new ByteArrayInputStream(baos.toByteArray())
// in a script we must set the classloader
bais.withObjectInputStream(getClass().classLoader){ ois ->
    def newData = ois.readObject ( )
    assert data == newData
}
        """
    }

}