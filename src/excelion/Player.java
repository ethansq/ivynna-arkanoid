package excelion;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class Player extends GameObject {
    private double xSpeed, diameter;
    private Color colour;
    private Boolean showColour = false;
    private int flashCount = 0;
    private ArrayList<Shape> shapes = new ArrayList<Shape>();
    private Rectangle2D rectangle;
    private Ellipse2D leftCircle, rightCircle;
    private BufferedImage[] images = new BufferedImage[2];
    private double actualWidth;
    private int extraLives = 3;
    private Area recArea = new Area();
    private Area totalArea = new Area();
    private boolean enlarged = false;
        
    public Player(int x, int y, int width, int height, Color color) {
        super(x, y, width, height, color);
        this.colour = color;
        diameter = height;
        leftCircle = new Ellipse2D.Double(x, y, diameter, diameter);
        rectangle = new Rectangle2D.Double(x + diameter / 2, y, width, height);        
        rightCircle = new Ellipse2D.Double(x + width, y, diameter, diameter);
        actualWidth = diameter + width;
        
        recArea.add(new Area(rectangle));
        totalArea.add(new Area(rectangle));
        totalArea.add(new Area(leftCircle));
        totalArea.add(new Area(rightCircle));
        
        try {
            images[0] = ImageIO.read(new File(".\\src\\images\\ship_circle1.png"));
            images[1] = ImageIO.read(new File(".\\src\\images\\ship_base2.png"));
        } 
        catch (IOException io) {
        }
    }
    
    public void update(Arkanoid panel) {
        if (flashCount < 150) {
            flashCount++;
        } else {
            showColour = false;
        }
    }
    
    public boolean powerupCollision(PowerUp power) {
        boolean topY = power.getY() > y && power.getY() < y + height;
        boolean bottomY = power.getY() + power.getHeight() > y && power.getY() < y + height;
        
        if ((topY == true || bottomY == true) && power.getX() > x && power.getX() < x + width + diameter) {
            return true;
        }
        else if ((topY == true || bottomY == true) && power.getX() + power.getWidth() > x && power.getX() + power.getWidth() < x + width + diameter) {
            return true;
        } else {
            return false;
        }
    }
    
    public void grow() {
        width = width + 70;
        x = x - 35;
        actualWidth = diameter + width;
        enlarged = true;
    }
    
    public void subside() {
        width = width - 70;
        x = x + 35;
        actualWidth = diameter + width;
        enlarged = false;
    }
    
    public boolean isEnlarged() {
        return enlarged;
    }
    
    public void death() {
        extraLives--;
    }
    
    public int numLives() {
        return extraLives;
    }
    
    public void lifeGain(int gain) {
        extraLives = extraLives + gain;
    }
    
    public Area recArea() {
        return recArea;
    }
    
    public Area getArea() {
        return totalArea;
    }
    
    public void setPosition(int xNew, int yNew) {
        x = xNew;
        y = yNew;
    }
    
    public double getActualWidth() {
        return actualWidth;
    }
    
    public double circleRadius() {
        return diameter / 2;
    }
    
    public double getXSpeed() {
        return xSpeed;
    }
    
    public void setSpeed(int spd) {
        xSpeed = spd;
    }
    
    public void reset(Arkanoid panel) {
        x = panel.getWidth() / 2 - (int)actualWidth / 2;
    }
    
    public Rectangle2D getPlatform() {
        return rectangle;
    }
    
    public double xRect() {
        return x + circleRadius();
    }
    
    public double rectWidth() {
        return width;
    }
    
    public void showColour() {
        showColour = true;
        flashCount = 0;
    }
    
    public void paintComponent(Graphics2D g2) {
        leftCircle.setFrame(x - 2, y - 2, diameter + 2, diameter + 3);
        rectangle.setFrame(x + diameter / 2, y - 2, width, height + 3);        
        rightCircle.setFrame(x + width - 0.75, y - 2, diameter + 2, diameter + 3);
        
        g2.setColor(colour);
        if (showColour == true) {
            g2.fill(rectangle);
            g2.draw(rectangle);
            g2.fill(leftCircle);
            g2.draw(leftCircle);
            g2.fill(rightCircle);
            g2.draw(rightCircle);
        }
        
        g2.drawImage(images[1], (int)(x + 1 + diameter / 2), (int)y, width, height, null);
        g2.drawImage(images[0], (int)x, (int)y, (int)diameter, (int)diameter, null);
        g2.drawImage(images[0], (int)(x + width), (int)y, (int)diameter, (int)diameter, null);
    }
}