// Superclass skeleton for fuctional test like BooksTest.
// Subclasses must implement the suite() method.

// todo: maybe make a selfrunning psvm

abstract class WebTest {

    @Property grailsHome
    @Property props
    @Property ant = new AntBuilder()

    def webtestHome
    public Map configMap
  

    abstract void suite()

    void runTests (){
        initWebTestHome()                
        initConfigMap()
        prepare()
        
        suite()

        style()
    }

    void webtest(String name, Closure yield){
        ant.testSpec(name:name){
            config(configMap)
            steps(){
                yield.delegate = ant
                yield()
            }
        }
    }

    // try to get from build.properties, environment variable, grailsHome/downloads/webtest
    def initWebTestHome() {
        webtestHome = props.webtestHome
        if (! webtestHome) {
            webtestHome = props.'env.WEBTEST_HOME'
        }
        if (! webtestHome) {
            webtestHome = grailsHome + '/downloads/webtest'
        }
        println "webtestHome is <$webtestHome>"
    }

   // prepare a configmap based on build.properties
    def initConfigMap () {
        def configMap = [:]
        def prefix = 'webtest_'
        props.keySet().each{ name ->
            if (name.startsWith(prefix)) configMap.put(name - prefix, props[name])
        }
    }

    // prepare the ant taskdef, classpath and filesystem for reporting
    void prepare() {        
        def rootLoader = this.class.classLoader.rootLoader
        if (rootLoader) {
            def loadDir = new File("$webtestHome/lib/")
            rootLoader.addURL(loadDir.toURL())
            loadDir.eachFileMatch(~/.*\.jar$/){
                rootLoader.addURL(it.toURL())
            }
        } else {
            println 'No RootLoader, assuming CP set by ANT call.'
        }
        ant.taskdef(file:"${webtestHome}/webtestTaskdefs.properties")
        
        ant.delete(dir: props.webtest_resultpath)
        ant.mkdir (dir: props.webtest_resultpath)
    }

    def style() {
        ant.style(
            basedir:    props.webtest_resultpath,
            destdir:    props.webtest_resultpath,
            includes:   props.webtest_resultfile,
            extension:  '.html',
            style:      webtestHome+'/resources/WebTestReport.xsl'){
            param(name:'reporttime', expression: new Date().toString())
            param(name:'title', expression: props.projectName)
        }
        // copy resources needed by the html page to the same dir:
        // the report must be ok too when opened from filesystem (without webserver)
        ant.copy(todir: props.webtest_resultpath){
            fileset(dir: webtestHome+'/resources/')
        }
        // on windows, start the standard browser on the report file
        if(! props.'os.name'?.contains('Windows')) return
        def reportHtml = "$props.webtest_resultpath/$props.webtest_resultfile" - '.xml' + '.html'
        def filename = new java.io.File(reportHtml).canonicalPath
        println "opening: $filename"
        "cmd /c $filename".execute()
    }
}