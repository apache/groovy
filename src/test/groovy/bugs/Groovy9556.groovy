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
package groovy.bugs

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit
import org.junit.Test

final class Groovy9556 {
// .\gradlew --no-daemon --max-workers 2 :test --tests groovy.bugs.Groovy9556 --debug-jvm
    @Test
    void testInheritConstructors() {
        def config = new CompilerConfiguration(
            targetDirectory: File.createTempDir(),
            jointCompilationOptions: [memStub: false]
        )

        def parentDir = File.createTempDir()
        config.jointCompilationOptions.stubDir = parentDir

        try {
            def a = new File(parentDir, 'CreateSignatureBase.java')
            a.write '''
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

public abstract class CreateSignatureBase implements SignatureInterface
{
    public CreateSignatureBase(KeyStore keystore, char[] pin)
            throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, CertificateException
    { }
    @Override
    public byte[] sign(InputStream content) throws IOException
    {
        return null;
    }
}

interface SignatureInterface {
    byte[] sign(InputStream content) throws IOException;
}
            '''
            def b = new File(parentDir, 'B.groovy')
            b.write '''
import groovy.transform.*

@InheritConstructors class CreateSignature extends CreateSignatureBase {
    void signPDF(String pdDocument, String out) {

    }
}
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(a, b)
            cu.compile()
        } finally {
            parentDir.deleteDir()
            config.targetDirectory.deleteDir()
        }
    }
}
