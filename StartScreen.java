// Menu screen:Allows players to choose between Human vs Human or Human vs COmputer

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
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
        JButton humanButton = createStyledButton("Human vs Human", false, false);
        JButton computerButton = createStyledButton("Play vs Computer", true, false);
        JButton loadButton = createStyledButton("Load Saved Game", false, true);
        humanButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        computerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loadButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        humanButton.setMaximumSize(new Dimension(360, 48));
        computerButton.setMaximumSize(new Dimension(360, 48));
        loadButton.setMaximumSize(new Dimension(360, 48));
        buttonPanel.add(Box.createVerticalGlue());
        buttonPanel.add(humanButton);
        buttonPanel.add(Box.createVerticalStrut(20));
        buttonPanel.add(computerButton);
        buttonPanel.add(Box.createVerticalStrut(20));
        buttonPanel.add(loadButton);
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
    //style the button 
    private JButton createStyledButton(String text, boolean vsComputer, boolean isLoad) {
        JButton button = new JButton(text) {
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
        if (isLoad) {
            button.addActionListener(e -> loadGame());
        } else {
            button.addActionListener(e -> startGame(vsComputer));
        }
        return button;
    }

    //start game logic (default 1st player to start)
    private void startGame(boolean vsComp) {
        this.vsComputer = vsComp;
        this.dispose();
        player1 = new HumanPlayer();
        player2 = vsComputer ? new ComputerPlayer() : new HumanPlayer();
        controller = new GameController(player1, player2);
        showPlayer1Placement();
    }
    //load game logic
    private void loadGame() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Load Game");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Battleship Save Files", "bsg"));
            int userSelection = fileChooser.showOpenDialog(this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToLoad = fileChooser.getSelectedFile();
                // Ask user to choose game mode
                Object[] options = {"Human vs Human", "Human vs Computer"};
                int choice = JOptionPane.showOptionDialog(this,
                    "Choose game mode for loaded game:",
                    "Select Mode",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]);
                if (choice == JOptionPane.CLOSED_OPTION) {
                    return; 
                }
                boolean isVsComputer = (choice == 1);
                BufferedReader reader = new BufferedReader(new FileReader(fileToLoad));
                // Create players based on user choice 
                Player p1 = new HumanPlayer();
                Player p2 = isVsComputer ? new ComputerPlayer() : new HumanPlayer();
                String line;
                int currentPlayerNum = 1;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("CURRENT_PLAYER:")) {
                        currentPlayerNum = Integer.parseInt(line.substring("CURRENT_PLAYER:".length()));
                    } else if (line.equals("PLAYER_1_START")) {
                        loadPlayerState(reader, p1);
                    } else if (line.equals("PLAYER_2_START")) {
                        loadPlayerState(reader, p2);
                    }
                }
                reader.close();
                GameController loadedController = new GameController(p1, p2);
                // Set current player
                while (loadedController.getCurrentPlayer() != (currentPlayerNum == 1 ? p1 : p2)) {
                    loadedController.switchPlayers();
                }
                this.dispose();
                SwingUtilities.invokeLater(() -> {
                    GameWindow window = new GameWindow(loadedController);
                    window.setVisible(true);
                });
                JOptionPane.showMessageDialog(null, "Game loaded successfully!");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading game: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    //load player ship infor
    private void loadPlayerState(BufferedReader reader, Player player) throws IOException {
        String line;
        line = reader.readLine();
        int shipsCount = Integer.parseInt(line.substring("SHIPS_COUNT:".length()));
        for (int i = 0; i < shipsCount; i++) {
            line = reader.readLine();
            String[] parts = line.substring("SHIP:".length()).split(",");
            int length = Integer.parseInt(parts[0]);
            int row = Integer.parseInt(parts[1]);
            int col = Integer.parseInt(parts[2]);
            boolean horizontal = Boolean.parseBoolean(parts[3]);
            int hits = Integer.parseInt(parts[4]);
            // Place ship on board
            player.getBoard().placeShip(row, col, length, horizontal);
            Ship ship = new Ship(length, row, col, horizontal);
            ship.setHits(hits);
            player.addShip(ship);
        }
        // Load shipGrid
        line = reader.readLine(); 
        char[][] shipGrid = player.getBoard().getShipGrid();
        for (int r = 0; r < 10; r++) {
            line = reader.readLine();
            for (int c = 0; c < 10; c++) {
                shipGrid[r][c] = line.charAt(c);
            }
        }
        // Load infoGrid
        line = reader.readLine();
        char[][] infoGrid = player.getBoard().getInfoGrid();
        for (int r = 0; r < 10; r++) {
            line = reader.readLine();
            for (int c = 0; c < 10; c++) {
                infoGrid[r][c] = line.charAt(c);
            }
        }
        reader.readLine(); // 
    }
    
    private void showPlayer1Placement() {
        new ShipPlacementScreen(true, () -> {
            ShipPlacementScreen currentScreen = null;
            for (Window window : Window.getWindows()) {
                if (window instanceof ShipPlacementScreen && window.isVisible()) {
                    currentScreen = (ShipPlacementScreen) window;
                    break;
                }
            }
            if (currentScreen != null) {
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
            // Store reference to the screen before it closes
            ShipPlacementScreen currentScreen = null;
            for (Window window : Window.getWindows()) {
                if (window instanceof ShipPlacementScreen && window.isVisible()) {
                    currentScreen = (ShipPlacementScreen) window;
                    break;
                }
            }
            if (currentScreen != null) {
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