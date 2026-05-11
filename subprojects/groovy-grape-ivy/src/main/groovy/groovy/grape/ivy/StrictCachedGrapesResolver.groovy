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
 * <p>This resolver guards both directions:</p>
 * <ol>
 *   <li>Sets {@code allownomd=false} in the constructor — prevents <em>new</em>
 *       stub synthesis. If the JAR exists but no ivy descriptor, the resolver
 *       returns null and the chain falls through to localm2/ibiblio where the
 *       real POM gets parsed.</li>
 *   <li>Overrides {@link #findIvyFileRef} — if an ivy descriptor exists but its
 *       companion {@code ivy-<rev>.xml.original} (the original POM) does not,
 *       treats the descriptor as if not present. The {@code .original} file is
 *       Ivy's record that the descriptor was downloaded from a real repo; its
 *       absence is the load-bearing signal that we're looking at a synthesised
 *       stub rather than a real cached descriptor. This silently self-heals
 *       existing stub corruption.</li>
 * </ol>
 *
 * <p>Strictness is enabled by default. Disable with
 * {@code -Dgroovy.grape.strict-cached-grapes=false} if you maintain a custom
 * cache where hand-installed JAR + IVY descriptors legitimately lack the
 * {@code .original} POM. Strictness is also automatically skipped for snapshot
 * revisions, since Maven snapshot caches use timestamped filenames.</p>
 */
@CompileStatic
class StrictCachedGrapesResolver extends FileSystemResolver {

    private static final String ENABLE_PROPERTY = 'groovy.grape.strict-cached-grapes'

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
        return ref
    }

    /**
     * Visible for testing: decide whether to treat a cached ivy descriptor as a
     * stub and reject it. Returns true iff strictness is enabled, the revision
     * is not a snapshot, the ivy file's location is known, and no companion
     * {@code ivy-<rev>.xml.original} file exists alongside it.
     */
    boolean shouldRejectAsStub(ModuleRevisionId mrid, File ivyFile) {
        if (!shouldEnforce()) return false
        if (mrid == null) return false
        String rev = mrid.revision
        if (rev != null && rev.endsWith('-SNAPSHOT')) return false
        if (ivyFile == null) return false
        File original = new File(ivyFile.parentFile, ivyFile.name + '.original')
        return !original.exists()
    }

    private boolean shouldEnforce() {
        Boolean.parseBoolean(System.getProperty(ENABLE_PROPERTY, 'true'))
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
}
