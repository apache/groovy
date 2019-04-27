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

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class to help with RMI
 */
public class JmxConnectorHelper {
    public static Map createRmiRegistry(int initPort) {
        Map<String, Object> result = new HashMap<String, Object>(2);
        int counter = 0;
        int port = initPort;
        Registry reg = null;
        while (reg == null && counter <= 4) {
            try {
                reg = LocateRegistry.createRegistry(port);
                result.put("registry", reg);
                result.put("port", port);
                break;
            } catch (RemoteException ex) {
                counter++;
                port = port + 1;
                System.out.println("JmxBuilder - *** FAILED *** to create RMI Registry - Will Retry on port [" + port + "].");
                try {
                    Thread.currentThread().sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return result;
    }

    public static void destroyRmiRegistry(Registry reg) {
        try {
            if (reg != null) {
                java.rmi.server.UnicastRemoteObject.unexportObject(reg, true);
            }
        } catch (NoSuchObjectException e) {
            throw new RuntimeException(e);
        }
    }
}
