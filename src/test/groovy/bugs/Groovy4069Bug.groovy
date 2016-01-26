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

class Groovy4069Bug extends GroovyTestCase {

    void testEMCConstructorWithSubClassingTest1V1() {
        def shell = new GroovyShell()
        shell.evaluate """
            ExpandoMetaClass.enableGlobally()
            try {            
                // ChildX.metaClass
                def oldMetaClass = ParentX.metaClass
                
                def emc = new ExpandoMetaClass(ParentX, true, true)
                emc.initialize()
                GroovySystem.metaClassRegistry.setMetaClass(ParentX, emc)
                
                emc.constructor = { Map m -> ParentX.newInstance() }
                
                assert new ChildX([:]).class.name == 'ChildX'
                
                GroovySystem.metaClassRegistry.removeMetaClass(ParentX) 
                GroovySystem.metaClassRegistry.setMetaClass(ParentX, oldMetaClass)
                
                assert new ChildX([:]).class.name == 'ChildX'
            } finally {
                ExpandoMetaClass.disableGlobally()
            }
            class ParentX { def a }
            class ChildX extends ParentX { def b }
        """
    }
    
    void testEMCConstructorWithSubClassingTest1V2() {
        def shell = new GroovyShell()
        shell.evaluate """
            ExpandoMetaClass.enableGlobally()
            try {
                ChildX.metaClass
                def oldMetaClass = ParentX.metaClass
                
                def emc = new ExpandoMetaClass(ParentX, true, true)
                emc.initialize()
                GroovySystem.metaClassRegistry.setMetaClass(ParentX, emc)
                
                emc.constructor = { Map m -> ParentX.newInstance() }
                
                assert new ChildX([:]).class.name == 'ChildX'
                
                GroovySystem.metaClassRegistry.removeMetaClass(ParentX) 
                GroovySystem.metaClassRegistry.setMetaClass(ParentX, oldMetaClass)
                
                assert new ChildX([:]).class.name == 'ChildX'
            } finally {
                ExpandoMetaClass.disableGlobally()
            }
            class ParentX { def a }
            class ChildX extends ParentX { def b }
        """
    }
    
    void testEMCConstructorWithSubClassingTest2V1() {
        def shell = new GroovyShell()
        shell.evaluate """
            ExpandoMetaClass.enableGlobally()
            try {            
                // ChildY.metaClass
                def oldMetaClass = ChildY.metaClass
                
                def emc = new ExpandoMetaClass(ChildY, true, true)
                emc.initialize()
                GroovySystem.metaClassRegistry.setMetaClass(ChildY, emc)
                
                emc.constructor = { Map m -> ParentY.newInstance() }
                
                assert new ChildY([:]).class.name == 'ParentY'
                
                GroovySystem.metaClassRegistry.removeMetaClass(ChildY) 
                GroovySystem.metaClassRegistry.setMetaClass(ChildY, oldMetaClass)
                
                assert new ChildY([:]).class.name == 'ChildY'
            } finally {
                ExpandoMetaClass.disableGlobally()
            }
            class ParentY { def a }
            class ChildY extends ParentY { def b }
        """
    }

    void testEMCConstructorWithSubClassingTest2V2() {
        def shell = new GroovyShell()
        shell.evaluate """
            ExpandoMetaClass.enableGlobally()
            try {            
                ChildY.metaClass
                def oldMetaClass = ChildY.metaClass
                
                def emc = new ExpandoMetaClass(ChildY, true, true)
                emc.initialize()
                GroovySystem.metaClassRegistry.setMetaClass(ChildY, emc)
                
                emc.constructor = { Map m -> ParentY.newInstance() }
                
                assert new ChildY([:]).class.name == 'ParentY'
                
                GroovySystem.metaClassRegistry.removeMetaClass(ChildY) 
                GroovySystem.metaClassRegistry.setMetaClass(ChildY, oldMetaClass)
                
                assert new ChildY([:]).class.name == 'ChildY'
            } finally {
                ExpandoMetaClass.disableGlobally()
            }
            class ParentY { def a }
            class ChildY extends ParentY { def b }
        """
    }
}
