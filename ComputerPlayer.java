// AI player that makes strategic shots by targeting adjacent cells to hits,
// falling back to random shots when no hits are available

import java.util.*;

public class ComputerPlayer extends Player 
{
    private Random rand = new Random();

    public int[] chooseShot(char [][] opponentBoard) // returns move
    {
        int row, col;
        // looks for a hit (that's not destroyed)
        for (int r=0; r<10; r++)
        {
            for(int c=0; c<10; c++)
            {
                if(opponentBoard[r][c] == 'H')
                // if there is a hit, next move will shoot grid coordinate above, below, left, or right of hit 
                {
                    row = r;
                    col = c;
                    
                    // Try to find a valid adjacent cell
                    List<int[]> validMoves = new ArrayList<>();
                    
                    // Check all 4 directions
                    // Below
                    if (row + 1 < 10 && opponentBoard[row + 1][col] == ' ') {
                        validMoves.add(new int[]{row + 1, col});
                    }
                    // Above
                    if (row - 1 >= 0 && opponentBoard[row - 1][col] == ' ') {
                        validMoves.add(new int[]{row - 1, col});
                    }
                    // Right
                    if (col + 1 < 10 && opponentBoard[row][col + 1] == ' ') {
                        validMoves.add(new int[]{row, col + 1});
                    }
                    // Left
                    if (col - 1 >= 0 && opponentBoard[row][col - 1] == ' ') {
                        validMoves.add(new int[]{row, col - 1});
                    }
                    
                    // If there are valid moves adjacent to this hit, pick one randomly
                    if (!validMoves.isEmpty()) {
                        int randomIndex = rand.nextInt(validMoves.size());
                        return validMoves.get(randomIndex);
                    }
                    // Otherwise, continue searching for another hit with available adjacent cells
                }
            }
        }
 
        // No hits found or no valid adjacent cells, pick random cell
        do {
            row = rand.nextInt(Board.SIZE);      // generate random row
            col = rand.nextInt(Board.SIZE);      // generate random column
        } while (opponentBoard[row][col] != ' '); //find empty cell
        return new int[]{row, col};               // return location
    }
}