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
package org.codehaus.groovy.runtime;

import groovy.lang.MetaMethod;
import groovy.lang.MetaProperty;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.ClassInfo;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Utility class for MissingMethodException, MissingPropertyException etc.
 * This class contains methods assisting in ranking and listing probable intended
 * methods/fields when a exception is thrown.
 */
public class MethodRankHelper{
    //These are the costs for the various edit operations
    //they are used by the two DamerauLevenshtein implementations
    public static final int DL_SUBSTITUTION = 10;
    public static final int DL_DELETE = 10; //This is also the cost for a insert
    public static final int DL_TRANSPOSITION = 5;
    public static final int DL_CASE = 5;
    
    public static final int MAX_RECOMENDATIONS = 5;
    public static final int MAX_METHOD_SCORE = 50;
    public static final int MAX_CONSTRUCTOR_SCORE = 20;
    public static final int MAX_FIELD_SCORE = 30;
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    private static final class Pair<U,V> {
        private final U u;
        private final V v;
        public Pair(U u, V v){
            this.u = u;
            this.v = v;
        }
    }
    
    /**
     * Returns a string detailing possible solutions to a missing method
     * if no good solutions can be found a empty string is returned.
     *
     * @param methodName the name of the method that doesn't exist
     * @param type the class on which the method is invoked
     * @param arguments the arguments passed to the method
     * @return a string with probable solutions to the exception
     */
    public static String getMethodSuggestionString(String methodName, Class type, Object[] arguments){
        ClassInfo ci = ClassInfo.getClassInfo(type);
        List<MetaMethod> methods = new ArrayList<MetaMethod>(ci.getMetaClass().getMethods());
        methods.addAll(ci.getMetaClass().getMetaMethods());
        List<MetaMethod> sugg = rankMethods(methodName,arguments,methods);
        StringBuilder sb = new StringBuilder();
        if (!sugg.isEmpty()){
            sb.append("\nPossible solutions: ");
            for(int i = 0; i < sugg.size(); i++) {
                if(i != 0) sb.append(", ");
                sb.append(sugg.get(i).getName()).append("(");
                sb.append(listParameterNames(sugg.get(i).getParameterTypes()));
                sb.append(")");
            }
        }
        Class[] argumentClasses = getArgumentClasses(arguments);
        List<Pair<Class,Class>> conflictClasses = getConflictClasses(sugg,argumentClasses);
        if (!conflictClasses.isEmpty()){
            sb.append("\nThe following classes appear as argument class and as parameter class, ");
            sb.append("but are defined by different class loader:\n");
            boolean first = true;
            for(Pair<Class,Class> pair: conflictClasses) {
                if (!first) {
                    sb.append(", ");
                } else {
                    first = false;
                }
                sb.append(pair.u.getName()).append(" (defined by '");
                sb.append(pair.u.getClassLoader());
                sb.append("' and '");
                sb.append(pair.v.getClassLoader());
                sb.append("')");
            }
            sb.append("\nIf one of the method suggestions matches the method you wanted to call, ");
            sb.append("\nthen check your class loader setup.");
        }
        return sb.toString();
    }
    
    private static List<Pair<Class,Class>> getConflictClasses(List<MetaMethod> sugg, Class[] argumentClasses) {
        List<Pair<Class,Class>> ret = new LinkedList<Pair<Class,Class>>();
        Set<Class> recordedClasses = new HashSet<Class>();
        for (MetaMethod method : sugg) {
            Class[] para = method.getNativeParameterTypes();
            for (Class aPara : para) {
                if (recordedClasses.contains(aPara)) continue;
                for (Class argumentClass : argumentClasses) {
                    if (argumentClass == null) continue;
                    if (argumentClass == aPara) continue;
                    if (argumentClass.getName().equals(aPara.getName())) {
                        ret.add(new Pair<Class, Class>(argumentClass, aPara));
                    }
                }
                recordedClasses.add(aPara);
            }
        }
        return ret;
    }

    private static Class[] getArgumentClasses(Object[] arguments) {
        Class[] argumentClasses = new Class[arguments.length];
        for (int i=0; i<argumentClasses.length; i++) {
            Object arg = arguments[i];
            if (arg==null) continue;
            argumentClasses[i] = arg.getClass();
        }
        return argumentClasses;
    }

