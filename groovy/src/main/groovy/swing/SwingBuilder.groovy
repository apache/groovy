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
import javax.swing.table.TableColumn
import javax.swing.border.BevelBorder
import javax.swing.border.EtchedBorder
import org.codehaus.groovy.runtime.MethodClosure

/**
 * A helper class for creating Swing widgets using GroovyMarkup
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class SwingBuilder  extends FactoryBuilderSupport {

    // Properties
    LinkedList containingWindows = new LinkedList()

    // local fields
    private static final Logger LOG = Logger.getLogger(SwingBuilder.name)
    // tracks all containing windows, for auto-owned dialogs
    private boolean headless = false

    public SwingBuilder() {
        registerWidgets()
        headless = GraphicsEnvironment.isHeadless()
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
        registerFactory("bind", new BindFactory())
        addAttributeDelegate(BindFactory.&bindingAttributeDelegate)
        registerFactory("model", new ModelFactory())

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
        registerBeanFactory("list", JList)
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
        registerFactory("gridBagLayout", new LayoutFactory(GridBagLayout))
        registerFactory("gridLayout", new LayoutFactory(GridLayout))
        registerFactory("overlayLayout", new LayoutFactory(OverlayLayout))
        registerFactory("springLayout", new LayoutFactory(SpringLayout))
        registerBeanFactory("gridBagConstraints", GridBagConstraints)
        registerBeanFactory("gbc", GridBagConstraints) // shortcut name
        // constraints delegate
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

    public SwingBuilder doOutside(Closure c) {
        c.setDelegate(this)
        if (!(c instanceof MethodClosure)) {
            c = c.curry([this])
        }
        new Thread(c).start()
        return this
    }

    /**
     * Factory method to create a SwingBuilder, and run the
     * the closure in it on the EDT
     *
     * @param c run this closure in the new builder using the edt method
     */
    public static SwingBuilder edtBuilder(Closure c) {
        return build(c)
    }

    /**
     * Factory method static SwingBuilder.build(Closure).
     * @param c run this closure in a new builder using the edt method
     * @deprecated  please use edtBuilder(Closure) instead
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

    public LookAndFeel lookAndFeel(Object laf, Closure initCode = null) {
        lookAndFeel([:], laf, initCode)
    }

    public LookAndFeel lookAndFeel(Map attributes = [:], Object laf = null, Closure initCode = null) {
        // if we get rid of this warning, we can make it static.
        if (context) {
            LOG.warning "For best result do not call lookAndFeel when it is a child of a SwingBuidler node, initializaiton of the Look and Feel may be inconsistant."
        }

        LookAndFeelHelper.instance.lookAndFeel(laf, attributes, initCode)
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
