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

import java.util.logging.Level;
import java.util.logging.Logger;

public class MockManagedObject {
    private static Logger logger = Logger.getLogger(MockManagedObject.class.getName());
    private String something;
    private int somethingElse;
    private boolean avail;

    public String getSomething() {
        logger.log(Level.FINEST, "Returning Something " + something);
        return something;
    }

    public void setSomething(String thing) {
        logger.log(Level.INFO, "Setting Something value to " + thing);
        something = thing;
    }

    public int getSomethingElse() {
        logger.log(Level.FINEST, "Returning SomethingElse " + somethingElse);
        return somethingElse;
    }

    public void setSomethingElse(int thing) {
        somethingElse = thing;
    }

    public MockManagedObject() {
    }

    public MockManagedObject(String thing) {
    }

    public MockManagedObject(String thing, int count) {
    }

    public void doSomething() {
        logger.log(Level.FINER, "JMX Invoke - doSomething().. (no param)");
    }

    public void doSomethingElse(int qty, String name) {
    }

    public void doWork(int hour, String what) {

    }

    public void dontDoThis(Object param) {
        logger.log(Level.FINER, "Jmx Invoke - method dontDoThis() with param : " + param);
    }
    
    public void setAvailable(boolean flag) {
        avail = flag;
    }
    
    public boolean isAvailable() {
        return avail;
    }
    
    public String get(String str) {
        return str;
    }
    
    public String set(String str) {
        return str;
    }
}
