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
// translated from http://www.ffconsultancy.com/languages/ray_tracer/code/1/ray.java
// by Derek Young -  Sep 6, 07

class Vec {
    public double x, y, z;
    public Vec(double x2, double y2, double z2) { x=x2; y=y2; z=z2; }

    static Vec add(Vec a, Vec b) { return new Vec(a.x+b.x, a.y+b.y, a.z+b.z); }
    static Vec sub(Vec a, Vec b) { return new Vec(a.x-b.x, a.y-b.y, a.z-b.z); }
    static Vec scale(double s, Vec a) { return new Vec(s*a.x, s*a.y, s*a.z); }

    static double dot(Vec a, Vec b) { return a.x*b.x + a.y*b.y + a.z*b.z; }

    static Vec unitise(Vec a) { return scale(1 / Math.sqrt(Vec.dot(a, a)), a); }


    }


    class Ray {
    public Vec orig, dir;
    public Ray(Vec o, Vec d) { orig=o; dir=d; }
    }

    class Hit {
    public double lambda;
    public Vec normal;
     public Hit(double l, Vec n) { lambda=l; normal=n; }
    }

    abstract class Scene {
    abstract public Hit intersect(Hit i, Ray ray);
    }

    class Sphere extends Scene {
        static double infinity=Float.POSITIVE_INFINITY;
    public Vec center;
    public double radius;

    public Sphere(Vec c, double r) { center=c; radius=r; }

    public double ray_sphere(Ray ray) {
        Vec v = Vec.sub(center, ray.orig);
        double b = Vec.dot(v, ray.dir),
        disc = b*b - Vec.dot(v, v) + radius*radius;
        if (disc < 0) return infinity;
        double d = Math.sqrt(disc), t2 = b+d;
        if (t2 < 0) return infinity;
        double t1 = b-d;
        return (t1 > 0 ? t1 : t2);
    }

    public Hit intersect(Hit i, Ray ray) {
        double l = ray_sphere(ray);
        if (l >= i.lambda) return i;
        Vec n = Vec.add(ray.orig, Vec.sub(Vec.scale(l, ray.dir), center));
        return new Hit(l, Vec.unitise(n));
    }
    }

    class Group extends Scene {
    public Sphere bound;
    public ArrayList objs;

    public Group(Sphere b) {
        bound = b;
        objs = new ArrayList();
    }

    public Hit intersect(Hit i, Ray ray) {
        double l = bound.ray_sphere(ray);
        if (l >= i.lambda) return i;
        ListIterator it = objs.listIterator(0);
        while (it.hasNext()) {
        Scene scene = (Scene)it.next();
        i = scene.intersect(i, ray);
        }
        return i;
    }
    }

class rayMain {
    double delta=Math.sqrt(Math.ulp(1.0d)), infinity=Float.POSITIVE_INFINITY;


    double ray_trace(Vec light, Ray ray, Scene scene) {
    Hit i = scene.intersect(new Hit(infinity, new Vec(0, 0, 0)), ray);
    if (i.lambda == infinity) return 0;
    Vec o = Vec.add(ray.orig, Vec.add(Vec.scale(i.lambda, ray.dir),
                  Vec.scale(delta, i.normal)));
    double g = Vec.dot(i.normal, light);
    if (g >= 0) return 0.0d;
    Ray sray = new Ray(o, Vec.scale(-1, light));
    Hit si = scene.intersect(new Hit(infinity, new Vec(0, 0, 0)), sray);
    return (si.lambda == infinity ? -g : 0);
    }

    Scene create(int level, Vec c, double r) {
    Sphere sphere = new Sphere(c, r);
    if (level == 1) return sphere;
    Group group = new Group(new Sphere(c, 3*r));
    group.objs.add(sphere);
    double rn = 3*r/Math.sqrt(12);
        for (int dz in [-1, 1]) {
      for (int dx in [-1, 1]) {
          //for (int dx=-1; dx<=1; dx+=2) {
        Vec c2 = new Vec(c.x+dx*rn, c.y+rn, c.z+dz*rn);
        group.objs.add(create(level-1, c2, r/2));
        }
    }
    return group;
    }

    void run(int n, int level, int ss) {
    Scene scene = create(level, new Vec(0, -1, 0), 1);
    FileOutputStream out = new FileOutputStream("groovyimage.pgm");
    out.write(("P5\n"+n+" "+n+"\n255\n").getBytes());
        for (int yi in 0..(n-1)) {
          int y = (n-1) - yi
          for (int x in 0..(n - 1)) {
        //print('.');
        double g=0;
                for (int dx in 0..(ss - 1)) {
                    for (int dy in 0..(ss - 1)) {
            Vec d = new Vec(x+dx*1.0d/ss-n/2.0d, y+dy*1.0d/ss-n/2.0d, n);
            Ray ray = new Ray(new Vec(0, 0, -4), Vec.unitise(d));
            g += ray_trace(Vec.unitise(new Vec(-1, -3, 2)),
                       ray, scene);
            }
                }
                out.write((int)(0.5d+255.0d*g/(ss*ss)));
        }
        }
    out.close();
    }

}

(new rayMain()).run(Integer.parseInt(args[1]),
        Integer.parseInt(args[0]), 4);

