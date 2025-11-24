public class Ship 
{
    private int length;
    private int hits;
    private int startRow, startCol;
    private boolean horizontal;

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
    public boolean isSunk() 
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