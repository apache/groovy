
/**
 * Test case for the eachObject method on a file containing
 * zero, one or more objects (object stream).  Also test cases
 * for eachDir, eachFileMatch and runAfter methods.
 *
 * @author Hein Meling
 */
class GroovyClosureMethodsTest extends GroovyTestCase {

    private String dirname = "src/test/groovy"

    private String filename = "${dirname}/eachObject"

    void testEachObjectMany() {
        def file = new File(filename)
        oos = new ObjectOutputStream(new FileOutputStream(file))
        list = [1, 2, 3, "foo", 9, "bar", 191, file, 9129]
        list.each {
            oos.writeObject(it)
        }

        println("Contents of file with multiple objects: " + file)
        int c = 0
        file.eachObject {
            print "${it} "
            c++
        }
        assert list.size() == c
        println ""
        //ensure to remove the created file
        file.delete()
    }

    void testEachObjectOne() {
        def file = new File(filename)
        oos = new ObjectOutputStream(new FileOutputStream(file))
        oos.writeObject(file)

        println("Contents of file with one object: " + file)
        int c = 0
        file.eachObject {
            print "${it} "
            c++
        }
        assert c == 1
        println ""
        //ensure to remove the created file
        file.delete()
    }

    void testEachObjectEmptyFile() {
        def file = new File(filename)
        oos = new ObjectOutputStream(new FileOutputStream(file))

        println("Contents of empty file: " + file)
        int c = 0
        file.eachObject {
            print "${it} "
            c++
        }
        assert c == 0
        println ""
        //ensure to remove the created file
        file.delete()
    }

    void testEachObjectNullFile() {
        def file = new File(filename)
        oos = new ObjectOutputStream(new FileOutputStream(file))
        oos.writeObject(null)
        oos.writeObject("foo")
        oos.writeObject(null)

        println("Contents of null file: " + file)
        int c = 0
        file.eachObject {
            print "${it} "
            c++
        }
        assert c == 3
        println ""
        //ensure to remove the created file
        file.delete()
    }

    void testEachDir() {
        def dir = new File(dirname)

        println("Directories in: " + dir)
        int c = 0
        dir.eachDir {
            print "${it} "
            c++
        }
        println ""
        assert c > 0
    }

    void testEachFileMatch() {
        def file = new File(dirname)

        print "Files with the text Groovy: "
        file.eachFileMatch(~"^Groovy.*") {
            print "${it} "
        }
        println ""

        print "Files with the text Closure: "
        file.eachFileMatch(~"^Closure.*") {
            print "${it} "
        }
        println ""

        print "This file is here: "
        int c = 0
        file.eachFileMatch(~"^GroovyClosureMethodsTest.groovy") {
            print "${it} "
            c++
        }
        assert c == 1
        println ""
    }

    void testRunAfter() {
        timer = new Timer()
        boolean status = false
        timer.runAfter(2000) {
            println "Running after 2 seconds wait"
            status = true
        }
        println "I should run first"
        assert status == false
        Thread.sleep(3000)
        println "I should run last"
        assert status == true
    }

}
