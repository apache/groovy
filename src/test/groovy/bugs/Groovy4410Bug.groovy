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

import groovy.test.GroovyTestCase

class Groovy4410Bug extends GroovyTestCase {
    void testBridgeMethodWithArrayTypeParameterV1() {
        StringProducer1 sp = new StringProducer1()
        assert sp.gimme(null) == 'Hello World'
        
        G4410JavaStringProducer jsp = new G4410JavaStringProducer()
        assert jsp.gimme(null) == 'Hello World'
        
        HackProducer1 hp = new HackProducer1()
        assert hp.gimme(null) == 'Hello World'
    }

    void testBridgeMethodWithArrayTypeParameterV2() {
        StringProducer2 sp = new StringProducer2()
        assert sp.gimme(null) == 'Hello World'
        
        HackProducer2 hp = new HackProducer2()
        assert hp.gimme(null) == 'Hello World'
    }
}
class HackProducer1 implements G4410Producer1 {
    Object gimme(String[] a) {
        "Hello World"
    }
}

class StringProducer1 implements G4410Producer1 {
    String gimme(String[] a1) {
        "Hello World"
    }
}

class HackProducer2 implements G4410Producer2<Object> {
    Object gimme(String[] a1) {
        "Hello World"
    }
}

class StringProducer2 implements G4410Producer2<String> {
    String gimme(String[] a1) {
        "Hello World"
    }
}