import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        Player player1 = new HumanPlayer();
        Player player2 = new HumanPlayer(); // change to ComputerPlayer() if you want AI
        GameController controller = new GameController(player1, player2);
        // Place ships automatically
        controller.setupGame();

        // Launch GUI
        SwingUtilities.invokeLater(() -> {
            GameWindow window = new GameWindow(controller);
            window.setVisible(true);
        });
    }
}
