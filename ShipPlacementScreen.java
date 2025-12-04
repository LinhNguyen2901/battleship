//  allows players to place their 4 ships on a 10x10 grid by dragging them from a side panel onto the board

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
    private JButton clearButton;
    private boolean isPlayer1;
    private Runnable onComplete;
    
    // Ship sizes for battleship
    private int[] shipSizes = {5, 4, 3, 2};
    private String[] shipNames = {"Carrier (5)", "Battleship (4)", "Cruiser (3)", "Destroyer (2)"};
    private Color[] shipColors = {
        new Color(50, 100, 200),   // Blue
        new Color(100, 200, 100),  // Green
        new Color(200, 150, 50),   // Orange
        new Color(150, 100, 200),  // Purple
        new Color(200, 100, 150)   // Pink
    };

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
        
        JLabel instructionLabel = new JLabel("Drag ships from the right panel onto the board. Right-click to rotate.", SwingConstants.CENTER);
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
        
        buttonPanel.add(clearButton);
        buttonPanel.add(randomButton);
        buttonPanel.add(confirmButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void clearAllShips() {
        ships.clear();
        board.reset();
        boardPanel.repaint();
        shipPanel.resetShips();
        confirmButton.setEnabled(false);
    }
    
    private void placeShipsRandomly() {
        clearAllShips();
        
        java.util.Random rand = new java.util.Random();
        for (int i = 0; i < shipSizes.length; i++) {
            int size = shipSizes[i];
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
        
        boardPanel.repaint();
        shipPanel.allShipsPlaced();
        confirmButton.setEnabled(true);
    }
    
    private void confirmPlacement() {
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
        private DraggableShipComponent draggedShip = null;
        
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
        
        public boolean tryPlaceShip(DraggableShipComponent dragShip, Point dropPoint) {
            // Convert point to grid coordinates
            Component comp = getComponentAt(dropPoint);
            if (comp instanceof CellButton) {
                CellButton cell = (CellButton) comp;
                int gridRow = cell.row;
                int gridCol = cell.col;
                
                if (board.canPlaceShip(gridRow, gridCol, dragShip.length, dragShip.horizontal)) {
                    board.placeShip(gridRow, gridCol, dragShip.length, dragShip.horizontal);
                    Ship ship = new Ship(dragShip.length, gridRow, gridCol, dragShip.horizontal);
                    ships.add(ship);
                    repaint();
                    
                    // Check if all ships placed
                    if (ships.size() == shipSizes.length) {
                        confirmButton.setEnabled(true);
                        JOptionPane.showMessageDialog(ShipPlacementScreen.this, 
                            "All ships placed! Click 'Confirm' to continue.");
                    }
                    return true;
                }
            }
            return false;
        }
        
        private class CellButton extends JButton {
            int row, col;
            
            public CellButton(int r, int c) {
                this.row = r;
                this.col = c;
            }
            
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw placed ships
                if (board.getShipCoord(row, col) == 'S') {
                    // Find which ship this belongs to
                    int shipIndex = -1;
                    for (int i = 0; i < ships.size(); i++) {
                        if (ships.get(i).occupies(row, col)) {
                            shipIndex = i;
                            break;
                        }
                    }
                    
                    Color shipColor = shipIndex >= 0 ? shipColors[shipIndex % shipColors.length] : new Color(50, 100, 200);
                    g2d.setColor(shipColor);
                    
                    int size = Math.min(getWidth(), getHeight());
                    int padding = 5;
                    g2d.fillOval(padding, padding, size - 2*padding, size - 2*padding);
                }
                
                // Show preview if dragging
                if (draggedShip != null && draggedShip.isBeingDragged) {
                    Point boardMouse = getMousePosition();
                    if (boardMouse != null) {
                        Component hoverComp = getComponentAt(boardMouse);
                        if (hoverComp instanceof CellButton) {
                            CellButton hoverCell = (CellButton) hoverComp;
                            boolean canPlace = board.canPlaceShip(hoverCell.row, hoverCell.col, 
                                                                  draggedShip.length, draggedShip.horizontal);
                            
                            // Check if this cell is part of the preview
                            boolean isPreviewCell = false;
                            for (int i = 0; i < draggedShip.length; i++) {
                                int previewRow = draggedShip.horizontal ? hoverCell.row : hoverCell.row + i;
                                int previewCol = draggedShip.horizontal ? hoverCell.col + i : hoverCell.col;
                                if (previewRow == this.row && previewCol == this.col) {
                                    isPreviewCell = true;
                                    break;
                                }
                            }
                            
                            if (isPreviewCell) {
                                g2d.setColor(canPlace ? new Color(100, 255, 100, 100) : new Color(255, 100, 100, 100));
                                g2d.fillRect(0, 0, getWidth(), getHeight());
                            }
                        }
                    }
                }
            }
        }
    }
    
    private class ShipSelectionPanel extends JPanel {
        private ArrayList<DraggableShipComponent> draggableShips;
        
        public ShipSelectionPanel() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createTitledBorder("Ships to Place"));
            setPreferredSize(new Dimension(220, 600));
            
            draggableShips = new ArrayList<>();
            
            for (int i = 0; i < shipSizes.length; i++) {
                DraggableShipComponent ship = new DraggableShipComponent(
                    shipSizes[i], shipNames[i], shipColors[i], this
                );
                ship.setMaximumSize(new Dimension(200, 80));
                ship.setAlignmentX(Component.LEFT_ALIGNMENT);
                draggableShips.add(ship);
                add(ship);
                add(Box.createVerticalStrut(10));
            }
        }
        
        public void resetShips() {
            for (DraggableShipComponent ship : draggableShips) {
                ship.reset();
            }
            repaint();
        }
        
        public void allShipsPlaced() {
            for (DraggableShipComponent ship : draggableShips) {
                ship.setPlaced(true);
            }
            repaint();
        }
    }
    
    private class DraggableShipComponent extends JPanel {
        int length;
        String name;
        Color color;
        boolean horizontal = true;
        boolean placed = false;
        boolean isBeingDragged = false;
        ShipSelectionPanel parent;
        
        public DraggableShipComponent(int length, String name, Color color, ShipSelectionPanel parent) {
            this.length = length;
            this.name = name;
            this.color = color;
            this.parent = parent;
            
            setPreferredSize(new Dimension(200, 70));
            setMaximumSize(new Dimension(200, 70));
            setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
            setBackground(Color.WHITE);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            MouseAdapter mouseHandler = new MouseAdapter() {
                private Point dragStart;
                
                @Override
                public void mousePressed(MouseEvent e) {
                    if (!placed) {
                        if (SwingUtilities.isRightMouseButton(e)) {
                            horizontal = !horizontal;
                            repaint();
                        } else {
                            isBeingDragged = true;
                            dragStart = e.getPoint();
                            boardPanel.draggedShip = DraggableShipComponent.this;
                        }
                    }
                }
                
                @Override
                public void mouseReleased(MouseEvent e) {
                    if (isBeingDragged) {
                        isBeingDragged = false;
                        
                        // Convert mouse position to board coordinates
                        Point panelPoint = SwingUtilities.convertPoint(
                            DraggableShipComponent.this, e.getPoint(), boardPanel
                        );
                        
                        if (boardPanel.tryPlaceShip(DraggableShipComponent.this, panelPoint)) {
                            placed = true;
                        }
                        
                        boardPanel.draggedShip = null;
                        boardPanel.repaint();
                        repaint();
                    }
                }
                
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (isBeingDragged) {
                        boardPanel.repaint();
                    }
                }
            };
            
            addMouseListener(mouseHandler);
            addMouseMotionListener(mouseHandler);
        }
        
        public void reset() {
            placed = false;
            horizontal = true;
            repaint();
        }
        
        public void setPlaced(boolean p) {
            placed = p;
            repaint();
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (placed) {
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.setFont(new Font("Arial", Font.ITALIC, 12));
                g2d.drawString("✓ " + name + " - Placed", 10, 35);
            } else {
                // Draw label
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("Arial", Font.BOLD, 12));
                g2d.drawString(name, 10, 15);
                
                // Draw ship circles
                int circleSize = 25;
                int startX = 10;
                int startY = 25;
                
                g2d.setColor(color);
                if (horizontal) {
                    for (int i = 0; i < length; i++) {
                        g2d.fillOval(startX + i * (circleSize + 2), startY, circleSize, circleSize);
                    }
                } else {
                    for (int i = 0; i < length; i++) {
                        g2d.fillOval(startX, startY + i * (circleSize + 2), circleSize, circleSize);
                    }
                }
            }
        }
    }
}