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
import javafx.geometry.HPos
import javafx.geometry.Pos
import javafx.geometry.VPos
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.canvas.Canvas
import javafx.scene.chart.AreaChart
import javafx.scene.chart.BarChart
import javafx.scene.chart.BubbleChart
import javafx.scene.chart.CategoryAxis
import javafx.scene.chart.LineChart
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.ScatterChart
import javafx.scene.chart.StackedAreaChart
import javafx.scene.chart.StackedBarChart
import javafx.scene.control.Accordion
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.CheckMenuItem
import javafx.scene.control.ChoiceBox
import javafx.scene.control.ColorPicker
import javafx.scene.control.ComboBox
import javafx.scene.control.ContextMenu
import javafx.scene.control.DatePicker
import javafx.scene.control.Hyperlink
import javafx.scene.control.Label
import javafx.scene.control.ListView
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuButton
import javafx.scene.control.MenuItem
import javafx.scene.control.PasswordField
import javafx.scene.control.ProgressBar
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.RadioButton
import javafx.scene.control.RadioMenuItem
import javafx.scene.control.ScrollPane
import javafx.scene.control.Separator
import javafx.scene.control.SeparatorMenuItem
import javafx.scene.control.Slider
import javafx.scene.control.Spinner
import javafx.scene.control.SplitMenuButton
import javafx.scene.control.SplitPane
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.control.TableView
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.control.TitledPane
import javafx.scene.control.ToggleButton
import javafx.scene.control.ToolBar
import javafx.scene.control.Tooltip
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.effect.Bloom
import javafx.scene.effect.BoxBlur
import javafx.scene.effect.ColorAdjust
import javafx.scene.effect.DropShadow
import javafx.scene.effect.GaussianBlur
import javafx.scene.effect.Glow
import javafx.scene.effect.InnerShadow
import javafx.scene.effect.Lighting
import javafx.scene.effect.MotionBlur
import javafx.scene.effect.Reflection
import javafx.scene.effect.SepiaTone
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.FlowPane
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import javafx.scene.layout.TilePane
import javafx.scene.layout.VBox
import javafx.scene.shape.Arc
import javafx.scene.shape.ArcTo
import javafx.scene.shape.Circle
import javafx.scene.shape.ClosePath
import javafx.scene.shape.CubicCurve
import javafx.scene.shape.CubicCurveTo
import javafx.scene.shape.Ellipse
import javafx.scene.shape.HLineTo
import javafx.scene.shape.Line
import javafx.scene.shape.LineTo
import javafx.scene.shape.MoveTo
import javafx.scene.shape.Path
import javafx.scene.shape.Polygon
import javafx.scene.shape.Polyline
import javafx.scene.shape.QuadCurve
import javafx.scene.shape.QuadCurveTo
import javafx.scene.shape.Rectangle
import javafx.scene.shape.SVGPath
import javafx.scene.shape.VLineTo
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import javafx.scene.transform.Affine
import javafx.scene.transform.Rotate
import javafx.scene.transform.Scale
import javafx.scene.transform.Shear
import javafx.scene.transform.Translate
import org.apache.groovy.groovyfx.factory.BindFactory
import org.apache.groovy.groovyfx.factory.BorderPanePositionFactory
import org.apache.groovy.groovyfx.factory.FXAttributes
import org.apache.groovy.groovyfx.factory.NodeFactory
import org.apache.groovy.groovyfx.factory.PassThruFactory
import org.apache.groovy.groovyfx.factory.PieChartFactory
import org.apache.groovy.groovyfx.factory.SceneFactory
import org.apache.groovy.groovyfx.factory.SeriesFactory
import org.apache.groovy.groovyfx.factory.StageFactory
import org.apache.groovy.groovyfx.factory.TableColumnFactory
import org.apache.groovy.groovyfx.factory.XYChartFactory
import org.apache.groovy.lang.annotation.Incubating
import org.codehaus.groovy.runtime.InvokerHelper

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Builder that exposes JavaFX stages, scenes, layouts, controls, shapes,
 * charts, bindings and threading helpers through a Groovy DSL, in the manner
 * of {@code SwingBuilder}.
 * <p>
 * Most nodes are created by a single reflective factory: the node name maps
 * to a JavaFX class, a single unnamed argument is passed to a matching
 * constructor (or becomes the {@code text} of a control), named attributes
 * are set as properties, and nested nodes are attached to their parent
 * according to the parent's type. Factories exist only where JavaFX deviates
 * from that shape, such as scenes (whose size is fixed at construction),
 * {@code BorderPane} positions and charts.
 * <p>
 * Attribute values receive a few conveniences: an {@code ObservableValue}
 * given for a property is <em>bound</em> to it rather than assigned, a list
 * given for an {@code ObservableList} property replaces its contents,
 * strings are accepted for {@code Paint} properties, and numbers or lists for
 * {@code Insets}. Closures are coerced to the listener types JavaFX expects,
 * so {@code onAction: { ... }} just works.
 * <p>
 * Layout constraints are given as attributes of the child: {@code row},
 * {@code column}, {@code rowSpan}, {@code columnSpan}, {@code hgrow},
 * {@code vgrow}, {@code halignment}, {@code valignment}, {@code margin},
 * {@code fillWidth} and {@code fillHeight} inside a {@code gridPane};
 * {@code hgrow} or {@code vgrow} and {@code margin} inside an {@code hbox} or
 * {@code vbox}; {@code alignment} and {@code margin} inside a
 * {@code stackPane}, {@code borderPane} or {@code tilePane}; and
 * {@code topAnchor}, {@code bottomAnchor}, {@code leftAnchor} and
 * {@code rightAnchor} inside an {@code anchorPane}. A constraint name that
 * is also a property of the child (for example {@code alignment} on a
 * {@code Labeled}) sets the child's property instead.
 */
