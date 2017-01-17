package com.alexeus.graph.util;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import static com.alexeus.graph.constants.Constants.WAY;

/**
 * Created by alexeus on 15.01.2017.
 * Класс, загружающий картинки.
 * Некоторые из них нужны в более, чем одной вкладке, и нет смысла загружать их два раза. По этой причине ссылки
 * на все картинки содержатся в приватной карте images, откуда (если картинка уже загружена) она и извлекается.
 */
public class ImageLoader {

    private static ImageLoader instance = new ImageLoader();

    private HashMap<String, BufferedImage> images;

    private ImageLoader() {
        images = new HashMap<>();
    }

    public static ImageLoader getInstance() {
        return instance;
    }

    public BufferedImage getImage(String path) {
        return images.containsKey(path) ? images.get(path) : loadImage(path);
    }

    public ImageIcon getIcon(String path, int width, int height) {
        return PictureTormentor.getIconOfImage(getImage(path), width, height);
    }

    private BufferedImage loadImage(String path) {
        File file = new File(WAY + path);
        try {
            BufferedImage loadedImage = ImageIO.read(file);
            images.put(path, loadedImage);
            return loadedImage;
        } catch (IOException e) {
            System.out.println("Путь к файлу: " + path + ".");
            e.printStackTrace();
            return null;
        }
    }

}
