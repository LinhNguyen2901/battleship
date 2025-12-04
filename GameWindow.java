import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.io.*;

public class GameWindow extends JFrame {

    private GameController controller;
    private BoardPanel myBoardPanel;      // Current player's ship board 
    private BoardPanel opponentBoardPanel; // Guess board
    private JLabel statusLabel;
    private JLabel shipsInfoLabel;
    private JButton switchButton;

    public GameWindow(GameController controller) {
        this.controller = controller;

        setTitle("Battleship Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLayout(new BorderLayout());

        // Top panel: player info, ships, remaining ships
        JPanel topPanel = new JPanel(new BorderLayout());
        statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 22));
        shipsInfoLabel = new JLabel("", SwingConstants.CENTER);
        shipsInfoLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        topPanel.add(statusLabel, BorderLayout.NORTH);
        topPanel.add(shipsInfoLabel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // Middle panel: boards side by side
        JPanel boardsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        boardsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Left board: ship board of the current player
        JPanel leftPanel = new JPanel(new BorderLayout());
        JLabel myBoardLabel = new JLabel("Your Ships", SwingConstants.CENTER);
        myBoardLabel.setFont(new Font("Arial", Font.BOLD, 18)); 
        leftPanel.add(myBoardLabel, BorderLayout.NORTH);
        myBoardPanel = new BoardPanel(controller, true); // true = player's ship board
        leftPanel.add(myBoardPanel, BorderLayout.CENTER);
        boardsPanel.add(leftPanel);

        // Right board: guess board
        JPanel rightPanel = new JPanel(new BorderLayout());
        JLabel opponentBoardLabel = new JLabel("Opponent Board (Click to shoot)", SwingConstants.CENTER);
        opponentBoardLabel.setFont(new Font("Arial", Font.BOLD, 18)); 
        rightPanel.add(opponentBoardLabel, BorderLayout.NORTH);
        opponentBoardPanel = new BoardPanel(controller, false); // false = opponent guess board
        rightPanel.add(opponentBoardPanel, BorderLayout.CENTER);
        boardsPanel.add(rightPanel);

        add(boardsPanel, BorderLayout.CENTER);

        // Bottom panel: buttons
        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton saveButton = new JButton("Save Game");
        saveButton.setFont(new Font("Arial", Font.BOLD, 14));
        saveButton.setPreferredSize(new Dimension(120, 35));
        saveButton.addActionListener(e -> saveGame());
        
        JButton loadButton = new JButton("Load Game");
        loadButton.setFont(new Font("Arial", Font.BOLD, 14));
        loadButton.setPreferredSize(new Dimension(120, 35));
        loadButton.addActionListener(e -> loadGame());
        
        switchButton = new JButton("New Game");
        switchButton.setFont(new Font("Arial", Font.BOLD, 14));
        switchButton.setPreferredSize(new Dimension(120, 35));
        switchButton.setEnabled(true); 
        switchButton.setVisible(true); 
        switchButton.addActionListener(e -> restartGame());
        
        bottomPanel.add(saveButton);
        bottomPanel.add(loadButton);
        bottomPanel.add(switchButton);
        add(bottomPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(null); // center window
        updateStatus();
    }

    private void saveGame() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Game");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Battleship Save Files", "bsg"));
            
            int userSelection = fileChooser.showSaveDialog(this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                String filePath = fileToSave.getAbsolutePath();
                if (!filePath.endsWith(".bsg")) {
                    filePath += ".bsg";
                }
                
                PrintWriter writer = new PrintWriter(new FileWriter(filePath));
                
                // Save current player (1 or 2)
                int currentPlayerNum = (controller.getCurrentPlayer() == controller.getPlayer1()) ? 1 : 2;
                writer.println("CURRENT_PLAYER:" + currentPlayerNum);
                
                // Save Player 1
                savePlayerState(writer, controller.getPlayer1(), 1);
                
                // Save Player 2
                savePlayerState(writer, controller.getPlayer2(), 2);
                
                writer.close();
                JOptionPane.showMessageDialog(this, "Game saved successfully!");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving game: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void savePlayerState(PrintWriter writer, Player player, int playerNum) {
        writer.println("PLAYER_" + playerNum + "_START");
        
        // Save ships
        writer.println("SHIPS_COUNT:" + player.getShips().size());
        for (Ship ship : player.getShips()) {
            writer.println("SHIP:" + ship.getLength() + "," + ship.getStartRow() + "," + 
                          ship.getStartCol() + "," + ship.isHorizontal() + "," + ship.getHits());
        }
        
        // Save shipGrid
        writer.println("SHIPGRID_START");
        char[][] shipGrid = player.getBoard().getShipGrid();
        for (int r = 0; r < 10; r++) {
            StringBuilder row = new StringBuilder();
            for (int c = 0; c < 10; c++) {
                row.append(shipGrid[r][c]);
            }
            writer.println(row.toString());
        }
        
        // Save infoGrid
        writer.println("INFOGRID_START");
        char[][] infoGrid = player.getBoard().getInfoGrid();
        for (int r = 0; r < 10; r++) {
            StringBuilder row = new StringBuilder();
            for (int c = 0; c < 10; c++) {
                row.append(infoGrid[r][c]);
            }
            writer.println(row.toString());
        }
        
        writer.println("PLAYER_" + playerNum + "_END");
    }
    
    private void loadGame() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Load Game");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Battleship Save Files", "bsg"));
            
            int userSelection = fileChooser.showOpenDialog(this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToLoad = fileChooser.getSelectedFile();
                BufferedReader reader = new BufferedReader(new FileReader(fileToLoad));
                
                // Reset both players first
                controller.getPlayer1().reset();
                controller.getPlayer2().reset();
                
                String line;
                int currentPlayerNum = 1;
                
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("CURRENT_PLAYER:")) {
                        currentPlayerNum = Integer.parseInt(line.substring("CURRENT_PLAYER:".length()));
                    } else if (line.equals("PLAYER_1_START")) {
                        loadPlayerState(reader, controller.getPlayer1());
                    } else if (line.equals("PLAYER_2_START")) {
                        loadPlayerState(reader, controller.getPlayer2());
                    }
                }
                
                reader.close();
                
                // Set current player
                while (controller.getCurrentPlayer() != (currentPlayerNum == 1 ? controller.getPlayer1() : controller.getPlayer2())) {
                    controller.switchPlayers();
                }
                
                // Reset all buttons
                for (int r = 0; r < 10; r++) {
                    for (int c = 0; c < 10; c++) {
                        myBoardPanel.buttons[r][c].ship = null;
                        myBoardPanel.buttons[r][c].shipIndex = -1;
                        myBoardPanel.buttons[r][c].isHit = false;
                        opponentBoardPanel.buttons[r][c].ship = null;
                        opponentBoardPanel.buttons[r][c].shipIndex = -1;
                        opponentBoardPanel.buttons[r][c].isHit = false;
                    }
                }
                
                // Update boards
                myBoardPanel.updateBoard();
                opponentBoardPanel.updateBoard();
                opponentBoardPanel.setEnabled(true);
                updateStatus();
                
                JOptionPane.showMessageDialog(this, "Game loaded successfully!");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading game: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading game: Invalid file format", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadPlayerState(BufferedReader reader, Player player) throws IOException {
        String line;
        
        // Load ships
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
        line = reader.readLine(); // "SHIPGRID_START"
        char[][] shipGrid = player.getBoard().getShipGrid();
        for (int r = 0; r < 10; r++) {
            line = reader.readLine();
            for (int c = 0; c < 10; c++) {
                shipGrid[r][c] = line.charAt(c);
            }
        }
        
        // Load infoGrid
        line = reader.readLine(); // "INFOGRID_START"
        char[][] infoGrid = player.getBoard().getInfoGrid();
        for (int r = 0; r < 10; r++) {
            line = reader.readLine();
            for (int c = 0; c < 10; c++) {
                infoGrid[r][c] = line.charAt(c);
            }
        }
        
        reader.readLine(); // "PLAYER_X_END"
    }
    
    private void restartGame() {
        // Reset both players for a new game
        controller.getPlayer1().reset();
        controller.getPlayer2().reset();
        controller.setupGame();
        
        // Reset to player 1
        while (controller.getCurrentPlayer() != controller.getPlayer1()) {
            controller.switchPlayers();
        }
        
        // Reset all buttons
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                myBoardPanel.buttons[r][c].ship = null;
                myBoardPanel.buttons[r][c].shipIndex = -1;
                myBoardPanel.buttons[r][c].isHit = false;
                opponentBoardPanel.buttons[r][c].ship = null;
                opponentBoardPanel.buttons[r][c].shipIndex = -1;
                opponentBoardPanel.buttons[r][c].isHit = false;
            }
        }
        
        // Reset board display
        myBoardPanel.updateBoard();
        opponentBoardPanel.updateBoard();
        // Re-enable opponent board
        opponentBoardPanel.setEnabled(true);
        // Update status label
        updateStatus();
    }

    // Show a full-screen overlay covering the application window for delayMs milliseconds.
    // Disables the owner window while the overlay is visible to prevent peeking.
    private void showFullScreenOverlay(String message, int delayMs, Runnable onComplete) {
        final JFrame owner = this; // use this frame directly

        try {
            Point loc = owner.getLocationOnScreen();
            Dimension size = owner.getSize();

            // disable owner to prevent interaction
            owner.setEnabled(false);

            final JWindow overlay = new JWindow(owner);
            overlay.setBackground(Color.BLACK);
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBackground(Color.BLACK);
            JLabel lbl = new JLabel(message, SwingConstants.CENTER);
            lbl.setForeground(Color.WHITE);
            lbl.setFont(new Font("Arial", Font.BOLD, 36));
            panel.add(lbl);
            overlay.getContentPane().add(panel);
            overlay.setBounds(loc.x, loc.y, size.width, size.height);
            overlay.setAlwaysOnTop(true);
            overlay.setFocusableWindowState(false);
            overlay.validate(); overlay.repaint(); overlay.setVisible(true);

            javax.swing.Timer timer = new javax.swing.Timer(delayMs, e -> {
                overlay.setVisible(false);
                overlay.dispose();
                owner.setEnabled(true);
                if (onComplete != null) SwingUtilities.invokeLater(onComplete);
            });
            timer.setRepeats(false);
            timer.start();
        } catch (IllegalComponentStateException ex) {
            // fallback to glass pane overlay sized to the window
            final JComponent glass = (JComponent) owner.getGlassPane();
            JPanel overlay = new JPanel(new BorderLayout());
            overlay.setBackground(Color.BLACK);
            JLabel lbl = new JLabel(message, SwingConstants.CENTER);
            lbl.setForeground(Color.WHITE);
            lbl.setFont(new Font("Arial", Font.BOLD, 36));
            overlay.add(lbl, BorderLayout.CENTER);
            glass.setLayout(new BorderLayout());
            glass.removeAll();
            glass.add(overlay, BorderLayout.CENTER);
            glass.setVisible(true);
            javax.swing.Timer timer = new javax.swing.Timer(delayMs, e -> {
                glass.setVisible(false); glass.removeAll(); owner.setEnabled(true); if (onComplete!=null) SwingUtilities.invokeLater(onComplete);
            });
            timer.setRepeats(false); timer.start();
        }
    }

    // Show a full-screen overlay displaying an image scaled to the application window.
    private void showFullScreenOverlay(Image image, int delayMs, Runnable onComplete) {
        if (image == null) {
            showFullScreenOverlay("", delayMs, onComplete);
            return;
        }

        final JFrame owner = this;
        try {
            Point loc = owner.getLocationOnScreen();
            Dimension size = owner.getSize();

            owner.setEnabled(false);

            final JWindow overlay = new JWindow(owner);
            overlay.setBackground(Color.BLACK);

            Image scaled = image.getScaledInstance(size.width, size.height, Image.SCALE_SMOOTH);
            JLabel imgLabel = new JLabel(new ImageIcon(scaled));
            imgLabel.setHorizontalAlignment(SwingConstants.CENTER);
            // ensure background behind image is black
            JPanel holder = new JPanel(new BorderLayout());
            holder.setBackground(Color.BLACK);
            holder.add(imgLabel, BorderLayout.CENTER);

            overlay.getContentPane().add(holder);
            overlay.setBounds(loc.x, loc.y, size.width, size.height);
            overlay.setAlwaysOnTop(true);
            overlay.setFocusableWindowState(false);
            overlay.validate(); overlay.repaint(); overlay.setVisible(true);

            javax.swing.Timer timer = new javax.swing.Timer(delayMs, e -> {
                overlay.setVisible(false);
                overlay.dispose();
                owner.setEnabled(true);
                if (onComplete != null) SwingUtilities.invokeLater(onComplete);
            });
            timer.setRepeats(false);
            timer.start();
        } catch (IllegalComponentStateException ex) {
            // fallback to glass pane: scale image to glass pane size
            final JComponent glass = (JComponent) owner.getGlassPane();
            glass.setLayout(new BorderLayout());
            glass.removeAll();
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBackground(Color.BLACK);
            int w = owner.getWidth();
            int h = owner.getHeight();
            Image scaled = image.getScaledInstance(Math.max(1,w), Math.max(1,h), Image.SCALE_SMOOTH);
            JLabel imgLabel = new JLabel(new ImageIcon(scaled));
            imgLabel.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(imgLabel, BorderLayout.CENTER);
            glass.add(panel, BorderLayout.CENTER);
            glass.setVisible(true);
            javax.swing.Timer timer = new javax.swing.Timer(delayMs, e -> {
                glass.setVisible(false); glass.removeAll(); owner.setEnabled(true); if (onComplete!=null) SwingUtilities.invokeLater(onComplete);
            });
            timer.setRepeats(false); timer.start();
        }
    }

    private void updateStatus() {
        Player current = controller.getCurrentPlayer();
        Player opponent = controller.getOpponent();
        int currentPlayerNumber = (current == controller.getPlayer1()) ? 1 : 2;
        int myShipsRemaining = countRemainingShips(current);
        int opponentShipsRemaining = countRemainingShips(opponent);
        String shipsInfo = getShipsInfo(current);

        // Check win/lose
        if (opponent.getBoard().allShipsSunk(opponent.getShips())) {
            statusLabel.setText("Player " + currentPlayerNumber + " WINS!");
            shipsInfoLabel.setText("Your ships: " + shipsInfo + " | Remaining: " + myShipsRemaining);
            opponentBoardPanel.setEnabled(false);
        } else {
            statusLabel.setText("Player " + currentPlayerNumber + "'s Turn");
            shipsInfoLabel.setText("Your ships: " + shipsInfo + " | Your remaining: " + myShipsRemaining + 
                                   " | Opponent remaining: " + opponentShipsRemaining);
        }
        // Enable new game button
        switchButton.setEnabled(true);
    }

    private String getShipsInfo(Player player) {
        // Count number of ships by length
        Map<Integer, Integer> shipCounts = new HashMap<>();
        for (Ship s : player.getShips()) {
            int length = s.getLength();
            shipCounts.put(length, shipCounts.getOrDefault(length, 0) + 1);
        }
    
        java.util.List<String> infoParts = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : shipCounts.entrySet()) {
            infoParts.add(entry.getValue() + " ship(s) of " + entry.getKey());
        }
        return String.join(", ", infoParts);
    }

    private int countRemainingShips(Player player) {
        int count = 0;
        for (Ship s : player.getShips()) {
            if (!s.isSunk()) {
                count++;
            }
        }
        return count;
    }

    private class BoardPanel extends JPanel {
        private CellButton[][] buttons = new CellButton[10][10];
        private GameController controller;
        private boolean isMyBoard; // true = player's ship board, false = opponent guess board

        public BoardPanel(GameController controller, boolean isMyBoard) {
            this.controller = controller;
            this.isMyBoard = isMyBoard;
            setLayout(new GridLayout(10, 10));
            setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
            setBackground(Color.WHITE);

            for (int r = 0; r < 10; r++) {
                for (int c = 0; c < 10; c++) {
                    CellButton btn = new CellButton();
                    btn.setPreferredSize(new Dimension(50, 50));
                    btn.setBackground(Color.WHITE);
                    btn.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
                    btn.setOpaque(true);
                    final int row = r, col = c;
                    
                    // Only allow clicks on opponent guess board
                    if (!isMyBoard) {
                        btn.addActionListener(e -> handleClick(row, col));
                    } else {
                        btn.setEnabled(false);
                    }
                    
                    buttons[r][c] = btn;
                    add(btn);
                }
            }
            updateBoard();
        }

        private void handleClick(int r, int c) {
            Player current = controller.getCurrentPlayer();
            Player opponent = controller.getOpponent();

            if (current instanceof HumanPlayer) {
                // Check if cell already chosen
                char[][] opponentView = opponent.getBoardForOpponent();
                if (opponentView[r][c] != ' ') {
                    return;
                }
                ((HumanPlayer) current).setNextShot(r, c, opponentView);
                Player targetOpponent = opponent;
                String result = controller.takeTurn();
                
                char[][] updatedView = targetOpponent.getBoardForOpponent();
                boolean sunk = updatedView[r][c] == 'D';
                boolean hit = updatedView[r][c] == 'H' || updatedView[r][c] == 'D';
                
                // If hit, update board display
                if (hit) {
                    controller.switchPlayers(); 
                    opponentBoardPanel.updateBoard();
                    controller.switchPlayers();
                }
                
                // Display result message
                if (result.equals("gameOver")) {
                    controller.switchPlayers();
                    myBoardPanel.updateBoard();
                    opponentBoardPanel.updateBoard();
                    opponentBoardPanel.setEnabled(false);
                    updateStatus();
                    JOptionPane.showMessageDialog(GameWindow.this, 
                        "Player " + (controller.getCurrentPlayer() == controller.getPlayer1() ? 1 : 2) + " WINS!");
                } else {
                    // Show hit/miss popup
                    String message;
                    if (sunk)
                        message = "You sink a ship!";
                    else if (hit)
                        message = "Hit!";
                    else
                        message = "Miss!";
                    int choice = JOptionPane.showOptionDialog(
                        this,
                        message,
                        "Result",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
                        null,
                        new Object[]{"Switch Player"},   // <-- custom button text
                        "Switch Player"
                    );

                    if (choice == 0) {
                        // Show a full-window overlay for 2 seconds while players switch
                        showFullScreenOverlay("Switching player...", 2000, () -> {
                            myBoardPanel.updateBoard();
                            opponentBoardPanel.updateBoard();
                            updateStatus();
                        });
                    }
                }
            }
        }

        public void updateBoard() {
            Player current = controller.getCurrentPlayer();
            Player opponent = controller.getOpponent();
            
            if (isMyBoard) {
                // Player's ship board: show shipGrid and mark hits
                char[][] infoGrid = current.getBoard().getInfoGrid();
                java.util.List<Ship> ships = current.getShips();
                
                // Reset all buttons
                for (int r = 0; r < 10; r++) {
                    for (int c = 0; c < 10; c++) {
                        CellButton btn = buttons[r][c];
                        btn.ship = null;
                        btn.shipIndex = -1;
                        btn.isHit = false;
                        btn.setBackground(Color.WHITE);
                    }
                }
                
                // Assign ship info to each button
                for (int shipIdx = 0; shipIdx < ships.size(); shipIdx++) {
                    Ship ship = ships.get(shipIdx);
                    int startRow = ship.getStartRow();
                    int startCol = ship.getStartCol();
                    boolean horizontal = ship.isHorizontal();
                    
                    for (int i = 0; i < ship.getLength(); i++) {
                        int r = horizontal ? startRow : startRow + i;
                        int c = horizontal ? startCol + i : startCol;
                        
                        if (r >= 0 && r < 10 && c >= 0 && c < 10) {
                            CellButton btn = buttons[r][c];
                            btn.ship = ship;
                            btn.shipIndex = shipIdx;
                            char info = infoGrid[r][c];
                            btn.isHit = (info == 'H' || info == 'D');
                        }
                    }
                }
                
                // Repaint all buttons
                for (int r = 0; r < 10; r++) {
                    for (int c = 0; c < 10; c++) {
                        buttons[r][c].repaint();
                    }
                }
            } else {
                // Opponent board: show infoGrid for guessing
                char[][] view = opponent.getBoardForOpponent();
                for (int r = 0; r < 10; r++) {
                    for (int c = 0; c < 10; c++) {
                        CellButton btn = buttons[r][c];
                        char cell = view[r][c];
                        btn.ship = null;
                        btn.shipIndex = -1;
                        
                        switch (cell) {
                            case 'H':
                                btn.isHit = true;
                                btn.setBackground(new Color(255, 200, 200)); // light red
                                break;
                            case 'M':
                                btn.isHit = false;
                                btn.setBackground(new Color(200, 200, 255)); // light blue for miss
                                break;
                            case 'D':
                                btn.isHit = true;
                                btn.setBackground(new Color(255, 120, 120)); // light red, same as hit
                                break;
                            default:
                                btn.isHit = false;
                                btn.setBackground(Color.WHITE);
                        }
                        btn.repaint();
                    }
                }
            }
        }
        
        public void setEnabled(boolean enabled) {
            for (int r = 0; r < 10; r++) {
                for (int c = 0; c < 10; c++) {
                    buttons[r][c].setEnabled(enabled && !isMyBoard);
                }
            }
        }
    }
    
    // Custom button to draw ships as ovals
    private class CellButton extends JButton {
        Ship ship = null;
        int shipIndex = -1;
        boolean isHit = false;
        
        // Colors for different ships
        private Color[] shipColors = {
            new Color(50, 100, 200),   // Blue
            new Color(100, 200, 100),  // Green
            new Color(200, 150, 50),   // Orange
            new Color(150, 100, 200),  // Purple
            new Color(200, 100, 150)   // Pink
        };
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            if (ship != null) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int width = getWidth();
                int height = getHeight();
                int padding = 2;
                
                // Get ship color
                Color shipColor = shipColors[shipIndex % shipColors.length];
                if (isHit) {
                    // Ship hit: red
                    shipColor = Color.RED;
                }
                
                // Draw circle for each grid cell
                int circleSize = Math.min(width, height) - 2 * padding;
                int x = (width - circleSize) / 2;
                int y = (height - circleSize) / 2;
                
                g2d.setColor(shipColor);
                g2d.fillOval(x, y, circleSize, circleSize);
                
                // Draw X if hit
                if (isHit) {
                    g2d.setColor(Color.WHITE);
                    g2d.setStroke(new BasicStroke(2));
                    int x1 = padding + 5;
                    int y1 = padding + 5;
                    int x2 = width - padding - 5;
                    int y2 = height - padding - 5;
                    g2d.drawLine(x1, y1, x2, y2);
                    g2d.drawLine(x2, y1, x1, y2);
                }
            } else if (isHit) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.GRAY);
                g2d.setStroke(new BasicStroke(1));
                int width = getWidth();
                int height = getHeight();
                int padding = 8;
                g2d.drawLine(padding, padding, width - padding, height - padding);
                g2d.drawLine(width - padding, padding, padding, height - padding);
            }
        }
    }

    public static void main(String[] args) {
        Player p1 = new HumanPlayer();
        Player p2 = new HumanPlayer(); 
        GameController gc = new GameController(p1, p2);
        gc.setupGame();

        SwingUtilities.invokeLater(() -> {
            GameWindow gw = new GameWindow(gc);
            gw.setVisible(true);
        });
    }
}
