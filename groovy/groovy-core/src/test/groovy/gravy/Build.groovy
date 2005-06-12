package groovy.gravy


/**
 * Represents a build process
 */
class Build implements Runnable {
    def ant = new AntBuilder()
    def args
    def pom
    def defaultTargets = ['clean', 'compile']

    static void main(args) {
        // autogenerate this
        def b = new Build(args)
        //b.args = args
        b.run()
    }

    Build(someArgs) {
        args = someArgs;
        if (args instanceof String) {
            args = [args]
        }
        /*
        if (args == null || args.size() == 0) {
            args = defaultTargets
        }
        */
    }

    void run() {
        for (a in args) {
            println "Target: ${a}"
            invokeMethod(a.toString(), null)
        }
    }

    def getPom() {
        if (pom == null) {
            pom = parsePOM()
        }
        return pom
    }


    // Default goals
    def clean() {
        ant.rmdir(dir:'gravy')
    }

    def compile() {
        ant.mkdir(dir:'gravy/classes')
        ant.compile(srdir:'src/main/java', destdir:'gravy/classes') {
            fileset {
                includes(name:'**/*.java')
            }
        }
    }

    protected def parsePOM() {
        return new XmlParser().parse("project.xml")
    }
}