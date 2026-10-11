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
package groovy.console.ui.language

import org.junit.jupiter.api.Test

import javax.swing.JTextArea
import javax.swing.plaf.basic.BasicTextAreaUI
import javax.swing.text.BadLocationException
import javax.swing.text.JTextComponent
import javax.swing.text.Position
import javax.swing.text.View
import java.awt.Graphics
import java.awt.Rectangle
import java.awt.Shape
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage

final class WavyUnderlinePainterTest {

    @Test
    void waveCoversEachSpanThePainterCanDraw() {
        def image = new BufferedImage(40, 40, BufferedImage.TYPE_INT_RGB)
        def g = image.createGraphics()
        try {
            def painter = new WavyUnderlinePainter()
            def bounds = new Rectangle(2, 4, 30, 20)
            def area = new JTextArea('x')
            boolean fail = false
            Rectangle start = new Rectangle(0, 10, 1, 12)
            Rectangle end = new Rectangle(20, 10, 1, 12)
            area.setUI(locations { int pos ->
                if (fail) {
                    throw new BadLocationException('bad', pos)
                }
                pos == 0 ? start : end
            })

            start = null
            painter.paint(g, 0, 1, bounds, area)
            start = new Rectangle(0, 10, 1, 12)
            end = null
            painter.paint(g, 0, 1, bounds, area)
            fail = true
            painter.paint(g, 0, 1, bounds, area)
            fail = false

            // One pixel wide, then wider than one wave step.
            end = new Rectangle(1, 10, 1, 12)
            painter.paint(g, 0, 1, bounds, area)
            end = new Rectangle(20, 10, 1, 12)
            painter.paint(g, 0, 1, bounds, area)

            // First row ends at the allocation edge. The last row starts at the
            // left. Two rows sit between them, so the middle of the wave is drawn.
            start = new Rectangle(31, 0, 1, 8)
            end = new Rectangle(2, 24, 1, 8)
            painter.paint(g, 0, 1, bounds, area)
            start = new Rectangle(2, 0, 1, 8)
            end = new Rectangle(24, 24, 1, 8)
            painter.paint(g, 0, 1, bounds, area)

            def whole = offsets(0, 4) { p0, p1, allocation -> allocation }
            assert painter.paintLayer(g, 0, 4, bounds, area, whole).is(bounds)
            // The start matches the view and the end does not, so the span is measured.
            assert painter.paintLayer(g, 0, 2, bounds, area, whole) != null

            def missing = offsets(0, 4) { p0, p1, allocation -> null }
            assert painter.paintLayer(g, 1, 2, bounds, area, missing) == null

            def broken = offsets(0, 4) { p0, p1, allocation ->
                throw new BadLocationException('bad', p0)
            }
            assert painter.paintLayer(g, 1, 2, bounds, area, broken) == null

            def round = new RoundRectangle2D.Double(3, 5, 9, 7, 2, 2)
            def shaped = offsets(0, 4) { p0, p1, allocation -> round }
            def painted = painter.paintLayer(g, 1, 3, bounds, area, shaped)
            assert painted.x == 3
            assert painted.width == 9
        } finally {
            g.dispose()
        }
    }

    private static BasicTextAreaUI locations(Closure<Rectangle> at) {
        new BasicTextAreaUI() {
            @Override
            Rectangle modelToView(JTextComponent component, int pos) throws BadLocationException {
                at.call(pos)
            }
        }
    }

    private static View offsets(int start, int end, Closure<Shape> shape) {
        new View(null) {
            @Override
            float getPreferredSpan(int axis) { 0 }

            @Override
            void paint(Graphics graphics, Shape allocation) { }

            @Override
            Shape modelToView(int pos, Shape allocation, Position.Bias bias) { null }

            @Override
            int viewToModel(float x, float y, Shape allocation, Position.Bias[] biasReturn) { 0 }

            @Override
            int getStartOffset() { start }

            @Override
            int getEndOffset() { end }

            @Override
            Shape modelToView(int p0, Position.Bias b0, int p1, Position.Bias b1, Shape allocation) {
                shape.call(p0, p1, allocation)
            }
        }
    }
}
