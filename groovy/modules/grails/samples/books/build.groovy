// General buildfile for the project in that's root this file is located.
// Obeys the environment variable 'GRAILS_HOME'.
// Adapt build.properties to you personal needs.
// Start with argument 'test' to only run the tests without build/deploy/restart

ant = new AntBuilder()
ant.property(file:          'build.properties')
ant.property(environment:   'env')
props = ant.antProject.properties

grailsHome = initGrailsHome()

if (args.toList().contains('test')){
    startTests()
    return
}
/*
withJetty { startTests() }
*/

warApplication()
deploy()

withServer {
    startTests()
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

def startTests(){
    new BooksTest(grailsHome:grailsHome, props:ant.antProject.properties).runTests()
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

def withServer (Closure yield) {
    if (props.serverDir =~ /\b5./) {
        withTomcat5(yield)
        return
    }
    withUnknownServer(yield)
}

def unknownServer (String command, boolean doPrint) {
    def filename = 'server-out.txt'
    ant.exec(dir: props.serverDir, executable: props.executable, output: filename,
        searchpath: true ){
        arg(line: command)
    }
    if (doPrint) println new java.io.File(filename).text
}
def withUnknownServer (Closure yield) {
    unknownServer(props.serverStopCommand, false)
    Thread.start { unknownServer(props.serverStartCommand, true) }
    sleep 10     // wait for server startup
    yield()
    unknownServer(props.serverStopCommand, true)
}

def tomcat (String command) {
    ant.ant(antfile:grailsHome + '/tomcat.xml', target:command){
        property(name:'build', value:'./')
        property(name:'username', value:props.serverAdminUsername)
        property(name:'password', value:props.serverAdminPassword)
    }
}
def withTomcat5 (Closure yield) {
    ant.echo(message:'*** tomcat is assumed to be running')
    try { tomcat('undeploy') } catch (Exception mayNotYetBeThere){}
    tomcat('deploy')
    ant.echo(message:'tomcat deployment done')
    yield()
}

def withJetty (Closure yield) {
    def server   = new org.mortbay.jetty.Server()
    def listener = new org.mortbay.http.SocketListener()
    listener.setPort(8080);
    server.addListener(listener)
    server.addWebApplication("/.","books.war")
    server.start()
        Thread.start { yield() }
    server.stop()
}