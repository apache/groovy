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
package groovy.grape.maven

import groovy.grape.GrapeEngine
import groovy.grape.GrapeUtil
import groovy.transform.AutoFinal
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.NamedParam
import groovy.transform.NamedParams
import org.codehaus.groovy.reflection.ReflectionUtils
import org.eclipse.aether.AbstractRepositoryListener
import org.eclipse.aether.RepositoryEvent
import org.eclipse.aether.RepositorySystem
import org.eclipse.aether.RepositorySystemSession
import org.eclipse.aether.artifact.Artifact
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.collection.CollectRequest
import org.eclipse.aether.graph.Dependency
import org.eclipse.aether.graph.DependencyFilter
import org.eclipse.aether.graph.DependencyNode
import org.eclipse.aether.internal.impl.scope.OptionalDependencySelector
import org.eclipse.aether.internal.impl.scope.ScopeDependencySelector
import org.eclipse.aether.repository.LocalRepository
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.repository.RepositoryPolicy
import org.eclipse.aether.resolution.ArtifactRequest
import org.eclipse.aether.resolution.ArtifactResult
import org.eclipse.aether.resolution.DependencyRequest
import org.eclipse.aether.resolution.DependencyResolutionException
import org.eclipse.aether.supplier.RepositorySystemSupplier
import org.eclipse.aether.transfer.AbstractTransferListener
import org.eclipse.aether.transfer.TransferEvent
import org.eclipse.aether.util.graph.selector.AndDependencySelector
import org.eclipse.aether.util.repository.SimpleArtifactDescriptorPolicy

import java.util.concurrent.CopyOnWriteArrayList

/**
 * Implementation supporting {@code @Grape} and {@code @Grab} annotations based on Maven.
 *
 * @since 6.0.0
 */
@AutoFinal
@CompileStatic
class GrapeMaven implements GrapeEngine {
    private static final List<String> DEFAULT_CONF = Collections.singletonList('default')
    private static final Map<String, Set<String>> MUTUALLY_EXCLUSIVE_KEYS = processGrabArgs([
            ['group', 'groupId', 'organisation', 'organization', 'org'],
            ['module', 'artifactId', 'artifact'],
            ['version', 'revision', 'rev'],
            ['conf', 'scope', 'configuration'],
    ])
    private static final boolean DEBUG_GRAB = Boolean.getBoolean('groovy.grape.debug')

    static {
        // Configure SLF4J Simple Logger early so Maven/Aether infrastructure logs at WARN
        // by default, or DEBUG when groovy.grape.debug is set.
        String level = DEBUG_GRAB ? 'debug' : 'warn'
        System.setProperty('org.slf4j.simpleLogger.defaultLogLevel', level)

        // If SimpleLoggerFactory was already initialised (e.g. by the test framework),
        // reset its internal logger cache so subsequent getLogger() calls pick up the
        // new level.  This is a best-effort reflection-only approach; if it fails the
        // system-property set above will at least govern any not-yet-created loggers.
        try {
            Class<?> lfc = Class.forName('org.slf4j.LoggerFactory')
            Object ilf = lfc.getMethod('getILoggerFactory').invoke(null)
            if (ilf != null && ilf.getClass().getName() == 'org.slf4j.simple.SimpleLoggerFactory') {
                java.lang.reflect.Field f = ilf.getClass().getDeclaredField('loggerMap')
                f.setAccessible(true)
                ((java.util.Map) f.get(ilf)).clear()
            }
        } catch (Exception ignored) {
            // Not using slf4j-simple, or reflection failed — best effort only
        }
    }

    @CompileDynamic // maps a->[b,c], b->[a,c] and c->[a,b] given [a,b,c]
    private static Map<String, Set<String>> processGrabArgs(List<List<String>> grabArgs) {
        grabArgs.inject([:]) { Map m, List g -> g.each { a -> m[a] = (g - a) as Set }; m }
    }

