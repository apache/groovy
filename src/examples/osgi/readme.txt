About this Example
------------------
This example demonstrates two different usages of OSGi. The "hello-groovy-bundle" 
service demonstrates how to write an OSGi bundle using Groovy. It is a simple 
service that does two things when started within an OSGi container: 1) it prints 
out a message to the console, and 2) it adds a Groovy service to the OSGi context 
that can be consumed by the second example.  

The "hello-groovy-test-harness" service, also written in Groovy, demonstrates how 
to import and use the previous "hello-groovy-bundle" OSGi service. It locates and 
invokes the service from the first example, which results in a message being 
written to the console. 


Building this Example
---------------------
IMPORTANT: You must edit build.properties before building the example. There are 
three properties that must be set in build.properties: 
  groovy.bin.dir - The example requires you specify the location of your 
                   groovy-all jar. This is hwo the build finds groovyc. 
  groovy.version - The example requires you specify the version of your groovy-all 
                   jar. This is so that the jar files can be built correctly. 
  osgi.jar - The example requires you specify the location of the OSGi jar. 
                   This is required to compile the code. 

This example was tested using the OSGi jar from Equinox 3.4, the OSGi container 
that ships with Eclipse. You can download the Equinox jar from the Equinox website
or search for it within your Eclipse directories. The jar will have a name similar 
to : org.eclipse.osgi_3.4.0.v20080605-1900.jar
  
Once these properties are set, simply run ant to build: 
  
  ant

The build creates three jar files: 
  hello-bundle-imports-groovy.jar - OSGi bundle written in Groovy that resolves the 
        groovy-all Jar file from the container.
  hello-bundle-contains-groovy.jar - OSGi bundle written in Groovy that resolves the
        groovy-all Jar file from within itself. The container never sees Groovy. 
  hello-groovy-test-harness.jar - OSGi bundle that loads and tests one of the previous
        two services. 
  
The build also prints out the file URLs of the jar files. You need these URLs to 
run the example. Also printed to the console is the command to run the Equinox 
container. The final output of the Ant script may look like this: 

     [echo] To run the OSGi console, run the following command:
     [echo]  java -jar ../../../../equinox-3.4/eclipse/plugins/org.eclipse.osgi_3.4.0.v20080605-1900.jar -console
     [echo] To install these applications in the container, run the following commands in the OSGi container:
     [echo]  install file:/home/user/dev/groovy-core/target/dist/groovy-all-1.7-beta-1-SNAPSHOT.jar
     [echo]  install file:/home/user/dev/groovy-core/src/examples/osgi/build/hello-bundle-imports-groovy.jar
     [echo]  install file:/home/user/dev/groovy-core/src/examples/osgi/build/hello-bundle-contains-groovy.jar
     [echo]  install file:/home/user/dev/groovy-core/src/examples/osgi/build/hello-groovy-test-harness.jar
     [echo] To start the applications in the container, run the following commands in the OSGi container:
     [echo]  start [bundle1] [bundle2]
     [echo] Where [bundle1] and [bundle] are the bundle IDs printed by the console in the previous step.


Running this Example
--------------------
To run the example you must start the OSGi container, install the services, and 
start the services. 

To start the Equinox container, invoke the OSGi jar using java: 
  
  java -jar ../../../../equinox-3.4/eclipse/plugins/org.eclipse.osgi_3.4.0.v20080605-1900.jar -console

This opens an OSGi console. You should be presented with an OSGi prompt: 

  osgi> 

Type the command "ss" to get a system status: 

  osgi> ss

  Framework is launched.

  id      State       Bundle
  0       ACTIVE      org.eclipse.osgi_3.4.0.v20080605-1900

Install the three bundles using the "install" command and the file URLs of the 
jars built by Ant. Remember, the Ant script printed the file URLs to the console 
as part of the build. 

osgi> install file:/home/user/dev/groovy-core/target/dist/groovy-all-1.7-beta-1-SNAPSHOT.jar
Bundle id is 1

osgi> install file:/home/user/dev/groovy-core/src/examples/osgi/build/hello-bundle-imports-groovy.jar
Bundle id is 2

osgi> install file:/home/user/dev/groovy-core/src/examples/osgi/build/hello-groovy-test-harness.jar
Bundle id is 3

Run the ss command to verify the bundles loaded correctly: 


osgi> ss

Framework is launched.

id      State       Bundle
0       ACTIVE      org.eclipse.osgi_3.4.0.v20080605-1900
1       INSTALLED   groovy-all_1.7.0.beta-1-SNAPSHOT
2       INSTALLED   org.codehaus.groovy.osgi.hello-groovy-bundle_1.0.0
3       INSTALLED   org.codehaus.groovy.osgi.harness.hello-groovy-test-harness_1.0.0

Start the bundles with the "start" command to see them working: 

osgi> start 1 2 3
Groovy BundleActivator started
1 GroovyGreeter services found.
Hello from the Groovy Greeter!

As expected, bundle 2 printed out a message from an object implemented in Groovy, 
and bundle 3 printed out a message from a service implemented in Groovy, which it 
loaded as an OSGi service from the BundleContext. 

You may wish to uninstall the services using the "uninstall" command: 

  osgi> uninstall 3 2 1
  Groovy BundleActivator stopped
