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
package org.codehaus.groovy.benchmarks.vm5;
/*
 The Great Computer Language Shootout
 http://shootout.alioth.debian.org/

 contributed by Java novice Jarkko Miettinen
 modified ~3 lines of the original C#-version
 by Isaac Gouy
 */

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class spectralnorm
{

    private static final NumberFormat formatter = new DecimalFormat("#.000000000");

    public static void main(String[] args) {
        long start = System.currentTimeMillis ();

        int n = 100;
        if (args.length > 0) n = Integer.parseInt(args[0]);

        System.out.println(formatter.format(new spectralnorm().Approximate(n)));

        System.out.println((System.currentTimeMillis () - start) + "ms");
    }

    private final double Approximate(int n) {
        // create unit vector
        double[] u = new double[n];
        for (int i=0; i<n; i++) u[i] =  1;

        // 20 steps of the power method
        double[] v = new double[n];
        for (int i=0; i<n; i++) v[i] = 0;

        for (int i=0; i<10; i++) {
            MultiplyAtAv(n,u,v);
            MultiplyAtAv(n,v,u);
        }

        // B=AtA         A multiplied by A transposed
        // v.Bv /(v.v)   eigenvalue of v
        double vBv = 0, vv = 0;
        for (int i=0; i<n; i++) {
            vBv += u[i]*v[i];
            vv  += v[i]*v[i];
        }

        return Math.sqrt(vBv/vv);
    }


    /* return element i,j of infinite matrix A */
    private final double A(int i, int j){
        return 1.0/((i+j)*(i+j+1)/2 +i+1);
    }

    /* multiply vector v by matrix A */
    private final void MultiplyAv(int n, double[] v, double[] Av){
        for (int i=0; i<n; i++){
            Av[i] = 0;
            for (int j=0; j<n; j++) Av[i] += A(i,j)*v[j];
        }
    }

    /* multiply vector v by matrix A transposed */
    private final void MultiplyAtv(int n, double[] v, double[] Atv){
        for (int i=0;i<n;i++){
            Atv[i] = 0;
            for (int j=0; j<n; j++) Atv[i] += A(j,i)*v[j];
        }
    }

    /* multiply vector v by matrix A and then by matrix A transposed */
    private final void MultiplyAtAv(int n, double[] v, double[] AtAv){
        double[] u = new double[n];
        MultiplyAv(n,v,u);
        MultiplyAtv(n,u,AtAv);
    }
}
