package tests;


import android.test.InstrumentationTestCase;
import android.util.Log;

import java.util.Random;

import jankes.my2048.Grid;

public class GridTest extends InstrumentationTestCase {

    private static final String TAG = "My2048";

    private static class LoggingEventListener implements Grid.EventListener {
        @Override
        public void blockMoved(int startRow, int startColumn, int endRow, int endColumn) {
            Log.d(TAG, String.format("block moved: startRow %1d, startColumn = %2d, endRow = %3d, endColumn = %4d",
                    startRow, startColumn, endRow, endColumn));
        }

        @Override
        public void blocksMerged(int srcRow, int srcColumn, int dstRow, int dstColumn, int newValue) {
            Log.d(TAG, String.format("blocks merged: srcRow = %1d, srcColumn = %2d, dstRow = %3d, dstColumn = %4d, newValue = %5d",
                    srcRow, srcColumn, dstRow, dstColumn, newValue));
        }

        @Override
        public void newBlock(int row, int column, int value) {
            Log.d(TAG, String.format("new block: row = %1d, column = %2d, value = %3d", row, column, value));
        }
    }

    public void testShiftLeft1() {
        Random r = new Random(1000);

        Grid test1 = Grid.New(r);
        Log.d(TAG, "initial grid:");
        Log.d(TAG, test1.toString());

        Grid test1Left = test1.shiftLeft(r, new LoggingEventListener());
        Log.d(TAG, "shifted left:");
        Log.d(TAG, test1Left.toString());
    }

    public void testShiftLeft2() {
        runShiftLeftTest(Grid.New(new int[] {
                2, 2, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0,
        }));
    }

    public void testShiftLeft3() {
        runShiftLeftTest(Grid.New(new int[] {
                2, 4, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0,
        }));
    }

    public void testShiftLeft4() {
        runShiftLeftTest(Grid.New(new int[] {
                2, 2, 2, 2,
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0,
        }));
    }

    public void testShiftLeft5() {
        runShiftLeftTest(Grid.New(new int[] {
                0, 0, 0, 0,
                0, 0, 2, 2,
                0, 0, 0, 0,
                0, 0, 0, 0,
        }));
    }

    public void testShiftLeft6() {
        runShiftLeftTest(Grid.New(new int[] {
                0, 0, 0, 0,
                0, 2, 0, 2,
                0, 0, 0, 0,
                0, 0, 0, 0,
        }));
    }

    public void testShiftLeft7() {
        runShiftLeftTest(Grid.New(new int[] {
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 2, 2, 2,
                0, 0, 0, 0,
        }));
    }

    public void testShiftLeft8() {
        runShiftLeftTest(Grid.New(new int[] {
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 2, 2, 4,
                0, 0, 0, 0,
        }));
    }

    private void runShiftLeftTest(Grid g) {
        Random r = new Random(1000);

        Log.d(TAG, "initial grid:");
        Log.d(TAG, g.toString());

        Grid test1Left = g.shiftLeft(r, new LoggingEventListener());
        Log.d(TAG, "shifted left:");
        Log.d(TAG, test1Left.toString());
    }

}
