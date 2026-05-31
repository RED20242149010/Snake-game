import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;
import java.util.Random;

public class SnakeGame extends JPanel implements ActionListener, KeyListener {

    // Constants
    private static final int WIDTH = 600;
    private static final int HEIGHT = 600;
    private static final int UNIT_SIZE = 20;
    private static final int DELAY = 100;

    // Colors
    private static final Color BACKGROUND_COLOR = new Color(15, 15, 15);
    private static final Color SNAKE_HEAD_COLOR = new Color(50, 205, 50);
    private static final Color SNAKE_BODY_COLOR = new Color(34, 139, 34);
    private static final Color FOOD_COLOR = new Color(220, 20, 60);
    private static final Color SCORE_COLOR = new Color(255, 255, 255);
    private static final Color GRID_COLOR = new Color(25, 25, 25);

    // Game State
    private LinkedList<Point> snake;
    private Point food;
    private int direction; // 0=UP, 1=DOWN, 2=LEFT, 3=RIGHT
    private int nextDirection;
    private boolean running;
    private boolean paused;
    private int score;
    private int highScore;
    private Timer timer;
    private Random random;

    // Constructor
    public SnakeGame() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(BACKGROUND_COLOR);
        setFocusable(true);
        addKeyListener(this);
        random = new Random();
        highScore = 0;
        initGame();
    }

    // Initialize / Reset Game
    private void initGame() {
        snake = new LinkedList<>();
        snake.add(new Point(WIDTH / 2 / UNIT_SIZE, HEIGHT / 2 / UNIT_SIZE));
        snake.add(new Point(WIDTH / 2 / UNIT_SIZE - 1, HEIGHT / 2 / UNIT_SIZE));
        snake.add(new Point(WIDTH / 2 / UNIT_SIZE - 2, HEIGHT / 2 / UNIT_SIZE));

        direction = 3; // Start moving RIGHT
        nextDirection = 3;
        score = 0;
        running = true;
        paused = false;

        spawnFood();

        if (timer != null) {
            timer.stop();
        }
        timer = new Timer(DELAY, this);
        timer.start();
    }

    // Spawn food at random position not on snake
    private void spawnFood() {
        int cols = WIDTH / UNIT_SIZE;
        int rows = HEIGHT / UNIT_SIZE;
        Point newFood;
        do {
            newFood = new Point(random.nextInt(cols), random.nextInt(rows));
        } while (snake.contains(newFood));
        food = newFood;
    }

    // Game update logic
    private void update() {
        if (!running || paused) return;

        direction = nextDirection;

        // Get current head position
        Point head = snake.getFirst();
        Point newHead;

        switch (direction) {
            case 0: newHead = new Point(head.x, head.y - 1); break; // UP
            case 1: newHead = new Point(head.x, head.y + 1); break; // DOWN
            case 2: newHead = new Point(head.x - 1, head.y); break; // LEFT
            default: newHead = new Point(head.x + 1, head.y); break; // RIGHT
        }

        // Check wall collision
        if (newHead.x < 0 || newHead.x >= WIDTH / UNIT_SIZE ||
                newHead.y < 0 || newHead.y >= HEIGHT / UNIT_SIZE) {
            gameOver();
            return;
        }

        // Check self collision
        if (snake.contains(newHead)) {
            gameOver();
            return;
        }

        // Move snake
        snake.addFirst(newHead);

        // Check food collision
        if (newHead.equals(food)) {
            score += 10;
            if (score > highScore) highScore = score;
            spawnFood();
        } else {
            snake.removeLast(); // Remove tail if no food eaten
        }
    }

    // Game Over
    private void gameOver() {
        running = false;
        timer.stop();
    }

    // Paint the game
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw grid
        drawGrid(g2d);

        if (running) {
            // Draw food
            drawFood(g2d);

            // Draw snake
            drawSnake(g2d);

            // Draw score
            drawScore(g2d);

            // Draw paused overlay
            if (paused) {
                drawPaused(g2d);
            }
        } else {
            // Draw game over screen
            drawGameOver(g2d);
        }
    }

    // Draw grid lines
    private void drawGrid(Graphics2D g2d) {
        g2d.setColor(GRID_COLOR);
        for (int x = 0; x < WIDTH; x += UNIT_SIZE) {
            g2d.drawLine(x, 0, x, HEIGHT);
        }
        for (int y = 0; y < HEIGHT; y += UNIT_SIZE) {
            g2d.drawLine(0, y, WIDTH, y);
        }
    }

    // Draw food
    private void drawFood(Graphics2D g2d) {
        int x = food.x * UNIT_SIZE;
        int y = food.y * UNIT_SIZE;

        // Glow effect
        g2d.setColor(new Color(220, 20, 60, 60));
        g2d.fillOval(x - 4, y - 4, UNIT_SIZE + 8, UNIT_SIZE + 8);

        // Food circle
        g2d.setColor(FOOD_COLOR);
        g2d.fillOval(x + 2, y + 2, UNIT_SIZE - 4, UNIT_SIZE - 4);

        // Shine effect
        g2d.setColor(new Color(255, 150, 150));
        g2d.fillOval(x + 4, y + 4, 5, 5);
    }

    // Draw snake
    private void drawSnake(Graphics2D g2d) {
        for (int i = 0; i < snake.size(); i++) {
            Point p = snake.get(i);
            int x = p.x * UNIT_SIZE;
            int y = p.y * UNIT_SIZE;

            if (i == 0) {
                // Head
                g2d.setColor(SNAKE_HEAD_COLOR);
                g2d.fillRoundRect(x + 1, y + 1, UNIT_SIZE - 2, UNIT_SIZE - 2, 8, 8);

                // Eyes
                g2d.setColor(Color.WHITE);
                drawEyes(g2d, x, y);
            } else {
                // Body gradient
                float ratio = (float) i / snake.size();
                int green = (int) (139 + (34 - 139) * ratio);
                g2d.setColor(new Color(34, Math.max(green, 34), 34));
                g2d.fillRoundRect(x + 1, y + 1, UNIT_SIZE - 2, UNIT_SIZE - 2, 6, 6);
            }
        }
    }

    // Draw snake eyes based on direction
    private void drawEyes(Graphics2D g2d, int x, int y) {
        int eyeSize = 4;
        int pupilSize = 2;

        int eye1X, eye1Y, eye2X, eye2Y;

        switch (direction) {
            case 0: // UP
                eye1X = x + 4; eye1Y = y + 4;
                eye2X = x + 12; eye2Y = y + 4;
                break;
            case 1: // DOWN
                eye1X = x + 4; eye1Y = y + 12;
                eye2X = x + 12; eye2Y = y + 12;
                break;
            case 2: // LEFT
                eye1X = x + 4; eye1Y = y + 4;
                eye2X = x + 4; eye2Y = y + 12;
                break;
            default: // RIGHT
                eye1X = x + 12; eye1Y = y + 4;
                eye2X = x + 12; eye2Y = y + 12;
                break;
        }

        // White of eyes
        g2d.setColor(Color.WHITE);
        g2d.fillOval(eye1X, eye1Y, eyeSize, eyeSize);
        g2d.fillOval(eye2X, eye2Y, eyeSize, eyeSize);

        // Pupils
        g2d.setColor(Color.BLACK);
        g2d.fillOval(eye1X + 1, eye1Y + 1, pupilSize, pupilSize);
        g2d.fillOval(eye2X + 1, eye2Y + 1, pupilSize, pupilSize);
    }

    // Draw score on screen
    private void drawScore(Graphics2D g2d) {
        g2d.setColor(SCORE_COLOR);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("Score: " + score, 10, 22);
        g2d.drawString("High Score: " + highScore, WIDTH - 160, 22);

        // Snake length
        g2d.setFont(new Font("Arial", Font.PLAIN, 13));
        g2d.setColor(new Color(150, 150, 150));
        g2d.drawString("Length: " + snake.size(), 10, 42);
    }

    // Draw paused overlay
    private void drawPaused(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font("Arial", Font.BOLD, 48));
        FontMetrics fm = g2d.getFontMetrics();
        String text = "PAUSED";
        int x = (WIDTH - fm.stringWidth(text)) / 2;
        g2d.drawString(text, x, HEIGHT / 2);

        g2d.setFont(new Font("Arial", Font.PLAIN, 20));
        fm = g2d.getFontMetrics();
        String sub = "Press P to Resume";
        x = (WIDTH - fm.stringWidth(sub)) / 2;
        g2d.setColor(Color.WHITE);
        g2d.drawString(sub, x, HEIGHT / 2 + 40);
    }

    // Draw game over screen
    private void drawGameOver(Graphics2D g2d) {
        // Dark overlay
        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        // Draw dead snake remains
        drawSnake(g2d);

        // Game Over Text
        g2d.setColor(new Color(220, 20, 60));
        g2d.setFont(new Font("Arial", Font.BOLD, 56));
        FontMetrics fm = g2d.getFontMetrics();
        String gameOverText = "GAME OVER";
        int x = (WIDTH - fm.stringWidth(gameOverText)) / 2;
        g2d.drawString(gameOverText, x, HEIGHT / 2 - 60);

        // Score
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        fm = g2d.getFontMetrics();
        String scoreText = "Score: " + score;
        x = (WIDTH - fm.stringWidth(scoreText)) / 2;
        g2d.drawString(scoreText, x, HEIGHT / 2);

        // High Score
        g2d.setColor(new Color(255, 215, 0));
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        fm = g2d.getFontMetrics();
        String highText = "High Score: " + highScore;
        x = (WIDTH - fm.stringWidth(highText)) / 2;
        g2d.drawString(highText, x, HEIGHT / 2 + 35);

        // Restart instruction
        g2d.setColor(new Color(150, 255, 150));
        g2d.setFont(new Font("Arial", Font.PLAIN, 18));
        fm = g2d.getFontMetrics();
        String restartText = "Press ENTER to Play Again";
        x = (WIDTH - fm.stringWidth(restartText)) / 2;
        g2d.drawString(restartText, x, HEIGHT / 2 + 80);

        // Controls hint
        g2d.setColor(new Color(120, 120, 120));
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        fm = g2d.getFontMetrics();
        String controlsText = "Arrow Keys / WASD to move | P to Pause";
        x = (WIDTH - fm.stringWidth(controlsText)) / 2;
        g2d.drawString(controlsText, x, HEIGHT - 20);
    }

    // Timer action (game loop)
    @Override
    public void actionPerformed(ActionEvent e) {
        update();
        repaint();
    }

    // Key press handler
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        // Direction controls (prevent reversing)
        switch (key) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                if (direction != 1) nextDirection = 0;
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                if (direction != 0) nextDirection = 1;
                break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                if (direction != 3) nextDirection = 2;
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                if (direction != 2) nextDirection = 3;
                break;

            // Pause
            case KeyEvent.VK_P:
                if (running) {
                    paused = !paused;
                    if (!paused) {
                        timer.start();
                    }
                }
                break;

            // Restart
            case KeyEvent.VK_ENTER:
                if (!running) {
                    initGame();
                }
                break;
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    // Main method - Entry point
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("🐍 Snake Game");
            SnakeGame game = new SnakeGame();

            frame.add(game);
            frame.pack();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);
            frame.setVisible(true);
        });
    }
}