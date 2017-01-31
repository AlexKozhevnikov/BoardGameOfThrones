package com.alexeus.graph.util;

import javax.swing.*;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;

/**
 * Created by alexeus on 17.01.2017.
 * Мучитель изображений - класс-утилита, совершающий некоторые необходимые пытки над картинками.
 */
public class PictureTormentor {

    /**
     * Метод возвращает новую картинку с применённым к старой картинке цветовым фильтром
     * @param image картинка для истязаний
     * @param color цвет фильтра
     * @return измождённая цветом картинка
     */
    public static BufferedImage dye(BufferedImage image, Color color)
    {
        int w = image.getWidth();
        int h = image.getHeight();
        BufferedImage dyed = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dyed.createGraphics();
        g.drawImage(image, 0,0, null);
        g.setComposite(AlphaComposite.SrcAtop);
        g.setColor(color);
        g.fillRect(0,0,w,h);
        g.dispose();
        return dyed;
    }

    /**
     * Метод возвращает новую картинку, полученную поворотом старой картинки на 90 градусов
     * @param image       картинка для экзекуции
     * @param isClockwise true, если по часовой стрелке, и false в противном случае
     * @return измождённая поворотом картинка
     */
    public static BufferedImage getRotatedPicture(BufferedImage image, boolean isClockwise) {
        int w = image.getWidth(null), h = image.getHeight(null);
        GraphicsConfiguration gc = getDefaultConfiguration();
        BufferedImage result = gc.createCompatibleImage(h, w, Transparency.TRANSLUCENT);
        Graphics2D g = result.createGraphics();
        int centerOfRotation = isClockwise ? Math.max(w / 2, h / 2) : Math.min(w / 2, h / 2);
        g.rotate(Math.toRadians(isClockwise ? 90: -90), centerOfRotation, centerOfRotation);
        g.drawRenderedImage(image, null);
        g.dispose();
        return result;
    }

    /**
     * Метод превращает данную картинку в иконку с заданной высотой и шириной
     * @param srcImg картинка для пытки
     * @param w      требуемая ширина
     * @param h      требуемая высота
     * @return измождённая превращением иконка
     */
    static ImageIcon getIconOfImage(Image srcImg, int w, int h) {
        BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resizedImg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(srcImg, 0, 0, w, h, null);
        g2.dispose();
        return new ImageIcon(resizedImg);
    }

    /**
     * Метод возвращает новую картинку, полученную из исходной осерением.
     * @param srcImg картинка для истязания
     * @return ихмождённая осерением картинка
     */
    public static BufferedImage getGrayImage(BufferedImage srcImg) {
        ColorConvertOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
        return op.filter(srcImg, null);
    }

    private static GraphicsConfiguration getDefaultConfiguration() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        return gd.getDefaultConfiguration();
    }
}