    /**
     * Returns a string detailing possible solutions to a missing constructor
     * if no good solutions can be found a empty string is returned.
     *
     * @param arguments the arguments passed to the constructor
     * @param type the class on which the constructor is invoked
     * @return a string with probable solutions to the exception
     */
    public static String getConstructorSuggestionString(Class type, Object[] arguments){
        Constructor[] sugg = rankConstructors(arguments, type.getConstructors());
        if(sugg.length >0){
            StringBuilder sb = new StringBuilder();
            sb.append("\nPossible solutions: ");
            for(int i = 0; i < sugg.length; i++){
                if(i != 0) sb.append(", ");
                sb.append(type.getName()).append("(");
                sb.append(listParameterNames(sugg[i].getParameterTypes()));
                sb.append(")");
            }
            return sb.toString();
        } else{
            return "";
        }
    }
    
    /**
     * Returns a string detailing possible solutions to a missing field or property
     * if no good solutions can be found a empty string is returned.
     *
     * @param fieldName the missing field
     * @param type the class on which the field is sought
     * @return a string with probable solutions to the exception
     */
    public static String getPropertySuggestionString(String fieldName, Class type){
        ClassInfo ci = ClassInfo.getClassInfo(type);
        List<MetaProperty>  fi = ci.getMetaClass().getProperties();
        List<RankableField> rf = new ArrayList<RankableField>(fi.size());
        StringBuilder sb = new StringBuilder();
        sb.append("\nPossible solutions: ");
        
        for(MetaProperty mp : fi) rf.add(new RankableField(fieldName, mp));
        Collections.sort(rf);

        int i = 0;
        for (RankableField f : rf) {
            if (i > MAX_RECOMENDATIONS) break;
            if (f.score > MAX_FIELD_SCORE) break;
            if(i > 0) sb.append(", ");
            sb.append(f.f.getName());
            i++;
        }
        return i > 0? sb.toString(): "";
    }
    
    /**
     * creates a comma separated list of each of the class names.
     *
     * @param cachedClasses the array of Classes
     * @return the Class names
     */
    private static String listParameterNames(Class[] cachedClasses){
        StringBuilder sb = new StringBuilder();
      for(int i =0; i < cachedClasses.length;i++){
          if(i != 0) sb.append(", ");
          sb.append(cachedClasses[i].getName());
      }
      return sb.toString();
    }
    
    
    private static String listParameterNames(CachedClass[] cachedClasses){
        StringBuilder sb = new StringBuilder();
        for(int i =0; i < cachedClasses.length;i++){
            if(i != 0) sb.append(", ");
            sb.append(cachedClasses[i].getName());
        }
        return sb.toString();
      }
    
    /**
     * Returns a sorted(ranked) list of a selection of the methods among candidates which
     * most closely resembles original.
     *
     * @param name
     * @param original
     * @param methods
     * @return a sorted lists of Methods
     */
    private static List<MetaMethod> rankMethods(String name, Object[] original, List<MetaMethod> methods) {
        List<RankableMethod> rm = new ArrayList<RankableMethod>(methods.size());
        if (original==null) original = EMPTY_OBJECT_ARRAY;
        Class[] ta = new Class[original.length];
    
        Class nullC =  NullObject.class;
        for(int i = 0; i < original.length; i++){
            //All nulls have to be wrapped so that they can be compared
            ta[i] = original[i] == null?nullC: original[i].getClass();
        }

        for (MetaMethod m:methods) {
            rm.add(new RankableMethod(name, ta, m));
        }
        Collections.sort(rm);
        
        List<MetaMethod> l =  new ArrayList<MetaMethod>(rm.size());
        for (RankableMethod m : rm) {
            if (l.size() > MAX_RECOMENDATIONS) break;
            if (m.score > MAX_METHOD_SCORE) break;
            l.add(m.m);
        }
        return l;
    }

    /**
     * This class wraps a method object and a score variable so methods 
     * Can easily be ranked by their likeness to a another method
     *
     */
    private static final class RankableMethod implements Comparable {
        final MetaMethod m;
        final Integer score;

        public RankableMethod(String name, Class[] argumentTypes, MetaMethod m2) {
            this.m = m2;
            int nameDist = delDistance(name, m2.getName());

            //unbox primitives
            Class[] mArgs = new Class[m2.getParameterTypes().length];
            for(int i =0; i < mArgs.length; i++){
                //All args have to be boxed since argumentTypes is always boxed
                mArgs[i] = boxVar(m2.getParameterTypes()[i].getTheClass());
            }
            int argDist = damerauLevenshteinDistance(argumentTypes,mArgs);
            this.score = nameDist + argDist;
        }

        public int compareTo(Object o) {
            RankableMethod mo = (RankableMethod) o;
            return score.compareTo(mo.score);
        }
    }

