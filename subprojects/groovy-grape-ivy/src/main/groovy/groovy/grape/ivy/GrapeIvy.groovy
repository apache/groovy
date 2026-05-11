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
package groovy.grape.ivy

import groovy.grape.GrapeEngine
import groovy.grape.GrapeUtil

import groovy.transform.AutoFinal
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.NamedParam
import groovy.transform.NamedParams
import org.apache.ivy.Ivy
import org.apache.ivy.core.IvyContext
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
import org.apache.ivy.core.resolve.IvyNode
import org.apache.ivy.core.resolve.ResolveOptions
import org.apache.ivy.core.settings.IvySettings
import org.apache.ivy.plugins.matcher.ExactPatternMatcher
import org.apache.ivy.plugins.matcher.PatternMatcher
import org.apache.ivy.plugins.resolver.ChainResolver
import org.apache.ivy.plugins.resolver.IBiblioResolver
import org.apache.ivy.plugins.resolver.ResolverSettings
import org.apache.ivy.util.Message
import org.codehaus.groovy.reflection.ReflectionUtils
import org.w3c.dom.Element

import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import java.text.ParseException
import java.util.regex.Pattern

/**
 * Implementation supporting {@code @Grape} and {@code @Grab} annotations based on Ivy.
 */
@AutoFinal @CompileStatic
class GrapeIvy implements GrapeEngine {

    static {
        // GROOVY-12005 / 2026-05: Maven Central (Fastly) currently returns HTTP 404
        // with a 1-byte body for requests whose User-Agent is the JDK URLConnection
        // default "Java/<version>". Maven's own requests are not blocked, so we
        // adopt a Maven-shaped User-Agent — that's the value Maven Resolver's HTTP
        // transport sends, and it passes the CDN cleanly. Ivy 2.5.3's
        // BasicURLHandler uses java.net.URLConnection, which honours -Dhttp.agent.
        //
        // This static initializer covers direct grape use (CLI `grape install`,
        // `groovy script-with-@Grab.groovy`, embedded usage) — those paths load
        // GrapeIvy before any other class touches the network, so it runs early
        // enough that HttpURLConnection's userAgent static init reads our value.
        //
        // For test JVMs the same property MUST be set on the JVM command line —
        // see the matching `systemProperty 'http.agent', ...` in
        // build-logic/.../org.apache.groovy-tested.gradle — because by the time
        // GrapeIvy is class-loaded for the first @Grab in a test, HttpURLConnection
        // has already been initialised and its userAgent is locked in.
        //
        // Respect any value the user has already configured (e.g. -Dhttp.agent=…).
        if (System.getProperty('http.agent') == null) {
            // Maven version is decorative — the CDN's filter is on the bare
            // "Java/<version>" default, not on a specific Maven version string.
            String ua = "Apache-Maven/3.9.14 (Java ${System.getProperty('java.version')}; ${System.getProperty('os.name')} ${System.getProperty('os.version')})"
            System.setProperty('http.agent', ua)
        }
    }

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

    /** Enables or disables grape resolution. */
    boolean enableGrapes = true

    /** Active Ivy instance used for dependency resolution. */
    Ivy ivyInstance
    /** Ivy settings backing this engine. */
    IvySettings settings
    /** Artifact files downloaded during the last resolution. */
    Set<String> downloadedArtifacts = []
    /** Module revisions resolved during the last resolution. */
    Set<String> resolvedDependencies = []
    // weak hash map so we don't leak loaders directly
    /** Dependencies loaded into each class loader. */
    final Map<ClassLoader, Set<IvyGrabRecord>> loadedDeps = [] as WeakHashMap
    /** Stores the IvyGrabRecord(s) for all dependencies in each grab() call. */
    final Set<IvyGrabRecord> grabRecordsForCurrDependencies = [] as LinkedHashSet

