package excelion;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class PowerUp extends GameObject {
    private BufferedImage image;
    private boolean collected = false;
    private Rectangle2D rectangle;
    private String type;
    private int ySpeed = 1;
    private Area area;

    public PowerUp(int x, int y, int width, int height, Color color, String type) {
        super(x, y, width, height, color);
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.type = type;
        rectangle = new Rectangle2D.Double(x, y, width, height);
        area = new Area(rectangle);
        try {
            image = ImageIO.read(new File(".\\src\\images\\pow_" + type + ".png"));
        } catch (IOException io) {
            System.out.println("Cannot read image");
        }
    }

    public Area getArea() {
        return area;
    }
    
    public void update(Arkanoid panel) {
        y = y + ySpeed;
    }
    
    public String getType() {
        return type;
    }
    
    public boolean outOfPanel(Arkanoid panel) {
        return y > panel.getHeight();
    }

    public void paintComponent(Graphics2D g2) {
        rectangle.setFrame(x, y, width, height);
        g2.drawImage(image, (int)x, (int)y, width, height, null);
    }
}