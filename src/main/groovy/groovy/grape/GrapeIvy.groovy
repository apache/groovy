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

import groovy.transform.*
import org.apache.groovy.plugin.GroovyRunner
import org.apache.groovy.plugin.GroovyRunnerRegistry
import org.apache.ivy.Ivy
import org.apache.ivy.core.IvyContext
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

import java.util.jar.JarFile
import java.util.regex.Pattern
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipFile

/**
 * Implementation supporting {@code @Grape} and {@code @Grab} annotations based on Ivy.
 */
@AutoFinal @CompileStatic
class GrapeIvy implements GrapeEngine {

    private static final String METAINF_PREFIX = 'META-INF/services/'
    private static final String RUNNER_PROVIDER_CONFIG = GroovyRunner.getName()
    private static final List<String> DEFAULT_CONF = Collections.singletonList('default')
    private static final Map<String, Set<String>> MUTUALLY_EXCLUSIVE_KEYS = processGrabArgs([
            ['group', 'groupId', 'organisation', 'organization', 'org'],
            ['module', 'artifactId', 'artifact'],
            ['version', 'revision', 'rev'],
            ['conf', 'scope', 'configuration'],
    ])

    @CompileDynamic // maps a->[b,c], b->[a,c] and c->[a,b] given [a,b,c]
    private static Map<String, Set<String>> processGrabArgs(List<List<String>> grabArgs) {
        grabArgs.inject([:]) { Map m, List g -> g.each { a -> m[a] = (g - a) as Set }; m }
    }

    boolean enableGrapes = true

    Ivy ivyInstance
    IvySettings settings
    Set<String> downloadedArtifacts = []
    Set<String> resolvedDependencies = []
    // weak hash map so we don't leak loaders directly
    final Map<ClassLoader, Set<IvyGrabRecord>> loadedDeps = [] as WeakHashMap
    /** Stores the IvyGrabRecord(s) for all dependencies in each grab() call. */
    final Set<IvyGrabRecord> grabRecordsForCurrDependencies = [] as LinkedHashSet

    GrapeIvy() {
        Message.setDefaultLogger(new DefaultMessageLogger(System.getProperty('ivy.message.logger.level', '-1') as int))

        settings = new IvySettings()
        settings.setVariable('user.home.url', new File(System.getProperty('user.home')).toURI().toURL() as String)
        File grapeConfig = getLocalGrapeConfig()
        if (grapeConfig.exists()) {
            try {
                settings.load(grapeConfig)
            } catch (java.text.ParseException e) {
                System.err.println("Local Ivy config file '${grapeConfig.getCanonicalPath()}' appears corrupt - ignoring it and using default config instead\nError was: ${e.getMessage()}")
                settings.load(GrapeIvy.getResource('defaultGrapeConfig.xml'))
            }
        } else {
            settings.load(GrapeIvy.getResource('defaultGrapeConfig.xml'))
        }
        settings.setDefaultCache(getGrapeCacheDir())
        settings.setVariable('ivy.default.configuration.m2compatible', 'true')

        ivyInstance = Ivy.newInstance(settings)
        IvyContext.getContext().setIvy(ivyInstance)
    }

    File getGroovyRoot() {
        String root = System.getProperty('groovy.root')
        def groovyRoot
        if (root == null) {
            groovyRoot = new File(System.getProperty('user.home'), '.groovy')
        } else {
            groovyRoot = new File(root)
        }
        try {
            groovyRoot = groovyRoot.getCanonicalFile()
        } catch (IOException ignore) {
            // skip canonicalization then, it may not exist yet
        }
        groovyRoot
    }

    File getGrapeDir() {
        String root = System.getProperty('grape.root')
        if (root == null) {
            return getGroovyRoot()
        }
        File grapeRoot = new File(root)
        try {
            grapeRoot = grapeRoot.getCanonicalFile()
        } catch (IOException ignore) {
            // skip canonicalization then, it may not exist yet
        }
        grapeRoot
    }

    File getGrapeCacheDir() {
        File cache = new File(grapeDir, 'grapes')
        if (!cache.exists()) {
            cache.mkdirs()
        } else if (!cache.isDirectory()) {
            throw new RuntimeException("The grape cache dir $cache is not a directory")
        }
        cache
    }

