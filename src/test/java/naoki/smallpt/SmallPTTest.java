package naoki.smallpt;

import org.junit.Test;

import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.*;

import naoki.smallpt.primitives.Vec;
import naoki.smallpt.primitives.Ray;
import naoki.smallpt.textures.surface.Polygon;
import naoki.smallpt.textures.surface.PolygonSurface;
import naoki.smallpt.textures.surface.Surface;

/**
 *
 * @author naoki
 */
public class SmallPTTest {

    public static org.hamcrest.Matcher<java.lang.Double> isCloseTo(double expected) {
        return closeTo(expected, 0.001);
    }
    
    public SmallPTTest() {
    }

    @Test
    public void testSomeMethod() {
        assertEquals(1, 1);
    }
    
    @Test
    public void polygonIntersect() {
        Polygon p = new Polygon(new Vec(0, 3, 1), new Vec(3, 0, 1), new Vec(0, 0, 1), null);
        Surface[] f = {null};
        assertTrue(p.intersect(new Ray(new Vec(1, 1, 2), new Vec(0, 0, -1)), f) > 0);
        assertFalse(p.intersect(new Ray(new Vec(1, 1, 0), new Vec(0,0, 1)), f) > 0);
    }
 
    @Test
    public void surfaceIntersect() {
        SmallPT sp = new SmallPT();
        PolygonSurface s = new PolygonSurface(10, new Vec(0, 0, 1), new double[]{0, 3, 1, 3, 0, 1, 0, 0, 1}, new int[]{0, 1, 2, 0}, null);
        Surface[] f = {null};
        assertTrue(s.intersect(new Ray(new Vec(0, 0, 0), new Vec(0, 0, 1) ), f) > 0);
    }
}
