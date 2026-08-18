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

import org.eclipse.aether.DefaultRepositorySystemSession
import org.eclipse.aether.RepositorySystemSession
import org.eclipse.aether.internal.impl.DefaultChecksumPolicyProvider
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.repository.RepositoryPolicy
import org.eclipse.aether.spi.connector.checksum.ChecksumPolicy
import org.eclipse.aether.spi.connector.checksum.ChecksumPolicy.ChecksumKind
import org.eclipse.aether.transfer.ChecksumFailureException
import org.eclipse.aether.transfer.TransferResource
import org.junit.jupiter.api.Test

import java.util.function.Function

import static groovy.test.GroovyAssert.shouldFail

/**
 * Tests that Grape's checksum policy rejects mismatched artifacts while tolerating artifacts
 * for which no checksum is published.
 */
final class GrapeChecksumPolicyProviderTest {

    private static final GrapeChecksumPolicyProvider PROVIDER =
        new GrapeChecksumPolicyProvider(new DefaultChecksumPolicyProvider())

    private static final RepositorySystemSession SESSION =
        new DefaultRepositorySystemSession({ Runnable r -> Boolean.FALSE } as Function)

    private static final RemoteRepository REPOSITORY =
        new RemoteRepository.Builder('test', 'default', 'https://repo.example.invalid/maven2').build()

    private static final TransferResource RESOURCE = new TransferResource(
        'test', 'https://repo.example.invalid/maven2', 'org/example/demo/1.0/demo-1.0.jar', null, null, null)

    private static ChecksumPolicy policyFor(String policy) {
        PROVIDER.newChecksumPolicy(SESSION, REPOSITORY, RESOURCE, policy)
    }

    private static ChecksumFailureException mismatch() {
        ChecksumFailureException.mismatch('expected', ChecksumKind.REMOTE_EXTERNAL.name(), 'actual')
    }

    @Test
    void testAbsentChecksumIsToleratedUnderFail() {
        ChecksumPolicy policy = policyFor(RepositoryPolicy.CHECKSUM_POLICY_FAIL)
        // Grape accepts the artifact, matching the Ivy-backed engine.
        policy.onNoMoreChecksums()

        // Guard the premise: the stock policy this one wraps rejects the same situation, so
        // the test above is meaningful and will start failing if Maven Resolver ever relaxes
        // CHECKSUM_POLICY_FAIL itself.
        ChecksumPolicy stock = new DefaultChecksumPolicyProvider()
            .newChecksumPolicy(SESSION, REPOSITORY, RESOURCE, RepositoryPolicy.CHECKSUM_POLICY_FAIL)
        shouldFail(ChecksumFailureException) {
            stock.onNoMoreChecksums()
        }
    }

    @Test
    void testMismatchedChecksumStillFailsUnderFail() {
        ChecksumPolicy policy = policyFor(RepositoryPolicy.CHECKSUM_POLICY_FAIL)

        // A mismatch is rejected: the wrapped fail policy throws, carrying the mismatch detail.
        ChecksumFailureException thrown = shouldFail(ChecksumFailureException) {
            policy.onChecksumMismatch('SHA-1', ChecksumKind.REMOTE_EXTERNAL, mismatch())
        } as ChecksumFailureException
        assert thrown.expected == 'expected'
        assert thrown.actual == 'actual'

        // A mismatch must not be accepted even after the retry opportunity.
        assert !policy.onTransferChecksumFailure(mismatch())
    }

    @Test
    void testUnreadableChecksumIsRejectedUnderFail() {
        // A published checksum that could not be validated (an error, not an absence) must not be
        // tolerated: only a genuine absence is. Maven Resolver signals the error via
        // onChecksumError before reaching onNoMoreChecksums.
        ChecksumPolicy policy = policyFor(RepositoryPolicy.CHECKSUM_POLICY_FAIL)
        policy.onChecksumError('SHA-1', ChecksumKind.REMOTE_EXTERNAL,
            ChecksumFailureException.processingFailure('unreadable', new IOException('boom')))
        shouldFail(ChecksumFailureException) {
            policy.onNoMoreChecksums()
        }
    }

    @Test
    void testMatchingChecksumIsAcceptedUnderFail() {
        ChecksumPolicy policy = policyFor(RepositoryPolicy.CHECKSUM_POLICY_FAIL)
        assert policy.onChecksumMatch('SHA-1', ChecksumKind.REMOTE_EXTERNAL)
    }

    @Test
    void testWarnPolicyIsLeftUnwrapped() {
        ChecksumPolicy policy = policyFor(RepositoryPolicy.CHECKSUM_POLICY_WARN)
        // Stock warn semantics: a failure is logged and the artifact accepted anyway.
        assert policy.onTransferChecksumFailure(mismatch())
        policy.onNoMoreChecksums()
    }

    @Test
    void testIgnorePolicyRemainsNull() {
        assert policyFor(RepositoryPolicy.CHECKSUM_POLICY_IGNORE) == null
    }

    @Test
    void testEffectiveChecksumPolicyIsDelegated() {
        assert PROVIDER.getEffectiveChecksumPolicy(SESSION,
            RepositoryPolicy.CHECKSUM_POLICY_FAIL,
            RepositoryPolicy.CHECKSUM_POLICY_WARN) == RepositoryPolicy.CHECKSUM_POLICY_WARN
        assert PROVIDER.getEffectiveChecksumPolicy(SESSION,
            RepositoryPolicy.CHECKSUM_POLICY_WARN,
            RepositoryPolicy.CHECKSUM_POLICY_IGNORE) == RepositoryPolicy.CHECKSUM_POLICY_IGNORE
    }
}
