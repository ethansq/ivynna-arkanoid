package excelion;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.*;
import java.io.*;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;

public class Arkanoid extends JPanel implements Runnable {
    private ArrayList<GameObject> objects = new ArrayList<>();
    private ArrayList<PowerUp> puppies = new ArrayList<>();
    private String[] powerups = new String[] {"multiballs", "enlarge", "lasers", "extralife", "catch"}; 
    private ArrayList<Blocks> blocks = new ArrayList<>();   
    private String[] colours = new String[] {"green", "violet", "blue", "yellow", "red", "gray", "invincible"};
    private Blocks[] blcArray = new Blocks[blocks.size()];
    private ArrayList<Bullet> bullets = new ArrayList<>();
    private BufferedImage[] images = new BufferedImage[10];
    private Box boxLayout = new Box(BoxLayout.Y_AXIS);
    private JLabel infoField = new JLabel("");
    private String infoFieldDefaultText = "";
    private Player ship;
    private Ball ball;
    private Thread thread = new Thread(this);
    private boolean isRunning = false;
    private boolean isGameStarted = false;    
    private boolean isPaused = false;
    private boolean isCompleted = false;
    private JPanel pausePanel = new JPanel();
    private boolean hasMultiballs;
    private boolean hasEnlarge = false;    
    private boolean hasLasers = false;
    private boolean hasCatch = false;
    private java.util.Timer[] timers = { //timers used for powerup expirations
        new java.util.Timer("enlarge"),
        new java.util.Timer("lasers"),
        new java.util.Timer("catch")
    };
    private int score = 0;
    private File fontFile = new File(".\\src\\images\\American Captain.TTF");
    private JPanel gameInfo = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (isGameStarted == true) {
                try { //importing font
                    Font font = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(30f);
                    g.setFont(font);
                    g.setColor(Color.WHITE);
                } catch (IOException | FontFormatException io) {}

                FontMetrics fm = g.getFontMetrics();
                g.drawString("LIVES REMAINING", 5, 5 + fm.getAscent());
                g.drawString("SCORE", this.getWidth() - 5 - fm.stringWidth("SCORE"), 5 + fm.getAscent());
                g.drawString(score + "", this.getWidth() - 5 - fm.stringWidth(score + ""), this.getHeight() - 5);

                try {
                    for (int i = ship.numLives() - 1; i > -1; i--) {
                        g.drawImage(images[1], 4 + 27 * i, this.getHeight() - 7 - fm.getAscent(), fm.getAscent(), 25, null);
                    }
                } catch (NullPointerException npe) {             
                }
            }
        }
    };
    private JPanel startWindow = new JPanel(); //starting menu window
    private JPanel guidePanel = new JPanel(); //gridlayout to hold the icons and info
    private JPanel panelContainer = new JPanel() { //overall panel that holds everything
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(images[0], 0, 0, getWidth(), getHeight(), null); //draws the background image on the main panel
        }
    };
    private KeySelected keySelected = new KeySelected(); //listeners and detectors
    private MouseInteracted mouseInteraction = new MouseInteracted();
    private MousePosition mousePosition = new MousePosition();

    public Arkanoid() {
        try { //importing images and sounds
            images[0] = ImageIO.read(new File(".\\src\\images\\bg_space1.jpg"));
            //InputStream is = null;
            //URL url = getClass().getResource("/bg_space1.jpg");
            //is = getClass().getResourceAsStream("/images/bg_space1.jpg");
            //is = url.openStream();
            //System.out.println((is != null));
            images[0] = ImageIO.read(getClass().getResourceAsStream("/images/bg_space1.jpg"));
            images[1] = ImageIO.read(new File(".\\src\\images\\heart.png"));
            Clip clip = AudioSystem.getClip(); //hot-fix for clip initialization buffer lag
            
            BufferedReader inputStream = new BufferedReader(new FileReader(new File(".\\src\\texts\\descrip_notes.txt")));
            infoFieldDefaultText = inputStream.readLine();
            infoField.setText(infoFieldDefaultText);
        } catch (IOException | LineUnavailableException e) {
        }
        
        JFrame frame = new JFrame();
        Dimension dimension = new Dimension(650, 800);
        JButton start = new JButton("          START GAME          "); //functionality buttons
        JButton restart = new JButton("          MAIN MENU          ");
        JButton resume = new JButton("             RESUME             ");      
        try {
            start.setFont(Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(30f));
            start.setForeground(Color.WHITE);
            restart.setFont(Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(30f));
            restart.setForeground(Color.WHITE);
            resume.setFont(Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(30f));
            resume.setForeground(Color.WHITE);
            infoField.setFont(Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(19f));
            infoField.setForeground(Color.WHITE);
        } catch (IOException | FontFormatException io) {
        }
        start.setContentAreaFilled(false); //decorating/customizing buttons
        start.setFocusPainted(false);
        start.setActionCommand("Start Game");
        start.addActionListener(new ButtonClicked());
        restart.setContentAreaFilled(false);
        restart.setFocusPainted(false);
        restart.setActionCommand("Main Menu");
        restart.addActionListener(new ButtonClicked());  
        resume.setContentAreaFilled(false);
        resume.setFocusPainted(false);
        resume.setActionCommand("Resume");
        resume.addActionListener(new ButtonClicked());  
        
        startWindow.setPreferredSize(new Dimension(520, 700));
        startWindow.setMaximumSize(new Dimension(520, 700));
        startWindow.setOpaque(false);
        startWindow.setLayout(new BoxLayout(startWindow, BoxLayout.Y_AXIS));
        startWindow.add(Box.createVerticalStrut(100));
        start.setAlignmentX(Component.CENTER_ALIGNMENT);
        startWindow.add(start);
        startWindow.add(Box.createVerticalStrut(60));
        
        guidePanel.setLayout(new GridLayout(0, 7, 5, 5));
        guidePanel.setPreferredSize(new Dimension(520, 300));
        guidePanel.setMaximumSize(new Dimension(520, 300));
        guidePanel.setOpaque(false);
        
        BufferedReader inputStream = null;
        String lineRead = "";
        try {
            inputStream = new BufferedReader(new FileReader(new File(".\\src\\texts\\help_guide.txt")));
            lineRead = inputStream.readLine();
            for (int x = 0; x < 9; x++) {
                StringTokenizer textLine = new StringTokenizer(lineRead); //string tokenizer code
                for (int y = 0; y < 7; y++) {
                    String text = textLine.nextToken(); //parsing through a premade text file to make things simpler
                    String fileLocation = "";
                    int imgWidth = 1; //to scale the img inside a label
                    int imgHeight = 1;
                    if (text.equals("empty")) {
                        fileLocation = ".\\src\\images\\empty.png";
                    } else if (x == 2) {
                        fileLocation = getFileLocation("Brick", text);
                        imgWidth = 50;
                        imgHeight = 22;
                    } else if (x == 4) {
                        fileLocation = getFileLocation("Powerup", text);
                        imgWidth = 27;
                        imgHeight = 15;
                    } else if (x == 6 || x == 8) {
                        fileLocation = getFileLocation("Miscellaneous", text);
                        if (text.equals("ball")) {
                            imgWidth = 19;
                            imgHeight = 19;
                        } else if (text.equals("ship")) {
                            imgWidth = 72;
                            imgHeight = 17;
                        }
                    }
                    BufferedImage img = ImageIO.read(new File(fileLocation)); //displaying the image to the JLabels
                    BufferedImage scaled = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_ARGB);
                    scaled.getGraphics().drawImage(img, 0, 0, imgWidth, imgHeight, null);
                    ImageIcon icon = new ImageIcon(scaled);
                    JLabel imgLabel = new JLabel(icon);
                    imgLabel.addMouseListener(new MouseHoverOver(text)); //adds mouse hover detection to display necessary information upon hover
                    guidePanel.add(imgLabel);
                }
                lineRead = inputStream.readLine();
             }
        } catch (IOException ioe) {
        }
        startWindow.add(guidePanel);
        
        gameInfo.setOpaque(false);
        gameInfo.setPreferredSize(new Dimension(650, 75));
        gameInfo.setMaximumSize(new Dimension(650, 75));
        gameInfo.setMinimumSize(new Dimension(650, 75));
        gameInfo.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        gameInfo.add(infoField);

        this.setPreferredSize(dimension);
        this.setOpaque(false);
        this.setMaximumSize(dimension);
        this.setMinimumSize(dimension);
        this.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        this.setLayout(new BorderLayout());
        this.add(startWindow, BorderLayout.NORTH);
        
        restart.setAlignmentX(Component.CENTER_ALIGNMENT); //building pause menu for later use
        resume.setAlignmentX(Component.CENTER_ALIGNMENT);
        pausePanel.setLayout(new BoxLayout(pausePanel, BoxLayout.Y_AXIS));
        pausePanel.add(Box.createVerticalStrut(325));
        pausePanel.add(resume);
        pausePanel.add(Box.createVerticalStrut(10));
        pausePanel.add(restart);
        pausePanel.setBackground(Color.BLACK);
        pausePanel.setVisible(false);
        this.add(pausePanel, BorderLayout.CENTER);

        boxLayout.add(Box.createVerticalStrut(20));
        boxLayout.add(this);     
        boxLayout.add(Box.createVerticalStrut(5));
        boxLayout.add(gameInfo);     
        boxLayout.add(Box.createVerticalStrut(25));
        
        panelContainer.add(boxLayout);
        frame.add(panelContainer);
        frame.setTitle("A Game to Stifle Imperial Progression (A Game in Progress)");
        frame.getContentPane().setBackground(Color.BLACK);
        frame.setSize(new Dimension(700, 955));
        frame.setMinimumSize(frame.getMinimumSize());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
        this.setFocusable(true);
    }
    
    public String getFileLocation(String type, String text) {
        String url = ""; //determining what image to display
        switch (type) {
            case "Brick":
                if (text.equals("invincible")) {
                    url = ".\\src\\bricks\\invincible_white1.png"; 
                } else {
                    url = ".\\src\\bricks\\" + (Arrays.asList(colours).indexOf(text) + 1) + "hp_" + text + "1.png";
                }
                break;
            case "Powerup":
                url = ".\\src\\images\\pow_" + text + ".png";
                break;
            case "Miscellaneous":
                if (text.equals("ship")) {
                    url = ".\\src\\images\\ship_base3.png";
                } else if (text.equals("ball")) {
                    url = ".\\src\\images\\ball2.png";
                }
                break;
        }
        return url;
    }
    
    public void startGame() {
        ship = new Player(getWidth() / 2 - (64 + 14) / 2, getHeight() - 40, 64, 14, Color.CYAN);
        //ball starting position set to middle of ship
        double xBall = ship.getX() + (int)ship.getActualWidth() / 2 - 5; //centre x and y of ship
        double yBall = ship.getY() - 13;
        ball = new Ball((int)xBall, (int)yBall, 10, new Color(255, 255, 255), 1, -2);
        ball.setFired(false);
        ball.setPositionOnShip(ship.getX() + (int)ship.getActualWidth() / 2 - xBall);
        objects.add(ship);
        objects.add(ball);    
        generateBlocks();
        start();
        
        infoField.setText("");
    }
    
    public void generateBlocks() {
        int brickWidth = 50;
        int brickHeight = 22;
        Random rand = new Random();
        BufferedReader inputStream = null;
        String lineRead = "";
        try {
            inputStream = new BufferedReader(new FileReader(new File(".\\src\\texts\\level_display.txt")));
            lineRead = inputStream.readLine();
            for (int y = 0; y < 12; y++) {
                StringTokenizer textLine = new StringTokenizer(lineRead);
                Blocks brick;
                for (int x = 0; x < 13; x++) {
                    String text = textLine.nextToken();
                    if (text.equals("inv")) { // invincible
                        brick = new Blocks(0 + brickWidth * x, 95 + brickHeight * y, 50, 22, Color.WHITE, 1); //makes an invincible block -1
                    } else if (x == 0 || x == 12 || y == 0 || y == 11) {
                        brick = new Blocks(0 + brickWidth * x, 95 + brickHeight * y, 50, 22, Color.WHITE, 1); //makes a green block 1
                    } else if (x == 1 || x == 11) {
                        brick = new Blocks(0 + brickWidth * x, 95 + brickHeight * y, 50, 22, Color.WHITE, 5); //makes a red block 5 
                    } else if (x == 2 || x == 10) {
                        brick = new Blocks(0 + brickWidth * x, 95 + brickHeight * y, 50, 22, Color.WHITE, 4); //makes a yellow block 4
                    } else if (x == 3 || x == 9) {
                        brick = new Blocks(0 + brickWidth * x, 95 + brickHeight * y, 50, 22, Color.WHITE, 2); //makes a violet block 2
                    } else if (x == 4 || x == 8) {
                        brick = new Blocks(0 + brickWidth * x, 95 + brickHeight * y, 50, 22, Color.WHITE, 6); //makes a gray block 6
                    } else {
                        brick = new Blocks(0 + brickWidth * x, 95 + brickHeight * y, 50, 22, Color.WHITE, 3); //makes a blue block 3
                    }
                    blocks.add(brick);
                }
                lineRead = inputStream.readLine();
            }
        } catch (IOException ioe) {
        }
        blcArray = blocks.toArray(blcArray);
    }
    
    public void playSound(String type) {
        String file = ".\\src\\sounds\\";
        
        switch (type) {
            case "Brick Collision":
                file = file + "block_collision1.wav";
                break;
            case "Invincible Brick Collision":
                file = file + "invincible_brick_collision1.wav";
                break;
            case "Ship Collision":
                file = file + "ship_collision.wav";
                break;
            case "Death":
                file = file + "death_music1.wav";
                break;
            case "Powerup Collected":
                file = file + "powerup_collected1.wav";
                break;
            case "Laser Fired":
                file = file + "bullet_fired1.wav";
                break;
            case "Victory":
                file = file + "victory_fanfare1.wav";
                break;
        }
        try {
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(new File(file));
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
            System.out.println("File not found!");
        }
    }
    
    public boolean hasCatch() {
        return hasCatch;
    }
    
    public void generatePower(int n) {
        //creates a powerup (on a 60% chance)
        int cX = (int)blcArray[n].getX() + blcArray[n].getWidth() / 2 - 10; //x of powerup spawn pos
        int cY = (int)blcArray[n].getY() + blcArray[n].getHeight() / 2; //y of powerup spawn pos
        Random rand = new Random();
        int rngNum = rand.nextInt(100);
        if (rngNum > 40) { //60% chance that a block will drop a powerup
            PowerUp power = new PowerUp(cX, cY, 18, 10, Color.WHITE, powerups[rand.nextInt(5)]);
            puppies.add(power);
        }
    }
    
    public void start() {
        thread = new Thread(this);       
        thread.start();
        isRunning = true;
    }

    public void stop() {
        isRunning = false;
    }

    public void run() {
        int ballsAlive = 1;
        addMouseListener(mouseInteraction);       
        addMouseMotionListener(mousePosition);
        addKeyListener(keySelected);
        
        while (isRunning == true) {
            while (isPaused == true) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ie) {
                    
                }
            }
            hasMultiballs = ballsAlive > 1;
            
            ListIterator objectsList = objects.listIterator();
            while (objectsList.hasNext()) { //parsing through all objects to update and paint
                Object i = objectsList.next();
                if (i instanceof Ball) {
                    if (((Ball)i).checkCollision(ship, this) == true) { //make an adjustment for checkCollision
                        if (hasCatch == true) {
                            ((Ball)i).setFired(false);
                            ((Ball)i).setPositionOnShip(ship.getX() + ship.getActualWidth() /2 - ((Ball)i).getX());
                        } else {
                            playSound("Ship Collision");
                            ship.showColour();
                        }
                    }
                    for (int n = 0; n < blcArray.length; n++) { //checking brick characteristics
                        if (blcArray[n] == null) {
                            continue;
                        }
                        if (((Ball)i).blockCollision(blcArray[n]) == true) { //if there is a collision
                            String soundName;
                            if (blcArray[n].isInvincible() == true) {
                                soundName = "Invincible Brick Collision";
                            } else {
                                soundName = "Brick Collision";
                                score = score + 50;
                            }
                            playSound(soundName);
                            blcArray[n].minushp();
                            if (blcArray[n].gethp() == 0) {
                                generatePower(n);
                                blcArray[n] = null; //deleting the dead brick
                            }
                            if (checkFinished() == true) {
                                try {
                                    isCompleted = true;
                                    this.repaint();
                                    ((Ball)i).setFired(false);
                                    playSound("Victory");
                                    Thread.sleep(5000);
                                    resetGame();
                                    stop();
                                } catch (InterruptedException ie) {
                                }
                            }
                        }
                    }
                    ((Ball)i).update(this);
                    if (((Ball)i).getY() == this.getHeight() - (((Ball)i).getRadius() * 2) +- 1 && ballsAlive == 1) { //if ball dies
                        try {
                            powerReset(); //resets powerups
                            if (ship.numLives() > 0) {
                                Thread.sleep(1250);
                            } else {
                                playSound("Death");
                                Thread.sleep(8500);
                                resetGame(); //resetting the game
                                return;
                            }
                            puppies = new ArrayList<>(); //resets all falling powerups
                            bullets = new ArrayList<>(); //resets all fired lasers
                            ship.reset(this); //resets ship to centre
                            ((Ball)i).reset(ship); //resets ball
                            ((Ball)i).setSpeed(1, -2);
                            ((Ball)i).setFired(false);
                            ship.death(); //ship healthpoints decrease
                        } catch (InterruptedException e) {
                        }
                    } else if (((Ball)i).getY() == this.getHeight() - (((Ball)i).getRadius() * 2) +- 1 && ballsAlive > 1) {
                        ballsAlive--;
                        objectsList.remove();
                    }
                } else if (i instanceof Player) {
                    ((Player)i).update(this);
                }
            }
            
            ListIterator powersList = puppies.listIterator();
            while (powersList.hasNext()) {
                Object p = powersList.next();
                ((PowerUp)p).update(this);
                if (ship.powerupCollision((PowerUp)p) == true) {
                    PowerUp pup = (PowerUp)p;
                    score = score + 100;
                    playSound("Powerup Collected");

                    if (pup.getType().equals("multiballs") && hasMultiballs == false) { //activation of powerup
                        Ball b1 = new Ball((int)(ship.getX() + ship.getActualWidth() / 2 - 5), (int)ship.getY() - 13, 10, Color.WHITE, 1, -2);
                        Ball b2 = new Ball((int)(ship.getX() + ship.getActualWidth() / 2 - 5), (int)ship.getY() - 13, 10, Color.WHITE, -1, -1.5);
                        objects.add(b1);
                        objects.add(b2);
                        b1.setFired(true);
                        b2.setFired(true);
                        ballsAlive = ballsAlive + 2;
                    } else if(((PowerUp)p).getType().equals("enlarge")) { //enlarge timer index 0
                        //enlarge stacks with catch
                        //enlarge stacks with laser
                        TimerTask task = new TimerTask() {
                            @Override
                            public void run() {
                                if (ship.isEnlarged() == true) { 
                                    ship.subside();
                                }
                                hasEnlarge = false;
                            }
                        };
                        if (hasEnlarge == false) {                            
                            ship.grow(); //method to grow
                            hasEnlarge = true;
                        } else { 
                            timers[0].cancel(); //if you collect 'enlarge' while 'enlarge' is active, reset the expiration of enlarge
                            timers[0] = new java.util.Timer("enlarge");
                        }
                        timers[0].schedule(task, 8000); //resets the timer by adding a new task
                    } else if (((PowerUp)p).getType().equals("lasers")) { //lasers timer index 1
                        //laser stacks with enlarge
                        //laser does not stack with catch
                        TimerTask task = new TimerTask() {
                            @Override
                            public void run() {
                                hasLasers = false;
                            }
                        };
                        if (hasLasers == false) {
                            hasLasers = true;
                            hasCatch = false; //ends catch powerup when laser is collected
                            timers[2].cancel();
                            timers[2] = new java.util.Timer("catch");
                        } else {
                            timers[1].cancel();
                            timers[1] = new java.util.Timer("lasers");
                        }
                        timers[1].schedule(task, 10000);
                    }
                    else if (((PowerUp)p).getType().equals("extralife")) {
                        ship.lifeGain(1);
                    } else if (((PowerUp)p).getType().equals("catch")) { //catch timer index 2
                        //catch stacks with enlarge
                        //catch does not stack with laser
                        hasCatch = true;
                        hasLasers = false;
                        TimerTask task = new TimerTask() {
                            @Override
                            public void run() {
                                hasCatch = false;
                            }
                        };
                        if (hasCatch == false) {                            
                            hasCatch = true;
                            hasLasers = false; //ends laser powerup when catch is collected
                            timers[1].cancel();
                            timers[1] = new java.util.Timer("lasers");
                        } else { 
                            timers[2].cancel(); //if you collect 'catch' while 'catch' is active, reset the expiration of catch
                            timers[2] = new java.util.Timer("catch");
                        }
                        timers[2].schedule(task, 15000); //resets the timer by adding a new task
                    }
                    powersList.remove();
                } else if (((PowerUp)p).outOfPanel(this) == true) {
                    powersList.remove();
                }
            }
            
            try {
                ListIterator bulletsList = bullets.listIterator(); //putting iteration in a method to dodge concurrent mod. exceptions
                iterateLasers(bulletsList);
            } catch (ConcurrentModificationException cme) { //if exception is thrown, rerun the iteration
                //System.out.println("Block Collision Error");
                iterateLasers(bullets.listIterator());
            }
            
            panelContainer.repaint();
            gameInfo.repaint();
            repaint();
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
            }
        }
    }    
    
    public void powerReset() {
        hasEnlarge = false; //resets all active powerups
        hasLasers = false;
        hasCatch = false;
        if (ship.isEnlarged() == true) {
            ship.subside();
        }
        for (int z = 3; z < 3; z++) {
            timers[z].cancel();
            timers[z] = new java.util.Timer(); //timers names are irrelevant anyways lol
        }
    }
    public void resetGame() {
        removeMouseListener(mouseInteraction); //resets everything
        removeMouseMotionListener(mousePosition);
        removeKeyListener(keySelected);
        startWindow.setVisible(true); //takes back to main menu
        powerReset();
        isCompleted = false;
        objects = new ArrayList<>();
        bullets = new ArrayList<>();
        puppies = new ArrayList<>();
        blocks = new ArrayList<>();
        blcArray = new Blocks[blocks.size()];
        isGameStarted = false;
        isPaused = false;
        score = 0;
        gameInfo.repaint();
        this.repaint();
    }
    
    public void iterateLasers(ListIterator it) {            
        while (it.hasNext()) { //parses through the fired laser array
            Object b = it.next();
            ((Bullet)b).update(this);
            if (((Bullet)b).getY() + ((Bullet)b).getHeight() < -5) {
                it.remove();
                continue;
            }
            for (int n = 0; n < blcArray.length; n++) { //checking brick characteristics
                if (blcArray[n] == null) {
                    continue;
                }
                if (((Bullet)b).checkCollision(blcArray[n]) == true) {
                    blcArray[n].minushp();
                    if (blcArray[n].gethp() == 0) {
                        generatePower(n);
                        blcArray[n] = null;
                    }
                    it.remove();
                    if (checkFinished() == true) {
                        try {
                            isCompleted = true;
                            this.repaint();
                            for (GameObject obj : objects) {
                                if (obj instanceof Ball) {
                                    ((Ball)obj).setFired(false);
                                }
                            }
                            playSound("Victory");
                            Thread.sleep(5000);
                            resetGame();
                            stop();
                        } catch (InterruptedException ie) {
                        }
                    }
                }
            }
        }
    }
    
    private boolean checkFinished() {
        Boolean gameFinished = true;
        for (int q = 0; q < blcArray.length; q++) {
            if (blcArray[q] != null && blcArray[q].gethp() > 0) {
                gameFinished = false;
                break;
            }
        }
        return gameFinished;
    }
    
    public void moveObjects(MouseEvent e) {
        //moving objects relative to the mouse
        boolean withinBounds = (e.getX() - ship.getActualWidth() / 2) > - 3 && e.getX() + ship.getActualWidth() / 2 < getWidth() + 3;               
        if (withinBounds == true) {
            ship.setPosition((int)(e.getX() - ship.getActualWidth() / 2), getHeight() - 40);
        }
        for (GameObject go : objects) {
            if (go instanceof Ball) {
                try {                    
                    if (((Ball)go).fired() == false) {
                        ((Ball)go).setPosition((int)(ship.getX() + (ship.getActualWidth() / 2) - ((Ball)go).getPositionOnShip()), (int)((Ball)go).getY());
                    }
                } catch (NullPointerException npe) {
                }
            }      
        }
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(30f);
            g.setFont(font);
            g.setColor(Color.WHITE);
            if (isCompleted == true) {
                FontMetrics fm = g.getFontMetrics();
                String scoreText = "Congratulations! You scored: " + score + " points!";
                Rectangle2D fontRect = fm.getStringBounds(scoreText, g2);
 
                int yPos = (this.getHeight() - (int)fontRect.getHeight()) / 2;
                int xPos = (this.getWidth() - (int)fontRect.getWidth()) / 2;
                g2.drawString(scoreText, xPos, yPos);
            }
            for (int n = 0; n < blcArray.length; n++) {
                if (blcArray[n] == null) {
                    continue;
                }
                blcArray[n].paintComponent(g2);
            }
            ListIterator bulletsList = bullets.listIterator();
            while (bulletsList.hasNext()) { //painting all fired bullets
                Object bu = bulletsList.next();
                ((Bullet)bu).paintComponent(g2);
            }
            ListIterator objectsList = objects.listIterator();
            while (objectsList.hasNext()) { //painting all objects
                Object ob = objectsList.next();
                if (ob instanceof Ball) {
                    ((Ball)ob).paintComponent(g2);
                } else if (ob instanceof Player) {
                    ((Player)ob).paintComponent(g2);
                }
            }            
            ListIterator powerList = puppies.listIterator();
            while (powerList.hasNext()) { //painting all falling powerups
                Object p = powerList.next();
                ((PowerUp)p).paintComponent(g2);
            }
        } catch (ConcurrentModificationException | IOException | FontFormatException excpt) {
        }
    }
    
    public class ButtonClicked implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("Start Game")) {
                startWindow.setVisible(false);
                isGameStarted = true;
                startGame();
            } else if (e.getActionCommand().equals("Resume")) {
                isPaused = false;
                pausePanel.setVisible(false);
            } else if (e.getActionCommand().equals("Main Menu")) {
                isPaused = false;
                pausePanel.setVisible(false);
                stop();
                resetGame();
            }
        }
    }
    
    public class MouseInteracted implements MouseListener { 
        public void mouseClicked(MouseEvent e) {}
        
        public void mousePressed(MouseEvent e) {
            for (GameObject go : objects) {
                if (go instanceof Ball) {
                    if (((Ball)go).fired() == false && isPaused == false) {
                        ((Ball)go).setFired(true);
                    }
                }
            }
            if (hasLasers == true && thread.getState() == Thread.State.TIMED_WAITING) { //fires lasers, one on left, one on right
                Bullet leftShot = new Bullet((int)(ship.getX() + ship.circleRadius() - 2), (int)ship.getY() - 3, 4, 20, Color.RED);
                Bullet rightShot = new Bullet((int)(ship.getX() + ship.getActualWidth() - ship.circleRadius() - 2), (int)ship.getY() - 3, 4, 20, Color.RED);
                playSound("Laser Fired");
                ListIterator bulletsList = bullets.listIterator();
                bulletsList.add(leftShot);
                bulletsList.add(rightShot);
            }
        }

        public void mouseReleased(MouseEvent e) {}
        
        public void mouseEntered(MouseEvent e) {}

        public void mouseExited(MouseEvent e) {}
    }
    
    public class MouseHoverOver implements MouseListener {
        private String name = "";
        private String text = "";
        
        public MouseHoverOver(String name) { //constructor for MouseListener
            this.name = name;
            BufferedReader inputStream = null;
            if (!name.equals("empty")) {
                try {
                    inputStream = new BufferedReader(new FileReader(new File(".\\src\\texts\\descrip_" + name + ".txt")));
                    text = inputStream.readLine();
                } catch (IOException ioe) {
                }
            }
        }
        
        public void mouseClicked(MouseEvent e) {}
        
        public void mousePressed(MouseEvent e) {}
        
        public void mouseReleased(MouseEvent e) {}
        
        public void mouseEntered(MouseEvent e) {
            if (!name.equals("empty") && isGameStarted == false) {
                infoField.setText(text); //if mouse hovers over, show text relating to the icon
            }
        }

        public void mouseExited(MouseEvent e) {
            if (isGameStarted == false) {
                infoField.setText(infoFieldDefaultText); //reset text to default when mouse moves off the icon
            }
        }
    }
    
    public class KeySelected implements KeyListener {
        public void keyTyped(KeyEvent e) {}
        
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                for (GameObject go : objects) {
                    if (go instanceof Ball) {
                        if (((Ball)go).fired() == false) {
                            ((Ball)go).setFired(true); 
                        }
                    }
                }
            }
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                if (isPaused == true) {
                    isPaused = false;
                    pausePanel.setVisible(false);
                } else {
                    isPaused = true;
                    pausePanel.setVisible(true);
                }
            }
        }
        public void keyReleased(KeyEvent e) {}
    }
    
    public class MousePosition implements MouseMotionListener {
        public void mouseDragged(MouseEvent e) {
            moveObjects(e);
        }

        public void mouseMoved(MouseEvent e) {
            moveObjects(e);
        }
    }
  
    public static void main(String[] args) {
        Arkanoid game = new Arkanoid();
    }
}