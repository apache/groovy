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
