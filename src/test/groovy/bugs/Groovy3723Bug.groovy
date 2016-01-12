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

class Groovy3723Bug extends GroovyTestCase {
    void testEMCPropertyAccessWitGetPropertySetProperty() {
        assertScript """
            class Dummy3723 {}
            
            Dummy3723.metaClass.id = 1
            
            Dummy3723.metaClass.getProperty = { name ->
               def metaProperty = delegate.metaClass.getMetaProperty(name)
               return metaProperty?.getProperty(delegate)
            }
            
            Dummy3723.metaClass.setProperty = { name, value ->
               def metaProperty = delegate.metaClass.getMetaProperty(name)
               metaProperty?.setProperty(delegate,value)
            }
            
            def d = new Dummy3723()
            // was failing with groovy.lang.GroovyRuntimeException: Cannot set read-only property: id
            d.id = 123
            // was failing with groovy.lang.GroovyRuntimeException: Cannot read write-only property: id
            assert d.id, 123
        """
    }
}
