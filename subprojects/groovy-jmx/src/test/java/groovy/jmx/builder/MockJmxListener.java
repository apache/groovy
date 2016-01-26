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
package groovy.jmx.builder;

import javax.management.Notification;
import javax.management.NotificationListener;

/**
 * Created by IntelliJ IDEA.
 * User: paulk
 * Date: 18/07/2010
 * Time: 12:16:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class MockJmxListener implements MockJmxListenerMBean, NotificationListener {

    public void handleNotification(Notification note, Object handback) {
        System.out.println("Notification " + note.toString() + " received");
    }

    public String getObjectName() {
        return null;
    }

    public void makeObject() {
        // do nothing
    }
}

interface MockJmxListenerMBean {
    String getObjectName();

    void makeObject();
}