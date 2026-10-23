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
package org.apache.groovy.groovyfx

import javafx.application.Platform
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.chart.CategoryAxis
import javafx.scene.chart.LineChart
import javafx.scene.chart.NumberAxis
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.Tab
import javafx.scene.effect.DropShadow
import javafx.scene.layout.BorderPane
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.text.Font
import javafx.scene.transform.Rotate
import javafx.stage.Stage
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledForJreRange
import org.junit.jupiter.api.condition.JRE

import static groovy.test.GroovyAssert.shouldFail

/**
 * Builder tests need the toolkit; the build runs them under Monocle's headless
 * platform, whose 21.x builds require Java 21.
 */
@EnabledForJreRange(min = JRE.JAVA_21, disabledReason = 'openjfx-monocle 21.x needs Java 21')
final class SceneGraphBuilderTest {

    @BeforeAll
    static void keepToolkitAlive() {
        // closing the last window must not shut the toolkit down for the rest of the JVM
        GroovyFX.ensureToolkit()
        Platform.implicitExit = false
    }

    @Test
    void buildsAStageSceneAndControls() {
        def fired = []
        def b = SceneGraphBuilder.fxBuilder {
            stage(id: 'main', title: 'Test') {
                scene(id: 'sc', width: 200, height: 100, fill: 'lightgray') {
                    vbox(id: 'box', spacing: 5, padding: 10) {
                        label('Hello', id: 'lbl', textFill: 'red', font: 18)
                        button('Click', id: 'btn', onAction: { fired << it })
                    }
                }
            }
        }
        assert b.main instanceof Stage
        assert b.main.title == 'Test'
        assert b.main.scene.is(b.sc)
        assert b.sc.width == 200 && b.sc.height == 100
        assert b.sc.fill == Color.LIGHTGRAY
        assert b.sc.root.is(b.box)
        assert b.box instanceof VBox
        assert b.box.spacing == 5
        assert b.box.padding == new Insets(10)
        assert b.box.children == [b.lbl, b.btn]
        assert b.lbl.text == 'Hello'
        assert b.lbl.textFill == Color.RED
        assert b.lbl.font == Font.font(18)
        assert b.lbl.id == 'lbl'
        b.btn.fire()
        assert fired.size() == 1
    }

    @Test
    void sceneWithoutParentRootGetsAGroup() {
        def b = SceneGraphBuilder.fxBuilder {
            scene(id: 'sc') {
                rectangle(id: 'r1', width: 10, height: 10)
                rectangle(id: 'r2', width: 10, height: 10)
            }
        }
        assert b.sc.root instanceof Group
        assert b.sc.root.children == [b.r1, b.r2]
    }

    @Test
    void sceneWithParentFollowedBySiblingKeepsTheGroupRoot() {
        def b = SceneGraphBuilder.fxBuilder {
            scene(id: 'sc') {
                vbox(id: 'box') { label('a', id: 'a') }
                label('b', id: 'b')
            }
        }
        assert b.sc.root instanceof Group
        assert b.sc.root.children == [b.box, b.b]
        assert b.box.children == [b.a]
    }

    @Test
    void sceneWithTwoPanesKeepsThemAsSiblings() {
        def b = SceneGraphBuilder.fxBuilder {
            scene(id: 'sc') {
                vbox(id: 'first') { label('a') }
                hbox(id: 'second') { label('b') }
            }
        }
        assert b.sc.root instanceof Group
        assert b.sc.root.children == [b.first, b.second]
        assert b.first.children.size() == 1 && b.second.children.size() == 1
    }

    @Test
    void explicitRootReceivesTheChildren() {
        def pane = new VBox()
        def b = SceneGraphBuilder.fxBuilder {
            scene(id: 'sc', root: pane) {
                hbox(id: 'row')
                label('b', id: 'b')
            }
        }
        assert b.sc.root.is(pane)
        assert pane.children == [b.row, b.b]
    }

    @Test
    void observableAttributesBindAndListsReplace() {
        def name = new SimpleStringProperty('Ada')
        def count = new SimpleIntegerProperty(3)
        def b = SceneGraphBuilder.fxBuilder {
            vbox {
                label(id: 'direct', text: name)
                label(id: 'viaBind', text: bind(name))
                label(id: 'converted', text: bind(name, converter: { "Name: $it" }))
                label(id: 'number', text: count)
                listView(id: 'list', items: ['a', 'b'])
                button(id: 'styled', styleClass: ['primary', 'large'])
            }
        }
        assert b.direct.text == 'Ada'
        assert b.viaBind.text == 'Ada'
        assert b.converted.text == 'Name: Ada'
        assert b.number.text == '3'
        name.set('Grace')
        count.set(4)
        assert b.direct.text == 'Grace'
        assert b.viaBind.text == 'Grace'
        assert b.converted.text == 'Name: Grace'
        assert b.number.text == '4'
        assert b.list.items == ['a', 'b']
        assert b.styled.styleClass == ['primary', 'large']
    }

