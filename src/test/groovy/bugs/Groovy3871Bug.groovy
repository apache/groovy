package groovy.bugs

/**
 * Fix for http://jira.codehaus.org/browse/GROOVY-3871
 * @author Guillaume Laforge
 */
class Groovy3871Bug extends GroovyTestCase {

    protected void setUp() {
        super.setUp()
        G3871Base.metaClass = null
        G3871Child.metaClass = null
    }

    protected void tearDown() {
        G3871Base.metaClass = null
        G3871Child.metaClass = null
        super.tearDown();
    }

    void testPropertyMissingInheritanceIssue() {
        // defining a propertyMissing on the base class
        G3871Base.metaClass.propertyMissing = { String name -> name }
        def baseInstance = new G3871Base()
        assert baseInstance.someProp == "someProp"

        // the child class inherits the propertyMissing
        def childInstance = new G3871Child()
        assert childInstance.otherProp == "otherProp"

        // when a propertyMissing is registered for the child
        // it should be used over the inherited one
        G3871Child.metaClass.propertyMissing = { String name -> name.reverse() }
        def otherChildInstance = new G3871Child()
        assert otherChildInstance.otherProp == "porPrehto"
    }
}

/** a dummy base class */
class G3871Base { }

/** a dummy child class */
class G3871Child extends G3871Base { }

