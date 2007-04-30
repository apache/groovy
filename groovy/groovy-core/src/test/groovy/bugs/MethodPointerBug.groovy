package groovy.bugs

/**
 * @author Pilho Kim
 * @version $Revision$
 */
class MethodPointerBug extends GroovyTestCase {

    def void sayHello() { 
        println "hello" 
    } 

    def MethodPointerBug getThisObject() { 
        return this
    } 

    // Test a standard method pointer operator ".&".  For example, foo.&bar.
    void testMethodPointer() {
        def bug = new MethodPointerBug()
        def x = bug.&sayHello
        x()
    } 

    // Test a standard method pointer operator ".&" with this object.  For example, this.&bar.
    void testThisMethodPointer() {
        def y = this.&sayHello
        y()
    } 

    ///////////////////////////////////////////////////////////////////////////////////////////
    // Test a default method pointer operator "&" with this object.  For example, &bar.
    // This shows that the issue GROOVY-826 has been fixed in groovy-1.0-jar-02.
/*
  todo - commented out due to groovy.g non-determinisms
    void testDefaultMethodPointer() {
        def z = &sayHello
        z()
    } 
*/
    // Test a default method pointer operator ".&" with returned object.  For example, someMethod().&bar.
    void testMethodPointerWithReturn() {
        def u = getThisObject().&sayHello
        u()
        def v = thisObject.&sayHello
        v()
    } 
}
