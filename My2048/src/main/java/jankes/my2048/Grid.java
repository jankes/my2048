package jankes.my2048;

import java.util.Arrays;
import java.util.Random;

public class Grid {
    private final int[] mValues;

    private Grid(int[] values) {
        mValues = values;
    }

    private static final int LEFT = 0;
    private static final int UP = 1;
    private static final int RIGHT = 2;
    private static final int DOWN =3;

    public static Grid New(int[] values) {
        return new Grid(values.clone());
    }

    public interface EventListener {
        void blockMoved(int startRow, int startColumn, int endRow, int endColumn);
        void blocksMerged(int srcRow, int srcColumn, int dstRow, int dstColumn, int newValue);
        void newBlock(int row, int column, int value);
    }

    public static Grid New(Random r) {
        Grid grid = new Grid(new int[] {
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0});
        grid.addBlock(r);
        grid.addBlock(r);
        return grid;
    }

    public int[] getBlockValues() {
        return mValues.clone();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Grid)) {
            return false;
        }
        Grid other = (Grid)obj;
        return Arrays.equals(mValues, other.mValues);
    }

    // Useful for debugging
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int row = 1; row <= 4; row++) {
            for (int col = 1; col <= 4; col++) {
                builder.append(get(LEFT, row, col));
                if (col < 4) {
                    for(int i = 0; i < 5 - digits(get(LEFT, row, col)); i++) {
                        builder.append(' ');
                    }
                }
            }
            builder.append('\n');
        }
        return builder.toString();
    }

    // Helper for the toString method
    // num must be in range [0, 9999]
    private int digits(int num) {
        if (num < 10) {
            return 1;
        } else if (num < 100) {
            return 2;
        } else if (num < 1000) {
            return 3;
        } else {
            return 4;
        }
    }

    private static class RowCol {
        public final int row;
        public final int col;
        public RowCol(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }

    public boolean containsBlock(int value) {
        for (int i = 0; i < 16; i++) {
            if (mValues[i] == value) {
                return true;
            }
        }
        return false;
    }

    public Grid shiftLeft(Random rand, EventListener listener) {
        return shift(LEFT, rand, listener);
    }

    public Grid shiftUp(Random rand, EventListener listener) {
        return shift(UP, rand, listener);
    }

    public Grid shiftRight(Random rand, EventListener listener) {
        return shift(RIGHT, rand, listener);
    }

    public Grid shiftDown(Random rand, EventListener listener) {
        return shift(DOWN, rand, listener);
    }

    private Grid shift(int transform, Random rand, EventListener listener) {
        Grid shifted = New(mValues);
        boolean moveOrMerge = false;
        for (int row = 1; row <= 4; row++) {
            for (int col = 2; col <= 4; col++) {
                if (shifted.get(transform, row, col) != 0) {
                    moveOrMerge |= shifted.shiftBlock(transform, row, col, listener);
                }
            }
        }
        if (moveOrMerge) {
            if (shifted.canAddBlock()) {
                RowCol addedRowCol = shifted.addBlock(rand);
                listener.newBlock(addedRowCol.row, addedRowCol.col, shifted.get(LEFT, addedRowCol.row, addedRowCol.col));
            }
        }
        return shifted;
    }

    private boolean shiftBlock(int transform, int row, int col, EventListener listener) {
        boolean blockMoved = false;
        int startCol = col;
        for (; col >= 2; col--) {
            if (get(transform, row, col - 1) == 0) {
                set(transform, row, col - 1, get(transform, row, col));
                set(transform, row, col, 0);
                blockMoved = true;
            } else if (get(transform, row, col - 1) == get(transform, row, col)) {
                int newValue = 2 * get(transform, row, col);
                set(transform, row, col - 1, newValue);
                set(transform, row, col, 0);
                if (blockMoved) {
                    callBlockMoved(listener, transform, row, startCol, row, col);
                }
                callBlocksMerged(listener, transform, row, col, row, col - 1, newValue);
                return true;
            } else {
                break;
            }
        }
        if (blockMoved) {
            callBlockMoved(listener, transform, row, startCol, row, col);
        }
        return blockMoved;
    }

    private void callBlockMoved(EventListener listener, int transform, int startRow, int startColumn, int endRow, int endColumn) {
        RowCol transformedStart = transformRowCol(transform, startRow, startColumn);
        RowCol transformedEnd = transformRowCol(transform, endRow, endColumn);
        listener.blockMoved(transformedStart.row, transformedStart.col, transformedEnd.row, transformedEnd.col);
    }

    private void callBlocksMerged(EventListener listener, int transform, int srcRow, int srcColumn, int dstRow, int dstColumn, int newValue) {
        RowCol transformedSrc = transformRowCol(transform, srcRow, srcColumn);
        RowCol transformedDst = transformRowCol(transform, dstRow, dstColumn);
        listener.blocksMerged(transformedSrc.row, transformedSrc.col, transformedDst.row, transformedDst.col, newValue);
    }

    private RowCol transformRowCol(int transform, int row, int column) {
        if (transform == LEFT) {
            return new RowCol(row, column);
        } else if (transform == UP) {
            return new RowCol(column, 5 - row);
        } else if (transform == RIGHT) {
            return new RowCol(5 - row, 5 - column);
        } else {
            // DOWN
            return new RowCol(5 - column, row);
        }
    }

    private boolean canAddBlock() {
        for(int i = 0; i < 16; i++) {
            if (mValues[i] == 0) {
                return true;
            }
        }
        return false;
    }

    private RowCol addBlock(Random r) {
        RowCol toAddRowCol = findEmptyLocation(r);

        int value = r.nextFloat() < 0.9f ? 2 : 4;
        set(LEFT, toAddRowCol.row, toAddRowCol.col, value);
        return toAddRowCol;
    }

    private RowCol findEmptyLocation(Random r) {
        while (true) {
            int row = 1 + r.nextInt(4);
            int col = 1 + r.nextInt(4);
            if (get(LEFT, row, col) != 0) {
                continue;
            }
            return new RowCol(row, col);
        }
    }

    public int get(int row, int column) {
        return get(LEFT, row, column);
    }

    private int get(int transform, int row, int column) {
        return mValues[index(transform, row, column)];
    }

    private void set(int transform, int row, int column, int value) {
        mValues[index(transform, row, column)] = value;
    }

    private int index(int transform, int row, int column) {
        if (transform == LEFT) {
            return (4 * row) + column - 5;
        } else if (transform == UP) {
            return (4 * column) - row;
        } else if (transform == RIGHT) {
            return 20 - column - (4 * row);
        } else {
            // DOWN
            return 15 + row - (4 * column);
        }
    }
}
