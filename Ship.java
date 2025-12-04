// Ships tracking their length, position, orientation,
// hit count, and whether they occupy specific board coordinates

import java.io.Serializable;

public class Ship implements Serializable
{
    private int length;             // length of ship
    private int hits;               // number of times ship has been hit
    private int startRow, startCol; //start grid
    private boolean horizontal;     //direction 

    public Ship(int l, int r, int c, boolean h) 
    {
        length = l;
        startRow = r;
        startCol = c;
        horizontal = h;
        hits = 0;
    }

    public void hit() 
    {
         hits++; 
    }
    public int getLength()
    {
        return length;
    }
    public boolean isHorizontal()
    {
        return horizontal==true;
    }
    public int getStartRow()
    {
        return startRow;
    }
    public int getStartCol()
    {
        return startCol;
    }
    public boolean isSunk()
    { 
        return hits >= length; 
    }
    
    public int getHits() 
    {
        return hits;
    }
    
    public void setHits(int h) 
    {
        hits = h;
    }

    //check if a ship on that cell ()
    public boolean occupies(int r, int c)
    {
        for (int i = 0; i < length; i++) 
        {
            int currentRow = startRow;
            int currentCol = startCol;
            if(horizontal)
            {
                currentCol += i;
            }
            else
            {
                currentRow +=i;
            }
            if (currentRow == r && currentCol == c)
            {
                return true;
            }
        }
        return false;
    }
}