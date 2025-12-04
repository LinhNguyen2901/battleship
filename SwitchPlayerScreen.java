// Overlay screen displayed between turns in Human vs Human mode, prompting
// the next player to begin their turn while hiding the previous player's information

import javax.swing.*;
import java.awt.*;

public class SwitchPlayerScreen extends JPanel {
    private GameController controller;
    private Runnable onComplete;

    public SwitchPlayerScreen(GameController controller, Runnable onComplete) {
        this.controller = controller;
        this.onComplete = onComplete;
        
        setLayout(new GridBagLayout());
        setupUI();
    }

    private void setupUI() {
        setOpaque(true);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(20, 20, 20, 20);

        // Player number indicator (large, eye-catching)
        Player nextPlayer = controller.getCurrentPlayer();
        int nextPlayerNumber = (nextPlayer == controller.getPlayer1()) ? 1 : 2;
        
        gbc.insets = new Insets(40, 20, 20, 20);
        JLabel playerNumberLabel = new JLabel("PLAYER " + nextPlayerNumber);
        playerNumberLabel.setFont(new Font("Arial", Font.BOLD, 72));
        playerNumberLabel.setForeground(new Color(100, 200, 255));
        playerNumberLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(playerNumberLabel, gbc);

        // Message label
        gbc.insets = new Insets(30, 20, 40, 20);
        JLabel messageLabel = new JLabel("Ready for your turn?");
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 28));
        messageLabel.setForeground(new Color(200, 200, 200));
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(messageLabel, gbc);

        // Spacer
        gbc.insets = new Insets(10, 20, 10, 20);
        add(Box.createVerticalStrut(20), gbc);

        // Switch button with hover effect
        gbc.insets = new Insets(20, 20, 50, 20);
        JButton switchBtn = new JButton("START TURN") {
            @Override
            protected void paintComponent(Graphics g) {
                if (getModel().isArmed()) {
                    g.setColor(new Color(80, 150, 255));
                } else if (getModel().isRollover()) {
                    g.setColor(new Color(70, 130, 230));
                } else {
                    g.setColor(new Color(50, 120, 200));
                }
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                
                super.paintComponent(g);
            }
        };
        switchBtn.setFont(new Font("Arial", Font.BOLD, 26));
        switchBtn.setForeground(Color.WHITE);
        switchBtn.setPreferredSize(new Dimension(280, 70));
        switchBtn.setContentAreaFilled(false);
        switchBtn.setBorder(BorderFactory.createEmptyBorder());
        switchBtn.setFocusPainted(false);
        switchBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        switchBtn.addActionListener(e -> {
            if (onComplete != null) {
                onComplete.run();
            }
        });
        add(switchBtn, gbc);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Dark gradient background
        GradientPaint gradient = new GradientPaint(0, 0, new Color(20, 20, 40), 
                                                  getWidth(), getHeight(), new Color(10, 10, 20));
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        // Subtle border effect
        g2d.setColor(new Color(100, 150, 255, 100));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(50, 100, getWidth() - 100, getHeight() - 200);
    }
}
