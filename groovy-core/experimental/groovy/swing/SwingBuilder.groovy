/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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

import groovy.model.DefaultTableModel
import groovy.swing.factory.ActionFactory
import groovy.swing.factory.AnimateFactory
import groovy.swing.factory.BeanFactory
import groovy.swing.factory.BindFactory
import groovy.swing.factory.BoxFactory
import groovy.swing.factory.BoxLayoutFactory
import groovy.swing.factory.ClosureColumnFactory
import groovy.swing.factory.CollectionFactory
import groovy.swing.factory.Factory
import groovy.swing.factory.FormattedTextFactory
import groovy.swing.factory.FrameFactory
import groovy.swing.factory.GlueFactory
import groovy.swing.factory.HBoxFactory
import groovy.swing.factory.HGlueFactory
import groovy.swing.factory.HStrutFactory
import groovy.swing.factory.MapFactory
import groovy.swing.factory.ModelFactory
import groovy.swing.factory.PropertyColumnFactory
import groovy.swing.factory.RichActionWidgetFactory
import groovy.swing.factory.RigidAreaFactory
import groovy.swing.factory.SeparatorFactory
import groovy.swing.factory.TDFactory
import groovy.swing.factory.TRFactory
import groovy.swing.factory.TableLayoutFactory
import groovy.swing.factory.TableModelFactory
import groovy.swing.factory.TextArgWidgetFactory
import groovy.swing.factory.VBoxFactory
import groovy.swing.factory.VGlueFactory
import groovy.swing.factory.VStrutFactory
import groovy.swing.factory.WidgetFactory
import groovy.swing.factory.WindowFactory
import groovy.swing.impl.ComponentFacade
import groovy.swing.impl.ContainerFacade
import groovy.swing.impl.Startable
import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.Component
import java.awt.Container
import java.awt.FlowLayout
import java.awt.GraphicsEnvironment
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.GridLayout
import java.awt.LayoutManager
import java.awt.Toolkit
import java.awt.Window
import java.lang.reflect.InvocationTargetException
import java.util.logging.Level
import java.util.logging.Logger
import javax.swing.AbstractButton
import javax.swing.Action
import javax.swing.ButtonGroup
import javax.swing.DefaultBoundedRangeModel
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JCheckBoxMenuItem
import javax.swing.JColorChooser
import javax.swing.JComponent
import javax.swing.JDesktopPane
import javax.swing.JEditorPane
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.JInternalFrame
import javax.swing.JLabel
import javax.swing.JLayeredPane
import javax.swing.JList
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JPasswordField
import javax.swing.JPopupMenu
import javax.swing.JProgressBar
import javax.swing.JRadioButton
import javax.swing.JRadioButtonMenuItem
import javax.swing.JScrollBar
import javax.swing.JScrollPane
import javax.swing.JSlider
import javax.swing.JSpinner
import javax.swing.JSplitPane
import javax.swing.JTabbedPane
import javax.swing.JTable
import javax.swing.JTextArea
import javax.swing.JTextField
import javax.swing.JTextPane
import javax.swing.JToggleButton
import javax.swing.JToolBar
import javax.swing.JTree
import javax.swing.JViewport
import javax.swing.KeyStroke
import javax.swing.OverlayLayout
import javax.swing.RootPaneContainer
import javax.swing.SpinnerDateModel
import javax.swing.SpinnerListModel
import javax.swing.SpinnerNumberModel
import javax.swing.SpringLayout
import javax.swing.SwingUtilities
import javax.swing.table.TableColumn
import javax.swing.table.TableModel
import org.codehaus.groovy.binding.FullBinding
import org.codehaus.groovy.binding.PropertyBinding
import org.codehaus.groovy.runtime.InvokerHelper

/**
 * A helper class for creating Swing widgets using GroovyMarkup
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision: 7995 $
 */
public class SwingBuilder extends BuilderSupport {

    private static final Logger LOG = Logger.getLogger(SwingBuilder.class.getName());
    private Map factories = new HashMap();
    private Object constraints;
    private Map widgets = new HashMap();
    // tracks all containing windows, for auto-owned dialogs
    private LinkedList containingWindows = new LinkedList();
    private boolean headless = false;


