package naoki.smallpt;

public class FakeMath {
    public static double sqrt(final double a) {
        final long x = Double.doubleToLongBits(a) >> 32;
        double y = Double.longBitsToDouble((x + 1072632448) << 31);
        return y;
    }

    public static double pow(final double a, final double b) {
        final int x = (int) (Double.doubleToLongBits(a) >> 32);
        final int y = (int) (b * (x - 1072632447) + 1072632447);
        return Double.longBitsToDouble(((long) y) << 32);
    }
}
