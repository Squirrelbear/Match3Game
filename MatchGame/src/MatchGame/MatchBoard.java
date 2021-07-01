package MatchGame;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Match Game
 * Author: Peter Mitchell (2021)
 *
 * MatchBoard class:
 * Represents a 2D grid of random numbers
 * with matching functionality where 3+ numbers can be
 * matched vertically or horizontally.
 */
public class MatchBoard {
    /**
     * 2D grid of all values on the board.
     */
    private int[][] cellValues;
    /**
     * Width and height of the number of elements on the board.
     */
    private int width, height;
    /**
     * Shared reference to the Random class.
     */
    private Random rand;
    /**
     * Maximum number from 0 to maxNums-1 for random numbers.
     */
    private int maxNums;
    /**
     * Used to swap from an adjacent match to one that allows swaps between any two points.
     */
    private boolean enforceAdjacent;

    /**
     * Creates a default board with a random set of numbers.
     *
     * @param width Number of cells horizontally.
     * @param height Number of cells Vertically.
     * @param maxNums Maximum number of different types of cells.
     */
    public MatchBoard(int width, int height, int maxNums) {
        this.width = width;
        this.height = height;
        cellValues = new int[width][height];
        rand = new Random();
        this.maxNums = maxNums;
        fillBoard();
        enforceAdjacent = true;
    }

    /**
     * Sets the value of enforceAdjacent to the value specified.
     * While true, this allows the class to accept any two cells on the board for swaps instead
     * of just adjacent ones.
     *
     * @param enforceAdjacent The state to set enforceAdjacent to.
     */
    public void setEnforceAdjacent(boolean enforceAdjacent) {
        this.enforceAdjacent = enforceAdjacent;
    }

    /**
     * Gets the value at the specified position x,y.
     *
     * @param x X coordinate.
     * @param y Y coordinate.
     * @return The value at position x,y on the board.
     */
    public int getCellValue(int x, int y) {
        return cellValues[x][y];
    }

