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
package org.apache.groovy.groovyfx.factory

import javafx.stage.Stage
import javafx.stage.StageStyle
import org.apache.groovy.groovyfx.GroovyFX

/**
 * Creates stages. The first {@code stage} node given no explicit stage reuses
 * the builder's {@code primaryStage} variable when one is set (as it is by
 * {@code GroovyFX.start}). A {@code style} attribute selects the
 * {@code StageStyle}, and {@code show: true} shows the stage once its
 * children have been built.
 */
class StageFactory extends NodeFactory {

    private static final String PRIMARY_STAGE_USED = 'groovyfx.primaryStageUsed'
    private static final String SHOW_ON_COMPLETION = 'groovyfx.showOnCompletion'

    StageFactory() {
        super(Stage)
    }

    @Override
    Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) {
        if (value instanceof Stage) return value
        if (value != null) {
            throw new IllegalArgumentException("The value of 'stage' must be a Stage. Found: ${value.getClass().name}")
        }
        Map variables = builder.variables
        Object primary = variables[GroovyFX.PRIMARY_STAGE]
        if (primary instanceof Stage && !variables[PRIMARY_STAGE_USED]) {
            variables[PRIMARY_STAGE_USED] = true
            return primary
        }
        new Stage()
    }

    @Override
    boolean onHandleNodeAttributes(FactoryBuilderSupport builder, Object node, Map attributes) {
        Stage stage = (Stage) node
        Object style = attributes.remove('style')
        if (style != null) {
            stage.initStyle(style instanceof StageStyle ? style : StageStyle.valueOf(style.toString().toUpperCase(Locale.ROOT)))
        }
        Object show = attributes.remove('show')
        if (show != null) {
            stage.properties[SHOW_ON_COMPLETION] = show as boolean
        }
        super.onHandleNodeAttributes(builder, node, attributes)
    }

    @Override
    void onNodeCompleted(FactoryBuilderSupport builder, Object parent, Object node) {
        Stage stage = (Stage) node
        if (stage.properties.remove(SHOW_ON_COMPLETION)) {
            stage.show()
        }
    }
}
