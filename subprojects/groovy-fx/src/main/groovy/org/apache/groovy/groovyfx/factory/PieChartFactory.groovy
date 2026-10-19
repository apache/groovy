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

import javafx.scene.chart.PieChart

/**
 * Creates pie charts. The {@code data} attribute accepts a map of names to
 * values, a list of {@code [name, value]} lists, or a list of
 * {@code PieChart.Data} instances:
 * <pre>
 * pieChart(data: [Groovy: 60, Java: 40])
 * </pre>
 */
class PieChartFactory extends NodeFactory {

    PieChartFactory() {
        super(PieChart)
    }

    @Override
    boolean onHandleNodeAttributes(FactoryBuilderSupport builder, Object node, Map attributes) {
        Object data = attributes.remove('data')
        if (data != null) {
            ((PieChart) node).data.setAll(toData(data))
        }
        super.onHandleNodeAttributes(builder, node, attributes)
    }

    static List<PieChart.Data> toData(Object data) {
        if (data instanceof Map) {
            return data.collect { Object name, Object value -> new PieChart.Data(name.toString(), (value as Number).doubleValue()) }
        }
        if (data instanceof Iterable) {
            return data.collect { Object item -> toSlice(item) }
        }
        throw new IllegalArgumentException("Cannot convert ${data} to pie chart data: expected a map of names to values")
    }

    private static PieChart.Data toSlice(Object item) {
        if (item instanceof PieChart.Data) return item
        if (item instanceof List && item.size() == 2) {
            return new PieChart.Data(item[0].toString(), (item[1] as Number).doubleValue())
        }
        throw new IllegalArgumentException("Cannot convert ${item} to a pie chart slice: expected [name, value]")
    }
}
