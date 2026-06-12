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

import groovy.transform.CompileStatic
import org.apache.ivy.core.module.descriptor.DependencyDescriptor
import org.apache.ivy.core.module.id.ModuleRevisionId
import org.apache.ivy.core.resolve.ResolveData
import org.apache.ivy.plugins.resolver.FileSystemResolver
import org.apache.ivy.plugins.resolver.util.ResolvedResource

import java.nio.file.Files
import java.util.jar.JarFile
import java.util.logging.Level
import java.util.logging.Logger
import java.util.zip.ZipException

/**
 * {@link FileSystemResolver} subclass that hardens Grape's {@code cachedGrapes}
 * resolver against stub-shaped synthetic descriptors.
 *
 * <p>Grape's default {@code cachedGrapes} (a plain {@code <filesystem>}) synthesises
 * a minimal ivy descriptor when it finds a JAR but no ivy file alongside —
 * {@code <publications>} only, no {@code <dependencies>}. The synthetic descriptor
 * is then written back to disk for next time, so the first artifact-only state
 * perpetuates itself. Any module hit via the chain after that returns only the
 * JAR (no transitive deps), causing runtime classloading failures — e.g. when
 * log4j-core's JAR is present but log4j-api is missing, calls into the log4j API
 * raise {@code NoClassDefFoundError} / {@code MissingPropertyException}.</p>
 *
 * <p>This resolver guards three failure modes:</p>
 * <ol>
 *   <li>Sets {@code descriptor=required} in the constructor — prevents <em>new</em>
 *       stub synthesis. If the JAR exists but no ivy descriptor, the resolver
 *       returns null and the chain falls through to localm2/ibiblio where the
 *       real POM gets parsed.</li>
 *   <li>Overrides {@link #findIvyFileRef} to reject existing stubs — if an ivy
 *       descriptor exists but its companion {@code ivy-<rev>.xml.original} (the
 *       original POM) does not, treats the descriptor as if not present. The
 *       {@code .original} file is Ivy's record that the descriptor was downloaded
 *       from a real repo; its absence is the load-bearing signal that we're
 *       looking at a synthesised stub rather than a real cached descriptor. This
 *       silently self-heals existing stub corruption.</li>
 *   <li>Validates the cached primary JAR's zip integrity. If the JAR exists but
 *       {@link JarFile} can't open it (e.g. truncated download from a CDN 429 or
 *       interrupted process), evicts the JAR + descriptor + {@code .original}
 *       and returns null. The chain falls through to localm2/ibiblio and Ivy
 *       re-downloads a fresh copy in the same {@code @Grab} call — no second
 *       grab needed. Catching corruption here, rather than at classloading time,
 *       keeps corrupt JARs off the classpath entirely.</li>
 * </ol>
 *
 * <p>Strict behaviour is unconditional — if this resolver is in use, both checks
 * run. Operators who don't want them can simply not declare this resolver in
 * their {@code ~/.groovy/grapeConfig.xml}, dropping back to the stock Ivy
 * {@code <filesystem>} resolver. Stub-rejection skips snapshot revisions
 * automatically (Maven snapshot caches use timestamped filenames); integrity
 * validation does not — corruption is corruption.</p>
 */
@CompileStatic
class StrictCachedGrapesResolver extends FileSystemResolver {

    private static final Logger LOGGER = Logger.getLogger(StrictCachedGrapesResolver.name)

    StrictCachedGrapesResolver() {
        // Prevent the in-memory synthesis path that creates stubs from
        // artifact-only state. With descriptor="required", a missing ivy file
        // makes the resolver return null and the chain proceeds to the next
        // resolver. (This replaces the deprecated allownomd=false setter.)
        setDescriptor(DESCRIPTOR_REQUIRED)
    }