@Incubating
class SceneGraphBuilder extends FactoryBuilderSupport {

    /**
     * Builder variable holding the attribute name used to publish created nodes as builder variables.
     */
    static final String DELEGATE_PROPERTY_OBJECT_ID = '_delegateProperty:id'

    /**
     * Default attribute name used when no custom delegate property id is configured.
     */
    static final String DEFAULT_DELEGATE_PROPERTY_OBJECT_ID = 'id'

    /**
     * Creates a new builder and optionally performs the default initialization.
     *
     * @param init whether superclass initialization should run immediately
     */
    SceneGraphBuilder(boolean init = true) {
        super(init)
        FXRuntime.requireJavaFX()
        setVariable(DELEGATE_PROPERTY_OBJECT_ID, DEFAULT_DELEGATE_PROPERTY_OBJECT_ID)
    }

    /**
     * Registers pass-through nodes and the attribute delegates that do not create visible nodes.
     */
    def registerSupportNodes() {
        registerFactory('node', new PassThruFactory(Node, false))
        registerFactory('bean', new PassThruFactory(Object, true))
        addAttributeDelegate(SceneGraphBuilder.&objectIDAttributeDelegate)
    }

    /**
     * Registers the binding node.
     */
    def registerBinding() {
        registerFactory('bind', new BindFactory())
    }

    /**
     * Registers stages and scenes.
     */
    def registerStages() {
        registerFactory('stage', new StageFactory())
        registerFactory('scene', new SceneFactory())
    }

    /**
     * Registers layout containers, {@code BorderPane} positions and the layout-constraint attribute delegate.
     */
    def registerLayouts() {
        registerBeanFactory('group', Group)
        registerBeanFactory('pane', Pane)
        registerBeanFactory('region', Region)
        registerBeanFactory('hbox', HBox)
        registerBeanFactory('vbox', VBox)
        registerBeanFactory('stackPane', StackPane)
        registerBeanFactory('flowPane', FlowPane)
        registerBeanFactory('tilePane', TilePane)
        registerBeanFactory('anchorPane', AnchorPane)
        registerBeanFactory('gridPane', GridPane)
        registerBeanFactory('borderPane', BorderPane)
        ['top', 'bottom', 'left', 'right', 'center'].each { String position ->
            registerFactory(position, new BorderPanePositionFactory(position))
        }
        addAttributeDelegate(SceneGraphBuilder.&layoutConstraintsAttributeDelegate)
    }

