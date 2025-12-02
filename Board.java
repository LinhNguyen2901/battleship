import java.io.Serializable;
import java.util.Arrays;

public class Board implements Serializable{
    public static final int SIZE = 10;
    private char[][] infoGrid; // ' ' empty, 'H' hit, 'M' miss, 'D' destroy // what oppponent sees
    private char[][] shipGrid; // ' ' empty, 'S' ship // what player sees

    public Board() 
    {
        infoGrid = new char[SIZE][SIZE];
        shipGrid = new char[SIZE][SIZE];
        for (char[] row : infoGrid) // creates empty grid
        {
            Arrays.fill(row, ' ');
        }
        for (char[] row : shipGrid) // creates empty grid
        {
            Arrays.fill(row, ' ');
        }
    }
    public char[][] getInfoGrid()
    {
        return infoGrid;
    }
    public char[][] getShipGrid()
    {
        return shipGrid;
    }
    public char getInfoCoord(int r, int c) 
    { 
        return infoGrid[r][c]; 
    }
    public char getShipCoord(int r, int c) 
    { 
        return shipGrid[r][c]; 
    }
    public void setshipGrid(int r, int c, char value) 
    { 
        shipGrid[r][c] = value; 
    }
    public void setInfoGrid(int r, int c, char value) 
    { 
        infoGrid[r][c] = value; 
    }

    public boolean inBounds(int r, int c) {
        return r >= 0 && r < SIZE && c >= 0 && c < SIZE;
    }

    public boolean canPlaceShip(int r, int c, int length, boolean horizontal) 
    {
      for(int i=0; i<length; i++)
      {
        if (r >= 0 && r < SIZE && c >= 0 && c < SIZE)
        {
            if (shipGrid[r][c] !=' ') // cannot place a ship if it's full
            {
                return false;

            }
            if (horizontal) // if ship is horizontal, increment columns
            {
                if ((c+1) < SIZE)
                {
                    c++;
                }
                else
                {
                    return false;
                }
            }
            else
            {
                if ((r+1) < SIZE) // if ship is vertical, increment rows
                {
                    ++r;
                }
                else
                {
                    return false;
                }
            }
        }
      }
      return true;

    }

    public void placeShip(int r, int c, int length, boolean horizontal) 
    {
        if (canPlaceShip(r,c, length, horizontal)) // places ship. updates ship grid
        {
            for (int i = 0; i < length; i++) 
            {
                shipGrid[r][c] = 'S';
                if (horizontal)
                {
                    c++;
                }
                else
                {
                    r++;
                }
            }
        }

    }

    // Record a shot: returns true if hit
    public boolean shoot(int r, int c, java.util.List<Ship> ships) 
    {
        if (shipGrid[r][c] == 'S') // if there is a ship at the location, it is marked as hit
        { 
            infoGrid[r][c] = 'H'; 
            for (Ship s : ships) // If all locations of a ship are hit, it is set to destroyed
            {
              if (s.occupies(r, c))
              {
                s.hit();
                if (s.isSunk()) 
                {
                    int sRow = s.getStartRow();
                    int sCol = s.getStartCol();
                    for(int i =0; i< s.getLength(); i++)
                    {
                        infoGrid[sRow][sCol]='D';
                        if (s.isHorizontal())
                        {
                            sCol++;
                        }
                        else
                        {
                            sRow++;
                        } 

                    }
                }
                break;
              }
        }
            return true; 
        }
        else
        { 
            infoGrid[r][c] = 'M'; // if location is empty, it is a miss
            return false;
        }
    }

    // Get a view for the opponent (shows hit, miss, destroy but does not show location of ships)
    public char getForOpponent(int r, int c) 
    {
        return infoGrid[r][c];
    }

    // Check if all ships in a list are sunk
    public boolean allShipsSunk(java.util.List<Ship> ships) {
        for (Ship s : ships)
        {
            if (!s.isSunk())
            {
                return false;
            }
        }
        return true;
    }
    
    // Reset board (for new game)
    public void reset() {
        for (char[] row : infoGrid) {
            Arrays.fill(row, ' ');
        }
        for (char[] row : shipGrid) {
            Arrays.fill(row, ' ');
        }
    }
}