    /**
     * Creates an Ivy-backed grape engine.
     */
    GrapeIvy() {
        Message.setDefaultLogger(new PlatformLoggingMessageLogger())

        settings = new IvySettings()
        def url = new File(System.getProperty('user.home')).toURI().toURL() as String
        settings.setVariable('user.home.url', url.endsWith("/") ? url[0..-2] : url)
        File grapeConfig = getLocalGrapeConfig()
        if (grapeConfig.exists()) {
            try {
                settings.load(grapeConfig)
            } catch (ParseException e) {
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

    /**
     * Returns the configured Groovy home directory for grape state.
     *
     * @return the Groovy home directory
     */
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

    /**
     * Returns the root directory used by grape.
     *
     * @return the grape root directory
     */
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

    /**
     * Returns the cache directory used for downloaded artifacts.
     *
     * @return the grape cache directory
     */
    File getGrapeCacheDir() {
        File cache = new File(getGrapeDir(), 'grapes')
        if (!cache.exists()) {
            cache.mkdirs()
        } else if (!cache.isDirectory()) {
            throw new RuntimeException("The grape cache dir $cache is not a directory")
        }
        cache
    }

    /**
     * Returns the local Ivy grape configuration file.
     *
     * @return the grape configuration file
     */
    File getLocalGrapeConfig() {
        String grapeConfig = System.getProperty('grape.config')
        if (grapeConfig) {
            new File(grapeConfig)
        } else {
            new File(getGrapeDir(), 'grapeConfig.xml')
        }
    }

    /**
     * Chooses the target class loader for a grab operation.
     *
     * @param args grab arguments
     * @return the chosen class loader
     */
    ClassLoader chooseClassLoader(Map args) {
        ClassLoader loader = (ClassLoader) args.classLoader
        if (!isValidTargetClassLoader(loader)) {
            int calleeDepth = (int) (args.calleeDepth ?: 1)
            Class caller = args.refObject?.getClass() ?: ReflectionUtils.getCallingClass(calleeDepth)
            loader = caller?.getClassLoader()
            while (loader && !isValidTargetClassLoader(loader)) {
                loader = loader.getParent()
            }
            // Some call paths (e.g. instance-style static dispatch) can shift frames.
            if (!isValidTargetClassLoader(loader)) {
                int from = Math.max(1, calleeDepth - 2)
                int to = calleeDepth + 2
                for (int i = from; i <= to && !isValidTargetClassLoader(loader); i += 1) {
                    Class alt = ReflectionUtils.getCallingClass(i)
                    ClassLoader altLoader = alt?.getClassLoader()
                    while (altLoader && !isValidTargetClassLoader(altLoader)) {
                        altLoader = altLoader.getParent()
                    }
                    if (isValidTargetClassLoader(altLoader)) {
                        loader = altLoader
                    }
                }
            }
            // Refless/static contexts can hide the script loader from stack walking.
            if (!isValidTargetClassLoader(loader)) {
                loader = Thread.currentThread().getContextClassLoader()
            }
            if (!isValidTargetClassLoader(loader)) {
                loader = GrapeIvy.class.getClassLoader()
            }
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

    /**
     * Converts dependency coordinates into an Ivy grab record.
     *
     * @param dep the dependency map
     * @return the corresponding grab record
     */
    IvyGrabRecord createGrabRecord(Map dep) {
        String module = dep.module ?: dep.artifactId ?: dep.artifact
        if (!module) {
            throw new RuntimeException('grab requires at least a module: or artifactId: or artifact: argument')
        }

        // check for malformed components of the coordinates
        dep.each { k, v ->
            if (v instanceof CharSequence) {
                if (k.toString().contains('v')) { // revision, version, rev
                    if (!(v ==~ '[^\\/:"<>|]*')) {
                        throw new RuntimeException("Grab: invalid value of '$v' for $k: should not contain any of / \\ : \" < > |")
                    }
                } else {
                    if (!(v ==~ '[-._a-zA-Z0-9]*')) {
                        throw new RuntimeException("Grab: invalid value of '$v' for $k: should only contain - . _ a-z A-Z 0-9")
                    }
                }
            }
        }

        // check for mutually exclusive arguments
        Set<String> keys = (Set<String>) dep.keySet()
        keys.each { key ->
            Set<String> badArgs = MUTUALLY_EXCLUSIVE_KEYS[key]
            if (badArgs && !badArgs.disjoint(keys)) {
                throw new RuntimeException("Grab: mutually exclusive arguments: ${keys.intersect(badArgs) + key}")
            }
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

    /**
     * Grabs the endorsed module for the current Groovy version.
     *
     * @param endorsedModule the endorsed module name
     */
    @Override // TODO deprecate
    grab(String endorsedModule) {
        grab(group: 'groovy.endorsed', module: endorsedModule, version: GroovySystem.getVersion())
    }

    /**
     * Grabs a dependency described by a single argument map.
     *
     * @param args the grab arguments
     * @return the grab result or exception when suppressed
     */
    @Override
    grab(Map args) {
        args.calleeDepth = args.calleeDepth ?: DEFAULT_CALLEE_DEPTH + 1
        grab(args, args)
    }

    /**
     * Grabs one or more dependencies and adds them to the target class loader.
     *
     * @param args the shared grab arguments
     * @param dependencies the dependency descriptors
     * @return {@code null} on success or the suppressed exception
     */
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
                GrapeUtil.addURL(loader, uri)
            }
            boolean runnerServicesFound = false
            for (URI uri : uris) {
                // TODO: check artifact type, jar vs library, etc.
                File file = new File(uri)
                GrapeUtil.processExtensionMethods(loader, file)
                Collection<String> services = GrapeUtil.processMetaInfServices(loader, file)
                if (!runnerServicesFound) {
                    runnerServicesFound = GrapeUtil.checkForRunner(services)
                }
            }
            if (runnerServicesFound) {
                GrapeUtil.registryLoad(loader)
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

    /**
     * Resolves the supplied grab records with Ivy.
     *
     * @param args the resolution arguments
     * @param grabRecords the dependencies to resolve
     * @return the Ivy resolve report
     */
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
            throw new RuntimeException("Error grabbing Grapes -- ${report.getAllProblemMessages()}${diagnoseHalfPopulatedLocalM2(report)}")
        }
        if (report.getDownloadSize() && reportDownloads) {
            System.err.println("Downloaded ${report.getDownloadSize() >> 10} Kbytes in ${report.getDownloadTime()}ms:\n  ${report.getAllArtifactsReports()*.toString().join('\n  ')}")
        }

        if (!args.preserveFiles) {
            def revision = report.getModuleDescriptor().getModuleRevisionId()
            cacheManager.getResolvedIvyPropertiesInCache(revision).delete()
            cacheManager.getResolvedIvyFileInCache(revision).delete()
        }

        report
    }

    /**
     * When a download fails, check whether the local Maven cache has the POM but
     * not the primary artifact for any failed dependency. Ivy binds artifact
     * downloads to the resolver that resolved the descriptor, so a localm2 with
     * only the POM blocks the chain from falling through to Maven Central.
     */
    private static String diagnoseHalfPopulatedLocalM2(ResolveReport report) {
        File m2root = new File(System.getProperty('user.home'), '.m2/repository')
        if (!m2root.isDirectory()) return ''
        StringBuilder hint = new StringBuilder()
        Set<String> seen = new LinkedHashSet<String>()
        // Artifact-level failures ("download failed: g#a;v!a.jar") — primary half-population.
        for (ArtifactDownloadReport adr : report.getFailedArtifactsReports()) {
            String classifier = (String) adr.getArtifact().getExtraAttribute('classifier')
            appendHintForCoord(adr.getArtifact().getModuleRevisionId(),
                    adr.getArtifact().getName(),
                    adr.getArtifact().getExt() ?: 'jar',
                    classifier, m2root, hint, seen)
        }
        // Dependency-level failures ("unresolved dependency: g#a;v not found") — typical when
        // the JAR-only-in-localm2 case combines with a transient Central descriptor lookup miss.
        for (IvyNode node : report.getUnresolvedDependencies()) {
            appendHintForCoord(node.getId(), node.getId().getName(), 'jar', null, m2root, hint, seen)
        }
        return hint.toString()
    }

    private static void appendHintForCoord(ModuleRevisionId mrid, String artName, String ext,
                                           String classifier, File m2root, StringBuilder hint,
                                           Set<String> seen) {
        String org = mrid.getOrganisation()
        String mod = mrid.getName()
        String rev = mrid.getRevision()
        String coord = "${org}:${mod}:${rev}".toString()
        if (!seen.add(coord)) return
        File dir = new File(m2root, "${org.replace('.', '/')}/${mod}/${rev}")
        if (!dir.isDirectory()) return
        File pom = new File(dir, "${mod}-${rev}.pom")
        String suffix = classifier ? "-${classifier}" : ''
        File primary = new File(dir, "${artName}-${rev}${suffix}.${ext}")
        if (pom.exists() && !primary.exists()) {
            File grapeIvyXml = new File(System.getProperty('user.home'),
                    ".groovy/grapes/${org}/${mod}/ivy-${rev}.xml")
            hint.append('\nHint: ').append(coord)
                .append(' has a POM but no ').append(ext).append(' in your local Maven cache.\n')
                .append('Either run:  mvn dependency:get -Dartifact=').append(coord).append('\n')
                .append('or remove these so Grape can fetch from Maven Central:\n')
                .append('  ').append(dir).append('\n')
                .append('  ').append(grapeIvyXml).append(' (and ivy-')
                .append(rev).append('.xml.original, ivydata-').append(rev).append('.properties)')
        } else if (primary.exists() && !pom.exists()) {
            hint.append('\nHint: ').append(coord)
                .append(' has a ').append(ext).append(' but no POM in your local Maven cache.\n')
                .append('Ivy needs the POM to resolve the descriptor; without it the chain falls through ')
                .append('to Maven Central and any transient Central failure surfaces as "not found".\n')
                .append('Either run:  mvn dependency:get -Dartifact=').append(coord).append('\n')
                .append('or remove ').append(dir).append(' to force a clean fetch.')
        }
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

    /**
     * Removes a cached artifact version from the grape cache.
     *
     * @param group the artifact group
     * @param module the artifact module
     * @param rev the artifact revision
     */
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
                        def dbf = DocumentBuilderFactory.newInstance()
                        try {
                            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
                            dbf.setFeature('http://apache.org/xml/features/disallow-doctype-decl', true)
                        } catch (ParserConfigurationException ignored) {
                            // feature not supported by this JAXP impl
                        }
                        dbf.setXIncludeAware(false)
                        dbf.setExpandEntityReferences(false)
                        def db = dbf.newDocumentBuilder()
                        def root = db.parse(ivyFile).getDocumentElement()
                        def publis = root.getElementsByTagName('publications')
                        for (int i = 0; i < publis.length; i += 1) {
                            def artifacts = ((Element) publis.item(i)).getElementsByTagName('artifact')
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

    /**
     * Enumerates cached grapes by group, module, and version.
     *
     * @return the cached grape coordinates
     */
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
                    if (m.matches()) versions.add(m.group(1))
                }
                grapes[moduleDir.getName()] = versions
            }
        }
        bunches
    }

    /**
     * Resolves dependencies using the implicit target class loader.
     *
     * @param args the resolution arguments
     * @param dependencies the dependencies to resolve
     * @return the resolved artifact URIs
     */
    @Override
    URI[] resolve(Map args, Map... dependencies) {
        resolve(args, null, dependencies)
    }

    /**
     * Resolves dependencies and optionally collects dependency metadata.
     *
     * @param args the resolution arguments
     * @param depsInfo the optional dependency metadata sink
     * @param dependencies the dependencies to resolve
     * @return the resolved artifact URIs
     */
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

    /**
     * Resolves dependencies for an explicit class loader.
     *
     * @param loader the target class loader
     * @param args the resolution arguments
     * @param dependencies the dependencies to resolve
     * @return the resolved artifact URIs
     */
    URI[] resolve(ClassLoader loader, Map args, Map... dependencies) {
        resolve(loader, args, null, dependencies)
    }

    /**
     * Resolves dependencies for an explicit class loader and optional metadata sink.
     *
     * @param loader the target class loader
     * @param args the resolution arguments
     * @param depsInfo the optional dependency metadata sink
     * @param dependencies the dependencies to resolve
     * @return the resolved artifact URIs
     */
    URI[] resolve(ClassLoader loader, Map args, List depsInfo, Map... dependencies) {
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
            def adlLocalFile = adl.getLocalFile()
            if (adlLocalFile) {
                results.add(adlLocalFile.toURI())
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

    /**
     * Lists dependencies already loaded into the supplied class loader.
     *
     * @param classLoader the class loader to inspect
     * @return the loaded dependency descriptors
     */
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

    /**
     * Adds a resolver to the front of the Ivy resolver chain.
     *
     * @param args the resolver configuration
     */
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

    /**
     * Creates an Ivy listener from a progress closure.
     *
     * @param c the progress callback
     * @return the corresponding Ivy listener
     */
    IvyListener makeIvyListener(Closure c) {
        [progress: c] as IvyListener
    }

    /**
     * Sets the Ivy logging verbosity.
     *
     * @param level the grape logging level
     */
    @Override
    void setLoggingLevel(int level) {
        // Map numeric level (from grape -q/-w/-i/-V/-d flags) to JUL level
        // for the platform logging backed Ivy logger.
        // 0=quiet/errors only, 1=warn, 2=info, 3=verbose, 4=debug
        java.util.logging.Level julLevel
        switch (level) {
            case 0:
                julLevel = java.util.logging.Level.SEVERE
                break
            case 1:
                julLevel = java.util.logging.Level.WARNING
                break
            case 2:
                julLevel = java.util.logging.Level.INFO
                break
            case 3:
                julLevel = java.util.logging.Level.FINE
                break
            case 4:
                julLevel = java.util.logging.Level.FINEST
                break
            default:
                julLevel = java.util.logging.Level.INFO
        }
        java.util.logging.Logger.getLogger('groovy.grape.ivy').setLevel(julLevel)
    }
}

/**
 * Value object describing a grabbed Ivy dependency.
 */
@CompileStatic
@EqualsAndHashCode
class IvyGrabRecord {
    /** Ivy module revision coordinates. */
    ModuleRevisionId mrid
    /** Requested Ivy configurations. */
    List<String> conf
    /** Requested extension. */
    String ext
    /** Requested artifact type. */
    String type
    /** Requested classifier. */
    String classifier
    /** Whether this dependency is forced. */
    boolean force
    /** Whether this dependency may change over time. */
    boolean changing
    /** Whether transitive dependencies should be resolved. */
    boolean transitive
}
