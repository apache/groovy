package foo.bar

class SampleMain {

    @Property String name = "James"


    static void main(args) {
        foo = new SampleMain()
        println "Hello ${foo.name}"

        assert foo.getName() == "James"
    }
}