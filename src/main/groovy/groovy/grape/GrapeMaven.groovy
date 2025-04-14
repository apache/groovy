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

import groovy.transform.AutoFinal
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.NamedParam
import groovy.transform.NamedParams
import org.codehaus.groovy.reflection.ReflectionUtils
import org.eclipse.aether.RepositorySystem
import org.eclipse.aether.RepositorySystemSession
import org.eclipse.aether.artifact.Artifact
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.collection.CollectRequest
import org.eclipse.aether.graph.Dependency
import org.eclipse.aether.graph.DependencyFilter
import org.eclipse.aether.repository.LocalRepository
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.repository.RepositoryPolicy
import org.eclipse.aether.resolution.ArtifactResult
import org.eclipse.aether.resolution.DependencyRequest
import org.eclipse.aether.supplier.RepositorySystemSupplier
import org.eclipse.aether.util.artifact.JavaScopes
import org.eclipse.aether.util.filter.DependencyFilterUtils

/**
 * Implementation supporting {@code @Grape} and {@code @Grab} annotations based on Maven.
 */
@AutoFinal
@CompileStatic
class GrapeMaven implements GrapeEngine {
    // weak hash map so we don't leak loaders directly
    final Map<ClassLoader, Set<MavenGrabRecord>> loadedDeps = [] as WeakHashMap
    /** Stores the MavenGrabRecord(s) for all dependencies in each grab() call. */
    final Set<MavenGrabRecord> grabRecordsForCurrDependencies = [] as LinkedHashSet
    boolean enableGrapes = true
    final List<RemoteRepository> repos = [
        new RemoteRepository.Builder("central", "default", "https://repo.maven.apache.org/maven2/").build()
    ]

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
        println "GrapeMaven.grab" // TODO remove debug
        try (RepositorySystem system = new RepositorySystemSupplier().get()) {
            def localRepo = new LocalRepository(getGrapeCacheDir().toURI())
            dependencies.each { Map dep ->
                try (RepositorySystemSession.CloseableSession session = system
                    .createSessionBuilder()
                    .withLocalRepositories(localRepo)
                    .setChecksumPolicy(RepositoryPolicy.CHECKSUM_POLICY_FAIL)
                    .build()) {
                    println "GrapeMaven.grab $dep" // TODO remove debug
                    String module = dep.module ?: dep.artifactId ?: dep.artifact
                    if (!module) {
                        throw new RuntimeException('grab requires at least a module: or artifactId: or artifact: argument')
                    }
                    String groupId = dep.group ?: dep.groupId ?: dep.organisation ?: dep.organization ?: dep.org ?: ''
                    String version = dep.version ?: dep.revision ?: dep.rev ?: '*'
                    if (version == '*') version = 'latest.default'
                    String classifier = dep.classifier ?: ''
                    String ext = dep.ext ?: dep.type ?: 'jar'
                    String type = dep.type ?: ''
                    println "GrapeMaven.grab $groupId $module $classifier $ext $version"

                    Artifact artifact = new DefaultArtifact(groupId, module, classifier, ext, version)

                    DependencyFilter classpathFilter = DependencyFilterUtils.classpathFilter(JavaScopes.COMPILE)

                    CollectRequest collectRequest = new CollectRequest(root: new Dependency(artifact, JavaScopes.COMPILE), repositories: repos)

                    DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, classpathFilter)

                    List<ArtifactResult> artifactResults =
                        system.resolveDependencies(session, dependencyRequest).getArtifactResults()

                    for (ArtifactResult found : artifactResults) {
                        println "$found resolved to $found.localArtifactResult.path"
                    }
                }
            }
        }
    }

    @Override
    @CompileDynamic
    Map<String, Map<String, List<String>>> enumerateGrapes() {
        Map<String, Map<String, List<String>>> bunches = [:].withDefault { [:].withDefault { [] } }
        String grapeCacheBase = grapeCacheDir.canonicalPath + File.separator
        grapeCacheDir.eachFileRecurse { File f ->
            if (f.name.endsWith('.jar')) {
                def version = f.parentFile.name
                def module = f.parentFile.parentFile.name
                def group = f.parentFile.parentFile.parentFile.canonicalPath - grapeCacheBase
                if (f.baseName == "$module-${version}") {
                    bunches[group.replace(File.separatorChar, '.' as char)][module] << version
                }
            }
        }
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
        dependencies.each { Map dep ->
            MavenGrabRecord mgr = createGrabRecord(dep)
            grabRecordsForCurrDependencies.add(mgr)
            localDeps.add(mgr)
        }

        List<URI> results = []
        results as URI[]
    }

    MavenGrabRecord createGrabRecord(Map dep) {
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
//            Set<String> badArgs = MUTUALLY_EXCLUSIVE_KEYS[key]
//            if (badArgs && !badArgs.disjoint(keys)) {
//                throw new RuntimeException("Grab: mutually exclusive arguments: ${keys.intersect(badArgs) + key}")
//            }
        }

        String groupId = dep.group ?: dep.groupId ?: dep.organisation ?: dep.organization ?: dep.org ?: ''
        String version = dep.version ?: dep.revision ?: dep.rev ?: '*'
        if (version == '*') version = 'latest.default'
        String classifier = dep.classifier ?: null
        String ext = dep.ext ?: dep.type ?: ''
        String type = dep.type ?: ''

//        ModuleRevisionId mrid = ModuleRevisionId.newInstance(groupId, module, version)

        boolean force = dep.containsKey('force') ? dep.force : true
        boolean changing = dep.containsKey('changing') ? dep.changing : false
        boolean transitive = dep.containsKey('transitive') ? dep.transitive : true

        new MavenGrabRecord(/*mrid: mrid, conf: getConfList(dep), force: force, changing: changing, transitive: transitive,*/ ext: ext, type: type, classifier: classifier)
    }

    @Override
    Map[] listDependencies(ClassLoader classLoader) {
        List<? extends Map> results = loadedDeps[classLoader]?.collect { MavenGrabRecord grabbed ->

            def dep = [:
//                group  : grabbed.mrid.getOrganisation(),
//                module : grabbed.mrid.getName(),
//                version: grabbed.mrid.getRevision()
            ]
/*
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
*/
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
        @NamedParam(value = 'name', type = String, required = true),
        @NamedParam(value = 'root', type = String, required = true),
        @NamedParam(value = 'm2Compatible', type = Boolean, required = false)
    ]) Map<String, Object> args) {
        repos << new RemoteRepository.Builder(
            (String) args.name,
            "default",
            (String) args.root
//            settings: (ResolverSettings) settings,
//            m2compatible: (boolean) args.getOrDefault('m2Compatible', Boolean.TRUE)
        ).build()
    }

    static File getGroovyRoot() {
        println "GrapeMaven.getGroovyRoot" // TODO remove debug
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
        println "GrapeMaven.getGrapeDir" // TODO remove debug
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
        File cache = new File(getGrapeDir(), 'grapesM2')
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

}

@CompileStatic
@EqualsAndHashCode
class MavenGrabRecord {
//    ModuleRevisionId mrid
//    List<String> conf
//    ArtifactType type
    String ext
    String type
    String classifier
//    boolean force
//    boolean changing
//    boolean transitive
}
