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
import org.apache.ivy.Ivy
import org.apache.ivy.core.IvyContext
import org.apache.ivy.core.cache.ResolutionCacheManager
import org.apache.ivy.core.event.IvyListener
import org.apache.ivy.core.event.download.PrepareDownloadEvent
import org.apache.ivy.core.event.resolve.StartResolveEvent
import org.apache.ivy.core.module.descriptor.Configuration
import org.apache.ivy.core.module.descriptor.DefaultDependencyArtifactDescriptor
import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor
import org.apache.ivy.core.module.descriptor.DefaultExcludeRule
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor
import org.apache.ivy.core.module.id.ArtifactId
import org.apache.ivy.core.module.id.ModuleId
import org.apache.ivy.core.module.id.ModuleRevisionId
import org.apache.ivy.core.report.ArtifactDownloadReport
import org.apache.ivy.core.report.ResolveReport
import org.apache.ivy.core.resolve.ResolveOptions
import org.apache.ivy.core.settings.IvySettings
import org.apache.ivy.plugins.matcher.ExactPatternMatcher
import org.apache.ivy.plugins.matcher.PatternMatcher
import org.apache.ivy.plugins.resolver.ChainResolver
import org.apache.ivy.plugins.resolver.IBiblioResolver
import org.apache.ivy.plugins.resolver.ResolverSettings
import org.apache.ivy.util.DefaultMessageLogger
import org.apache.ivy.util.Message
import org.codehaus.groovy.reflection.CachedClass
import org.codehaus.groovy.reflection.ClassInfo
import org.codehaus.groovy.reflection.ReflectionUtils
import org.codehaus.groovy.runtime.m12n.ExtensionModuleScanner
import org.codehaus.groovy.runtime.metaclass.MetaClassRegistryImpl

import javax.xml.parsers.DocumentBuilderFactory
import java.util.jar.JarFile
import java.util.regex.Pattern
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipFile

/**
 * Implementation supporting {@code @Grape} and {@code @Grab} annotations based on Ivy.
 */
class GrapeIvy implements GrapeEngine {

    private static final String METAINF_PREFIX = 'META-INF/services/'
    private static final String RUNNER_PROVIDER_CONFIG = GroovyRunner.name
    private static final List<String> DEF_CONFIG = ['default']

    private final exclusiveGrabArgs = [
            ['group', 'groupId', 'organisation', 'organization', 'org'],
            ['module', 'artifactId', 'artifact'],
            ['version', 'revision', 'rev'],
            ['conf', 'scope', 'configuration'],
    ].inject([:], { m, g -> g.each { a -> m[a] = (g - a) as Set }; m })

    boolean enableGrapes
    Ivy ivyInstance
    Set<String> resolvedDependencies
    Set<String> downloadedArtifacts
    // weak hash map so we don't leak loaders directly
    Map<ClassLoader, Set<IvyGrabRecord>> loadedDeps = new WeakHashMap<ClassLoader, Set<IvyGrabRecord>>()
    // set that stores the IvyGrabRecord(s) for all the dependencies in each grab() call
    Set<IvyGrabRecord> grabRecordsForCurrDependencies = new LinkedHashSet<IvyGrabRecord>()
    // we keep the settings so that addResolver can add to the resolver chain
    IvySettings settings

    @SuppressWarnings('Instanceof')
    GrapeIvy() {
        // if we are already initialized, quit
        if (enableGrapes) return

        // start ivy
        Message.defaultLogger = new DefaultMessageLogger(System.getProperty('ivy.message.logger.level', '-1') as int)
        settings = new IvySettings()

        // configure settings
        def grapeConfig = localGrapeConfig
        if (!grapeConfig.exists()) {
            grapeConfig = GrapeIvy.getResource('defaultGrapeConfig.xml')
        }
        try {
            settings.load(grapeConfig) // exploit multi-methods for convenience
        } catch (java.text.ParseException ex) {
            def configLocation = grapeConfig instanceof File ? grapeConfig.canonicalPath : grapeConfig.toString()
            System.err.println "Local Ivy config file '$configLocation' appears corrupt - ignoring it and using default config instead\nError was: " + ex.message
            grapeConfig = GrapeIvy.getResource('defaultGrapeConfig.xml')
            settings.load(grapeConfig)
        }

        // set up the cache dirs
        settings.defaultCache = grapeCacheDir

        settings.setVariable('ivy.default.configuration.m2compatible', 'true')
        ivyInstance = Ivy.newInstance(settings)
        IvyContext.context.ivy = ivyInstance
        resolvedDependencies = []
        downloadedArtifacts = []

        enableGrapes = true
    }

