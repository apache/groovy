package grails.util

// Superclass skeleton for fuctional tests.
// Subclasses must implement the suite() method.

class WebTest {

    @Property grailsHome
    @Property props
    @Property ant = new AntBuilder()    // may be initialize lazily for memory opt. in subclasses

    public webtestHome
    public Map configMap

    /** used from subclasses to nest the steps into */
    void webtest(String name, Closure yield){
        ant.testSpec(name:name){
            config(configMap)
            steps(){
                yield.delegate = ant
                yield()
            }
        }
    }

    /**
        Subclasses override this method to call all their test methods.
    */
    void suite() {}

    /** Main entry point to run the whole suite of tests. */
    void runTests (){
        initProps()
        initConfigMap()
        prepare()

        suite()     // template method call

        style()
    }


    void initProps () {
        ant.property(file:        'webtest/conf/webtest.properties')
        ant.property(environment: 'env')
        props = ant.antProject.properties
        grailsHome = props.grailsHome
        if (! grailsHome) {
            grailsHome = props.'env.GRAILS_HOME'
        }
        webtestHome = grailsHome + '/downloads/webtest'
        props.projectName = new File('.').absolutePath.tokenize('./\\')[-1]
        if (! props.webtest_basepath) props.webtest_basepath = props.projectName
        println 'Testing ' + props.projectName
    }

   // prepare a configmap based on webtest.properties
    def initConfigMap () {
        configMap = [:]
        def prefix = 'webtest_'
        props.keySet().each{ name ->
            if (name.startsWith(prefix)) configMap.put(name - prefix, props[name])
        }
    }

    // prepare the ant taskdef, classpath and filesystem for reporting
    void prepare() {        
        ant.taskdef(file:"${webtestHome}/webtestTaskdefs.properties")
        ant.delete () {
            fileset(dir: props.webtest_resultpath, excludes:'readme.txt')
        }
        ant.mkdir (dir: props.webtest_resultpath)
    }

    def style() {
        ant.style(
            basedir:    props.webtest_resultpath,
            destdir:    props.webtest_resultpath,
            includes:   props.webtest_resultfile,
            extension:  '.html',
            style:      webtestHome+'/resources/WebTestReport.xsl'){
            param(name:'reporttime',     expression: new Date().toString())
            param(name:'title',          expression: "The ${props.projectName} Project")
            param(name:'resources.dir',  expression: new File(webtestHome+'/resources').toURI())
        }

        // on windows, start the standard browser on the report file
        if(! props.'os.name'?.contains('Windows')) return
        def reportHtml = "$props.webtest_resultpath/$props.webtest_resultfile" - '.xml' + '.html'
        def filename = new File(reportHtml).canonicalPath
        println "opening: $filename"
        "cmd /c $filename".execute()
    }
}