package naoki.smallpt;

import static naoki.smallpt.SmallPTTest.isCloseTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Before;
import org.junit.Test;

import naoki.smallpt.SmallPTUtil.Randomizer;
import naoki.smallpt.primitives.Ray;
import naoki.smallpt.primitives.Vec;

public class SmallPTUtilTest {
    
    @Before
    public void setup() {
        SmallPTUtil.setRandomiwer(new Randomizer() {
            @Override
            public double getRandom() {
                return 0;
            }
        });
    }
    
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
    

    @Test
    public void test4() {
        Ray r = SmallPTUtil.createXDRay(Vec.EMPTY, Vec.EMPTY, Vec.EMPTY);
        System.out.println(r);
        assertThat(r.obj.x, isCloseTo(0));
        assertThat(r.obj.y, isCloseTo(0));
        assertThat(r.obj.z, isCloseTo(0));
        assertThat(r.dist.x, isCloseTo(1));
        assertThat(r.dist.y, isCloseTo(0));
        assertThat(r.dist.z, isCloseTo(0));
    }

    @Test
    public void test5() {
        Ray r = SmallPTUtil.createXDRay(Vec.UNIT_X, Vec.UNIT_X, Vec.UNIT_X);
        System.out.println(r);
        assertThat(r.obj.x, isCloseTo(1));
        assertThat(r.obj.y, isCloseTo(0));
        assertThat(r.obj.z, isCloseTo(0));
        assertThat(r.dist.x, isCloseTo(1));
        assertThat(r.dist.y, isCloseTo(0));
        assertThat(r.dist.z, isCloseTo(0));
    }
    @Test
    public void test6() {
        Ray r = SmallPTUtil.createXDRay(Vec.UNIT_Y, Vec.UNIT_Y, Vec.UNIT_Y);
        System.out.println(r);
        assertThat(r.obj.x, isCloseTo(0));
        assertThat(r.obj.y, isCloseTo(1));
        assertThat(r.obj.z, isCloseTo(0));
        assertThat(r.dist.x, isCloseTo(0));
        assertThat(r.dist.y, isCloseTo(1));
        assertThat(r.dist.z, isCloseTo(0));
    }

}
