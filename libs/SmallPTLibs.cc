#include "naoki_smallpt_SmallPTLibs.h"
#include <stdio.h>
#include <math.h>
#include <stdlib.h>

JNIEXPORT jdoubleArray JNICALL Java_naoki_smallpt_SmallPTLibs_createXDRay
    (JNIEnv *env, jobject obj, jdouble nl_x, jdouble nl_y, jdouble nl_z ){
    double nl_x2 = static_cast<double>(nl_x);
    double nl_y2 = static_cast<double>(nl_y);
    double nl_z2 = static_cast<double>(nl_z);
    double x, y, z;
    if (nl_x2 > .1 || nl_x2 < -.1) {
        x = nl_y2;
        y = 0;
        z = - nl_x2;
        if ((x + z) == 0) {
            x = 1; // Vec.UNIT_X.x;
        } else {
            double dist = sqrt(x * x + z * z);

            x /= dist;
            z /= dist;
        }
    } else {
        x = 0;
        y = -nl_z2;
        z = nl_y2;

        if ((y + z) == 0) {
            x = 1; // Vec.UNIT_X.x;
        } else {
            double dist = sqrt(y * y + z * z);

            y /= dist;
            z /= dist;
        }
    }

    double pi = MATH_PI2;
    double r1 = pi * (double)rand() / RAND_MAX,
            r2 = (double)rand() / RAND_MAX,
            r2s = sqrt(r2),
            s1r2 = sqrt(1 - r2),
            cosR1R2s = cos(r1) * r2s,
            sinR1R2s = sin(r1) * r2s;

    double nl_x3 = nl_y2 * z - nl_z2 * y;
    double nl_y3 = nl_z2 * x - nl_x2 * z;
    double nl_z3 = nl_x2 * y - nl_y2 * x;
    nl_x3 *= sinR1R2s;
    nl_y3 *= sinR1R2s;
    nl_z3 *= sinR1R2s;

    double u1_x = x,
            u1_y = y,
            u1_z = z;
    u1_x *= cosR1R2s;
    u1_y *= cosR1R2s;
    u1_z *= cosR1R2s;
    u1_x += nl_x3;
    u1_y += nl_y3;
    u1_z += nl_z3;
    u1_x += nl_x2 * s1r2;
    u1_y += nl_y2 * s1r2;
    u1_z += nl_z2 * s1r2;
    double dist = sqrt(u1_x * u1_x + u1_y * u1_y + u1_z * u1_z);

    if (dist == 0) {
        x = 1;
        y = 0;
        z = 0;
    } else {
        x = u1_x / dist;
        y = u1_y / dist;
        z = u1_z / dist;
    }



    /* jbyteArray用のメモリを確保 */
    jdoubleArray ret = env->NewDoubleArray(3);
    /* jbyteArrayの参照ポインタ取得 */
    jdouble* b = env->GetDoubleArrayElements(ret, 0);
    b[0] = x;
    b[1] = y;
    b[2] = z;
    env->ReleaseDoubleArrayElements(ret, b, 0);
    return ret;
}