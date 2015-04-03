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
package groovy.swing.binding.caricature

import groovy.swing.SwingBuilder
import java.awt.Color
import javax.swing.border.TitledBorder

SwingBuilder.build {
    frame(pack:true, show:true,
        defaultCloseOperation:javax.swing.JFrame.DISPOSE_ON_CLOSE)
    {
        borderLayout()
        panel(constraints:CENTER,) {
            // place in panel to insure square layout
            caricature = widget(new JCaricature(
                background:Color.WHITE, opaque:true))
        }
        hbox(constraints:SOUTH) {
            panel(border:new TitledBorder("Style")) {
                gridLayout(new java.awt.GridLayout(5, 2))
                label("Eyes") // font stuff
                eyeSlider = slider(minimum:0, maximum:4,
                    value:bind(target:caricature,
                        targetProperty:'eyeStyle',
                        value:2))
                label("Face") // font stuff
                faceSlider = slider(minimum:0, maximum:4,
                    value:bind(target:caricature,
                        targetProperty:'faceStyle',
                        value:2))
                label("Mouth") // font stuff
                mouthSlider = slider(minimum:0, maximum:4,
                    value:bind(target:caricature,
                        targetProperty:'mouthStyle',
                        value:2))
                label("Hair") // font stuff
                hairSlider = slider(minimum:0, maximum:4,
                    value:bind(target:caricature,
                        targetProperty:'hairStyle',
                        value:2))
                label("Nose") // font stuff
                noseSlider = slider(minimum:0, maximum:4,
                    value:bind(target:caricature,
                        targetProperty:'noseStyle',
                        value:2))
            }
            panel(border:new TitledBorder("Effects")) {
                gridLayout(new java.awt.GridLayout(2, 5))
                label("Rotation") // font stuff
                rotationSlider = slider(maximum:360,
                    value:bind(target:caricature,
                        targetProperty:'rotation',
                        value:0))
                label("Scale") // font stuff
                scaleSlider = slider(maximum: 150, minimum:50,
                    value:bind(target:caricature,
                        targetProperty:'scale',
                        converter: {it / 100f}, value:100))
            }
        }
    }
}
