// UI panel containing game control buttons: Home, Save Game, Load Game, and New Game

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class ButtonPanel extends JPanel {

    public JButton homeButton, newGameButton, saveButton, loadButton;
    private final Color BUTTON_COLOR = new Color(66, 135, 245);

    public ButtonPanel(ActionListener homeAction,
                       ActionListener newGameAction,
                       ActionListener saveAction,
                       ActionListener loadAction) {

        setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create buttons
        homeButton = createButton("Home", homeAction);
        saveButton = createButton("Save Game", saveAction);
        newGameButton = createButton("New Game", newGameAction);

        // Add buttons to panel
        add(homeButton);
        add(saveButton);
        add(newGameButton);
    }

    private JButton createButton(String text, ActionListener action) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(BUTTON_COLOR);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(130, 40));

        // Rounded rectangle border
        button.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2, true)); // true = rounded corners

        if (action != null) button.addActionListener(action);
        return button;
    }
}
