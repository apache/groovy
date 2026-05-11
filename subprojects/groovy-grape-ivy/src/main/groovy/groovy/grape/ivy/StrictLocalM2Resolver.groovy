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
import org.apache.ivy.plugins.resolver.IBiblioResolver
import org.apache.ivy.plugins.resolver.util.ResolvedResource

import java.util.regex.Pattern

/**
 * IBiblioResolver subclass that, for a local Maven repository (file://), validates
 * that the primary artifact actually exists alongside the POM before reporting
 * the descriptor as found. Without this, a half-populated local m2 entry (POM
 * present, JAR missing — common after Apache release-vote staging workflows or
 * partial Maven downloads) causes Ivy's chain to bind resolution to localm2 and
 * then fail to download the missing JAR, never falling through to Maven Central.
 *
 * Override is on findIvyFileRef rather than getDependency to avoid grape-cache
 * poisoning: returning null at descriptor-lookup time prevents Ivy from caching
 * the descriptor with resolver=localm2.
 *
 * Strictness is gated by -Dgroovy.grape.strict-localm2=true (default false for
 * this initial release; planned to flip to default-true in 6.1, drop the flag
 * in 7.0). Strictness is also automatically skipped for snapshot revisions
 * (Maven uses timestamp-suffixed filenames there), for non-m2-compatible
 * configurations, and when the resolver root is not a file URL.
 */
@CompileStatic
class StrictLocalM2Resolver extends IBiblioResolver {

    private static final String ENABLE_PROPERTY = 'groovy.grape.strict-localm2'

    /** Maven packaging values whose primary artifact has a non-jar extension. */
    private static final Map<String, String> NON_JAR_PACKAGING_EXT = [
        'war': 'war',
        'ear': 'ear',
        'aar': 'aar',
        'rar': 'rar',
        'zip': 'zip',
    ].asImmutable()

    private static final Pattern PACKAGING_PATTERN =
        ~/(?ms)<packaging>\s*([\w-]+)\s*<\/packaging>/

    @Override
    ResolvedResource findIvyFileRef(DependencyDescriptor dd, ResolveData data) {
        ResolvedResource pom = super.findIvyFileRef(dd, data)
        if (pom == null) return null
        ModuleRevisionId mrid = dd.getDependencyRevisionId()
        File pomFile = resolvedPomAsFile(pom)
        if (shouldRejectAsHalfPopulated(mrid, pomFile)) {
            return null
        }
        return pom
    }

    /**
     * Visible for testing: decide whether to reject this localm2 lookup as
     * half-populated. Returns true iff strictness is enabled, the revision is
     * not a snapshot, the resolver root is a file URL, and no primary artifact
     * matching the POM's packaging exists alongside the POM.
     */
    boolean shouldRejectAsHalfPopulated(ModuleRevisionId mrid, File pomFile) {
        if (!shouldEnforce()) return false
        if (mrid == null) return false
        String rev = mrid.revision
        if (rev != null && rev.endsWith('-SNAPSHOT')) return false
        File m2dir = computeM2Dir(mrid)
        if (m2dir == null) return false
        File jar = new File(m2dir, "${mrid.name}-${rev}.jar")
        if (jar.exists()) return false
        String packaging = (pomFile != null && pomFile.isFile()) ? readPackaging(pomFile) : null
        if (packaging == 'pom') return false
        String ext = NON_JAR_PACKAGING_EXT.getOrDefault(packaging, 'jar')
        File primary = new File(m2dir, "${mrid.name}-${rev}.${ext}")
        return !primary.exists()
    }

    private boolean shouldEnforce() {
        if (!isM2compatible()) return false
        Boolean.parseBoolean(System.getProperty(ENABLE_PROPERTY, 'false'))
    }

    /**
     * Visible for testing: compute the local m2 directory for a module
     * revision based on the resolver's root.
     */
    File computeM2Dir(ModuleRevisionId mrid) {
        if (mrid == null) return null
        String rootStr = getRoot()
        if (rootStr == null || !rootStr.startsWith('file:')) return null
        // The rendered ${user.home.url} can produce double-slashes; normalize them.
        String path = rootStr.substring('file:'.length()).replaceAll(/\/+/, '/')
        File rootDir = new File(path)
        if (!rootDir.isDirectory()) return null
        new File(rootDir, "${mrid.organisation.replace('.', '/')}/${mrid.name}/${mrid.revision}")
    }

    /**
     * Visible for testing: read the {@code <packaging>} element from a POM file.
     * Returns null if the element is absent or the file cannot be read.
     */
    static String readPackaging(File pomFile) {
        try {
            String text = pomFile.getText('UTF-8')
            def matcher = PACKAGING_PATTERN.matcher(text)
            return matcher.find() ? matcher.group(1) : null
        } catch (Exception ignored) {
            return null
        }
    }

    private static File resolvedPomAsFile(ResolvedResource pom) {
        try {
            String name = pom.resource.name
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
