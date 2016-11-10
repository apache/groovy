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
package groovy.swing.timelog

import java.text.SimpleDateFormat
import javax.swing.JFrame
import java.awt.Font
import java.awt.Color

SimpleDateFormat timeFormat = new SimpleDateFormat('HH:mm:ss')
timeFormat.setTimeZone(TimeZone.getTimeZone('GMT'))
SimpleDateFormat dateFormat = new SimpleDateFormat('dd MMM yyyy HH:mm:ss')
def convertTime = {it -> timeFormat.format new Date(it) }
def convertDate = {it -> dateFormat.format new Date(it) }

frame(title: 'Time Log Demo',
    defaultCloseOperation : JFrame.EXIT_ON_CLOSE)
{
    gridBagLayout()

    label('Client:', insets:[6,6,3,3])
    textField('', id: 'tfClient',
        enabled: bind( source: model, sourceProperty: 'running', converter: {!it} ),
        gridwidth: REMAINDER, fill: HORIZONTAL, insets: [6,3,3,6])

    label('00:00:00', font: new Font('Ariel', Font.BOLD, 42),
        foreground: bind(source: model, sourceProperty: 'running', converter: {it ? Color.GREEN : Color.RED}),
        text: bind(source: model, sourceProperty: 'elapsedTime', converter: convertTime),
        gridwidth: 2, gridheight: 2, anchor: EAST, weightx: 1.0, insets: [3,6,3,3])
    button(startAction, id: 'startButton',
        enabled: bind(source: model, sourceProperty: 'running', converter: {!it}),
        gridwidth: REMAINDER, insets: [3,3,3,6])
    button(stopAction, id: 'stopButton',
        enabled: bind(source: model, sourceProperty: 'running'),
        gridwidth: REMAINDER, insets: [3,3,3,6])

    separator(gridwidth: REMAINDER, fill: HORIZONTAL, insets: [9,6,9,6])

    scrollPane(minimumSize: [100, 100],
        gridwidth: REMAINDER, weighty: 1.0, fill: BOTH, insets: [3,6,6,6])
    {
        table(id: 'clientsTable') {
            tableModel(list: model.entries) {
                propertyColumn(header: 'Client',   propertyName: 'client')
                closureColumn( header: 'Start',    read: {convertDate it.start})
                closureColumn( header: 'Stop',     read: {convertDate it.stop})
                closureColumn( header: 'Duration', read: {convertTime it.stop - it.start})
            }
        }
    }
}