    @CompileStatic
    File getGroovyRoot() {
        String root = System.getProperty('groovy.root')
        def groovyRoot
        if (root == null) {
            groovyRoot = new File(System.getProperty('user.home'), '.groovy')
        } else {
            groovyRoot = new File(root)
        }
        try {
            groovyRoot = groovyRoot.canonicalFile
        } catch (IOException ignore) {
            // skip canonicalization then, it may not exist yet
        }
        groovyRoot
    }

    @CompileStatic
    File getLocalGrapeConfig() {
        String grapeConfig = System.getProperty('grape.config')
        if (grapeConfig) {
            return new File(grapeConfig)
        }
        new File(grapeDir, 'grapeConfig.xml')
    }

    @CompileStatic
    File getGrapeDir() {
        String root = System.getProperty('grape.root')
        if (root == null) {
            return groovyRoot
        }
        File grapeRoot = new File(root)
        try {
            grapeRoot = grapeRoot.canonicalFile
        } catch (IOException ignore) {
            // skip canonicalization then, it may not exist yet
        }
        grapeRoot
    }

    @CompileStatic
    File getGrapeCacheDir() {
        File cache = new File(grapeDir, 'grapes')
        if (!cache.exists()) {
            cache.mkdirs()
        } else if (!cache.isDirectory()) {
            throw new RuntimeException("The grape cache dir $cache is not a directory")
        }
        cache
    }

    @CompileStatic
    ClassLoader chooseClassLoader(Map args) {
        ClassLoader loader = (ClassLoader) args.classLoader
        if (!isValidTargetClassLoader(loader)) {
            loader = ((Class) ((args.refObject?.class
                    ?: ReflectionUtils.getCallingClass((int) (args.calleeDepth ?: 1))
            )))?.classLoader
            while (loader && !isValidTargetClassLoader(loader)) {
                loader = loader.parent
            }
            //if (!isValidTargetClassLoader(loader)) {
            //    loader = Thread.currentThread().contextClassLoader
            //}
            //if (!isValidTargetClassLoader(loader)) {
            //    loader = GrapeIvy.class.classLoader
            //}
            if (!isValidTargetClassLoader(loader)) {
                throw new RuntimeException('No suitable ClassLoader found for grab')
            }
        }
        loader
    }

    @CompileStatic
    private boolean isValidTargetClassLoader(loader) {
        isValidTargetClassLoaderClass(loader?.class)
    }

    @CompileStatic
    private boolean isValidTargetClassLoaderClass(Class loaderClass) {
        loaderClass != null &&
                (
                        (loaderClass.name == 'groovy.lang.GroovyClassLoader') ||
                                (loaderClass.name == 'org.codehaus.groovy.tools.RootLoader') ||
                                isValidTargetClassLoaderClass(loaderClass.superclass)
                )
    }

