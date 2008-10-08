/*
 * Copyright 2008 the original author or authors.
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
package groovy.grape;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * User: Danno.Ferrin
 * Date: Sep 30, 2008
 * Time: 9:36:46 PM
 */
public class Grape {

    protected static boolean enableGrapes;
    protected static GrapeEngine instance;

    public static void initGrape() {
        enableGrapes = getInstance() != null;
    }

    public static synchronized GrapeEngine getInstance() {
        if (instance == null) {
            try {
                // by default use GrapeIvy
                //TODO META-INF/services resolver?
                instance = (GrapeEngine) Class.forName("groovy.grape.GrapeIvy").newInstance();
            } catch (InstantiationException e) {
                //LOGME
            } catch (IllegalAccessException e) {
                //LOGME
            } catch (ClassNotFoundException e) {
                //LOGME
            }
            if (instance == null) {
                // failed somehow, create a
                instance = new GrapeEngine() {
                    public Object grab(String endorsedModule) { return null; }
                    public Object grab(Map args) { return null; }
                    public Object grab(Map args, Map... dependencies) { return null; }
                    public Map<String, Map<String, List<String>>> enumerateGrapes() { return Collections.emptyMap(); }
                };
            }
        }
        return instance;
    }

    public static void grab(String endorsed) {
        if (enableGrapes) getInstance().grab(endorsed);
    }

    public static void grab(Map dependency) {
        if (enableGrapes) getInstance().grab(dependency);
    }

    public static void grab(Map args, Map... dependencies) {
        if (enableGrapes) getInstance().grab(args, dependencies);
    }

    public static Map<String, Map<String, List<String>>> enumerateGrapes() {
        if (enableGrapes)
            return getInstance().enumerateGrapes();
        else
            return Collections.emptyMap();
    }
}
