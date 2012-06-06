/*
 * Copyright 2003-2012 the original author or authors.
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
import groovy.inspect.InspectorTest;
import groovy.lang.*;
//import groovy.text.TemplateTest;
//import groovy.text.XmlTemplateEngineTest;
import groovy.tree.NodePrinterTest;
import groovy.util.EvalTest;
//import groovy.xml.FactorySupportTest;
//import groovy.xml.XmlTest;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * JDK 1.4 non-security related Java Unit tests in the 'groovy' dir
 */
public class JavaSourceGroovyPackagesNonSecuritySuite {

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(InspectorTest.class);
        suite.addTestSuite(GroovyShellTest.class);
        suite.addTestSuite(GStringTest.class);
        suite.addTestSuite(IntRangeTest.class);
        suite.addTestSuite(MetaClassTest.class);
        suite.addTestSuite(ScriptIntegerDivideTest.class);
        suite.addTestSuite(ScriptPrintTest.class);
        suite.addTestSuite(ScriptTest.class);
        suite.addTestSuite(SequenceTest.class);
        suite.addTestSuite(TupleTest.class);
//        suite.addTestSuite(TemplateTest.class);
//        suite.addTestSuite(XmlTemplateEngineTest.class);
        suite.addTestSuite(NodePrinterTest.class);
        suite.addTestSuite(EvalTest.class);
//        suite.addTestSuite(FactorySupportTest.class);
        suite.addTest(new RangeTestSuite());
//        suite.addTestSuite(XmlTest.class);
        return suite;
    }
}
