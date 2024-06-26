import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class GraphicsPanel extends JPanel implements KeyListener, MouseListener, ActionListener {
    private BufferedImage background;
    private BufferedImage background2;
    private Player player;
    private boolean[] pressedKeys;
    private ArrayList<Coin> coins;
    private Timer timer;
    private int time;
    private JButton resetGame;
    private JButton pauseGame;
    private boolean paused;
    private boolean win;
    private boolean lose;

    public GraphicsPanel(String name) {
        try {
            background = ImageIO.read(new File("src/background.png"));
            background2 = ImageIO.read(new File("src/background2.png"));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        player = new Player("src/marioleft.png", "src/marioright.png", name);
        coins = new ArrayList<>();
        pressedKeys = new boolean[128];
        time = 0;
        timer = new Timer(1000, this); // this Timer will call the actionPerformed interface method every 1000ms = 1 second
        timer.start();
        resetGame = new JButton("Reset Game");
        resetGame.setFocusable(false);
        add(resetGame);
        resetGame.addActionListener(this);
        pauseGame = new JButton("Pause");
        pauseGame.setFocusable(false);
        add(pauseGame);
        pauseGame.addActionListener(this);
        addKeyListener(this);
        addMouseListener(this);
        setFocusable(true); // this line of code + one below makes this panel active for keylistener events
        requestFocusInWindow(); // see comment above
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);  // just do this
        if (player.getScore() >= 10) {
            g.drawImage(background2, 0, 0, null);
            player.setLeft("src/mariofrogleft.png");
            player.setRight("src/mariofrogright.png");
        } else {
            g.drawImage(background, 0, 0, null);  // the order that things get "painted" matter; we put background down first
            player.setLeft("src/marioleft.png");
            player.setRight("src/marioright.png");
        }
        g.drawImage(player.getPlayerImage(), player.getxCoord(), player.getyCoord(), null);
        if (paused) {
            g.setFont(new Font("Arial", Font.BOLD, 64));
            g.setColor(Color.DARK_GRAY);
            if (win) {
                g.drawString("YOU WIN!", 400, 200);
            } else if (lose) {
                g.drawString("GAME OVER", 400, 200);
            } else {
                g.drawString("PAUSED", 400, 200);
            }
        }

        // this loop does two things:  it draws each Coin that gets placed with mouse clicks,
        // and it also checks if the player has "intersected" (collided with) the Coin, and if so,
        // the score goes up and the Coin is removed from the arraylist
        for (int i = 0; i < coins.size(); i++) {
            Coin coin = coins.get(i);
            g.drawImage(coin.getImage(), coin.getxCoord(), coin.getyCoord(), null); // draw Coin
            if (player.playerRect().intersects(coin.coinRect())) { // check for collision
                if (coin instanceof SpikedBall) {
                    lose = true;
                    paused = true;
                } else {
                    player.collectCoin();
                    if (player.getScore() >= 20) {
                        win = true;
                        paused = true;
                    }
                }
                coins.remove(i);
                i--;
            }
        }

        // draw score
        g.setFont(new Font("Courier New", Font.BOLD, 24));
        g.drawString(player.getName() + "'s Score: " + player.getScore(), 20, 40);
        g.drawString("Time: " + time, 20, 70);
        resetGame.setLocation(20, 80);
        pauseGame.setLocation(20, 110);

        if (!paused) {
            // player moves left (A)
            if (pressedKeys[65]) {
                player.faceLeft();
                player.moveLeft();
            }

            // player moves right (D)
            if (pressedKeys[68]) {
                player.faceRight();
                player.moveRight();
            }

            // player moves up (W)
            if (pressedKeys[87]) {
                player.moveUp();
            }

            // player moves down (S)
            if (pressedKeys[83]) {
                player.moveDown();
            }
        }
    }

    // ----- KeyListener interface methods -----
    public void keyTyped(KeyEvent e) { } // unimplemented

    public void keyPressed(KeyEvent e) {
        // see this for all keycodes: https://stackoverflow.com/questions/15313469/java-keyboard-keycodes-list
        // A = 65, D = 68, S = 83, W = 87, left = 37, up = 38, right = 39, down = 40, space = 32, enter = 10
        int key = e.getKeyCode();
        pressedKeys[key] = true;
    }

    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        pressedKeys[key] = false;
    }

    // ----- MouseListener interface methods -----
    public void mouseClicked(MouseEvent e) { }  // unimplemented; if you move your mouse while clicking,
    // this method isn't called, so mouseReleased is best

    public void mousePressed(MouseEvent e) { } // unimplemented

    public void mouseReleased(MouseEvent e) {
        if (!paused) {
            if (e.getButton() == MouseEvent.BUTTON1) {  // left mouse click
                Point mouseClickLocation = e.getPoint();
                if (Math.random() < 0.75) {
                    Coin coin = new Coin(mouseClickLocation.x, mouseClickLocation.y, "src/coin.png");
                    coins.add(coin);
                } else {
                    SpikedBall ball = new SpikedBall(mouseClickLocation.x, mouseClickLocation.y, "src/spikedball.png");
                    coins.add(ball);
                }
            } else {
                Point mouseClickLocation = e.getPoint();
                if (player.playerRect().contains(mouseClickLocation)) {
                    player.turn();
                }
            }
        }
    }

    public void mouseEntered(MouseEvent e) { } // unimplemented

    public void mouseExited(MouseEvent e) { } // unimplemented

    // ACTIONLISTENER INTERFACE METHODS: used for buttons AND timers!
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof Timer) {
            if (!paused) {
                time++;
            }
        } else if (e.getSource() instanceof JButton) {
            JButton button = (JButton) e.getSource();
            if (button == resetGame) {
                player.setScore(0);
                player.setxCoord(50);
                player.setyCoord(435);
                player.faceRight();
                coins.clear();
                paused = false;
                win = false;
                lose = false;
            } else {
                if (!win && !lose) {
                    paused = !paused;
                }
            }
        }
    }
}
