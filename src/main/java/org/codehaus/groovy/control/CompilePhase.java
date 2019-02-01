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
package org.codehaus.groovy.control;

/**
* The phases of the GroovyCompiler. This is an enum facade on top of the 
* Phases object. In general, prefer using this object over Phases. 
*/
public enum CompilePhase {

    /**
    * source files are opened and environment configured
    */ 
    INITIALIZATION(Phases.INITIALIZATION),
    
    /**
    * the grammar is used to to produce tree of tokens representing the source code
    */ 
    PARSING(Phases.PARSING),
    
    /**
    * An abstract syntax tree (AST) is created from token trees
    */ 
    CONVERSION(Phases.CONVERSION),
    
    /**
    * Performs consistency and validity checks that the grammar can't check for, and resolves classes
    */ 
    SEMANTIC_ANALYSIS(Phases.SEMANTIC_ANALYSIS),
    
    /**
    * Complete building the AST
    */ 
    CANONICALIZATION(Phases.CANONICALIZATION),
    
    /**
    * instruction set is chosen, for example java5 or pre java5
    */ 
    INSTRUCTION_SELECTION(Phases.INSTRUCTION_SELECTION),
    
    /**
    * creates the binary output in memory
    */ 
    CLASS_GENERATION(Phases.CLASS_GENERATION),
    
    /**
    * write the binary output to the file system
    */ 
    OUTPUT(Phases.OUTPUT),
    
    /**
    * Perform any last cleanup
    */ 
    FINALIZATION(Phases.FINALIZATION),
    ;

    /**
    * The phases as an array, with a null entry. 
    */ 
    public static CompilePhase[] phases = {
        null,
        INITIALIZATION,
        PARSING,
        CONVERSION,
        SEMANTIC_ANALYSIS,
        CANONICALIZATION,
        INSTRUCTION_SELECTION,
        CLASS_GENERATION,
        OUTPUT,
        FINALIZATION,
    };

    int phaseNumber;
    CompilePhase(int phaseNumber) {
        this.phaseNumber = phaseNumber;
    }

    /**
    * Returns the underlieng integer Phase number. 
    */ 
    public int getPhaseNumber() {
        return phaseNumber;
    }

    /**
     * Returns the CompilePhase for the given integer phase number.
     * @param phaseNumber
     *      the phase number
     * @return
     *      the CompilePhase or null if not found
     */
    public static CompilePhase fromPhaseNumber(int phaseNumber) {
        for (CompilePhase phase : values()) {
            if (phase.phaseNumber == phaseNumber) {
                return phase;
            }
        }
        return null;
    }
}
