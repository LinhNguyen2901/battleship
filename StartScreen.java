// Game start menu allowing players to choose between Human vs Human or
// Human vs Computer game modes before ship placement begins

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class StartScreen extends JFrame {

    private BufferedImage backgroundImage;
    private Player player1;
    private Player player2;
    private GameController controller;
    private boolean vsComputer;

    public StartScreen() {
        setTitle("Battleship - Start");
        setSize(1200, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        loadBackgroundImage();

        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                if (backgroundImage != null) {
                    g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null);
                } else {
                    GradientPaint gradient = new GradientPaint(
                            0, 0, new Color(20, 100, 180),
                            getWidth(), getHeight(), new Color(10, 50, 100));
                    g2d.setPaint(gradient);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }

                // semi-transparent overlay
                g2d.setColor(new Color(0, 0, 0, 120));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        mainPanel.setLayout(new BorderLayout());

        // Buttons container
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(60, 0, 80, 0));

        JButton humanButton = createStyledButton("Human vs Human", false);
        JButton computerButton = createStyledButton("Play vs Computer", true);

        humanButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        computerButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        humanButton.setMaximumSize(new Dimension(360, 48));
        computerButton.setMaximumSize(new Dimension(360, 48));

        buttonPanel.add(Box.createVerticalGlue());
        buttonPanel.add(humanButton);
        buttonPanel.add(Box.createVerticalStrut(20));
        buttonPanel.add(computerButton);
        buttonPanel.add(Box.createVerticalGlue());

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        setVisible(true);
    }

    private void loadBackgroundImage() {
        try {
            File imageFile = new File("images/battleship.jpg");
            if (imageFile.exists()) {
                backgroundImage = ImageIO.read(imageFile);
            }
        } catch (Exception ignored) {}
    }

    private JButton createStyledButton(String text, boolean vsComputer) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                if (getModel().isArmed()) {
                    g.setColor(new Color(100, 200, 255));
                } else if (getModel().isRollover()) {
                    g.setColor(new Color(80, 180, 240));
                } else {
                    g.setColor(new Color(50, 150, 220));
                }
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                super.paintComponent(g);
            }
        };

        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setForeground(Color.WHITE);
        button.setContentAreaFilled(false);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setPreferredSize(new Dimension(300, 48));
        button.setMargin(new Insets(6, 12, 6, 12));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addActionListener(e -> startGame(vsComputer));

        return button;
    }

    private void startGame(boolean vsComp) {
        this.vsComputer = vsComp;
        this.dispose();
        
        player1 = new HumanPlayer();
        player2 = vsComputer ? new ComputerPlayer() : new HumanPlayer();
        controller = new GameController(player1, player2);
        
        // DON'T call setupGame() - we'll place ships manually
        // controller.setupGame();
        
        // Start player 1 placement
        showPlayer1Placement();
    }
    
    private void showPlayer1Placement() {
        new ShipPlacementScreen(true, () -> {
            // Store reference to the screen BEFORE it closes
            ShipPlacementScreen currentScreen = null;
            for (Window window : Window.getWindows()) {
                if (window instanceof ShipPlacementScreen && window.isVisible()) {
                    currentScreen = (ShipPlacementScreen) window;
                    break;
                }
            }
            
            if (currentScreen != null) {
                // Copy ships to player1
                for (Ship s : currentScreen.getShips()) {
                    player1.addShip(s);
                    player1.getBoard().placeShip(s.getStartRow(), s.getStartCol(), 
                        s.getLength(), s.isHorizontal());
                }
            }
            
            // Move to player 2 or start game
            if (vsComputer) {
                // Computer places ships automatically
                player2.placeShipsAutomatically();
                startGameWindow();
            } else {
                // Human player 2 places ships manually
                showPlayer2Placement();
            }
        });
    }
    
    private void showPlayer2Placement() {
        new ShipPlacementScreen(false, () -> {
            // Store reference to the screen BEFORE it closes
            ShipPlacementScreen currentScreen = null;
            for (Window window : Window.getWindows()) {
                if (window instanceof ShipPlacementScreen && window.isVisible()) {
                    currentScreen = (ShipPlacementScreen) window;
                    break;
                }
            }
            
            if (currentScreen != null) {
                // Copy ships to player2
                for (Ship s : currentScreen.getShips()) {
                    player2.addShip(s);
                    player2.getBoard().placeShip(s.getStartRow(), s.getStartCol(), 
                        s.getLength(), s.isHorizontal());
                }
            }
            
            startGameWindow();
        });
    }
    
    private void startGameWindow() {
        SwingUtilities.invokeLater(() -> {
            GameWindow window = new GameWindow(controller);
            window.setVisible(true);
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(StartScreen::new);
    }
}