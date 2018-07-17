package naoki.smallpt;

import static naoki.smallpt.SmallPTTest.isCloseTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

import naoki.smallpt.primitives.Vec;

public class SmallPTUtilTest {
    @Test
    public void test1() {
        Vec vec1 = Vec.EMPTY;
        Vec u = SmallPTUtil.w2u(vec1);
        System.out.println(u);
        assertThat(u.x, isCloseTo(1));
        assertThat(u.y, isCloseTo(0));
        assertThat(u.z, isCloseTo(0));
    }
    @Test
    public void test2() {
        Vec vec1 = new Vec(1, 0, 0);
        Vec u = SmallPTUtil.w2u(vec1);
        System.out.println(u);
        assertThat(u.x, isCloseTo(0));
        assertThat(u.y, isCloseTo(0));
        assertThat(u.z, isCloseTo(-1));
    }
    @Test
    public void test3() {
        Vec vec1 = new Vec(-1, 0, 0);
        Vec u = SmallPTUtil.w2u(vec1);
        System.out.println(u);
        assertThat(u.x, isCloseTo(0));
        assertThat(u.y, isCloseTo(0));
        assertThat(u.z, isCloseTo(1));
    }

}
