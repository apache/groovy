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
package groovy.grape

import groovy.transform.CompileStatic
import org.apache.groovy.plugin.GroovyRunner
import org.apache.groovy.plugin.GroovyRunnerRegistry
import org.codehaus.groovy.reflection.CachedClass
import org.codehaus.groovy.reflection.ClassInfo
import org.codehaus.groovy.runtime.m12n.ExtensionModuleScanner
import org.codehaus.groovy.runtime.metaclass.MetaClassRegistryImpl

import java.util.jar.JarFile
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipFile

/**
 * Utility methods shared between GrapeIvy and GrapeMaven implementations.
 */
@CompileStatic
class GrapeUtil {

    private static final String METAINF_PREFIX = 'META-INF/services/'
    private static final String RUNNER_PROVIDER_CONFIG = GroovyRunner.name

    /**
     * Adds a URI to a classloader's classpath via reflection.
     */
    static void addURL(ClassLoader loader, URI uri) {
        // Dynamic invocation needed as addURL is not part of ClassLoader interface
        loader.metaClass.invokeMethod(loader, 'addURL', uri.toURL())
    }

    /**
     * Processes and registers category methods (extension modules) from a JAR file.
     *
     * @param loader the classloader to register methods with
     * @param file the JAR file to process
     */
    static void processCategoryMethods(ClassLoader loader, File file) {
        // register extension methods if jar
        if (file.getName().toLowerCase().endsWith('.jar')) {
            def mcRegistry = GroovySystem.metaClassRegistry
            if (mcRegistry instanceof MetaClassRegistryImpl) {
                try (JarFile jar = new JarFile(file)) {
                    ZipEntry entry = jar.getEntry(ExtensionModuleScanner.MODULE_META_INF_FILE)
                    if (!entry) {
                        entry = jar.getEntry(ExtensionModuleScanner.LEGACY_MODULE_META_INF_FILE)
                    }
                    if (entry) {
                        Properties props = new Properties()

                        try (InputStream is = jar.getInputStream(entry)) {
                            props.load(is)
                        }

                        Map<CachedClass, List<MetaMethod>> metaMethods = [:]
                        mcRegistry.registerExtensionModuleFromProperties(props, loader, metaMethods)
                        // add old methods to the map
                        metaMethods.each { CachedClass c, List<MetaMethod> methods ->
                            // GROOVY-5543: if a module was loaded using grab, there are chances that subclasses
                            // have their own ClassInfo, and we must change them as well!
                            Set<CachedClass> classesToBeUpdated = [c].toSet()
                            ClassInfo.onAllClassInfo { ClassInfo info ->
                                if (c.getTheClass().isAssignableFrom(info.getCachedClass().getTheClass())) {
                                    classesToBeUpdated << info.getCachedClass()
                                }
                            }
                            classesToBeUpdated*.addNewMopMethods(methods)
                        }
                    }
                } catch (ZipException e) {
                    throw new RuntimeException("Grape could not load jar '$file'", e)
                }
            }
        }
    }

    /**
     * Searches the given File for known service provider configuration files to process.
     *
     * @param loader used to locate service provider files
     * @param f ZipFile in which to search for services
     * @return a collection of service provider files that were found
     */
    static Collection<String> processMetaInfServices(ClassLoader loader, File f) {
        List<String> services = []
        try (ZipFile zf = new ZipFile(f)) {
            String providerConfig = 'org.codehaus.groovy.runtime.SerializedCategoryMethods'
            ZipEntry serializedCategoryMethods = zf.getEntry(METAINF_PREFIX + providerConfig)
            if (serializedCategoryMethods != null) {
                services.add(providerConfig)

                try (InputStream is = zf.getInputStream(serializedCategoryMethods)) {
                    processSerializedCategoryMethods(is)
                }
            }
            // TODO: remove in a future release (replaced by GroovyRunnerRegistry)
            providerConfig = 'org.codehaus.groovy.plugins.Runners'
            ZipEntry pluginRunners = zf.getEntry(METAINF_PREFIX + providerConfig)
            if (pluginRunners != null) {
                services.add(providerConfig)

                try (InputStream is = zf.getInputStream(pluginRunners)) {
                    processRunners(is, f.getName(), loader)
                }
            }
            // GroovyRunners are loaded per ClassLoader using a ServiceLoader so here
            // it only needs to be indicated that the service provider file was found
            if (zf.getEntry(METAINF_PREFIX + RUNNER_PROVIDER_CONFIG) != null) {
                services.add(RUNNER_PROVIDER_CONFIG)
            }
        } catch (ZipException ignore) {
            // ignore files we can't process, e.g. non-jar/zip artifacts
            // TODO: log a warning
        }
        services
    }

    /**
     * Processes serialized category methods from an input stream.
     *
     * @param is the input stream containing category method definitions
     */
    static void processSerializedCategoryMethods(InputStream is) {
        is.getText().readLines().each {
            System.err.println(it.trim()) // TODO: implement this or delete it
        }
    }

    /**
     * Processes and registers Groovy runner implementations from a service provider file.
     *
     * @param is the input stream containing runner class names
     * @param name the name to register the runners under
     * @param loader the classloader to load runner classes from
     */
    static void processRunners(InputStream is, String name, ClassLoader loader) {
        GroovyRunnerRegistry registry = GroovyRunnerRegistry.instance
        is.getText().readLines()*.trim().each { String line ->
            if (!line.isEmpty() && line[0] != '#') {
                try {
                    registry[name] = (GroovyRunner) loader.loadClass(line).getDeclaredConstructor().newInstance()
                } catch (Exception e) {
                    throw new IllegalStateException("Error registering runner class '$line'", e)
                }
            }
        }
    }

    static boolean checkForRunner(Collection<String> services) {
        services.contains(RUNNER_PROVIDER_CONFIG)
    }

    static void registryLoad(ClassLoader classLoader) {
        GroovyRunnerRegistry.instance.load(classLoader)
    }
}
