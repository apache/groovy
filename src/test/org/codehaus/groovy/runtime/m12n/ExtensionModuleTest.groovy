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
package org.codehaus.groovy.runtime.m12n

/**
 * Unit tests for extension methods loading.
 */
class ExtensionModuleTest extends GroovyTestCase {

    // Apache License does not allow jars/binaries in the source
    private static final String moduleTest12JarContentsBase64Encoded =
'''UEsDBBQAAAAIALSK2UBQTDGungAAAEABAAA9ABwATUVUQS1JTkYvc2VydmljZXMvb3JnLmNvZGVo
YXVzLmdyb292eS5ydW50aW1lLkV4dGVuc2lvbk1vZHVsZVVUCQADBILoTwSC6E91eAsAAQTpAwAA
BOkDAACdjr8OgkAMh/d7ij6AHpGdyRAnXSTuJxS4CFfT9vzz9nJRGJyMU9P2931tlkHVe4HWDwhT
jYINKIGiKGiPgA/FIJ4CjNTEKdSyG/FOfDHvwWFqiyrF5wAx7NidP/sTcsKLjc3XyWoW43ZwIigF
cWdrarB3UWzHRLen5RjUj2jHTR5ssh+VfejKmc1XP1F7d10QI+rU1+Wf5xP7/YQxL1BLAwQKAAAA
AACpYaRAAAAAAAAAAAAAAAAAEgAcAE1FVEEtSU5GL3NlcnZpY2VzL1VUCQADvqujT9Wro091eAsA
AQTpAwAABOkDAABQSwMECgAAAAAAXZ6JPwAAAAAAAAAAAAAAAAkAHABNRVRBLUlORi9VVAkAA5FY
4k6eq6NPdXgLAAEE6QMAAATpAwAAUEsDBAoAAAAAAOxipEAAAAAAAAAAAAAAAAAhABwAb3JnL2Nv
ZGVoYXVzL2dyb292eS9ydW50aW1lL20xMm4vVVQJAAMbrqNPI66jT3V4CwABBOkDAAAE6QMAAFBL
AwQKAAAAAADhYqRAAAAAAAAAAAAAAAAAHAAcAG9yZy9jb2RlaGF1cy9ncm9vdnkvcnVudGltZS9V
VAkAAwauo08jrqNPdXgLAAEE6QMAAATpAwAAUEsDBAoAAAAAAOFipEAAAAAAAAAAAAAAAAAUABwA
b3JnL2NvZGVoYXVzL2dyb292eS9VVAkAAwauo08jrqNPdXgLAAEE6QMAAATpAwAAUEsDBAoAAAAA
AOFipEAAAAAAAAAAAAAAAAANABwAb3JnL2NvZGVoYXVzL1VUCQADBq6jTweuo091eAsAAQTpAwAA
BOkDAABQSwMECgAAAAAA4WKkQAAAAAAAAAAAAAAAAAQAHABvcmcvVVQJAAMGrqNPIK6jT3V4CwAB
BOkDAAAE6QMAAFBLAwQUAAAACADpYqRAiSnRV58BAAAAAwAAOwAcAG9yZy9jb2RlaGF1cy9ncm9v
dnkvcnVudGltZS9tMTJuL1Rlc3RTdHJpbmdFeHRlbnNpb24yLmNsYXNzVVQJAAMVrqNPI66jT3V4
CwABBOkDAAAE6QMAAJ1Sy07jQBCsTkIcjHeBQIDAPngTQMIi0ooDiAMITtbugSzSHp0whEGOB43H
EfwVXIIEEh/AR6GdsSMihLngQ/d0dVVPu+znl4cnANuo2ShhxkLVxizmbOTwzYTvJvyw8NPCPKG4
x0Ou9gn52vopoXAozhhh1OMh+x13mkw2/GagkbInWn5w6ktu6j5YUBc8Iux4QrbdllZe+HHktqUQ
3RtXxqHiHeZ2tuuh22CROlGSh+2ja8XCiIuwvkuYkKzLZMQa4u/VFZOHfsTqhNWad+l3fTfww7ab
qnbX30P6/ogF52a3jF4uahKq7zoHMQ/OmNQE+0TEssWOuXmTataCW0bsYBg2YfqDQRYWHCxiydCW
Haxg1cEalgi/PuUJYWxw0Z/mJWupN1DKJ4yogWOEyVqmPZUsH/VHtvquE+YyhAOHSkqkEBZg6X/J
PDmQMUTHEV25OpPOQxv3oLuk7ehYTMAyvujopAR8xajOwxjDeF/sIa/5wOQjcv82esjfo+Bt9jDU
Q/H2dZadcvS5ksyb6tdlTCTTS/pcSdaYSjTT/wFQSwMEFAAAAAgA7GKkQBC3L5sjAQAA6gEAAEEA
HABvcmcvY29kZWhhdXMvZ3Jvb3Z5L3J1bnRpbWUvbTEybi9UZXN0U3RhdGljU3RyaW5nRXh0ZW5z
aW9uMi5jbGFzc1VUCQADG66jTyOuo091eAsAAQTpAwAABOkDAAClUcFOAjEUnLLACqIi4tl4AxJt
4Ip6MZqYbPQA4V7WupQsbdLtov6TF08mHvwAP8r4upBoNJ7s4U3evDeZyev7x+sbgD7adQTYDdEK
scdQPVFauTOGoNOdMJTPza1k2ImUltf5YirtWExTYlqRiUU6EVb5fk2W3UxlDKeRsQmPSTkTecYT
a8zykdtcO7WQfNEfaD6WmRs54VQ8clbp5OLBSZ0powdDhlDo7F7aAcN+J5qLpeCp0AlfbQ67V2SU
yfTOh/g1ZaiPTG5jeal8ooO/jY69toEyKgzDfwRmaH6luJnOZexwiBId1b8SmLegWqWOEzLCSu8F
7LkYh1SrBdnCBtXGagE11Alr2CwYLz5ai4Nm7+mHtP1NGmAL24T0a8VW8xNQSwMEFAAAAAgAfYrZ
QNa0OuMuAQAA0AEAADcAHABvcmcvY29kZWhhdXMvZ3Jvb3Z5L3J1bnRpbWUvbTEybi9UZXN0TWFw
RXh0ZW5zaW9uLmNsYXNzVVQJAAOegehPvoHoT3V4CwABBOkDAAAE6QMAAGVQTU/CQBB9y0erpQii
KGqM8QYc3OBV48VoYoJ6gHhf6qYsKVvTbon6m7x40ejBH+CPMk4LiaHuYebt2zezb+b75/MLQA97
DsqoOahjvYIGNmxs2mgyWKdKK3PGUGx37hhK5+G9ZKj1lZY3yXQko6EYBcRYRqggBc12fyJmggdC
+3xgIqX9k84VQ2PBJ0YF/Fo8ZKQzCJPIk5cqqxzK2NDLxaOROlahPkoLXFiwbWy52EbLxo6LXbQY
emHkc4/MjEUScz8Kw9kTjxJt1FTyae9Y83w3hvqfsdvRRHpmiZp7pUkCqX0zziYmi9Ul17SBWD1L
HKJE+0pPESx1SHGFbvuUGeVy9x3slQDDKkVrThJ2UPknfUPhJSe1Mqm7kB4spHb3A0WGvDj9vEq5
gLVfUEsBAh4DFAAAAAgAtIrZQFBMMa6eAAAAQAEAAD0AGAAAAAAAAQAAALSBAAAAAE1FVEEtSU5G
L3NlcnZpY2VzL29yZy5jb2RlaGF1cy5ncm9vdnkucnVudGltZS5FeHRlbnNpb25Nb2R1bGVVVAUA
AwSC6E91eAsAAQTpAwAABOkDAABQSwECHgMKAAAAAACpYaRAAAAAAAAAAAAAAAAAEgAYAAAAAAAA
ABAA/UEVAQAATUVUQS1JTkYvc2VydmljZXMvVVQFAAO+q6NPdXgLAAEE6QMAAATpAwAAUEsBAh4D
CgAAAAAAXZ6JPwAAAAAAAAAAAAAAAAkAGAAAAAAAAAAQAP1BYQEAAE1FVEEtSU5GL1VUBQADkVji
TnV4CwABBOkDAAAE6QMAAFBLAQIeAwoAAAAAAOxipEAAAAAAAAAAAAAAAAAhABgAAAAAAAAAEAD9
QaQBAABvcmcvY29kZWhhdXMvZ3Jvb3Z5L3J1bnRpbWUvbTEybi9VVAUAAxuuo091eAsAAQTpAwAA
BOkDAABQSwECHgMKAAAAAADhYqRAAAAAAAAAAAAAAAAAHAAYAAAAAAAAABAA/UH/AQAAb3JnL2Nv
ZGVoYXVzL2dyb292eS9ydW50aW1lL1VUBQADBq6jT3V4CwABBOkDAAAE6QMAAFBLAQIeAwoAAAAA
AOFipEAAAAAAAAAAAAAAAAAUABgAAAAAAAAAEAD9QVUCAABvcmcvY29kZWhhdXMvZ3Jvb3Z5L1VU
BQADBq6jT3V4CwABBOkDAAAE6QMAAFBLAQIeAwoAAAAAAOFipEAAAAAAAAAAAAAAAAANABgAAAAA
AAAAEAD9QaMCAABvcmcvY29kZWhhdXMvVVQFAAMGrqNPdXgLAAEE6QMAAATpAwAAUEsBAh4DCgAA
AAAA4WKkQAAAAAAAAAAAAAAAAAQAGAAAAAAAAAAQAP1B6gIAAG9yZy9VVAUAAwauo091eAsAAQTp
AwAABOkDAABQSwECHgMUAAAACADpYqRAiSnRV58BAAAAAwAAOwAYAAAAAAAAAAAAtIEoAwAAb3Jn
L2NvZGVoYXVzL2dyb292eS9ydW50aW1lL20xMm4vVGVzdFN0cmluZ0V4dGVuc2lvbjIuY2xhc3NV
VAUAAxWuo091eAsAAQTpAwAABOkDAABQSwECHgMUAAAACADsYqRAELcvmyMBAADqAQAAQQAYAAAA
AAAAAAAAtIE8BQAAb3JnL2NvZGVoYXVzL2dyb292eS9ydW50aW1lL20xMm4vVGVzdFN0YXRpY1N0
cmluZ0V4dGVuc2lvbjIuY2xhc3NVVAUAAxuuo091eAsAAQTpAwAABOkDAABQSwECHgMUAAAACAB9
itlA1rQ64y4BAADQAQAANwAYAAAAAAAAAAAAtIHaBgAAb3JnL2NvZGVoYXVzL2dyb292eS9ydW50
aW1lL20xMm4vVGVzdE1hcEV4dGVuc2lvbi5jbGFzc1VUBQADnoHoT3V4CwABBOkDAAAE6QMAAFBL
BQYAAAAACwALAG8EAAB5CAAAAAA='''

