// Superclass skeleton for fuctional test like BooksTest.
// Subclasses must implement the suite() method.

// todo: maybe make a selfrunning psvm

abstract class WebTest {
    @Property ant
    @Property grailsHome

    public Map configMap

    abstract void suite()

    void runTests (){
        def props = ant.antProject.properties
        def webtestHome = locateWebTestHome(props)
        configMap = getConfigMap(props)
        prepare(webtestHome, props)

        suite()

        style(webtestHome, props)
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
    String locateWebTestHome(props) {
        def webtestHome = props.webtestHome
        if (! webtestHome) {
            webtestHome = props.'env.WEBTEST_HOME'
        }
        if (! webtestHome) {
            webtestHome = grailsHome + '/downloads/webtest'
        }
        println "webtestHome is <$webtestHome>"
        return webtestHome
    }

   // prepare a configmap based on build.properties
    Map getConfigMap (props) {
        def result = [:]
        props.keySet().each{ name ->
            if (name.startsWith('webtest_')) result.put(name[8..-1], props[name])
        }
        return result
    }

    // prepare the ant taskdef, classpath and filesystem for reporting
    void prepare(webtestHome, props) {
        ant.taskdef(file:"${webtestHome}/webtestTaskdefs.properties"){
            // webtest jars need to be in ${user.home}/.groovy/lib
        }
        ant.delete(dir: props.webtest_resultpath)
        ant.mkdir (dir: props.webtest_resultpath)
    }

    void style(webtestHome, props) {
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