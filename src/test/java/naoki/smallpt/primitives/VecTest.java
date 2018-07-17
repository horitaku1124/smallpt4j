package naoki.smallpt.primitives;

import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat; 
import static org.hamcrest.Matchers.*;

public class VecTest {
    
    private org.hamcrest.Matcher<java.lang.Double> isCloseTo(double expected) {
        return closeTo(expected, 0.001);
    }
    @Test
    public void testAdd() {
        Vec vec1 = new Vec(1, 1, 1);
        Vec vec2 = new Vec(2, 3, 4);
        Vec vec3 = vec1.add(vec2);
        assertThat(vec3.x, isCloseTo(3.0));
        assertThat(vec3.y, isCloseTo(4.0));
        assertThat(vec3.z, isCloseTo(5.0));
    }
    @Test
    public void testSub() {
        Vec vec1 = new Vec(1, 1, 1);
        Vec vec2 = new Vec(2, 3, 4);
        Vec vec3 = vec1.sub(vec2);
        assertThat(vec3.x, isCloseTo(-1));
        assertThat(vec3.y, isCloseTo(-2));
        assertThat(vec3.z, isCloseTo(-3));
    }
    @Test
    public void testMul() {
        Vec vec1 = new Vec(1, 2, 3);
        Vec vec2 = vec1.mul(3);
        assertThat(vec2.x, isCloseTo(3));
        assertThat(vec2.y, isCloseTo(6));
        assertThat(vec2.z, isCloseTo(9));
    }
    @Test
    public void testVecmul() {
        Vec vec1 = new Vec(1, 1, 1);
        Vec vec2 = new Vec(2, 3, 4);
        Vec vec3 = vec1.vecmul(vec2);
        assertThat(vec3.x, isCloseTo(2));
        assertThat(vec3.y, isCloseTo(3));
        assertThat(vec3.z, isCloseTo(4));
    }
}