    File getLocalGrapeConfig() {
        String grapeConfig = System.getProperty('grape.config')
        if (grapeConfig) {
            new File(grapeConfig)
        } else {
            new File(getGrapeDir(), 'grapeConfig.xml')
        }
    }

    ClassLoader chooseClassLoader(Map args) {
        ClassLoader loader = (ClassLoader) args.classLoader
        if (!isValidTargetClassLoader(loader)) {
            Class caller = args.refObject?.getClass() ?:
                    ReflectionUtils.getCallingClass((int) args.calleeDepth ?: 1)
            loader = caller?.getClassLoader()
            while (loader && !isValidTargetClassLoader(loader)) {
                loader = loader.getParent()
            }
            /*if (!isValidTargetClassLoader(loader)) {
                loader = Thread.currentThread().getContextClassLoader()
            }
            if (!isValidTargetClassLoader(loader)) {
                loader = GrapeIvy.getClass().getClassLoader()
            }*/
            if (!isValidTargetClassLoader(loader)) {
                throw new RuntimeException('No suitable ClassLoader found for grab')
            }
        }
        loader
    }

    private boolean isValidTargetClassLoader(ClassLoader loader) {
        isValidTargetClassLoaderClass(loader?.getClass())
    }

    private boolean isValidTargetClassLoaderClass(Class loaderClass) {
        loaderClass != null && (loaderClass.getName() == 'groovy.lang.GroovyClassLoader'
                || loaderClass.getName() == 'org.codehaus.groovy.tools.RootLoader'
                || isValidTargetClassLoaderClass(loaderClass.getSuperclass()))
    }

    IvyGrabRecord createGrabRecord(Map dep) {
        String module = dep.module ?: dep.artifactId ?: dep.artifact
        if (!module) {
            throw new RuntimeException('grab requires at least a module: or artifactId: or artifact: argument')
        }

        String groupId = dep.group ?: dep.groupId ?: dep.organisation ?: dep.organization ?: dep.org ?: ''
        // TODO: accept ranges and decode them?  except '1.0.0'..<'2.0.0' won't work in groovy
        String version = dep.version ?: dep.revision ?: dep.rev ?: '*'
        if (version == '*') version = 'latest.default'
        String classifier = dep.classifier ?: null
        String ext = dep.ext ?: dep.type ?: ''
        String type = dep.type ?: ''

        ModuleRevisionId mrid = ModuleRevisionId.newInstance(groupId, module, version)

        boolean force = dep.containsKey('force') ? dep.force : true
        boolean changing = dep.containsKey('changing') ? dep.changing : false
        boolean transitive = dep.containsKey('transitive') ? dep.transitive : true

        new IvyGrabRecord(mrid: mrid, conf: getConfList(dep), force: force, changing: changing, transitive: transitive, ext: ext, type: type, classifier: classifier)
    }

    @CompileDynamic
    private List<String> getConfList(Map dep) {
        def conf = dep.conf ?: dep.scope ?: dep.configuration ?: DEFAULT_CONF
        if (conf instanceof String) {
            if (conf.startsWith('[') && conf.endsWith(']')) conf = conf[1..-2]
            conf = conf.split(',').toList()
        }
        conf
    }

    @Override
    grab(String endorsedModule) {
        grab(group: 'groovy.endorsed', module: endorsedModule, version: GroovySystem.getVersion())
    }

    @Override
    grab(Map args) {
        args.calleeDepth = args.calleeDepth ?: DEFAULT_CALLEE_DEPTH + 1
        grab(args, args)
    }

