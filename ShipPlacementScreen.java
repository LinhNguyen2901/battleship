// Interactive UI for players to place their 4 ships on the board using drag-and-drop,
// with options for manual placement, rotation, random placement, and validation

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class ShipPlacementScreen extends JFrame 
{
    private Board board;
    private ArrayList<Ship> ships;
    private BoardPanel boardPanel;
    private ShipSelectionPanel shipPanel;
    private JButton confirmButton;
    private JButton randomButton;
    private JButton clearButton;
    private boolean isPlayer1;
    private Runnable onComplete;
    
    // Ship sizes and display information for battleship
    private int[] shipSizes = {5, 4, 3, 2};
    private String[] shipNames = {"Carrier (5)", "Battleship (4)", "Cruiser (3)", "Destroyer (2)"};
    private Color[] shipColors = {
        new Color(50, 100, 200),   // Blue
        new Color(100, 200, 100),  // Green
        new Color(200, 150, 50),   // Orange
        new Color(150, 100, 200),  // Purple
        new Color(200, 100, 150)   // Pink
    };

    /**
     * Constructor initializes the ship placement screen for a player
     * isPlayer1 true if this is player 1, false if player 2
     * onComplete callback to run when ship placement is confirmed
     */
    public ShipPlacementScreen(boolean isP1, Runnable onComp) 
    {
        isPlayer1 = isP1;
        onComplete = onComp;
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
    
    /**
     * Sets up all UI components including title, board, ship selection panel, and buttons
     */
    private void setupUI() 
    {
        // Top panel with instructions
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Player " + (isPlayer1 ? "1" : "2") + " - Place Your Ships", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel instructionLabel = new JLabel("Drag ships from the right panel onto the board. Use rotate buttons to change orientation.", SwingConstants.CENTER);
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
    
    /**
     * Clears all placed ships from the board and resets the UI
     */
    private void clearAllShips() 
    {
        ships.clear();
        board.reset();
        boardPanel.repaint();
        shipPanel.resetShips();
        confirmButton.setEnabled(false);
    }
    
    /**
     * Randomly places all ships on the board
     */
    private void placeShipsRandomly() 
    {
        clearAllShips();
        
        java.util.Random rand = new java.util.Random();
        for (int i = 0; i < shipSizes.length; i++) 
        {
            int size = shipSizes[i];
            boolean placed = false;
            // Keep trying random positions until ship fits
            while (!placed) 
            {
                boolean horizontal = rand.nextBoolean();
                int r = rand.nextInt(Board.SIZE);
                int c = rand.nextInt(Board.SIZE);
                if (board.canPlaceShip(r, c, size, horizontal)) 
                {
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
    
    /**
     * Confirms ship placement and executes the completion callback
     */
    private void confirmPlacement() 
    {
        if (onComplete != null) 
        {
            onComplete.run();
        }
        dispose();
    }
    
    public Board getBoard() 
    {
        return board;
    }
    
    public ArrayList<Ship> getShips() 
    {
        return ships;
    }
    
    /**
     * Panel displaying the 10x10 game board for ship placement
     */
    private class BoardPanel extends JPanel 
    {
        private CellButton[][] buttons = new CellButton[10][10];
        private DraggableShipComponent draggedShip = null;
        
        public BoardPanel() 
        {
            setLayout(new GridLayout(10, 10));
            setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
            setPreferredSize(new Dimension(500, 500));
            
            // Create all cell buttons
            for (int r = 0; r < 10; r++) 
            {
                for (int c = 0; c < 10; c++) 
                {
                    CellButton btn = new CellButton(r, c);
                    btn.setBackground(Color.WHITE);
                    btn.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
                    btn.setOpaque(true);
                    buttons[r][c] = btn;
                    add(btn);
                }
            }
        }
        
        /**
         * Attempts to place a dragged ship at the drop location
         * return true if ship was successfully placed, false otherwise
         */
        public boolean tryPlaceShip(DraggableShipComponent dragShip, Point dropPoint) 
        {
            // Convert point to grid coordinates
            Component comp = getComponentAt(dropPoint);
            if (comp instanceof CellButton) 
            {
                CellButton cell = (CellButton) comp;
                int gridRow = cell.row;
                int gridCol = cell.col;
                
                // Validate and place ship
                if (board.canPlaceShip(gridRow, gridCol, dragShip.length, dragShip.horizontal)) 
                {
                    board.placeShip(gridRow, gridCol, dragShip.length, dragShip.horizontal);
                    Ship ship = new Ship(dragShip.length, gridRow, gridCol, dragShip.horizontal);
                    ships.add(ship);
                    repaint();
                    
                    // Enable confirm button when all ships are placed
                    if (ships.size() == shipSizes.length) 
                    {
                        confirmButton.setEnabled(true);
                        JOptionPane.showMessageDialog(ShipPlacementScreen.this, 
                            "All ships placed! Click 'Confirm' to continue.");
                    }
                    return true;
                }
            }
            return false;
        }
        
        /**
         * Individual cell button on the game board
         */
        private class CellButton extends JButton 
        {
            int row, col;
            
            public CellButton(int r, int c) 
            {
                row = r;
                col = c;
            }
            
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw placed ships as colored circles
                if (board.getShipCoord(row, col) == 'S') {
                    // Find which ship occupies this cell
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
                
                // Show preview while dragging a ship
                if (draggedShip != null && draggedShip.isBeingDragged) {
                    Point boardMouse = BoardPanel.this.getMousePosition();
                    if (boardMouse != null) {
                        Component hoverComp = BoardPanel.this.getComponentAt(boardMouse);
                        if (hoverComp instanceof CellButton) {
                            CellButton hoverCell = (CellButton) hoverComp;
                            boolean canPlace = board.canPlaceShip(hoverCell.row, hoverCell.col, 
                                                                  draggedShip.length, draggedShip.horizontal);
                            
                            // Check if THIS cell is part of the preview
                            boolean isPreviewCell = false;
                            for (int i = 0; i < draggedShip.length; i++) {
                                int previewRow = draggedShip.horizontal ? hoverCell.row : hoverCell.row + i;
                                int previewCol = draggedShip.horizontal ? hoverCell.col + i : hoverCell.col;
                                if (previewRow == this.row && previewCol == this.col) {
                                    isPreviewCell = true;
                                    break;
                                }
                            }
                            
                            // Draw green for valid placement, red for invalid
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
    
    /**
     * Panel displaying draggable ship components that can be placed on the board
     */
    private class ShipSelectionPanel extends JPanel 
    {
        private ArrayList<ShipContainer> shipContainers;
        
        public ShipSelectionPanel() 
        {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createTitledBorder("Ships to Place"));
            setPreferredSize(new Dimension(220, 600));
            
            shipContainers = new ArrayList<>();
            
            // Create container for each ship with rotate button
            for (int i = 0; i < shipSizes.length; i++) 
            {
                ShipContainer container = new ShipContainer(
                    shipSizes[i], shipNames[i], shipColors[i]
                );
                container.setMaximumSize(new Dimension(200, 100));
                container.setAlignmentX(Component.LEFT_ALIGNMENT);
                shipContainers.add(container);
                add(container);
                add(Box.createVerticalStrut(5));
            }
        }
        
        /**
         * Resets all ships to unplaced state
         */
        public void resetShips() 
        {
            for (ShipContainer container : shipContainers) 
            {
                container.ship.reset();
            }
            repaint();
        }
        
        /**
         * Marks all ships as placed (used when random placement is clicked)
         */
        public void allShipsPlaced() 
        {
            for (ShipContainer container : shipContainers) 
            {
                container.ship.setPlaced(true);
            }
            repaint();
        }
    }
    
    /**
     * Container holding a ship and its rotate button
     */
    private class ShipContainer extends JPanel 
    {
        DraggableShipComponent ship;
        JButton rotateBtn;
        
        public ShipContainer(int length, String name, Color color) 
        {
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
            
            // Rotate button at top
            rotateBtn = new JButton("⟳");
            rotateBtn.setFont(new Font("Arial", Font.BOLD, 16));
            rotateBtn.setPreferredSize(new Dimension(40, 25));
            rotateBtn.setMargin(new Insets(0, 0, 0, 0));
            rotateBtn.setFocusPainted(false);
            
            // Ship component in center
            ship = new DraggableShipComponent(length, name, color, this);
            
            // Add rotate functionality
            rotateBtn.addActionListener(e -> {
                if (!ship.placed) {
                    ship.horizontal = !ship.horizontal;
                    ship.repaint();
                    boardPanel.repaint();
                }
            });
            
            // Layout
            JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 2));
            topPanel.setOpaque(false);
            topPanel.add(rotateBtn);
            
            add(topPanel, BorderLayout.NORTH);
            add(ship, BorderLayout.CENTER);
        }
    }
    
    /**
     * Component representing a draggable ship that can be placed on the board
     * Supports drag-and-drop
     */
    private class DraggableShipComponent extends JPanel 
    {
        int length;
        String name;
        Color color;
        boolean horizontal = true;
        boolean placed = false;
        boolean isBeingDragged = false;
        
        public DraggableShipComponent(int l, String n, Color c, ShipContainer parent) 
        {
            length = l;
            name = n;
            color = c;
            
            setPreferredSize(new Dimension(200, 70));
            setBackground(Color.WHITE);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            // Handle mouse events for drag-and-drop
            MouseAdapter mouseHandler = new MouseAdapter() 
            {
                
                public void mousePressed(MouseEvent e) 
                {
                    if (!placed) 
                    {
                        // Left-click starts dragging
                        isBeingDragged = true;
                        boardPanel.draggedShip = DraggableShipComponent.this;
                    }
                }
                
                public void mouseReleased(MouseEvent e) 
                {
                    if (isBeingDragged) 
                    {
                        isBeingDragged = false;
                        
                        // Convert mouse position to board coordinates
                        Point panelPoint = SwingUtilities.convertPoint(
                            DraggableShipComponent.this, e.getPoint(), boardPanel
                        );
                        
                        // Try to place ship at drop location
                        if (boardPanel.tryPlaceShip(DraggableShipComponent.this, panelPoint)) 
                        {
                            placed = true;
                            parent.rotateBtn.setEnabled(false);
                        }
                        
                        boardPanel.draggedShip = null;
                        boardPanel.repaint();
                        repaint();
                    }
                }
                
                public void mouseDragged(MouseEvent e) 
                {
                    if (isBeingDragged) 
                    {
                        boardPanel.repaint(); // Update preview
                    }
                }
            };
            
            addMouseListener(mouseHandler);
            addMouseMotionListener(mouseHandler);
        }
        
        /**
         * Resets ship to unplaced and horizontal state
         */
        public void reset() 
        {
            placed = false;
            horizontal = true;
            repaint();
        }
        
        public void setPlaced(boolean p) 
        {
            placed = p;
            repaint();
        }
        
        @Override
        protected void paintComponent(Graphics g) 
        {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (placed) 
            {
                // Show checkmark when placed
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.setFont(new Font("Arial", Font.ITALIC, 12));
                g2d.drawString("✓ " + name + " - Placed", 10, 35);
            } 
            else 
            {
                // Draw ship name
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("Arial", Font.BOLD, 12));
                g2d.drawString(name, 10, 15);
                
                // Draw ship as colored circles
                int circleSize = 25;
                int startX = 10;
                int startY = 25;
                
                g2d.setColor(color);
                if (horizontal) 
                {
                    // Draw circles horizontally
                    for (int i = 0; i < length; i++) 
                    {
                        g2d.fillOval(startX + i * (circleSize + 2), startY, circleSize, circleSize);
                    }
                } 
                else 
                {
                    // Draw circles vertically
                    for (int i = 0; i < length; i++) 
                    {
                        g2d.fillOval(startX, startY + i * (circleSize + 2), circleSize, circleSize);
                    }
                }
            }
        }
    }
}