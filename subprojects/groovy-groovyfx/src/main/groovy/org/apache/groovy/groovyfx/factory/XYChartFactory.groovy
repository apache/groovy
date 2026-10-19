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
package org.apache.groovy.groovyfx.factory

import javafx.scene.chart.Axis
import javafx.scene.chart.NumberAxis
import org.codehaus.groovy.runtime.InvokerHelper

/**
 * Creates {@code XYChart} subclasses, whose axes must be given at
 * construction. The {@code xAxis} and {@code yAxis} attributes supply them
 * and default to a {@code NumberAxis}; nested {@code series} nodes become the
 * chart's data.
 */
class XYChartFactory extends NodeFactory {

    XYChartFactory(Class chartClass) {
        super(chartClass)
    }

    @Override
    Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) {
        if (value != null && beanClass.isInstance(value)) return value
        Axis xAxis = axis(name, 'xAxis', attributes.remove('xAxis'))
        Axis yAxis = axis(name, 'yAxis', attributes.remove('yAxis'))
        InvokerHelper.invokeConstructorOf(beanClass, [xAxis, yAxis] as Object[])
    }

    private static Axis axis(Object name, String attribute, Object value) {
        if (value == null) return new NumberAxis()
        if (value instanceof Axis) return value
        throw new IllegalArgumentException("The ${attribute} of '${name}' must be an Axis. Found: ${value.getClass().name}")
    }
}