    @Override
    ResolvedResource findIvyFileRef(DependencyDescriptor dd, ResolveData data) {
        ResolvedResource ref = super.findIvyFileRef(dd, data)
        if (ref == null) return null
        ModuleRevisionId mrid = dd.getDependencyRevisionId()
        File ivyFile = resolvedRefAsFile(ref)
        if (shouldRejectAsStub(mrid, ivyFile)) {
            return null
        }
        if (shouldRejectAsCorruptArtifact(mrid, ivyFile)) {
            invalidateCacheEntry(ivyFile, mrid)
            return null
        }
        return ref
    }

    /**
     * Visible for testing: decide whether to treat a cached ivy descriptor as a
     * stub and reject it. Returns true iff the revision is not a snapshot, the
     * ivy file's location is known, and no companion {@code ivy-<rev>.xml.original}
     * file exists alongside it.
     */
    boolean shouldRejectAsStub(ModuleRevisionId mrid, File ivyFile) {
        if (mrid == null) return false
        String rev = mrid.revision
        if (rev != null && rev.endsWith('-SNAPSHOT')) return false
        if (ivyFile == null) return false
        File original = new File(ivyFile.parentFile, ivyFile.name + '.original')
        return !original.exists()
    }

    /**
     * Visible for testing: decide whether to treat a cached module as having a
     * corrupt primary JAR (truncated, zero bytes, garbage). Returns true iff the
     * JAR is locatable on disk in the cachedGrapes layout and opening it as a
     * {@link JarFile} raises {@link ZipException}. Unrelated I/O errors (e.g.
     * permissions) return false — we only reject on clear zip-format corruption.
     */
    boolean shouldRejectAsCorruptArtifact(ModuleRevisionId mrid, File ivyFile) {
        if (mrid == null) return false
        File jar = locatePrimaryJar(ivyFile, mrid)
        if (jar == null || !jar.isFile()) return false
        try (JarFile jf = new JarFile(jar)) {
            jf.entries()
            return false
        } catch (ZipException ignored) {
            return true
        } catch (IOException ignored) {
            return false
        }
    }

    /**
     * Visible for testing: extract the on-disk File for a resolved ivy resource,
     * or null if the resource is not file-backed.
     */
    static File resolvedRefAsFile(ResolvedResource ref) {
        try {
            String name = ref.resource.name
            if (name?.startsWith('file:')) {
                String path = name.substring('file:'.length()).replaceAll(/\/+/, '/')
                return new File(path)
            }
        } catch (Exception ignored) {
            // fall through
        }
        null
    }

    /**
     * Visible for testing: locate the primary JAR for a module revision in the
     * cachedGrapes layout ({@code <module-dir>/jars/<name>-<rev>.jar}), given the
     * ivy descriptor file. Returns null if any layout assumption fails.
     */
    static File locatePrimaryJar(File ivyFile, ModuleRevisionId mrid) {
        if (ivyFile == null || mrid == null) return null
        File parent = ivyFile.parentFile
        if (parent == null) return null
        File jarsDir = new File(parent, 'jars')
        if (!jarsDir.isDirectory()) return null
        new File(jarsDir, "${mrid.name}-${mrid.revision}.jar")
    }

    private static void invalidateCacheEntry(File ivyFile, ModuleRevisionId mrid) {
        File jar = locatePrimaryJar(ivyFile, mrid)
        if (jar != null && jar.exists()) {
            LOGGER.log(Level.WARNING,
                'Evicting corrupt cached JAR (next resolve will re-download): ' + jar)
            tryDelete(jar)
        }
        // Drop the descriptor + .original too, so the chain doesn't bind back to
        // cachedGrapes on the next pass — the next chain run finds nothing here
        // and falls through to localm2/ibiblio for a fresh download.
        tryDelete(ivyFile)
        if (ivyFile != null) {
            tryDelete(new File(ivyFile.parentFile, ivyFile.name + '.original'))
        }
    }

    private static void tryDelete(File f) {
        if (f == null || !f.exists()) return
        try {
            Files.deleteIfExists(f.toPath())
        } catch (Exception e) {
            LOGGER.log(Level.FINE, 'Could not delete cached file: ' + f, e)
        }
    }
}
