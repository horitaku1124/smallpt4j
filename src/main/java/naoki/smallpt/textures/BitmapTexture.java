package naoki.smallpt.textures;

import static naoki.smallpt.primitives.Reflection.DIFFUSE;
import static org.apache.commons.math3.util.FastMath.pow;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;

import javax.imageio.ImageIO;

import naoki.smallpt.SmallPT;
import naoki.smallpt.primitives.Point;
import naoki.smallpt.primitives.Vec;
import naoki.smallpt.primitives.combination.Col;
import naoki.smallpt.textures.surface.Surface;

public class BitmapTexture extends Texture {
    private final BufferedImage img;
    private final int width, height;
    private final Vec emission = Vec.ZERO;
    private final double enhance;
    private final double offset;

    public BitmapTexture(String file, double offset, double e) {
        try {
            img = ImageIO.read(SmallPT.class.getResourceAsStream(file));
            width = img.getWidth(null);
            height = img.getHeight(null);
            this.offset = offset;
            enhance = e;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
    
    public BitmapTexture(String file) {
        this(file, 0, 1);
    }
    
    @Override
    public Col getCol(Surface s, Vec x) {
        int rgb = getRgb(s, x);
        return new Col(emission, new Vec(intToDouble(rgb >> 16), intToDouble(rgb >> 8), intToDouble(rgb)), DIFFUSE);
    }
    
    private double intToDouble(int c) {
        return pow((c & 255) / 255., SmallPT.GAMMA) * enhance;
    }

    @Override
    public boolean isHit(Surface s, Vec x) {
        int rgb = getRgb(s, x);
        return rgb >> 24 != 0;
    }
    
    int getRgb(Surface s, Vec x) {
        Point pos = s.makeXY(x);
        return img.getRGB((int)((pos.x + offset) * width) % width, (int)((1 - pos.y) * height));
    }
}