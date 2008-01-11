package groovy.bugs

import java.beans.*

/**
 *  BeanInfo class usage
 */

class Groovy596_Bug extends GroovyTestCase {

    void testMetaClassUsageOfBeanInfoDoesNotConflictWithScriptUsageLeadingToStackOverflow() {
    	assertNotNull(new A());
    	assertNotNull(new B());
    	assertNotNull(new C());
    	assertNotNull(new D());
    }
}

class A extends java.beans.SimpleBeanInfo { }
class B extends A { }
class C implements java.beans.BeanInfo {
    public BeanDescriptor getBeanDescriptor() {return null;}
    public EventSetDescriptor[] getEventSetDescriptors() {return new EventSetDescriptor[0];}
    public int getDefaultEventIndex() {return 0;}
    public PropertyDescriptor[] getPropertyDescriptors() {return new PropertyDescriptor[0];}
    public int getDefaultPropertyIndex() {return 0;}
    public MethodDescriptor[] getMethodDescriptors() {return new MethodDescriptor[0];}
    public BeanInfo[] getAdditionalBeanInfo() {return new BeanInfo[0];}
    public java.awt.Image getIcon(int iconKind) {return null;}
}
class D extends C {}
