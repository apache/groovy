package gls.property

import gls.CompilableTestSupport

class MetaClassOverridingTest extends CompilableTestSupport {

    public void testOverridingMetaClassProperty() {
        shouldCompile """
            class A {
                 private MetaClass metaClass

                 MetaClass getMetaClass() { this.metaClass }
                 void setMetaClass(MetaClass mc) { this.metaClass = mc }
            }
        """

        shouldCompile """
            class A {
                 private MetaClass metaClass

                 void setMetaClass(MetaClass mc) { this.metaClass = mc }
            }
        """

        shouldCompile """
            class A {
                 private MetaClass metaClass

                 MetaClass getMetaClass() { this.metaClass }
            }
        """

        shouldCompile """
            class A {
                 private MetaClass metaClass
            }
        """        
    }

}