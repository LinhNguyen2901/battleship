// Manages the 10x10 game board with two grids: shipGrid tracks ship placements,
// and infoGrid tracks shot results (hits, misses, destroyed ships) visible to opponent

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
        for (char[] row : infoGrid) // creates empty info grid
        {
            Arrays.fill(row, ' ');
        }
        for (char[] row : shipGrid) // creates empty ship grid
        {
            Arrays.fill(row, ' ');
        }
    }

    //Getters 
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
    //Setters
    public void setshipGrid(int r, int c, char value) 
    { 
        shipGrid[r][c] = value; 
    }
    public void setInfoGrid(int r, int c, char value) 
    { 
        infoGrid[r][c] = value; 
    }

    public boolean inBounds(int r, int c) 
    {
        return r >= 0 && r < SIZE && c >= 0 && c < SIZE;
    }

public boolean canPlaceShip(int r, int c, int length, boolean horizontal) // returns true if it is valid to place a ship
{
    // First check if the ship would go out of bounds
    if (horizontal) 
    {
        if (c + length > SIZE) 
        {
            return false;
        }
    } 
    else 
    {
        if (r + length > SIZE) 
        {
            return false;
        }
    }
    
    // Then check if all cells are empty
    for (int i = 0; i < length; i++) 
    {
        int checkRow = horizontal ? r : r + i;
        int checkCol = horizontal ? c + i : c;
        
        if (shipGrid[checkRow][checkCol] != ' ') 
        {
            return false;
        }
    }
    
    return true;
}

    public void placeShip(int r, int c, int length, boolean horizontal) // places ship if valid and updates ship grid
    {
        if (canPlaceShip(r,c, length, horizontal))
        {
            for (int i = 0; i < length; i++) 
            {
                shipGrid[r][c] = 'S';   // updates shipGrid to conatian information of where ships are
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
    public boolean allShipsSunk(java.util.List<Ship> ships) 
    {
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
    public void reset() 
    {
        for (char[] row : infoGrid) 
        {
            Arrays.fill(row, ' ');
        }
        for (char[] row : shipGrid) 
        {
            Arrays.fill(row, ' ');
        }
    }
}
