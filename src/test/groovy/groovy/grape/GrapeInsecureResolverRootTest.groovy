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
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.shouldFail

/**
 * Tests the classification behind the warning Grape logs for plaintext resolver roots.
 * The classification is shared by both engines because every documented route to adding a
 * resolver -- {@code @GrabResolver}, the {@code grape} command line tool and
 * {@link Grape#addResolver(java.util.Map)} -- passes through the same facade method.
 */
@CompileStatic
final class GrapeInsecureResolverRootTest {

    @Test
    void testPlaintextRemoteRootsAreInsecure() {
        assert Grape.isInsecureResolverRoot('http://repo.corp.example/maven2')
        assert Grape.isInsecureResolverRoot('ftp://repo.corp.example/maven2')
    }

    @Test
    void testSchemeComparisonIgnoresCase() {
        assert Grape.isInsecureResolverRoot('HTTP://repo.corp.example/maven2')
        assert !Grape.isInsecureResolverRoot('HTTPS://repo.corp.example/maven2')
    }

    @Test
    void testSurroundingWhitespaceIsIgnored() {
        assert Grape.isInsecureResolverRoot('  http://repo.corp.example/maven2  ')
    }

    @Test
    void testEncryptedRootsAreNotInsecure() {
        assert !Grape.isInsecureResolverRoot('https://repo.maven.apache.org/maven2/')
    }

    @Test
    void testLocalRootsAreNotInsecure() {
        // file: roots never cross a network, so the warning would be noise.
        assert !Grape.isInsecureResolverRoot('file:/home/dev/repo')
        assert !Grape.isInsecureResolverRoot(new File('build').toURI().toString())
    }

    @Test
    void testLoopbackRootsAreExempt() {
        // A local mirror or proxy over plaintext is not exposed in transit.
        assert !Grape.isInsecureResolverRoot('http://localhost:8081/repository/maven-public')
        assert !Grape.isInsecureResolverRoot('http://LocalHost:8081/repository/maven-public')
        assert !Grape.isInsecureResolverRoot('http://127.0.0.1:8081/repo')
        assert !Grape.isInsecureResolverRoot('http://127.1.2.3/repo')
        assert !Grape.isInsecureResolverRoot('http://[::1]:8081/repo')
    }

    @Test
    void testNonLoopbackLookalikesAreStillInsecure() {
        // Guard the prefix test against hosts that merely start with the same text.
        assert Grape.isInsecureResolverRoot('http://127.evil.example/repo')
        assert Grape.isInsecureResolverRoot('http://localhost.evil.example/repo')
        // A dotted quad with an out-of-range octet is not a valid host: URI.getHost() returns
        // null for it, so it is treated as insecure rather than exempted as loopback.
        assert Grape.isInsecureResolverRoot('http://127.999.999.999/repo')
        assert Grape.isInsecureResolverRoot('http://127.0.0.256/repo')
    }

    @Test
    void testUnusableRootsAreLeftToTheEngine() {
        assert !Grape.isInsecureResolverRoot(null)
        assert !Grape.isInsecureResolverRoot('')
        assert !Grape.isInsecureResolverRoot('not a uri at all')
        assert !Grape.isInsecureResolverRoot('repo.corp.example/maven2') // no scheme
    }

    @Test
    void testUnknownSchemesAreNotReported() {
        // Deliberate: an allow-list of known-plaintext schemes, so encrypted transports such
        // as s3 and gs are not reported falsely. See the isInsecureResolverRoot javadoc.
        assert !Grape.isInsecureResolverRoot('s3://corp-artifacts/maven2')
        assert !Grape.isInsecureResolverRoot('gs://corp-artifacts/maven2')
    }

    // --- policy selection ---

    @Test
    void testPolicyDefaultsToWarn() {
        withPolicy(null) {
            assert Grape.insecureProtocolPolicy() == 'warn'
        }
    }

    @Test
    void testPolicyValuesAreRecognised() {
        withPolicy('fail') { assert Grape.insecureProtocolPolicy() == 'fail' }
        withPolicy('warn') { assert Grape.insecureProtocolPolicy() == 'warn' }
        withPolicy('ignore') { assert Grape.insecureProtocolPolicy() == 'ignore' }
    }

    @Test
    void testPolicyIsCaseInsensitiveAndTrimmed() {
        withPolicy('  FAIL  ') { assert Grape.insecureProtocolPolicy() == 'fail' }
    }

    @Test
    void testUnrecognisedPolicyFallsBackToWarnNotIgnore() {
        // A typo must not silently disable the check, so the fallback is the stricter of the
        // two non-failing policies.
        withPolicy('flase') { assert Grape.insecureProtocolPolicy() == 'warn' }
        withPolicy('true') { assert Grape.insecureProtocolPolicy() == 'warn' }
    }

    // --- policy application ---

    // These drive checkResolverRootProtocol directly rather than through addResolver, which would
    // mutate the JVM-global resolver list (there is no removeResolver) and leak into other tests.

    @Test
    void testFailPolicyRejectsPlaintextRoot() {
        withPolicy('fail') {
            def ex = shouldFail(RuntimeException) {
                Grape.checkResolverRootProtocol([name: 'corp', root: 'http://repo.corp.example/maven2'] as Map<String, Object>)
            }
            assert ex.message.contains('plaintext root')
            assert ex.message.contains(Grape.INSECURE_PROTOCOL_POLICY_SYSTEM_PROPERTY)
        }
    }

    @Test
    void testFailPolicyAllowsSecureLoopbackAndFileRoots() {
        withPolicy('fail') {
            // none of these is a plaintext remote root, so the check passes (no exception)
            Grape.checkResolverRootProtocol([name: 'secure', root: 'https://repo.corp.example/maven2'] as Map<String, Object>)
            Grape.checkResolverRootProtocol([name: 'local', root: 'http://localhost:8081/repo'] as Map<String, Object>)
            Grape.checkResolverRootProtocol([name: 'onDisk', root: 'file:/home/dev/repo'] as Map<String, Object>)
        }
    }

    @Test
    void testIgnorePolicyAcceptsPlaintextRoot() {
        withPolicy('ignore') {
            Grape.checkResolverRootProtocol([name: 'corp', root: 'http://repo.corp.example/maven2'] as Map<String, Object>)
        }
    }

    // addResolver must apply the policy before adding the resolver, so a rejected root never
    // reaches the engine. This is the one path that goes through the public facade, and because
    // it throws before the resolver is added it does not mutate the global engine.
    @Test
    void testAddResolverAppliesPolicyBeforeAdding() {
        withPolicy('fail') {
            shouldFail(RuntimeException) {
                Grape.addResolver([name: 'corp', root: 'http://repo.corp.example/maven2'] as Map<String, Object>)
            }
        }
    }

    private static void withPolicy(String value, Closure body) {
        String property = Grape.INSECURE_PROTOCOL_POLICY_SYSTEM_PROPERTY
        String previous = System.getProperty(property)
        if (value == null) {
            System.clearProperty(property)
        } else {
            System.setProperty(property, value)
        }
        try {
            body()
        } finally {
            if (previous == null) {
                System.clearProperty(property)
            } else {
                System.setProperty(property, previous)
            }
        }
    }
}
