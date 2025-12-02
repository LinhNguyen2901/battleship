import java.io.Serializable;

public class GameController implements Serializable {

    private Player player1;
    private Player player2;
    private Player currentPlayer;
    private Player opponent;

    public GameController(Player p1, Player p2) {
        this.player1 = p1;
        this.player2 = p2;
        this.currentPlayer = p1;
        this.opponent = p2;
    }

    public void setupGame() {
        player1.placeShipsAutomatically();
        player2.placeShipsAutomatically();
    }

    /** 
     * Attempts to play a turn.
     * @return "noMove" if human hasn't clicked;
     *         "turnDone" if turn finished;
     *         "gameOver" if opponent lost.
     */
    public String takeTurn() {

        char[][] opponentView = opponent.getBoardForOpponent();

        int[] move = currentPlayer.chooseShot(opponentView);

        if (move == null) {
            return "noMove"; // waiting for human click
        }

        int r = move[0];
        int c = move[1];

        boolean hit = opponent.getBoard().shoot(r, c, opponent.getShips());
        System.out.println((hit ? "HIT" : "MISS") + " at (" + r + "," + c + ")");

        // Check win
        if (opponent.getBoard().allShipsSunk(opponent.getShips())) {
            return "gameOver";
        }

        // Switch players
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
        this.player1 = p1;
        this.player2 = p2;
        this.currentPlayer = p1; 
    }
}
