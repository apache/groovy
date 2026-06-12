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

/**
 * Generates AsciiDoc documentation for SwingBuilder widgets.
 *
 * This script introspects a live SwingBuilder instance to discover all
 * registered nodes, their categories, underlying Swing classes, and
 * JavaBeans properties. It produces a single file
 * (_swing-builder-widgets.adoc) containing category summary tables
 * followed by detail sections for each widget.
 *
 * Usage:
 *   groovy GenerateSwingBuilderWidgetDocs.groovy [outputDir]
 *
 * If outputDir is omitted, output goes to src/spec/doc under this subproject.
 */

import groovy.swing.SwingBuilder
import groovy.swing.factory.BeanFactory
import groovy.swing.factory.ComponentFactory
import groovy.swing.factory.RichActionWidgetFactory
import groovy.swing.factory.TextArgWidgetFactory
import groovy.swing.factory.LayoutFactory
import groovy.swing.factory.WidgetFactory

import java.beans.BeanInfo
import java.beans.Introspector
import java.beans.PropertyDescriptor

// Headless mode should be set via -Djava.awt.headless=true on the JVM command line
if (!Boolean.getBoolean('java.awt.headless')) {
    System.setProperty('java.awt.headless', 'true')
}

def outputDir = args.length > 0 ? new File(args[0]) : new File('src/spec/doc')
outputDir.mkdirs()

def builder = new SwingBuilder()

// Collect all factories keyed by node name
Map<String, groovy.util.Factory> factories = builder.factories

// Collect registration groups (categories) - these come from the register*() method names
Map<String, Set<String>> groups = [:]
builder.registrationGroups.each { groupName ->
    def items = builder.getRegistrationGroupItems(groupName)
    if (items && groupName) {
        groups[groupName] = items
    }
}

// Also collect explicit methods (edt, doLater, etc.)
Map<String, Closure> explicitMethods = builder.localExplicitMethods

// Human-friendly category names derived from method name suffixes
def categoryLabel = { String groupName ->
    // e.g. "ActionButtonWidgets" -> "Action Button Widgets"
    // e.g. "MDIWidgets" -> "MDI Widgets"
    groupName.replaceAll(/([a-z])([A-Z])/, '$1 $2')
             .replaceAll(/([A-Z]+)([A-Z][a-z])/, '$1 $2')
}

/**
 * Known Swing class mappings for factories that don't expose the class via a standard field.
 * This covers factories extending AbstractFactory directly (e.g. RootPaneContainerFactory subclasses)
 * and custom factories with non-standard constructors.
 */
def knownSwingClasses = [
    'frame'            : javax.swing.JFrame,
    'dialog'           : javax.swing.JDialog,
    'window'           : javax.swing.JWindow,
    'internalFrame'    : javax.swing.JInternalFrame,
    'action'           : javax.swing.Action,
    'imageIcon'        : javax.swing.ImageIcon,
    'comboBox'         : javax.swing.JComboBox,
    'list'             : javax.swing.JList,
    'separator'        : javax.swing.JSeparator,
    'scrollPane'       : javax.swing.JScrollPane,
    'splitPane'        : javax.swing.JSplitPane,
    'table'            : javax.swing.JTable,
    'formattedTextField': javax.swing.JFormattedTextField,
    'box'              : javax.swing.Box,
    'hbox'             : javax.swing.Box,
    'vbox'             : javax.swing.Box,
]

/**
 * Determine the underlying Swing/AWT class for a given factory.
 */
