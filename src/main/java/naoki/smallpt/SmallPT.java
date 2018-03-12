/*
 Copyright (c) 2017 Naoki Kishida (naokikishida@gmail.com / twitter: @kis)
This software is released under the MIT License.
( https://github.com/kishida/smallpt4j/blob/master/LICENSE.txt )

This is based on the smallpt( http://www.kevinbeason.com/smallpt/ )
that is released under the MIT License.
( https://github.com/kishida/smallpt4j/blob/master/smallpt_LICENSE.txt )
*/

package naoki.smallpt;

import static naoki.smallpt.SmallPT.Reflection.DIFFUSE;
import static org.apache.commons.math3.util.FastMath.abs;
import static org.apache.commons.math3.util.FastMath.asin;
import static org.apache.commons.math3.util.FastMath.atan;
import static org.apache.commons.math3.util.FastMath.atan2;
import static org.apache.commons.math3.util.FastMath.cos;
import static org.apache.commons.math3.util.FastMath.floor;
import static org.apache.commons.math3.util.FastMath.log;
import static org.apache.commons.math3.util.FastMath.max;
import static org.apache.commons.math3.util.FastMath.min;
import static org.apache.commons.math3.util.FastMath.pow;
import static org.apache.commons.math3.util.FastMath.sin;
import static org.apache.commons.math3.util.FastMath.sqrt;
import static org.apache.commons.math3.util.FastMath.tan;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

public class SmallPT {

    private static final int SAMPLES_DEFAULT = 40;

    private static final double GAMMA = 2.2;
    private static final double RECIP_GAMMA = 1 / GAMMA;
    private static final double EPS = 1e-4;
    private static final double INF = 1e20;

    static final class Vec {        // Usage: time ./smallpt 5000  xv image.ppm
        static final Vec UNIT_X = new Vec(1, 0, 0);
        static final Vec UNIT_Y = new Vec(0, 1, 0);
        static final Vec ZERO = new Vec(0, 0, 0);

        double x, y, z;                  // position, also color (r,g,b)

        public Vec(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        Vec add(Vec b) {
            return new Vec(x + b.x, y + b.y, z + b.z);
        }

        Vec sub(Vec b) {
            return new Vec(x - b.x, y - b.y, z - b.z);
        }

        Vec mul(double b) {
            return new Vec(x * b, y * b, z * b);
        }

        Vec vecmul(Vec b) {
            return new Vec(x * b.x, y * b.y, z * b.z);
        }

        Vec normalize() {
            double dist = distant();
            if (dist == 0) {
                return UNIT_X;
            }
            x /= dist;
            y /= dist;
            z /= dist;
            return this;
        }
        double distant() {
            return sqrt(x * x + y * y + z * z);
        }

        double dot(Vec b) {
            return x * b.x + y * b.y + z * b.z;
        } // cross:

        Vec mod(Vec b) {
            return new Vec(y * b.z - z * b.y, z * b.x - x * b.z, x * b.y - y * b.x);
        }
    }

    static final class Ray {

        final Vec obj, dist;

        public Ray(Vec o, Vec d) {
            this.obj = o;
            this.dist = d;
        }

    }

    static enum Reflection {
        DIFFUSE, SPECULAR, REFRECTION, GLOSSY20, GLOSSY50, GLOSSY80
    }  // material types, used in radiance()

    static abstract class Surface {
        final Vec pos;
        final Texture texture;

        public Surface(Vec pos, Texture texture) {
            this.pos = pos;
            this.texture = texture;
        }

        public Surface(Vec pos, Vec emission, Vec color, Reflection reflection) {
            this(pos, new SolidTexture(emission, color, reflection));
        }
        
        abstract double intersect(Ray y, Surface[] robj);
        abstract void position(Vec p, Ray r, Vec[] n, Col[] c);
        abstract Point makeXY(Vec p);
    }
    
    static final class Sphere extends Surface {

        final double rad;       // radius

        public Sphere(double rad, Vec p, Vec e, Vec c, Reflection refl) {
            super(p, e, c, refl);
            this.rad = rad;
        }
        public Sphere(double rad, Vec p, Texture texture) {
            super(p, texture);
            this.rad = rad;
        }

