// Main game window which displays both boards and can make guesses

import javax.swing.*;
import javax.swing.Timer;
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
    private JPanel boardsPanel;            // Reference to boards panel for overlay
    private JLabel myBoardLabel;
    private JLabel opponentBoardLabel;
    public GameWindow(GameController controller) {
        this.controller = controller;
        setTitle("Battleship Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLayout(new BorderLayout());

        // Top: player info, ships, remaining ships
        JPanel topPanel = new JPanel(new BorderLayout());
        statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 22));
        shipsInfoLabel = new JLabel("", SwingConstants.CENTER);
        shipsInfoLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        topPanel.add(statusLabel, BorderLayout.NORTH);
        topPanel.add(shipsInfoLabel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // Middle: boards (current player ships and opponent (for making guess))
        boardsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        boardsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Left board: ship board of the current player
        JPanel leftPanel = new JPanel(new BorderLayout());
        myBoardLabel = new JLabel("Your Ships", SwingConstants.CENTER);
        myBoardLabel.setFont(new Font("Arial", Font.BOLD, 18)); 
        leftPanel.add(myBoardLabel, BorderLayout.NORTH);
        myBoardPanel = new BoardPanel(controller, true);
        leftPanel.add(myBoardPanel, BorderLayout.CENTER);
        boardsPanel.add(leftPanel);

        // Right board: guess board
        JPanel rightPanel = new JPanel(new BorderLayout());
        opponentBoardLabel = new JLabel("Opponent Board (Click to shoot)", SwingConstants.CENTER);
        opponentBoardLabel.setFont(new Font("Arial", Font.BOLD, 18)); 
        rightPanel.add(opponentBoardLabel, BorderLayout.NORTH);
        opponentBoardPanel = new BoardPanel(controller, false);
        rightPanel.add(opponentBoardPanel, BorderLayout.CENTER);
        boardsPanel.add(rightPanel);
        add(boardsPanel, BorderLayout.CENTER);

        // Buttons
        ButtonPanel buttonPanel = new ButtonPanel(
                e -> goHome(),
                e -> restartGame(),
                e -> saveGame()
        );
        this.switchButton = buttonPanel.newGameButton;
        add(buttonPanel, BorderLayout.SOUTH);
        setLocationRelativeTo(null);
        updateStatus();
    }

    //go back to home screen and discard all current game information
    private void goHome() {
        this.dispose();
        new StartScreen();
    }

    // save game to .bsg file (including board state and also player turn)
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
                // Save current player
                int currentPlayerNum = (controller.getCurrentPlayer() == controller.getPlayer1()) ? 1 : 2;
                writer.println("CURRENT_PLAYER:" + currentPlayerNum);
                savePlayerState(writer, controller.getPlayer1(), 1);
                savePlayerState(writer, controller.getPlayer2(), 2);
                writer.close();
                JOptionPane.showMessageDialog(this, "Game saved successfully!");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving game: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    //helper function to save player information like ship counts and stuff
    private void savePlayerState(PrintWriter writer, Player player, int playerNum) {
        writer.println("PLAYER_" + playerNum + "_START");
        // Save ships
        writer.println("SHIPS_COUNT:" + player.getShips().size());
        for (Ship ship : player.getShips()) {
            writer.println("SHIP:" + ship.getLength() + "," + ship.getStartRow() + "," + ship.getStartCol() + "," + ship.isHorizontal() + "," + ship.getHits());
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
    

    
    //restart game
    private void restartGame() {
    //check if game against computer
    boolean wasVsComputer = (controller.getPlayer2() instanceof ComputerPlayer);
    this.dispose();
    // Create new players
    Player newPlayer1 = new HumanPlayer();
    Player newPlayer2 = wasVsComputer ? new ComputerPlayer() : new HumanPlayer();
    GameController newController = new GameController(newPlayer1, newPlayer2);
    
    // ship placement for player 1 (both computer and human)
    new ShipPlacementScreen(true, () -> {
        for (Window window : Window.getWindows()) {
            if (window instanceof ShipPlacementScreen && window.isVisible()) {
                ShipPlacementScreen screen = (ShipPlacementScreen) window;
                for (Ship s : screen.getShips()) {
                    newPlayer1.addShip(s);
                    newPlayer1.getBoard().placeShip(s.getStartRow(), s.getStartCol(), s.getLength(), s.isHorizontal());
                }
                break;
            }
        }
        // player 2
        //if computer then just make it automatic
        if (wasVsComputer) {
            newPlayer2.placeShipsAutomatically();
            startNewGameWindow(newController);
        } else {
            new ShipPlacementScreen(false, () -> {
                for (Window window : Window.getWindows()) {
                    if (window instanceof ShipPlacementScreen && window.isVisible()) {
                        ShipPlacementScreen screen = (ShipPlacementScreen) window;
                        for (Ship s : screen.getShips()) {
                            newPlayer2.addShip(s);
                            newPlayer2.getBoard().placeShip(s.getStartRow(), s.getStartCol(), s.getLength(), s.isHorizontal());
                        }
                        break;
                    }
                }
                startNewGameWindow(newController);
            });
        }
    });
}

    // start new game
    private void startNewGameWindow(GameController newController) {
        SwingUtilities.invokeLater(() -> {
            GameWindow window = new GameWindow(newController);
            window.setVisible(true);
        });
    }


    // update status: current player, ship, guess
    private void updateStatus() {
        Player current = controller.getCurrentPlayer();
        Player opponent = controller.getOpponent();
        int currentPlayerNumber = (current == controller.getPlayer1()) ? 1 : 2;
        int myShipsRemaining = countRemainingShips(current);
        int opponentShipsRemaining = countRemainingShips(opponent);
        String shipsInfo = getShipsInfo(current);
        // board labels
        boolean isVsComputer = (controller.getPlayer1() instanceof ComputerPlayer || controller.getPlayer2() instanceof ComputerPlayer);
        if (isVsComputer) {
            // ifcurrent player is computer then always player 1 point of view
            if (current instanceof ComputerPlayer) {
                myBoardLabel.setText("Computer Board");
                opponentBoardLabel.setText("Your Ships");
            } else {
                myBoardLabel.setText("Your Ships");
                opponentBoardLabel.setText("Computer Board");
            }
        } 
        else {
            myBoardLabel.setText("Your Ships");
            opponentBoardLabel.setText("Opponent Board");
        }
        // Check  game end like win or lose
        if (opponent.getBoard().allShipsSunk(opponent.getShips())) {
            if (current instanceof ComputerPlayer) {
                statusLabel.setText("Game Over - Computer Wins! ");
            } else {
                statusLabel.setText(" Congratulations! You Win! ");
            }
            shipsInfoLabel.setText("Your ships: " + shipsInfo + " | Remaining: " + myShipsRemaining);
            opponentBoardPanel.setEnabled(false);
        } else {
            if (current instanceof ComputerPlayer) {
                statusLabel.setText("Computer's Turn");
            } else {
                statusLabel.setText("Player " + currentPlayerNumber + "'s Turn");
            }
            shipsInfoLabel.setText("Your ships: " + shipsInfo + " | Your remaining: " + myShipsRemaining + " | Opponent remaining: " + opponentShipsRemaining);
        }
        switchButton.setEnabled(true);
    }

    //switch player screen (use an overlay to the entire window and disable interaction)
    private void showSwitchPlayerScreen() {
        SwitchPlayerScreen screen = new SwitchPlayerScreen(controller, () -> {
            JComponent glassPane = (JComponent) getGlassPane();
            glassPane.setVisible(false);
            myBoardPanel.setEnabled(true);
            opponentBoardPanel.setEnabled(true);
            myBoardPanel.updateBoard();
            opponentBoardPanel.updateBoard();
            updateStatus();
        });
        
        // Overlay entire window
        JComponent glassPane = (JComponent) getGlassPane();
        glassPane.setLayout(new BorderLayout());
        glassPane.removeAll();
        glassPane.add(screen, BorderLayout.CENTER);
        glassPane.setVisible(true);
        glassPane.revalidate();
        glassPane.repaint();
        // Disable interaction 
        myBoardPanel.setEnabled(false);
        opponentBoardPanel.setEnabled(false);
    }

    //present information of ships like length and and number
    private String getShipsInfo(Player player) {
        Map<Integer, Integer> shipCounts = new HashMap<>();
        for (Ship s : player.getShips()) {
            int length = s.getLength();
            shipCounts.put(length, shipCounts.getOrDefault(length, 0) + 1);
        }
        
        java.util.List<String> infoParts = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : shipCounts.entrySet()) {
            infoParts.add(entry.getValue() + " ship of " + entry.getKey());
        }
        return String.join(", ", infoParts);
    }

    private int countRemainingShips(Player player) {
        int count = 0;
        for (Ship s : player.getShips()) {
            if (!s.isSunk()) {
                count += 1;
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
                    //can only clck on opponent board like making guess
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
                // if cell already guessed just do nothing
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
                
                controller.switchPlayers();
                opponentBoardPanel.updateBoard();
                controller.switchPlayers();
                // Show a message to player after each guess
                if (result.equals("gameOver")) {
                    myBoardPanel.updateBoard();
                    opponentBoardPanel.updateBoard();
                    opponentBoardPanel.setEnabled(false);
                    updateStatus();
                    JOptionPane.showMessageDialog(GameWindow.this, "Player " + (controller.getCurrentPlayer() == controller.getPlayer1() ? 1 : 2) + " wins!");
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
                        new Object[]{"OK"},
                        "OK"
                    );
                    if (choice == 0) {
                        if (controller.getCurrentPlayer() instanceof ComputerPlayer) {
                            // Computer plays automatically
                            playComputerTurn();
                        } else {
                            // Show switch player screen
                            showSwitchPlayerScreen();
                        }
                    }
                }
            }
        }

        private void playComputerTurn() {
            // Disable opponent board during computer's turn
            opponentBoardPanel.setEnabled(false);
            // when its computer turn
            statusLabel.setText("Computer's Turn");
            Player humanPlayer = controller.getOpponent();
            int humanShipsRemaining = countRemainingShips(humanPlayer);
            int computerShipsRemaining = countRemainingShips(controller.getCurrentPlayer());
            shipsInfoLabel.setText("Your remaining: " + humanShipsRemaining + " | Computer remaining: " + computerShipsRemaining);
            // default 1,5s for computer turn
            Timer timer = new Timer(1500, e -> {
                String result = controller.takeTurn();
                // Update boards after computer turn
                myBoardPanel.updateBoard();
                opponentBoardPanel.updateBoard();
                if (result.equals("gameOver")) {
                    myBoardLabel.setText("Computer Board");
                    opponentBoardLabel.setText("Your Board");
                    opponentBoardPanel.setEnabled(false);
                    statusLabel.setText("You lose! Computer Wins!");
                    int humanRemaining = countRemainingShips(controller.getCurrentPlayer());
                    int computerRemaining = countRemainingShips(controller.getOpponent());
                    shipsInfoLabel.setText("Your remaining: " + humanRemaining + " | Computer remaining: " + computerRemaining);
                    JOptionPane.showMessageDialog(GameWindow.this, "You lose! Computer wins!");
                } else {
                    //enable user to make guess
                    opponentBoardPanel.setEnabled(true);
                    updateStatus();
                }
            });
            timer.setRepeats(false);
            timer.start();
        }
        //update board (with red X for hit and blue for miss)
        public void updateBoard() {
            Player current = controller.getCurrentPlayer();
            Player opponent = controller.getOpponent();
            
            //show my board ship information (oppoenent's guess)
            if (isMyBoard) {
                char[][] infoGrid = current.getBoard().getInfoGrid();
                java.util.List<Ship> ships = current.getShips();
                for (int r = 0; r < 10; r++) {
                    for (int c = 0; c < 10; c++) {
                        CellButton btn = buttons[r][c];
                        btn.ship = null;
                        btn.shipIndex = -1;
                        btn.isHit = false;
                        btn.isOpponentShot = false;
                        btn.setBackground(Color.WHITE);
                    }
                }
                //Each button is a ship info
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
                // Mark opponent shots on our board
                char[][] opponentGuesses = current.getBoard().getInfoGrid();
                for (int r = 0; r < 10; r++) {
                    for (int c = 0; c < 10; c++) {
                        char cell = opponentGuesses[r][c];
                        if (cell == 'H' || cell == 'M' || cell == 'D') {
                            buttons[r][c].isOpponentShot = true;
                        }
                    }
                }
                // Repaint buttons
                for (int r = 0; r < 10; r++) {
                    for (int c = 0; c < 10; c++) {
                        buttons[r][c].repaint();
                    }
                }
            } else {
                // show guesses on opponeent board
                char[][] view = opponent.getBoardForOpponent();
                for (int r = 0; r < 10; r++) {
                    for (int c = 0; c < 10; c++) {
                        CellButton btn = buttons[r][c];
                        char cell = view[r][c];
                        btn.ship = null;
                        btn.shipIndex = -1;
                        btn.isHitMarked = false;
                        switch (cell) {
                            case 'H':       //for hit
                                btn.isHit = true;
                                btn.isHitMarked = true;
                                btn.setBackground(new Color(255, 200, 200));
                                break;
                            case 'M':       //for miss
                                btn.isHit = false;
                                btn.setBackground(new Color(200, 200, 255));
                                break;
                            case 'D':       //for destroy/sunk
                                btn.isHit = true;
                                btn.isHitMarked = true;
                                btn.setBackground(new Color(255, 120, 120));
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
    
    //ship gui
    private class CellButton extends JButton {
        Ship ship = null;
        int shipIndex = -1;
        boolean isHit = false;
        boolean isOpponentShot = false; 
        boolean isHitMarked = false; 
        //different colors for ships
        private Color[] shipColors = {
            new Color(50, 100, 200),   // Blue
            new Color(100, 200, 100),  // Green
            new Color(200, 150, 50),   // Orange
            new Color(150, 100, 200),  // Purple
            new Color(200, 100, 150)   // Pink
        };
        
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            if (ship != null) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int width = getWidth();
                int height = getHeight();
                int padding = 2;
                Color shipColor = shipColors[shipIndex % shipColors.length];
                if (isHit) {
                    shipColor = Color.RED;
                }
                // Draw circle for ship
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
            } else if (isOpponentShot) {
                // Draw opponent shots on opponent board as grey X
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(150, 150, 150)); 
                g2d.setStroke(new BasicStroke(2));
                int width = getWidth();
                int height = getHeight();
                int padding = 6;
                g2d.drawLine(padding, padding, width - padding, height - padding);
                g2d.drawLine(width - padding, padding, padding, height - padding);
            }
            // Draw opponent shots on our board as grey X (my board)
            if (isOpponentShot && ship == null) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(150, 150, 150)); 
                g2d.setStroke(new BasicStroke(2));
                int width = getWidth();
                int height = getHeight();
                int padding = 6;
                g2d.drawLine(padding, padding, width - padding, height - padding);
                g2d.drawLine(width - padding, padding, padding, height - padding);
            }
            // Draw X on opponent board when we hit
            if (isHitMarked) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(2));
                int width = getWidth();
                int height = getHeight();
                int padding = 8;
                g2d.drawLine(padding, padding, width - padding, height - padding);
                g2d.drawLine(width - padding, padding, padding, height - padding);
            }
        }
    }
}
