/*
 * Copyright 2003-2012 the original author or authors.
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
package groovy;

import java.beans.EventSetDescriptor;
import java.beans.SimpleBeanInfo;
import java.lang.reflect.Method;

public class StrangeBeanBeanInfo extends SimpleBeanInfo {

    public EventSetDescriptor[] getEventSetDescriptors() {
        try {
            Method[] events = StrangeEventListener.class.getMethods();
            Method addListener = StrangeBean.class.getMethod("addStrangeEventListener", new Class[] {StrangeEventListener.class});
            Method removeListener = StrangeBean.class.getMethod("removeStrangeEventListener", new Class[] {StrangeEventListener.class});
            Method getListeners = StrangeBean.class.getMethod("getStrangeEventListeners", new Class[0]);
            
            return new EventSetDescriptor[] {
                new EventSetDescriptor( 
                        "strangeEvent",
                        StrangeEventListener.class, 
                        events,
                        addListener,
                        removeListener,
                        getListeners)
            };
        } catch (Exception e) {
            e.printStackTrace(System.out);
            return super.getEventSetDescriptors();
        }
    }
}
