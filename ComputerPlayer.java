// computer player that makes strategic shots by targeting adjacent cells to hits,
// detecting ship direction from consecutive hits, and pursuing along that direction

import java.util.*;

public class ComputerPlayer extends Player 
{
    private Random rand = new Random();

    public int[] chooseShot(char [][] opponentBoard)
    {
        // First priority: Look for a line of 2+ hits to detect direction
        int[] directedShot = findDirectedShot(opponentBoard);
        if (directedShot != null) {
            return directedShot;
        }
        // Second priority: Look for a single hit and shoot adjacent
        int[] adjacentShot = findAdjacentToHit(opponentBoard);
        if (adjacentShot != null) {
            return adjacentShot;
        }
        //pick random cell
        int row, col;
        do {
            row = rand.nextInt(Board.SIZE);
            col = rand.nextInt(Board.SIZE);
        } while (opponentBoard[row][col] != ' ');
        return new int[]{row, col};
    }
    
    //if detect 2+ hits => detect direction and make guesses
    private int[] findDirectedShot(char[][] board) {
        //horizontal direction for ship
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 9; c++) {
                if (board[r][c] == 'H' && board[r][c + 1] == 'H') {
                    // Try shooting to the right
                    int endCol = c + 1;
                    while (endCol < 9 && board[r][endCol + 1] == 'H') {
                        endCol++;
                    }
                    if (endCol + 1 < 10 && board[r][endCol + 1] == ' ') {
                        return new int[]{r, endCol + 1};
                    }
                    // Try shooting to the left
                    if (c - 1 >= 0 && board[r][c - 1] == ' ') {
                        return new int[]{r, c - 1};
                    }
                }
            }
        }
        // Look for vertical lines of hits
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 10; c++) {
                if (board[r][c] == 'H' && board[r + 1][c] == 'H') {
                    // Try shooting below
                    int endRow = r + 1;
                    while (endRow < 9 && board[endRow + 1][c] == 'H') {
                        endRow++; 
                    }
                    if (endRow + 1 < 10 && board[endRow + 1][c] == ' ') {
                        return new int[]{endRow + 1, c};
                    }
                    // Try shooting upward
                    if (r - 1 >= 0 && board[r - 1][c] == ' ') {
                        return new int[]{r - 1, c};
                    }
                }
            }
        }
        
        return null;
    }
    
    //choose adjacent cell to make a guess
    //find valdi adjacent cell through all directions and pick a random valid
    private int[] findAdjacentToHit(char[][] board) {
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                if (board[r][c] == 'H') {
                    List<int[]> validMoves = new ArrayList<>();
                    if (r + 1 < 10 && board[r + 1][c] == ' ') {
                        validMoves.add(new int[]{r + 1, c});
                    }
                    if (r - 1 >= 0 && board[r - 1][c] == ' ') {
                        validMoves.add(new int[]{r - 1, c});
                    }
                    if (c + 1 < 10 && board[r][c + 1] == ' ') {
                        validMoves.add(new int[]{r, c + 1});
                    }
                    if (c - 1 >= 0 && board[r][c - 1] == ' ') {
                        validMoves.add(new int[]{r, c - 1});
                    }
                    if (!validMoves.isEmpty()) {
                        int randomIndex = rand.nextInt(validMoves.size());
                        return validMoves.get(randomIndex);
                    }
                }
            }
        }
        return null;
    }
}