package naoki.smallpt.primitives;

public class RGB extends Vec {
    static final public RGB ZERO = new RGB();
    static final public RGB EMPTY = ZERO;

    public RGB() {
        super();
    }
    public RGB(Vec vec) {
        super(vec.x, vec.y, vec.z);
    }
    public RGB(double r, double g, double b) {
        super(r, g, b);
    }

    public RGB mul(double b) {
        return new RGB(super.mul(b));
    }
}
