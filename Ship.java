public class Ship {
    private int length;
    private int hits;
    private int startRow, startCol;
    private boolean horizontal;

    public Ship(int length, int startRow, int startCol, boolean horizontal) {
        this.length = length;
        this.startRow = startRow;
        this.startCol = startCol;
        this.horizontal = horizontal;
        this.hits = 0;
    }

    public void hit() { hits++; }
    public boolean isSunk() { return hits >= length; }

    public boolean occupies(int r, int c) {
        for (int i = 0; i < length; i++) {
            int rr = startRow + (horizontal ? 0 : i);
            int cc = startCol + (horizontal ? i : 0);
            if (rr == r && cc == c) return true;
        }
        return false;
    }
}