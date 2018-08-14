package naoki.smallpt.textures.surface;

import static naoki.smallpt.SmallPT.PI_2;
import static naoki.smallpt.SmallPT.PI_half;
import static org.apache.commons.math3.util.FastMath.asin;
import static org.apache.commons.math3.util.FastMath.atan2;
import static org.apache.commons.math3.util.FastMath.sqrt;

import naoki.smallpt.SmallPT;
import naoki.smallpt.primitives.Point;
import naoki.smallpt.primitives.Ray;
import naoki.smallpt.primitives.Reflection;
import naoki.smallpt.primitives.Vec;
import naoki.smallpt.primitives.combination.Col;
import naoki.smallpt.textures.Texture;

public class Sphere extends Surface {

    private final double rad2;       // radius
    private final double inv_rad;

    public Sphere(double rad, Vec p, Vec e, Vec c, Reflection refl) {
        super(p, e, c, refl);
        this.rad2 = rad * rad;
        this.inv_rad = 1 / rad;
    }
    public Sphere(double rad, Vec p, Texture texture) {
        super(p, texture);
        this.rad2 = rad * rad;
        this.inv_rad = 1 / rad;
    }

    @Override
    public double intersect(Ray r, Surface[] robj) { // returns distance, 0 if nohit
        Vec op = pos.sub(r.obj); // Solve t^2*d.d + 2*t*(o-p).d + (o-p).(o-p)-R^2 = 0
        double t,
                b = op.dot(r.dist),
                det = b * b - op.dot(op) + rad2;
        if (det < 0) {
            return 0;
        }
        det = sqrt(det);
        robj[0] = this;
        return (t = b - det) > SmallPT.EPS ? t : ((t = b + det) > SmallPT.EPS ? t : 0);
    }
    
    @Override
    public void position(Vec x, Ray r, Vec[] n, Col[] c) {
        n[0] = x.sub(pos).normalize();
        c[0] = texture.getCol(this, x);
    }

    @Override
    public Point makeXY(Vec x) {
        Vec position = x.sub(pos).mul(inv_rad);
        double phi = atan2(position.z, position.x);
        double theta = asin(position.y);
        return new Point(1 - (phi + Math.PI) / PI_2, (theta + PI_half) / Math.PI);
    }
    
}