import java.util.ArrayList;
import java.util.Random;

abstract class Player {
    protected ArrayList<Ship> ships = new ArrayList<>();
    protected Board board = new Board();
    protected Random rand = new Random();

    public Board getBoard() { return board; }

    public void addShip(Ship s) { ships.add(s); }

    // Return a board for the opponent (hide unhit ships)
    public Board getBoardForOpponent() {
        Board masked = new Board();
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                char val = board.get(r, c);
                if (val == 'H' || val == 'M') masked.set(r, c, val);
                else masked.set(r, c, ' ');
            }
        }
        return masked;
    }

    // Place a ship randomly ensuring no touching (including diagonals)
    protected void placeShipRandomly(int length) {
        boolean placed = false;
        while (!placed) {
            boolean horizontal = rand.nextBoolean();
            int r = rand.nextInt(Board.SIZE);
            int c = rand.nextInt(Board.SIZE);
            if (canPlaceShipNoTouch(r, c, length, horizontal)) {
                board.placeShip(r, c, length, horizontal);
                ships.add(new Ship(length, r, c, horizontal));
                placed = true;
            }
        }
    }

    private boolean canPlaceShipNoTouch(int r, int c, int length, boolean horizontal) {
        for (int i = -1; i <= length; i++) {
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    int rr = r + (horizontal ? dr : i + dr);
                    int cc = c + (horizontal ? i + dc : dc);
                    if (board.inBounds(rr, cc) && board.get(rr, cc) == 'S') return false;
                }
            }
        }
        return board.canPlaceShip(r, c, length, horizontal);
    }

    public abstract void placeShipsAutomatically();
    public abstract int[] chooseShot();
}