    public SwingBuilder() {
        registerWidgets();
        headless = GraphicsEnvironment.isHeadless();
    }

    public Object getProperty(String name) {
        Object widget = widgets.get(name);
        if (widget == null) {
            return super.getProperty(name);
        }
        return widget;
    }

    protected void setParent(Object parent, Object child) {
        if (parent instanceof Collection) {
            ((Collection) parent).add(child);
        } else if (child instanceof Action) {
            setParentForAction(parent, (Action) child);
        } else if ((child instanceof LayoutManager) && (parent instanceof Container)) {
            Container target = getLayoutTarget((Container) parent);
            InvokerHelper.setProperty(target, "layout", child);
            // doesn't work, use toolTipText property
            //        } else if (child instanceof JToolTip && parent instanceof JComponent) {
            //            ((JToolTip) child).setComponent((JComponent) parent);
        } else if (parent instanceof JTable && child instanceof TableColumn) {
            JTable table = (JTable) parent;
            TableColumn column = (TableColumn) child;
            table.addColumn(column);
        } else if (parent instanceof JTabbedPane && child instanceof Component) {
            JTabbedPane tabbedPane = (JTabbedPane) parent;
            tabbedPane.add((Component) child);
        } else if (child instanceof Window) {
            // do nothing.  owner of window is set elsewhere, and this
            // shouldn't get added to any parent as a child
            // if it is a top level component anyway
        } else {
            Component component = null;
            if (child instanceof Component) {
                component = (Component) child;
            } else if (child instanceof ComponentFacade) {
                ComponentFacade facade = (ComponentFacade) child;
                component = facade.getComponent();
            }
            if (component != null) {
                setParentForComponent(parent, component);
            }
        }
    }

    private void setParentForComponent(Object parent, Component component) {
        if (parent instanceof JFrame && component instanceof JMenuBar) {
            JFrame frame = (JFrame) parent;
            frame.setJMenuBar((JMenuBar) component);
        } else if (parent instanceof RootPaneContainer) {
            RootPaneContainer rpc = (RootPaneContainer) parent;
            if (constraints != null) {
                rpc.getContentPane().add(component, constraints);
            } else {
                rpc.getContentPane().add(component);
            }
        } else if (parent instanceof JScrollPane) {
            JScrollPane scrollPane = (JScrollPane) parent;
            if (component instanceof JViewport) {
                scrollPane.setViewport((JViewport) component);
            } else {
                scrollPane.setViewportView(component);
            }
        } else if (parent instanceof JSplitPane) {
            JSplitPane splitPane = (JSplitPane) parent;
            if (splitPane.getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
                if (splitPane.getTopComponent() == null) {
                    splitPane.setTopComponent(component);
                } else {
                    splitPane.setBottomComponent(component);
                }
            } else {
                if (splitPane.getLeftComponent() == null) {
                    splitPane.setLeftComponent(component);
                } else {
                    splitPane.setRightComponent(component);
                }
            }
        } else if (parent instanceof JMenuBar && component instanceof JMenu) {
            JMenuBar menuBar = (JMenuBar) parent;
            menuBar.add((JMenu) component);
        } else if (parent instanceof Container) {
            Container container = (Container) parent;
            if (constraints != null) {
                container.add(component, constraints);
            } else {
                container.add(component);
            }
        } else if (parent instanceof ContainerFacade) {
            ContainerFacade facade = (ContainerFacade) parent;
            facade.addComponent(component);
        }
    }

    private void setParentForAction(Object parent, Action action) {
        try {
            InvokerHelper.setProperty(parent, "action", action);
        } catch (RuntimeException re) {
            // must not have an action property...
            // so we ignore it and go on
        }
        Object keyStroke = action.getValue("KeyStroke");
        if (parent instanceof JComponent) {
            JComponent component = (JComponent) parent;
            KeyStroke stroke = null;
            if (keyStroke instanceof String) {
                stroke = KeyStroke.getKeyStroke((String) keyStroke);
            } else if (keyStroke instanceof KeyStroke) {
                stroke = (KeyStroke) keyStroke;
            }
            if (stroke != null) {
                String key = action.toString();
                component.getInputMap().put(stroke, key);
                component.getActionMap().put(key, action);
            }
        }
    }

