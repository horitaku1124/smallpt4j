package naoki.smallpt.primitives;

import static org.apache.commons.math3.util.FastMath.sqrt;

public class Vec {        // Usage: time ./smallpt 5000  xv image.ppm
    static final public Vec UNIT_X = new Vec(1, 0, 0);
    static final public Vec UNIT_Y = new Vec(0, 1, 0);
    static final public Vec ZERO = new Vec();
    static final public Vec EMPTY = new Vec();

    final public double x, y, z;                  // position, also color (r,g,b)

    public Vec(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }


    private Vec() {
        this(0, 0, 0);
    }

    public Vec add(Vec b) {
        return new Vec(x + b.x, y + b.y, z + b.z);
    }

    public Vec sub(Vec b) {
        return new Vec(x - b.x, y - b.y, z - b.z);
    }

    public Vec mul(double b) {
        return new Vec(x * b, y * b, z * b);
    }

    public Vec vecmul(Vec b) {
        return new Vec(x * b.x, y * b.y, z * b.z);
    }

    public Vec normalize() {
        double dist = distant();
        if (dist == 0) {
            return UNIT_X;
        }
        return new Vec(x / dist, y / dist, z / dist);
    }
    public double distant() {
        return sqrt(x * x + y * y + z * z);
    }

    public double dot(Vec b) {
        return x * b.x + y * b.y + z * b.z;
    } // cross:

    public Vec mod(Vec b) {
        return new Vec(y * b.z - z * b.y, z * b.x - x * b.z, x * b.y - y * b.x);
    }
    
    @Override
    public String toString() {
        return String.format("x=%f y=%f z=%f", x, y, z);
    }
}