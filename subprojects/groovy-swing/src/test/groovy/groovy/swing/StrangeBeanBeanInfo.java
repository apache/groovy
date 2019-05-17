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
package groovy.swing;

import java.beans.EventSetDescriptor;
import java.beans.SimpleBeanInfo;
import java.lang.reflect.Method;

public class StrangeBeanBeanInfo extends SimpleBeanInfo {
    private static final Class[] EMPTY_CLASS_ARRAY = new Class[0];

    public EventSetDescriptor[] getEventSetDescriptors() {
        try {
            Method[] events = StrangeEventListener.class.getMethods();
            Method addListener = StrangeBean.class.getMethod("addStrangeEventListener", StrangeEventListener.class);
            Method removeListener = StrangeBean.class.getMethod("removeStrangeEventListener", StrangeEventListener.class);
            Method getListeners = StrangeBean.class.getMethod("getStrangeEventListeners", EMPTY_CLASS_ARRAY);
            
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