    public static Container getLayoutTarget(Container parent) {
        if (parent instanceof RootPaneContainer) {
            RootPaneContainer rpc = (RootPaneContainer) parent;
            parent = rpc.getContentPane();
        }
        return parent;
    }

    protected void nodeCompleted(Object parent, Object node) {
        // set models after the node has been completed
        if (node instanceof TableModel && parent instanceof JTable) {
            JTable table = (JTable) parent;
            TableModel model = (TableModel) node;
            table.setModel(model);
            if (model instanceof DefaultTableModel) {
                table.setColumnModel(((DefaultTableModel) model).getColumnModel());
            }
        }
        if (node instanceof Startable) {
            Startable startable = (Startable) node;
            startable.start();
        }
        if (node instanceof Window) {
            if (!containingWindows.isEmpty() && containingWindows.getLast() == node) {
                containingWindows.removeLast();
            }
        }
    }

    protected Object createNode(Object name) {
        return createNode(name, Collections.EMPTY_MAP, null);
    }

    protected Object createNode(Object name, Object value) {
        return createNode(name, Collections.EMPTY_MAP, value);
    }

    protected Object createNode(Object name, Map attributes) {
        return createNode(name, attributes, null);
    }

    protected Object createNode(Object name, Map attributes, Object value) {
        String widgetName = (String) attributes.remove("id");
        constraints = attributes.remove("constraints");
        Object widget;
        Factory factory = (Factory) factories.get(name);
        if (factory == null) {
            LOG.log(Level.WARNING, "Could not find match for name: $name");
            return null;
        }
        try {
            widget = factory.newInstance(this, name, value, attributes);
            if (widget == null) {
                LOG.log(Level.WARNING, "Factory for name: $name returned null");
                return null;
            }
            if (widgetName != null) {
                widgets.put(widgetName, widget);
            }
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("For name: $name created widget: $widget");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create component for '$name' reason: $e", e);
        }
        handleWidgetAttributes(widget, attributes);
        return widget;
    }

    protected void handleWidgetAttributes(Object widget, Map attributes) {
        // first, short circuit
        if (attributes.isEmpty() || (widget == null)) {
            return;
        }

        // some special cases...
        if (attributes.containsKey("buttonGroup")) {
            Object o = attributes.get("buttonGroup");
            if ((o instanceof ButtonGroup) && (widget instanceof AbstractButton)) {
                ((AbstractButton) widget).getModel().setGroup((ButtonGroup) o);
                attributes.remove("buttonGroup");
            }
        }

        // this next statement nd if/else is a workaround until GROOVY-305 is fixed
        Object mnemonic = attributes.remove("mnemonic");
        if (mnemonic != null) {
            if (mnemonic instanceof Number) {
                InvokerHelper.setProperty(widget, "mnemonic", new Character((char) ((Number) mnemonic).intValue()));
            } else {
                InvokerHelper.setProperty(widget, "mnemonic", new Character(mnemonic.toString().charAt(0)));
            }
        }

        // set the properties

        for (entry in attributes.entrySet()) {
            String property = entry.key.toString();
            Object value = entry.value;
            if (value instanceof FullBinding) {
                FullBinding fb = (FullBinding) value;
                PropertyBinding ptb = new PropertyBinding(widget, property);
                fb.setTargetBinding(ptb);
                try {
                    fb.update();
                } catch (Exception e) {
                    // just eat it?
                }
                try {
                    fb.rebind();
                } catch (Exception e) {
                    // just eat it?
                }
            } else {
                InvokerHelper.setProperty(widget, property, value);
            }
        }
    }

    public static String capitalize(String text) {
        char ch = text.charAt(0);
        if (Character.isUpperCase(ch)) {
            return text;
        }
        StringBuffer buffer = new StringBuffer(text.length());
        buffer.append(Character.toUpperCase(ch));
        buffer.append(text.substring(1));
        return buffer.toString();
    }

