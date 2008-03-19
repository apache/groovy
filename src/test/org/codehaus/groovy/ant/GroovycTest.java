/*
 *  Copyright © 2008 Russel Winder
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is
 *  distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied.  See the License for the specific language governing permissions and limitations under the
 *  License.
 */

package org.codehaus.groovy.ant ;

import java.io.File ;
import java.io.FileNotFoundException ;
import java.io.FileReader ;
import java.io.IOException ;
import org.apache.tools.ant.BuildException ;
import org.apache.tools.ant.Project ;
import org.apache.tools.ant.ProjectHelper ;
import groovy.util.GroovyTestCase ;

/**
 *  Unit tests for the {@link Groovyc} ant task.
 *
 *  <p>NB the *.groovy files in this directory should not get compiled with the rest of the test classes
 *  since that would ruin the whole point of testing compilation by the Ant tasks.  In fact it doesn't
 *  matter as the tests remove all class files that should not pre-exist from this directory at each
 *  step</p>
 *
 * @author Russel Winder
 */
public class GroovycTest extends GroovyTestCase {
  private final String classDirectory = "target/test-classes/org/codehaus/groovy/ant/" ;
  private final File antFile = new File ( "src/test/org/codehaus/groovy/ant/GroovycTest.xml" ) ;
  private Project project ;
  protected void setUp ( ) throws Exception {
    super.setUp ( ) ; //  Potentially throws Exception.
    project = new Project ( ) ;
    project.init ( ) ;
    ProjectHelper.getProjectHelper ( ).parse ( project , antFile ) ;
    project.executeTarget ( "clean" ) ;
    String altJavaHome = System.getProperty("java.home");
    if (altJavaHome.contains("jre")) {
        altJavaHome = altJavaHome.substring(0, altJavaHome.lastIndexOf("jre"));
    } else {
        altJavaHome = altJavaHome + "/jre";
    }
    project.setProperty("alt.java.home", altJavaHome);
  }
  private void ensureNotPresent ( final String classname ) {
    if ( ! ( new File ( classDirectory + "GroovycTest.class" ) ).exists ( ) ) {
      fail ( "Class file for GroovycTest does not exist and should." ) ;
    }
    if ( ( new File ( classDirectory + classname + ".class" ) ).exists ( ) ) {
      fail ( "Class file for " + classname + " already exists and shouldn't." ) ;
    }
  }
  private void ensureResultOK ( final String classname ) {
    if ( ! ( new File ( classDirectory + classname + ".class" ) ).exists ( ) ) {
      fail ( "Class file for " + classname + " does not exist and should." ) ;
    }
    final File result = new File ( classDirectory + classname + "_Result.txt" ) ;
    final char[] buffer = new char [ 10 ] ;
    try {
      ( new FileReader ( result ) ).read ( buffer ) ;
      assertEquals ( "OK." , new String ( buffer ).trim ( ) ) ;
    }
    catch ( final FileNotFoundException fnfe ) { fail ( "File " + result.getName ( ) + " should have been created but wasn't." ) ; }
    catch ( final IOException ioe ) { fail ( "Error reading file " + result.getName ( ) + "." ) ; }
  }

  public void testGroovycTest1_NoFork_NoClasspath ( ) {
    ensureNotPresent ( "GroovycTest1" ) ;
    project.executeTarget ( "GroovycTest1_NoFork_NoClasspath" ) ;
    ensureResultOK ( "GroovycTest1" ) ;
  }
  public void testGroovycTest1_NoFork_WithGroovyClasspath ( ) {
    ensureNotPresent ( "GroovycTest1" ) ;
    project.executeTarget ( "GroovycTest1_NoFork_WithGroovyClasspath" ) ;
    ensureResultOK ( "GroovycTest1" ) ;
  }
  public void testGroovycTest1_NoFork_WithJavaClasspath ( ) {
    ensureNotPresent ( "GroovycTest1" ) ;
    project.executeTarget ( "GroovycTest1_NoFork_WithJavaClasspath" ) ;
    ensureResultOK ( "GroovycTest1" ) ;
  }
  public void testGroovycTest1_NoFork_WithBothClasspath ( ) {
    ensureNotPresent ( "GroovycTest1" ) ;
    project.executeTarget ( "GroovycTest1_NoFork_WithBothClasspath" ) ;
    ensureResultOK ( "GroovycTest1" ) ;
  }

