import javax.swing.*;
import java.awt.*;
import java.util.*;

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
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        shipsInfoLabel = new JLabel("", SwingConstants.CENTER);
        shipsInfoLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        topPanel.add(statusLabel, BorderLayout.NORTH);
        topPanel.add(shipsInfoLabel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // Middle panel: boards side by side
        JPanel boardsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        boardsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Left board: ship board of the current player
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(new JLabel("Your Ships", SwingConstants.CENTER), BorderLayout.NORTH);
        myBoardPanel = new BoardPanel(controller, true); // true = player's ship board
        leftPanel.add(myBoardPanel, BorderLayout.CENTER);
        boardsPanel.add(leftPanel);

        // Right board: guess board
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(new JLabel("Opponent Board (Click to shoot)", SwingConstants.CENTER), BorderLayout.NORTH);
        opponentBoardPanel = new BoardPanel(controller, false); // false = opponent guess board
        rightPanel.add(opponentBoardPanel, BorderLayout.CENTER);
        boardsPanel.add(rightPanel);

        add(boardsPanel, BorderLayout.CENTER);

        // Bottom panel: new game button
        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        switchButton = new JButton("New Game");
        switchButton.setFont(new Font("Arial", Font.BOLD, 16));
        switchButton.setPreferredSize(new Dimension(150, 40));
        switchButton.setEnabled(true); 
        switchButton.setVisible(true); 
        switchButton.addActionListener(e -> restartGame());
        bottomPanel.add(switchButton);
        add(bottomPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(null); // center window
        updateStatus();
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
                    String message = hit ? "HIT!" : "MISS!";
                    JOptionPane.showMessageDialog(GameWindow.this, message);
                    
                    // After OK, update both boards for next player's view
                    myBoardPanel.updateBoard();
                    opponentBoardPanel.updateBoard();
                    updateStatus();
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
                                btn.setBackground(new Color(255, 200, 200)); // light red, same as hit
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
