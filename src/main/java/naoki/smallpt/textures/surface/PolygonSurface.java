package naoki.smallpt.textures.surface;

import static org.apache.commons.math3.util.FastMath.max;
import static org.apache.commons.math3.util.FastMath.min;

import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import naoki.smallpt.SmallPT;
import naoki.smallpt.primitives.Point;
import naoki.smallpt.primitives.Ray;
import naoki.smallpt.primitives.Vec;
import naoki.smallpt.primitives.combination.Col;
import naoki.smallpt.textures.Texture;

public class PolygonSurface extends Surface {
    private final Vec center;
    private final Polygon[] polygons;
    private final Vec[] vertexes;
    private final Sphere bound;

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