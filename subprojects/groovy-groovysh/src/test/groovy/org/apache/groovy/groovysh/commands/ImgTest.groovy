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
package org.apache.groovy.groovysh.commands

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import javax.imageio.ImageIO
import javax.swing.JLabel
import java.awt.image.BufferedImage
import java.nio.file.Path

import static groovy.test.GroovyAssert.shouldFail

/**
 * Smoke tests for the {@code /img} command. Asserts on the fallback path
 * (dumb terminal doesn't speak Sixel/Kitty/iTerm2) — the actual ANSI-image
 * emission isn't testable through a captured byte stream without a real
 * graphics-protocol terminal.
 */
class ImgTest extends SystemTestSupport {

    @TempDir
    Path tmp

    private Path writePng(String name, int w, int h) {
        Path file = tmp.resolve(name)
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB)
        ImageIO.write(image, 'png', file.toFile())
        file
    }

    @Test
    void unsupportedTerminalProducesSummaryLine() {
        Path file = writePng('chart.png', 12, 8)
        system.execute("/img ${forwardSlashes(file)}")
        // Dumb terminal — none of Sixel/Kitty/iTerm2 — falls through to the
        // summary-line branch in img(). Assert on dimensions and the file
        // identifier; don't pin to exact wording.
        def out = printer.output.join()
        assert out.contains('12x8')
        assert out.contains('chart.png')
    }

    @Test
    void missingFileSurfacesAClearError() {
        // /img saves exceptions via saveException(); JLine's
        // AbstractCommandRegistry.invoke rethrows them, so the user sees a
        // clear error rather than a silent no-op.
        def thrown = shouldFail(IllegalArgumentException) {
            system.execute('/img no-such-file.png')
        }
        assert thrown.message.contains('File not found')
        assert thrown.message.contains('no-such-file.png')
    }

    @Test
    void imgFromBufferedImageVariable() {
        // /img $var resolves the engine variable to its value via xargs.
        BufferedImage src = new BufferedImage(20, 10, BufferedImage.TYPE_INT_RGB)
        engine.put('imgVar', src)
        system.execute('/img $imgVar')
        def out = printer.output.join()
        // Dumb terminal — falls through to the summary, which echoes the
        // BufferedImage's actual dimensions.
        assert out.contains('20x10')
    }

    @Test
    void imgFromObjectWithCreateBufferedImage() {
        // Duck-typed dispatch: anything with createBufferedImage(int,int) —
        // mirrors JFreeChart's signature without us depending on JFreeChart.
        engine.put('chart', new ChartLikeForTest())
        system.execute('/img $chart --width=64 --height=32')
        def out = printer.output.join()
        assert out.contains('64x32')
    }

    @Test
    void imgFromObjectWithToBufferedImage() {
        // Sibling duck-type: anything with toBufferedImage(int,int) —
        // mirrors Smile Figure's signature without us depending on Smile.
        engine.put('figure', new FigureLikeForTest())
        system.execute('/img $figure --width=72 --height=48')
        def out = printer.output.join()
        assert out.contains('72x48')
    }

    @Test
    void imgFromJComponent() {
        // JComponent path — laid out and painted into a BufferedImage at the
        // requested size.
        engine.put('label', new JLabel('hello'))
        system.execute('/img $label --width=80 --height=24')
        def out = printer.output.join()
        assert out.contains('80x24')
    }

    @Test
    void imgFromUnsupportedTypeErrors() {
        engine.put('thing', 42)
        def thrown = shouldFail(IllegalArgumentException) {
            system.execute('/img $thing')
        }
        assert thrown.message.contains("don't know how to render")
        assert thrown.message.contains('Integer')
    }

    @Test
    void undefinedVariableProducesClearError() {
        // $panel is not defined — JLine resolves it to null and passes null into
        // input.args(). Should surface as a friendly message, not a raw NPE on
        // startsWith().
        def thrown = shouldFail(IllegalArgumentException) {
            system.execute('/img $panel')
        }
        assert thrown.message.contains('null')
        assert !thrown.message.contains('startsWith')
    }

    @Test
    void trailingWidthFlagWithoutValueProducesClearError() {
        // `/img --width` (no value, no positional) used to walk past the end
        // of args() and surface as an opaque ArrayIndexOutOfBoundsException
        // via saveException. Should now be a targeted IllegalArgumentException
        // naming the missing flag.
        def thrown = shouldFail(IllegalArgumentException) {
            system.execute('/img --width')
        }
        assert thrown.message.contains('--width')
        assert thrown.message.contains('missing value')
    }

    /** A test stand-in for JFreeChart-shaped objects. */
    static class ChartLikeForTest {
        BufferedImage createBufferedImage(int w, int h) {
            new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB)
        }
    }

    /** A test stand-in for Smile-Figure-shaped objects. */
    static class FigureLikeForTest {
        BufferedImage toBufferedImage(int w, int h) {
            new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB)
        }
    }
}
