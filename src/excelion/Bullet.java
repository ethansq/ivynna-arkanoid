package excelion;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Bullet extends GameObject {
    private BufferedImage image;
    private Rectangle2D rectangle;
    private int ySpeed = -2;
    private Area area;

    public Bullet(int x, int y, int width, int height, Color color) {
        super(x, y, width, height, color);
        super.x = x;
        super.y = y;
        super.height = height;
        rectangle = new Rectangle2D.Double(x, y, width, height);
        try {
            image = ImageIO.read(new File(".\\src\\images\\bullet.png"));
        } catch (IOException io) {
            System.out.println("Cannot read bullet image");
        }
    }

    public void update(Arkanoid game) {
        y = y + ySpeed;
    }

    public Area getArea() {
        return area;
    }

    public boolean checkCollision(Blocks block) {
        if (block.getArea().contains(x + (width / 2), y)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean outOfPanel(Arkanoid panel) {
        return y > panel.getHeight();
    }

    public void paintComponent(Graphics2D g2) {
        rectangle.setFrame(x, y, width, height);
        g2.drawImage(image, (int)x, (int)y, width, height, null);
    }
}
