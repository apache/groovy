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
public class SwingBuilder  extends FactoryBuilderSupport {

    // local fields
    private static final Logger LOG = Logger.getLogger(SwingBuilder.name)
    private static boolean headless = false

    public SwingBuilder() {
        registerWidgets()
        headless = GraphicsEnvironment.isHeadless()
        containingWindows = new LinkedList()
    }

    protected void registerWidgets() {
        //
        // non-widget support classes
        //
        registerFactory("action", new ActionFactory())
        registerFactory("actions", new CollectionFactory())
        registerFactory("map", new MapFactory())
        registerFactory("imageIcon", new ImageIconFactory())
        registerBeanFactory("buttonGroup", ButtonGroup)
        addAttributeDelegate(SwingBuilder.&buttonGroupAttributeDelegate)

        //object id delegage, for propertyNotFound
        addAttributeDelegate(SwingBuilder.&objectIDAttributeDelegate)

        // binding related classes
        BindFactory bindFactory = new BindFactory()
        registerFactory("bind", bindFactory)
        addAttributeDelegate(bindFactory.&bindingAttributeDelegate)
        registerFactory("bindProxy", new BindProxyFactory())

        // ulimate pass through types
        registerFactory("widget", new WidgetFactory(Component, true))
        registerFactory("container", new WidgetFactory(Component, false))
        registerFactory("bean", new WidgetFactory(Object, true))


        //
        // standalone window classes
        //
        registerFactory("dialog", new DialogFactory())
        registerBeanFactory("fileChooser", JFileChooser)
        registerFactory("frame", new FrameFactory())
        registerBeanFactory("optionPane", JOptionPane)
        registerFactory("window", new WindowFactory())


        //
        // widgets
        //
        registerFactory("button", new RichActionWidgetFactory(JButton))
        registerFactory("checkBox", new RichActionWidgetFactory(JCheckBox))
        registerFactory("checkBoxMenuItem", new RichActionWidgetFactory(JCheckBoxMenuItem))
        registerFactory("menuItem", new RichActionWidgetFactory(JMenuItem))
        registerFactory("radioButton", new RichActionWidgetFactory(JRadioButton))
        registerFactory("radioButtonMenuItem", new RichActionWidgetFactory(JRadioButtonMenuItem))
        registerFactory("toggleButton", new RichActionWidgetFactory(JToggleButton))

        registerFactory("editorPane", new TextArgWidgetFactory(JEditorPane))
        registerFactory("label", new TextArgWidgetFactory(JLabel))
        registerFactory("passwordField", new TextArgWidgetFactory(JPasswordField))
        registerFactory("textArea", new TextArgWidgetFactory(JTextArea))
        registerFactory("textField", new TextArgWidgetFactory(JTextField))
        registerFactory("textPane", new TextArgWidgetFactory(JTextPane))

        registerBeanFactory("colorChooser", JColorChooser)
        registerFactory("comboBox", new ComboBoxFactory())
        registerBeanFactory("desktopPane", JDesktopPane)
        registerFactory("formattedTextField", new FormattedTextFactory())
        registerFactory("internalFrame", new InternalFrameFactory())
        registerBeanFactory("layeredPane", JLayeredPane)
        registerFactory("list", new ListFactory())
        registerBeanFactory("menu", JMenu)
        registerBeanFactory("menuBar", JMenuBar)
        registerBeanFactory("panel", JPanel)
        registerBeanFactory("popupMenu", JPopupMenu)
        registerBeanFactory("progressBar", JProgressBar)
        registerBeanFactory("scrollBar", JScrollBar)
        registerFactory("scrollPane", new ScrollPaneFactory())
        registerFactory("separator", new SeparatorFactory())
        registerBeanFactory("slider", JSlider)
        registerBeanFactory("spinner", JSpinner)
        registerFactory("splitPane", new SplitPaneFactory())
        registerFactory("tabbedPane", new TabbedPaneFactory(JTabbedPane))
        registerFactory("table", new TableFactory())
        registerBeanFactory("tableColumn", TableColumn)
        registerBeanFactory("toolBar", JToolBar)
        //registerBeanFactory("tooltip", JToolTip) // doesn't work, use toolTipText property
        registerBeanFactory("tree", JTree)
        registerBeanFactory("viewport", JViewport) // sub class?


        //
        // MVC models
        //
        registerBeanFactory("boundedRangeModel", DefaultBoundedRangeModel)

        // spinner models
        registerBeanFactory("spinnerDateModel", SpinnerDateModel)
        registerBeanFactory("spinnerListModel", SpinnerListModel)
        registerBeanFactory("spinnerNumberModel", SpinnerNumberModel)

        // table models
        registerFactory("tableModel", new TableModelFactory())
        registerFactory("propertyColumn", new PropertyColumnFactory())
        registerFactory("closureColumn", new ClosureColumnFactory())


        //
        // Layouts
        //
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

        addAttributeDelegate(SwingBuilder.&constraintsAttributeDelegate)


        // Box layout and friends
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

        // table layout
        registerFactory("tableLayout", new TableLayoutFactory())
        registerFactory("tr", new TRFactory())
        registerFactory("td", new TDFactory())

        //
        // borders
        //
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

        //
        // Renderers
        //
        RendererFactory renderFactory = new RendererFactory()
        registerFactory("tableCellRenderer", renderFactory)
        registerFactory("listCellRenderer", renderFactory)
        registerFactory("onRender", new RendererUpdateFactory())        
    }