        @Override
        double intersect(Ray r, Surface[] robj) { // returns distance, 0 if nohit
            Vec op = pos.sub(r.obj); // Solve t^2*d.d + 2*t*(o-p).d + (o-p).(o-p)-R^2 = 0
            double t,
                    b = op.dot(r.dist),
                    det = b * b - op.dot(op) + rad * rad;
            if (det < 0) {
                return 0;
            }
            det = sqrt(det);
            robj[0] = this;
            return (t = b - det) > EPS ? t : ((t = b + det) > EPS ? t : 0);
        }
        
        @Override
        void position(Vec x, Ray r, Vec[] n, Col[] c) {
            n[0] = x.sub(pos).normalize();
            c[0] = texture.getCol(this, x);
        }

        @Override
        Point makeXY(Vec x) {
            Vec position = x.sub(pos).mul(1 / rad);
            double phi = atan2(position.z, position.x);
            double theta = asin(position.y);
            return new Point(1 - (phi + Math.PI) / (2 * Math.PI), (theta + Math.PI / 2) / Math.PI);
        }
        
    }
    
    static final class Plane extends Surface {
        final double width, height;
        final boolean xy;
        public Plane(double x, double y, boolean xy, Vec pos, Vec emission, Vec color, Reflection reflection) {
            super(pos, emission, color, reflection);
            this.width = x;
            this.height = y;
            this.xy = xy;
        }
        public Plane(double x, double y, boolean xy, Vec pos, Texture tex) {
            super(pos, tex);
            this.width = x;
            this.height = y;
            this.xy = xy;
        }
        @Override
        double intersect(Ray ray, Surface[] robj) {
            double dz, pz, oz;
            if (xy) {
                dz = ray.dist.z; pz = pos.z; oz = ray.obj.z;
            } else { // zy
                dz = ray.dist.x; pz = pos.x; oz = ray.obj.x;
            }
            
            if (dz < EPS && dz > -EPS) {
                return 0;
            }
            double d = (pz - oz) / dz;
            if (d < 0) {
                return 0;
            }
            Vec x = ray.obj.add(ray.dist.mul(d));
            Vec p = x.sub(pos);
            double px, py;
            if (xy) {
                px = p.x; py = p.y;
            } else {
                px = p.z; py = p.y;
            }
            double w;
            if (width < 0) {
                px -= width;
                w = -width;
            } else {
                w = width;
            }
            if (px < 0 || px > w || py < 0 || py > height) {
                return 0;
            }
            if (!texture.isHit(this, x)) {
                return 0;
            }
            robj[0] = this;
            return d;
        }

        static final Vec TO_FRONT = new Vec(0, 0, 1);
        static final Vec TO_BACK = new Vec(0, 0, -1);
        static final Vec TO_RIGHT = new Vec(1, 0, 0);
        static final Vec TO_LEFT = new Vec(-1, 0, 0);
        
        @Override
        void position(Vec p, Ray r, Vec[] n, Col[] c) {
            if (xy) {
                n[0] =r.dist.z > 0 ? TO_BACK : TO_FRONT;
            } else {
                n[0] =r.dist.x > 0 ? TO_LEFT : TO_RIGHT;
            }
            c[0] =  texture.getCol(this, p);
        }
        
        @Override
        Point makeXY(Vec p) {
            Vec d = p.sub(pos);
            if (xy) {
                return new Point(d.x / width, d.y / height);
            } else {
                return new Point(d.z / width, d.y / height);
            }
        }
    }
    static final class Point {
        final double x, y;

        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
    static final class Col {
        final Vec emission, color;
        final Reflection reflection;

        public Col(Vec emission, Vec color,Reflection reflection) {
            this.emission = emission;
            this.color = color;
            this.reflection= reflection;
        }
    }
    static abstract class Texture {
        abstract Col getCol(Surface s, Vec x);
        boolean isHit(Surface s, Vec x)  {
            return true;
        }
    }
    static class SolidTexture extends Texture {
        final Col col;

        public SolidTexture(Vec emission, Vec color, Reflection ref) {
            this.col = new Col(emission, color, ref);
        }
        @Override
        Col getCol(Surface s, Vec x) {
            return col;
        }
    }
    static class CheckTexture extends Texture {
        final Col col1, col2;
        final double freq;

