// Abstract base class for all players providing ship management, board access,
// and automatic ship placement functionality

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

abstract class Player implements Serializable{
    protected ArrayList<Ship> ships = new ArrayList<Ship>();
    protected Board board = new Board();
    private Random rand = new Random();

    public Board getBoard() 
    { 
        return board;
    }

    // Return a board for the opponent (shows hits, misses, and destroy)
    public char[][] getBoardForOpponent() 
    {
        return board.getInfoGrid();
    }

    public void addShip(Ship s) // adds ship to arrayList
    { 
        ships.add(s); 
    }

    protected void placeShipAutomatically(int length) // places a ship randomly
    {
        boolean placed = false;
        while (!placed) 
        {
            boolean horizontal = rand.nextBoolean(); // randomly chooses if horizontal
            int r = rand.nextInt(Board.SIZE);        // randomly chooses row
            int c = rand.nextInt(Board.SIZE);        // randomly chooses column
            if (board.canPlaceShip(r, c, length, horizontal)) // checks if ship would stay in bounds if placed there
            {
                board.placeShip(r, c, length, horizontal);      // updates shipGrid array
                placed = true;
                Ship s = new Ship(length, r, c, horizontal);
                ships.add(s);                                   // adds ship to arrayList
            }
        }
    }
    public void placeShipsAutomatically() 
    {
        int[] shipSizes = {5, 4, 3, 2}; // every user has 5 ships, this lists their sizes
        for (int size : shipSizes) 
        {
            placeShipAutomatically(size); // place each ship of size in shipSizes array
        }
    }

    public ArrayList<Ship> getShips() {
        return ships;
    }
    
    // Reset player
    public void reset() {
        ships.clear();
        board.reset();
    }

    public abstract int[] chooseShot(char [][] opponentBoard); 
}
