import java.util.Arrays;

public class Board {
    public static final int SIZE = 10;
    private char[][] grid; // ' ' empty, 'S' ship, 'H' hit, 'M' miss

    public Board() {
        grid = new char[SIZE][SIZE];
        for (char[] row : grid) Arrays.fill(row, ' ');
    }

    public char get(int r, int c) { return grid[r][c]; }
    public void set(int r, int c, char value) { grid[r][c] = value; }

    public boolean inBounds(int r, int c) {
        return r >= 0 && r < SIZE && c >= 0 && c < SIZE;
    }

    public boolean canPlaceShip(int r, int c, int length, boolean horizontal) {
        for (int i = 0; i < length; i++) {
            int rr = r + (horizontal ? 0 : i);
            int cc = c + (horizontal ? i : 0);
            if (!inBounds(rr, cc) || grid[rr][cc] != ' ') return false;
        }
        return true;
    }

    public void placeShip(int r, int c, int length, boolean horizontal) {
        for (int i = 0; i < length; i++) {
            int rr = r + (horizontal ? 0 : i);
            int cc = c + (horizontal ? i : 0);
            grid[rr][cc] = 'S';
        }
    }

    // Record a shot: returns true if hit
    public boolean shoot(int r, int c) {
        if (grid[r][c] == 'S') { grid[r][c] = 'H'; return true; }
        if (grid[r][c] == ' ') { grid[r][c] = 'M'; }
        return false;
    }

    // Get a view for the opponent (hide unhit ships)
    public char getForOpponent(int r, int c) {
        char val = grid[r][c];
        if (val == 'H') return 'H';
        if (val == 'M') return 'M';
        return ' ';
    }

    // Check if all ships in a list are sunk
    public boolean allShipsSunk(java.util.List<Ship> ships) {
        for (Ship s : ships) if (!s.isSunk()) return false;
        return true;
    }
}
