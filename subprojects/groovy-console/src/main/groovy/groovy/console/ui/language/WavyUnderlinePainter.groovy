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

import groovy.transform.CompileStatic

import javax.swing.text.BadLocationException
import javax.swing.text.DefaultHighlighter
import javax.swing.text.JTextComponent
import javax.swing.text.Position
import javax.swing.text.View
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.RenderingHints
import java.awt.Shape

/**
 * Thin red wave under a diagnostic span. A filled background hides the token.
 */
@CompileStatic
final class WavyUnderlinePainter extends DefaultHighlighter.DefaultHighlightPainter {

    private static final Color WAVE = new Color(220, 38, 38)
    /** Half a period, in pixels. Two steps make one tight wave. */
    private static final int STEP = 2
    /** How far the wave rises and falls from its center. */
    private static final int AMPLITUDE = 1

    WavyUnderlinePainter() {
        super(WAVE)
    }

    @Override
    void paint(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent component) {
        try {
            Rectangle start = component.UI.modelToView(component, offs0)
            Rectangle end = component.UI.modelToView(component, offs1)
            if (start == null || end == null) {
                return
            }
            Rectangle alloc = rectangleOf(bounds)
            int startX = (int) start.x
            int startY = (int) start.y
            int row = (int) start.height
            int endX = (int) end.x
            int endY = (int) end.y
            if (startY == endY) {
                wave(g, startX, startY + row - 2, Math.max(endX - startX, STEP))
            } else {
                int left = (int) alloc.x
                int span = (int) alloc.width
                wave(g, startX, startY + row - 2, Math.max(left + span - startX, STEP))
                int y = startY + row
                while (y < endY) {
                    wave(g, left, y + row - 2, Math.max(span, STEP))
                    y += row
                }
                wave(g, left, endY + (int) end.height - 2, Math.max(endX - left, STEP))
            }
        } catch (BadLocationException ignored) {
        }
    }

    @Override
    Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent component, View view) {
        Rectangle area = span(offs0, offs1, bounds, view)
        if (area == null) {
            return null
        }
        wave(g, (int) area.x, (int) area.y + (int) area.height - 2, Math.max((int) area.width, STEP))
        return area
    }

    private static Rectangle span(int offs0, int offs1, Shape bounds, View view) {
        if (offs0 == view.startOffset && offs1 == view.endOffset) {
            return rectangleOf(bounds)
        }
        try {
            Shape shape = view.modelToView(offs0, Position.Bias.Forward, offs1, Position.Bias.Backward, bounds)
            return shape == null ? null : rectangleOf(shape)
        } catch (BadLocationException ignored) {
            return null
        }
    }

    private static Rectangle rectangleOf(Shape shape) {
        if (shape instanceof Rectangle) {
            return (Rectangle) shape
        }
        return shape.bounds
    }

    private void wave(Graphics g, int x, int y, int width) {
        Graphics2D g2 = (Graphics2D) g.create()
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g2.color = WAVE
            g2.stroke = new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
            int x0 = x
            int end = x + width
            boolean down = true
            while (x0 < end) {
                int x1 = Math.min(x0 + STEP, end)
                int y0 = y + (down ? AMPLITUDE : -AMPLITUDE)
                int y1 = y + (down ? -AMPLITUDE : AMPLITUDE)
                g2.drawLine(x0, y0, x1, y1)
                x0 = x1
                down = !down
            }
        } finally {
            g2.dispose()
        }
    }
}