    /**
     * Do some overrides for standard component handlers, else use super
     */
    public void registerBeanFactory(String nodeName, Class klass) {
        // poke at the type to see if we need special handling
        if (LayoutManager.isAssignableFrom(klass)) {
            registerFactory(nodeName, new LayoutFactory(klass))
        } else if (JScrollPane.isAssignableFrom(klass)) {
            registerFactory(nodeName, new ScrollPaneFactory(klass))
        } else if (JTable.isAssignableFrom(klass)) {
            registerFactory(nodeName, new TableFactory(klass))
        } else if (JComponent.isAssignableFrom(klass)
            || JApplet.isAssignableFrom(klass)
            || JDialog.isAssignableFrom(klass)
            || JFrame.isAssignableFrom(klass)
            || JWindow.isAssignableFrom(klass)
        ) {
            registerFactory(nodeName, new ComponentFactory(klass))
        } else {
            super.registerBeanFactory(nodeName, klass)
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
            try {
                if (!(c instanceof MethodClosure)) {
                    c = c.curry([this])
                }
                SwingUtilities.invokeAndWait(c)
            } catch (InterruptedException e) {
                throw new GroovyRuntimeException("interrupted swing interaction", e)
            } catch (InvocationTargetException e) {
                throw new GroovyRuntimeException("exception in event dispatch thread", e.getTargetException())
            }
        }
        return this
    }

    /**
     * Utilitiy method to run a closure in EDT,
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
     * Utility method to create a SwingBuilder, and run the
     * the closure in the EDT
     *
     * @param c run this closre in the builder using the edt method
     */
    public static SwingBuilder build(Closure c) {
        SwingBuilder builder = new SwingBuilder()
        return builder.edt(c)
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

    public static LookAndFeel lookAndFeel(Object lookAndFeel, Closure initCode) {
        lookAndFeel([:], lookAndFeel, initCode)
    }

    public static LookAndFeel lookAndFeel(Map attributes = [:], Object lookAndFeel = null, Closure initCode = null) {
        // if we get rid of this warning, we can make it static.
        //if (context) {
        //    LOG.warning "For best result do not call lookAndFeel when it is a child of a SwingBuidler node, initializaiton of the Look and Feel may be inconsistant."
        //}
        LookAndFeelHelper.instance.lookAndFeel(lookAndFeel, attributes, initCode)
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

    private static LookAndFeel _laf(List s) {
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

    public static buttonGroupAttributeDelegate(def builder, def node, def attributes) {
        if (attributes.containsKey("buttonGroup")) {
            def o = attributes.get("buttonGroup")
            if ((o instanceof ButtonGroup) && (node instanceof AbstractButton)) {
                node.model.group = o
                attributes.remove("buttonGroup")
            }
        }
    }

    public static objectIDAttributeDelegate(def builder, def node, def attributes) {
        def theID = attributes.remove('id')
        if (theID) {
            builder.setVariable(theID, node)
        }
    }

    public static constraintsAttributeDelegate(def builder, def node, def attributes) {
        builder.context.constraints = attributes.remove('constraints')
    }
}
