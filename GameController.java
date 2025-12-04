// Game flow: switch player, and check win
import java.io.Serializable;

public class GameController implements Serializable 
{
    private Player player1;
    private Player player2;
    private Player currentPlayer;
    private Player opponent;
    public GameController(Player p1, Player p2) 
    {
        player1 = p1;
        player2 = p2;
        currentPlayer = p1;
        opponent = p2;
    }
    public void setupGame() {
        player1.placeShipsAutomatically();
        player2.placeShipsAutomatically();
    }
    // return noMove if the player hasn't done turn. 
    // return turnDone if player is finished and gameOver if opponent lost
    public String takeTurn() {
        char[][] opponentView = opponent.getBoardForOpponent();
        int[] move = currentPlayer.chooseShot(opponentView);
        if (move == null) {
            return "noMove";
        }
        // Check win
        if (opponent.getBoard().allShipsSunk(opponent.getShips())) {
            return "gameOver";
        }
        switchPlayers();
        return "turnDone";
    }
    public Player getCurrentPlayer() {
        return currentPlayer;
    }
    public Player getOpponent() {
        return opponent;
    }
    public Player getPlayer1() {
        return player1;
    }
    public Player getPlayer2() {
        return player2;
    }
    public void switchPlayers() {
        Player temp = currentPlayer;
        currentPlayer = opponent;
        opponent = temp;
    }
    public void setPlayers(Player p1, Player p2) {
        player1 = p1;
        player2 = p2;
        currentPlayer = p1; 
    }
}