    /**
     * Returns a sorted(ranked) list of a selection of the constructors among candidates which
     * most closely resembles original.
     *
     * @param original
     * @param candidates
     * @return a sorted lists of Methods
     */
    private static Constructor[] rankConstructors(Object[] original, Constructor[] candidates) {
        RankableConstructor[] rc = new RankableConstructor[candidates.length];
        Class[] ta = new Class[original.length];

        Class nullC = NullObject.class;
        for (int i = 0; i < original.length; i++) {
            //All nulls have to be wrapped so that they can be compared
            ta[i] = original[i] == null ? nullC : original[i].getClass();
        }

        for (int i = 0; i < candidates.length; i++) {
            rc[i] = new RankableConstructor(ta, candidates[i]);
        }
        Arrays.sort(rc);
        List<Constructor> l = new ArrayList<Constructor>();
        int index = 0;
        while (l.size() < MAX_RECOMENDATIONS && index < rc.length && rc[index].score < MAX_CONSTRUCTOR_SCORE) {
            l.add(rc[index].c);
            index++;
        }
        return l.toArray(new Constructor[0]);
    }

    /**
     * This class wraps a method object and a score variable so methods 
     * Can easily be ranked by their likeness to a another method
     *
     */
    private static final class RankableConstructor implements Comparable {
        final Constructor c;
        final Integer score;

        public RankableConstructor(Class[] argumentTypes, Constructor c) {
            this.c = c;
            //unbox primitives
            Class[] cArgs = new Class[c.getParameterTypes().length];
            for(int i =0; i < cArgs.length; i++){
                //All args have to be boxed since argumentTypes is always boxed
                cArgs[i] = boxVar(c.getParameterTypes()[i]);
            }

            this.score = damerauLevenshteinDistance(argumentTypes,cArgs);
        }

        public int compareTo(Object o) {
            RankableConstructor co = (RankableConstructor) o;
            return score.compareTo(co.score);
        }
    }
    
    /**
     * This class wraps a method object and a score variable so methods 
     * Can easily be ranked by their likeness to a another method
     *
     */
    private static final class RankableField implements Comparable {
        final MetaProperty f;
        final Integer score;

        public RankableField(String name, MetaProperty mp) {
            this.f = mp;
            this.score = delDistance(name,mp.getName());
        }

        public int compareTo(Object o) {
            RankableField co = (RankableField) o;
            return score.compareTo(co.score);
        }
    }
    
    /**
     * If c is a primitive class this method returns a boxed version
     * otherwise c is returned.
     * In java 1.5 this can be simplified thanks to the Type class.
     * @param c
     * @return a boxed version of c if c can be boxed, else c
     */
    protected static Class boxVar(Class c){
        if(Boolean.TYPE.equals(c)){
            return Boolean.class;
        }else if(Character.TYPE.equals(c)){
            return Character.class;
        }else if(Byte.TYPE.equals(c)){
            return Byte.class;
        }else if(Double.TYPE.equals(c)){
            return Double.class;
        }else if(Float.TYPE.equals(c)){
            return Float.class;
        }else if(Integer.TYPE.equals(c)){
            return Integer.class;
        }else if(Long.TYPE.equals(c)){
            return Long.class;
        }else if(Short.TYPE.equals(c)){
            return Short.class;
        }else{
            return c;
        }
    }
    
    /**
     * This is a small wrapper for nulls
     */
    private static class NullObject{
    }