  public void testGroovycTest1_ForkGroovy_NoClasspath ( ) {
    ensureNotPresent ( "GroovycTest1" ) ;
    project.executeTarget ( "GroovycTest1_ForkGroovy_NoClasspath" ) ;
    ensureResultOK ( "GroovycTest1" ) ;
  }
  public void testGroovycTest1_ForkGroovy_WithGroovyClasspath ( ) {
    ensureNotPresent ( "GroovycTest1" ) ;
    project.executeTarget ( "GroovycTest1_ForkGroovy_WithGroovyClasspath" ) ;
    ensureResultOK ( "GroovycTest1" ) ;

  }
  public void testGroovycTest1_ForkGroovy_WithJavaClasspath ( ) {
    ensureNotPresent ( "GroovycTest1" ) ;
    project.executeTarget ( "GroovycTest1_ForkGroovy_WithJavaClasspath" ) ;
    ensureResultOK ( "GroovycTest1" ) ;
  }
  public void testGroovycTest1_ForkGroovy_WithBothClasspath ( ) {
    ensureNotPresent ( "GroovycTest1" ) ;
    project.executeTarget ( "GroovycTest1_ForkGroovy_WithBothClasspath" ) ;
    ensureResultOK ( "GroovycTest1" ) ;
  }

  public void testGroovycTest1_Joint_NoFork_NoClasspath ( ) {
    ensureNotPresent ( "GroovycTest1" ) ;
    project.executeTarget ( "GroovycTest1_Joint_NoFork_NoClasspath" ) ;
    ensureResultOK ( "GroovycTest1" ) ;
  }
  public void testGroovycTest1_Joint_NoFork_WithGroovyClasspath ( ) {
    ensureNotPresent ( "GroovycTest1" ) ;
    project.executeTarget ( "GroovycTest1_Joint_NoFork_WithGroovyClasspath" ) ;
    ensureResultOK ( "GroovycTest1" ) ;
  }
  public void testGroovycTest1_Joint_NoFork_WithJavaClasspath ( ) {
    ensureNotPresent ( "GroovycTest1" ) ;
    project.executeTarget ( "GroovycTest1_Joint_NoFork_WithJavaClasspath" ) ;
    ensureResultOK ( "GroovycTest1" ) ;
  }
  public void testGroovycTest1_Joint_NoFork_WithBothClasspath ( ) {
    ensureNotPresent ( "GroovycTest1" ) ;
    project.executeTarget ( "GroovycTest1_Joint_NoFork_WithBothClasspath" ) ;
    ensureResultOK ( "GroovycTest1" ) ;
  }

  public void testGroovycTest1_Joint_ForkGroovy_NoClasspath ( ) {
    ensureNotPresent ( "GroovycTest1" ) ;
    project.executeTarget ( "GroovycTest1_Joint_ForkGroovy_NoClasspath" ) ;
    ensureResultOK ( "GroovycTest1" ) ;
  }
  public void testGroovycTest1_Joint_ForkGroovy_WithGroovyClasspath ( ) {
    ensureNotPresent ( "GroovycTest1" ) ;
    project.executeTarget ( "GroovycTest1_Joint_ForkGroovy_WithGroovyClasspath" ) ;
    ensureResultOK ( "GroovycTest1" ) ;

  }
  public void testGroovycTest1_Joint_ForkGroovy_WithJavaClasspath ( ) {
    ensureNotPresent ( "GroovycTest1" ) ;
    project.executeTarget ( "GroovycTest1_Joint_ForkGroovy_WithJavaClasspath" ) ;
    ensureResultOK ( "GroovycTest1" ) ;
  }
  public void testGroovycTest1_Joint_ForkGroovy_WithBothClasspath ( ) {
    ensureNotPresent ( "GroovycTest1" ) ;
    project.executeTarget ( "GroovycTest1_Joint_ForkGroovy_WithBothClasspath" ) ;
    ensureResultOK ( "GroovycTest1" ) ;
  }


    public void testGroovycTest1_ForkGroovy_NoClasspath_WithJavaHome ( ) {
      ensureNotPresent ( "GroovycTest1" ) ;
      project.executeTarget ( "GroovycTest1_ForkGroovy_NoClasspath_WithJavaHome" ) ;
      ensureResultOK ( "GroovycTest1" ) ;
    }
    public void testGroovycTest1_ForkGroovy_WithGroovyClasspath_WithJavaHome ( ) {
      ensureNotPresent ( "GroovycTest1" ) ;
      project.executeTarget ( "GroovycTest1_ForkGroovy_WithGroovyClasspath_WithJavaHome" ) ;
      ensureResultOK ( "GroovycTest1" ) ;

    }
    public void testGroovycTest1_ForkGroovy_WithJavaClasspath_WithJavaHome ( ) {
      ensureNotPresent ( "GroovycTest1" ) ;
      project.executeTarget ( "GroovycTest1_ForkGroovy_WithJavaClasspath_WithJavaHome" ) ;
      ensureResultOK ( "GroovycTest1" ) ;
    }
    public void testGroovycTest1_ForkGroovy_WithBothClasspath_WithJavaHome ( ) {
      ensureNotPresent ( "GroovycTest1" ) ;
      project.executeTarget ( "GroovycTest1_ForkGroovy_WithBothClasspath_WithJavaHome" ) ;
      ensureResultOK ( "GroovycTest1" ) ;
    }


}
