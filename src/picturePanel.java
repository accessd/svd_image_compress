/**
 * Класс, использующийся для отрисовки на JPanel
 */
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class picturePanel extends javax.swing.JPanel {

    private BufferedImage originalImage = null;

    private Image image = null;

    int iw, ih;

    File f_1;

    // Берем прорисовку в свои руки.
    public void paint(Graphics g) {
        // Рисуем картинку
        if (image != null) {
            g.drawImage(image, 0, 0, null);
        }
        // Рисуем рамку
        super.paintBorder(g);
    }

    public BufferedImage getImage() {
        return originalImage;
    }

    // Метод для настройки картинки.
    public void setImage(BufferedImage image) {
        this.originalImage = image;
        int w = this.getWidth();
        int h = this.getHeight();
        if ((originalImage != null) && (w > 0) && (h > 0))
            this.image = originalImage.getScaledInstance(w, h,
                    image.SCALE_DEFAULT);
        getParent().repaint();
    }

    public void setImageFile(File imageFile) {
        try {
            originalImage = ImageIO.read(imageFile);
            iw = originalImage.getWidth(null);
            ih = originalImage.getHeight(null);
            setImage(originalImage);
        } catch (IOException ex) {
            System.err.println("Error in load image!");
            ex.printStackTrace();
            this.repaint();
        }
        //return bi;
    }

}