    private static final String moduleTest107225JarContentsBase64Encoded =
'''UEsDBAoAAAgIAAp4nEUAAAAAAgAAAAAAAAAJAAAATUVUQS1JTkYvAwBQSwMECgAACAgACnicRbJ/
Au4bAAAAGQAAABQAAABNRVRBLUlORi9NQU5JRkVTVC5NRvNNzMtMSy0u0Q1LLSrOzM+zUjDUM+Dl
4uUCAFBLAwQKAAAICAAKeJxFAAAAAAIAAAAAAAAAEgAAAE1FVEEtSU5GL3NlcnZpY2VzLwMAUEsD
BAoAAAgIAAp4nEXQIC+RagAAAH8AAAA9AAAATUVUQS1JTkYvc2VydmljZXMvb3JnLmNvZGVoYXVz
Lmdyb292eS5ydW50aW1lLkV4dGVuc2lvbk1vZHVsZcvNTynNSfVLzE21dS/Kzy+rVAhJLS5RMDcy
MuXKBcuFpRYVZ+bn2RrqGeiBhHVLgAq4UitKUvNA4s45icXFqcW2+UXpesn5KakZiaXFeulgo/SK
SvNKMnNT9XINjfL0IMaDTHCF6eXiAgBQSwMEFAAAAAgAw1GdRW8ojyutAAAAHAEAADoAHAAgb3Jn
L2NvZGVoYXVzL2dyb292eS9ydW50aW1lL20xMm4vR3Jvb3Z5NzIyNUV4dGVuc2lvbi5qYXZhVVQJ
AANeG6FU2VyhVHV4CwABBOgDAAAE6AMAAI2OywrCMBBF1+YrhqwUIWBBhHYp4ge4zSam0xiaJiWP
opT+u22N4NLZDNx75jC9kK1QCM4rJl2ND5ECU9654cV8slF3yLpDYStCdNc7HyGXRljFzsaF5HEu
+3Q3WoI0IgS4rsipKI6XZ0QbtLMwEgDIVIgizusWvbYqCxd6m5OAptl9LtbxGJO3a7ynJbiWVks3
/aPML/46N9lHR04bjabmtOR0ECYhp9PXPZE3UEsDBAoAAAAAAAp4nEUAAAAAAAAAAAAAAAAEABwA
b3JnL1VUCQAD9AygVFNcoVR1eAsAAQToAwAABOgDAABQSwMECgAAAAAACnicRQAAAAAAAAAAAAAA
AA0AHABvcmcvY29kZWhhdXMvVVQJAAP0DKBUU1yhVHV4CwABBOgDAAAE6AMAAFBLAwQKAAAAAAAK
eJxFAAAAAAAAAAAAAAAAFAAcAG9yZy9jb2RlaGF1cy9ncm9vdnkvVVQJAAP0DKBUU1yhVHV4CwAB
BOgDAAAE6AMAAFBLAwQKAAAAAAAKeJxFAAAAAAAAAAAAAAAAHAAcAG9yZy9jb2RlaGF1cy9ncm9v
dnkvcnVudGltZS9VVAkAA/QMoFRTXKFUdXgLAAEE6AMAAAToAwAAUEsDBAoAAAAAACZ3nUUAAAAA
AAAAAAAAAAAhABwAb3JnL2NvZGVoYXVzL2dyb292eS9ydW50aW1lL20xMm4vVVQJAAO3XaFUwl2h
VHV4CwABBOgDAAAE6AMAAFBLAwQUAAAACAAmd51FahmOEG0BAABsAgAAOgAcAG9yZy9jb2RlaGF1
cy9ncm9vdnkvcnVudGltZS9tMTJuL0dyb292eTcyMjVFeHRlbnNpb24uY2xhc3NVVAkAA7ddoVTW
XaFUdXgLAAEE6AMAAAToAwAAjVFbSwJBGD3jbW2zvJZ2Lx9CpVqSIrDoIalepB6KoMdVJxtbZ2Td
lSL6T/VSUNAP6EdF3+ZSYD40D+ebOXxnznf5+Hx9B1DGko4xpDVkdASQ9mAqimkvZqPIaZjRMMsQ
2RNSOPsMwULxgiFUVU3OEK8JyU/cTp3b52bdIkZv2Ur173bK5W2G1UKtbfZNwzJlyzhzbCFbu8W/
FEOxUBvoBnzVUj3X5qNz9TPl2g1+JDy73PGP3eGtw2VPKLnhiWLQMc6QHf7gwBVWk9sxzGGe2qgs
q5sYFrDIkLzPXwluNfOVfN+0XJ5/YNhSdstoUKvXptsz/BJtVzqiw43OZlkaI/wZEr+up/U2bzg0
P7Pb5bLJsP6vmfhlUrtRRw0ohkxhxECwgigt0DtBMK9rwhi9FigyiuHSC9gTXRgmCCMDkiSTiPup
a7Rsj828IXD5gmDpGaFUmOAZkcchpfatTPjKOd8kmNKGE3XCJMUAUl9QSwECFAMKAAAICAAKeJxF
AAAAAAIAAAAAAAAACQAAAAAAAAAAABAA7UEAAAAATUVUQS1JTkYvUEsBAhQDCgAACAgACnicRbJ/
Au4bAAAAGQAAABQAAAAAAAAAAAAAAKSBKQAAAE1FVEEtSU5GL01BTklGRVNULk1GUEsBAhQDCgAA
CAgACnicRQAAAAACAAAAAAAAABIAAAAAAAAAAAAQAO1BdgAAAE1FVEEtSU5GL3NlcnZpY2VzL1BL
AQIUAwoAAAgIAAp4nEXQIC+RagAAAH8AAAA9AAAAAAAAAAAAAACkgagAAABNRVRBLUlORi9zZXJ2
aWNlcy9vcmcuY29kZWhhdXMuZ3Jvb3Z5LnJ1bnRpbWUuRXh0ZW5zaW9uTW9kdWxlUEsBAh4DFAAA
AAgAw1GdRW8ojyutAAAAHAEAADoAGAAAAAAAAQAAAKSBbQEAACBvcmcvY29kZWhhdXMvZ3Jvb3Z5
L3J1bnRpbWUvbTEybi9Hcm9vdnk3MjI1RXh0ZW5zaW9uLmphdmFVVAUAA14boVR1eAsAAQToAwAA
BOgDAABQSwECHgMKAAAAAAAKeJxFAAAAAAAAAAAAAAAABAAYAAAAAAAAABAA7UGOAgAAb3JnL1VU
BQAD9AygVHV4CwABBOgDAAAE6AMAAFBLAQIeAwoAAAAAAAp4nEUAAAAAAAAAAAAAAAANABgAAAAA
AAAAEADtQcwCAABvcmcvY29kZWhhdXMvVVQFAAP0DKBUdXgLAAEE6AMAAAToAwAAUEsBAh4DCgAA
AAAACnicRQAAAAAAAAAAAAAAABQAGAAAAAAAAAAQAO1BEwMAAG9yZy9jb2RlaGF1cy9ncm9vdnkv
VVQFAAP0DKBUdXgLAAEE6AMAAAToAwAAUEsBAh4DCgAAAAAACnicRQAAAAAAAAAAAAAAABwAGAAA
AAAAAAAQAO1BYQMAAG9yZy9jb2RlaGF1cy9ncm9vdnkvcnVudGltZS9VVAUAA/QMoFR1eAsAAQTo
AwAABOgDAABQSwECHgMKAAAAAAAmd51FAAAAAAAAAAAAAAAAIQAYAAAAAAAAABAA7UG3AwAAb3Jn
L2NvZGVoYXVzL2dyb292eS9ydW50aW1lL20xMm4vVVQFAAO3XaFUdXgLAAEE6AMAAAToAwAAUEsB
Ah4DFAAAAAgAJnedRWoZjhBtAQAAbAIAADoAGAAAAAAAAAAAAKSBEgQAAG9yZy9jb2RlaGF1cy9n
cm9vdnkvcnVudGltZS9tMTJuL0dyb292eTcyMjVFeHRlbnNpb24uY2xhc3NVVAUAA7ddoVR1eAsA
AQToAwAABOgDAABQSwUGAAAAAAsACwDkAwAA8wUAAAAA'''

