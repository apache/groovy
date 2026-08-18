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

import groovy.transform.AutoFinal
import groovy.transform.CompileStatic
import org.eclipse.aether.RepositorySystemSession
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.repository.RepositoryPolicy
import org.eclipse.aether.spi.connector.checksum.ChecksumPolicy
import org.eclipse.aether.spi.connector.checksum.ChecksumPolicy.ChecksumKind
import org.eclipse.aether.spi.connector.checksum.ChecksumPolicyProvider
import org.eclipse.aether.transfer.ChecksumFailureException
import org.eclipse.aether.transfer.TransferResource
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Supplies the checksum policy Grape uses for remote artifact downloads.
 *
 * <p>Maven Resolver's stock {@code CHECKSUM_POLICY_FAIL} rejects a download both when a
 * published checksum does not match and when no checksum is published at all. Grape wants
 * only the first of those: a mismatch means the bytes are not what the repository said they
 * would be and must never reach the classpath, whereas a repository that simply publishes no
 * {@code .sha1}/{@code .md5} is a common and legitimate situation, particularly for internal
 * and older repositories.
 *
 * <p>This provider therefore delegates to the stock {@link ChecksumPolicyProvider} for every
 * policy and wraps only the {@code fail} policy, relaxing its
 * {@link ChecksumPolicy#onNoMoreChecksums()} response. The result matches the semantics the
 * Ivy-backed engine has always had, so both Grape engines behave alike.
 *
 * @since 6.0.0
 */
@AutoFinal
@CompileStatic
class GrapeChecksumPolicyProvider implements ChecksumPolicyProvider {

    private final ChecksumPolicyProvider delegate

    /**
     * Wraps the stock checksum policy provider.
     *
     * @param delegate the stock provider whose policies are wrapped
     */
    GrapeChecksumPolicyProvider(ChecksumPolicyProvider delegate) {
        this.delegate = delegate
    }

    /**
     * Returns the policy for the given resource, relaxing {@code fail} to tolerate artifacts
     * that publish no checksum.
     *
     * @param session the session during which the request is made
     * @param repository the repository hosting the resource
     * @param resource the resource the policy will be applied to
     * @param policy the identifier of the policy to apply
     * @return the policy to apply, or {@code null} if checksums should be ignored
     */
    @Override
    ChecksumPolicy newChecksumPolicy(RepositorySystemSession session, RemoteRepository repository, TransferResource resource, String policy) {
        ChecksumPolicy checksumPolicy = delegate.newChecksumPolicy(session, repository, resource, policy)
        if (checksumPolicy != null && RepositoryPolicy.CHECKSUM_POLICY_FAIL == policy) {
            return new AbsenceTolerantChecksumPolicy(checksumPolicy, resource)
        }
        checksumPolicy
    }

    /**
     * Returns the least strict of the two supplied policies.
     *
     * @param session the session during which the request is made
     * @param policy1 a policy to compare
     * @param policy2 a policy to compare
     * @return the least strict policy of the two
     */
    @Override
    String getEffectiveChecksumPolicy(RepositorySystemSession session, String policy1, String policy2) {
        delegate.getEffectiveChecksumPolicy(session, policy1, policy2)
    }

    /**
     * Wraps a checksum policy so that the absence of any published checksum is tolerated while
     * every other outcome, in particular a mismatch, is left to the wrapped policy.
     */
    @AutoFinal
    @CompileStatic
    private static class AbsenceTolerantChecksumPolicy implements ChecksumPolicy {

        private static final Logger LOG = LoggerFactory.getLogger(AbsenceTolerantChecksumPolicy)

        private final ChecksumPolicy delegate
        private final TransferResource resource

        // Set when a checksum was published but could not be validated (a fetch, IO or
        // calculation error, as opposed to a checksum simply not being published). A genuine
        // absence never reaches onChecksumError; Maven Resolver's ChecksumValidator only calls
        // it on an error, and skips a missing checksum silently. Reset per transfer attempt.
        private boolean checksumErrorSeen

        AbsenceTolerantChecksumPolicy(ChecksumPolicy delegate, TransferResource resource) {
            this.delegate = delegate
            this.resource = resource
        }

        @Override
        boolean onChecksumMatch(String algorithm, ChecksumKind kind) {
            delegate.onChecksumMatch(algorithm, kind)
        }

        @Override
        void onChecksumMismatch(String algorithm, ChecksumKind kind, ChecksumFailureException exception) throws ChecksumFailureException {
            delegate.onChecksumMismatch(algorithm, kind, exception)
        }

        @Override
        void onChecksumError(String algorithm, ChecksumKind kind, ChecksumFailureException exception) throws ChecksumFailureException {
            checksumErrorSeen = true
            delegate.onChecksumError(algorithm, kind, exception)
        }

        /**
         * Accepts the download only when no checksum was published at all; a checksum that was
         * published but could not be validated is left to the wrapped {@code fail} policy, which
         * rejects it.
         *
         * <p>This is the single point where the policy departs from the wrapped {@code fail}
         * policy, and only for a genuine absence: tolerating a repository that publishes no
         * checksum is the intent, whereas a checksum present but unreadable is closer to a
         * mismatch than to an absence and must not be accepted unverified.
         */
        @Override
        void onNoMoreChecksums() throws ChecksumFailureException {
            if (checksumErrorSeen) {
                delegate.onNoMoreChecksums() // fail policy: rejects the unverifiable artifact
            }
            LOG.debug('No checksum published for {}{}; accepting the artifact unverified',
                resource.repositoryUrl, resource.resourceName)
        }

        @Override
        void onTransferRetry() {
            checksumErrorSeen = false // each attempt re-validates from scratch
            delegate.onTransferRetry()
        }

        @Override
        boolean onTransferChecksumFailure(ChecksumFailureException exception) {
            delegate.onTransferChecksumFailure(exception)
        }
    }
}