    @Override
    grab(Map args, Map... dependencies) {
        ClassLoader loader = null
        grabRecordsForCurrDependencies.clear()

        try {
            // identify the target classloader early, so we fail before checking repositories
            loader = chooseClassLoader(
                    refObject: args.remove('refObject'),
                    classLoader: args.remove('classLoader'),
                    calleeDepth: args.calleeDepth ?: DEFAULT_CALLEE_DEPTH,
            )

            // check for non-fail null
            // if we were in fail mode we would have already thrown an exception
            if (!loader) return

            URI[] uris = resolve(loader, args, dependencies)
            for (URI uri : uris) {
                addURL(loader, uri)
            }
            boolean runnerServicesFound = false
            for (URI uri : uris) {
                // TODO: check artifact type, jar vs library, etc.
                File file = new File(uri)
                processCategoryMethods(loader, file)
                Collection<String> services = processMetaInfServices(loader, file)
                if (!runnerServicesFound) {
                    runnerServicesFound = services.contains(RUNNER_PROVIDER_CONFIG)
                }
            }
            if (runnerServicesFound) {
                GroovyRunnerRegistry.getInstance().load(loader)
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

    @CompileDynamic
    private void addURL(ClassLoader loader, URI uri) {
        loader.addURL(uri.toURL())
    }

    private processCategoryMethods(ClassLoader loader, File file) {
        // register extension methods if jar
        if (file.getName().toLowerCase().endsWith('.jar')) {
            def mcRegistry = GroovySystem.getMetaClassRegistry()
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
    private Collection<String> processMetaInfServices(ClassLoader loader, File f) {
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

    void processSerializedCategoryMethods(InputStream is) {
        is.getText().readLines().each {
            System.err.println(it.trim()) // TODO: implement this or delete it
        }
    }

    void processRunners(InputStream is, String name, ClassLoader loader) {
        GroovyRunnerRegistry registry = GroovyRunnerRegistry.getInstance()
        is.getText().readLines()*.trim().each { String line ->
            if (!line.isEmpty() && line[0] != '#')
            try {
                registry[name] = (GroovyRunner) loader.loadClass(line).newInstance()
            } catch (Exception e) {
                throw new IllegalStateException("Error registering runner class '$line'", e)
            }
        }
    }

    ResolveReport getDependencies(Map args, IvyGrabRecord... grabRecords) {
        def cacheManager = ivyInstance.getResolutionCacheManager()
        def millis = System.currentTimeMillis()
        def md = new DefaultModuleDescriptor(ModuleRevisionId.newInstance('caller', 'all-caller', 'working' + millis.toString()[-2..-1]), 'integration', null, true)
        md.addConfiguration(new Configuration('default'))
        md.setLastModified(millis)

        addExcludesIfNeeded(args, md)

        for (IvyGrabRecord grabRecord : grabRecords) {
            List<String> confs = grabRecord.conf ?: ['*']
            DefaultDependencyDescriptor dd = (DefaultDependencyDescriptor) md.getDependencies().find {
                it.getDependencyRevisionId() == grabRecord.mrid
            }
            if (!dd) {
                dd = new DefaultDependencyDescriptor(md, grabRecord.mrid, grabRecord.force, grabRecord.changing, grabRecord.transitive)
                confs.each { conf -> dd.addDependencyConfiguration('default', conf) }
                md.addDependency(dd)
            }

            if (grabRecord.classifier != null
                    || (grabRecord.ext != null && grabRecord.ext != 'jar')
                    || (grabRecord.type != null && grabRecord.type != 'jar')) {
                // add artifact descriptor to dependency descriptor
                def dad = new DefaultDependencyArtifactDescriptor(dd, grabRecord.mrid.name, grabRecord.type ?: 'jar', grabRecord.ext ?: 'jar', null, grabRecord.classifier ? [classifier: grabRecord.classifier] : null)
                confs.each { conf -> dd.addDependencyArtifact(conf, dad) }
            }
        }

        // resolve grab and dependencies
        def resolveOptions = new ResolveOptions(
            confs: DEFAULT_CONF as String[], outputReport: false,
            validate: (args.containsKey('validate') ? (boolean) args.validate : false)
        )
        ivyInstance.getSettings().setDefaultResolver(args.autoDownload ? 'downloadGrapes' : 'cachedGrapes')
        if (args.disableChecksums) {
            ivyInstance.getSettings().setVariable('ivy.checksums', '')
        }
        boolean reportDownloads = Boolean.getBoolean('groovy.grape.report.downloads')
        if (reportDownloads) {
            addIvyListener()
        }

        ResolveReport report = null
        int attempt = 8 // max of 8 times
        while (true) {
            try {
                report = ivyInstance.resolve(md, resolveOptions)
                break
            } catch (IOException e) {
                if (attempt--) {
                    if (reportDownloads) System.err.println('Grab Error: retrying...')
                    sleep(attempt > 4 ? 350 : 1000)
                    continue
                }
                throw new RuntimeException("Error grabbing grapes -- ${e.getMessage()}")
            }
        }

        if (report.hasError()) {
            throw new RuntimeException("Error grabbing Grapes -- ${report.getAllProblemMessages()}")
        }
        if (report.getDownloadSize() && reportDownloads) {
            System.err.println("Downloaded ${report.getDownloadSize() >> 10} Kbytes in ${report.getDownloadTime()}ms:\n  ${report.getAllArtifactsReports()*.toString().join('\n  ')}")
        }
        md = report.getModuleDescriptor()

        if (!args.preserveFiles) {
            cacheManager.getResolvedIvyFileInCache(md.getModuleRevisionId()).delete()
            cacheManager.getResolvedIvyPropertiesInCache(md.getModuleRevisionId()).delete()
        }

        report
    }

    private addIvyListener() {
        ivyInstance.eventManager.addIvyListener { ivyEvent ->
            switch (ivyEvent) {
            case StartResolveEvent:
                ((StartResolveEvent) ivyEvent).getModuleDescriptor().getDependencies().each {
                    def name = it.toString()
                    if (resolvedDependencies.add(name)) {
                        System.err.println("Resolving $name")
                    }
                }
                break
            case PrepareDownloadEvent:
                ((PrepareDownloadEvent) ivyEvent).getArtifacts().each {
                    def name = it.toString()
                    if (downloadedArtifacts.add(name)) {
                        System.err.println("Preparing to download artifact $name")
                    }
                }
                break
            }
        }
    }

    void uninstallArtifact(String group, String module, String rev) {
        // TODO: consider transitive uninstall as an option
        Pattern ivyFilePattern = ~/ivy-(.*)\.xml/ // TODO: get pattern from ivy conf
        grapeCacheDir.eachDir { File groupDir ->
            if (groupDir.getName() == group) groupDir.eachDir { File moduleDir ->
                if (moduleDir.getName() == module) moduleDir.eachFileMatch(ivyFilePattern) { File ivyFile ->
                    def m = ivyFilePattern.matcher(ivyFile.getName())
                    if (m.matches() && m.group(1) == rev) {
                        // TODO: handle other types? e.g. 'dlls'
                        def jardir = new File(moduleDir, 'jars')
                        if (!jardir.exists()) return
                        def db = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder()
                        def root = db.parse(ivyFile).getDocumentElement()
                        def publis = root.getElementsByTagName('publications')
                        for (int i = 0; i < publis.length; i += 1) {
                            def artifacts = ((org.w3c.dom.Element) publis.item(i)).getElementsByTagName('artifact')
                            processArtifacts(artifacts, rev, jardir)
                        }
                        ivyFile.delete()
                    }
                }
            }
        }
    }

    private void processArtifacts(org.w3c.dom.NodeList artifacts, String rev, File jardir) {
        for (int i = 0, n = artifacts.getLength(); i < n; i += 1) {
            org.w3c.dom.Node artifact = artifacts.item(i)
            def attrs = artifact.getAttributes()
            def name = attrs.getNamedItem('name').getTextContent() + "-$rev"
            def classifier = attrs.getNamedItemNS('m', 'classifier')?.getTextContent()
            if (classifier) name += "-$classifier"
            name += ".${attrs.getNamedItem('ext').getTextContent()}"
            def jarfile = new File(jardir, name)
            if (jarfile.exists()) {
                System.err.println("Deleting ${jarfile.getName()}")
                jarfile.delete()
            }
        }
    }

    private addExcludesIfNeeded(Map args, DefaultModuleDescriptor md) {
        args.excludes?.each { Map<String, String> map ->
            def excludeRule = new DefaultExcludeRule(
                    new ArtifactId(
                            new ModuleId(map.group, map.module),
                            PatternMatcher.ANY_EXPRESSION,
                            PatternMatcher.ANY_EXPRESSION,
                            PatternMatcher.ANY_EXPRESSION),
                    ExactPatternMatcher.INSTANCE,
                    null)
            excludeRule.addConfiguration('default')
            md.addExcludeRule(excludeRule)
        }
    }

    @Override
    Map<String, Map<String, List<String>>> enumerateGrapes() {
        Map<String, Map<String, List<String>>> bunches = [:]
        Pattern ivyFilePattern = ~/ivy-(.*)\.xml/ // TODO: get pattern from ivy conf
        grapeCacheDir.eachDir { File groupDir ->
            Map<String, List<String>> grapes = [:]
            bunches[groupDir.getName()] = grapes
            groupDir.eachDir { File moduleDir ->
                List<String> versions = []
                moduleDir.eachFileMatch(ivyFilePattern) { File ivyFile ->
                    def m = ivyFilePattern.matcher(ivyFile.getName())
                    if (m.matches()) versions += m.group(1)
                }
                grapes[moduleDir.getName()] = versions
            }
        }
        bunches
    }

    @Override
    URI[] resolve(Map args, Map... dependencies) {
        resolve(args, null, dependencies)
    }

    @Override
    URI[] resolve(Map args, List depsInfo, Map... dependencies) {
        // identify the target classloader early, so we fail before checking repositories
        ClassLoader loader = chooseClassLoader(
                refObject: args.remove('refObject'),
                classLoader: args.remove('classLoader'),
                calleeDepth: args.calleeDepth ?: DEFAULT_CALLEE_DEPTH,
        )

        // check for non-fail null
        // if we were in fail mode we would have already thrown an exception
        if (!loader) {
            return new URI[0]
        }

        resolve(loader, args, depsInfo, dependencies)
    }

    URI[] resolve(ClassLoader loader, Map args, Map... dependencies) {
        resolve(loader, args, null, dependencies)
    }

    URI[] resolve(ClassLoader loader, Map args, List depsInfo, Map... dependencies) {
        // check for mutually exclusive arguments
        Set<String> keys = args.keySet()
        keys.each { key ->
            Set<String> badArgs = MUTUALLY_EXCLUSIVE_KEYS[key]
            if (badArgs && !badArgs.disjoint(keys)) {
                throw new RuntimeException("Mutually exclusive arguments passed into grab: ${keys.intersect(badArgs) + key}")
            }
        }

        // check the kill switch
        if (!enableGrapes) {
            return new URI[0]
        }

        boolean populateDepsInfo = (depsInfo != null)
        Set<IvyGrabRecord> localDeps = getLoadedDepsForLoader(loader)
        dependencies.each { Map dep ->
            IvyGrabRecord igr = createGrabRecord(dep)
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
            report = getDependencies(args, (localDeps as IvyGrabRecord[]).reverse(true))
        } catch (Exception e) {
            localDeps.removeAll(grabRecordsForCurrDependencies)
            grabRecordsForCurrDependencies.clear()
            throw e
        }

        List<URI> results = []
        for (ArtifactDownloadReport adl : report.getAllArtifactsReports()) {
            // TODO: check artifact type, jar vs library, etc.
            if (adl.getLocalFile()) {
                results += adl.getLocalFile().toURI()
            }
        }

        if (populateDepsInfo) {
            report.getDependencies().each { ivyNode ->
                def id = ivyNode.id
                depsInfo << ['group': id.organisation, 'module': id.name, 'revision': id.revision]
            }
        }

        results as URI[]
    }

    private Set<IvyGrabRecord> getLoadedDepsForLoader(ClassLoader loader) {
        // use a LinkedHashSet to preserve the initial insertion order
        loadedDeps.computeIfAbsent(loader, k -> [] as LinkedHashSet)
    }

    @Override
    Map[] listDependencies(ClassLoader classLoader) {
        List<? extends Map> results = loadedDeps[classLoader]?.collect { IvyGrabRecord grabbed ->
            def dep = [
                    group  : grabbed.mrid.getOrganisation(),
                    module : grabbed.mrid.getName(),
                    version: grabbed.mrid.getRevision()
            ]
            if (grabbed.conf != DEFAULT_CONF) {
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
            dep
        }
        results as Map[]
    }

    @Override
    void addResolver(@NamedParams([
        @NamedParam(value='name', type=String, required=true),
        @NamedParam(value='root', type=String, required=true),
        @NamedParam(value='m2Compatible', type=Boolean, required=false)
    ]) Map<String, Object> args) {
        def resolver = new IBiblioResolver(
            name: (String) args.name,
            root: (String) args.root,
            settings: (ResolverSettings) settings,
            m2compatible: (boolean) args.getOrDefault('m2Compatible', Boolean.TRUE)
        )

        def chainResolver = (ChainResolver) settings.getResolver('downloadGrapes')
        chainResolver.resolvers.add(0, resolver)

        ivyInstance = Ivy.newInstance(settings)
        resolvedDependencies = []
        downloadedArtifacts = []
    }
}

@CompileStatic
@EqualsAndHashCode
class IvyGrabRecord {
    ModuleRevisionId mrid
    List<String> conf
    String ext
    String type
    String classifier
    boolean force
    boolean changing
    boolean transitive
}
