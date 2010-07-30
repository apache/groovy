/*
 * Copyright 2003-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

class A extends java.beans.SimpleBeanInfo {}
class B extends A {}
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