    @Test
    void gridPaneConstraintsComeFromChildAttributes() {
        def b = SceneGraphBuilder.fxBuilder {
            gridPane(id: 'grid', hgap: 4) {
                label('Name', id: 'nameLabel', row: 0, column: 0, halignment: 'RIGHT')
                textField(id: 'nameField', row: 0, column: 1, columnSpan: 2, hgrow: 'ALWAYS', margin: [2, 4])
            }
        }
        assert b.grid instanceof GridPane
        assert b.grid.children == [b.nameLabel, b.nameField]
        assert GridPane.getRowIndex(b.nameLabel) == 0
        assert GridPane.getColumnIndex(b.nameLabel) == 0
        assert GridPane.getHalignment(b.nameLabel) == HPos.RIGHT
        assert GridPane.getColumnIndex(b.nameField) == 1
        assert GridPane.getColumnSpan(b.nameField) == 2
        assert GridPane.getHgrow(b.nameField) == Priority.ALWAYS
        assert GridPane.getMargin(b.nameField) == new Insets(2, 4, 2, 4)
    }

    @Test
    void boxConstraintsAndPropertyNameCollisions() {
        def b = SceneGraphBuilder.fxBuilder {
            hbox(id: 'row', alignment: 'CENTER_LEFT') {
                textField(id: 'grows', hgrow: 'ALWAYS')
                label('fixed', id: 'fixed', alignment: 'CENTER_RIGHT')
            }
        }
        assert b.row instanceof HBox
        assert b.row.alignment == Pos.CENTER_LEFT
        assert HBox.getHgrow(b.grows) == Priority.ALWAYS
        // alignment is a property of Labeled, so it sets the label's own alignment
        assert b.fixed.alignment == Pos.CENTER_RIGHT
    }

    @Test
    void borderPanePositions() {
        def b = SceneGraphBuilder.fxBuilder {
            borderPane(id: 'bp') {
                top { label('header', id: 'header') }
                center(margin: 8, alignment: 'TOP_LEFT') { textArea(id: 'body') }
                bottom { label('footer', id: 'footer') }
            }
        }
        assert b.bp.top.is(b.header)
        assert b.bp.center.is(b.body)
        assert b.bp.bottom.is(b.footer)
        assert b.bp.left == null
        assert BorderPane.getMargin(b.body) == new Insets(8)
        assert BorderPane.getAlignment(b.body) == Pos.TOP_LEFT
    }

    @Test
    void tabsMenusAndContent() {
        def b = SceneGraphBuilder.fxBuilder {
            vbox {
                menuBar(id: 'bar') {
                    menu('File', id: 'file') {
                        menuItem('Open', id: 'open')
                        separatorMenuItem()
                        menuItem('Exit')
                    }
                }
                tabPane(id: 'tabs') {
                    tab('One', id: 'one') { label('first', id: 'first') }
                    tab('Two') { scrollPane(id: 'scroll') { label('scrolled', id: 'scrolled') } }
                }
                button('With tooltip', id: 'tipped') { tooltip('helpful', id: 'tip') }
            }
        }
        assert b.bar.menus == [b.file]
        assert b.file.items.size() == 3
        assert b.file.items[0].is(b.open)
        assert b.tabs.tabs.size() == 2
        assert b.one instanceof Tab
        assert b.one.content.is(b.first)
        assert b.scroll.content.is(b.scrolled)
        assert b.tipped.tooltip.is(b.tip)
        assert b.tip.text == 'helpful'
    }

    @Test
    void shapesEffectsAndTransforms() {
        def b = SceneGraphBuilder.fxBuilder {
            group {
                rectangle(id: 'rect', width: 20, height: 10, fill: '#ff0000', stroke: 'blue',
                    effect: dropShadow(radius: 3), transforms: [rotate(angle: 45)])
                circle(id: 'c', radius: 5, fill: Color.GREEN)
                polygon(id: 'poly', points: [0, 0, 10, 0, 5, 5])
                path(id: 'p') {
                    moveTo(x: 0, y: 0)
                    lineTo(x: 10, y: 10)
                    closePath()
                }
            }
        }
        assert b.rect instanceof Rectangle
        assert b.rect.fill == Color.RED
        assert b.rect.stroke == Color.BLUE
        assert b.rect.effect instanceof DropShadow
        assert b.rect.transforms.size() == 1 && b.rect.transforms[0] instanceof Rotate
        assert b.c.fill == Color.GREEN
        assert b.poly.points == [0, 0, 10, 0, 5, 5]
        assert b.p.elements.size() == 3
    }

