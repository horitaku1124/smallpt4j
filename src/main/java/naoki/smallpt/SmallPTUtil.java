package naoki.smallpt;

import static org.apache.commons.math3.util.FastMath.abs;
import static org.apache.commons.math3.util.FastMath.cos;
import static org.apache.commons.math3.util.FastMath.min;
import static org.apache.commons.math3.util.FastMath.pow;
import static org.apache.commons.math3.util.FastMath.sin;
import static org.apache.commons.math3.util.FastMath.sqrt;

import naoki.smallpt.primitives.Ray;
import naoki.smallpt.primitives.Vec;

public class SmallPTUtil {
    interface Randomizer {
        double getRandom();
    }
    static Randomizer random;
    static void setRandomiwer(Randomizer r) {
        SmallPTUtil.random = r;
    }
    private static final double PI_2 = 2 * Math.PI;

    public static double clamp(double x) {
        return x < 0 ? 0 : x > 1 ? 1 : x;
    }

    public static int toInt(double x) {
        return min(255, (int) (pow(clamp(x), SmallPT.RECIP_GAMMA) * 255 + .5));
    }

    public static double getRandom() {
        return random.getRandom();
    }

    public static Vec _w2u(Vec w) {
        return ((abs(w.x) > .1 ? Vec.UNIT_Y : Vec.UNIT_X).mod(w)).normalize();
    }
    public static Vec w2u(Vec w) {
        if (w.x > .1 || w.x < -.1) {
            double x = w.z;
            double y = 0;
            double z = - w.x;
            if ((x + z) == 0) {
                return Vec.UNIT_X;
            }

            double dist = sqrt(x * x + z * z);

            x /= dist;
            z /= dist;
            return new Vec(x, y, z);
        } else {
            double x = 0;
            double y = -w.z;
            double z = w.y;

            if ((y + z) == 0) {
                return Vec.UNIT_X;
            }
            double dist = sqrt(y * y + z * z);

            y /= dist;
            z /= dist;
            return new Vec(x, y, z);
        }
    }

    public static Ray _createXDRay(Vec x, Vec u, Vec nl) {
        double r1 = PI_2 * getRandom(),
                r2 = getRandom(),
                r2s = sqrt(r2);
        return new Ray(x, (u.mul(cos(r1) * r2s).add(nl.mod(u).mul(sin(r1) * r2s)).add(nl.mul(sqrt(1 - r2)))).normalize());
    }
    public static Ray createXDRay(Vec x, Vec u, Vec nl) {
        double r1 = PI_2 * getRandom(),
                r2 = getRandom(),
                r2s = sqrt(r2),
                s1r2 = sqrt(1 - r2),
                cosR1R2s = cos(r1) * r2s,
                sinR1R2s = sin(r1) * r2s;

        double nl_x = nl.y * u.z - nl.z * u.y;
        double nl_y = nl.z * u.x - nl.x * u.z;
        double nl_z = nl.x * u.y - nl.y * u.x;
        nl_x *= sinR1R2s;
        nl_y *= sinR1R2s;
        nl_z *= sinR1R2s;

        double u1_x = u.x,
               u1_y = u.y,
               u1_z = u.z;
        u1_x *= cosR1R2s;
        u1_y *= cosR1R2s;
        u1_z *= cosR1R2s;
        u1_x += nl_x;
        u1_y += nl_y;
        u1_z += nl_z;
        u1_x += nl.x * s1r2;
        u1_y += nl.y * s1r2;
        u1_z += nl.z * s1r2;
        double dist = sqrt(u1_x * u1_x + u1_y * u1_y + u1_z * u1_z);
        Vec result;
        if (dist == 0) {
            result = Vec.UNIT_X;
        } else {
            u1_x /= dist;
            u1_y /= dist;
            u1_z /= dist;
            result = new Vec(u1_x, u1_y, u1_z);
        }
        return new Ray(x, result);
    }
}
