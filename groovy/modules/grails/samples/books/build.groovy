// General buildfile for the project in that's root this file is located.
// Obeys the environment variable 'GRAILS_HOME'.
// Adapt build.properties to you personal needs.

ant = new AntBuilder()
ant.property(file:          'build.properties')
ant.property(environment:   'env')
props = ant.antProject.properties

grailsHome = initGrailsHome()
warApplication()
deploy()

withServer {
    new BooksTest(ant: ant, grailsHome: grailsHome).runTests()
}

// method implementations ---------------------------------

String initGrailsHome () {
    def grailsHome = props.grailsHome
    if (! grailsHome) {
        grailsHome = props.'env.GRAILS_HOME'
    }
    println "grailsHome is <$grailsHome>"
    return grailsHome
}

// call the general 'war' target and 'init' only if needed
String warApplication () {
    buildFile = grailsHome + '/src/grails/build.xml'

    if ( ! new java.io.File('tmp').exists()) {
        ant.ant(antfile: buildFile, target:'init')
    }
    ant.ant(antfile:buildFile, target:'war')
    return grailsHome
}

def deploy () {
    targetDir = "$props.serverDir/$props.serverWebappDir"
    ant.copy(file:'grails-app.war', todir: targetDir)
}

def server (String command, boolean doPrint) {
    def filename = 'server-out.txt'
    ant.exec(dir: props.serverDir, executable: props.executable, output: filename,
        searchpath: true ){
        arg(line: command)
    }
    if (doPrint) println new java.io.File(filename).text
}

def withServer (Closure yield) {
    // stopping a running server at this point is not reliable
    Thread.start { server(props.serverStartCommand, true) }
    sleep 5 // grant the server some time to init
    yield()
    server(props.serverStopCommand, true)
}