    // weak hash map so we don't leak loaders directly
    final Map<ClassLoader, Set<MavenGrabRecord>> loadedDeps = [] as WeakHashMap
    /** Stores the MavenGrabRecord(s) for all dependencies in each grab() call. */
    final Set<MavenGrabRecord> grabRecordsForCurrDependencies = [] as LinkedHashSet
    boolean enableGrapes = true
    final List<Closure> progressListeners = new CopyOnWriteArrayList<>()
    final List<RemoteRepository> repos = [
        new RemoteRepository.Builder("central", "default", "https://repo.maven.apache.org/maven2/").build()
    ]

    @CompileDynamic
    void addProgressListener(Closure listener) {
        if (listener != null) {
            progressListeners.add(listener)
        }
    }

    void removeProgressListener(Closure listener) {
        progressListeners.remove(listener)
    }

    private boolean hasProgressListeners() {
        !progressListeners.isEmpty()
    }

    @CompileDynamic
    private void fireProgressEvent(String type, String name) {
        if (!name) return
        Map<String, String> event = [type: type, name: name]
        progressListeners.each { Closure listener ->
            listener.call(event)
        }
    }

    private AbstractRepositoryListener createRepositoryListener() {
        new AbstractRepositoryListener() {
            @Override
            void artifactResolving(RepositoryEvent event) {
                Artifact artifact = event?.artifact
                if (artifact != null && artifact.extension != 'pom') {
                    fireProgressEvent('resolving', artifact.toString())
                }
            }
        }
    }

    private AbstractTransferListener createTransferListener() {
        new AbstractTransferListener() {
            @Override
            void transferInitiated(TransferEvent event) {
                String resourceName = event?.resource?.resourceName
                String displayName = displayDownloadName(resourceName)
                if (displayName != null) {
                    fireProgressEvent('downloading', displayName)
                }
            }
        }
    }

    private static String displayDownloadName(String resourceName) {
        if (!resourceName) return null
        String fileName = resourceName.tokenize('/').last()
        if (!fileName
            || fileName.startsWith('maven-metadata')
            || fileName.endsWith('.pom')
            || fileName.endsWith('.sha1')
            || fileName.endsWith('.md5')
            || fileName.endsWith('.asc')) {
            return null
        }
        fileName
    }

    /**
     * Grab the endorsed module for the current Groovy version.
     */
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
            Set<MavenGrabRecord> grabRecordsForCurrLoader = getLoadedDepsForLoader(loader)
            grabRecordsForCurrLoader.removeAll(grabRecordsForCurrDependencies)
            grabRecordsForCurrDependencies.clear()

