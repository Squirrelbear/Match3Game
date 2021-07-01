package MatchGame;

import javax.swing.*;
import java.awt.*;

/**
 * Match Game
 * Author: Peter Mitchell (2021)
 *
 * Game class:
 * Entry point for creating the GUI and
 * associated panels. Also acts as the
 * inbetween point for communication between panels.
 */
public class Game extends JFrame {
    /**
     * Reference to the MatchPanel object to apply the restart action.
     */
    private MatchPanel matchPanel;
    /**
     * Reference to the StatusPanel object to apply restarts and update score visuals.
     */
    private StatusPanel statusPanel;

    /**
     * Creates the Game class instance to start the application.
     *
     * @param args Not used.
     */
    public static void main(String[] args) {
        Game game = new Game();
    }

    /**
     * Creates the JFrame, and the two panels then adds them and
     * makes the frame visible.
     */
    public Game() {
        super("Match Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());
        matchPanel = new MatchPanel(15,15, this);
        getContentPane().add(matchPanel);
        statusPanel = new StatusPanel(this, 15*32);
        getContentPane().add(statusPanel);
        pack();
        setVisible(true);
    }

    /**
     * Update the score in the status panel.
     *
     * @param newScore The updated score to show.
     * @param scoreDelta The difference between the new score and old score.
     */
    public void notifyScoreUpdate(int newScore, int scoreDelta) {
        statusPanel.updateScore(newScore, scoreDelta);
    }

    /**
     * Forces a state reset on the game. Resets the visual score to 0.
     * Triggers a fresh board in matchPanel with all new Colours.
     */
    public void restart() {
        statusPanel.updateScore(0,0);
        matchPanel.restart();
    }
}