def resolveSwingClass = { String nodeName, groovy.util.Factory factory ->
    if (factory == null) return null

    // Check known mappings first
    if (knownSwingClasses.containsKey(nodeName)) {
        return knownSwingClasses[nodeName]
    }

    // BeanFactory and subclasses (ComponentFactory, TabbedPaneFactory, etc.) store beanClass
    if (factory instanceof BeanFactory) {
        return factory.beanClass
    }
    // RichActionWidgetFactory stores klass
    if (factory instanceof RichActionWidgetFactory) {
        return factory.klass
    }
    // TextArgWidgetFactory extends RichActionWidgetFactory
    if (factory instanceof TextArgWidgetFactory) {
        return factory.klass
    }
    // LayoutFactory stores its layout class
    if (factory instanceof LayoutFactory) {
        try {
            return factory.klass
        } catch (Exception e) {
            // some layout factories may not expose klass
        }
    }
    // WidgetFactory stores restrictedType
    if (factory instanceof WidgetFactory) {
        return factory.restrictedType
    }

    // For factories registered via registerBeanFactory in FactoryBuilderSupport,
    // the anonymous inner class wraps the bean class. Try to get it reflectively.
    try {
        def field = factory.getClass().getDeclaredFields().find { it.name == 'val$beanClass' || it.name == 'beanClass' }
        if (field) {
            field.accessible = true
            return field.get(factory)
        }
    } catch (Exception ignored) {}

    return null
}

/**
 * Get JavaBeans properties for a class, excluding Object-level properties.
 */
def getProperties = { Class clazz ->
    if (clazz == null) return []
    try {
        BeanInfo info = Introspector.getBeanInfo(clazz, Object)
        return info.propertyDescriptors
            .findAll { it.name != 'class' }
            .sort { it.name }
    } catch (Exception e) {
        return []
    }
}

/**
 * Format a Java type name for display (simplify common types).
 */
static String formatType(Class type) {
    if (type == null) return 'Object'
    if (type.isArray()) return formatType(type.componentType) + '[]'
    def name = type.name
    // Simplify common java.lang and java.awt types
    name = name.replaceAll(/^java\.lang\./, '')
    name = name.replaceAll(/^java\.awt\./, 'awt.')
    name = name.replaceAll(/^javax\.swing\./, '')
    name = name.replaceAll(/^java\.util\./, '')
    // Simplify primitive wrapper names
    return name
}

// ============================================================
// Collect widget details
// ============================================================

def widgetDetails = [:] // nodeName -> [swingClass, beanProps, factoryClass, isLeaf]

factories.each { nodeName, factory ->
    def swingClass = resolveSwingClass(nodeName, factory)
    def props = getProperties(swingClass)
    def isLeaf = factory.leaf
    widgetDetails[nodeName] = [
        swingClass: swingClass,
        beanProps: props,
        factoryClass: factory.getClass(),
        isLeaf: isLeaf
    ]
}

/**
 * Produce a minimal example argument hint based on node name.
 */
String exampleArgs(String nodeName) {
    switch (nodeName) {
        case ~/.*[Bb]order.*/:
            return ''
        case ~/.*[Ll]ayout.*/:
            return ''
        case 'frame': case 'dialog': case 'window':
            return "title: 'My Window'"
        case 'label':
            return "'Hello'"
        case 'button': case 'checkBox': case 'radioButton': case 'toggleButton':
        case 'menuItem': case 'checkBoxMenuItem': case 'radioButtonMenuItem':
            return "'Click Me'"
        case 'textField': case 'textArea': case 'passwordField':
        case 'editorPane': case 'textPane':
            return "text: 'initial text', columns: 20"
        case 'action':
            return "name: 'MyAction', closure: { println 'action' }"
        case 'bind':
            return "source: model, sourceProperty: 'name'"
        case 'imageIcon':
            return "url: getClass().getResource('/icon.png')"
        default:
            return ''
    }
}

// ============================================================
// Generate single output file
// ============================================================

def outFile = new File(outputDir, '_swing-builder-widgets.adoc')
def out = new StringBuilder()
out << "// Generated by GenerateSwingBuilderWidgetDocs.groovy - do not edit manually\n"
out << "[[swing-builder-widgets]]\n"
out << "= SwingBuilder Widgets Reference\n\n"
out << "The following tables list all nodes available in `SwingBuilder`, grouped by category.\n"
out << "Each node name links to its detail section showing properties.\n\n"

// Emit groups in a logical order
def orderedGroups = [
    'SupportNodes', 'Binding', 'PassThruNodes',
    'Windows', 'ActionButtonWidgets', 'TextWidgets',
    'BasicWidgets', 'MDIWidgets', 'MenuWidgets',
    'Containers', 'DataModels',
    'TableComponents', 'Renderers', 'Editors',
    'BasicLayouts', 'BoxLayout', 'TableLayout',
    'Borders', 'Threading'
]

