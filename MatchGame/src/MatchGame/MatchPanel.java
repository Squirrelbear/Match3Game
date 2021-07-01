package MatchGame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Match Game
 * Author: Peter Mitchell (2021)
 *
 * MatchPanel class:
 * Shows a visual interactive panel representing a MatchBoard.
 * Receives clicks and propagates updates as the game continues.
 */
public class MatchPanel extends JPanel implements MouseListener {
    /**
     * Reference to the Game object to pass score updates.
     */
    private Game game;
    /**
     * States to enumerate through to determine how input is handled.
     * ChoosePos1 is for setting the first Position for matching.
     * ChoosePos2 comes after ChoosePos1 to set the second Position.
     * After two valid positions are chosen PauseForDestroy pauses input until
     * all matches are resolved. Then the game state returns to ChoosePos1.
     * If an invalid position is selected for ChoosePos2, it will swap the selection to
     * be handled with ChoosePos1 instead.
     */
    private enum GameState { ChoosePos1, ChoosePos2, PauseForDestroy }
    /**
     * Stores the GameState as described in GameState.
     */
    private GameState gameState;
    /**
     * Positions for the match to be performed.
     */
    private Position pos1, pos2;
    /**
     * Number of cells horizontally (width), and vertically (height).
     */
    private int width, height;
    /**
     * Number of pixels for each cell representing both width and height.
     */
    private final int CELL_DIM = 32;
    /**
     * Colours to use for different number states in each cell of the matchBoard.
     */
    private Color[] colors = new Color[] { Color.BLUE, Color.BLACK, Color.YELLOW, Color.GREEN, Color.RED };
    /**
     * Reference to the MatchBoard containing the games board state for all cells.
     */
    private MatchBoard matchBoard;
    /**
     * The most recent matches that occurred. Can be rendered with a background using drawRecentMatches().
     */
    private List<Position> recentMatches;
    /**
     * The most recent new cells that were added. Can be rendered with a background using drawRecentMatches().
     */
    private List<Position> recentNewCells;
    /**
     * Timer for delaying match testing. The timer is configured with configureTimer()
     * and is called when two valid positions have been matched to continue matching
     * until there are no more matches.
     */
    private Timer stateTimer;
    /**
     * The current score based on the number of matches cells.
     */
    private int score;

    /**
     * Initialises all the properties of the MatchPanel.
     * Including setting up the MatchBoard object with a fresh board.
     *
     * @param width Number of cells horizontally.
     * @param height Number of cells vertically.
     * @param game Reference to the Game object for passing score updates.
     */
    public MatchPanel(int width, int height, Game game) {
        this.game = game;
        this.width = width;
        this.height = height;
        matchBoard = new MatchBoard(width,height, colors.length);
        gameState = GameState.ChoosePos1;
        recentMatches = new ArrayList<>();
        recentNewCells = new ArrayList<>();
        setPreferredSize(new Dimension(width*CELL_DIM, height*CELL_DIM));
        setBackground(Color.gray);
        addMouseListener(this);
        createStableBoardState();
        configureTimer();
        score = 0;
    }

    /**
     * Draws all the recent activity, all the current cell values,
     * and an overlay whenever a pos1 is set.
     *
     * @param g Reference to the Graphics area to draw on.
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        //drawGrid(g);
        drawRecentMatches(g);
        drawAllCells(g);
        if(gameState == GameState.ChoosePos2)
            drawSelectedPos1(g);
    }

    /**
     * Handles the clicks to set pos1 and pos2. After two Positions have been set
     * the  matches are all evaluated until the player is given the ability to click again.
     *
     * @param e Event information about the MouseEvent that occurred.
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        if(!(gameState == GameState.ChoosePos1 || gameState == GameState.ChoosePos2)) return;

        int x = e.getX() / CELL_DIM;
        int y = e.getY() / CELL_DIM;
        if(!isValidPosition(x,y)) return;

        if(gameState == GameState.ChoosePos2 && !isAdjacentToPos1(x,y)) {
            System.out.println("Too far apart! Force swapping back to choosing pos1.");
            gameState = GameState.ChoosePos1;
        }

        if(gameState == GameState.ChoosePos1) {
            setPos1(new Position(x,y));
        } else {
            setPos2(new Position(x,y));
            attemptCellSwap();
        }

        repaint();
    }

    /**
     * Resets all the current board to a new puzzle and resets the score back to 0.
     */
    public void restart() {
        score = 0;
        matchBoard.fillBoard();
        createStableBoardState();
        repaint();
    }