    @Test
    void chartsSeriesAndAxes() {
        def b = SceneGraphBuilder.fxBuilder {
            lineChart(id: 'chart', title: 'Trend', xAxis: categoryAxis(label: 'Month'), yAxis: numberAxis(label: 'Sales')) {
                series('2026', id: 's1', data: [Jan: 10, Feb: 14])
                series('2025', data: [['Jan', 8], ['Feb', 9]])
            }
            pieChart(id: 'pie', data: [Groovy: 60, Java: 40])
        }
        assert b.chart instanceof LineChart
        assert b.chart.title == 'Trend'
        assert b.chart.XAxis instanceof CategoryAxis && b.chart.XAxis.label == 'Month'
        assert b.chart.YAxis instanceof NumberAxis
        assert b.chart.data.size() == 2
        assert b.chart.data[0].is(b.s1)
        assert b.s1.data*.XValue == ['Jan', 'Feb']
        assert b.s1.data*.YValue == [10, 14]
        assert b.pie.data*.name == ['Groovy', 'Java']
        assert b.pie.data*.pieValue == [60.0d, 40.0d]
    }

    @Test
    void tableColumnsAndPassThroughNodes() {
        def existing = new Button('existing')
        def b = SceneGraphBuilder.fxBuilder {
            vbox {
                tableView(id: 'table', items: [[name: 'Ada', age: 36]]) {
                    tableColumn('Name', id: 'nameCol', property: 'name')
                    tableColumn('Age', property: 'age')
                }
                node(existing, id: 'reused', text: 'renamed')
            }
        }
        assert b.table.columns.size() == 2
        assert b.table.columns[0].is(b.nameCol)
        assert b.nameCol.cellValueFactory != null
        assert b.reused.is(existing)
        assert existing.text == 'renamed'
    }

    @Test
    void showAttributeShowsTheStageWhenComplete() {
        def b = SceneGraphBuilder.fxBuilder {
            stage(id: 'win', show: true) {
                scene { label('shown') }
            }
        }
        assert b.win.showing
        b.fx { b.win.close() }
    }

    @Test
    void helpfulErrorsForMisuse() {
        def message = shouldFail(IllegalArgumentException) {
            SceneGraphBuilder.fxBuilder {
                textField { button('nested') }
            }
        }.message
        assert message.contains('Cannot add a Button to a TextField')

        // FactoryBuilderSupport wraps failures raised while creating a node
        def wrapped = shouldFail(RuntimeException) {
            SceneGraphBuilder.fxBuilder {
                vbox { top { label('x') } }
            }
        }
        assert wrapped.cause instanceof IllegalArgumentException
        assert wrapped.message.contains("'top' must be nested directly inside a borderPane")

        // a chart's only constructor arguments are its axes, so a positional value is not silently dropped
        wrapped = shouldFail(RuntimeException) {
            SceneGraphBuilder.fxBuilder {
                lineChart('Sales')
            }
        }
        assert wrapped.cause instanceof IllegalArgumentException
        assert wrapped.message.contains("The value of 'lineChart' must be a LineChart. Found: java.lang.String")

        message = shouldFail(IllegalArgumentException) {
            SceneGraphBuilder.fxBuilder {
                bean(new StringBuilder(), length: new SimpleStringProperty('x'))
            }
        }.message
        assert message.contains("Cannot bind attribute 'length' of StringBuilder: it has no lengthProperty() method")
    }

    @Test
    void fxRunsOnTheApplicationThreadAndPropagatesFailures() {
        def builder = new SceneGraphBuilder()
        def onFxThread = false
        builder.fx { onFxThread = Platform.fxApplicationThread }
        assert onFxThread

        def error = shouldFail(IllegalStateException) {
            builder.fx { throw new IllegalStateException('boom') }
        }
        assert error.message == 'boom'
    }

    @Test
    void javaFXIsDetected() {
        assert FXRuntime.isAvailable()
        GroovyFX.ensureToolkit()
        assert new Label('x').text == 'x'
    }
}
