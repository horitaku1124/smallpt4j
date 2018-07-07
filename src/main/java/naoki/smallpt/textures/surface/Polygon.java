package naoki.smallpt.textures.surface;

import naoki.smallpt.primitives.Vec;
import naoki.smallpt.primitives.Point;
import naoki.smallpt.primitives.Ray;
import naoki.smallpt.primitives.combination.Col;
import naoki.smallpt.textures.Texture;

public class Polygon extends Surface {
	public final Vec p1, p3;
	public final Vec normal;
	public final Vec e1, e2;

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
    public double intersect(Ray y, Surface[] robj) {
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
    public void position(Vec p, Ray r, Vec[] n, Col[] c) {
        n[0] = normal;
        c[0] = texture.getCol(this, p);
    }

    @Override
    public Point makeXY(Vec p) {
        return new Point(0, 0);
    }
}