        public CheckTexture(Vec col1, Vec col2, double freq) {
            this.col1 = new Col(Vec.ZERO, col1, DIFFUSE);
            this.col2 = new Col(Vec.ZERO, col2, DIFFUSE);
            this.freq = freq;
        }

        @Override
        Col getCol(Surface s, Vec x) {
            Point p = s.makeXY(x);
            return (under(p.x / freq) - 0.5) * (under(p.y / freq) - 0.5) > 0 ? col1 : col2;
        }
        private double under(double d) {
            return d - floor(d);
        }
    }
    
    static class BitmapTexture extends Texture {
        final BufferedImage img;
        final int width, height;
        final Vec emission = Vec.ZERO;
        final double enhance;
        final double base;
        final double offset;

        public BitmapTexture(String file, double offset, double e, double base) {
            try {
                img = ImageIO.read(SmallPT.class.getResourceAsStream(file));
                width = img.getWidth(null);
                height = img.getHeight(null);
                this.offset = offset;
                enhance = e;
                this.base = base;
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
        
        public BitmapTexture(String file) {
            this(file, 0, 1, 0);
        }
        
        @Override
        Col getCol(Surface s, Vec x) {
            int rgb = getRgb(s, x);
            return new Col(emission, new Vec(intToDouble(rgb >> 16), intToDouble(rgb >> 8), intToDouble(rgb)), DIFFUSE);
        }
        
        private double intToDouble(int c) {
            return pow((c & 255) / 255., GAMMA) * enhance + base;
        }

        @Override
        boolean isHit(Surface s, Vec x) {
            int rgb = getRgb(s, x);
            return rgb >> 24 != 0;
        }
        
        protected int getRgb(Surface s, Vec x) {
            Point pos = s.makeXY(x);
            return img.getRGB((int)((pos.x + offset) * width) % width, (int)((1 - pos.y) * height));
        }
    }
    
    static class EmissionTexture extends BitmapTexture {
        final Vec emission = new Vec(9, 9, 9);
        final Vec color = Vec.ZERO;
        public EmissionTexture(String file) {
            super(file);
        }

        @Override
        Col getCol(Surface s, Vec x) {
            return new Col(emission, color, DIFFUSE);
        }

        @Override
        boolean isHit(Surface s, Vec x) {
            int rgb = getRgb(s, x);
            return (rgb >> 24 != 0) && (rgb >> 16 & 255) < 80;
        }
        
    }

    static class Polygon extends Surface {
        final Vec p1, p3;
        final Vec normal;
        final Vec e1, e2;

        public Polygon(Vec p1, Vec p2, Vec p3, Texture texture) {
            super(p2, texture);
            this.p1 = p1;
            this.p3 = p3;
            e1 = p1.sub(pos);
            e2 = p3.sub(pos);
            normal = e1.mod(e2).normalize();
        }
        private double det(Vec v1, Vec v2, Vec v3) {
            return v1.x * v2.y * v3.z + v2.x * v3.y * v1.z + v3.x * v1.y * v2.z
                    -v1.x * v3.y * v2.z - v2.x * v1.y * v3.z - v3.x * v2.y * v1.z;
        }
        @Override
        double intersect(Ray y, Surface[] robj) {
            Vec ray = y.dist.mul(-1);
            double deno = det(e1, e2, ray);
            if (deno <= 0) {
                return 0;
            }
            
            Vec d = y.obj.sub(pos);
            double u = det(d, e2, ray) / deno;
            if (u < 0 || u > 1) {
                return 0;
            }
            double v = det(e1, d, ray) / deno;
            if (v < 0 || u + v > 1) {
                return 0;
            }
            double t = det(e1, e2, d) / deno;
            if (t < 0) {
                return 0;
            }
            robj[0] = this;
            return t;
        }

        @Override
        void position(Vec p, Ray r, Vec[] n, Col[] c) {
            n[0] = normal;
            c[0] = texture.getCol(this, p);
        }

        @Override
        Point makeXY(Vec p) {
            return new Point(0, 0);
        }
    }

    static class PolygonSurface extends Surface {
        final Vec center;
        final double rad;
        final Polygon[] polygons;
        final Vec[] vertexes;
        final Sphere bound;

        public PolygonSurface(double rad, Vec pos, double[] vers, int[] surs, Texture tex) {
            super(pos, tex);
            Vec[] vs = IntStream.range(0, vers.length / 3)
                    .map(i -> i * 3)
                    .mapToObj(i -> new Vec(vers[i], 20 - vers[i + 1], vers[i + 2]))
                    .toArray(Vec[]::new);

            double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
            double minY = Double.POSITIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
            double minZ = Double.POSITIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;
            for (Vec ver : vs) {
                minX = min(minX, ver.x);
                maxX = max(maxX, ver.x);
                minY = min(minY, ver.y);
                maxY = max(maxY, ver.y);
                minZ = min(minZ, ver.z);
                maxZ = max(maxZ, ver.z);
            }
            center = new Vec((minX + maxX) / 2, (minY + maxY) / 2, (minZ + maxZ) / 2);
            double r = Double.NEGATIVE_INFINITY;
            for (Vec ver : vs) {
                r = max(r, center.sub(ver).distant());
            }
            this.rad = rad;
            double t = rad / r;
            bound = new Sphere(rad, pos, tex);
            vertexes = Arrays.stream(vs)
                    .map(v -> v.sub(center).mul(t).add(pos))
                    .toArray(Vec[]::new);
            polygons = IntStream.range(0, surs.length / 5)
                    .mapToObj(i -> i * 5)
                    .flatMap(i -> Stream.of(new Polygon(vertexes[surs[i]], vertexes[surs[i + 1]], vertexes[surs[i + 2]], tex),
                                             new Polygon(vertexes[surs[i + 2]], vertexes[surs[i + 3]], vertexes[surs[i]], tex)))
                    .filter(p -> p.pos != p.p1 && p.p1 != p.p3 && p.p3 != p.pos)
                    .toArray(Polygon[]::new);
        }

        @Override
        double intersect(Ray y, Surface[] robj) {
            double dist = bound.intersect(y, robj);
            if (dist == 0) {
                return 0;
            }
            double t = INF;
            for (Surface obj : polygons) {
                Surface[] cobj = {null};
                double d = obj.intersect(y, cobj);
                if (d != 0 && d < t) {
                    t = d;
                    robj[0] = obj;
                }
            }
            return t;
        }

        @Override
        void position(Vec p, Ray r, Vec[] n, Col[] c) {
            throw new UnsupportedOperationException();
        }

        @Override
        Point makeXY(Vec p) {
            throw new UnsupportedOperationException();
        }
    }

    static final Surface spheres[] = {//Scene: radius, position, emission, color, material
        new Plane(-50, 50, false, new Vec(-8.99, 10, 100), new BitmapTexture("/javashutubotsu.png", 0, 1, .2)),
        new Plane(50, 50, false, new Vec(108.99, 10, 50), new BitmapTexture("/javashutubotsu.png", 0, 1, .1)),
        new Sphere(1e5,  new Vec(1e5 + 1 - 10, 40.8, 81.6),  Vec.ZERO, new Vec(.75, .25, .25), Reflection.DIFFUSE),//Left
        new Sphere(1e5,  new Vec(-1e5 + 99 + 10, 40.8, 81.6), Vec.ZERO, new Vec(.25, .25, .75), Reflection.DIFFUSE),//Rght
        new Sphere(1e5,  new Vec(50, 40.8, 1e5 - 300),         Vec.ZERO, new Vec(.75, .75, .75), Reflection.DIFFUSE),//Back
        new Sphere(8, new Vec(80, 8, -150), new Vec(10, 10, 10), Vec.ZERO, Reflection.DIFFUSE),
        new Sphere(10, new Vec(20, 10, -160), Vec.ZERO, new Vec(.25, .75, .75), Reflection.DIFFUSE),
        
        new Sphere(1e5,  new Vec(50, 40.8, -1e5 + 170),  Vec.ZERO, Vec.ZERO, Reflection.DIFFUSE),//Frnt
        new Sphere(1e5,  new Vec(50, 1e5, 81.6),         Vec.ZERO, new Vec(.75, .75, .75), Reflection.DIFFUSE),//Botm
        new Sphere(1e5,  new Vec(50, -1e5 + 81.6, 81.6), Vec.ZERO, new Vec(.75, .75, .75), Reflection.DIFFUSE),//Top
        new Sphere(13, new Vec(22, 13, 47),          Vec.ZERO, new Vec(1, 1, 1).mul(.999), Reflection.SPECULAR),//Mirr
        new Sphere(10, new Vec(78, 10, 78),          Vec.ZERO, new Vec(1, 1, 1).mul(.999), Reflection.REFRECTION),//Glas
        new Sphere(600,  new Vec(50, 681.6 - .27, 81.6), new Vec(15, 15, 15), Vec.ZERO, Reflection.DIFFUSE), //Lite
        new Plane(40, 30, true, new Vec(32, 0, 60), new BitmapTexture("/duke600px.png")),
        new Sphere(10, new Vec(90, 40, 80), new BitmapTexture("/Earth-hires.jpg", .65, 1.5, 0)),
        //new Plane(24, 16, true, new Vec(25, 0, 72), new EmissionTexture("/duke600px.png")),
        new Sphere(10, new Vec(15, 10, 90), Vec.ZERO, new Vec(.25, .6, .3), Reflection.GLOSSY20),
        new PolygonSurface(23, new Vec(17, 55, 65), NapoData.cod, NapoData.jun, new SolidTexture(Vec.ZERO, new Vec(.25, .5, .75), Reflection.GLOSSY50))
    };
    static final double FOCAL_Z = 60;
    static final double LENS_RAD = 1.5;

    static double clamp(double x) {
        return x < 0 ? 0 : x > 1 ? 1 : x;
    }

    static int toInt(double x) {
        return min(255, (int) (pow(clamp(x), RECIP_GAMMA) * 255 + .5));
    }

    static boolean intersect(Ray r, double[] t, Surface[] robj) {
        t[0] = INF;
        for (Surface obj : spheres) {
            Surface[] cobj = {null};
            double d = obj.intersect(r, cobj);
            if (d != 0 && d < t[0]) {
                t[0] = d;
                robj[0] = cobj[0];
            }
        }
        return t[0] < INF;
    }

    private static double getRandom() {
        return ThreadLocalRandom.current().nextDouble();
    }
    
    static Vec radiance(Ray r, int depth) {
        double[] t = {0};                               // distance to intersection
        Surface[] robj = {null};
        Vec[] rn = {null};
        Col[] rc = {null};
        if (!intersect(r, t, robj)) {
            return Vec.ZERO; // if miss, return black
        }
        Surface obj = robj[0];        // the hit object
        Vec x = r.obj.add(r.dist.mul(t[0]));

        obj.position(x, r, rn, rc);
        Col tex = rc[0];
        Vec n = rn[0];
        Vec nl = n.dot(r.dist) < 0 ? n : n.mul(-1);
        Vec f = tex.color;
        double p = max(f.x, max(f.y, f.z)); // max refl
        depth++;
        if (depth > 5) {
            if (depth < 50 && getRandom() < p) {// 最大反射回数を設定
                f = f.mul(1 / p);
            } else {
                return tex.emission; //R.R.
            }
        }
        if (null == tex.reflection) {
            throw new IllegalStateException();
        } else switch(tex.reflection) {
            case DIFFUSE: {
                double r1 = 2 * Math.PI * getRandom(),
                        r2 = getRandom(),
                        r2s = sqrt(r2);
                Vec w = nl,
                        u = ((abs(w.x) > .1 ? Vec.UNIT_Y : Vec.UNIT_X).mod(w)).normalize(),
                        v = w.mod(u);
                Vec d = (u.mul(cos(r1) * r2s).add(v.mul(sin(r1) * r2s)).add(w.mul(sqrt(1 - r2)))).normalize();
                return tex.emission.add(f.vecmul(radiance(new Ray(x, d), depth)));
            }
            case SPECULAR: {
                // Ideal SPECULAR reflection
                return tex.emission.add(f.vecmul(radiance(new Ray(x, r.dist.sub(n.mul(2 * n.dot(r.dist)))), depth)));
            }
            case REFRECTION: {
                Ray reflectionRay = new Ray(x, r.dist.sub(n.mul(2 * n.dot(r.dist))));     // Ideal dielectric REFRACTION
                boolean into = n.dot(nl) > 0;                // Ray from outside going in?
                double nc = 1,
                        nt = 1.5,
                        nnt = into ? nc / nt : nt / nc,
                        ddn = r.dist.dot(nl),
                        cos2t = 1 - nnt * nnt * (1 - ddn * ddn);
                if (cos2t < 0) { // Total internal reflection
                    return tex.emission.add(f.vecmul(radiance(reflectionRay, depth)));
                }
                Vec tdir = (r.dist.mul(nnt).sub(n.mul((into ? 1 : -1) * (ddn * nnt + sqrt(cos2t))))).normalize();
                double a = nt - nc,
                        b = nt + nc,
                        R0 = a * a / (b * b),
                        c = 1 - (into ? -ddn : tdir.dot(n));
                double Re = R0 + (1 - R0) * c * c * c * c * c,
                        Tr = 1 - Re,
                        probability = .25 + .5 * Re,
                        RP = Re / probability,
                        TP = Tr / (1 - probability);
                return tex.emission.add(f.vecmul(depth > 2 ? (getRandom() < probability // Russian roulette
                        ? radiance(reflectionRay, depth).mul(RP) : radiance(new Ray(x, tdir), depth).mul(TP))
                        : radiance(reflectionRay, depth).mul(Re).add(radiance(new Ray(x, tdir), depth).mul(Tr))));
            }
            case GLOSSY50:
            case GLOSSY80:
            case GLOSSY20: {
                double lo_s = .75;
                double alphaX, alphaY;
                switch(tex.reflection) {
                    case GLOSSY20: alphaX = .25; break;
                    case GLOSSY50: alphaX = .50; break;
                    case GLOSSY80: alphaX = .80; break;
                    default: alphaX = .5;
                }
                alphaY = alphaX; // need to change by param
                Vec in = r.dist.mul(-1);
                Vec w = nl;
                Vec u = (abs(w.x) > .1 ? Vec.UNIT_Y : Vec.UNIT_X).mod(w).normalize();
                Vec v = w.mod(u);
                
                Vec halfv, dir;
                do {
                    double u1 = getRandom();
                    double u2 = getRandom();
                    double phi = atan(alphaY / alphaX * tan(2.0 * Math.PI * u2));
                    if (.25 <= u2 && u2 <= .75) {
                        phi += Math.PI;
                    } else if (u2 > .75) {
                        phi += 2 * Math.PI;
                    }
                    double theta = atan(sqrt(-log(u1) / 
                            (pow(cos(phi), 2) / pow(alphaX, 2) + pow(sin(phi), 2) / pow(alphaY, 2))));
                    halfv = u.mul(cos(phi) * sin(theta)).add(v.mul(sin(phi) * sin(theta))).add(w.mul(cos(theta)));
                    dir = halfv.mul(2 * in.dot(halfv)).sub(in);
                } while (nl.dot(dir) < 0);
                
                double weight = lo_s * halfv.dot(in) * pow(halfv.dot(nl), 3) *
                        sqrt(dir.dot(nl) / in.dot(nl));
                return tex.emission.add(tex.color.vecmul(radiance(new Ray(x, dir), depth)).mul(weight / p));
            }
                
            default:
                throw new IllegalStateException();
        }
    }

    // http://l2program.co.uk/900/concentric-disk-sampling
    // https://www.dartdocs.org/documentation/dartray/0.0.1/core/ConcentricSampleDisk.html
    static void concentricSampleDisk(double u1, double u2, double[] dx, double[] dy) {
        double r, theta;
        double sx = 2 * u1 - 1;
        double sy = 2 * u2 - 1;
        
        if (sx == 0 && sy == 0) {
            dx[0] = 0;
            dy[0] = 0;
            return;
        }
        
        if (sx >= -sy) {
            if (sx > sy) {
                r = sx;
                if (sy > 0) {
                    theta = sy / r;
                } else {
                    theta = 8 + sy / r;
                }
            } else {
                r = sy;
                theta = 2 - sx / r;
            }
        } else {
            if (sx <= sy) {
                r = -sx;
                theta = 4 - sy / r;
            } else {
                r = -sy;
                theta = 6 + sx / r;
            }
        }
        theta *= Math.PI / 4;
        dx[0] = r * cos(theta);
        dy[0] = r * sin(theta);
    }
    
    static final double CAMERA_Z = 295.6;
    static final double SCREEN_Z = 140;
    public static void main(String... argv) throws IOException {
        int w = 1024,
                h = 768,
                samps = (argv.length > 0 ? Integer.parseInt(argv[0]) : SAMPLES_DEFAULT )/ 4; // # samples

        Ray cam = new Ray(new Vec(50, 52, CAMERA_Z), new Vec(0, -0.042612, -1).normalize()); // cam pos, dir
        Vec cx = new Vec(w * .5135 / h, 0, 0),
                cy = (cx.mod(cam.dist)).normalize().mul(.5135);

        Instant start = Instant.now();
        Vec[] c = new Vec[w * h];
        Arrays.fill(c, new Vec(0, 0, 0)); // Don't use ZERO

        AtomicInteger count = new AtomicInteger();
        double[] lensU = {0}, lensV = {0};
        IntStream.range(0, h).parallel().forEach(y -> {
            System.out.printf("Rendering (%d spp) %5.2f%%%n", samps * 4, 100. * count.getAndIncrement() / (h - 1));
            for (int x = 0; x < w; x++) {// Loop cols
                int i = (h - y - 1) * w + x;
                for (int sy = 0; sy < 2; sy++) { // 2x2 subpixel rows
                    for (int sx = 0; sx < 2; sx++) {        // 2x2 subpixel cols
                        Vec r = new Vec(0, 0, 0); // Don't use ZERO
                        for (int s = 0; s < samps; s++) {
                            double r1 = 2 * getRandom(),
                                    dx = r1 < 1 ? sqrt(r1) - 1 : 1 - sqrt(2 - r1);
                            double r2 = 2 * getRandom(),
                                    dy = r2 < 1 ? sqrt(r2) - 1 : 1 - sqrt(2 - r2);
                            Vec d = cx.mul(((sx + .5 + dx) / 2 + x) / w - .5)
                                    .add(cy.mul(((sy + .5 + dy) / 2 + y) / h - .5)).add(cam.dist);
                            //r = r.add(radiance(new Ray(cam.obj.add(d.mul(140)), d.normalize()), 0));
                            Ray ray = new Ray(cam.obj.add(d.mul(SCREEN_Z)), d.normalize());
                            if (LENS_RAD == 0) {
                                r = r.add(radiance(ray, 0));
                            } else {
                                double focalDistance = CAMERA_Z - SCREEN_Z - FOCAL_Z;
                                concentricSampleDisk(getRandom(), getRandom(), lensU, lensV);
                                double ft = abs(focalDistance / ray.dist.dot(cam.dist));
                                Vec pFocus = ray.obj.add(ray.dist.mul(ft));
                                Vec robj = ray.obj.add(new Vec(lensU[0] * LENS_RAD, lensV[0] * LENS_RAD, 0));
                                Ray rray = new Ray(robj,
                                        pFocus.sub(robj).normalize());
                                r = r.add(radiance(rray, 0));
                            }
                        } // Camera rays are pushed ^^^^^ forward to start in interior
                        r = r.mul(1. / samps);
                        c[i] = c[i].add(new Vec(clamp(r.x), clamp(r.y), clamp(r.z)).mul(.25));
                    }
                }
            }
        });

        Instant finish = Instant.now();
        System.out.printf("Samples:%d Type:%s Time:%s%n",
                samps * 4,
                "master",
                Duration.between(start, finish));
        int[] imagesource = new int[w * h];
        for (int i = 0; i < w * h; ++i) {
            imagesource[i] = 255 << 24 | toInt(c[i].x) << 16 | toInt(c[i].y) << 8 | toInt(c[i].z);
        }
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        out.setRGB(0, 0, w, h, imagesource, 0, w);
        File f = new File("image.png");
        ImageIO.write(out, "png", f);

    }

}
