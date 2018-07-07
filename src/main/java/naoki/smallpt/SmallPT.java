/*
 Copyright (c) 2017 Naoki Kishida (naokikishida@gmail.com / twitter: @kis)
This software is released under the MIT License.
( https://github.com/kishida/smallpt4j/blob/master/LICENSE.txt )

This is based on the smallpt( http://www.kevinbeason.com/smallpt/ )
that is released under the MIT License.
( https://github.com/kishida/smallpt4j/blob/master/smallpt_LICENSE.txt )
*/

package naoki.smallpt;

import static naoki.smallpt.primitives.Reflection.DIFFUSE;
import static org.apache.commons.math3.util.FastMath.abs;
import static org.apache.commons.math3.util.FastMath.cos;
import static org.apache.commons.math3.util.FastMath.max;
import static org.apache.commons.math3.util.FastMath.min;
import static org.apache.commons.math3.util.FastMath.pow;
import static org.apache.commons.math3.util.FastMath.sin;
import static org.apache.commons.math3.util.FastMath.sqrt;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import javax.imageio.ImageIO;

import naoki.smallpt.primitives.Ray;
import naoki.smallpt.primitives.Reflection;
import naoki.smallpt.primitives.Vec;
import naoki.smallpt.primitives.combination.Col;
import naoki.smallpt.textures.BitmapTexture;
import naoki.smallpt.textures.EmissionTexture;
import naoki.smallpt.textures.SolidTexture;
import naoki.smallpt.textures.surface.Plane;
import naoki.smallpt.textures.surface.PolygonSurface;
import naoki.smallpt.textures.surface.Sphere;
import naoki.smallpt.textures.surface.Surface;

public class SmallPT {
    public static final int SAMPLES_DEFAULT = 40;

    public static final double GAMMA = 2.2;
    public static final double RECIP_GAMMA = 1 / GAMMA;
    public static final double EPS = 1e-4;
    public static final double INF = 1e20;

    final Surface spheres[] = {//Scene: radius, position, emission, color, material
        new Sphere(1e5,  new Vec(1e5 + 1, 40.8, 81.6),   new Vec(), new Vec(.75, .25, .25), Reflection.DIFFUSE),//Left
        new Sphere(1e5,  new Vec(-1e5 + 99, 40.8, 81.6), new Vec(), new Vec(.25, .25, .75), Reflection.DIFFUSE),//Rght
        new Sphere(1e5,  new Vec(50, 40.8, 1e5),         new Vec(), new Vec(.75, .75, .75), Reflection.DIFFUSE),//Back
        new Sphere(1e5,  new Vec(50, 40.8, -1e5 + 170),  new Vec(), new Vec(), Reflection.DIFFUSE),//Frnt
        new Sphere(1e5,  new Vec(50, 1e5, 81.6),         new Vec(), new Vec(.75, .75, .75), Reflection.DIFFUSE),//Botm
        new Sphere(1e5,  new Vec(50, -1e5 + 81.6, 81.6), new Vec(), new Vec(.75, .75, .75), Reflection.DIFFUSE),//Top
        new Sphere(13, new Vec(27, 13, 47),          new Vec(), new Vec(1, 1, 1).mul(.999), Reflection.SPECULAR),//Mirr
        new Sphere(10, new Vec(73, 10, 78),          new Vec(), new Vec(1, 1, 1).mul(.999), Reflection.REFRECTION),//Glas
        new Sphere(600,  new Vec(50, 681.6 - .27, 81.6), new Vec(6, 6, 6), new Vec(), Reflection.DIFFUSE), //Lite
        new Plane(40, 30, new Vec(30, 0, 60), new BitmapTexture("/duke600px.png")),
        new Sphere(10, new Vec(80, 40, 85), new BitmapTexture("/Earth-hires.jpg", .65, 1.5)),
        new Plane(32, 24, new Vec(45, 0, 100), new EmissionTexture("/duke600px.png")),
        new PolygonSurface(25, new Vec(27, 52, 70), NapoData.cod, NapoData.jun, new SolidTexture(new Vec(), new Vec(.25, .5, .75), DIFFUSE))
    };

    static double clamp(double x) {
        return x < 0 ? 0 : x > 1 ? 1 : x;
    }

    static int toInt(double x) {
        return min(255, (int) (pow(clamp(x), RECIP_GAMMA) * 255 + .5));
    }

    boolean intersect(Ray r, double[] t, Surface[] robj) {
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
    
    Vec radiance(Ray r, int depth) {
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
            case DIFFUSE:
                double r1 = 2 * Math.PI * getRandom(),
                        r2 = getRandom(),
                        r2s = sqrt(r2);
                Vec w = nl,
                        u = ((abs(w.x) > .1 ? Vec.UNIT_Y : Vec.UNIT_X).mod(w)).normalize(),
                        v = w.mod(u);
                Vec d = (u.mul(cos(r1) * r2s).add(v.mul(sin(r1) * r2s)).add(w.mul(sqrt(1 - r2)))).normalize();
                return tex.emission.add(f.vecmul(radiance(new Ray(x, d), depth)));
            case SPECULAR:
                // Ideal SPECULAR reflection
                return tex.emission.add(f.vecmul(radiance(new Ray(x, r.dist.sub(n.mul(2 * n.dot(r.dist)))), depth)));
            case REFRECTION:
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
            default:
                throw new IllegalStateException();
        }
    }

    public static void main(String... argv) throws IOException {
        SmallPT sp = new SmallPT();
        int w = 1024,
                h = 768,
                samps = (argv.length > 0 ? Integer.parseInt(argv[0]) : SAMPLES_DEFAULT )/ 4; // # samples

        Ray cam = new Ray(new Vec(50, 52, 295.6), new Vec(0, -0.042612, -1).normalize()); // cam pos, dir
        Vec cx = new Vec(w * .5135 / h, 0, 0),
                cy = (cx.mod(cam.dist)).normalize().mul(.5135);

        Instant start = Instant.now();
        Vec[] c = new Vec[w * h];
        Arrays.fill(c, new Vec()); // Don't use ZERO

        AtomicInteger count = new AtomicInteger();
        IntStream.range(0, h).parallel().forEach(y -> {
            System.out.printf("Rendering (%d spp) %5.2f%%%n", samps * 4, 100. * count.getAndIncrement() / (h - 1));
            for (int x = 0; x < w; x++) {// Loop cols
                int i = (h - y - 1) * w + x;
                for (int sy = 0; sy < 2; sy++) { // 2x2 subpixel rows
                    for (int sx = 0; sx < 2; sx++) {        // 2x2 subpixel cols
                        Vec r = new Vec(); // Don't use ZERO
                        for (int s = 0; s < samps; s++) {
                            double r1 = 2 * getRandom(),
                                    dx = r1 < 1 ? sqrt(r1) - 1 : 1 - sqrt(2 - r1);
                            double r2 = 2 * getRandom(),
                                    dy = r2 < 1 ? sqrt(r2) - 1 : 1 - sqrt(2 - r2);
                            Vec d = cx.mul(((sx + .5 + dx) / 2 + x) / w - .5)
                                    .add(cy.mul(((sy + .5 + dy) / 2 + y) / h - .5)).add(cam.dist);
                            r = r.add(sp.radiance(new Ray(cam.obj.add(d.mul(140)), d.normalize()), 0));
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
        System.out.println("Version 1.0.3");
    }
}
