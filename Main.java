// Launches the start screen

import javax.swing.SwingUtilities;

public class Main 
{
    public static void main(String[] args) 
    {
 
      SwingUtilities.invokeLater(() -> 
      {
            StartScreen startScreen = new StartScreen();
            startScreen.setVisible(true);
      });
    }
}
