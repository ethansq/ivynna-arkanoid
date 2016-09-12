package excelion;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;

public class Ball extends GameObject {
    private double diameter, radius; //radius is actually diameter
    private double xSpeed;
    private double ySpeed;
    public BufferedImage[] images = new BufferedImage[1];
    private Ellipse2D circle;
    private Random rand = new Random();
    private boolean fired = false;
    private Area area;
    private double positionOnShip;

    public Ball(int x, int y, int diameter, Color color, double xSpeed, double ySpeed) {
        super(x, y, diameter, diameter, color);
        this.diameter = diameter;
        this.xSpeed = xSpeed;
        this.ySpeed = ySpeed;
        this.circle = new Ellipse2D.Double(x, y, diameter, diameter);
        this.radius = diameter / 2;
        try {
            images[0] = ImageIO.read(new File(".\\src\\images\\ball2.png"));
        } catch (IOException io) {
            System.out.println("File could not be found!");
        }
        area = new Area(circle);
    }
    
    public double getRadius() {
        return diameter / 2;
    }
    
    public int getDiameter() {
        return (int)diameter;
    }
    
    public void ballStop() {
        xSpeed = 0;
        ySpeed = 0;
    }
    
    public void setSpeed(double xSpd, double ySpd) {
        xSpeed = xSpd;
        ySpeed = ySpd;
    }
    
    public void setPosition(int xNew, int yNew) {
        x = xNew;
        y = yNew;
    }
    
    public void setPositionOnShip(double x) {
        positionOnShip = x;
    }
    
    public double getPositionOnShip() {
        return positionOnShip;
    }
    
    public void reset(Player ship) {
        x = ship.getX() + (int)ship.getActualWidth() / 2 - 5;
        y = ship.getY() - 12;       
    }
    
    public boolean blockCollision(Blocks block) {
        if (block.getArea().contains(x + diameter, y + radius) && xSpeed > 0) { //left of brick
            setSpeed(-xSpeed, ySpeed);
            return true;
        } 
        else if (block.getArea().contains(x, y + radius) && xSpeed < 0) { //right of brick
            setSpeed(-xSpeed, ySpeed);
            return true;
        } 
        else if (block.getArea().contains(x + radius, y + diameter) && ySpeed > 0) { //top of brick
            setSpeed(xSpeed, -ySpeed);
            return true;
        } 
        else if (block.getArea().contains(x + radius, y) && ySpeed < 0) { //bottom of brick
            setSpeed(xSpeed, -ySpeed);
            return true;
        } else
            return false;
    }
    
    public boolean checkCollision(Player ship, Arkanoid game) { //collision with ship
        if (fired == true && ySpeed > 0 && Math.pow((x + radius - (ship.getX() + ship.circleRadius())), 2) + Math.pow((y + radius - (ship.getY() + ship.circleRadius())), 2) <= Math.pow((radius + ship.circleRadius()), 2) && y < ship.getY() + ship.getHeight() / 2) {
            ySpeed = -ySpeed;
            setSpeed(-3.75, ySpeed);
            //while (Math.pow((x - ship.getX()), 2) + Math.pow((y - ship.getY()), 2) <= Math.pow((radius + ship.circleRadius()), 2)) {
            //    x = (int)(x + xSpeed);
            //    y = (int)(y + ySpeed);
            //}
            return true;
        } else if (fired == true && ySpeed > 0 && Math.pow((x + radius - (ship.getX() + ship.getActualWidth() - ship.circleRadius())), 2) + Math.pow((y + radius - (ship.getY() + ship.circleRadius())), 2) <= Math.pow((radius + ship.circleRadius()), 2) && y < ship.getY() + ship.getHeight() / 2) {
            ySpeed = -ySpeed;
            setSpeed(3.75, ySpeed);
            //while (Math.pow((x - ship.getX()), 2) + Math.pow((y - ship.getY()), 2) <= Math.pow((radius + ship.circleRadius()), 2)) {
            //    x = (int)(x + xSpeed);
            //    y = (int)(y + ySpeed);
            //}
            return true;
        } else if (ySpeed > 0 && y + diameter >= ship.getY() - 1 && y + diameter <= ship.getY() + 2 && x + radius >= ship.getX() + ship.circleRadius() && x + radius <= ship.getX() + ship.getActualWidth() - ship.circleRadius()) {
            double xRelative = x + radius - ship.xRect();
            double ratio = xRelative / ship.rectWidth() - 1;
            double cosVariable = Math.cos(ratio * Math.PI);
            double multiplier = cosVariable * 3.0;
            multiplier = Math.abs(multiplier) < 1 ? 1 : multiplier;

            if (x + radius >= ship.getX() + ship.getActualWidth() / 2 && xSpeed > 0) { //right side of ship coming from left
                setSpeed(xSpeed, ySpeed); //multiplier is positive
            } else if (x + radius >= ship.getX() + ship.getActualWidth() / 2 && xSpeed <= 0){ //right side of ship coming from right
                setSpeed(multiplier, ySpeed); //multiplier is positive
            } else if (x + radius <= ship.getX() + ship.getActualWidth() / 2 && xSpeed >= 0) { //left side of ship coming from left
                setSpeed(multiplier, ySpeed); //multiplier is negative
            } else if (x + radius <= ship.getX() + ship.getActualWidth() / 2 && xSpeed < 0) { //left side of ship coming from right
                setSpeed(xSpeed, ySpeed); //multiplier is negative
            }
            ySpeed = -ySpeed;
            return true;
        } else {
            return false;
        }
    }
    
    public void update(Arkanoid panel) {
        if (fired == true) {
            if (x + xSpeed < 0) { //left wall impact
                x = 0;
                xSpeed = -xSpeed;
            } 
            else if (x + xSpeed > panel.getWidth() - diameter - 1) { //right wall impact
                x = (int)(panel.getWidth() - diameter - 1);
                xSpeed = -xSpeed;
            } 
            else { //no horizontal impact
                if (Math.abs(xSpeed) < 0.8) {
                    xSpeed = 0;
                }
                x += xSpeed;
            }
            if (y > panel.getHeight() - diameter - 1) { //floor impact
                y = (int)(panel.getHeight() - diameter - 1);
                ySpeed = -ySpeed;
            }
            else if (y < 0) {
                y = 0;
                ySpeed = -ySpeed;
            }
            else { //no vertical impact
                y += ySpeed;
            }
        }
    }
    
    public boolean fired() {
        return fired;
    }
    
    public double getXSpeed() {
        return xSpeed;
    }
    
    public double getYSpeed() {
        return ySpeed;
    }
    
    public void setFired(boolean bool) {
        fired = bool;
    }

    public void paintComponent(Graphics2D g2) {
        circle.setFrame(x, y, diameter, diameter);
        g2.drawImage(images[0], (int)x, (int)y, (int)diameter, (int)diameter, null);
    }
}