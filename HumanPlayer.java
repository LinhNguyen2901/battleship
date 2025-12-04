// Human player 
public class HumanPlayer extends Player
{
    private int[] nextShot = null;  // Stores the next shot
    //return the shot chosen by player
    public int[] chooseShot(char[][] opponentBoard) {
        int[] shot = nextShot;
        nextShot = null;
        return shot;
    }
    //set next shot (can only be an empty cell)
    public void setNextShot(int r, int c, char[][] opponentBoard) { 
        if(opponentBoard[r][c]==' ') 
            nextShot = new int[]{r, c}; 
        else
            nextShot = null; 
    }
}