package org.codehaus.groovy.binding;

import groovy.lang.Closure;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * I decided against using org.jdesktop.animation.timing.interpolation.Evaluator<T>.create because
 * (a) it is package access, not public (hard stop)
 * (b) I could pre-calc the bounds and keep them in the closure, a classic memory over time optimization.
 *
 * I also use straight Java for these, even 5% in a tight loop matters.
 * But we will exploit multiple dispatch!, i.e. this works for each type...
 * <code>
 * import java.awt.Color
 * import java.awt.Dimension
 * import java.awt.Point
 * import java.awt.Rectangle
 * import java.awt.geom.Point2D.Double as P2DDouble
 * import java.awt.geom.Point2D.Float as P2DFloat
 * import java.awt.geom.Rectangle2D.Double as R2DDouble
 * import java.awt.geom.Rectangle2D.Float as R2DFloat
 * import org.codehaus.groovy.binding.TimingFrameworkInterpolators as interp
 *
 * vals = [ [java.awt.Color.GREEN,new java.awt.Color(255,0,255)],
 *          [new P2DDouble(3,40),new P2DDouble(30,4)],
 *          [new P2DFloat(3,40),new P2DFloat(30,4)],
 *          [new Point(3,40),new Point(30,4)],
 *          [new Dimension(3,40), new Dimension(30,4)],
 *          [new R2DDouble(3,40,5,60), new R2DDouble(30,4,50,6)],
 *          [new R2DFloat(3,40,5,60), new R2DFloat(30,4,50,6)],
 *          [new Rectangle(3,40,5,60), new Rectangle(30,4,50,6)]
 * ]
 *
 * vals.each {println interp.getInterpolator(it[0], it[1])(0.5)}
 * </code>
 */
public class TimingFrameworkInterpolators {

    public static Closure getInterpolator(Color c1, Color c2) {
        return new ColorInterpolator(c1, c2);
    }

    static class ColorInterpolator extends Closure {
        int rb, rr;
        int gb, gr;
        int bb, br;
        int ab, ar;

        public ColorInterpolator(Color c1, Color c2) {
            super(null);
            setResolveStrategy(TO_SELF);
            rb = c1.getRed(); rr=c2.getRed()-c1.getRed();
            gb=c1.getGreen(); gr=c2.getGreen()-c1.getGreen();
            bb=c1.getBlue(); br=c2.getBlue()-c1.getBlue();
            ab=c1.getAlpha(); ar=c2.getAlpha()-c1.getAlpha();
        }

        public Object doCall(Object val) {
            float floatVal = ((Number)val).floatValue();
            return new Color((int)(rb + rr*floatVal + 0.5),
                    (int)(gb + gr*floatVal + 0.5),
                    (int)(bb + br*floatVal + 0.5),
                    (int)(ab + ar*floatVal + 0.5));
        }
    }

    public static Closure getInterpolator(Point2D.Double p1, Point2D.Double p2) {
        return new PointDoubleInterpolator(p1, p2);
    }

    static class PointDoubleInterpolator extends Closure {
        double xb, xr;
        double yb, yr;

        public PointDoubleInterpolator(Point2D.Double p1, Point2D.Double p2) {
            super(null);
            setResolveStrategy(TO_SELF);
            xb = p1.x; xr = p2.x - p1.x;
            yb = p1.y; yr = p2.y - p1.y;
        }

        public Object doCall(Object val) {
            double doubleVal = ((Number)val).doubleValue();
            return new Point2D.Double(xb + xr*doubleVal, yb + yr*doubleVal);
        }
    }

    public static Closure getInterpolator(Point2D.Float p1, Point2D.Float p2) {
        return new PointFloatInterpolator(p1, p2);
    }

    static class PointFloatInterpolator extends Closure {
        float xb, xr;
        float yb, yr;

        public PointFloatInterpolator(Point2D.Float p1, Point2D.Float p2) {
            super(null);
            setResolveStrategy(TO_SELF);
            xb = p1.x; xr = p2.x - p1.x;
            yb = p1.y; yr = p2.y - p1.y;
        }

        public Object doCall(Object val) {
            float floatVal = ((Number)val).floatValue();
            return new Point2D.Float(xb + xr*floatVal, yb + yr*floatVal);
        }
    }

    public static Closure getInterpolator(Point p1, Point p2) {
        return new PointInterpolator(p1, p2);
    }

    static class PointInterpolator extends Closure {
        float xb, xr; // keep as float to recude conversions?
        float yb, yr; // keep as float to recude conversions?

        public PointInterpolator(Point p1, Point p2) {
            super(null);
            setResolveStrategy(TO_SELF);
            xb = p1.x; xr = p2.x - p1.x;
            yb = p1.y; yr = p2.y - p1.y;
        }

        public Object doCall(Object val) {
            float floatVal = ((Number)val).floatValue();
            return new Point((int)(xb + xr*floatVal + 0.5f),
                (int)(yb + yr*floatVal + 0.5f));
        }
    }

    public static Closure getInterpolator(Line2D.Double l1, Line2D.Double l2) {
        return new LineDoubleInterpolator(l1, l2);
    }

    static class LineDoubleInterpolator extends Closure {
        double x1b, x1r;
        double y1b, y1r;
        double x2b, x2r;
        double y2b, y2r;

        public LineDoubleInterpolator(Line2D.Double l1, Line2D.Double l2) {
            super(null);
            setResolveStrategy(TO_SELF);
            x1b = l1.x1; x1r = l2.x1 - l1.x1;
            y1b = l1.y1; y1r = l2.y1 - l1.y1;
            x2b = l1.x2; x2r = l2.x2 - l1.x2;
            y2b = l1.y2; y2r = l2.y2 - l1.y2;
        }

