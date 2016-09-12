package excelion;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Blocks extends GameObject {
    private BufferedImage image;
    private Rectangle2D rectangle;
    private int x, y, width, height;
    private int maxhp, hp;
    private String[] colours = {"green", "violet", "blue", "yellow", "red", "gray"};
    private Area area;
    private boolean isInvincible = false;

    public Blocks(int x, int y, int width, int height, Color color, int hp) {
        super(x, y, width, height, color);
        rectangle = new Rectangle2D.Double(x, y, width, height);  
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.maxhp = hp;
        this.hp = hp;
        try {
            if (maxhp < 0) {
                isInvincible = true;
                image = ImageIO.read(new File("src\\bricks\\invincible_white1.png"));
            } else {
                image = ImageIO.read(new File("src\\bricks\\" + maxhp + "hp_" + colours[maxhp - 1] + "1.png"));
            }
        } catch (IOException io) {
        }
        area = new Area(rectangle);
    }
    
    public Rectangle2D getRectangle() {
        return rectangle;
    }
    
    public boolean isInvincible() {
        return isInvincible;
    }
    
    public int maxhp() {
        return maxhp;
    }
    
    public Area getArea() {
        return area;
    }
    
    public void minushp() {
        hp--;
        int colournum = maxhp - hp;
        try {
            if (hp > 0) {
                image = ImageIO.read(new File(".\\src\\bricks\\" + hp + "hp_" + colours[maxhp - 1] + "1.png"));
            }
        } catch (IOException io) {
            System.out.println("Image not found!");
        }
    }
    
    public int gethp() {
        return hp;
    }
    
    public void update(Arkanoid panel) {
        
    }
    
    public void paintComponent(Graphics2D g2) {
        rectangle.setFrame(x, y, width, height);
        g2.drawImage(image, x, y, width, height, null);
    }
}