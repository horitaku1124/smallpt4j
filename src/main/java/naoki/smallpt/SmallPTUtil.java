package naoki.smallpt;

import static org.apache.commons.math3.util.FastMath.abs;
import static org.apache.commons.math3.util.FastMath.min;
import static org.apache.commons.math3.util.FastMath.pow;
import static org.apache.commons.math3.util.FastMath.sqrt;

import java.util.concurrent.ThreadLocalRandom;

import naoki.smallpt.primitives.Vec;

public class SmallPTUtil {

    public static double clamp(double x) {
        return x < 0 ? 0 : x > 1 ? 1 : x;
    }

    public static int toInt(double x) {
        return min(255, (int) (pow(clamp(x), SmallPT.RECIP_GAMMA) * 255 + .5));
    }

    public static double getRandom() {
        return ThreadLocalRandom.current().nextDouble();
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
}