// Include any groups not in our ordered list
def remainingGroups = groups.keySet() - orderedGroups.toSet()
orderedGroups.addAll(remainingGroups.sort())

orderedGroups.each { groupName ->
    def items = groups[groupName]
    if (!items) return

    out << "[[swing-group-${groupName.toLowerCase()}]]\n"
    out << "== ${categoryLabel(groupName)}\n\n"
    out << '[cols="2,3,1", options="header"]\n'
    out << "|===\n"
    out << "| Node Name | Swing Class | Leaf\n\n"

    items.sort().each { nodeName ->
        def detail = widgetDetails[nodeName]
        def swingClassName = detail?.swingClass ? "`${detail.swingClass.simpleName}`" : '-'
        def isLeaf = detail?.isLeaf ?: false
        out << "| <<swing-widget-${nodeName},${nodeName}>>\n"
        out << "| ${swingClassName}\n"
        out << "| ${isLeaf}\n\n"
    }
    out << "|===\n\n"
}

// Explicit methods section
if (explicitMethods) {
    out << "[[swing-group-methods]]\n"
    out << "== Utility Methods\n\n"
    out << "These are explicit methods (not node factories) available on `SwingBuilder`:\n\n"
    out << '[cols="2,4", options="header"]\n'
    out << "|===\n"
    out << "| Method | Description\n\n"

    def methodDescriptions = [
        'edt': 'Run a closure on the Event Dispatch Thread (blocking)',
        'doLater': 'Run a closure on the EDT asynchronously (non-blocking)',
        'doOutside': 'Run a closure outside the EDT in a new thread',
        'keyStrokeAction': 'Create keyboard shortcut bindings on a component',
        'shortcut': 'Create a platform-appropriate KeyStroke (uses Cmd on Mac, Ctrl elsewhere)',
    ]

    explicitMethods.keySet().sort().each { methodName ->
        def desc = methodDescriptions[methodName] ?: ''
        out << "| [[swing-widget-${methodName}]]`${methodName}()`\n"
        out << "| ${desc}\n\n"
    }
    out << "|===\n"
}

// ============================================================
// Widget detail sections
// ============================================================

out << "\n[[swing-widget-details]]\n"
out << "== Widget Details\n\n"

widgetDetails.sort { it.key }.each { nodeName, detail ->
    def swingClass = detail.swingClass

    out << "'''\n\n"
    out << "[[swing-widget-${nodeName}]]\n"
    out << "=== ${nodeName}\n\n"

    if (swingClass) {
        def jdkPackage = swingClass.name.startsWith('javax.swing') || swingClass.name.startsWith('java.awt')
        out << "[horizontal]\n"
        out << "Swing Class:: `${swingClass.name}`\n"
        out << "Leaf:: ${detail.isLeaf ?: false}\n"
        if (jdkPackage) {
            out << "API Docs:: jdk:${swingClass.name}[${swingClass.simpleName} javadoc]\n"
        }
        out << "\n"
    }

    // Usage example
    out << ".Basic usage\n"
    out << "[source,groovy]\n"
    out << "----\n"
    out << "swing.${nodeName}(${exampleArgs(nodeName)})\n"
    out << "----\n\n"

    def props = detail.beanProps
    if (props) {
        out << "==== Properties\n\n"
        out << '[cols="2,2,1,1", options="header"]\n'
        out << "|===\n"
        out << "| Property | Type | Readable | Writable\n\n"
        props.each { PropertyDescriptor pd ->
            def type = pd.propertyType ? formatType(pd.propertyType) : 'Object'
            def readable = pd.readMethod ? 'icon:check[]' : ''
            def writable = pd.writeMethod ? 'icon:check[]' : ''
            out << "| `${pd.name}`\n"
            out << "| `${type}`\n"
            out << "| ${readable}\n"
            out << "| ${writable}\n\n"
        }
        out << "|===\n\n"
    }
}

outFile.text = out.toString()

println "Generated SwingBuilder widget documentation:"
println "  Output: ${outFile.absolutePath}"
println "  Categories: ${groups.size()}"
println "  Total nodes: ${factories.size()}"