    /**
     * This is a slightly modified version of the Damerau Levenshtein distance
     * algorithm. It has a additional test to see if a character has switched case,
     * in the original algorithm this counts as a substitution.
     * The "cost" for a substitution is given as 10 instead of 1 in this version,
     * this enables transpositions and case modifications to have a lower cost than
     * substitutions.
     *
     * Currently the lowercase versions of t_j and s_i isn't cached, its probable
     * that some speed could be gained from this.
     * 
     * This version is based on Chas Emerick's implementation of Levenshtein Distance
     * for jakarta commons.
     * @param s a CharSequence
     * @param t the CharSequence to be compared to s
     * @return a value representing the edit distance between s and t
     */
    public static int delDistance(CharSequence s, CharSequence t) {
        if (s == null || t == null) {
            throw new IllegalArgumentException("Strings must not be null");
        }

        int n = s.length(); // length of s
        int m = t.length(); // length of t

        if (n == 0) {
            return m;
        } else if (m == 0) {
            return n;
        }

        //we have to keep 3 rows instead of the 2 used in Levenshtein
        int[][] vals = new int[3][n + 1];


        int[] _d; //placeholder to assist in rotating vals

        // indexes into strings s and t
        int i; // iterates through s
        int j; // iterates through t

        char t_j; // jth character of t
        char s_i; // ith character of s
        int cost; // cost

        for (i = 0; i <= n; i++) {
            vals[1][i] = i * DL_DELETE;
        }

        for (j = 1; j <= m; j++) {
            t_j = t.charAt(j - 1);
            vals[0][0] = j * DL_DELETE;

            for (i = 1; i <= n; i++) {
                s_i = s.charAt(i - 1);
                if (Character.isLowerCase(s_i) ^ Character.isLowerCase(t_j)) {
                    //if s_i and t_i don't have have the same case
                    cost = caselessCompare(s_i, t_j) ? DL_CASE : DL_SUBSTITUTION;
                } else {
                    //if they share case check for substitution
                    cost = s_i == t_j ? 0 : DL_SUBSTITUTION;
                }

                // minimum of cell to the left+1, to the top+1, diagonally left and up +cost
                vals[0][i] = Math.min(Math.min(vals[0][i - 1] + DL_DELETE, vals[1][i] + DL_DELETE), vals[1][i - 1] + cost);

                //Check for transposition, somewhat more complex now since we have to check for case
                if (i > 1 && j > 1) {
                    cost = Character.isLowerCase(s_i) ^ Character.isLowerCase(t.charAt(j - 2)) ? DL_CASE : 0;
                    cost = Character.isLowerCase(s.charAt(i - 2)) ^ Character.isLowerCase(t_j) ? cost + DL_CASE : cost;

                    if (caselessCompare(s_i, t.charAt(j - 2)) && caselessCompare(s.charAt(i - 2), t_j)) {
                        vals[0][i] = Math.min(vals[0][i], vals[2][i - 2] + DL_TRANSPOSITION + cost);
                    }
                }
            }

            // rotate all value arrays upwards(older rows get a higher index)
            _d = vals[2];
            vals[2] = vals[1];
            vals[1] = vals[0];
            vals[0] = _d;
        }

        // our last action in the above loop was to rotate vals, so vals[1] now
        // actually has the most recent cost counts
        return vals[1][n];
    }

    /**
     * Compares two characters whilst ignoring case.
     * @param a the first character
     * @param b the second character
     * @return true if the characters are equal
     */
    private static boolean caselessCompare(char a, char b){
        return Character.toLowerCase(a) == Character.toLowerCase(b);
    }
    
    /**
     * This is a implementation of DL distance between two Object arrays instead
     * of character streams. The objects are compared using their equals method.
     * No objects may be null.
     * This implementation is based on Chas Emerick's implementation of Levenshtein Distance
     * for jakarta commons.
     * @param s an Object array
     * @param t this array is compared to s
     * @return the edit distance between the two arrays
     */
    public static int damerauLevenshteinDistance(Object[] s, Object[] t) {
        if (s == null || t == null) {
            throw new IllegalArgumentException("Arrays must not be null");
        }

        int n = s.length; // length of s
        int m = t.length; // length of t

        if (n == 0) {
            return m;
        } else if (m == 0) {
            return n;
        }

        int[][] vals = new int[3][n + 1];


        int[] _d; //placeholder to assist in rotating vals

        // indexes into arrays s and t
        int i; // iterates through s
        int j; // iterates through t

        Object t_j; // jth object of t

        int cost; // cost

        for (i = 0; i <= n; i++) {
            vals[1][i] = i * DL_DELETE ;
        }

        for (j = 1; j <= m; j++) {
            t_j = t[j - 1];
            vals[0][0] = j * DL_DELETE ;

            for (i = 1; i <= n; i++) {
                cost = s[i - 1].equals(t_j)? 0 : DL_SUBSTITUTION;
                // minimum of cell to the left+1, to the top+1, diagonally left and up +cost
                vals[0][i] = Math.min(Math.min(vals[0][i - 1] + DL_DELETE, vals[1][i] + DL_DELETE), vals[1][i - 1] + cost);

                //Check for transposition
                if(i > 1 && j > 1 && s[i -1].equals(t[j -2]) && s[i- 2].equals(t_j)){
                    vals[0][i] = Math.min(vals[0][i], vals[2][i-2] + DL_TRANSPOSITION);
                }
            }

            // rotate all value arrays upwards(older rows get a higher index)
            _d = vals[2];
            vals[2] = vals[1];
            vals[1] = vals[0];
            vals[0] = _d;
        }

        return vals[1][n];
    }
}