    /**
     * Registers controls.
     */
    def registerControls() {
        registerBeanFactory('button', Button)
        registerBeanFactory('toggleButton', ToggleButton)
        registerBeanFactory('radioButton', RadioButton)
        registerBeanFactory('checkBox', CheckBox)
        registerBeanFactory('hyperlink', Hyperlink)
        registerBeanFactory('label', Label)
        registerBeanFactory('textField', TextField)
        registerBeanFactory('passwordField', PasswordField)
        registerBeanFactory('textArea', TextArea)
        registerBeanFactory('choiceBox', ChoiceBox)
        registerBeanFactory('comboBox', ComboBox)
        registerBeanFactory('listView', ListView)
        registerBeanFactory('treeView', TreeView)
        registerBeanFactory('treeItem', TreeItem)
        registerBeanFactory('tableView', TableView)
        registerFactory('tableColumn', new TableColumnFactory())
        registerBeanFactory('slider', Slider)
        registerBeanFactory('spinner', Spinner)
        registerBeanFactory('progressBar', ProgressBar)
        registerBeanFactory('progressIndicator', ProgressIndicator)
        registerBeanFactory('separator', Separator)
        registerBeanFactory('datePicker', DatePicker)
        registerBeanFactory('colorPicker', ColorPicker)
        registerBeanFactory('tooltip', Tooltip)
        registerBeanFactory('scrollPane', ScrollPane)
        registerBeanFactory('splitPane', SplitPane)
        registerBeanFactory('tabPane', TabPane)
        registerBeanFactory('tab', Tab)
        registerBeanFactory('titledPane', TitledPane)
        registerBeanFactory('accordion', Accordion)
        registerBeanFactory('toolBar', ToolBar)
    }

    /**
     * Registers menus and menu items.
     */
    def registerMenus() {
        registerBeanFactory('menuBar', MenuBar)
        registerBeanFactory('menu', Menu)
        registerBeanFactory('menuItem', MenuItem)
        registerBeanFactory('checkMenuItem', CheckMenuItem)
        registerBeanFactory('radioMenuItem', RadioMenuItem)
        registerBeanFactory('separatorMenuItem', SeparatorMenuItem)
        registerBeanFactory('contextMenu', ContextMenu)
        registerBeanFactory('menuButton', MenuButton)
        registerBeanFactory('splitMenuButton', SplitMenuButton)
    }

    /**
     * Registers shapes, text and path elements.
     */
    def registerShapes() {
        registerBeanFactory('rectangle', Rectangle)
        registerBeanFactory('circle', Circle)
        registerBeanFactory('ellipse', Ellipse)
        registerBeanFactory('line', Line)
        registerBeanFactory('polygon', Polygon)
        registerBeanFactory('polyline', Polyline)
        registerBeanFactory('arc', Arc)
        registerBeanFactory('cubicCurve', CubicCurve)
        registerBeanFactory('quadCurve', QuadCurve)
        registerBeanFactory('svgPath', SVGPath)
        registerBeanFactory('text', Text)
        registerBeanFactory('textFlow', TextFlow)
        registerBeanFactory('path', Path)
        registerBeanFactory('moveTo', MoveTo)
        registerBeanFactory('lineTo', LineTo)
        registerBeanFactory('hLineTo', HLineTo)
        registerBeanFactory('vLineTo', VLineTo)
        registerBeanFactory('arcTo', ArcTo)
        registerBeanFactory('quadCurveTo', QuadCurveTo)
        registerBeanFactory('cubicCurveTo', CubicCurveTo)
        registerBeanFactory('closePath', ClosePath)
    }

    /**
     * Registers images and the canvas.
     */
    def registerGraphics() {
        registerBeanFactory('image', Image)
        registerBeanFactory('imageView', ImageView)
        registerBeanFactory('canvas', Canvas)
    }

    /**
     * Registers charts, axes and series. Axes are passed to a chart through
     * its {@code xAxis} and {@code yAxis} attributes.
     */
    def registerCharts() {
        registerFactory('lineChart', new XYChartFactory(LineChart))
        registerFactory('areaChart', new XYChartFactory(AreaChart))
        registerFactory('barChart', new XYChartFactory(BarChart))
        registerFactory('scatterChart', new XYChartFactory(ScatterChart))
        registerFactory('bubbleChart', new XYChartFactory(BubbleChart))
        registerFactory('stackedAreaChart', new XYChartFactory(StackedAreaChart))
        registerFactory('stackedBarChart', new XYChartFactory(StackedBarChart))
        registerFactory('pieChart', new PieChartFactory())
        registerFactory('series', new SeriesFactory())
        registerBeanFactory('numberAxis', NumberAxis)
        registerBeanFactory('categoryAxis', CategoryAxis)
    }

