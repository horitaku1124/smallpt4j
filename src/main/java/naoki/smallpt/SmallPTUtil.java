package naoki.smallpt;

import naoki.smallpt.primitives.Ray;
import naoki.smallpt.primitives.Vec;

import static org.apache.commons.math3.util.FastMath.*;

class SmallPTUtil {
    interface Randomizer {
        double getRandom();
    }
    private static Randomizer random;
    static void setRandomiwer(Randomizer r) {
        SmallPTUtil.random = r;
    }
    private static final double PI_2 = 2 * Math.PI;

    static double clamp(double x) {
        return x < 0 ? 0 : x > 1 ? 1 : x;
    }

    static int toInt(double x) {
        return min(255, (int) (pow(clamp(x), SmallPT.RECIP_GAMMA) * 255 + .5));
    }

    static double getRandom() {
        return random.getRandom();
    }


    static Ray w2u_createXDRay(Vec vx, Vec nl) {
        double x, y, z;
        if (nl.x > .1 || nl.x < -.1) {
            x = nl.z;
            y = 0;
            z = - nl.x;
            if ((x + z) == 0) {
                x = Vec.UNIT_X.x;
            } else {
                double dist = sqrt(x * x + z * z);

                x /= dist;
                z /= dist;
            }
        } else {
            x = 0;
            y = -nl.z;
            z = nl.y;

            if ((y + z) == 0) {
                x = Vec.UNIT_X.x;
            } else {
                double dist = sqrt(y * y + z * z);

                y /= dist;
                z /= dist;
            }
        }

        double r1 = PI_2 * getRandom(),
                r2 = getRandom(),
                r2s = sqrt(r2),
                s1r2 = sqrt(1 - r2),
                cosR1R2s = cos(r1) * r2s,
                sinR1R2s = sin(r1) * r2s;

        double nl_x = nl.y * z - nl.z * y;
        double nl_y = nl.z * x - nl.x * z;
        double nl_z = nl.x * y - nl.y * x;
        nl_x *= sinR1R2s;
        nl_y *= sinR1R2s;
        nl_z *= sinR1R2s;

        double u1_x = x,
                u1_y = y,
                u1_z = z;
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
        return new Ray(vx, result);
    }
}
