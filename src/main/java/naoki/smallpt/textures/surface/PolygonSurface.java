package naoki.smallpt.textures.surface;

import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import naoki.smallpt.SmallPT;
import naoki.smallpt.primitives.Point;
import naoki.smallpt.primitives.Ray;
import naoki.smallpt.primitives.Vec;
import naoki.smallpt.primitives.combination.Col;
import naoki.smallpt.textures.Texture;

import static java.lang.Math.min;
import static java.lang.Math.max;

public class PolygonSurface extends Surface {
    private final Vec center;
    private final Polygon[] polygons;
    private final Vec[] vertexes;
    private final Sphere bound;

    public PolygonSurface(double rad, Vec pos, double[] vers, int[] surs, Texture tex) {
        super(pos, tex);

        double[] minMaxXYZ = new double[] {
                Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY
        };
        Vec[] vs = IntStream.range(0, vers.length / 3)
                .map(i -> i * 3)
                .mapToObj(i -> new Vec(vers[i], 20 - vers[i + 1], vers[i + 2]))
                .peek(ver -> {
                    minMaxXYZ[0] = min(minMaxXYZ[0], ver.x);
                    minMaxXYZ[1] = max(minMaxXYZ[1], ver.x);
                    minMaxXYZ[2] = min(minMaxXYZ[2], ver.y);
                    minMaxXYZ[3] = max(minMaxXYZ[3], ver.y);
                    minMaxXYZ[4] = min(minMaxXYZ[4], ver.z);
                    minMaxXYZ[5] = max(minMaxXYZ[5], ver.z);
                })
                .toArray(Vec[]::new);

        center = new Vec(
                (minMaxXYZ[0] + minMaxXYZ[1]) / 2,
                (minMaxXYZ[2] + minMaxXYZ[3]) / 2,
                (minMaxXYZ[4] + minMaxXYZ[5]) / 2);
        double r = Double.NEGATIVE_INFINITY;
        for (Vec ver : vs) {
            r = max(r, center.sub(ver).distant());
        }
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
    public double intersect(Ray y, Surface[] robj) {
        double dist = bound.intersect(y, robj);
        if (dist == 0) {
            return 0;
        }
        double t = SmallPT.INF;
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
    public void position(Vec p, Ray r, Vec[] n, Col[] c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Point makeXY(Vec p) {
        throw new UnsupportedOperationException();
    }
}