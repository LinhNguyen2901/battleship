import javax.swing.*;
import java.awt.*;

public class StartScreen extends JFrame {

    public StartScreen() {
        setTitle("Battleship - Start");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("Battleship", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        add(titleLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        JButton humanButton = new JButton("Play vs Human");
        humanButton.setFont(new Font("Arial", Font.BOLD, 16));
        humanButton.addActionListener(e -> startGame(false)); // false = vs human

        JButton computerButton = new JButton("Play vs Computer");
        computerButton.setFont(new Font("Arial", Font.BOLD, 16));
        computerButton.addActionListener(e -> startGame(true)); // true = vs computer

        buttonPanel.add(humanButton);
        buttonPanel.add(computerButton);

        add(buttonPanel, BorderLayout.CENTER);

        setLocationRelativeTo(null); // center screen
        setVisible(true);
    }

    private void startGame(boolean vsComputer) {
        Player player1 = new HumanPlayer();
        Player player2 = vsComputer ? new ComputerPlayer() : new HumanPlayer();

        GameController controller = new GameController(player1, player2);
        controller.setupGame();

        // Open the main game window
        SwingUtilities.invokeLater(() -> {
            GameWindow window = new GameWindow(controller);
            window.setVisible(true);
        });

        // Close the start screen
        this.dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(StartScreen::new);
    }
}