    private static final URL moduleTest12JarURL
    private static final URL moduleTest107225JarURL

    static {
        File tmpJar = File.createTempFile('module-test-1.2-test', 'jar')
        tmpJar.deleteOnExit()
        tmpJar << moduleTest12JarContentsBase64Encoded.decodeBase64()
        moduleTest12JarURL = tmpJar.toURI().toURL()

        File tmpJar7225 = File.createTempFile('module-test-1.0.7225-test', 'jar')
        tmpJar7225.deleteOnExit()
        tmpJar7225 << moduleTest107225JarContentsBase64Encoded.decodeBase64()
        moduleTest107225JarURL = tmpJar.toURI().toURL()
    }

    void testThatModuleHasBeenLoaded() {
        ExtensionModuleHelperForTests.doInFork '''
            ExtensionModuleRegistry registry = GroovySystem.metaClassRegistry.moduleRegistry
            assert registry.modules
            // look for the 'Test module' module; it should always be available
            assert registry.modules.any { it.name == 'Test module' && it.version == '1.0-test' }

            // the following methods are added by the test module
            def str = 'This is a string'
            assert str.reverseToUpperCase() == str.toUpperCase().reverse()
            assert String.answer() == 42
        '''
    }

    void testThatModuleCanBeLoadedWithGrab() {
        ExtensionModuleHelperForTests.doInFork """
            ExtensionModuleRegistry registry = GroovySystem.metaClassRegistry.moduleRegistry
            // ensure that the module isn't loaded
            assert !registry.modules.any { it.name == 'Test module for Grab' && it.version == '1.2-test' }

            def resolver = "@GrabResolver('$moduleTest12JarURL')"

            assertScript resolver + '''
            @Grab(value='module-test:module-test:1.2-test', changing='true')
            import org.codehaus.groovy.runtime.m12n.*

            // ensure that the module is now loaded
            ExtensionModuleRegistry registry = GroovySystem.metaClassRegistry.moduleRegistry
            assert registry.modules.any { it.name == 'Test module for Grab' && it.version == '1.2-test' }

            // the following methods are added by the 'Test module for Grab' module
            def str = 'This is a string'
            assert str.reverseToUpperCase2() == str.toUpperCase().reverse()
            assert String.answer2() == 42
            '''

            // the module should still be available
            assert registry.modules.any { it.name == 'Test module for Grab' && it.version == '1.2-test' }
        """
    }

