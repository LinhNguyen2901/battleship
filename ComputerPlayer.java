import java.util.*;

public class ComputerPlayer extends Player 
{
    private Random rand = new Random();

    public int[] chooseShot(char [][] opponentBoard) // returns move
    {
        int row, col;
        // looks for a hit
        for (int r=0; r<10; r++)
        {
            for(int c=0; c<10; c++)
            {
                if(opponentBoard[r][c] == 'H')
                // if there is a hit, next move will shoot grid coordinate above, below, left, or right of hit 
                {
                    row = r;
                    col = c;
                    boolean invalidMove = true;
                    while (invalidMove) // chooses randomly amoung above, below, left, and right
                    {
                    int option = rand.nextInt(4); // generate random number 0-3
                    if(option==0) // chooses to shoot coordinate below the hit
                    {
                        if(row+1<10) // does not go out of bounds
                        {
                            row++;
                        }
                    }
                    else if(option==1) // chooses to shoot coordinate above the hit
                    {
                        if(row-1 >=0)
                        {
                            row--;
                        }
                    }
                    else if(option==2) // chooses to shoot coordinate to the left of the hit
                    {
                        if(col-1 >= 0)
                        {
                            col--;
                        }
                    }
                    else if(option==3) // chooses to shoot coordinate to the right of the hit
                    {
                        if(col+1<10)
                        {
                            col++;
                        }
                    }
                    if (opponentBoard[row][col]==' ') // valid move is made if coordinate is empty (no hit or miss)
                    {
                            invalidMove = false;
                    }
                    else
                    {
                        // set row and column back if move was invalid
                        row = r;
                        col = c;
                    }

                    }
                }
            }

        }
 
        // Otherwise, pick random cell
        do {
            row = rand.nextInt(Board.SIZE);      // generate random row
            col = rand.nextInt(Board.SIZE);      // generate random column
        } while (opponentBoard[row][col] !=' '); //find empty cell
        return new int[]{row, col};               // return location
    }
}