    /**
     * Sets the value of pos1 and iterates the GameState on to choosing pos2.
     *
     * @param pos Position to store in pos1.
     */
    private void setPos1(Position pos) {
        pos1 = pos;
        gameState = GameState.ChoosePos2;
        System.out.println("Set pos1 as: " + pos.x + " " + pos.y);
    }

    /**
     * Sets the value of pos2 and iterates the GameState on to pausing while matching is completed.
     *
     * @param pos Position to store in pos2.
     */
    private void setPos2(Position pos) {
        pos2 = pos;
        gameState = GameState.PauseForDestroy;
        System.out.println("Set pos2 as: " + pos.x + " " + pos.y);
    }

    /**
     * Tests if the specified position is adjacent vertically or horizontally
     * to the pos1 that has already been set.
     *
     * @param x X coordinate to test.
     * @param y Y coordinate to test.
     * @return True if passed position is adjacent to pos1.
     */
    private boolean isAdjacentToPos1(int x, int y) {
        int diffx = Math.abs(x-pos1.x);
        int diffy = Math.abs(y-pos1.y);
        return (diffx == 0 || diffy == 0) && (diffx == 1 || diffy == 1);
    }

    /**
     * Iterates through all Positions on the board to make them draw.
     *
     * @param g Reference to the Graphics object for drawing.
     */
    private void drawAllCells(Graphics g) {
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                drawCell(g, x, y);
            }
        }
    }

    /**
     * Attempts to perform the swap between pos1 and pos2.
     * If there are no matches from the swap the cells are reset and the state
     * returns to inputting the first position.
     * If one or more matches are found the cells are swapped, then all cells
     * are shuffled down to fill any gaps based on matches. Then cells at the top
     * are filled in with new random values. Then the timer is started to continue matching
     * until there are no more matches.
     */
    private void attemptCellSwap() {
        List<Position> matches = matchBoard.getMatchesFromSwap(pos1, pos2);
        updateScore(matches.size());
        if(!matches.isEmpty()) {
            matchBoard.swapCells(pos1, pos2);
            List<Position> cellsToReplace = matchBoard.shuffledDownToFill(matches);
            matchBoard.fillPositions(cellsToReplace);
            recentMatches = matches;
            recentNewCells = cellsToReplace;
            stateTimer.start();
        } else {
            System.out.println("Nothing to match here. :(");
            gameState = GameState.ChoosePos1;
        }
    }

    /**
     * Increases the score by amount and notifies the Game object that the
     * score has changed. Does nothing if addAmount is not a positive number.
     *
     * @param addAmount The amount of score to increase by.
     */
    private void updateScore(int addAmount) {
        if(addAmount > 0) {
            score += addAmount;
            game.notifyScoreUpdate(score, addAmount);
            //System.out.println("Score: " + score);
        }
    }

    /**
     * Draws a circle on the board at the relative board position for x,y.
     * Colour is based on the array of colours and the number stored in that
     * cell for MatchBoard.
     *
     * @param g Reference to the Graphics object for drawing.
     * @param x X coordinate.
     * @param y Y coordinate.
     */
    private void drawCell(Graphics g, int x, int y) {
        g.setColor(colors[matchBoard.getCellValue(x,y)]);
        g.fillOval(x * CELL_DIM, y * CELL_DIM, CELL_DIM, CELL_DIM);
    }

    /**
     * Draws rectangles to show where recent matches have occurred, and
     * where recent new cells have been randomly filled in.
     *
     * @param g Reference to the Graphics object for drawing.
     */
    private void drawRecentMatches(Graphics g) {
        g.setColor(Color.ORANGE);
        for(Position p : recentMatches) {
            g.fillRect(p.x * CELL_DIM, p.y * CELL_DIM, CELL_DIM, CELL_DIM);
        }
        g.setColor(new Color(0,128,0));
        for(Position p : recentNewCells) {
            g.fillRect(p.x * CELL_DIM, p.y * CELL_DIM, CELL_DIM, CELL_DIM);
        }
    }

    /**
     * Draws a cross with one vertical line and one horizontal line to show where the selected pos1 position is.
     *
     * @param g Reference to the Graphics object for drawing.
     */
    private void drawSelectedPos1(Graphics g) {
        g.setColor(Color.WHITE);
        g.drawLine(pos1.x*CELL_DIM,pos1.y*CELL_DIM+CELL_DIM/2,pos1.x*CELL_DIM+CELL_DIM, pos1.y*CELL_DIM+CELL_DIM/2);
        g.drawLine(pos1.x*CELL_DIM+CELL_DIM/2,pos1.y*CELL_DIM,pos1.x*CELL_DIM+CELL_DIM/2, pos1.y*CELL_DIM+CELL_DIM);
    }

    /**
     * Draws a grid by drawing vertical lines and then horizontal lines based on the number of cells.
     *
     * @param g A reference to the Graphics object for rendering.
     */
    private void drawGrid(Graphics g) {
        g.setColor(Color.BLACK);
        // Draw vertical lines
        int y2 = 0;
        int y1 = height * CELL_DIM;
        for(int x = 0; x < width; x++)
            g.drawLine(x * CELL_DIM, y1, x * CELL_DIM, y2);

        // Draw horizontal lines
        int x2 = 0;
        int x1 = width * CELL_DIM;
        for(int y = 0; y < height; y++)
            g.drawLine(x1, y * CELL_DIM, x2, y * CELL_DIM);
    }

    /**
     * Tests if the position specified by x,y is inside the valid
     * bounds of the panel.
     *
     * @param x X coordinate.
     * @param y Y coordinate.
     * @return True if the Position x,y is inside the MatchBoard.
     */
    private boolean isValidPosition(int x, int y) {
        if(x < 0 || y < 0 || x >= width || y >= height) return false;
        return true;
    }

    /**
     * Wipes out all matches without modifying the score.
     * This is used for creating a start game state and for restarts.
     * Every time matches are found, they are all directly filled with new
     * random numbers.
     */
    private void createStableBoardState() {
        List<Position> matches = matchBoard.findAllMatches();
        while(!matches.isEmpty()) {
            System.out.println("Fixing board state!");
            matchBoard.fillPositions(matches);
            matches = matchBoard.findAllMatches();
        }
    }

    /**
     * Configures the stateTimer's actionPerformed event and delay.
     * The stateTimer is set to not repeat and have 500ms delay.
     * When the timer triggers it will find all matches on the board,
     * then update and start the timer again. Once there are no more matches,
     * the game state is swapped back to choosing pos1.
     *
     * The timer is ready to begin after this method by calling stateTimer.start();
     */
    private void configureTimer() {
        stateTimer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<Position> matches = matchBoard.findAllMatches();
                updateScore(matches.size());
                if(!matches.isEmpty()) {
                    List<Position> cellsToReplace = matchBoard.shuffledDownToFill(matches);
                    matchBoard.fillPositions(cellsToReplace);
                    recentMatches = matches;
                    recentNewCells = cellsToReplace;
                    repaint();
                    stateTimer.start();
                    System.out.println("Made changes, waiting again!");
                } else {
                    System.out.println("All done :)");
                    gameState = GameState.ChoosePos1;
                }
            }
        });
        stateTimer.setRepeats(false);
    }

    /**
     * Not used.
     *
     * @param e Not used.
     */
    @Override
    public void mousePressed(MouseEvent e) {}
    /**
     * Not used.
     *
     * @param e Not used.
     */
    @Override
    public void mouseReleased(MouseEvent e) {}
    /**
     * Not used.
     *
     * @param e Not used.
     */
    @Override
    public void mouseEntered(MouseEvent e) {}
    /**
     * Not used.
     *
     * @param e Not used.
     */
    @Override
    public void mouseExited(MouseEvent e) {}
}
