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
package groovy.swing

import groovy.swing.factory.ActionFactory
import groovy.swing.factory.BevelBorderFactory
import groovy.swing.factory.BindFactory
import groovy.swing.factory.BindGroupFactory
import groovy.swing.factory.BindProxyFactory
import groovy.swing.factory.BoxFactory
import groovy.swing.factory.BoxLayoutFactory
import groovy.swing.factory.ButtonGroupFactory
import groovy.swing.factory.CellEditorFactory
import groovy.swing.factory.CellEditorGetValueFactory
import groovy.swing.factory.CellEditorPrepareFactory
import groovy.swing.factory.ClosureColumnFactory
import groovy.swing.factory.CollectionFactory
import groovy.swing.factory.ColumnFactory
import groovy.swing.factory.ColumnModelFactory
import groovy.swing.factory.ComboBoxFactory
import groovy.swing.factory.ComponentFactory
import groovy.swing.factory.CompoundBorderFactory
import groovy.swing.factory.DialogFactory
import groovy.swing.factory.EmptyBorderFactory
import groovy.swing.factory.EtchedBorderFactory
import groovy.swing.factory.FormattedTextFactory
import groovy.swing.factory.FrameFactory
import groovy.swing.factory.GlueFactory
import groovy.swing.factory.GridBagFactory
import groovy.swing.factory.HBoxFactory
import groovy.swing.factory.HGlueFactory
import groovy.swing.factory.HStrutFactory
import groovy.swing.factory.ImageIconFactory
import groovy.swing.factory.InternalFrameFactory
import groovy.swing.factory.LayoutFactory
import groovy.swing.factory.LineBorderFactory
import groovy.swing.factory.ListFactory
import groovy.swing.factory.MapFactory
import groovy.swing.factory.MatteBorderFactory
import groovy.swing.factory.PropertyColumnFactory
import groovy.swing.factory.RendererFactory
import groovy.swing.factory.RendererUpdateFactory
import groovy.swing.factory.RichActionWidgetFactory
import groovy.swing.factory.RigidAreaFactory
import groovy.swing.factory.ScrollPaneFactory
import groovy.swing.factory.SeparatorFactory
import groovy.swing.factory.SplitPaneFactory
import groovy.swing.factory.TDFactory
import groovy.swing.factory.TRFactory
import groovy.swing.factory.TabbedPaneFactory
import groovy.swing.factory.TableFactory
import groovy.swing.factory.TableLayoutFactory
import groovy.swing.factory.TableModelFactory
import groovy.swing.factory.TextArgWidgetFactory
import groovy.swing.factory.TitledBorderFactory
import groovy.swing.factory.VBoxFactory
import groovy.swing.factory.VGlueFactory
import groovy.swing.factory.VStrutFactory
import groovy.swing.factory.WidgetFactory
import groovy.swing.factory.WindowFactory
import org.codehaus.groovy.runtime.MethodClosure

import javax.swing.*
import javax.swing.border.BevelBorder
import javax.swing.border.EtchedBorder
import javax.swing.table.TableColumn
import java.awt.*
import java.lang.reflect.InvocationTargetException
import java.util.logging.Logger

/**
 * Builder that exposes Swing components, layouts, bindings, and threading helpers through a Groovy DSL.
 */
class SwingBuilder extends FactoryBuilderSupport {

    private static final Logger LOG = Logger.getLogger(SwingBuilder.name)
    private static boolean headless = false

    /**
     * Context key containing the attribute name used to publish created nodes as builder variables.
     */
    static final String DELEGATE_PROPERTY_OBJECT_ID = "_delegateProperty:id"
    /**
     * Default attribute name used when no custom delegate property id is configured.
     */
    static final String DEFAULT_DELEGATE_PROPERTY_OBJECT_ID = "id"

    private static final Random random = new Random()

    /**
     * Creates a new Swing builder and optionally performs the default initialization.
     *
     * @param init whether superclass initialization should run immediately
     */
    SwingBuilder(boolean init = true) {
        super(init)
        headless = GraphicsEnvironment.isHeadless()
        containingWindows = new LinkedList()
        this[DELEGATE_PROPERTY_OBJECT_ID] = DEFAULT_DELEGATE_PROPERTY_OBJECT_ID
    }