    @SuppressWarnings('Instanceof')
    IvyGrabRecord createGrabRecord(Map deps) {
        // parse the actual dependency arguments
        String module = deps.module ?: deps.artifactId ?: deps.artifact
        if (!module) {
            throw new RuntimeException('grab requires at least a module: or artifactId: or artifact: argument')
        }

        String groupId = deps.group ?: deps.groupId ?: deps.organisation ?: deps.organization ?: deps.org ?: ''
        String ext = deps.ext ?: deps.type ?: ''
        String type = deps.type ?: ''

        //TODO accept ranges and decode them?  except '1.0.0'..<'2.0.0' won't work in groovy
        String version = deps.version ?: deps.revision ?: deps.rev ?: '*'
        if ('*' == version) version = 'latest.default'

        ModuleRevisionId mrid = ModuleRevisionId.newInstance(groupId, module, version)

        boolean force = deps.containsKey('force') ? deps.force : true
        boolean changing = deps.containsKey('changing') ? deps.changing : false
        boolean transitive = deps.containsKey('transitive') ? deps.transitive : true
        def conf = deps.conf ?: deps.scope ?: deps.configuration ?: DEF_CONFIG
        if (conf instanceof String) {
            if (conf.startsWith('[') && conf.endsWith(']')) conf = conf[1..-2]
            conf = conf.split(',').toList()
        }
        def classifier = deps.classifier ?: null

        new IvyGrabRecord(mrid: mrid, conf: conf, changing: changing, transitive: transitive, force: force, classifier: classifier, ext: ext, type: type)
    }

    @Override
    @CompileStatic
    grab(String endorsedModule) {
        grab(group: 'groovy.endorsed', module: endorsedModule, version: GroovySystem.version)
    }

    @Override
    @CompileStatic
    grab(Map args) {
        args.calleeDepth = args.calleeDepth ?: DEFAULT_CALLEE_DEPTH + 1
        grab(args, args)
    }

    @Override
    @CompileStatic
    grab(Map args, Map... dependencies) {
        ClassLoader loader = null
        grabRecordsForCurrDependencies.clear()

        try {
            // identify the target classloader early, so we fail before checking repositories
            loader = chooseClassLoader(
                    classLoader: args.remove('classLoader'),
                    refObject: args.remove('refObject'),
                    calleeDepth: args.calleeDepth ?: DEFAULT_CALLEE_DEPTH,
            )

            // check for non-fail null.
            // If we were in fail mode we would have already thrown an exception
            if (!loader) return

            def uris = resolve(loader, args, dependencies)
            for (URI uri in uris) {
                addURL(loader, uri)
            }
            boolean runnerServicesFound = false
            for (URI uri in uris) {
                //TODO check artifact type, jar vs library, etc
                File file = new File(uri)
                processCategoryMethods(loader, file)
                Collection<String> services = processMetaInfServices(loader, file)
                if (!runnerServicesFound) {
                    runnerServicesFound = services.contains(RUNNER_PROVIDER_CONFIG)
                }
            }
            if (runnerServicesFound) {
                GroovyRunnerRegistry.instance.load(loader)
            }
        } catch (Exception e) {
            // clean-up the state first
            Set<IvyGrabRecord> grabRecordsForCurrLoader = getLoadedDepsForLoader(loader)
            grabRecordsForCurrLoader.removeAll(grabRecordsForCurrDependencies)
            grabRecordsForCurrDependencies.clear()

            if (args.noExceptions) {
                return e
            }
            throw e
        }
        null
    }

    private void addURL(ClassLoader loader, URI uri) {
        loader.addURL(uri.toURL())
    }

    @SuppressWarnings('Instanceof')
    @CompileStatic
    private processCategoryMethods(ClassLoader loader, File file) {
        // register extension methods if jar
        if (file.name.toLowerCase().endsWith('.jar')) {
            def mcRegistry = GroovySystem.metaClassRegistry
            if (mcRegistry instanceof MetaClassRegistryImpl) {
                try (JarFile jar = new JarFile(file)) {
                    def entry = jar.getEntry(ExtensionModuleScanner.MODULE_META_INF_FILE)
                    if (!entry) {
                        entry = jar.getEntry(ExtensionModuleScanner.LEGACY_MODULE_META_INF_FILE)
                    }
                    if (entry) {
                        Properties props = new Properties()

                        try (InputStream is = jar.getInputStream(entry)) {
                            props.load(is)
                        }

                        Map<CachedClass, List<MetaMethod>> metaMethods = new HashMap<CachedClass, List<MetaMethod>>()
                        mcRegistry.registerExtensionModuleFromProperties(props, loader, metaMethods)
                        // add old methods to the map
                        metaMethods.each { CachedClass c, List<MetaMethod> methods ->
                            // GROOVY-5543: if a module was loaded using grab, there are chances that subclasses
                            // have their own ClassInfo, and we must change them as well!
                            Set<CachedClass> classesToBeUpdated = [c].toSet()
                            ClassInfo.onAllClassInfo { ClassInfo info ->
                                if (c.theClass.isAssignableFrom(info.cachedClass.theClass)) {
                                    classesToBeUpdated << info.cachedClass
                                }
                            }
                            classesToBeUpdated*.addNewMopMethods(methods)
                        }
                    }
                } catch (ZipException zipException) {
                    throw new RuntimeException("Grape could not load jar '$file'", zipException)
                }
            }
        }
    }

