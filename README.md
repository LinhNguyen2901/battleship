# Battleship Game - README

## Team Members
Sofia Bailey
Linh Nguyen

## Game Description
This game implements the Battleship board game in Java with a graphical user interface. Players place 4 ships on a 10x10 grid and take turns shooting at their opponent's board to sink all enemy ships.

## How to Run
```
java -jar hwx.jar
```

Or compile and run from source:
```
javac *.java
java Main
```

## Game Rules
- Each player has 4 ships with the following sizes:
  - Carrier: 5 squares
  - Battleship: 4 squares
  - Cruiser: 3 squares
  - Destroyer: 2 squares
- Ships can be placed horizontally or vertically on a 10x10 grid
- Players take turns shooting at grid coordinates
- A ship is sunk when all of its squares have been hit
- First player to sink all opponent ships wins

Rules were from this link:
https://instructions.hasbro.com/en-ca/instruction/battleship-game

## How to Use the Interface

### Start Screen
- **Human vs Human**: Two players take turns on the same computer
- **Play vs Computer**: Play against a computer opponent

### Ship Placement Screen
Players can place ships in three ways:
1. **Drag and Drop**: Drag ships from the right panel onto the board
   - Hit the rotate button to rotate a ship horizontally/vertically
   - Green preview = valid placement
   - Red preview = invalid placement
2. **Random Button**: Automatically place all ships randomly
3. **Clear All Button**: Remove all placed ships and start over

Once all 4 ships are placed, click **Confirm** to begin the game.

### Game Screen
**Left Board**: Your ships
- Colored circles = your ships
- Red circles with X = your ships that have been hit
- Grey X = opponent's missed shots

**Right Board**: Opponent's board (where you shoot)
- Light blue cells = your missed shots
- Light red cells = your hits
- Darker red cells = destroyed ship sections
- Click any empty white cell to shoot

**Controls**:
- **Home**: Return to start screen
- **Save Game**: Save current game state to a .bsg file
- **Load Game**: Load a previously saved game
- **New Game**: Restart with the same game mode (reshows ship placement)

### Turn Flow
**Human vs Human Mode**:
1. Player 1 shoots
2. Result popup shows hit/miss/sunk
3. Switch player screen appears
4. Player 2 clicks "START TURN" to begin their turn
5. Repeat

**Human vs Computer Mode**:
1. You shoot and see the result
2. Status changes to "Computer's Turn" for 2 seconds
3. Computer shoots automatically
4. Your turn again

## Implementation Details

### File Structure
- `Main.java` - Application entry point
- `StartScreen.java` - Game mode selection menu
- `ShipPlacementScreen.java` - Drag-and-drop ship placement interface
- `GameWindow.java` - Main game interface with both boards
- `GameController.java` - Game logic and turn management
- `Board.java` - 10x10 grid management with ship and shot tracking
- `Ship.java` - Individual ship representation
- `Player.java` - Abstract player base class
- `HumanPlayer.java` - Uses user input to choose shot
- `ComputerPlayer.java` - Computer chooses shot
- `ButtonPanel.java` - Reusable button panel component
- `SwitchPlayerScreen.java` - Turn transition overlay


## Extra Features Implemented

### 1. Save/Load Game (Required Extra Feature)
- Save game state to .bsg files
- Preserves:
  - Current player turn
  - All ship placements
  - All shots fired
  - Hit/miss/destroyed status
- Load game and continue from exact state

### 2. Human vs Computer Mode
- Computer opponent has a two-tier strategy:
  1. **Direction Detection**: When 2+ consecutive hits are found, computer determines ship orientation and pursues along that line
  2. **Adjacent Targeting**: After single hits, shoots adjacent cells to find ship direction
  3. **Random Shots**: When no hits exist, shoots randomly

## Sources
- Battleship Image: https://i.ytimg.com/vi/OqHO2q21RJU/hq720.jpg?sqp=-oaymwEhCK4FEIIDSFryq4qpAxMIARUAAAAAGAElAADIQj0AgKJD&rs=AOn4CLA7G0rgc6l8psMLgHPnYtwXYHns3Q
