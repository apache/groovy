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
import java.net.URI;

/**
 * User: Danno.Ferrin
 * Date: Sep 30, 2008
 * Time: 9:36:46 PM
 */
public class Grape {

    private static boolean enableGrapes = Boolean.valueOf(System.getProperties().getProperty("groovy.grape.enable", "true"));
    private static boolean enableAutoDownload = Boolean.valueOf(System.getProperties().getProperty("groovy.grape.autoDownload", "true"));
    protected static GrapeEngine instance;

    /**
     * This is a static access kill-switch.  All of the static shortcut
     * methods in this class will not work if this property is set to false.
     * <br />
     * By default it is set to true.
     */
    public static boolean getEnableGrapes() {
        return enableGrapes;
    }

    /**
     * This is a static access kill-switch.  All of the static shortcut
     * methods in this class will not work if this property is set to false.
     * <br />
     * By default it is set to true.
     */
    public static void setEnableGrapes(boolean enableGrapes) {
        Grape.enableGrapes = enableGrapes;
    }

    /**
     * This is a static access auto download enabler.  It will set the
     * 'autoDownload' value to the passed in arguments map if not already
     * set.  If 'autoDownlowd' is set the value will not be adjusted.
     *
     * This applies to the grab and resolve calls.
     *
     * If it is set to false, only previously downloaded grapes
     * will be used.  This may cause failure in the grape call
     * if the library has not yet been downloaded
     *
     * Ifit is set to true, then any jars not already downlaoded will
     * automaticllay be downloaded.  Also, any versions expressed as a range
     * will be checked for new versions and downloaded (with dependenceis)
     * if found.
     *
     * <br />
     * By default it is set to false.
     */
    public static boolean getEnableAutoDownload() {
        return enableAutoDownload;
    }

    /**
     * This is a static access auto download enabler.  It will set the
     * 'autoDownload' value to the passed in arguments map if not already
     * set.  If 'autoDownload' is set the value will not be adjusted
     *
     * This applies to the grab and resolve calls.
     *
     * If it is set to false, only previously downloaded grapes
     * will be used.  This may cause failure in the grape call
     * if the library has not yet been downloaded
     *
     * Ifit is set to true, then any jars not already downlaoded will
     * automaticllay be downloaded.  Also, any versions expressed as a range
     * will be checked for new versions and downloaded (with dependenceis)
     * if found.
     *
     * <br />
     * By default it is set to false.
     */
    public static void setEnableAutoDownload(boolean enableAutoDownload) {
        Grape.enableAutoDownload = enableAutoDownload;
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
        }
        return instance;
    }

    public static void grab(String endorsed) {
        if (enableGrapes) {
            GrapeEngine instance = getInstance();
            if (instance != null) {
                instance.grab(endorsed);
            }
        }
    }

    public static void grab(Map dependency) {
        if (enableGrapes) {
            GrapeEngine instance = getInstance();
            if (instance != null) {
                if (!dependency.containsKey("autoDownload")) {
                    dependency.put("autoDownload" , Boolean.valueOf(enableAutoDownload));
                }
                instance.grab(dependency);
            }
        }
    }

    public static void grab(Map args, Map... dependencies) {
        if (enableGrapes) {
            GrapeEngine instance = getInstance();
            if (instance != null) {
                if (!args.containsKey("autoDoanload")) {
                    args.put("autoDownload" , Boolean.valueOf(enableAutoDownload));
                }
                instance.grab(args, dependencies);
            }
        }
    }

    public static Map<String, Map<String, List<String>>> enumerateGrapes() {
        Map<String, Map<String, List<String>>> grapes = null;
        if (enableGrapes) {
            GrapeEngine instance = getInstance();
            if (instance != null) {
                grapes = instance.enumerateGrapes();
            }
        }
        if (grapes == null) {
            return Collections.emptyMap();
        } else {
            return grapes;
        }
    }

    public static URI[] resolve(Map args, Map... dependencies) {
        URI[] uris = null;
        if (enableGrapes) {
            GrapeEngine instance = getInstance();
            if (instance != null) {
                if (!args.containsKey("autoDownload")) {
                    args.put("autoDownload" , Boolean.valueOf(enableAutoDownload));
                }
                uris = instance.resolve(args, dependencies);
            }
        }
        if (uris == null) {
            return new URI[0];
        } else {
            return uris;
        }

    }

    public static Map[] listDependencies(ClassLoader cl) {
        Map[] maps = null;
        if (enableGrapes) {
            GrapeEngine instance = getInstance();
            if (instance != null) {
                maps = instance.listDependencies(cl);
            }
        }
        if (maps == null) {
            return new Map[0];
        } else {
            return maps;
        }

    }
}

