package org.codehaus.groovy.sandbox.ui;

import java.io.IOException;

/**
 * Factory to build a command line prompt.  Should build the most featureful
 * prompt available.
 * 
 * Currently readline prompt will be looked up dynamically, and defaults to 
 * normal System.in prompt.
 */
public class PromptFactory {
  public static Prompt buildPrompt() throws IOException {
    try {
      return (Prompt) Class.forName("org.codehaus.groovy.sandbox.ui.ReadlinePrompt").newInstance();
    } catch (ClassNotFoundException e) {
      // nothing
      return new JavaPrompt();
    } catch (Exception e) {
      e.printStackTrace();
      return new JavaPrompt();
    }
  }
}