    /**
     * Registers effects, for use as {@code effect:} attribute values.
     */
    def registerEffects() {
        registerBeanFactory('dropShadow', DropShadow)
        registerBeanFactory('innerShadow', InnerShadow)
        registerBeanFactory('reflection', Reflection)
        registerBeanFactory('gaussianBlur', GaussianBlur)
        registerBeanFactory('boxBlur', BoxBlur)
        registerBeanFactory('motionBlur', MotionBlur)
        registerBeanFactory('bloom', Bloom)
        registerBeanFactory('glow', Glow)
        registerBeanFactory('colorAdjust', ColorAdjust)
        registerBeanFactory('sepiaTone', SepiaTone)
        registerBeanFactory('lighting', Lighting)
    }

    /**
     * Registers transforms, for use in {@code transforms:} attribute lists.
     */
    def registerTransforms() {
        registerBeanFactory('rotate', Rotate)
        registerBeanFactory('scale', Scale)
        registerBeanFactory('translate', Translate)
        registerBeanFactory('shear', Shear)
        registerBeanFactory('affine', Affine)
    }

    /**
     * Registers the threading helper methods {@code fx} and {@code fxLater}.
     */
    def registerThreading() {
        registerExplicitMethod 'fx', this.&fx
        registerExplicitMethod 'fxLater', this.&fxLater
    }

    /**
     * Registers a bean factory backed by the reflective {@link NodeFactory}.
     *
     * @param nodeName the builder node name
     * @param groupName the factory group name
     * @param klass the bean class to register
     */
    @Override
    void registerBeanFactory(String nodeName, String groupName, Class klass) {
        registerFactory(nodeName, groupName, new NodeFactory(klass))
    }

