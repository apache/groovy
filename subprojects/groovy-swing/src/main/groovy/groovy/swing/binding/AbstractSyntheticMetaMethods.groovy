/*
 * Copyright 2007-2008 the original author or authors.
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
package groovy.swing.binding
/**
 * Created by IntelliJ IDEA.
 * User: Danno.Ferrin
 * Date: Jun 19, 2008
 * Time: 12:52:31 PM
 * To change this template use File | Settings | File Templates.
 */
class AbstractSyntheticMetaMethods {

    static void enhance(Object o, Map enhancedMethods) {
        Class klass = o.getClass()
        MetaClassRegistry mcr = GroovySystem.metaClassRegistry
        MetaClass mc = mcr.getMetaClass(klass)
        boolean init = false
        mcr.removeMetaClass klass //??
        //if (!(mc instanceof ExpandoMetaClass)) {
            mc = new ExpandoMetaClass(klass)
            init = true
        //}
        enhancedMethods.each {k, v ->
            if (mc.getMetaMethod(k) == null) {
                mc.registerInstanceMethod(k, v)
            }
        }
        if (init) {
            mc.initialize()
            mcr.setMetaClass(klass, mc)
        }

    }

}
