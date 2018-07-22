package naoki.smallpt.primitives;

public class Ray {
    public final Vec obj, dist;

    public Ray(Vec o, Vec d) {
        this.obj = o;
        this.dist = d;
    }
    
    @Override
    public String toString() {
        return "Ray.Obj = " + obj + "\nRay.dist = " + dist;
    }
}