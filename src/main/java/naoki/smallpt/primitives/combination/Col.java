package naoki.smallpt.primitives.combination;

import naoki.smallpt.primitives.Reflection;
import naoki.smallpt.primitives.Vec;

public class Col {
    public final Vec emission, color;
    public final Reflection reflection;

    public Col(Vec emission, Vec color,Reflection reflection) {
        this.emission = emission;
        this.color = color;
        this.reflection= reflection;
    }
}