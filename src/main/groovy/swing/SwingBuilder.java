/*
 $Id$

 Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is
distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing permissions and limitations under the License.

This work is copyright by the author(s) and is part of a greater work collectively copyright by the
Groovy community. See the NOTICE.txt file distributed with this work for additional information.

 */
package groovy.swing;

import groovy.lang.Closure;
import groovy.lang.MissingMethodException;

import groovy.model.DefaultTableModel;
import groovy.model.ValueHolder;
import groovy.model.ValueModel;

import groovy.swing.impl.ComponentFacade;
import groovy.swing.impl.ContainerFacade;
import groovy.swing.impl.DefaultAction;
import groovy.swing.impl.Factory;
import groovy.swing.impl.Startable;
import groovy.swing.impl.TableLayout;
import groovy.swing.impl.TableLayoutCell;
import groovy.swing.impl.TableLayoutRow;

import groovy.util.BuilderSupport;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.Window;

import java.text.Format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JToolTip;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.OverlayLayout;
import javax.swing.RootPaneContainer;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.codehaus.groovy.runtime.InvokerHelper;

/**
 * A helper class for creating Swing widgets using GroovyMarkup
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class SwingBuilder extends BuilderSupport {

    private Logger log = Logger.getLogger(getClass().getName());
    private Map factories = new HashMap();
    private Object constraints;
    private Map passThroughNodes = new HashMap();
    private Map widgets = new HashMap();
    // tracks all containing windows, for auto-owned dialogs
    private LinkedList containingWindows = new LinkedList();

    public SwingBuilder() {
        registerWidgets();
    }

    public Object getProperty(String name) {
        Object widget = widgets.get(name);
        if (widget == null) {
            return super.getProperty(name);
        }
        return widget;
    }

    protected void setParent(Object parent, Object child) {
        if (child instanceof Action) {
            Action action = (Action) child;
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
        } else if (child instanceof LayoutManager) {
            if (parent instanceof RootPaneContainer) {
                RootPaneContainer rpc = (RootPaneContainer) parent;
                parent = rpc.getContentPane();
            }
            InvokerHelper.setProperty(parent, "layout", child);
        } else if (child instanceof JToolTip && parent instanceof JComponent) {
            ((JToolTip) child).setComponent((JComponent) parent);
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
                    if (child instanceof JViewport) {
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
        }
    }

    protected void nodeCompleted(Object parent, Object node) {
        // set models after the node has been completed
        if (node instanceof TableModel && parent instanceof JTable) {
            JTable table = (JTable) parent;
            TableModel model = (TableModel) node;
            table.setModel(model);
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
        return createNode(name, Collections.EMPTY_MAP);
    }

    protected Object createNode(Object name, Object value) {
        if (passThroughNodes.containsKey(name) && (value != null) && ((Class) passThroughNodes.get(name)).isAssignableFrom(value.getClass())) {
            // value may need to go into containing windows list
            if (value instanceof Window) {
                containingWindows.add(value);
            }
            return value;
        } else if (value instanceof String) {
            Object widget = createNode(name);
            if (widget != null) {
                InvokerHelper.invokeMethod(widget, "setText", value);
            }
            return widget;
        } else {
            throw new MissingMethodException((String) name, getClass(), new Object[]{value}, false);
        }
    }

    protected Object createNode(Object name, Map attributes, Object value) {
        if (passThroughNodes.containsKey(name) && (value != null) && ((Class) passThroughNodes.get(name)).isAssignableFrom(value.getClass())) {
            // value may need to go into containing windows list
            if (value instanceof Window) {
                containingWindows.add(value);
            }
            handleWidgetAttributes(value, attributes);
            return value;
        } else {
            Object widget = createNode(name, attributes);
            if (widget != null && value != null) {
                InvokerHelper.invokeMethod(widget, "setText", value.toString());
            }
            return widget;
        }
    }

    protected Object createNode(Object name, Map attributes) {
        String widgetName = (String) attributes.remove("id");
        constraints = attributes.remove("constraints");
        Object widget = null;
        if (passThroughNodes.containsKey(name)) {
            widget = attributes.get(name);
            if ((widget != null) && ((Class) passThroughNodes.get(name)).isAssignableFrom(widget.getClass())) {
                // value may need to go into containing windows list
                if (widget instanceof Window) {
                    containingWindows.add(widget);
                }
                attributes.remove(name);
            } else {
                widget = null;
            }
        }
        if (widget == null) {
            Factory factory = (Factory) factories.get(name);
            if (factory != null) {
                try {
                    widget = factory.newInstance(attributes);
                    if (widgetName != null) {
                        widgets.put(widgetName, widget);
                    }
                    if (widget == null) {
                        log.log(Level.WARNING, "Factory for name: " + name + " returned null");
                    } else {
                        if (log.isLoggable(Level.FINE)) {
                            log.fine("For name: " + name + " created widget: " + widget);
                        }
                    }
                }
                catch (Exception e) {
                    throw new RuntimeException("Failed to create component for" + name + " reason: " + e, e);
                }
            } else {
                log.log(Level.WARNING, "Could not find match for name: " + name);
            }
        }
        handleWidgetAttributes(widget, attributes);
        return widget;
    }

    protected void handleWidgetAttributes(Object widget, Map attributes) {
        if (widget != null) {
            if (widget instanceof Action) {
                /* TODO we could move this custom logic into the MetaClass for Action */
                Action action = (Action) widget;

                Closure closure = (Closure) attributes.remove("closure");
                if (closure != null && action instanceof DefaultAction) {
                    DefaultAction defaultAction = (DefaultAction) action;
                    defaultAction.setClosure(closure);
                }

                Object accel = attributes.remove("accelerator");
                KeyStroke stroke = null;
                if (accel instanceof KeyStroke) {
                    stroke = (KeyStroke) accel;
                } else if (accel != null) {
                    stroke = KeyStroke.getKeyStroke(accel.toString());
                }
                action.putValue(Action.ACCELERATOR_KEY, stroke);

                Object mnemonic = attributes.remove("mnemonic");
                if ((mnemonic != null) && !(mnemonic instanceof Number)) {
                    mnemonic = new Integer(mnemonic.toString().charAt(0));
                }
                action.putValue(Action.MNEMONIC_KEY, mnemonic);

                for (Iterator iter = attributes.entrySet().iterator(); iter.hasNext();) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    String actionName = (String) entry.getKey();    // todo dk: misleading naming. this can be any property name

                    // typically standard Action names start with upper case, so lets upper case it            
                    actionName = capitalize(actionName);            // todo dk: in general, this shouldn't be capitalized
                    Object value = entry.getValue();

                    action.putValue(actionName, value);
                }

            } else {
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
                if ((mnemonic != null) && (mnemonic instanceof Number)) {
                    InvokerHelper.setProperty(widget, "mnemonic", new Character((char) ((Number) mnemonic).intValue()));
                } else if (mnemonic != null) {
                    InvokerHelper.setProperty(widget, "mnemonic", new Character(mnemonic.toString().charAt(0)));
                }

                // set the properties
                for (Iterator iter = attributes.entrySet().iterator(); iter.hasNext();) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    String property = entry.getKey().toString();
                    Object value = entry.getValue();
                    InvokerHelper.setProperty(widget, property, value);
                }
            }
        }
    }

    protected String capitalize(String text) {
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
        registerBeanFactory("action", DefaultAction.class);
        passThroughNodes.put("action", Action.class);
        registerBeanFactory("buttonGroup", ButtonGroup.class);
        registerFactory("map", new Factory() {      // todo dk: is that still needed?
            public Object newInstance(Map properties) throws InstantiationException, IllegalAccessException {
                return properties;
            }
        });
        // ulimate pass through type
        passThroughNodes.put("widget", Component.class);

        //
        // standalone window classes
        //
        registerFactory("dialog", new Factory() {
            public Object newInstance(Map properties) throws InstantiationException, IllegalAccessException {
                return createDialog(properties);
            }
        });
        registerBeanFactory("fileChooser", JFileChooser.class);
        registerFactory("frame", new Factory() {
            public Object newInstance(Map properties) throws InstantiationException, IllegalAccessException {
                return createFrame(properties);
            }
        });
        registerBeanFactory("optionPane", JOptionPane.class);
        registerFactory("window", new Factory() {
            public Object newInstance(Map properties) throws InstantiationException, IllegalAccessException {
                return createWindow(properties);
            }
        });

        //
        // widgets
        //
        registerBeanFactory("button", JButton.class);
        registerBeanFactory("checkBox", JCheckBox.class);
        registerBeanFactory("checkBoxMenuItem", JCheckBoxMenuItem.class);
        registerBeanFactory("colorChooser", JColorChooser.class);
        registerFactory("comboBox", new Factory() {
            public Object newInstance(Map properties) throws InstantiationException, IllegalAccessException {
                return createComboBox(properties);
            }
        });
        registerBeanFactory("desktopPane", JDesktopPane.class);
        registerBeanFactory("editorPane", JEditorPane.class);
        registerFactory("formattedTextField", new Factory() {
            public Object newInstance(Map properties) throws InstantiationException, IllegalAccessException {
                return createFormattedTextField(properties);
            }
        });
        registerBeanFactory("internalFrame", JInternalFrame.class);
        registerBeanFactory("label", JLabel.class);
        registerBeanFactory("layeredPane", JLayeredPane.class);
        registerBeanFactory("list", JList.class);
        registerBeanFactory("menu", JMenu.class);
        registerBeanFactory("menuBar", JMenuBar.class);
        registerBeanFactory("menuItem", JMenuItem.class);
        registerBeanFactory("panel", JPanel.class);
        registerBeanFactory("passwordField", JPasswordField.class);
        registerBeanFactory("popupMenu", JPopupMenu.class);
        registerBeanFactory("progressBar", JProgressBar.class);
        registerBeanFactory("radioButton", JRadioButton.class);
        registerBeanFactory("radioButtonMenuItem", JRadioButtonMenuItem.class);
        registerBeanFactory("scrollBar", JScrollBar.class);
        registerBeanFactory("scrollPane", JScrollPane.class);
        registerBeanFactory("separator", JSeparator.class);
        registerBeanFactory("slider", JSlider.class);
        registerBeanFactory("spinner", JSpinner.class);
        registerFactory("splitPane", new Factory() {
            public Object newInstance(Map properties) {
                JSplitPane answer = new JSplitPane();
                answer.setLeftComponent(null);
                answer.setRightComponent(null);
                answer.setTopComponent(null);
                answer.setBottomComponent(null);
                return answer;
            }
        });
        registerBeanFactory("tabbedPane", JTabbedPane.class);
        registerBeanFactory("table", JTable.class);
        registerBeanFactory("textArea", JTextArea.class);
        registerBeanFactory("textPane", JTextPane.class);
        registerBeanFactory("textField", JTextField.class);
        registerBeanFactory("toggleButton", JToggleButton.class);
        registerBeanFactory("toolBar", JToolBar.class);
        //registerBeanFactory("tooltip", JToolTip.class); // doesn't work, use toolTipText property
        registerBeanFactory("tree", JTree.class);
        registerBeanFactory("viewport", JViewport.class); // sub class?

        //
        // MVC models
        //
        registerBeanFactory("boundedRangeModel", DefaultBoundedRangeModel.class);

        // spinner models
        registerBeanFactory("spinnerDateModel", SpinnerDateModel.class);
        registerBeanFactory("spinnerListModel", SpinnerListModel.class);
        registerBeanFactory("spinnerNumberModel", SpinnerNumberModel.class);

        // table models
        registerFactory("tableModel", new Factory() {
            public Object newInstance(Map properties) {
                ValueModel model = (ValueModel) properties.remove("model");
                if (model == null) {
                    Object list = properties.remove("list");
                    if (list == null) {
                        list = new ArrayList();
                    }
                    model = new ValueHolder(list);
                }
                return new DefaultTableModel(model);
            }
        });
        passThroughNodes.put("tableModel", TableModel.class);

        registerFactory("propertyColumn", new Factory() {
            public Object newInstance(Map properties) {
                Object current = getCurrent();
                if (current instanceof DefaultTableModel) {
                    DefaultTableModel model = (DefaultTableModel) current;
                    Object header = properties.remove("header");
                    if (header == null) {
                        header = "";
                    }
                    String property = (String) properties.remove("propertyName");
                    if (property == null) {
                        throw new IllegalArgumentException("Must specify a property for a propertyColumn");
                    }
                    Class type = (Class) properties.remove("type");
                    if (type == null) {
                        type = Object.class;
                    }
                    return model.addPropertyColumn(header, property, type);
                } else {
                    throw new RuntimeException("propertyColumn must be a child of a tableModel");
                }
            }
        });

        registerFactory("closureColumn", new Factory() {
            public Object newInstance(Map properties) {
                Object current = getCurrent();
                if (current instanceof DefaultTableModel) {
                    DefaultTableModel model = (DefaultTableModel) current;
                    Object header = properties.remove("header");
                    if (header == null) {
                        header = "";
                    }
                    Closure readClosure = (Closure) properties.remove("read");
                    if (readClosure == null) {
                        throw new IllegalArgumentException("Must specify 'read' Closure property for a closureColumn");
                    }
                    Closure writeClosure = (Closure) properties.remove("write");
                    Class type = (Class) properties.remove("type");
                    if (type == null) {
                        type = Object.class;
                    }
                    return model.addClosureColumn(header, readClosure, writeClosure, type);
                } else {
                    throw new RuntimeException("closureColumn must be a child of a tableModel");
                }
            }
        });

        //Standard Layouts
        registerBeanFactory("borderLayout", BorderLayout.class);
        registerBeanFactory("cardLayout", CardLayout.class);
        registerBeanFactory("flowLayout", FlowLayout.class);
        registerBeanFactory("gridBagLayout", GridBagLayout.class);
        registerBeanFactory("gridLayout", GridLayout.class);
        registerBeanFactory("overlayLayout", OverlayLayout.class);
        registerBeanFactory("springLayout", SpringLayout.class);
        registerBeanFactory("gridBagConstraints", GridBagConstraints.class);
        registerBeanFactory("gbc", GridBagConstraints.class); // shortcut name

        // box layout
        registerFactory("boxLayout", new Factory() {
            public Object newInstance(Map properties) throws InstantiationException, IllegalAccessException {
                return createBoxLayout(properties);
            }
        });

        // Box related layout components
        registerFactory("hbox", new Factory() {
            public Object newInstance(Map properties) {
                return Box.createHorizontalBox();
            }
        });
        registerFactory("hglue", new Factory() {
            public Object newInstance(Map properties) {
                return Box.createHorizontalGlue();
            }
        });
        registerFactory("hstrut", new Factory() {
            public Object newInstance(Map properties) {
                Object num = properties.remove("width");
                if (num instanceof Number) {
                    return Box.createHorizontalStrut(((Number) num).intValue());
                } else {
                    return Box.createHorizontalStrut(6);
                }
            }
        });
        registerFactory("vbox", new Factory() {
            public Object newInstance(Map properties) {
                return Box.createVerticalBox();
            }
        });
        registerFactory("vglue", new Factory() {
            public Object newInstance(Map properties) {
                return Box.createVerticalGlue();
            }
        });
        registerFactory("vstrut", new Factory() {
            public Object newInstance(Map properties) {
                Object num = properties.remove("height");
                if (num instanceof Number) {
                    return Box.createVerticalStrut(((Number) num).intValue());
                } else {
                    return Box.createVerticalStrut(6);
                }
            }
        });
        registerFactory("glue", new Factory() {
            public Object newInstance(Map properties) {
                return Box.createGlue();
            }
        });
        registerFactory("rigidArea", new Factory() {
            public Object newInstance(Map properties) {
                Dimension dim;
                Object o = properties.remove("size");
                if (o instanceof Dimension) {
                    dim = (Dimension) o;
                } else {
                    int w, h;
                    o = properties.remove("width");
                    w = ((o instanceof Number)) ? ((Number) o).intValue() : 6;
                    o = properties.remove("height");
                    h = ((o instanceof Number)) ? ((Number) o).intValue() : 6;
                    dim = new Dimension(w, h);
                }
                return Box.createRigidArea(dim);
            }
        });

        // table layout
        registerBeanFactory("tableLayout", TableLayout.class);
        registerFactory("tr", new Factory() {
            public Object newInstance(Map properties) {
                Object parent = getCurrent();
                if (parent instanceof TableLayout) {
                    return new TableLayoutRow((TableLayout) parent);
                } else {
                    throw new RuntimeException("'tr' must be within a 'tableLayout'");
                }
            }
        });
        registerFactory("td", new Factory() {
            public Object newInstance(Map properties) {
                Object parent = getCurrent();
                if (parent instanceof TableLayoutRow) {
                    return new TableLayoutCell((TableLayoutRow) parent);
                } else {
                    throw new RuntimeException("'td' must be within a 'tr'");
                }
            }
        });
    }

    protected Object createBoxLayout(Map properties) {
        Object parent = getCurrent();
        if (parent instanceof Container) {
            Object axisObject = properties.remove("axis");
            int axis = BoxLayout.X_AXIS;
            if (axisObject != null) {
                Integer i = (Integer) axisObject;
                axis = i.intValue();
            }

            Container target = (Container) parent;
            if (target instanceof RootPaneContainer) {
                target = ((RootPaneContainer) target).getContentPane();
            }
            BoxLayout answer = new BoxLayout(target, axis);

            // now let's try to set the layout property
            InvokerHelper.setProperty(parent, "layout", answer);
            return answer;
        } else {
            throw new RuntimeException("Must be nested inside a Container");
        }
    }

    protected Object createDialog(Map properties) {
        JDialog dialog;
        Object owner = properties.remove("owner");
        // if owner not explicit, use the last window type in the list
        if ((owner == null) && !containingWindows.isEmpty()) {
            owner = containingWindows.getLast();
        }
        if (owner instanceof Frame) {
            dialog = new JDialog((Frame) owner);
        } else if (owner instanceof Dialog) {
            dialog = new JDialog((Dialog) owner);
        } else {
            dialog = new JDialog();
        }
        containingWindows.add(dialog);
        return dialog;
    }

    /*
     * Uses 'format," or "value,"  (in order)
     */
    protected Object createFormattedTextField(Map properties) {
        JFormattedTextField ftf;
        if (properties.containsKey("format")) {
            ftf = new JFormattedTextField((Format) properties.remove("format"));
        } else if (properties.containsKey("value")) {
            ftf = new JFormattedTextField(properties.remove("value"));
        } else {
            ftf = new JFormattedTextField();
        }
        return ftf;
    }

    protected Object createFrame(Map properties) {
        JFrame frame = new JFrame();
        containingWindows.add(frame);
        return frame;
    }

    protected Object createWindow(Map properties) {
        JWindow window;
        Object owner = properties.remove("owner");
        // if owner not explicit, use the last window type in the list
        if ((owner == null) && !containingWindows.isEmpty()) {
            owner = containingWindows.getLast();
        }
        if (owner instanceof Frame) {
            window = new JWindow((Frame) owner);
        } else if (owner instanceof Window) {
            window = new JWindow((Window) owner);
        } else {
            window = new JWindow();
        }
        containingWindows.add(window);
        return window;
    }

    protected Object createComboBox(Map properties) {
        Object items = properties.remove("items");
        if (items instanceof Vector) {
            return new JComboBox((Vector) items);
        } else if (items instanceof List) {
            List list = (List) items;
            return new JComboBox(list.toArray());
        } else if (items instanceof Object[]) {
            return new JComboBox((Object[]) items);
        } else {
            return new JComboBox();
        }
    }

    protected void registerBeanFactory(String name, final Class beanClass) {
        registerFactory(name, new Factory() {
            public Object newInstance(Map properties) throws InstantiationException, IllegalAccessException {
                return beanClass.newInstance();
            }
        });

    }

    protected void registerFactory(String name, Factory factory) {
        factories.put(name, factory);
    }
}