    /**
     * Runs a closure on the JavaFX application thread and waits for it to finish,
     * starting the toolkit if necessary. When already on the application thread
     * the closure runs immediately.
     *
     * @param c the closure to run
     * @return this builder
     */
    SceneGraphBuilder fx(@DelegatesTo(SceneGraphBuilder) Closure c) {
        GroovyFX.ensureToolkit()
        c.delegate = this
        if (Platform.fxApplicationThread) {
            c.call(this)
            return this
        }
        Map<String, Object> continuationData = getContinuationData()
        Throwable failure = null
        CountDownLatch done = new CountDownLatch(1)
        Platform.runLater {
            try {
                restoreFromContinuationData(continuationData)
                c.call(this)
            } catch (Throwable t) {
                failure = t
            } finally {
                done.countDown()
            }
        }
        if (!done.await(GroovyFX.TOOLKIT_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            throw new IllegalStateException("The JavaFX application thread did not run the closure within ${GroovyFX.TOOLKIT_TIMEOUT_SECONDS} seconds")
        }
        if (failure != null) {
            throw failure
        }
        this
    }

    /**
     * Schedules a closure to run on the JavaFX application thread, starting the toolkit if necessary.
     *
     * @param c the closure to run
     * @return this builder
     */
    SceneGraphBuilder fxLater(@DelegatesTo(SceneGraphBuilder) Closure c) {
        GroovyFX.ensureToolkit()
        c.delegate = this
        Platform.runLater { c.call(this) }
        this
    }

    /**
     * Creates a new builder and runs the supplied closure against it on the JavaFX application thread.
     *
     * @param c the closure to execute with the new builder
     * @return the initialized builder
     */
    static SceneGraphBuilder fxBuilder(@DelegatesTo(SceneGraphBuilder) Closure c) {
        new SceneGraphBuilder().fx(c)
    }

    /**
     * Publishes created nodes into the builder variable namespace using the configured id attribute,
     * and gives scene-graph nodes that id for CSS lookup when they have none.
     *
     * @param builder the active builder
     * @param node the created node
     * @param attributes the node attributes to inspect and mutate
     */
    static objectIDAttributeDelegate(FactoryBuilderSupport builder, Object node, Map attributes) {
        String idAttr = builder.getVariables()[DELEGATE_PROPERTY_OBJECT_ID] ?: DEFAULT_DELEGATE_PROPERTY_OBJECT_ID
        Object theID = attributes.remove(idAttr)
        if (theID) {
            builder.setVariable(theID.toString(), node)
            if (node instanceof Node && node.id == null) {
                node.id = theID.toString()
            }
        }
    }

    /**
     * Applies layout-constraint attributes of a child according to the container being built.
     *
     * @param builder the active builder
     * @param node the child node
     * @param attributes the node attributes to inspect and mutate
     */
    static layoutConstraintsAttributeDelegate(FactoryBuilderSupport builder, Object node, Map attributes) {
        if (!(node instanceof Node) || attributes.isEmpty()) return
        Object container = builder.current
        if (!(container instanceof Node)) return
        Node child = node
        MetaClass metaClass = InvokerHelper.getMetaClass(node)
        // a constraint that is also a property of the child is the child's property
        Closure constraint = { String key ->
            attributes.containsKey(key) && metaClass.hasProperty(node, key) == null ? attributes.remove(key) : null
        }
        Object v
        switch (container) {
            case GridPane:
                if ((v = constraint('row')) != null) GridPane.setRowIndex(child, v as int)
                if ((v = constraint('column')) != null) GridPane.setColumnIndex(child, v as int)
                if ((v = constraint('rowSpan')) != null) GridPane.setRowSpan(child, v as int)
                if ((v = constraint('columnSpan')) != null) GridPane.setColumnSpan(child, v as int)
                if ((v = constraint('halignment')) != null) GridPane.setHalignment(child, enumValue(HPos, v))
                if ((v = constraint('valignment')) != null) GridPane.setValignment(child, enumValue(VPos, v))
                if ((v = constraint('hgrow')) != null) GridPane.setHgrow(child, enumValue(Priority, v))
                if ((v = constraint('vgrow')) != null) GridPane.setVgrow(child, enumValue(Priority, v))
                if ((v = constraint('fillWidth')) != null) GridPane.setFillWidth(child, v as boolean)
                if ((v = constraint('fillHeight')) != null) GridPane.setFillHeight(child, v as boolean)
                if ((v = constraint('margin')) != null) GridPane.setMargin(child, FXAttributes.toInsets(v))
                break
            case HBox:
                if ((v = constraint('hgrow')) != null) HBox.setHgrow(child, enumValue(Priority, v))
                if ((v = constraint('margin')) != null) HBox.setMargin(child, FXAttributes.toInsets(v))
                break
            case VBox:
                if ((v = constraint('vgrow')) != null) VBox.setVgrow(child, enumValue(Priority, v))
                if ((v = constraint('margin')) != null) VBox.setMargin(child, FXAttributes.toInsets(v))
                break
            case StackPane:
                if ((v = constraint('alignment')) != null) StackPane.setAlignment(child, enumValue(Pos, v))
                if ((v = constraint('margin')) != null) StackPane.setMargin(child, FXAttributes.toInsets(v))
                break
            case BorderPane:
                if ((v = constraint('alignment')) != null) BorderPane.setAlignment(child, enumValue(Pos, v))
                if ((v = constraint('margin')) != null) BorderPane.setMargin(child, FXAttributes.toInsets(v))
                break
            case TilePane:
                if ((v = constraint('alignment')) != null) TilePane.setAlignment(child, enumValue(Pos, v))
                if ((v = constraint('margin')) != null) TilePane.setMargin(child, FXAttributes.toInsets(v))
                break
            case FlowPane:
                if ((v = constraint('margin')) != null) FlowPane.setMargin(child, FXAttributes.toInsets(v))
                break
            case AnchorPane:
                if ((v = constraint('topAnchor')) != null) AnchorPane.setTopAnchor(child, v as Double)
                if ((v = constraint('bottomAnchor')) != null) AnchorPane.setBottomAnchor(child, v as Double)
                if ((v = constraint('leftAnchor')) != null) AnchorPane.setLeftAnchor(child, v as Double)
                if ((v = constraint('rightAnchor')) != null) AnchorPane.setRightAnchor(child, v as Double)
                break
        }
    }

    private static <E extends Enum<E>> E enumValue(Class<E> type, Object value) {
        type.isInstance(value) ? (E) value : Enum.valueOf(type, value.toString().toUpperCase(Locale.ROOT))
    }
}
