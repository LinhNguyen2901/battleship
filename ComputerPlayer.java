import java.util.*;

public class ComputerPlayer extends Player {
    private Random rand = new Random();
    private LinkedList<int[]> targetQueue = new LinkedList<>(); // coordinates to try next
    private Set<String> tried = new HashSet<>(); // avoid shooting same cell twice

    @Override
    public void placeShipsAutomatically() {
        int[] shipSizes = {5, 4, 3, 3, 2};
        for (int size : shipSizes) {
            boolean placed = false;
            while (!placed) {
                int r = rand.nextInt(Board.SIZE);
                int c = rand.nextInt(Board.SIZE);
                boolean horizontal = rand.nextBoolean();
                if (board.canPlaceShip(r, c, size, horizontal)) {
                    board.placeShip(r, c, size, horizontal);
                    ships.add(new Ship(size, r, c, horizontal));
                    placed = true;
                }
            }
        }
    }

    @Override
    public int[] chooseShot() {
        // If there are target coordinates in queue, try them first
        while (!targetQueue.isEmpty()) {
            int[] next = targetQueue.poll();
            String key = next[0] + "," + next[1];
            if (!tried.contains(key)) {
                tried.add(key);
                return next;
            }
        }

        // Otherwise, pick random untried cell
        int r, c;
        do {
            r = rand.nextInt(Board.SIZE);
            c = rand.nextInt(Board.SIZE);
        } while (tried.contains(r + "," + c));

        tried.add(r + "," + c);
        return new int[]{r, c};
    }

    /** Call this after making a move */
    public void handleShotResult(int r, int c, boolean hit) {
        if (!hit) return;

        // Add adjacent cells to target queue
        int[][] directions = {{-1,0},{1,0},{0,-1},{0,1}};
        for (int[] dir : directions) {
            int nr = r + dir[0];
            int nc = c + dir[1];
            if (nr >= 0 && nr < Board.SIZE && nc >= 0 && nc < Board.SIZE) {
                String key = nr + "," + nc;
                if (!tried.contains(key)) {
                    targetQueue.add(new int[]{nr, nc});
                }
            }
        }
    }
}