    /**
     * Registers support nodes, explicit methods, and attribute delegates that do not create visible widgets.
     */
    def registerSupportNodes() {
        registerFactory("action", new ActionFactory())
        registerFactory("actions", new CollectionFactory())
        registerFactory("map", new MapFactory())
        registerFactory("imageIcon", new ImageIconFactory())
        registerFactory("buttonGroup", new ButtonGroupFactory())
        addAttributeDelegate(ButtonGroupFactory.&buttonGroupAttributeDelegate)

        //object id delegate, for propertyNotFound
        addAttributeDelegate(SwingBuilder.&objectIDAttributeDelegate)

        addAttributeDelegate(SwingBuilder.&clientPropertyAttributeDelegate)
        registerFactory("noparent", new CollectionFactory())
        registerExplicitMethod("keyStrokeAction", this.&createKeyStrokeAction)
        registerExplicitMethod("shortcut", this.&shortcut)
    }

    /**
     * Registers binding-related factories and delegates.
     */
    def registerBinding() {
        BindFactory bindFactory = new BindFactory()
        registerFactory("bind", bindFactory)
        addAttributeDelegate(bindFactory.&bindingAttributeDelegate)
        registerFactory("bindProxy", new BindProxyFactory())
        registerFactory ("bindGroup", new BindGroupFactory())
    }

    /**
     * Registers pass-through nodes that wrap existing beans or components.
     */
    def registerPassThruNodes() {
        registerFactory("widget", new WidgetFactory(Component, true))
        registerFactory("container", new WidgetFactory(Component, false))
        registerFactory("bean", new WidgetFactory(Object, true))
    }


    /**
     * Registers top-level window factories.
     */
    def registerWindows() {
        registerFactory("dialog", new DialogFactory())
        registerBeanFactory("fileChooser", JFileChooser)
        registerFactory("frame", new FrameFactory())
        registerBeanFactory("optionPane", JOptionPane)
        registerFactory("window", new WindowFactory())
    }

    /**
     * Registers button-like widgets that can share Swing actions.
     */
    def registerActionButtonWidgets() {
        registerFactory("button", new RichActionWidgetFactory(JButton))
        registerFactory("checkBox", new RichActionWidgetFactory(JCheckBox))
        registerFactory("checkBoxMenuItem", new RichActionWidgetFactory(JCheckBoxMenuItem))
        registerFactory("menuItem", new RichActionWidgetFactory(JMenuItem))
        registerFactory("radioButton", new RichActionWidgetFactory(JRadioButton))
        registerFactory("radioButtonMenuItem", new RichActionWidgetFactory(JRadioButtonMenuItem))
        registerFactory("toggleButton", new RichActionWidgetFactory(JToggleButton))
    }

    /**
     * Registers text-oriented Swing widgets.
     */
    def registerTextWidgets() {
        registerFactory("editorPane", new TextArgWidgetFactory(JEditorPane))
        registerFactory("label", new TextArgWidgetFactory(JLabel))
        registerFactory("passwordField", new TextArgWidgetFactory(JPasswordField))
        registerFactory("textArea", new TextArgWidgetFactory(JTextArea))
        registerFactory("textField", new TextArgWidgetFactory(JTextField))
        registerFactory("formattedTextField", new FormattedTextFactory())
        registerFactory("textPane", new TextArgWidgetFactory(JTextPane))
    }

    /**
     * Registers desktop and internal-frame widgets used for MDI applications.
     */
    def registerMDIWidgets() {
        registerBeanFactory("desktopPane", JDesktopPane)
        registerFactory("internalFrame", new InternalFrameFactory())
    }

    /**
     * Registers basic selection and value widgets.
     */
    def registerBasicWidgets() {
        registerBeanFactory("colorChooser", JColorChooser)

        registerFactory("comboBox", new ComboBoxFactory())
        registerFactory("list", new ListFactory())
        registerBeanFactory("progressBar", JProgressBar)
        registerFactory("separator", new SeparatorFactory())
        registerBeanFactory("scrollBar", JScrollBar)
        registerBeanFactory("slider", JSlider)
        registerBeanFactory("spinner", JSpinner)
        registerBeanFactory("tree", JTree)
    }

    /**
     * Registers menu-related widgets.
     */
    def registerMenuWidgets() {
        registerBeanFactory("menu", JMenu)
        registerBeanFactory("menuBar", JMenuBar)
        registerBeanFactory("popupMenu", JPopupMenu)
    }

