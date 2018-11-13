package net.blerg.concurrencyExperiment.java;

import org.apache.commons.lang3.math.NumberUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {

    private static final ExecutorService executor = Executors.newCachedThreadPool();

    public static void main(String[] args) throws IOException {
        long startTime = System.currentTimeMillis();
        BufferedImage[] images = readImages();
        BufferedImage hdr = createHdr(images);
        saveImage(hdr);
        long timeTaken = System.currentTimeMillis() - startTime;

        System.out.println("Java time taken: " + timeTaken + "ms");
        executor.shutdown();
    }

    private static BufferedImage[] readImages() throws IOException {
        BufferedImage[] images = new BufferedImage[4];

        for (int i=0; i<4; i++) {
            String fileName = "resources/" + (i+1) + ".JPG";
            images[i] = ImageIO.read(new File(fileName));
        }

        return images;
    }

    private static BufferedImage createHdr(BufferedImage[] images) {
        int width = images[0].getWidth();
        int height = images[0].getHeight();


        BufferedImage hdr = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        Set<Future<Pixel>> pixels = new HashSet<>();

        for (int x=0;x<hdr.getWidth(); x++) {
            for (int y=0; y<hdr.getHeight(); y++) {
                int[] colours = new int[4];
                colours[0] = images[0].getRGB(x,y);
                colours[1] = images[1].getRGB(x,y);
                colours[2] = images[2].getRGB(x,y);
                colours[3] = images[3].getRGB(x,y);
                pixels.add(calculateColour(x, y, colours));
            }
        }

        pixels.stream().forEach(futurePixel -> {
            try {
                Pixel pixel = futurePixel.get();
                hdr.setRGB(pixel.getX(), pixel.getY(), pixel.getRgb());
            } catch (Exception e) {
                System.out.println("Got an exception " + e.getLocalizedMessage());
            }

        });

        return hdr;
    }

    public static Future<Pixel> calculateColour(int x, int y, int[] colours) {
        return executor.submit(() -> {
            int largestNumber = 0;
            int indexWithLargestNumber = 0;

            for (int i=0; i<colours.length; i++) {

                int red = (new Color(colours[i])).getRed();
                int green = (new Color(colours[i])).getGreen();
                int blue = (new Color(colours[i])).getBlue();

                int max = NumberUtils.max(red,green,blue);
                int min = NumberUtils.min(red,green,blue);

                int diff = max - min;

                if (diff > largestNumber) {
                    largestNumber = diff;
                    indexWithLargestNumber = i;
                }
            }

            return new Pixel(x, y, colours[indexWithLargestNumber]);
        });
    }

    private static void saveImage(BufferedImage hdr) throws IOException {
        File hdrFile = new File("java-hdr.jpg");
        ImageIO.write(hdr, "jpg", hdrFile);
    }
}
