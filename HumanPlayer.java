public class HumanPlayer extends Player {
    private int[] nextShot = null;

    @Override
    public void placeShipsAutomatically() {
        int[] shipSizes = {5, 4, 3, 3, 2};
        for (int size : shipSizes) placeShipRandomly(size);
    }

    @Override
    public int[] chooseShot() {
        while (nextShot == null) {
            try { Thread.sleep(20); } catch (Exception ignored) {}
        }
        int[] shot = nextShot;
        nextShot = null;
        return shot;
    }

    public void setNextShot(int r, int c) { nextShot = new int[]{r, c}; }
}