    /**
     * Registers container widgets that can host child components.
     */
    def registerContainers() {
        registerBeanFactory("panel", JPanel)
        registerFactory("scrollPane", new ScrollPaneFactory())
        registerFactory("splitPane", new SplitPaneFactory())
        registerFactory("tabbedPane", new TabbedPaneFactory(JTabbedPane))
        registerBeanFactory("toolBar", JToolBar)
        registerBeanFactory("viewport", JViewport) // sub class?
        registerBeanFactory("layeredPane", JLayeredPane)
    }

    /**
     * Registers standard Swing data model nodes.
     */
    def registerDataModels() {
        registerBeanFactory("boundedRangeModel", DefaultBoundedRangeModel)

        // spinner models
        registerBeanFactory("spinnerDateModel", SpinnerDateModel)
        registerBeanFactory("spinnerListModel", SpinnerListModel)
        registerBeanFactory("spinnerNumberModel", SpinnerNumberModel)
    }

    /**
     * Registers tables, columns, and table model helper nodes.
     */
    def registerTableComponents() {
        registerFactory("table", new TableFactory())
        registerBeanFactory("tableColumn", TableColumn)
        registerFactory("tableModel", new TableModelFactory())
        registerFactory("propertyColumn", new PropertyColumnFactory())
        registerFactory("closureColumn", new ClosureColumnFactory())
        registerFactory("columnModel", new ColumnModelFactory())
        registerFactory("column", new ColumnFactory())
    }

    /**
     * Registers core Swing layout managers and their attribute delegates.
     */
    def registerBasicLayouts() {
        registerFactory("borderLayout", new LayoutFactory(BorderLayout))
        registerFactory("cardLayout", new LayoutFactory(CardLayout))
        registerFactory("flowLayout", new LayoutFactory(FlowLayout))
        registerFactory("gridLayout", new LayoutFactory(GridLayout))
        registerFactory("overlayLayout", new LayoutFactory(OverlayLayout))
        registerFactory("springLayout", new LayoutFactory(SpringLayout))

        registerFactory("gridBagLayout", new GridBagFactory())
        registerBeanFactory("gridBagConstraints", GridBagConstraints)
        registerBeanFactory("gbc", GridBagConstraints) // shortcut name
        // constraints delegate
        addAttributeDelegate(GridBagFactory.&processGridBagConstraintsAttributes)

        addAttributeDelegate(LayoutFactory.&constraintsAttributeDelegate)
    }

    /**
     * Registers {@code BoxLayout}-based containers and spacing helpers.
     */
    def registerBoxLayout() {
        registerFactory("boxLayout", new BoxLayoutFactory())
        registerFactory("box", new BoxFactory())
        registerFactory("hbox", new HBoxFactory())
        registerFactory("hglue", new HGlueFactory())
        registerFactory("hstrut", new HStrutFactory())
        registerFactory("vbox", new VBoxFactory())
        registerFactory("vglue", new VGlueFactory())
        registerFactory("vstrut", new VStrutFactory())
        registerFactory("glue", new GlueFactory())
        registerFactory("rigidArea", new RigidAreaFactory())
    }

    /**
     * Registers table-layout nodes.
     */
    def registerTableLayout() {
        registerFactory("tableLayout", new TableLayoutFactory())
        registerFactory("tr", new TRFactory())
        registerFactory("td", new TDFactory())
    }

    /**
     * Registers border factories.
     */
    def registerBorders() {
        registerFactory("lineBorder", new LineBorderFactory())
        registerFactory("loweredBevelBorder", new BevelBorderFactory(BevelBorder.LOWERED))
        registerFactory("raisedBevelBorder", new BevelBorderFactory(BevelBorder.RAISED))
        registerFactory("etchedBorder", new EtchedBorderFactory(EtchedBorder.LOWERED))
        registerFactory("loweredEtchedBorder", new EtchedBorderFactory(EtchedBorder.LOWERED))
        registerFactory("raisedEtchedBorder", new EtchedBorderFactory(EtchedBorder.RAISED))
        registerFactory("titledBorder", new TitledBorderFactory())
        registerFactory("emptyBorder", new EmptyBorderFactory())
        registerFactory("compoundBorder", new CompoundBorderFactory())
        registerFactory("matteBorder", new MatteBorderFactory())
    }

    /**
     * Registers renderer factories for tables, lists, and headers.
     */
    def registerRenderers() {
        RendererFactory renderFactory = new RendererFactory()
        registerFactory("tableCellRenderer", renderFactory)
        registerFactory("listCellRenderer", renderFactory)
        registerFactory("onRender", new RendererUpdateFactory())
        registerFactory("cellRenderer", renderFactory)
        registerFactory("headerRenderer", renderFactory)
    }

