package pacman;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class PacMan extends JPanel implements ActionListener, KeyListener {
    class Block {
        int x;
        int y;
        int width;
        int height;
        Image image;

        int startX;
        int startY;
        char direction = 'U'; 
        int velocityX = 0;
        int velocityY = 0;
        boolean isPacMan = false;

        Block(Image image, int x, int y, int width, int height) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.startX = x;
            this.startY = y;
        }

        void updateDirection(char direction) {
            char prevDirection = this.direction;
            this.direction = direction;
            updateVelocity();
            this.x += this.velocityX;
            this.y += this.velocityY;
            for (Block wall : walls) {
                if (collision(this, wall)) {
                    this.x -= this.velocityX;
                    this.y -= this.velocityY;
                    this.direction = prevDirection;
                    updateVelocity();
                }
            }
        }

        void updateVelocity() {
            int speed = tileSize / 4;
            if (isPacMan && isPowerUpActive) speed *= 2;
            
            switch (this.direction) {
                case 'U' -> {
                    velocityX = 0;
                    velocityY = -speed;
                }
                case 'D' -> {
                    velocityX = 0;
                    velocityY = speed;
                }
                case 'L' -> {
                    velocityX = -speed;
                    velocityY = 0;
                }
                case 'R' -> {
                    velocityX = speed;
                    velocityY = 0;
                }
            }
        }

        void reset() {
            this.x = startX;
            this.y = startY;
        }
    }

    private final int rowCount = 21;
    private final int columnCount = 19;
    private final int tileSize = 32;
    private final int boardWidth = columnCount * tileSize;
    private final int boardHeight = rowCount * tileSize;

    private Image wallImage;
    private Image blueGhostImage;
    private Image orangeGhostImage;
    private Image pinkGhostImage;
    private Image redGhostImage;
    private Image cherryImage;

    private Image pacmanUpImage;
    private Image pacmanDownImage;
    private Image pacmanLeftImage;
    private Image pacmanRightImage;

    private final String[] tileMap = {
        "0XX00X     O    X00",
        "0   O    X        0",
        "X X0 XXX X XXX XX X",
        "X    O     O    O X",
        "0 XX X XXXXX X XX 0",
        "X O  X   O   X    X",
        "XXXX XXXX XXXX XXXX",
        "OOOX X   O   X XOOO",
        "XXXX X XXrXX X XXXX",
        "O    O  bpo   O   O",
        "XXXX X XXXXX X XXXX",
        "OOOX X   O   X XOOO",
        "XXXX X XXXXX X XXXX",
        "X O O    X   O    X",
        "X XX XXX X XXX XX X",
        "X  X  O  P )   X  X",
        "XX X X XXXXX X X XX",
        "0    X   X   X    0",
        "X XXXXXX X XXXXXX X",
        "0   O   O   O     0",
        "0XX  X     O    X0 "
    };

    private ArrayList<Block> walls;
    private ArrayList<Block> foods;
    private ArrayList<Block> ghosts;
    private ArrayList<Block> cherries;
    private Block pacman;

    private Timer gameLoop;
    private Timer powerUpTimer;
    private final char[] directions = {'U', 'D', 'L', 'R'};
    private final Random random = new Random();
    
    private int score = 0;
    private int lives = 3;
    private boolean gameOver = false;
    private boolean isPowerUpActive = false;

    public PacMan() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);

        // Load images
        wallImage = new ImageIcon(getClass().getResource("./wall.png")).getImage();
        blueGhostImage = new ImageIcon(getClass().getResource("./blueGhost.png")).getImage();
        orangeGhostImage = new ImageIcon(getClass().getResource("./orangeGhost.png")).getImage();
        pinkGhostImage = new ImageIcon(getClass().getResource("./pinkGhost.png")).getImage();
        redGhostImage = new ImageIcon(getClass().getResource("./redGhost.png")).getImage();
        cherryImage = new ImageIcon(getClass().getResource("./cherry.png")).getImage();

        pacmanUpImage = new ImageIcon(getClass().getResource("./pacmanUp.png")).getImage();
        pacmanDownImage = new ImageIcon(getClass().getResource("./pacmanDown.png")).getImage();
        pacmanLeftImage = new ImageIcon(getClass().getResource("./pacmanLeft.png")).getImage();
        pacmanRightImage = new ImageIcon(getClass().getResource("./pacmanRight.png")).getImage();

        loadMap();
        initializeGhosts();

        gameLoop = new Timer(50, this);
        gameLoop.start();
    }

    private void loadMap() {
        walls = new ArrayList<>();
        foods = new ArrayList<>();
        ghosts = new ArrayList<>();
        cherries = new ArrayList<>();

        for (int r = 0; r < rowCount; r++) {
            String row = tileMap[r];
            for (int c = 0; c < columnCount; c++) {
                char tile = row.charAt(c);
                int x = c * tileSize;
                int y = r * tileSize;

                switch (tile) {
                    case 'X' -> walls.add(new Block(wallImage, x, y, tileSize, tileSize));
                    case 'b' -> ghosts.add(new Block(blueGhostImage, x, y, tileSize, tileSize));
                    case 'o' -> ghosts.add(new Block(orangeGhostImage, x, y, tileSize, tileSize));
                    case 'p' -> ghosts.add(new Block(pinkGhostImage, x, y, tileSize, tileSize));
                    case 'r' -> ghosts.add(new Block(redGhostImage, x, y, tileSize, tileSize));
                    case 'P' -> {
                        pacman = new Block(pacmanRightImage, x, y, tileSize, tileSize);
                        pacman.isPacMan = true;
                    }
                    case ' ' -> foods.add(new Block(null, x + 14, y + 14, 4, 4));
                    case 'O' -> cherries.add(new Block(cherryImage, x, y, tileSize, tileSize));
                }
            }
        }
    }

    private void initializeGhosts() {
        for (Block ghost : ghosts) {
            char dir = directions[random.nextInt(4)];
            ghost.updateDirection(dir);
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawGame(g);
    }

    private void drawGame(Graphics g) {
        // Draw Pac-Man
        g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);

        // Draw entities
        ghosts.forEach(ghost -> g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null));
        walls.forEach(wall -> g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null));
        
        // Draw food
        g.setColor(Color.WHITE);
        foods.forEach(food -> g.fillRect(food.x, food.y, food.width, food.height));
        
        // Draw cherries
        cherries.forEach(cherry -> g.drawImage(cherry.image, cherry.x, cherry.y, cherry.width, cherry.height, null));

        // Draw UI
        g.setFont(new Font("Arial", Font.BOLD, 16));
        String status = gameOver ? 
            "Game Over! Score: " + score : 
            String.format("Lives: %d  Score: %d", lives, score);
        g.setColor(Color.YELLOW);
        g.drawString(status, 10, 20);
    }

    private void move() {
        // Update Pac-Man position
        pacman.x += pacman.velocityX;
        pacman.y += pacman.velocityY;

        // Screen wrapping
        handleWrapping(pacman);
        handleCollisions();
        moveGhosts();
        checkFoodCollision();
        checkCherryCollision();
        checkGameState();
    }

    private void handleWrapping(Block entity) {
        if (entity.x < 0) entity.x = boardWidth - tileSize;
        if (entity.x >= boardWidth) entity.x = 0;
        if (entity.y < 0) entity.y = boardHeight - tileSize;
        if (entity.y >= boardHeight) entity.y = 0;
    }

    private void handleCollisions() {
        // Wall collision
        for (Block wall : walls) {
            if (collision(pacman, wall)) {
                pacman.x -= pacman.velocityX;
                pacman.y -= pacman.velocityY;
                break;
            }
        }

        // Ghost collision
        for (Block ghost : ghosts) {
            if (collision(ghost, pacman)) {
                if (isPowerUpActive) {
                    ghost.reset();
                    score += 200;
                } else {
                    handleLifeLoss();
                }
            }
        }
    }

    private void handleLifeLoss() {
        lives--;
        if (lives <= 0) {
            gameOver = true;
        } else {
            resetPositions();
        }
    }

    private void moveGhosts() {
        for (Block ghost : ghosts) {
            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;
            handleWrapping(ghost);

            for (Block wall : walls) {
                if (collision(ghost, wall)) {
                    ghost.x -= ghost.velocityX;
                    ghost.y -= ghost.velocityY;
                    ghost.updateDirection(directions[random.nextInt(4)]);
                }
            }
        }
    }

    private void checkFoodCollision() {
        foods.removeIf(food -> {
            if (collision(pacman, food)) {
                score += 10;
                return true;
            }
            return false;
        });
    }

    private void checkCherryCollision() {
        cherries.removeIf(cherry -> {
            if (collision(pacman, cherry)) {
                activatePowerUp();
                score += 100;
                return true;
            }
            return false;
        });
    }

    private void activatePowerUp() {
        isPowerUpActive = true;
        if (powerUpTimer != null) powerUpTimer.stop();
        powerUpTimer = new Timer(5000, e -> { // 30-second power-up
            isPowerUpActive = false;
            powerUpTimer.stop();
        });
        powerUpTimer.start();
    }

    private void checkGameState() {
        if (foods.isEmpty()) {
            loadMap();
            resetPositions();
        }
    }

    private boolean collision(Block a, Block b) {
        return a.x < b.x + b.width &&
               a.x + a.width > b.x &&
               a.y < b.y + b.height &&
               a.y + a.height > b.y;
    }

    private void resetPositions() {
        pacman.reset();
        pacman.velocityX = 0;
        pacman.velocityY = 0;
        ghosts.forEach(Block::reset);
        initializeGhosts();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) move();
        repaint();
        if (gameOver) stopAllTimers();
    }

    private void stopAllTimers() {
        gameLoop.stop();
        if (powerUpTimer != null) powerUpTimer.stop();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (gameOver) {
            resetGame();
            return;
        }

        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP -> pacman.updateDirection('U');
            case KeyEvent.VK_DOWN -> pacman.updateDirection('D');
            case KeyEvent.VK_LEFT -> pacman.updateDirection('L');
            case KeyEvent.VK_RIGHT -> pacman.updateDirection('R');
        }

        updatePacmanImage();
    }

    private void resetGame() {
        loadMap();
        resetPositions();
        lives = 3;
        score = 0;
        isPowerUpActive = false;
        gameOver = false;
        gameLoop.start();
    }

    private void updatePacmanImage() {
        switch (pacman.direction) {
            case 'U' -> pacman.image = pacmanUpImage;
            case 'D' -> pacman.image = pacmanDownImage;
            case 'L' -> pacman.image = pacmanLeftImage;
            case 'R' -> pacman.image = pacmanRightImage;
        }
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyPressed(KeyEvent e) {}
}