    protected void registerWidgets() {
        //
        // non-widget support classes
        //
        registerFactory("action", new ActionFactory());
        registerFactory("actions", new CollectionFactory());
        registerBeanFactory("buttonGroup", ButtonGroup);
        registerFactory("map", new MapFactory());

        // binding related classes
        registerFactory("animate", new AnimateFactory());
        registerFactory("bind", new BindFactory());
        registerFactory("model", new ModelFactory());

        // ulimate pass through types
        registerFactory("widget", new WidgetFactory(Component)); //TODO prohibit child content somehow
        registerFactory("container", new WidgetFactory(Component));
        registerFactory("bean", new WidgetFactory(Object));


        //
        // standalone window classes
        //
        registerFactory("dialog", new DialogFactory());
        registerBeanFactory("fileChooser", JFileChooser);
        registerFactory("frame", new FrameFactory());
        registerBeanFactory("optionPane", JOptionPane);
        registerFactory("window", new WindowFactory());


        //
        // widgets
        //
        registerFactory("button", new RichActionWidgetFactory(JButton));
        registerFactory("checkBox", new RichActionWidgetFactory(JCheckBox));
        registerFactory("checkBoxMenuItem", new RichActionWidgetFactory(JCheckBoxMenuItem));
        registerFactory("menuItem", new RichActionWidgetFactory(JMenuItem));
        registerFactory("radioButton", new RichActionWidgetFactory(JRadioButton));
        registerFactory("radioButtonMenuItem", new RichActionWidgetFactory(JRadioButtonMenuItem));
        registerFactory("toggleButton", new RichActionWidgetFactory(JToggleButton));

        registerFactory("editorPane", new TextArgWidgetFactory(JEditorPane));
        registerFactory("label", new TextArgWidgetFactory(JLabel));
        registerFactory("passwordField", new TextArgWidgetFactory(JPasswordField));
        registerFactory("textArea", new TextArgWidgetFactory(JTextArea));
        registerFactory("textField", new TextArgWidgetFactory(JTextField));
        registerFactory("textPane", new TextArgWidgetFactory(JTextPane));

        registerBeanFactory("colorChooser", JColorChooser);
        registerFactory("comboBox", new ComboBoxFactory());
        registerBeanFactory("desktopPane", JDesktopPane);
        registerFactory("formattedTextField", new FormattedTextFactory());
        registerBeanFactory("internalFrame", JInternalFrame);
        registerBeanFactory("layeredPane", JLayeredPane);
        registerBeanFactory("list", JList);
        registerBeanFactory("menu", JMenu);
        registerBeanFactory("menuBar", JMenuBar);
        registerBeanFactory("panel", JPanel);
        registerBeanFactory("popupMenu", JPopupMenu);
        registerBeanFactory("progressBar", JProgressBar);
        registerBeanFactory("scrollBar", JScrollBar);
        registerBeanFactory("scrollPane", JScrollPane);
        registerFactory("separator", new SeparatorFactory());
        registerBeanFactory("slider", JSlider);
        registerBeanFactory("spinner", JSpinner);
        registerFactory("splitPane", new SplitPaneFactory());
        registerBeanFactory("tabbedPane", JTabbedPane);
        registerBeanFactory("table", JTable);
        registerBeanFactory("tableColumn", TableColumn);
        registerBeanFactory("toolBar", JToolBar);
        //registerBeanFactory("tooltip", JToolTip); // doesn't work, use toolTipText property
        registerBeanFactory("tree", JTree);
        registerBeanFactory("viewport", JViewport); // sub class?


        //
        // MVC models
        //
        registerBeanFactory("boundedRangeModel", DefaultBoundedRangeModel);

        // spinner models
        registerBeanFactory("spinnerDateModel", SpinnerDateModel);
        registerBeanFactory("spinnerListModel", SpinnerListModel);
        registerBeanFactory("spinnerNumberModel", SpinnerNumberModel);

        // table models
        registerFactory("tableModel", new TableModelFactory());
        registerFactory("propertyColumn", new PropertyColumnFactory());
        registerFactory("closureColumn", new ClosureColumnFactory());


        //                                   
        // Layouts
        //
        registerBeanFactory("borderLayout", BorderLayout);
        registerBeanFactory("cardLayout", CardLayout);
        registerBeanFactory("flowLayout", FlowLayout);
        registerBeanFactory("gridBagLayout", GridBagLayout);
        registerBeanFactory("gridLayout", GridLayout);
        registerBeanFactory("overlayLayout", OverlayLayout);
        registerBeanFactory("springLayout", SpringLayout);
        registerBeanFactory("gridBagConstraints", GridBagConstraints);
        registerBeanFactory("gbc", GridBagConstraints); // shortcut name

        // Box layout and friends
        registerFactory("boxLayout", new BoxLayoutFactory());
        registerFactory("box", new BoxFactory());
        registerFactory("hbox", new HBoxFactory());
        registerFactory("hglue", new HGlueFactory());
        registerFactory("hstrut", new HStrutFactory());
        registerFactory("vbox", new VBoxFactory());
        registerFactory("vglue", new VGlueFactory());
        registerFactory("vstrut", new VStrutFactory());
        registerFactory("glue", new GlueFactory());
        registerFactory("rigidArea", new RigidAreaFactory());

        // table layout
        registerFactory("tableLayout", new TableLayoutFactory());
        registerFactory("tr", new TRFactory());
        registerFactory("td", new TDFactory());

    }