    /**
     * Registers cell editor factories.
     */
    def registerEditors() {
      registerFactory("cellEditor", new CellEditorFactory())
      registerFactory("editorValue", new CellEditorGetValueFactory())
      registerFactory("prepareEditor", new CellEditorPrepareFactory())
    }

    /**
     * Registers threading helper methods such as {@code edt} and {@code doLater}.
     */
    def registerThreading() {
        registerExplicitMethod "edt", this.&edt
        registerExplicitMethod "doOutside", this.&doOutside
        registerExplicitMethod "doLater", this.&doLater
    }


    /**
     * Registers a bean factory, substituting Swing-specific factory implementations for special component types.
     *
     * @param nodeName the builder node name
     * @param groupName the factory group name
     * @param klass the bean class to register
     */
    void registerBeanFactory(String nodeName, String groupName, Class klass) {
        // poke at the type to see if we need special handling
        if (LayoutManager.isAssignableFrom(klass)) {
            registerFactory(nodeName, groupName, new LayoutFactory(klass))
        } else if (JScrollPane.isAssignableFrom(klass)) {
            registerFactory(nodeName, groupName, new ScrollPaneFactory(klass))
        } else if (JTable.isAssignableFrom(klass)) {
            registerFactory(nodeName, groupName, new TableFactory(klass))
        } else if (JComponent.isAssignableFrom(klass)
            || JDialog.isAssignableFrom(klass)
            || JFrame.isAssignableFrom(klass)
            || JWindow.isAssignableFrom(klass)
        ) {
            registerFactory(nodeName, groupName, new ComponentFactory(klass))
        } else {
            super.registerBeanFactory(nodeName, groupName, klass)
        }

    }

