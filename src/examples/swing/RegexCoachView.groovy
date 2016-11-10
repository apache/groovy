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
import static java.awt.BorderLayout.*
import static javax.swing.JSplitPane.VERTICAL_SPLIT
import static javax.swing.WindowConstants.EXIT_ON_CLOSE

frame(title: 'The Groovy Regex Coach', location: [20, 40], size: [600, 500], defaultCloseOperation: EXIT_ON_CLOSE) {
    panel {
        borderLayout()
        splitPane(orientation: VERTICAL_SPLIT, dividerLocation: 150) {
            panel {
                borderLayout()
                label(constraints: NORTH, text: 'Regular expression:')
                scrollPane(constraints: CENTER) {
                    textPane(id: 'regexPane')
                }
                label(constraints: SOUTH, id: 'regexStatus', text: ' ')
            }
            panel {
                borderLayout()
                label(constraints: NORTH, text: 'Target string:')
                scrollPane(constraints: CENTER) {
                    textPane(id: 'targetPane')
                }
                panel(constraints: SOUTH) {
                    borderLayout()
                    label(constraints: NORTH, id: 'targetStatus', text: ' ')
                    panel(constraints: SOUTH) {
                        flowLayout()
                        button('<<-', id: 'scanLeft')
                        button('->>', id: 'scanRight')
                    }
                }
            }
        }
    }
}