            if (args.noExceptions) {
                return e
            }
            throw asRuntimeGrabError(e)
        }
        null
    }


    @Override
    @CompileDynamic
    Map<String, Map<String, List<String>>> enumerateGrapes() {
        Map<String, Map<String, List<String>>> bunches = [:]
        File cacheRoot = grapeCacheDir.canonicalFile

        cacheRoot.eachFileRecurse { File f ->
            if (!f.isFile()) return
            String name = f.name
            if (name.endsWith('.sha1') || name.endsWith('.md5') || name.endsWith('.asc')) return

            File versionDir = f.parentFile
            File moduleDir = versionDir?.parentFile
            File groupDir = moduleDir?.parentFile
            if (!versionDir || !moduleDir || !groupDir) return

            String version = versionDir.name
            String module = moduleDir.name
            String expectedPrefix = module + '-' + version
            if (!name.startsWith(expectedPrefix)) return

            String groupPath = cacheRoot.toPath().relativize(groupDir.toPath()).toString()
            if (!groupPath) return
            String groupKey = groupPath.replace(File.separatorChar, '.' as char)

            Map<String, List<String>> modules = bunches.computeIfAbsent(groupKey) { [:] }
            List<String> versions = modules.computeIfAbsent(module) { [] }
            if (!versions.contains(version)) {
                versions << version
            }
        }

        bunches.values()*.values()*.each { List<String> versions -> versions.sort() }
        bunches
    }

    void uninstallArtifact(String group, String module, String rev) {
        String groupPath = group.replace('.' as char, File.separatorChar)
        def artifactDir = new File(grapeCacheDir, groupPath + File.separator + module + File.separator + rev)
        if (artifactDir.exists()) {
            artifactDir.deleteDir()
        }
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

    private Set<MavenGrabRecord> getLoadedDepsForLoader(ClassLoader loader) {
        // use a LinkedHashSet to preserve the initial insertion order
        loadedDeps.computeIfAbsent(loader, k -> [] as LinkedHashSet)
    }

    URI[] resolve(ClassLoader loader, Map args, Map... dependencies) {
        resolve(loader, args, null, dependencies)
    }

    URI[] resolve(ClassLoader loader, Map args, List depsInfo, Map... dependencies) {
        if (!enableGrapes) {
            return new URI[0]
        }

        boolean populateDepsInfo = (depsInfo != null)
        Set<MavenGrabRecord> localDeps = getLoadedDepsForLoader(loader)
        List<MavenGrabRecord> grabRecords = []
        for (Map dep : dependencies) {
             MavenGrabRecord mgr = createGrabRecord(dep)
             grabRecordsForCurrDependencies.add(mgr)
             localDeps.add(mgr)
             grabRecords.add(mgr)
        }

        try {
            URI[] results = getDependencies(args, populateDepsInfo ? depsInfo : null, grabRecords as MavenGrabRecord[])
            return results
        } catch (Exception e) {
            localDeps.removeAll(grabRecordsForCurrDependencies)
            grabRecordsForCurrDependencies.clear()
            throw asRuntimeGrabError(e)
        }
    }

    URI[] getDependencies(Map args, List depsInfo, MavenGrabRecord... grabRecords) {
        try (RepositorySystem system = new RepositorySystemSupplier().get()) {
            def localRepo = new LocalRepository(grapeCacheDir.toPath())
            String checksumPolicy = args.disableChecksums ?
                RepositoryPolicy.CHECKSUM_POLICY_IGNORE :
                RepositoryPolicy.CHECKSUM_POLICY_WARN

            // Extract exclusions from args if provided by @GrabExclude
            List<Map<String, String>> exclusions = (List<Map<String, String>>) args.get('excludes') ?: []

            try (RepositorySystemSession.CloseableSession session = system
                .createSessionBuilder()
                .withLocalRepositories(localRepo)
                .setChecksumPolicy(checksumPolicy)
                .setDependencySelector(new AndDependencySelector(
                    ScopeDependencySelector.legacy(['compile', 'runtime'], ['test']),
                    OptionalDependencySelector.fromRoot()
                ))
                .setRepositoryListener(hasProgressListeners() ? createRepositoryListener() : null)
                .setTransferListener(hasProgressListeners() ? createTransferListener() : null)
                .setArtifactDescriptorPolicy(new SimpleArtifactDescriptorPolicy(true, true))
                // Set Java version properties for profile evaluation
                .setSystemProperty('java.version', System.getProperty('java.version'))
                .setSystemProperty('java.home', System.getProperty('java.home'))
                // Set OS detection properties (similar to os-maven-plugin)
                .setSystemProperty('os.detected.name', detectOsName())
                .setSystemProperty('os.detected.arch', detectOsArch())
                .setSystemProperty('os.detected.classifier', "${detectOsName()}-${detectOsArch()}")
                // Only the most essential configurations
                .setSystemProperty('aether.artifactDescriptor.ignoreErrors', 'true')
                .setConfigProperty('aether.artifactDescriptor.ignoreInvalidActivationExpressions', 'true')
                .build()) {

                List<URI> results = []

                for (MavenGrabRecord grabRecord : grabRecords) {
                    // Skip if this grab record is in the exclusion list
                    if (isExcluded(grabRecord, exclusions)) {
                        continue
                    }

                    String coords = "${grabRecord.groupId()}:${grabRecord.module()}"
                    if (grabRecord.ext()) {
                        coords += ":${grabRecord.ext()}"
                    }
                    if (grabRecord.classifier()) {
                        coords += ":${grabRecord.classifier()}"
                    }
                    coords += ":${grabRecord.version()}"

                    if (DEBUG_GRAB) {
                        System.err.println("[GrapeDebug] resolving root=${coords} transitive=${grabRecord.transitive()}")
                    }

                    Artifact artifact = new DefaultArtifact(coords)

                    String scope = grabRecord.conf()?.get(0) ?: 'compile'
                    if (scope == 'default') scope = 'compile'

                    List<ArtifactResult> artifactResults = []
                    if (grabRecord.transitive()) {
                        // Create a filter that prevents traversal into excluded artifacts
                        DependencyFilter exclusionFilter = createExclusionFilter(exclusions)

                        CollectRequest collectRequest = new CollectRequest(
                            root: new Dependency(artifact, scope),
                            repositories: repos
                        )
                        DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, exclusionFilter)
                        try {
                            artifactResults = system.resolveDependencies(session, dependencyRequest).getArtifactResults()
                        } catch (DependencyResolutionException dre) {
                            // Keep resolved artifacts; tolerate unresolved non-root transitives.
                            artifactResults = []
                            if (dre.result?.artifactResults) {
                                for (ArtifactResult ar : dre.result.artifactResults) {
                                    if (ar.resolved) {
                                        artifactResults << ar
                                        continue
                                    }
                                    Artifact missing = ar?.request?.artifact
                                    if (!isIgnorableUnresolvedArtifact(system, session, artifact, missing)) {
                                        throw dre
                                    }
                                }
                            } else {
                                throw dre
                            }
                        }
                    } else {
                        // Non-transitive resolution
                        ArtifactRequest artifactRequest = new ArtifactRequest(artifact, repos, null)
                        artifactResults = [system.resolveArtifact(session, artifactRequest)]
                    }

                    for (ArtifactResult found : artifactResults) {
                        if (found.artifact.path) {
                            if (DEBUG_GRAB) {
                                System.err.println("[GrapeDebug] resolved ${found.artifact.groupId}:${found.artifact.artifactId}:${found.artifact.extension}:${found.artifact.version} -> ${found.artifact.path}")
                            }
                            results << found.artifact.path.toUri()

                            if (depsInfo != null) {
                                depsInfo << [
                                    'group': found.artifact.groupId,
                                    'module': found.artifact.artifactId,
                                    'revision': found.artifact.version
                                ]
                            }
                        }
                    }
                }

                return results as URI[]
            }
        }
    }

    private static DependencyFilter createExclusionFilter(List<Map<String, String>> exclusions) {
        if (!exclusions) {
            return null  // No exclusions, no filtering needed
        }

        return { DependencyNode node, Collection parents ->
            // Return false to prevent traversal into excluded artifacts
            // This stops their dependencies from being collected
            return !isExcludedArtifact(node.artifact.groupId, node.artifact.artifactId, exclusions)
        } as DependencyFilter
    }

    private static boolean isExcluded(MavenGrabRecord grabRecord, List<Map<String, String>> exclusions) {
        return isExcludedArtifact(grabRecord.groupId(), grabRecord.module(), exclusions)
    }

    private static boolean isExcludedArtifact(String groupId, String artifactId, List<Map<String, String>> exclusions) {
        for (Map<String, String> exclusion : exclusions) {
            String excludeGroup = exclusion.group ?: exclusion.groupId
            String excludeModule = exclusion.module ?: exclusion.artifactId

            if (excludeGroup && excludeModule) {
                if (groupId == excludeGroup && artifactId == excludeModule) {
                    return true  // This artifact is excluded
                }
            }
        }
        return false
    }

    static MavenGrabRecord createGrabRecord(Map dep) {
        String module = dep.module ?: dep.artifactId ?: dep.artifact
        if (!module) {
            throw new RuntimeException('grab requires at least a module: or artifactId: or artifact: argument')
        }

        // check for malformed components of the coordinates
        dep.each { k, v ->
            if (v instanceof CharSequence) {
                if (k.toString().contains('v')) { // revision, version, rev
                    if (!(v ==~ '[^\\\\/:"<>|]*')) {
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
        String version = dep.version ?: dep.revision ?: dep.rev ?: '*'
        if (version == '*') version = 'LATEST'
        String classifier = dep.classifier ?: null
        String ext = dep.ext ?: dep.type ?: 'jar'
        String type = dep.type ?: ''

        boolean force = dep.containsKey('force') ? dep.force : true
        boolean changing = dep.containsKey('changing') ? dep.changing : false
        boolean transitive = dep.containsKey('transitive') ? dep.transitive : true

        new MavenGrabRecord(
            groupId,
            module,
            version,
            getConfList(dep),
            ext,
            type,
            classifier,
            force,
            changing,
            transitive
        )
    }

    @CompileDynamic
    private static List<String> getConfList(Map dep) {
        def conf = dep.conf ?: dep.scope ?: dep.configuration ?: DEFAULT_CONF
        if (conf instanceof String) {
            if (conf.startsWith('[') && conf.endsWith(']')) conf = conf[1..-2]
            conf = conf.tokenize(',')
        }
        (conf as List<String>)
    }

    @Override
    Map[] listDependencies(ClassLoader classLoader) {
        List<? extends Map> results = loadedDeps[classLoader]?.collect { MavenGrabRecord grabbed ->
            def dep = [
                group  : grabbed.groupId(),
                module : grabbed.module(),
                version: grabbed.version()
            ]
            if (grabbed.conf() != DEFAULT_CONF) {
                dep.conf = grabbed.conf()
            }
            if (grabbed.changing()) {
                dep.changing = grabbed.changing()
            }
            if (!grabbed.transitive()) {
                dep.transitive = grabbed.transitive()
            }
            if (!grabbed.force()) {
                dep.force = grabbed.force()
            }
            if (grabbed.classifier()) {
                dep.classifier = grabbed.classifier()
            }
            if (grabbed.ext()) {
                dep.ext = grabbed.ext()
            }
            if (grabbed.type()) {
                dep.type = grabbed.type()
            }
            dep
        }
        results as Map[]
    }

    @Override
    void addResolver(@NamedParams([
        @NamedParam(value = 'name', type = String, required = true),
        @NamedParam(value = 'root', type = String, required = true),
        @NamedParam(value = 'm2Compatible', type = Boolean, required = false)
    ]) Map<String, Object> args) {
        String root = (String) (args.root ?: args.value)
        if (!root) {
            throw new RuntimeException("GrabResolver requires a root: or value: argument")
        }
        String name = (String) (args.name ?: "resolver-${repos.size()}")
        RemoteRepository resolver = new RemoteRepository.Builder(name, "default", root).build()
        // Keep parity with Ivy: most recently added resolver should be checked first.
        repos.add(0, resolver)
    }

    @Override
    void setLoggingLevel(int level) {
        // Maven Resolver uses SLF4J for logging, which is configured externally
        // We could potentially adjust logging here if needed, but for now
        // Maven Resolver logging is controlled via SLF4J configuration
        // Level mapping: 0=quiet/errors, 1=warn, 2=info, 3=verbose, 4=debug
    }

    static File getGroovyRoot() {
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

    static File getGrapeDir() {
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

    static File getGrapeCacheDir() {
        String prefix = System.getProperty('grape.prefix') ?: 'grapesM2'
        if (prefix.contains('/') || prefix.contains('\\')) {
            throw new RuntimeException("The grape prefix must not contain '/' or '\\' characters")
        }
        File cache = new File(getGrapeDir(), prefix)
        if (!cache.exists()) {
            cache.mkdirs()
        } else if (!cache.isDirectory()) {
            throw new RuntimeException("The grape cache dir $cache is not a directory")
        }
        cache
    }

    private ClassLoader chooseClassLoader(Map args) {
        ClassLoader loader = (ClassLoader) args.classLoader
        if (!isValidTargetClassLoader(loader)) {
            Class caller = args.refObject?.getClass() ?:
                ReflectionUtils.getCallingClass((int) args.calleeDepth ?: 1)
            loader = caller?.getClassLoader()
            while (loader && !isValidTargetClassLoader(loader)) {
                loader = loader.getParent()
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

    private static String detectOsName() {
        String osName = System.getProperty('os.name').toLowerCase()
        if (osName.contains('win')) {
            return 'windows'
        } else if (osName.contains('mac')) {
            return 'osx'
        } else if (osName.contains('linux')) {
            return 'linux'
        } else if (osName.contains('sunos')) {
            return 'sunos'
        } else if (osName.contains('freebsd')) {
            return 'freebsd'
        }
        // Default fallback
        return osName.replaceAll('[^a-zA-Z0-9]', '')
    }

    private static String detectOsArch() {
        String osArch = System.getProperty('os.arch').toLowerCase()
        if (osArch.matches('.*64.*')) {
            if (osArch.contains('aarch64') || osArch.contains('arm64')) {
                return 'aarch_64'
            }
            return 'x86_64'
        }
        if (osArch.matches('.*86.*') || osArch == 'i386') {
            return 'x86'
        }
        if (osArch.contains('ppc64')) {
            return 'ppc64'
        }
        if (osArch.contains('ppc')) {
            return 'ppc'
        }
        if (osArch.contains('s390')) {
            return 's390'
        }
        if (osArch.contains('sparc')) {
            return 'sparc'
        }
        // Default fallback
        return osArch.replaceAll('[^a-zA-Z0-9]', '')
    }

    private boolean isMissingJarButPomExists(RepositorySystem system, RepositorySystemSession session, Artifact artifact) {
        if (artifact == null) return false
        // We only treat missing jar-like artifacts as ignorable if corresponding POM exists.
        if (artifact.extension != 'jar') return false
        try {
            Artifact pomArtifact = new DefaultArtifact(artifact.groupId, artifact.artifactId, 'pom', artifact.version)
            ArtifactRequest pomReq = new ArtifactRequest(pomArtifact, repos, null)
            ArtifactResult pomRes = system.resolveArtifact(session, pomReq)
            return pomRes?.resolved
        } catch (Exception ignore) {
            return false
        }
    }

    private boolean isIgnorableUnresolvedArtifact(RepositorySystem system,
                                                  RepositorySystemSession session,
                                                  Artifact rootArtifact,
                                                  Artifact missingArtifact) {
        if (missingArtifact == null) return false
        // Never ignore unresolved root artifact.
        if (rootArtifact != null
            && missingArtifact.groupId == rootArtifact.groupId
            && missingArtifact.artifactId == rootArtifact.artifactId
            && missingArtifact.version == rootArtifact.version) {
            return false
        }
        // Ignore classifier/native variants that are often platform-specific/optional.
        if (missingArtifact.classifier) return true
        // Ignore POM-only coordinates requested as jars.
        if (isMissingJarButPomExists(system, session, missingArtifact)) return true
        // For transitive-only misses, keep Ivy-like lenient behavior and continue.
        true
    }

    private static RuntimeException asRuntimeGrabError(Exception e) {
        if (e instanceof RuntimeException) return (RuntimeException) e
        String msg = e.message ?: e.class.name
        return new RuntimeException("Error grabbing Grapes -- ${msg}", e)
    }

}

record MavenGrabRecord(
    String groupId,
    String module,
    String version,
    List<String> conf,
    String ext,
    String type,
    String classifier,
    boolean force,
    boolean changing,
    boolean transitive
) {}