    @CompileStatic
    void processOtherServices(ClassLoader loader, File f) {
        processMetaInfServices(loader, f) // ignore result
    }

    /**
     * Searches the given File for known service provider
     * configuration files to process.
     *
     * @param loader used to locate service provider files
     * @param f ZipFile in which to search for services
     * @return a collection of service provider files that were found
     */
    @CompileStatic
    private Collection<String> processMetaInfServices(ClassLoader loader, File f) {
        List<String> services = new ArrayList<>()
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
                    processRunners(is, f.name, loader)
                }
            }
            // GroovyRunners are loaded per ClassLoader using a ServiceLoader so here
            // it only needs to be indicated that the service provider file was found
            if (zf.getEntry(METAINF_PREFIX + RUNNER_PROVIDER_CONFIG) != null) {
                services.add(RUNNER_PROVIDER_CONFIG)
            }
        } catch (ZipException ignore) {
            // ignore files we can't process, e.g. non-jar/zip artifacts
            // TODO log a warning
        }
        services
    }

    @CompileStatic
    void processSerializedCategoryMethods(InputStream is) {
        is.text.readLines().each {
            System.err.println it.trim() // TODO implement this or delete it
        }
    }

    @CompileStatic
    void processRunners(InputStream is, String name, ClassLoader loader) {
        GroovyRunnerRegistry registry = GroovyRunnerRegistry.instance
        is.text.readLines()*.trim().findAll { String line -> !line.isEmpty() && line[0] != '#' }.each {
            String line = (String) it
            try {
                registry[name] = (GroovyRunner) loader.loadClass(line).newInstance()
            } catch (Exception ex) {
                throw new IllegalStateException("Error registering runner class '$it'", ex)
            }
        }
    }

    ResolveReport getDependencies(Map args, IvyGrabRecord... grabRecords) {
        ResolutionCacheManager cacheManager = ivyInstance.resolutionCacheManager

        def millis = System.currentTimeMillis()
        def md = new DefaultModuleDescriptor(ModuleRevisionId
                .newInstance('caller', 'all-caller', 'working' + millis.toString()[-2..-1]), 'integration', null, true)
        md.addConfiguration(new Configuration('default'))
        md.lastModified = millis

        addExcludesIfNeeded(args, md)

        for (IvyGrabRecord grabRecord : grabRecords) {
            def conf = grabRecord.conf ?: ['*']
            DefaultDependencyDescriptor dd = (DefaultDependencyDescriptor) md.dependencies.find {
                it.dependencyRevisionId.equals(grabRecord.mrid)
            }
            if (dd) {
                addDependencyArtifactDescriptor(dd, grabRecord, conf)
            } else {
                dd = new DefaultDependencyDescriptor(md, grabRecord.mrid, grabRecord.force,
                        grabRecord.changing, grabRecord.transitive)
                conf.each { dd.addDependencyConfiguration('default', it) }
                addDependencyArtifactDescriptor(dd, grabRecord, conf)
                md.addDependency(dd)
            }
        }

        // resolve grab and dependencies
        ResolveOptions resolveOptions = new ResolveOptions()
                .setConfs(GrapeIvy.DEF_CONFIG as String[])
                .setOutputReport(false)
                .setValidate((boolean) (args.containsKey('validate') ? args.validate : false))

        ivyInstance.settings.defaultResolver = args.autoDownload ? 'downloadGrapes' : 'cachedGrapes'
        if (args.disableChecksums) {
            ivyInstance.settings.setVariable('ivy.checksums', '')
        }
        boolean reportDownloads = System.getProperty('groovy.grape.report.downloads', 'false') == 'true'
        if (reportDownloads) {
            addIvyListener()
        }

        ResolveReport report = null
        int attempt = 8 // max of 8 times
        while (true) {
            try {
                report = ivyInstance.resolve(md, resolveOptions)
                break
            } catch (IOException ioe) {
                if (attempt--) {
                    if (reportDownloads)
                        System.err.println 'Grab Error: retrying...'
                    sleep attempt > 4 ? 350 : 1000
                    continue
                }
                throw new RuntimeException("Error grabbing grapes -- $ioe.message")
            }
        }

        if (report.hasError()) {
            throw new RuntimeException("Error grabbing Grapes -- $report.allProblemMessages")
        }
        if (report.downloadSize && reportDownloads) {
            System.err.println "Downloaded ${report.downloadSize >> 10} Kbytes in ${report.downloadTime}ms:\n  ${report.allArtifactsReports*.toString().join('\n  ')}"
        }
        md = report.moduleDescriptor

        if (!args.preserveFiles) {
            cacheManager.getResolvedIvyFileInCache(md.moduleRevisionId).delete()
            cacheManager.getResolvedIvyPropertiesInCache(md.moduleRevisionId).delete()
        }

        report
    }

    private addIvyListener() {
        ivyInstance.eventManager.addIvyListener([progress: { ivyEvent ->
            switch (ivyEvent) {
                case StartResolveEvent:
                    ivyEvent.moduleDescriptor.dependencies.each { it ->
                        def name = it.toString()
                        if (!resolvedDependencies.contains(name)) {
                            resolvedDependencies << name
                            System.err.println "Resolving $name"
                        }
                    }
                    break
                case PrepareDownloadEvent:
                    ivyEvent.artifacts.each { it ->
                        def name = it.toString()
                        if (!downloadedArtifacts.contains(name)) {
                            downloadedArtifacts << name
                            System.err.println "Preparing to download artifact $name"
                        }
                    }
                    break
            }
        }] as IvyListener)
    }

    @CompileStatic
    private void addDependencyArtifactDescriptor(DefaultDependencyDescriptor dd, IvyGrabRecord grabRecord, List<String> conf) {
        // TODO: find out "unknown" reason and change comment below - also, confirm conf[0] check vs conf.contains('optional')
        if (conf[0] != 'optional' || grabRecord.classifier) {
            // for some unknown reason optional dependencies should not have an artifactDescriptor
            def dad = new DefaultDependencyArtifactDescriptor(dd,
                    grabRecord.mrid.name, grabRecord.type ?: 'jar', grabRecord.ext ?: 'jar', null, grabRecord.classifier ? [classifier: grabRecord.classifier] : null)
            conf.each { dad.addConfiguration(it) }
            dd.addDependencyArtifact('default', dad)
        }
    }

    void uninstallArtifact(String group, String module, String rev) {
        // TODO consider transitive uninstall as an option
        Pattern ivyFilePattern = ~/ivy-(.*)\.xml/ //TODO get pattern from ivy conf
        grapeCacheDir.eachDir { File groupDir ->
            if (groupDir.name == group) groupDir.eachDir { File moduleDir ->
                if (moduleDir.name == module) moduleDir.eachFileMatch(ivyFilePattern) { File ivyFile ->
                    def m = ivyFilePattern.matcher(ivyFile.name)
                    if (m.matches() && m.group(1) == rev) {
                        // TODO handle other types? e.g. 'dlls'
                        def jardir = new File(moduleDir, 'jars')
                        if (!jardir.exists()) return
                        def dbf = DocumentBuilderFactory.newInstance()
                        def db = dbf.newDocumentBuilder()
                        def root = db.parse(ivyFile).documentElement
                        def publis = root.getElementsByTagName('publications')
                        for (int i = 0; i < publis.length; i++) {
                            def artifacts = publis.item(i).getElementsByTagName('artifact')
                            processArtifacts(artifacts, rev, jardir)
                        }
                        ivyFile.delete()
                    }
                }
            }
        }
    }

    private void processArtifacts(artifacts, String rev, File jardir) {
        for (int j = 0; j < artifacts.length; j++) {
            def artifact = artifacts.item(j)
            def attrs = artifact.attributes
            def name = attrs.getNamedItem('name').textContent + "-$rev"
            def classifier = attrs.getNamedItemNS('m', 'classifier')?.textContent
            if (classifier) name += "-$classifier"
            name += ".${attrs.getNamedItem('ext').textContent}"
            def jarfile = new File(jardir, name)
            if (jarfile.exists()) {
                System.err.println "Deleting ${jarfile.name}"
                jarfile.delete()
            }
        }
    }

    private addExcludesIfNeeded(Map args, DefaultModuleDescriptor md) {
        if (!args.containsKey('excludes')) return
        args.excludes.each { map ->
            def excludeRule = new DefaultExcludeRule(new ArtifactId(
                    new ModuleId(map.group, map.module), PatternMatcher.ANY_EXPRESSION,
                    PatternMatcher.ANY_EXPRESSION,
                    PatternMatcher.ANY_EXPRESSION),
                    ExactPatternMatcher.INSTANCE, null)
            excludeRule.addConfiguration('default')
            md.addExcludeRule(excludeRule)
        }
    }

    @Override
    @CompileStatic
    Map<String, Map<String, List<String>>> enumerateGrapes() {
        Map<String, Map<String, List<String>>> bunches = [:]
        Pattern ivyFilePattern = ~/ivy-(.*)\.xml/ //TODO get pattern from ivy conf
        grapeCacheDir.eachDir { File groupDir ->
            Map<String, List<String>> grapes = [:]
            bunches[groupDir.name] = grapes
            groupDir.eachDir { File moduleDir ->
                List<String> versions = []
                moduleDir.eachFileMatch(ivyFilePattern) { File ivyFile ->
                    def m = ivyFilePattern.matcher(ivyFile.name)
                    if (m.matches()) versions += m.group(1)
                }
                grapes[moduleDir.name] = versions
            }
        }
        bunches
    }

    @Override
    @CompileStatic
    URI[] resolve(Map args, Map... dependencies) {
        resolve(args, null, dependencies)
    }

    @Override
    @CompileStatic
    URI[] resolve(Map args, List depsInfo, Map... dependencies) {
        // identify the target classloader early, so we fail before checking repositories
        ClassLoader loader = chooseClassLoader(
                classLoader: args.remove('classLoader'),
                refObject: args.remove('refObject'),
                calleeDepth: args.calleeDepth ?: DEFAULT_CALLEE_DEPTH,
        )

        // check for non-fail null.
        // If we were in fail mode we would have already thrown an exception
        if (!loader) {
            return [] as URI[]
        }

        resolve(loader, args, depsInfo, dependencies)
    }

    @CompileStatic
    URI[] resolve(ClassLoader loader, Map args, Map... dependencies) {
        resolve(loader, args, null, dependencies)
    }

    URI[] resolve(ClassLoader loader, Map args, List depsInfo, Map... dependencies) {
        // check for mutually exclusive arguments
        Set keys = args.keySet()
        keys.each { a ->
            Set badArgs = exclusiveGrabArgs[a]
            if (badArgs && !badArgs.disjoint(keys)) {
                throw new RuntimeException("Mutually exclusive arguments passed into grab: ${keys.intersect(badArgs) + a}")
            }
        }

        // check the kill switch
        if (!enableGrapes) {
            return [] as URI[]
        }

        boolean populateDepsInfo = (depsInfo != null)

        Set<IvyGrabRecord> localDeps = getLoadedDepsForLoader(loader)

        dependencies.each {
            IvyGrabRecord igr = createGrabRecord(it)
            grabRecordsForCurrDependencies.add(igr)
            localDeps.add(igr)
        }
        // the call to reverse ensures that the newest additions are in
        // front causing existing dependencies to come last and thus
        // claiming higher priority.  Thus when module versions clash we
        // err on the side of using the class already loaded into the
        // classloader rather than adding another jar of the same module
        // with a different version
        ResolveReport report = null
        try {
            report = getDependencies(args, *localDeps.asList().reverse())
        } catch (Exception e) {
            // clean-up the state first
            localDeps.removeAll(grabRecordsForCurrDependencies)
            grabRecordsForCurrDependencies.clear()
            throw e
        }

        List<URI> results = []
        for (ArtifactDownloadReport adl in report.allArtifactsReports) {
            //TODO check artifact type, jar vs library, etc
            if (adl.localFile) {
                results += adl.localFile.toURI()
            }
        }

        if (populateDepsInfo) {
            def deps = report.dependencies
            deps.each { depNode ->
                def id = depNode.id
                depsInfo << ['group': id.organisation, 'module': id.name, 'revision': id.revision]
            }
        }

        results as URI[]
    }

    @CompileStatic
    private Set<IvyGrabRecord> getLoadedDepsForLoader(ClassLoader loader) {
        Set<IvyGrabRecord> localDeps = loadedDeps.get(loader)
        if (localDeps == null) {
            // use a linked set to preserve initial insertion order
            localDeps = new LinkedHashSet<IvyGrabRecord>()
            loadedDeps.put(loader, localDeps)
        }
        localDeps
    }

    @Override
    Map[] listDependencies(ClassLoader classLoader) {
        if (loadedDeps.containsKey(classLoader)) {
            List<Map> results = []
            loadedDeps[classLoader].each { IvyGrabRecord grabbed ->
                def dep = [
                        group  : grabbed.mrid.organisation,
                        module : grabbed.mrid.name,
                        version: grabbed.mrid.revision
                ]
                if (grabbed.conf != GrapeIvy.DEF_CONFIG) {
                    dep.conf = grabbed.conf
                }
                if (grabbed.changing) {
                    dep.changing = grabbed.changing
                }
                if (!grabbed.transitive) {
                    dep.transitive = grabbed.transitive
                }
                if (!grabbed.force) {
                    dep.force = grabbed.force
                }
                if (grabbed.classifier) {
                    dep.classifier = grabbed.classifier
                }
                if (grabbed.ext) {
                    dep.ext = grabbed.ext
                }
                if (grabbed.type) {
                    dep.type = grabbed.type
                }
                results << dep
            }
            return results
        }
        null
    }

    @Override
    @CompileStatic
    void addResolver(Map<String, Object> args) {
        ChainResolver chainResolver = (ChainResolver) settings.getResolver('downloadGrapes')

        IBiblioResolver resolver = new IBiblioResolver(
                name: (String) args.name,
                root: (String) args.root,
                m2compatible: (boolean) (args.m2Compatible ?: true),
                settings: (ResolverSettings) settings)

        chainResolver.add(resolver)

        ivyInstance = Ivy.newInstance(settings)
        resolvedDependencies = []
        downloadedArtifacts = []
    }
}

@CompileStatic
class IvyGrabRecord {
    ModuleRevisionId mrid
    List<String> conf
    boolean changing
    boolean transitive
    boolean force
    String classifier
    String ext
    String type

    @Override
    int hashCode() {
        (mrid.hashCode() ^ conf.hashCode()
                ^ (changing ? 0xaaaaaaaa : 0x55555555)
                ^ (transitive ? 0xbbbbbbbb : 0x66666666)
                ^ (force ? 0xcccccccc : 0x77777777)
                ^ (classifier ? classifier.hashCode() : 0)
                ^ (ext ? ext.hashCode() : 0)
                ^ (type ? type.hashCode() : 0))
    }

    @Override
    boolean equals(Object obj) {
        if (null == obj || obj.class != IvyGrabRecord) {
            return false
        }

        IvyGrabRecord o = (IvyGrabRecord) obj

        ((changing == o.changing)
                && (transitive == o.transitive)
                && (force == o.force)
                && (mrid == o.mrid)
                && (conf == o.conf)
                && (classifier == o.classifier)
                && (ext == o.ext)
                && (type == o.type))
    }
}
