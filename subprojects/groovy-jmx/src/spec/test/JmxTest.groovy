/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
import gls.CompilableTestSupport

class JmxTest extends CompilableTestSupport {

    void testIntroduction() {
        shouldCompile '''
            import javax.management.Attribute
            import javax.management.MBeanServerConnection

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
            @Grab('org.jfree:jfreechart:1.5.0')

            // tag::tomcat[]
            import groovy.swing.SwingBuilder
            import groovy.jmx.GroovyMBean

            import javax.management.ObjectName
            import javax.management.remote.JMXConnectorFactory as JmxFactory
            import javax.management.remote.JMXServiceURL as JmxUrl
            import javax.swing.WindowConstants as WC

            import org.jfree.chart.ChartFactory
            import org.jfree.data.category.DefaultCategoryDataset as Dataset
            import org.jfree.chart.plot.PlotOrientation as Orientation

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
            def frame = swing.frame(title:'Catalina Module Processing Time', defaultCloseOperation:WC.DISPOSE_ON_CLOSE) {
                panel(id:'canvas') { rigidArea(width:800, height:350) }
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
            @Grab('org.springframework:spring-context:5.2.8.RELEASE')
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
            @Grab('org.springframework:spring-context:5.2.8.RELEASE')
            // tag::spring_usage[]
            import org.springframework.context.support.ClassPathXmlApplicationContext
            import java.lang.management.ManagementFactory
            import javax.management.ObjectName
            import javax.management.Attribute
            import groovy.jmx.GroovyMBean

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

    void testJmxBuilder() {
        shouldCompile '''
            @Grab('org.codehaus.groovy:groovy-jmx:2.1.6')
            import groovy.jmx.builder.JmxBuilder
            import javax.management.ObjectName

            // tag::instantiating_jmxbuilder[]
            def jmx = new JmxBuilder()
            // end::instantiating_jmxbuilder[]

            // tag::connector_server[]
            jmx.connectorServer(port: 9000).start()
            // end::connector_server[]

            // tag::connector_server_and_local_registry[]
            import java.rmi.registry.LocateRegistry
            //...

            LocateRegistry.createRegistry(9000)
            jmx.connectorServer(port: 9000).start()
            // end::connector_server_and_local_registry[]

            // tag::client_connector[]
            def client = jmx.connectorClient(port: 9000)
            client.connect()
            // end::client_connector[]

            // tag::client_connector_usage[]
            client.getMBeanServerConnection()
            // end::client_connector_usage[]

            class Foo {}
            class Bar {}
            class SomeBar {}
            if (true) {
                // tag::jmxbuilder_export[]
                def beans = jmx.export {
                    bean(new Foo())
                    bean(new Bar())
                    bean(new SomeBar())
                }
                // end::jmxbuilder_export[]
            }

            // tag::request_controller[]
            class RequestController {
                // constructors
                RequestController() { super() }
                RequestController(Map resource) { }

                // attributes
                boolean isStarted() { true }
                int getRequestCount() { 0 }
                int getResourceCount() { 0 }
                void setRequestLimit(int limit) { }
                int getRequestLimit() { 0 }

                // operations
                void start() { }
                void stop() { }
                void putResource(String name, Object resource) { }
                void makeRequest(String res) { }
                void makeRequest() { }
            }
            // end::request_controller[]

            if (true) {            
                // tag::implicit_export[]
                jmx.export {
                    bean(new RequestController(resource: "Hello World"))
                }
                // end::implicit_export[]
            }

            if (true) {
                // tag::using_bean[]
                def ctrl = new RequestController(resource:"Hello World")
                def beans = jmx.export {
                    bean(target: ctrl, name: "jmx.tutorial:type=Object")
                }
                // end::using_bean[]
            }

            if (true) {
                // tag::export_all_attributes[]
                def objName = new ObjectName("jmx.tutorial:type=Object")
                def beans = jmx.export {
                    bean(target: new RequestController(),
                    name: objName,
                    attributes: "*")
                }
                // end::export_all_attributes[]
            }

            if (true) {
                // tag::export_attribute_list[]
                def objName = new ObjectName("jmx.tutorial:type=Object")
                def beans = jmx.export {
                    bean(
                        target: new RequestController(),
                        name: objName,
                        attributes: ["Resource", "RequestCount"]
                    )
                }
                // end::export_attribute_list[]
            }

            if (true) {
                // tag::export_attribute_with_explicit_descriptors[]
                def objName = new ObjectName("jmx.tutorial:type=Object")
                def beans = jmx.export {
                    bean(
                        target: new RequestController(),
                        name: objName,
                        attributes: [
                            "Resource": [desc: "The resource to request.", readable: true, writable: true, defaultValue: "Hello"],
                            "RequestCount": "*"
                        ]
                    )
                }
                // end::export_attribute_with_explicit_descriptors[]
            }

            if (true) {
                // tag::export_all_constructors[]
                def objName = new ObjectName("jmx.tutorial:type=Object")
                def beans = jmx.export {
                    bean(
                        target: new RequestController(),
                        name: objName,
                        constructors: "*"
                    )
                }
                // end::export_all_constructors[]
            }

            if (true) {
                // tag::export_constructors_using_parameter_descriptor[]
                def objName = new ObjectName("jmx.tutorial:type=Object")
                def beans = jmx.export {
                    bean(
                        target: new RequestController(),
                        name: objName,
                        constructors: [
                            "RequestController": ["Object"]
                        ]
                    )
                }
                // end::export_constructors_using_parameter_descriptor[]
            }

            if (true) {
                // tag::export_constructor_with_explicit_descriptors[]
                def objName = new ObjectName("jmx.tutorial:type=Object")
                def beans = jmx.export {
                    bean(target: new RequestController(), name: objName,
                        constructors: [
                            "RequestController": [
                                desc: "Constructor takes param",
                                params: ["Object" : [name: "Resource", desc: "Resource for controller"]]
                            ]
                        ]
                    )
                }
                // end::export_constructor_with_explicit_descriptors[]
            }

            if (true) {
                // tag::export_all_operations[]
                def objName = new ObjectName("jmx.tutorial:type=Object")
                def beans = jmx.export {
                    bean(
                        target: new RequestController(),
                        name: objName,
                        operations: "*"
                    )
                }
                // end::export_all_operations[]
            }

            if (true) {
                // tag::export_operation_list[]
                def objName = new ObjectName("jmx.tutorial:type=Object")
                def beans = jmx.export {
                    bean(
                        target: new RequestController(),
                        name: objName,
                        operations: ["start", "stop"]
                    )
                }
                // end::export_operation_list[]
            }

            if (true) {
                // tag::export_operations_by_signature[]
                def objName = new ObjectName("jmx.tutorial:type=Object")
                def beans = jmx.export {
                    bean(
                        target: new RequestController(),
                        name: objName,
                        operations: [
                            "makeRequest": ["String"]
                        ]
                    )
                }
                // end::export_operations_by_signature[]
            }

            if (true) {
                // tag::export_operations_with_explicit_descriptors[]
                def objName = new ObjectName("jmx.tutorial:type=Object")
                def beans = jmx.export {
                    bean(target: new RequestController(), name: objName,
                        operations: [
                            "start": [desc: "Starts request controller"],
                            "stop": [desc: "Stops the request controller"],
                            "setResource": [params: ["Object"]],
                            "makeRequest": [
                                desc: "Executes the request.",
                                params: [
                                    "String": [name: "Resource", desc: "The resource to request"]
                                ]
                            ]
                        ]
                    )
                }
                // end::export_operations_with_explicit_descriptors[]
            }

            // tag::embedding_descriptor[]
            class RequestControllerGroovy {
                // attributes
                boolean started
                int requestCount
                int resourceCount
                int requestLimit
                Map resources

                // operations
                void start() { }
                void stop(){ }
                void putResource(String name, Object resource) { }
                void makeRequest(String res) { }
                void makeRequest() { }

                static descriptor = [
                    name: "jmx.builder:type=EmbeddedObject",
                    operations: ["start", "stop", "putResource"],
                    attributes: "*"
                ]
            }
            
            // export
            jmx.export(
                bean(new RequestControllerGroovy())
            )
            // end::embedding_descriptor[]

            // tag::exporting_timer[]
            def timer = jmx.timer(name: "jmx.builder:type=Timer", event: "heartbeat", period: "1s")
            timer.start()
            // end::exporting_timer[]

            if (true) {
                // tag::exporting_timer_beans[]
                def beans = jmx.export {
                    timer(name: "jmx.builder:type=Timer1", event: "event.signal", period: "1s")
                    timer(name: "jmx.builder:type=Timer2", event: "event.log", period: "1s")
                }
                beans[0].start()
                beans[1].start()
                // end::exporting_timer_beans[]
            }

            def callback
            // tag::event_handling_closures[]
            callback = { ->
                // event handling code here.
            }
            // end::event_handling_closures[]

            // tag::event_handling_closures_event[]
            callback = { event ->
                // event handling code
            }
            // end::event_handling_closures_event[]

            // tag::handling_attribute_onchange_event[]
            jmx.export {
                bean(
                    target: new RequestController(), name: "jmx.tutorial:type=Object",
                    attributes: [
                        "Resource": [
                            readable: true, writable: true,
                            onChange: { e ->
                                println e.oldValue
                                println e.newValue
                            }
                        ]
                    ]
                )
            }
            // end::handling_attribute_onchange_event[]

            // tag::event_handler[]
            class EventHandler {
                void handleStart(e){
                    println e
                }
            }
            // end::event_handler[]

            if (true) {
                // tag::event_handler_usage[]
                def handler = new EventHandler()

                def beans = jmx.export {
                    bean(target: new RequestController(), name: "jmx.tutorial:type=Object",
                        operations: [
                            "start": [
                                desc:"Starts request controller",
                                onCall:handler.&handleStart
                            ]
                        ]
                    )
                }
                // end::event_handler_usage[]
            }

            if (true) {
                // tag::listener_mbean[]
                def beans = jmx.export {
                    timer(name: "jmx.builder:type=Timer", event: "heartbeat", period: "1s").start()
                    bean(target: new RequestController(), name: "jmx.tutorial:type=Object",
                        operations: "*",
                        listeners: [
                            heartbeat: [
                                from: "jmx.builder:type=Timer",
                                call: { e ->
                                    println e
                                }
                            ]
                        ]
                    )
                }
                // end::listener_mbean[]
            }

            // tag::jmxbuilders_listener[]
            jmx.timer(name: "jmx.builder:type=Timer", period: "1s").start()

            jmx.listener(
                from: "jmx.builder:type=Timer",
                call: { e ->
                    println "beep..."
                }
            )
            // end::jmxbuilders_listener[]

            // tag::declare_emitter[]
            def emitter = jmx.emitter()
            // end::declare_emitter[]

            // tag::emitter_broadcast_event[]
            emitter.send()
            // end::emitter_broadcast_event[]

            // tag::emitter_broadcast_event_with_objects[]
            emitter.send("Hello!")
            // end::emitter_broadcast_event_with_objects[]
        '''
    }
}
