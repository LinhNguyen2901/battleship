import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class ShipPlacementScreen extends JFrame {
    private Board board;
    private ArrayList<Ship> ships;
    private BoardPanel boardPanel;
    private ShipSelectionPanel shipPanel;
    private JButton confirmButton;
    private JButton randomButton;
    private JButton rotateButton;
    private JButton clearButton;
    private boolean isPlayer1;
    private Runnable onComplete;
    
    private boolean isHorizontal = true;
    
    // Ship sizes for battleship
    private int[] shipSizes = {5, 4, 3, 3, 2};
    private String[] shipNames = {"Carrier (5)", "Battleship (4)", "Cruiser (3)", "Submarine (3)", "Destroyer (2)"};
    private int currentShipIndex = 0;

    public ShipPlacementScreen(boolean isPlayer1, Runnable onComplete) {
        this.isPlayer1 = isPlayer1;
        this.onComplete = onComplete;
        this.board = new Board();
        this.ships = new ArrayList<>();
        
        setTitle("Ship Placement - Player " + (isPlayer1 ? "1" : "2"));
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);
        
        setupUI();
        setVisible(true);
    }
    
    private void setupUI() {
        // Top panel with instructions
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Player " + (isPlayer1 ? "1" : "2") + " - Place Your Ships", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel instructionLabel = new JLabel("Click on the board to place ships. Use Rotate button to change orientation.", SwingConstants.CENTER);
        instructionLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        
        topPanel.add(titleLabel, BorderLayout.NORTH);
        topPanel.add(instructionLabel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);
        
        // Center panel with board
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        boardPanel = new BoardPanel();
        centerPanel.add(boardPanel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
        
        // Right panel with ships to place
        shipPanel = new ShipSelectionPanel();
        add(shipPanel, BorderLayout.EAST);
        
        // Bottom panel with buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        rotateButton = new JButton("Rotate ⟳");
        rotateButton.setFont(new Font("Arial", Font.BOLD, 14));
        rotateButton.setPreferredSize(new Dimension(120, 40));
        rotateButton.addActionListener(e -> toggleOrientation());
        
        clearButton = new JButton("Clear All");
        clearButton.setFont(new Font("Arial", Font.BOLD, 14));
        clearButton.setPreferredSize(new Dimension(120, 40));
        clearButton.addActionListener(e -> clearAllShips());
        
        randomButton = new JButton("Random");
        randomButton.setFont(new Font("Arial", Font.BOLD, 14));
        randomButton.setPreferredSize(new Dimension(120, 40));
        randomButton.addActionListener(e -> placeShipsRandomly());
        
        confirmButton = new JButton("Confirm");
        confirmButton.setFont(new Font("Arial", Font.BOLD, 14));
        confirmButton.setPreferredSize(new Dimension(120, 40));
        confirmButton.setEnabled(false);
        confirmButton.addActionListener(e -> confirmPlacement());
        
        buttonPanel.add(rotateButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(randomButton);
        buttonPanel.add(confirmButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void toggleOrientation() {
        isHorizontal = !isHorizontal;
        boardPanel.repaint();
        shipPanel.repaint();
    }
    
    private void clearAllShips() {
        ships.clear();
        board.reset();
        currentShipIndex = 0;
        boardPanel.repaint();
        shipPanel.repaint();
        confirmButton.setEnabled(false);
    }
    
    private void placeShipsRandomly() {
        // Clear existing ships
        clearAllShips();
        
        // Place ships randomly
        java.util.Random rand = new java.util.Random();
        for (int size : shipSizes) {
            boolean placed = false;
            while (!placed) {
                boolean horizontal = rand.nextBoolean();
                int r = rand.nextInt(Board.SIZE);
                int c = rand.nextInt(Board.SIZE);
                if (board.canPlaceShip(r, c, size, horizontal)) {
                    board.placeShip(r, c, size, horizontal);
                    Ship s = new Ship(size, r, c, horizontal);
                    ships.add(s);
                    placed = true;
                }
            }
        }
        
        currentShipIndex = shipSizes.length;
        boardPanel.repaint();
        shipPanel.repaint();
        confirmButton.setEnabled(true);
    }
    
    private void confirmPlacement() {
        // Pass the board and ships to the game
        if (onComplete != null) {
            onComplete.run();
        }
        dispose();
    }
    
    public Board getBoard() {
        return board;
    }
    
    public ArrayList<Ship> getShips() {
        return ships;
    }
    
    private class BoardPanel extends JPanel {
        private CellButton[][] buttons = new CellButton[10][10];
        
        public BoardPanel() {
            setLayout(new GridLayout(10, 10));
            setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
            setPreferredSize(new Dimension(500, 500));
            
            for (int r = 0; r < 10; r++) {
                for (int c = 0; c < 10; c++) {
                    CellButton btn = new CellButton(r, c);
                    btn.setBackground(Color.WHITE);
                    btn.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
                    btn.setOpaque(true);
                    buttons[r][c] = btn;
                    add(btn);
                }
            }
        }
        
        private class CellButton extends JButton {
            int row, col;
            
            public CellButton(int r, int c) {
                this.row = r;
                this.col = c;
                
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if (currentShipIndex < shipSizes.length) {
                            boardPanel.repaint();
                        }
                    }
                    
                    @Override
                    public void mouseExited(MouseEvent e) {
                        boardPanel.repaint();
                    }
                    
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        handleCellClick(row, col);
                    }
                });
            }
            
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw placed ships
                if (board.getShipCoord(row, col) == 'S') {
                    g2d.setColor(new Color(50, 100, 200));
                    g2d.fillOval(5, 5, getWidth() - 10, getHeight() - 10);
                }
                
                // Show preview of ship being placed (only on hovered cell)
                if (currentShipIndex < shipSizes.length && getModel().isRollover()) {
                    int shipLength = shipSizes[currentShipIndex];
                    boolean canPlace = board.canPlaceShip(row, col, shipLength, isHorizontal);
                    
                    // Draw preview for all cells that would be occupied
                    g2d.setColor(canPlace ? new Color(100, 255, 100, 100) : new Color(255, 100, 100, 100));
                    
                    // Draw this cell's preview
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                    
                    // Draw adjacent cells preview
                    for (int i = 1; i < shipLength; i++) {
                        int previewRow = isHorizontal ? row : row + i;
                        int previewCol = isHorizontal ? col + i : col;
                        if (previewRow >= 0 && previewRow < 10 && previewCol >= 0 && previewCol < 10) {
                            CellButton previewBtn = buttons[previewRow][previewCol];
                            Graphics2D g2dPreview = (Graphics2D) previewBtn.getGraphics();
                            if (g2dPreview != null) {
                                g2dPreview.setColor(canPlace ? new Color(100, 255, 100, 100) : new Color(255, 100, 100, 100));
                                g2dPreview.fillRect(0, 0, previewBtn.getWidth(), previewBtn.getHeight());
                            }
                        }
                    }
                }
            }
        }
        
        private void handleCellClick(int r, int c) {
            if (currentShipIndex >= shipSizes.length) {
                return; // All ships placed
            }
            
            int shipLength = shipSizes[currentShipIndex];
            
            if (board.canPlaceShip(r, c, shipLength, isHorizontal)) {
                board.placeShip(r, c, shipLength, isHorizontal);
                Ship ship = new Ship(shipLength, r, c, isHorizontal);
                ships.add(ship);
                currentShipIndex++;
                
                repaint();
                shipPanel.repaint();
                
                if (currentShipIndex >= shipSizes.length) {
                    confirmButton.setEnabled(true);
                    JOptionPane.showMessageDialog(ShipPlacementScreen.this, 
                        "All ships placed! Click 'Confirm' to continue.");
                }
            } else {
                JOptionPane.showMessageDialog(ShipPlacementScreen.this, 
                    "Cannot place ship there!", "Invalid Placement", JOptionPane.WARNING_MESSAGE);
            }
        }
    }
    
    private class ShipSelectionPanel extends JPanel {
        public ShipSelectionPanel() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createTitledBorder("Ships to Place"));
            setPreferredSize(new Dimension(200, 500));
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            
            int y = 40;
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            g2d.setColor(Color.BLACK);
            g2d.drawString("Ships:", 20, y);
            
            y += 30;
            g2d.setFont(new Font("Arial", Font.PLAIN, 12));
            for (int i = 0; i < shipSizes.length; i++) {
                if (i < currentShipIndex) {
                    g2d.setColor(new Color(0, 150, 0));
                    g2d.drawString("✓ " + shipNames[i], 20, y);
                } else if (i == currentShipIndex) {
                    g2d.setColor(Color.BLUE);
                    g2d.setFont(new Font("Arial", Font.BOLD, 13));
                    g2d.drawString("→ " + shipNames[i], 20, y);
                    g2d.setFont(new Font("Arial", Font.PLAIN, 12));
                } else {
                    g2d.setColor(Color.GRAY);
                    g2d.drawString("○ " + shipNames[i], 20, y);
                }
                y += 25;
            }
            
            // Show orientation
            g2d.setColor(Color.BLACK);
            y += 30;
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            g2d.drawString("Orientation:", 20, y);
            y += 25;
            g2d.setFont(new Font("Arial", Font.PLAIN, 12));
            if (isHorizontal) {
                g2d.drawString("→ Horizontal", 20, y);
            } else {
                g2d.drawString("↓ Vertical", 20, y);
            }
        }
    }
}