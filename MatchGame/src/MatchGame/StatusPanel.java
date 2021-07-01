package MatchGame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Match Game
 * Author: Peter Mitchell (2021)
 *
 * StatusPanel class:
 * Defines a panel to show the score, changes in score, and provides
 * buttons for restarting and quitting.
 */
public class StatusPanel extends JPanel implements ActionListener {
    /**
     * Reference to the Game object for calling the restart method.
     */
    private Game game;
    /**
     * Reference to the scoreLabel to provide access for changing the score text.
     */
    private JLabel scoreLabel;
    /**
     * Reference to the scoreChangeLabel to provide access to changing the delta score text.
     */
    private JLabel scoreChangeLabel;
    /**
     * Reference to the restartButton to check when it is the source of an actionPerformed().
     */
    private JButton restartButton;
    /**
     * Reference to the quitButton to check when it is the source of an actionPerformed().
     */
    private JButton quitButton;

    /**
     * Creates all the labels and buttons for the score, restart, and quit.
     *
     * @param game Reference to the Game object.
     * @param panelHeight The height to use for the panelHeight.
     */
    public StatusPanel(Game game, int panelHeight) {
        this.game = game;
        setPreferredSize(new Dimension(100, panelHeight));
        setBackground(Color.darkGray);
        Font mainFont = new Font("Arial", 0, 40);
        Font scoreChangeFont = new Font("Arial", 0, 60);

        // Set up the Score panels/labels
        JPanel topPanel = new JPanel();
        topPanel.setBackground(Color.darkGray);
        topPanel.setPreferredSize(new Dimension(100,100));
        JLabel scoreTextLabel = new JLabel("Score");
        scoreTextLabel.setForeground(Color.WHITE);
        scoreTextLabel.setFont(mainFont);
        scoreLabel = new JLabel("0");
        scoreLabel.setForeground(Color.WHITE);
        scoreLabel.setFont(mainFont);
        topPanel.add(scoreTextLabel);
        topPanel.add(scoreLabel);

        // Set up the Score change panels/labels
        JPanel middlePanel = new JPanel();
        middlePanel.setBackground(Color.darkGray);
        middlePanel.setPreferredSize(new Dimension(100, 300));
        scoreChangeLabel = new JLabel("");
        scoreChangeLabel.setForeground(Color.WHITE);
        scoreChangeLabel.setFont(scoreChangeFont);
        middlePanel.add(scoreChangeLabel);

        // Create the buttons
        restartButton = new JButton("Restart");
        restartButton.addActionListener(this);
        quitButton = new JButton("Quit");
        quitButton.addActionListener(this);

        // Add everything to the top level panel
        add(topPanel);
        add(middlePanel);
        add(restartButton);
        add(quitButton);
    }

    /**
     * Update the labels based on the new score
     *
     * @param newScore The new score value to replace the old score.
     * @param scoreDelta The amount of change from the previous score.
     */
    public void updateScore(int newScore, int scoreDelta) {
        scoreLabel.setText(""+newScore);
        if(scoreDelta > 0)
            scoreChangeLabel.setText("+"+scoreDelta);
        else {
            scoreChangeLabel.setText("");
        }
    }

    /**
     * Triggers when either quit or restart button are pressed.
     * Preforms the appropriate action when pressed.
     *
     * @param e Event information, used for the event source.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == restartButton) {
            game.restart();
        } else if(e.getSource() == quitButton) {
            System.exit(0);
        }
    }
}
