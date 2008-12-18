/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.swing

import groovy.swing.factory.*
import java.awt.*
import java.lang.reflect.InvocationTargetException
import java.util.logging.Logger
import javax.swing.*
import javax.swing.border.BevelBorder
import javax.swing.border.EtchedBorder
import javax.swing.table.TableColumn
import org.codehaus.groovy.runtime.MethodClosure

/**
 * A helper class for creating Swing widgets using GroovyMarkup
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class SwingBuilder extends FactoryBuilderSupport {

    // local fields
    private static final Logger LOG = Logger.getLogger(SwingBuilder.name)
    private static boolean headless = false

    public static final String DELEGATE_PROPERTY_OBJECT_ID = "_delegateProperty:id";
    public static final String DEFAULT_DELEGATE_PROPERTY_OBJECT_ID = "id";

    public SwingBuilder(boolean init = true) {
        super(init)
        //registerWidgets()
        headless = GraphicsEnvironment.isHeadless()
        containingWindows = new LinkedList()
        this[DELEGATE_PROPERTY_OBJECT_ID] = DEFAULT_DELEGATE_PROPERTY_OBJECT_ID
    }

    def registerSupportNodes() {
        registerFactory("action", new ActionFactory())
        registerFactory("actions", new CollectionFactory())
        registerFactory("map", new MapFactory())
        registerFactory("imageIcon", new ImageIconFactory())
        registerFactory("buttonGroup", new ButtonGroupFactory())
        addAttributeDelegate(ButtonGroupFactory.&buttonGroupAttributeDelegate)

        //object id delegage, for propertyNotFound
        addAttributeDelegate(SwingBuilder.&objectIDAttributeDelegate)
    }

    def registerBinding() {
        BindFactory bindFactory = new BindFactory()
        registerFactory("bind", bindFactory)
        addAttributeDelegate(bindFactory.&bindingAttributeDelegate)
        registerFactory("bindProxy", new BindProxyFactory())
        registerFactory ("bindGroup", new BindGroupFactory());
    }

    def registerPassThruNodes() {
        registerFactory("widget", new WidgetFactory(Component, true))
        registerFactory("container", new WidgetFactory(Component, false))
        registerFactory("bean", new WidgetFactory(Object, true))
    }


    def registerWindows() {
        registerFactory("dialog", new DialogFactory())
        registerBeanFactory("fileChooser", JFileChooser)
        registerFactory("frame", new FrameFactory())
        registerBeanFactory("optionPane", JOptionPane)
        registerFactory("window", new WindowFactory())
    }

    def registerActionButtonWidgets() {
        registerFactory("button", new RichActionWidgetFactory(JButton))
        registerFactory("checkBox", new RichActionWidgetFactory(JCheckBox))
        registerFactory("checkBoxMenuItem", new RichActionWidgetFactory(JCheckBoxMenuItem))
        registerFactory("menuItem", new RichActionWidgetFactory(JMenuItem))
        registerFactory("radioButton", new RichActionWidgetFactory(JRadioButton))
        registerFactory("radioButtonMenuItem", new RichActionWidgetFactory(JRadioButtonMenuItem))
        registerFactory("toggleButton", new RichActionWidgetFactory(JToggleButton))
    }

    def registerTextWidgets() {
        registerFactory("editorPane", new TextArgWidgetFactory(JEditorPane))
        registerFactory("label", new TextArgWidgetFactory(JLabel))
        registerFactory("passwordField", new TextArgWidgetFactory(JPasswordField))
        registerFactory("textArea", new TextArgWidgetFactory(JTextArea))
        registerFactory("textField", new TextArgWidgetFactory(JTextField))
        registerFactory("formattedTextField", new FormattedTextFactory())
        registerFactory("textPane", new TextArgWidgetFactory(JTextPane))
    }

    def registerMDIWidgets() {
        registerBeanFactory("desktopPane", JDesktopPane)
        registerFactory("internalFrame", new InternalFrameFactory())
    }

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

    def registerMenuWidgets() {
        registerBeanFactory("menu", JMenu)
        registerBeanFactory("menuBar", JMenuBar)
        registerBeanFactory("popupMenu", JPopupMenu)
    }

    def registerContainers() {
        registerBeanFactory("panel", JPanel)
        registerFactory("scrollPane", new ScrollPaneFactory())
        registerFactory("splitPane", new SplitPaneFactory())
        registerFactory("tabbedPane", new TabbedPaneFactory(JTabbedPane))
        registerBeanFactory("toolBar", JToolBar)
        registerBeanFactory("viewport", JViewport) // sub class?
        registerBeanFactory("layeredPane", JLayeredPane)
    }

    def registerDataModels() {
        registerBeanFactory("boundedRangeModel", DefaultBoundedRangeModel)

        // spinner models
        registerBeanFactory("spinnerDateModel", SpinnerDateModel)
        registerBeanFactory("spinnerListModel", SpinnerListModel)
        registerBeanFactory("spinnerNumberModel", SpinnerNumberModel)
    }

    def registerTableComponents() {
        registerFactory("table", new TableFactory())
        registerBeanFactory("tableColumn", TableColumn)
        registerFactory("tableModel", new TableModelFactory())
        registerFactory("propertyColumn", new PropertyColumnFactory())
        registerFactory("closureColumn", new ClosureColumnFactory())
    }

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

    def registerTableLayout() {
        registerFactory("tableLayout", new TableLayoutFactory())
        registerFactory("tr", new TRFactory())
        registerFactory("td", new TDFactory())
    }

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

    def registerRenderers() {
        RendererFactory renderFactory = new RendererFactory()
        registerFactory("tableCellRenderer", renderFactory)
        registerFactory("listCellRenderer", renderFactory)
        registerFactory("onRender", new RendererUpdateFactory())
    }

    def registerThreading() {
        registerExplicitMethod "edt", this.&edt
        registerExplicitMethod "doOutside", this.&doOutside
        registerExplicitMethod "doLater", this.&doLater
    }


    /**
     * Do some overrides for standard component handlers, else use super
     */
    public void registerBeanFactory(String nodeName, String groupName, Class klass) {
        // poke at the type to see if we need special handling
        if (LayoutManager.isAssignableFrom(klass)) {
            registerFactory(nodeName, groupName, new LayoutFactory(klass))
        } else if (JScrollPane.isAssignableFrom(klass)) {
            registerFactory(nodeName, groupName, new ScrollPaneFactory(klass))
        } else if (JTable.isAssignableFrom(klass)) {
            registerFactory(nodeName, groupName, new TableFactory(klass))
        } else if (JComponent.isAssignableFrom(klass)
            || JApplet.isAssignableFrom(klass)
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
     * Utilitiy method to run a closure in EDT,
     * using <code>SwingUtilities.invokeAndWait</cod>.
     *
     * @param c this closure is run in the EDT
     */
    public SwingBuilder edt(Closure c) {
        c.setDelegate(this)
        if (headless || SwingUtilities.isEventDispatchThread()) {
            c.call(this)
        } else {
            Map<String, Object> continuationData = getContinuationData();
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
                throw new GroovyRuntimeException("interrupted swing interaction", e)
            } catch (InvocationTargetException e) {
                throw new GroovyRuntimeException("exception in event dispatch thread", e.getTargetException())
            } finally {
                restoreFromContinuationData(continuationData);
            }
        }
        return this
    }

    /**
     * Utility method to run a closure in EDT,
     * using <code>SwingUtilities.invokeLater</cod>.
     *
     * @param c this closure is run in the EDT
     */
    public SwingBuilder doLater(Closure c) {
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
     * Utility method to run a closure in a separate Thread.
     * <p>
     * The closure is wrapped in a thread, and the thread is started
     * immediatly.
     *
     * @param c this closure is started in a separate thread
     */
    public SwingBuilder doOutside(Closure c) {
        c.setDelegate(this)
        if (!(c instanceof MethodClosure)) {
            c = c.curry([this])
        }
        Thread.start(c)
        return this
    }

    /**
     * Factory method to create a SwingBuilder, and run the
     * the closure in it on the EDT
     *
     * @param c run this closure in the new builder using the edt method
     */
    public static SwingBuilder edtBuilder(Closure c) {
        SwingBuilder builder = new SwingBuilder()
        return builder.edt(c)
    }

    /**
     * Old factory method static SwingBuilder.build(Closure).
     * @param c run this closure in the builder using the edt method
     */
    @Deprecated
    public static SwingBuilder '$static_methodMissing'(String method, Object args) {
        if (method == 'build' && args.length == 1 && args[0] instanceof Closure) {
            return edtBuilder(args[0])
        } else {
            throw new MissingMethodException(method, SwingBuilder, args, true)
        }
    }

    /**
     * Compatibility API.
     *
     * @param c run this closure in the builder using the edt method
     */
    public Object build(Closure c) {
        c.setDelegate(this)
        return c.call()
    }

    public KeyStroke shortcut(key, modifier = 0) {
        return KeyStroke.getKeyStroke(key, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | modifier)
    }

    public KeyStroke shortcut(String key, modifier = 0) {
        KeyStroke ks = KeyStroke.getKeyStroke(key)
        if (ks == null) {
            return null
        } else {
            return KeyStroke.getKeyStroke(ks.getKeyCode(), ks.getModifiers() | modifier | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())        }
    }

    public static LookAndFeel lookAndFeel(Object laf, Closure initCode) {
        lookAndFeel([:], laf, initCode)
    }

    public static LookAndFeel lookAndFeel(Map attributes = [:], Object laf = null, Closure initCode = null) {
        // if we get rid of this warning, we can make it static.
        //if (context) {
        //    LOG.warning "For best result do not call lookAndFeel when it is a child of a SwingBuidler node, initializaiton of the Look and Feel may be inconsistant."
        //}
        LookAndFeelHelper.instance.lookAndFeel(laf, attributes, initCode)
    }

    public static LookAndFeel lookAndFeel(Object... lafs) {
        if (lafs.length == 1) {
            lookAndFeel([:], lafs[0], null as Closure);
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
                LOG.fine "Could not instantiate Look and Feel $laf because of ${t}. Attemting next option."
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

    public static objectIDAttributeDelegate(def builder, def node, def attributes) {
        def idAttr = builder.getAt(DELEGATE_PROPERTY_OBJECT_ID) ?: DEFAULT_DELEGATE_PROPERTY_OBJECT_ID
        def theID = attributes.remove(idAttr)
        if (theID) {
            builder.setVariable(theID, node)
        }
    }
}
