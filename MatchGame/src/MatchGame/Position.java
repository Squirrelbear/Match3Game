package MatchGame;

import java.util.Objects;

/**
 * Match Game
 * Author: Peter Mitchell (2021)
 *
 * Position class:
 * Used to represent a single position x,y.
 */
public class Position {
    /**
     * X coordinate.
     */
    public int x;
    /**
     * Y coordinate.
     */
    public int y;

    /**
     * Sets the value of Position.
     *
     * @param x X coordinate.
     * @param y Y coordinate.
     */
    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Compares the Position object against another object.
     * Any non-Position object will return false. Otherwise compares x and y for equality.
     *
     * @param o Object to compare this Position against.
     * @return True if the object o is equal to this position for both x and y.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return x == position.x && y == position.y;
    }

    /**
     * Used to create a hash representation of the object.
     *
     * @return A hash representing this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
