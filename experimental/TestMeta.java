import java.util.ArrayList;
import java.util.HashMap;

import org.codehaus.groovy.runtime.MetaClassActions;
import org.codehaus.groovy.runtime.MetaClassActionsGenerator;

/*
 * Copyright 2005 John G. Wilson
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
 *
 */

/**
 * @author John Wilson
 *
 */

public class TestMeta {
  
  /**
   * @param args
   */
  public static void main(String[] args) throws Exception {   
    final MetaClassActions m1 = MetaClassActionsGenerator.getActions(ArrayList.class);
    final MetaClassActions m2 = MetaClassActionsGenerator.getActions(HashMap.class);
  }
}