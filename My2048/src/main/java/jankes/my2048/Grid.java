package jankes.my2048;

import java.util.Random;

public class Grid {
    private final int[] mValues;

    private Grid(int[] values) {
        mValues = values;
    }

    private static final int LEFT = 0;
    private static final int RIGHT = 1;
    private static final int UP = 2;
    private static final int DOWN =3;

    // public for testing purposes
    public static Grid New(int[] values) {
        return new Grid(values.clone());
    }

    public interface EventListener {
        void blockMoved(int startRow, int startColumn, int endRow, int endColumn);
        void blocksMerged(int srcRow, int srcColumn, int dstRow, int dstColumn, int newValue);
        void newBlock(int row, int column, int value);
    }

    private static class RowCol {
        public final int row;
        public final int col;
        public RowCol(int row, int col) {
            this.row = row;
            this.col = col;
        }
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

    private RowCol addBlock(Random r) {
        RowCol toAddRowCol = findEmptyLocation(r);

        int value = r.nextFloat() < 0.9f ? 2 : 4;
        set(toAddRowCol.row, toAddRowCol.col, value);
        return toAddRowCol;
    }

    private RowCol findEmptyLocation(Random r) {
        while (true) {
            int row = 1 + r.nextInt(4);
            int col = 1 + r.nextInt(4);
            if (get(row, col) != 0) {
                continue;
            }
            return new RowCol(row, col);
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

    // Useful for debugging
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int row = 1; row <= 4; row++) {
            for (int col = 1; col <= 4; col++) {
                builder.append(get(row, col));
                if (col < 4) {
                    for(int i = 0; i < 5 - digits(get(row, col)); i++) {
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

    public int get(int row, int column) {
        return mValues[(column - 1) + (4 * (row - 1))];
    }

    private void set(int row, int column, int value) {
        mValues[(column - 1) + (4 * (row - 1))] = value;
    }

    public Grid shiftLeft(Random r, EventListener listener) {
        Grid shifted = New(mValues);
        boolean moveOrMerge = false;
        for (int row = 1; row <= 4; row++) {
            for (int col = 2; col <= 4; col++) {
                if (shifted.get(row, col) != 0) {
                    moveOrMerge |= shifted.shiftBlockLeft(row, col, listener);
                }
            }
        }
        if (moveOrMerge) {
            if (shifted.canAddBlock()) {
                RowCol addedRowCol = shifted.addBlock(r);
                listener.newBlock(addedRowCol.row, addedRowCol.col, shifted.get(addedRowCol.row, addedRowCol.col));
            }
        }
        return shifted;
    }

    private Grid shift(int direction, Random r, EventListener listener) {

        return null;
    }

    private boolean shiftBlockLeft(int row, int col, EventListener listener) {
        boolean blockMoved = false;
        int startCol = col;
        for (; col >= 2; col--) {
            if (get(row, col - 1) == 0) {
                set(row, col - 1, get(row, col));
                set(row, col, 0);
                blockMoved = true;
            } else if (get(row, col - 1) == get(row, col)) {
                int newValue = 2 * get(row, col);
                set(row, col - 1, newValue);
                set(row, col, 0);
                if (blockMoved) {
                    listener.blockMoved(row, startCol, row, col);
                }
                listener.blocksMerged(row, col, row, col - 1, newValue);
                return true;
            } else {
                break;
            }
        }
        if (blockMoved) {
            listener.blockMoved(row, startCol, row, col);
        }
        return blockMoved;
    }

    private void shiftBlockRight(int row, int col) {

    }
}
