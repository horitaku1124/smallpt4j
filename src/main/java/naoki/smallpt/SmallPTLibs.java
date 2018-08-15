package naoki.smallpt;

public class SmallPTLibs {

    static {
        System.loadLibrary("hello");
    }

    // nativeメソッドの宣言
    public native double[] createXDRay(double nl_x, double nl_y, double nl_z);
}