    void testExtensionModuleUsingGrabAndMap() {
        ExtensionModuleHelperForTests.doInFork """
            ExtensionModuleRegistry registry = GroovySystem.metaClassRegistry.moduleRegistry
            // ensure that the module isn't loaded
            assert !registry.modules.any { it.name == 'Test module for Grab' && it.version == '1.2-test' }

            def resolver = "@GrabResolver('$moduleTest12JarURL')"

            assertScript resolver + '''
            @Grab(value='module-test:module-test:1.2-test', changing='true')
            import org.codehaus.groovy.runtime.m12n.*

            def map = [:]
            assert 'foo'.taille() == 3
            assert map.taille() == 0
            '''
        """
    }

    /**
     * Test case that reproduces GROOVY-7225.
     */
    void testExtensionModuleUsingGrabAndClosure() {
        ExtensionModuleHelperForTests.doInFork """
            ExtensionModuleRegistry registry = GroovySystem.metaClassRegistry.moduleRegistry
            // ensure that the module isn't loaded
            assert !registry.modules.any { it.name == 'Groovy Test 7225' && it.version == '1.0.7225-test' }

            assertScript '''
            @GrabResolver('$moduleTest107225JarURL')
            @Grab(value='module-test:module-test:1.0.7225-test', changing='true')
            import org.codehaus.groovy.runtime.m12n.*

            assert 'test'.groovy7225() == 'test: ok'
            assert {->}.groovy7225() == '{"field":"value"}'
            '''
        """
    }
}