    /**
     * Fills the board with an entirely random set of numbers within the range 0 to maxNums-1
     */
    public void fillBoard() {
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                cellValues[x][y] = rand.nextInt(maxNums);
            }
        }
    }

    /**
     * Swaps the values at the two specified positions on the board.
     *
     * @param pos1 First position for the swap.
     * @param pos2 Second position for the swap.
     */
    public void swapCells(Position pos1, Position pos2) {
        int temp = cellValues[pos1.x][pos1.y];
        cellValues[pos1.x][pos1.y] = cellValues[pos2.x][pos2.y];
        cellValues[pos2.x][pos2.y] = temp;
    }

    /**
     * Tests swapping the specified positions for all matches that come as a result.
     * The cells are then swapped back before returning a list of all matches found.
     *
     * @param pos1 First position of the swap.
     * @param pos2 Second position of the swap.
     * @return A list of the matches Positions without making changes to these positions.
     */
    public List<Position> getMatchesFromSwap(Position pos1, Position pos2) {
        List<Position> matched = new ArrayList<>();
        // Reject invalid swap if enforcing an adjacent swap
        int diffx = Math.abs(pos1.x - pos2.x);
        int diffy = Math.abs(pos1.y - pos2.y);
        if(enforceAdjacent && !((diffx == 0 || diffy == 0) && (diffx == 1 || diffy == 1))) return matched;

        // Temporarily swap the cells
        swapCells(pos1, pos2);

        // Test for all relevant rows/columns based on changes from the swap
        getMatchesOnRow(pos1.y, matched);
        if(!enforceAdjacent || diffy > 0)
            getMatchesOnRow(pos2.y, matched);
        getMatchesOnColumn(pos1.x, matched);
        if(!enforceAdjacent || diffx > 0)
            getMatchesOnColumn(pos2.x, matched);

        // Reverse the swapping of cells
        swapCells(pos1, pos2);

        return matched;
    }

    /**
     * Shuffles down cells to fill all matches positions.
     * Then returns a list of positions that are considered to be empty at the top.
     *
     * @param positions A list of matched positions to remove.
     * @return A list of positions that should be filled with new random values.
     */
    public List<Position> shuffledDownToFill(List<Position> positions) {
        // Initialise an array to track the number of cells removed for each column
        int[] affectedColumns = new int[width];
        for(int x = 0; x < width; x++) {
            affectedColumns[x] = 0;
        }
        // Count the column for each position as being affected, then move all the cells down to fill gaps.
        for(Position pos : positions) {
            affectedColumns[pos.x]++;
            shuffleDownToFill(pos);
        }

        // Get all the positions based on the affectedColumn counts
        return getAffectedPositionsFromAffectedColumns(affectedColumns);
    }

    /**
     * Fills all specified positions with new random numbers from 0 to maxNums-1.
     *
     * @param positions Positions to fill with random numbers.
     */
    public void fillPositions(List<Position> positions) {
        for(Position p : positions) {
            cellValues[p.x][p.y] = rand.nextInt(maxNums);
        }
    }

    /**
     * Tests every row and every column for matches.
     *
     * @return A list of all positions that were matched on any row or column.
     */
    public List<Position> findAllMatches() {
        List<Position> matched = new ArrayList<>();
        // Test all horizontally
        for(int y = 0; y < height; y++) {
            getMatchesOnRow(y, matched);
        }

        // Test all vertically
        for(int x = 0; x < width; x++) {
            getMatchesOnColumn(x, matched);
        }

        return matched;
    }

    /**
     * Tests for all cells on a row for any matches of 3+ of the same number in a row.
     * Continues adding all positions until the number changes for matches larger than 3.
     *
     * @param y Y position for row.
     * @param matchedRef A reference to the matched list to add matches to.
     */
    private void getMatchesOnRow(int y, List<Position> matchedRef) {
        for(int x = 0; x < width - 2; x++) {
            // Test for three of the same number in a row
            if(cellValues[x][y] == cellValues[x+1][y] && cellValues[x][y] == cellValues[x+2][y]) {
                int matchValue = cellValues[x][y];
                Position p = new Position(x,y);
                if(!matchedRef.contains(p))
                    matchedRef.add(p);
                // Step forward to the second cell. Second and third will be matches at least.
                x++;
                while(x < width && cellValues[x][y] == matchValue) {
                    p = new Position(x,y);
                    if(!matchedRef.contains(p))
                        matchedRef.add(p);
                    x++;
                }
                // Step back because the latest cell was not a match
                x--;
            }
        }
    }

    /**
     * Tests for all cells on a column for any matches of 3+ of the same number in a row.
     * Continues adding all positions until the number changes for matches larger than 3.
     *
     * @param x X position for a column.
     * @param matchedRef A reference to the matched list to add matches to.
     */
    private void getMatchesOnColumn(int x, List<Position> matchedRef) {
        for(int y = 0; y < height - 2; y++) {
            // Test for three of the same number in a row
            if(cellValues[x][y] == cellValues[x][y+1] && cellValues[x][y] == cellValues[x][y+2]) {
                int matchValue = cellValues[x][y];
                Position p = new Position(x,y);
                if(!matchedRef.contains(p))
                    matchedRef.add(p);
                // Step forward to the second cell. Second and third will be matches at least.
                y++;
                while(y < height && cellValues[x][y] == matchValue) {
                    p = new Position(x,y);
                    if(!matchedRef.contains(p))
                        matchedRef.add(p);
                    y++;
                }
                // Step back because the latest cell was not a match
                y--;
            }
        }
    }

    /**
     * Moves all cells above the specified Position down by 1 to overwrite pos.
     *
     * @param pos The Position to overwrite by shifting down.
     */
    private void shuffleDownToFill(Position pos) {
        for(int y = pos.y; y >= 1; y--) {
            cellValues[pos.x][y] = cellValues[pos.x][y-1];
        }
    }

    /**
     * Iterates through the affectedColumns counts. Any larger than 0 will create N positions
     * on the result list starting from the top and stepping down.
     *
     * @param affectedColumns An array with counts of how many cells were changed in each column.
     * @return A list of positions at the top of columns to fill with new numbers.
     */
    private List<Position> getAffectedPositionsFromAffectedColumns(int[] affectedColumns) {
        List<Position> affectedPositions = new ArrayList<>();
        for(int x = 0; x < affectedColumns.length; x++) {
            if(affectedColumns[x] > 0) {
                for(int y = 0; y < affectedColumns[x]; y++) {
                    affectedPositions.add(new Position(x,y));
                }
            }
        }
        return affectedPositions;
    }
}
