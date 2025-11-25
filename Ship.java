public class Ship 
{
    private int length;             // length of ship
    private int hits;               // number of times ship has been hit
    private int startRow, startCol; // row and column where ship begins
    private boolean horizontal;     // stores if ship is placed horizontally or vertically

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
    public boolean isSunk() // returns if all coordinates of ship have been hit
    { 
        return hits >= length; 
    }

    public boolean occupies(int r, int c) // see if a ship is on this cell
    {
        for (int i = 0; i < length; i++) 
        {
            int currentRow = startRow;
            int currentCol = startCol;
            if(horizontal)
            {
                currentCol += i; // increment columns by i if horizontal
            }
            else
            {
                currentRow +=i; // increment row by i if  ship is not horizontal
            }
            if (currentRow == r && currentCol == c) // if the coordinates match, a ship is there
            {
                return true;
            }
        }
        return false;
    }
}