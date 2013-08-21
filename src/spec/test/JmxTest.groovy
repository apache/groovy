class JmxTest extends GroovyTestCase {

    void testIntroduction() {
        shouldCompile '''
            MBeanServerConnection server = null
            String beanName = 'Person'
            
            // tag::introduction_example[]
            println server.getAttribute(beanName, 'Age')
            server.setAttribute(beanName, new Attribute('Name', 'New name'))
            Object[] params = [5, 20]
            String[] signature = [Integer.TYPE, Integer.TYPE]
            println server.invoke(beanName, 'add', params, signature)
            // end::introduction_example[]

            // tag::introduction_simplified_example[]
            def mbean = new GroovyMBean(server, beanName)
            println mbean.Age
            mbean.Name = 'New name'
            println mbean.add(5, 20)
            // end::introduction_simplified_example[]
        '''
    }

    void testTheJvm() {
        shouldCompile '''
            // tag::the_jvm[]
            import java.lang.management.*

            def os = ManagementFactory.operatingSystemMXBean
            println """OPERATING SYSTEM:
            \tarchitecture = $os.arch
            \tname = $os.name
            \tversion = $os.version
            \tprocessors = $os.availableProcessors
            """

            def rt = ManagementFactory.runtimeMXBean
            println """RUNTIME:
            \tname = $rt.name
            \tspec name = $rt.specName
            \tvendor = $rt.specVendor
            \tspec version = $rt.specVersion
            \tmanagement spec version = $rt.managementSpecVersion
            """

            def cl = ManagementFactory.classLoadingMXBean
            println """CLASS LOADING SYSTEM:
            \tisVerbose = ${cl.isVerbose()}
            \tloadedClassCount = $cl.loadedClassCount
            \ttotalLoadedClassCount = $cl.totalLoadedClassCount
            \tunloadedClassCount = $cl.unloadedClassCount
            """

            def comp = ManagementFactory.compilationMXBean
            println """COMPILATION:
            \ttotalCompilationTime = $comp.totalCompilationTime
            """

            def mem = ManagementFactory.memoryMXBean
            def heapUsage = mem.heapMemoryUsage
            def nonHeapUsage = mem.nonHeapMemoryUsage
            println """MEMORY:
            HEAP STORAGE:
            \tcommitted = $heapUsage.committed
            \tinit = $heapUsage.init
            \tmax = $heapUsage.max
            \tused = $heapUsage.used
            NON-HEAP STORAGE:
            \tcommitted = $nonHeapUsage.committed
            \tinit = $nonHeapUsage.init
            \tmax = $nonHeapUsage.max
            \tused = $nonHeapUsage.used
            """

            ManagementFactory.memoryPoolMXBeans.each { mp ->
                println "\tname: " + mp.name
                String[] mmnames = mp.memoryManagerNames
                mmnames.each{ mmname ->
                    println "\t\tManager Name: $mmname"
                }
                println "\t\tmtype = $mp.type"
                println "\t\tUsage threshold supported = " + mp.isUsageThresholdSupported()
            }
            println()

            def td = ManagementFactory.threadMXBean
            println "THREADS:"
            td.allThreadIds.each { tid ->
                println "\tThread name = ${td.getThreadInfo(tid).threadName}"
            }
            println()

            println "GARBAGE COLLECTION:"
            ManagementFactory.garbageCollectorMXBeans.each { gc ->
                println "\tname = $gc.name"
                println "\t\tcollection count = $gc.collectionCount"
                println "\t\tcollection time = $gc.collectionTime"
                String[] mpoolNames = gc.memoryPoolNames
                mpoolNames.each { mpoolName ->
                    println "\t\tmpool name = $mpoolName"
                }
            }
            // end::the_jvm[]
        '''   
    }

    void testTomcat() {
        shouldCompile '''
            @Grab('org.jfree:jfreechart:1.0.15')
            // tag::tomcat[]
            import javax.management.ObjectName
            import javax.management.remote.JMXConnectorFactory as JmxFactory
            import javax.management.remote.JMXServiceURL as JmxUrl
            import org.jfree.chart.ChartFactory
            import org.jfree.data.category.DefaultCategoryDataset as Dataset
            import org.jfree.chart.plot.PlotOrientation as Orientation
            import groovy.swing.SwingBuilder
            import javax.swing.WindowConstants as WC

            def serverUrl = 'service:jmx:rmi:///jndi/rmi://localhost:9004/jmxrmi'
            def server = JmxFactory.connect(new JmxUrl(serverUrl)).MBeanServerConnection
            def serverInfo = new GroovyMBean(server, 'Catalina:type=Server').serverInfo
            println "Connected to: $serverInfo"

            def query = new ObjectName('Catalina:*')
            String[] allNames = server.queryNames(query, null)
            def modules = allNames.findAll { name ->
                name.contains('j2eeType=WebModule')
            }.collect{ new GroovyMBean(server, it) }

            println "Found ${modules.size()} web modules. Processing ..."
            def dataset = new Dataset()

            modules.each { m ->
                println m.name()
                dataset.addValue m.processingTime, 0, m.path
            }

            def labels = ['Time per Module', 'Module', 'Time']
            def options = [false, true, true]
            def chart = ChartFactory.createBarChart(*labels, dataset,
                            Orientation.VERTICAL, *options)
            def swing = new SwingBuilder()
            def frame = swing.frame(title:'Catalina Module Processing Time', defaultCloseOperation:WC.EXIT_ON_CLOSE) {
                panel(id:'canvas') { rigidArea(width:600, height:250) }
            }
            frame.pack()
            frame.show()
            chart.draw(swing.canvas.graphics, swing.canvas.bounds)
            // end::tomcat[]
        '''   
    }

    void testWebLogic() {
        shouldCompile '''
            // tag::weblogic[]
            import javax.management.remote.*
            import javax.management.*
            import javax.naming.Context

            def urlRuntime = '/jndi/weblogic.management.mbeanservers.runtime'
            def urlBase = 'service:jmx:t3://localhost:7001'

            def serviceURL = new JMXServiceURL(urlBase + urlRuntime)
            def h = new Hashtable()
            h.put(Context.SECURITY_PRINCIPAL, 'weblogic')
            h.put(Context.SECURITY_CREDENTIALS, 'weblogic')
            h.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, 'weblogic.management.remote')
            def server = JMXConnectorFactory.connect(serviceURL, h).MBeanServerConnection
            def domainName = new ObjectName('com.bea:Name=RuntimeService,Type=weblogic.management.mbeanservers.runtime.RuntimeServiceMBean')
            def rtName = server.getAttribute(domainName, 'ServerRuntime')
            def rt = new GroovyMBean(server, rtName)
            println "Server: name=$rt.Name, state=$rt.State, version=$rt.WeblogicVersion"
            def destFilter = Query.match(Query.attr('Type'), Query.value('JMSDestinationRuntime'))
            server.queryNames(new ObjectName('com.bea:*'), destFilter).each { name ->
                def jms = new GroovyMBean(server, name)
                println "JMS Destination: name=$jms.Name, type=$jms.DestinationType, messages=$jms.MessagesReceivedCount"
            }
            // end::weblogic[]
        '''   
    }

    void testSpringClasses() {
        shouldCompile '''
            @Grab('org.springframework:spring-context:3.2.0.RELEASE')
            // tag::spring_classes[]
            import org.springframework.jmx.export.annotation.*

            @ManagedResource(objectName="bean:name=calcMBean", description="Calculator MBean")
            public class Calculator {

                private int invocations

                @ManagedAttribute(description="The Invocation Attribute")
                public int getInvocations() {
                    return invocations
                }

                private int base = 10

                @ManagedAttribute(description="The Base to use when adding strings")
                public int getBase() {
                    return base
                }

                @ManagedAttribute(description="The Base to use when adding strings")
                public void setBase(int base) {
                    this.base = base
                }

                @ManagedOperation(description="Add two numbers")
                @ManagedOperationParameters([
                    @ManagedOperationParameter(name="x", description="The first number"),
                    @ManagedOperationParameter(name="y", description="The second number")])
                public int add(int x, int y) {
                    invocations++
                    return x + y
                }

                @ManagedOperation(description="Add two strings representing numbers of a particular base")
                @ManagedOperationParameters([
                    @ManagedOperationParameter(name="x", description="The first number"),
                    @ManagedOperationParameter(name="y", description="The second number")])
                public String addStrings(String x, String y) {
                    invocations++
                    def result = Integer.valueOf(x, base) + Integer.valueOf(y, base)
                    return Integer.toString(result, base)
                }
            }
            // end::spring_classes[]
        '''   
    }

    void testSpringUsage() {
        shouldCompile '''
            @Grab('org.springframework:spring-context:3.2.0.RELEASE')
            // tag::spring_usage[]
            import org.springframework.context.support.ClassPathXmlApplicationContext
            import java.lang.management.ManagementFactory
            import javax.management.ObjectName
            import javax.management.Attribute

            // get normal bean
            def ctx = new ClassPathXmlApplicationContext("beans.xml")
            def calc = ctx.getBean("calcBean")

            Thread.start {
                // access bean via JMX, use a separate thread just to
                // show that we could access remotely if we wanted
                def server = ManagementFactory.platformMBeanServer
                def mbean = new GroovyMBean(server, 'bean:name=calcMBean')
                sleep 1000
                assert 8 == mbean.add(7, 1)
                mbean.Base = 8
                assert '10' == mbean.addStrings('7', '1')
                mbean.Base = 16
                sleep 2000
                println "Number of invocations: $mbean.Invocations"
                println mbean
            }

            assert 15 == calc.add(9, 6)
            assert '11' == calc.addStrings('10', '1')
            sleep 2000
            assert '20' == calc.addStrings('1f', '1')
            // end::spring_usage[]
        '''   
    }

    void testTroubleshooting() {
        shouldCompile '''
            import javax.management.remote.*

            def password = '123'
            def serverUrl = 'service:jmx://localhost:8080'

            // tag::troubleshooting[]
            def jmxEnv = null
            if (password != null) {
                jmxEnv = [(JMXConnector.CREDENTIALS): (String[])["monitor", password]]
            }
            def connector = JMXConnectorFactory.connect(new JMXServiceURL(serverUrl), jmxEnv)
            // end::troubleshooting[]
        '''   
    }
}