    public void registerBeanFactory(String theName, final Class beanClass) {
        registerFactory(theName, new BeanFactory(beanClass));
    }

    public void registerFactory(String name, Factory factory) {
        factories.put(name, factory);
    }

    public Object getConstraints() {
        return constraints;
    }
    
    public LinkedList getContainingWindows() {
        return containingWindows;
    }

    public Object getCurrent() { //NOPMD not pointless, makes it public from private
        return super.getCurrent();
    }
    
    public static void checkValueIsNull(Object value, Object name) {
        if (value != null) {
            throw new RuntimeException("$name elements do not accept a value argument.");
        }
    }

    public static boolean checkValueIsType(Object value, Object name, Class type) {
        if (value != null) {
            if (type.isAssignableFrom(value.getClass())) {
                return true;
            } else {
                throw new RuntimeException("The value argument of $name must be of type $type.name");
            }
        } else {
            return false;
        }
    }

    public static boolean checkValueIsTypeNotString(Object value, Object name, Class type) {
        if (value != null) {
            if (type.isAssignableFrom(value.getClass())) {
                return true;
            } else if (value instanceof String) {
                return false;
            } else {
                throw new RuntimeException("The value argument of $name must be of type $type.name or a String.");
            }
        } else {
            return false;
        }
    }

    public SwingBuilder edt(Closure c) {
        c.setDelegate(this);
        if (headless || SwingUtilities.isEventDispatchThread()) {
            c.call(this);
        } else {
            try {
                SwingUtilities.invokeAndWait(c.curry([this]));
            } catch (InterruptedException e) {
                throw new GroovyRuntimeException("interrupted swing interaction", e);
            } catch (InvocationTargetException e) {
                throw new GroovyRuntimeException("exception in event dispatch thread", e.getTargetException());
            }
        }
        return this;
    }
    
    public static SwingBuilder build(Closure c) {
        SwingBuilder builder = new SwingBuilder();
        return builder.edt(c);
    }
    
    public KeyStroke shortcut(int key, int modifier) {
        return KeyStroke.getKeyStroke(key, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | modifier);
    }

    public KeyStroke shortcut(int key) {
        return shortcut(key, 0);
    }

    public KeyStroke shortcut(char key, int modifier) {
        return KeyStroke.getKeyStroke(key, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | modifier);
    }

    public KeyStroke shortcut(char key) {
        return shortcut(key, 0);
    }

    public KeyStroke shortcut(Character key, int modifier) {
        return shortcut(key.charValue() as char, modifier);
    }

    public KeyStroke shortcut(Character key) {
        return shortcut(key.charValue(), 0);
    }

    public KeyStroke shortcut(String key, int modifier) {
        KeyStroke ks = KeyStroke.getKeyStroke(key);
        if (ks == null) {
            return null;
        } else {
            return KeyStroke.getKeyStroke(ks.getKeyCode(), ks.getModifiers() | modifier | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        }             
    }

    public KeyStroke shortcut(String key) {
        return shortcut(key, 0);
    }
}