    /**
     * Runs a closure on the Event Dispatch Thread using {@link SwingUtilities#invokeAndWait(Runnable)} when needed.
     *
     * <p>When already on the EDT, or when running headless, the closure executes immediately.</p>
     *
     * @param c the closure to run on the EDT
     * @return this builder
     */
    SwingBuilder edt(@DelegatesTo(SwingBuilder) Closure c) {
        c.setDelegate(this)
        if (headless || SwingUtilities.isEventDispatchThread()) {
            c.call(this)
        } else {
            Map<String, Object> continuationData = getContinuationData()
            try {
                if (!(c instanceof MethodClosure)) {
                    c = c.curry([this])
                }
                SwingUtilities.invokeAndWait {
                    restoreFromContinuationData(continuationData)
                    c()
                    continuationData = getContinuationData()
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new GroovyRuntimeException("interrupted swing interaction", e)
            } catch (InvocationTargetException e) {
                throw new GroovyRuntimeException("exception in event dispatch thread", e.getTargetException())
            } finally {
                restoreFromContinuationData(continuationData)
            }
        }
        return this
    }

    /**
     * Schedules a closure to run on the Event Dispatch Thread using {@link SwingUtilities#invokeLater(Runnable)}.
     *
     * <p>When running headless, the closure executes immediately.</p>
     *
     * @param c the closure to run on the EDT
     * @return this builder
     */
    SwingBuilder doLater(@DelegatesTo(SwingBuilder) Closure c) {
        c.setDelegate(this)
        if (headless) {
            c.call()
        } else {
            if (!(c instanceof MethodClosure)) {
                c = c.curry([this])
            }
            SwingUtilities.invokeLater(c)
        }
        return this
    }

    /**
     * Runs a closure outside of the Event Dispatch Thread.
     *
     * <p>If called from the EDT a new thread is started immediately; otherwise the closure runs in the current thread.</p>
     *
     * @param c the closure to run outside the EDT
     * @return this builder
     */
    SwingBuilder doOutside(@DelegatesTo(SwingBuilder) Closure c) {
        c.setDelegate(this)
        if (!(c instanceof MethodClosure)) {
            c = c.curry([this])
        }
        if( SwingUtilities.isEventDispatchThread() )
            new Thread(c).start()
        else
            c.call()
        return this
    }

    /**
     * Creates a new builder and runs the supplied closure on the EDT.
     *
     * @param c the closure to execute with the new builder
     * @return the initialized builder
     */
    static SwingBuilder edtBuilder(@DelegatesTo(SwingBuilder) Closure c) {
        SwingBuilder builder = new SwingBuilder()
        return builder.edt(c)
    }

    /**
     * Supports the legacy static {@code SwingBuilder.build(Closure)} entry point.
     *
     * @param method the missing static method name
     * @param args the missing static method arguments
     * @return the builder created for the legacy {@code build} call
     */
    @Deprecated
    static SwingBuilder '$static_methodMissing'(String method, Object args) {
        if (method == 'build' && args.length == 1 && args[0] instanceof Closure) {
            return edtBuilder(args[0])
        } else {
            throw new MissingMethodException(method, SwingBuilder, args, true)
        }
    }

    /**
     * Executes a closure against this builder for compatibility with older APIs.
     *
     * @param c the closure to execute
     * @return the closure result
     */
    Object build(@DelegatesTo(SwingBuilder) Closure c) {
        c.setDelegate(this)
        return c.call()
    }

    /**
     * Creates a menu-shortcut keystroke for the supplied key and additional modifiers.
     *
     * @param key the key specification accepted by {@link KeyStroke#getKeyStroke(Object, int)}
     * @param modifier additional modifier mask bits
     * @return the created keystroke
     */
    KeyStroke shortcut(key, modifier = 0) {
        return KeyStroke.getKeyStroke(key, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | modifier)
    }

    /**
     * Creates a menu-shortcut keystroke from a string description.
     *
     * @param key the keystroke description
     * @param modifier additional modifier mask bits
     * @return the created keystroke, or {@code null} if the description cannot be parsed
     */
    KeyStroke shortcut(String key, modifier = 0) {
        KeyStroke ks = KeyStroke.getKeyStroke(key)
        if (ks == null) {
            return null
        } else {
            return KeyStroke.getKeyStroke(ks.getKeyCode(), ks.getModifiers() | modifier | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())        }
    }

    /**
     * Resolves and installs a look and feel with an optional initialization closure.
     *
     * @param laf the look and feel instance, alias, or class name
     * @param initCode optional initialization closure
     * @return the installed look and feel
     */
    static LookAndFeel lookAndFeel(Object laf, Closure initCode) {
        lookAndFeel([:], laf, initCode)
    }

    /**
     * Resolves and installs a look and feel with optional attributes and initialization logic.
     *
     * @param attributes look and feel configuration attributes
     * @param laf the look and feel instance, alias, or class name
     * @param initCode optional initialization closure
     * @return the installed look and feel
     */
    static LookAndFeel lookAndFeel(Map attributes = [:], Object laf = null, Closure initCode = null) {
        // if we get rid of this warning, we can make it static.
        //if (context) {
        //    LOG.warning "For best result do not call lookAndFeel when it is a child of a SwingBuilder node, initialization of the Look and Feel may be inconsistent."
        //}
        groovy.swing.LookAndFeelHelper.instance.lookAndFeel(laf, attributes, initCode)
    }

    /**
     * Attempts multiple look and feel candidates until one installs successfully.
     *
     * @param lafs the look and feel candidates to try
     * @return the first installed look and feel, or {@code null} if all candidates fail
     */
    static LookAndFeel lookAndFeel(Object... lafs) {
        if (lafs.length == 1) {
            lookAndFeel([:], lafs[0], null as Closure)
        }
        for (Object laf in lafs) {
            try {
                // (ab)use multi-methods
                if (laf instanceof ArrayList) {
                    // multi-method bug
                    return _laf(*laf)
                } else {
                    return _laf(laf)
                }
            } catch (Throwable t) {
                LOG.fine "Could not instantiate Look and Feel $laf because of ${t}. Attempting next option."
            }
        }
        LOG.warning "All Look and Feel options failed: $lafs"
        return null
    }

    private static LookAndFeel _laf(java.util.List s) {
        _laf(*s)
    }

    private static LookAndFeel _laf(String s, Map m) {
        lookAndFeel(m, s, null as Closure)
    }

    private static LookAndFeel _laf(LookAndFeel laf, Map m) {
        lookAndFeel(m, laf, null as Closure)
    }

    private static LookAndFeel _laf(String s) {
        lookAndFeel([:], s, null as Closure)
    }

    private static LookAndFeel _laf(LookAndFeel laf) {
        lookAndFeel([:], laf, null as Closure)
    }

    /**
     * Publishes created nodes into the builder variable namespace using the configured id attribute.
     *
     * @param builder the active builder
     * @param node the created node
     * @param attributes the node attributes to inspect and mutate
     */
    static objectIDAttributeDelegate(def builder, def node, def attributes) {
        def idAttr = builder.getAt(DELEGATE_PROPERTY_OBJECT_ID) ?: DEFAULT_DELEGATE_PROPERTY_OBJECT_ID
        def theID = attributes.remove(idAttr)
        if (theID) {
            builder.setVariable(theID, node)
            if(node) {
                try {
                    if (!node.name) node.name = theID
                } catch (MissingPropertyException mpe) {
                    // ignore
                }
            }
        }
    }

    /**
     * Applies {@code clientProperties} and {@code clientProperty*} attributes to the target component.
     *
     * @param builder the active builder
     * @param node the component receiving client properties
     * @param attributes the node attributes to inspect and mutate
     */
    static clientPropertyAttributeDelegate(def builder, def node, def attributes) {
        def clientPropertyMap = attributes.remove("clientProperties")
        clientPropertyMap.each { key, value ->
           node.putClientProperty key, value
        }
        attributes.findAll { it.key =~ /clientProperty(\w)/ }.each { key, value ->
           attributes.remove(key)
           node.putClientProperty(key - "clientProperty", value)
        }
    }

    /**
     * Binds one or more keystrokes to an action on the resolved target component.
     *
     * <p>The target component is taken from the method argument, a {@code component:} attribute,
     * or the current builder node. Unknown conditions default to {@link JComponent#WHEN_FOCUSED}.</p>
     *
     * @param attributes action registration attributes
     * @param component optional explicit target component
     */
    void createKeyStrokeAction( Map attributes, JComponent component = null ) {
        component = findTargetComponent(attributes, component)
        if( !attributes.containsKey("keyStroke") ) {
            throw new RuntimeException("You must define a value for keyStroke:")
        }
        if( !attributes.containsKey("action") ) {
            throw new RuntimeException("You must define a value for action:")
        }

        def condition = attributes.remove("condition") ?: JComponent.WHEN_FOCUSED
        if (condition instanceof GString) condition = condition as String
        if( condition instanceof String ) {
            condition = condition.toUpperCase(Locale.ROOT).replace(" ", "_")
            if( !condition.startsWith("WHEN_") ) condition = "WHEN_"+condition
        }
        switch(condition) {
            case JComponent.WHEN_FOCUSED:
            case JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT:
            case JComponent.WHEN_IN_FOCUSED_WINDOW:
                // everything is fine, no further processing
                break
            case "WHEN_FOCUSED":
                condition = JComponent.WHEN_FOCUSED
                break
            case "WHEN_ANCESTOR_OF_FOCUSED_COMPONENT":
                condition = JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
                break
            case "WHEN_IN_FOCUSED_WINDOW":
                condition = JComponent.WHEN_IN_FOCUSED_WINDOW
                break
            default:
                // let's be lenient and assign WHEN_FOCUSED by default
                condition = JComponent.WHEN_FOCUSED
        }
        def actionKey = attributes.remove("actionKey")
        if( !actionKey ) actionKey = "Action"+Math.abs(random.nextLong())

        def keyStroke = attributes.remove("keyStroke")
        // accept String, Number, KeyStroke, List<String>, List<Number>, List<KeyStroke>
        def action = attributes.remove("action")

        if( keyStroke instanceof GString ) keyStroke = keyStroke as String
        if( keyStroke instanceof String || keyStroke instanceof Number ) keyStroke = [keyStroke]
        keyStroke.each { ks ->
            switch(ks) {
                case KeyStroke:
                    component.getInputMap(condition).put(ks, actionKey)
                    break
                case String:
                    component.getInputMap(condition).put(KeyStroke.getKeyStroke(ks), actionKey)
                    break
                case Number:
                    component.getInputMap(condition).put(KeyStroke.getKeyStroke(ks.intValue()), actionKey)
                    break
                default:
                    throw new RuntimeException("Cannot apply ${ks} as a KeyStroke value.")
            }
        }
        component.actionMap.put(actionKey, action)
    }

    private findTargetComponent( Map attributes, JComponent component ) {
        if( component ) return component
        if( attributes.containsKey("component") ) {
            def c = attributes.remove("component")
            if( !(c instanceof JComponent) ) {
                throw new RuntimeException("The property component: is not of type JComponent.")
            }
            return c
        }
        def c = getCurrent()
        if( c instanceof JComponent ) {
            return c
        }
        throw new RuntimeException("You must define one of the following: a value of type JComponent, a component: attribute or nest this node inside another one that produces a JComponent.")
    }
}
