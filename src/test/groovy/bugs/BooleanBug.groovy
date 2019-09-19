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

/**
 */
class BooleanBug extends GroovyTestCase {
    
    void testBug() {
        def x = new BooleanBean(name:'James', foo:true)
        def y = new BooleanBean(name:'Bob', foo:false)

        assert x.foo
        assert ! y.foo
        y.foo = true
        assert y.foo
    }
    
    void testBug2() {
        BooleanBean bean = new BooleanBean(name:'Gromit', foo:false)
        def value = isApplicableTo(bean)
        assert value
    }
    
    public boolean isApplicableTo(BooleanBean field) {
        return !field.isFoo();
    }
    
    public testBooleanAsMethodArgumentFromCompare() {
        assertScript """
            def foo(x){x}
            def i = 0
            assert foo(i==0)==true
        """
    }

}

class BooleanBean {
    String name
    boolean foo
}