        public Object doCall(Object val) {
            double doubleVal = ((Number)val).doubleValue();
            return new Line2D.Double(x1b + x1r*doubleVal,
                    y1b + y1r*doubleVal,
                    x2b + x2r*doubleVal,
                    y2b + y2r*doubleVal);
        }
    }

    public static Closure getInterpolator(Line2D.Float l1, Line2D.Float l2) {
        return new LineFloatInterpolator(l1, l2);
    }

    static class LineFloatInterpolator extends Closure {
        float x1b, x1r;
        float y1b, y1r;
        float x2b, x2r;
        float y2b, y2r;

        public LineFloatInterpolator(Line2D.Float l1, Line2D.Float l2) {
            super(null);
            setResolveStrategy(TO_SELF);
            x1b = l1.x1; x1r = l2.x1 - l1.x1;
            y1b = l1.y1; y1r = l2.y1 - l1.y1;
            x2b = l1.x2; x2r = l2.x2 - l1.x2;
            y2b = l1.y2; y2r = l2.y2 - l1.y2;
        }

        public Object doCall(Object val) {
            float floatVal = ((Number)val).floatValue();
            return new Line2D.Float(x1b + x1r*floatVal,
                    y1b + y1r*floatVal,
                    x2b + x2r*floatVal,
                    y2b + y2r*floatVal);
        }
    }

    public static Closure getInterpolator(Dimension d1, Dimension d2) {
        return new DimensionInterpolator(d1, d2);
    }

    static class DimensionInterpolator extends Closure {
        int wb, wr;
        int hb, hr;

        public DimensionInterpolator(Dimension d1, Dimension d2) {
            super(null);
            setResolveStrategy(TO_SELF);
            wb = d1.width; wr = d2.width - d1.width;
            hb = d1.height; hr = d2.height - d1.height;
        }

        public Object doCall(Object val) {
            float floatVal = ((Number)val).floatValue();
            return new Dimension((int)(wb + wr*floatVal + 0.5),
                    (int)(hb + hr*floatVal + 0.5));
        }
    }
    //TODO any Dimension2D derivities?

    public static Closure getInterpolator(Rectangle2D.Double l1, Rectangle2D.Double l2) {
        return new RectangleDoubleInterpolator(l1, l2);
    }

    static class RectangleDoubleInterpolator extends Closure {
        double xb, xr;
        double yb, yr;
        double wb, wr;
        double hb, hr;

        public RectangleDoubleInterpolator(Rectangle2D.Double l1, Rectangle2D.Double l2) {
            super(null);
            setResolveStrategy(TO_SELF);
            xb = l1.x; xr = l2.x - l1.x;
            yb = l1.y; yr = l2.y - l1.y;
            wb = l1.width; wr = l2.width - l1.width;
            hb = l1.height; wr = l2.height - l1.height;
        }

        public Object doCall(Object val) {
            double doubleVal = ((Number)val).doubleValue();
            return new Rectangle2D.Double(xb + xr*doubleVal,
                    yb + yr*doubleVal,
                    wb + wr*doubleVal,
                    hb + hr*doubleVal);
        }
    }

    public static Closure getInterpolator(Rectangle2D.Float l1, Rectangle2D.Float l2) {
        return new RectangleFloatInterpolator(l1, l2);
    }

    static class RectangleFloatInterpolator extends Closure {
        float xb, xr;
        float yb, yr;
        float wb, wr;
        float hb, hr;

        public RectangleFloatInterpolator(Rectangle2D.Float l1, Rectangle2D.Float l2) {
            super(null);
            setResolveStrategy(TO_SELF);
            xb = l1.x; xr = l2.x - l1.x;
            yb = l1.y; yr = l2.y - l1.y;
            wb = l1.width; wr = l2.width - l1.width;
            hb = l1.height; wr = l2.height - l1.height;
        }

        public Object doCall(Object val) {
            float floatVal = ((Number)val).floatValue();
            return new Rectangle2D.Float(xb + xr*floatVal,
                    yb + yr*floatVal,
                    wb + wr*floatVal,
                    hb + hr*floatVal);
        }
    }

    public static Closure getInterpolator(Rectangle l1, Rectangle l2) {
        return new RectangleInterpolator(l1, l2);
    }

    static class RectangleInterpolator extends Closure {
        float xb, xr;
        float yb, yr;
        float wb, wr;
        float hb, hr;

        public RectangleInterpolator(Rectangle l1, Rectangle l2) {
            super(null);
            setResolveStrategy(TO_SELF);
            xb = l1.x; xr = l2.x - l1.x;
            yb = l1.y; yr = l2.y - l1.y;
            wb = l1.width; wr = l2.width - l1.width;
            hb = l1.height; wr = l2.height - l1.height;
        }

        public Object doCall(Object val) {
            float floatVal = ((Number)val).floatValue();
            return new Rectangle((int)(xb + xr*floatVal + 0.5f),
                    (int)(yb + yr*floatVal + 0.5f),
                    (int)(wb + wr*floatVal + 0.5f),
                    (int)(hb + hr*floatVal + 0.5f));
        }
    }


    //TODO? RoundRectangle2D
    //TODO? Ellipse2D
    //TODO? Arc2D
    //TODO? QuadCurve2D
    //TODO? CubicCurve2D
    //TODO allow interpolators to be registered?
}
