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

import javafx.scene.chart.XYChart

/**
 * Creates a chart series:
 * <pre>
 * lineChart {
 *     series('Sales', data: [[1, 10], [2, 14], [3, 9]])
 *     series('Costs', data: [Jan: 4, Feb: 5])
 * }
 * </pre>
 * The {@code data} attribute accepts a map of x to y values, a list of
 * {@code [x, y]} or {@code [x, y, extra]} lists, or a list of
 * {@code XYChart.Data} instances.
 */
class SeriesFactory extends AbstractFactory {

    @Override
    Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) {
        if (value instanceof XYChart.Series) return value
        XYChart.Series series = new XYChart.Series()
        if (value != null) {
            series.name = value.toString()
        }
        series
    }

    @Override
    boolean onHandleNodeAttributes(FactoryBuilderSupport builder, Object node, Map attributes) {
        XYChart.Series series = (XYChart.Series) node
        Object data = attributes.remove('data')
        if (data != null) {
            series.data.setAll(toData(data))
        }
        FXAttributes.apply(node, attributes)
        false
    }

    @Override
    void setChild(FactoryBuilderSupport builder, Object parent, Object child) {
        if (child instanceof XYChart.Data) {
            ((XYChart.Series) parent).data.add(child)
        }
    }

    static List<XYChart.Data> toData(Object data) {
        if (data instanceof Map) {
            return data.collect { Object x, Object y -> new XYChart.Data(x, y) }
        }
        if (data instanceof Iterable) {
            return data.collect { Object item -> toDataPoint(item) }
        }
        throw new IllegalArgumentException("Cannot convert ${data} to series data: expected a map or a list of [x, y] pairs")
    }

    private static XYChart.Data toDataPoint(Object item) {
        if (item instanceof XYChart.Data) return item
        if (item instanceof List) {
            if (item.size() == 2) return new XYChart.Data(item[0], item[1])
            if (item.size() == 3) return new XYChart.Data(item[0], item[1], item[2])
        }
        throw new IllegalArgumentException("Cannot convert ${item} to a data point: expected [x, y] or [x, y, extra]")
    }
}
