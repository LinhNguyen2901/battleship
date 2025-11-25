public class HumanPlayer extends Player
{
    private int[] nextShot = null;  // Stores the next shot chosen by the human player

    public int[] chooseShot(char[][] opponentBoard) 
     /* Returns the next shot chosen by the human player.
     * After returning the shot, clears nextShot so it won't be reused.
     * If no shot has been chosen yet, returns null. */
    {
        int[] shot = nextShot;
        nextShot = null; // Clear next shot after retrieving it
        return shot;
    }

    public void setNextShot(int r, int c, char[][] opponentBoard) 
     /* Sets the next shot for the human player.
     * Ensures the player can only select an empty cell (' ') on the opponent's board.
     * If the selected cell is already hit/miss/destroyed, nextShot remains null.*/
    { 
        if(opponentBoard[r][c]==' ') // Only allow empty cells
        {
            nextShot = new int[]{r, c}; 
        }
        else
        {
            nextShot = null; // Invalid cell, ignore the selection
        }
    }
}