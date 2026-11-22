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
package groovy.jmx;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.DynamicMBean;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;

/**
 * Dynamic MBean that records the operation name, signature and arguments
 * of the most recent invocation. Operation order in {@link #getMBeanInfo()}
 * is the order supplied to the constructor.
 */
public class RecordingMBean implements DynamicMBean {

    public interface MarkerA {
    }

    public interface MarkerB {
    }

    public static class BothMarkers implements MarkerA, MarkerB {
    }

    private final MBeanOperationInfo[] operations;
    private String action;
    private String[] signature;
    private Object[] params;

    public RecordingMBean(MBeanOperationInfo... operations) {
        this.operations = operations;
    }

    public static MBeanOperationInfo op(String name, String... types) {
        MBeanParameterInfo[] params = new MBeanParameterInfo[types.length];
        for (int i = 0; i < types.length; i++) {
            params[i] = new MBeanParameterInfo("p" + i, types[i], "");
        }
        return new MBeanOperationInfo(name, name, params, String.class.getName(), MBeanOperationInfo.UNKNOWN);
    }

    public String getAction() {
        return action;
    }

    public String[] getSignature() {
        return signature;
    }

    public Object[] getParams() {
        return params;
    }

    @Override
    public Object getAttribute(String attribute) {
        return null;
    }

    @Override
    public void setAttribute(Attribute attribute) {
    }

    @Override
    public AttributeList getAttributes(String[] attributes) {
        return new AttributeList();
    }

    @Override
    public AttributeList setAttributes(AttributeList attributes) {
        return new AttributeList();
    }

    @Override
    public Object invoke(String actionName, Object[] params, String[] signature) {
        this.action = actionName;
        this.signature = signature == null ? new String[0] : signature.clone();
        this.params = params == null ? new Object[0] : params.clone();
        return actionName;
    }

    @Override
    public MBeanInfo getMBeanInfo() {
        return new MBeanInfo(RecordingMBean.class.getName(), "recording", null, null, operations, null);
    }
}
