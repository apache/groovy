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
package org.codehaus.groovy.runtime;

import groovy.lang.GroovyClassLoader;
import groovy.test.GroovyTestCase;

public class JdkDynamicProxyTest extends GroovyTestCase {

    public void testJdkDynamicProxySameLoader() throws Exception {

        // Instantiate all beans.
        final GroovyClassLoader loader = new GroovyClassLoader();
        JdkDynamicProxyServiceBean sb1 = (JdkDynamicProxyServiceBean) JdkDynamicProxyInvocationHandler.getProxiedObject(loader.loadClass("org.codehaus.groovy.runtime.JdkDynamicProxyServiceBeanImpl1").newInstance());
        JdkDynamicProxyServiceBean sb2 = (JdkDynamicProxyServiceBean) JdkDynamicProxyInvocationHandler.getProxiedObject(loader.loadClass("org.codehaus.groovy.runtime.JdkDynamicProxyServiceBeanImpl2").newInstance());

        // Manually wire beans together.
        sb1.setJdkDynamicProxyServiceBean(sb2);
        assertEquals("SERVICE", sb1.doService());
    }

    public void testJdkDynamicProxyDifferentLoaders() throws Exception {

        // Instantiate all beans.
        JdkDynamicProxyServiceBean sb1 = (JdkDynamicProxyServiceBean) JdkDynamicProxyInvocationHandler.getProxiedObject(new GroovyClassLoader().loadClass("org.codehaus.groovy.runtime.JdkDynamicProxyServiceBeanImpl1").newInstance());
        JdkDynamicProxyServiceBean sb2 = (JdkDynamicProxyServiceBean) JdkDynamicProxyInvocationHandler.getProxiedObject(new GroovyClassLoader().loadClass("org.codehaus.groovy.runtime.JdkDynamicProxyServiceBeanImpl2").newInstance());

        // Manually wire beans together.
        sb1.setJdkDynamicProxyServiceBean(sb2);
        assertEquals("SERVICE", sb1.doService());
    }

}
