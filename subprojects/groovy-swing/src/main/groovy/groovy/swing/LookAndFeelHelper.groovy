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

import javax.swing.*
import javax.swing.plaf.metal.DefaultMetalTheme
import javax.swing.plaf.metal.MetalLookAndFeel
import javax.swing.plaf.metal.MetalTheme

/**
 * Helper for resolving and configuring Swing look and feels.
 */
class LookAndFeelHelper {

    // protected so you can subclass and replace the singleton
    /**
     * Shared helper instance used by {@link #getInstance()}.
     */
    protected static LookAndFeelHelper instance;
    private LookAndFeelHelper() {
        // linux GTK bug : http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6389282
        UIManager.getInstalledLookAndFeels()
    }

    /**
     * Returns the shared helper instance.
     *
     * @return the shared helper instance
     */
    static LookAndFeelHelper getInstance() {
        return instance ?: (instance = new LookAndFeelHelper())
    }

    private final Map<String, String> lafCodeNames = [
        // stuff built into various JDKs
        metal   : 'javax.swing.plaf.metal.MetalLookAndFeel',
        nimbus  : getNimbusLAFName(),
        mac     : getAquaLAFName(),
        motif   : 'com.sun.java.swing.plaf.motif.MotifLookAndFeel',
        windows : 'com.sun.java.swing.plaf.windows.WindowsLookAndFeel',
        win2k   : 'com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel',
        gtk     : 'com.sun.java.swing.plaf.gtk.GTKLookAndFeel',
        synth   : 'javax.swing.plaf.synth.SynthLookAndFeel',

        // generic aliases in UIManager
        system        : UIManager.getSystemLookAndFeelClassName(),
        crossPlatform : UIManager.getCrossPlatformLookAndFeelClassName(),

        // jgoodies, requires external library
        plastic   : 'com.jgoodies.looks.plastic.PlasticLookAndFeel',
        plastic3D : 'com.jgoodies.looks.plastic.Plastic3DLookAndFeel',
        plasticXP : 'com.jgoodies.looks.plastic.PlasticXPLookAndFeel',

        // substance, requires external library
        substance : getSubstanceLAFName(),

        // napkin, requires external library
        napkin : 'net.sourceforge.napkinlaf.NapkinLookAndFeel'
    ]

    /**
     * Registers an alias for a look and feel implementation class.
     *
     * @param alias the alias name
     * @param className the look and feel class name
     * @return the registered class name
     */
    String addLookAndFeelAlias(String alias, String className) {
        lafCodeNames[alias] = className
    }

    private final Map extendedAttributes = [
        'javax.swing.plaf.metal.MetalLookAndFeel' : [
            theme : { laf, theme ->
                if (!(theme instanceof MetalTheme)) {
                    if (theme == 'ocean') {
                        theme = Class.forName('javax.swing.plaf.metal.OceanTheme').getConstructor().newInstance()
                    } else if (theme == 'steel') {
                        theme = new DefaultMetalTheme();
                    } else {
                        theme = Class.forName(theme as String).getConstructor().newInstance()
                    }
                };
                MetalLookAndFeel.currentTheme = theme
            },
            boldFonts : { laf, bold -> UIManager.put('swing.boldMetal', bold as Boolean) },
            noxp : { laf, xp -> UIManager.put('swing.noxp', bold as Boolean) },
        ],
        'org.jvnet.substance.SubstanceLookAndFeel' : [
            // use setters instead of properties to get multi-dispatch
            theme: { laf, theme -> laf.setCurrentTheme(theme) },
            skin: { laf, skin -> laf.setSkin(skin) },
            watermark : { laf, watermark -> laf.setCurrentWatermark(watermark) },
        ],
    ]

    /**
     * Registers a handler for a custom look and feel attribute.
     *
     * @param className the look and feel class name
     * @param attr the attribute name
     * @param handler the handler closure
     * @return the registered handler
     */
    String addLookAndFeelAttributeHandler(String className, String attr, Closure handler) {
        Map attrs = extendedAttributes[className]
        if (attrs == null) {
            attrs = [:]
            extendedAttributes[className] = attrs
        }
        attrs[attr] = handler
    }

    /**
     * Indicates that look and feel helper nodes are always leaf nodes.
     *
     * @return {@code true}
     */
    boolean isLeaf() {
        return true
    }

    /**
     * Creates or updates the current look and feel from the supplied value and attributes.
     *
     * @param value the requested look and feel instance, alias, or class name
     * @param attributes additional look and feel configuration attributes
     * @param initClosure optional initialization callback invoked with the resolved look and feel
     * @return the configured look and feel
     */
    LookAndFeel lookAndFeel(Object value, Map attributes, Closure initClosure) {
        if (value instanceof Closure && initClosure == null) {
            initClosure = value
            value = null
        }
        if (value == null) {
            value = attributes.remove('lookAndFeel')
        }
        if (value instanceof GString) value = value.toString()

        LookAndFeel laf = UIManager.getLookAndFeel()
        try {
            if (FactoryBuilderSupport.checkValueIsTypeNotString(value, 'lookAndFeel', LookAndFeel)) {
                UIManager.setLookAndFeel(value)
            } else if (value != null) {
                UIManager.setLookAndFeel(lafCodeNames[value] ?: value)
            }

            LookAndFeel lafInstance = UIManager.getLookAndFeel()
            String lafClassName = lafInstance.class.name

            // assume all configuration must be done prior to LAF being installed
            Map possibleAttributes = extendedAttributes[lafClassName] ?: [:]

            attributes.each {k, v ->
                if (possibleAttributes[k]) {
                    possibleAttributes[k](lafInstance, v)
                } else {
                    try {
                        lafInstance."$k" = v
                    } catch (MissingPropertyException mpe) {
                        throw new RuntimeException("SwingBuilder initialization for the Look and Feel Class $lafClassName does accept the attribute $k")
                    }
                }
            }

            if (initClosure) {
                initClosure.call(lafInstance)
            }

            return lafInstance
        } catch (RuntimeException e) {
            UIManager.setLookAndFeel(laf)
            throw e
        }
    }

    /**
     * Finds the first available Nimbus look and feel implementation.
     *
     * @return the first available class name, or {@code null} if none are available
     */
    static String getNimbusLAFName() {
        for (klass in [
            'javax.swing.plaf.nimbus.NimbusLookAndFeel',
            'com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel',
            'sun.swing.plaf.nimbus.NimbusLookAndFeel',
            'org.jdesktop.swingx.plaf.nimbus.NimbusLookAndFeel'
        ]) {
            try {
                return Class.forName(klass).getName()
            } catch (Throwable t) {
                // ignore it, try the next on the list
            }
        }
        return null
    }

    /**
     * Finds the first available Aqua look and feel implementation.
     *
     * @return the first available class name, or {@code null} if none are available
     */
    static String getAquaLAFName() {
        for (klass in [
            'com.apple.laf.AquaLookAndFeel',
            'apple.laf.AquaLookAndFeel'
        ]) {
            try {
                return Class.forName(klass).getName()
            } catch (Throwable t) {
                // ignore it, try the next on the list
            }
        }
        return null
    }

    /**
     * Finds the first available Substance look and feel implementation.
     *
     * @return the first available class name, or {@code null} if none are available
     */
    static String getSubstanceLAFName() {
        for (klass in [
            'org.pushingpixels.substance.api.SubstanceLookAndFeel',
            'org.jvnet.substance.SubstanceLookAndFeel'
        ]) {
            try {
                return Class.forName(klass).getName()
            } catch (Throwable t) {
                // ignore it, try the next on the list
            }
        }
        return null
   }
}
