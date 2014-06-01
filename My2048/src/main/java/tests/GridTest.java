package tests;

import android.test.InstrumentationTestCase;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import jankes.my2048.Grid;

public class GridTest extends InstrumentationTestCase {

    // Deterministic grid shifting depends on a constant random seed value
    // The test cases expected shifted grids depend on the seed
    private static final long SEED = 1;

    private interface GridEvent{}

    private static class MoveEvent implements GridEvent {
        public final int startRow;
        public final int startCol;
        public final int endRow;
        public final int endCol;

        public MoveEvent(int startRow, int startCol, int endRow, int endCol) {
            this.startRow = startRow;
            this.startCol = startCol;
            this.endRow = endRow;
            this.endCol = endCol;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if(!(obj instanceof MoveEvent)) {
                return false;
            }
            MoveEvent other =(MoveEvent)obj;
            return (startRow == other.startRow) && (startCol == other.startCol) &&
                    (endRow == other.endRow) && (endCol == other.endCol);
        }

        @Override
        public String toString() {
            return String.format("MoveEvent: from (%d, %d) to (%d, %d)", startRow, startCol, endRow, endCol);
        }
    }

    private static class MergeEvent implements GridEvent {
        public final int srcRow;
        public final int srcCol;
        public final int dstRow;
        public final int dstCol;
        public final int newValue;

        public MergeEvent(int srcRow, int srcCol, int dstRow, int dstCol, int newValue) {
            this.srcRow = srcRow;
            this.srcCol = srcCol;
            this.dstRow = dstRow;
            this.dstCol = dstCol;
            this.newValue = newValue;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if(!(obj instanceof MergeEvent)) {
                return false;
            }
            MergeEvent other =(MergeEvent)obj;
            return (srcRow == other.srcRow) && (srcCol == other.srcCol) &&
                    (dstRow == other.dstRow) && (dstCol == other.dstCol) &&
                    (newValue == other.newValue);
        }

        @Override
        public String toString() {
            return String.format("MergeEvent: (%d, %d) into (%d, %d) newValue = %d", srcRow, srcCol,
                    dstRow, dstCol, newValue);
        }
    }

    private static class ShiftTestCase {
        public final String name;
        public final Grid before;
        public final Grid after;
        public final GridEvent[] expectedEvents;

        public ShiftTestCase(String name, Grid before, Grid after, GridEvent[] expectedEvents) {
            this.name = name;
            this.before = before;
            this.after = after;
            this.expectedEvents = expectedEvents;
        }
    }

    private ShiftTestCase[] getShiftLeftTestCases() {
        return new ShiftTestCase[] {
                new ShiftTestCase(
                        "single block",
                        Grid.New(new int[]{
                                2, 0, 0, 0,
                                0, 0, 0, 0,
                                0, 0, 0, 0,
                                0, 0, 0, 0,
                        }),
                        Grid.New(new int[]{
                                2, 0, 0, 0,
                                0, 0, 0, 0,
                                0, 0, 0, 0,
                                0, 0, 0, 0,
                        }),
                        new GridEvent[] { }),

                new ShiftTestCase(
                        "two block, no move no merge",
                        Grid.New(new int[]{
                                2, 4, 0, 0,
                                0, 0, 0, 0,
                                0, 0, 0, 0,
                                0, 0, 0, 0,
                        }),
                        Grid.New(new int[]{
                                2, 4, 0, 0,
                                0, 0, 0, 0,
                                0, 0, 0, 0,
                                0, 0, 0, 0,
                        }),
                        new GridEvent[] { }),

                new ShiftTestCase(
                        "one merge",
                        Grid.New(new int[]{
                                0, 0, 0, 0,
                                2, 2, 0, 0,
                                0, 0, 0, 0,
                                0, 0, 0, 0,
                        }),
                        Grid.New(new int[]{
                                0, 0, 0, 0,
                                4, 0, 0, 0,
                                2, 0, 0, 0,
                                0, 0, 0, 0,
                        }),
                        new GridEvent[] {
                                new MergeEvent(2, 2, 2, 1, 4)
                        }),

                new ShiftTestCase(
                        "one merge test 2",
                        Grid.New(new int[]{
                                0, 0, 0, 0,
                                2, 8, 2, 2,
                                0, 0, 0, 0,
                                0, 0, 0, 0,
                        }),
                        Grid.New(new int[]{
                                0, 0, 0, 0,
                                2, 8, 4, 0,
                                2, 0, 0, 0,
                                0, 0, 0, 0,
                        }),
                        new GridEvent[] {
                                new MergeEvent(2, 4, 2, 3, 4)
                        }),

                new ShiftTestCase(
                        "one move",
                        Grid.New(new int[]{
                                0, 0, 0, 0,
                                0, 0, 0, 0,
                                0, 0, 0, 0,
                                0, 0, 2, 0,
                        }),
                        Grid.New(new int[]{
                                0, 0, 0, 0,
                                0, 0, 0, 0,
                                2, 0, 0, 0,
                                2, 0, 0, 0,
                        }),
                        new GridEvent[] {
                                new MoveEvent(4, 3, 4, 1)
                        }),

                new ShiftTestCase(
                        "two moves",
                        Grid.New(new int[]{
                                0, 0, 0, 0,
                                0, 0, 2, 4,
                                0, 0, 0, 0,
                                0, 0, 0, 0,
                        }),
                        Grid.New(new int[]{
                                0, 0, 0, 0,
                                2, 4, 0, 0,
                                2, 0, 0, 0,
                                0, 0, 0, 0,
                        }),
                        new GridEvent[] {
                                new MoveEvent(2, 3, 2, 1), new MoveEvent(2, 4, 2, 2)
                        }),

                new ShiftTestCase(
                        "move move merge",
                        Grid.New(new int[]{
                                0, 0, 0, 0,
                                0, 0, 2, 2,
                                0, 0, 0, 0,
                                0, 0, 0, 0,
                        }),
                        Grid.New(new int[]{
                                0, 0, 0, 0,
                                4, 0, 0, 0,
                                2, 0, 0, 0,
                                0, 0, 0, 0,
                        }),
                        new GridEvent[] {
                                new MoveEvent(2, 3, 2, 1), new MoveEvent(2, 4, 2, 2),
                                new MergeEvent(2, 2, 2, 1, 4)
                        }),

                new ShiftTestCase(
                        "merge move merge",
                        Grid.New(new int[]{
                                2, 2, 4, 0,
                                0, 0, 0, 0,
                                0, 0, 0, 0,
                                0, 0, 0, 0,
                        }),
                        Grid.New(new int[]{
                                8, 0, 0, 0,
                                0, 0, 0, 0,
                                2, 0, 0, 0,
                                0, 0, 0, 0,
                        }),
                        new GridEvent[] {
                                new MergeEvent(1, 2, 1, 1, 4), new MoveEvent(1, 3, 1, 2),
                                new MergeEvent(1, 2, 1, 1, 8)
                        }),

                new ShiftTestCase(
                        "merge move move merge",
                        Grid.New(new int[]{
                                0, 0, 0, 0,
                                2, 2, 2, 2,
                                0, 0, 0, 0,
                                0, 0, 0, 0,
                        }),
                        Grid.New(new int[]{
                                0, 0, 0, 0,
                                4, 4, 0, 0,
                                2, 0, 0, 0,
                                0, 0, 0, 0,
                        }),
                        new GridEvent[] {
                                new MergeEvent(2, 2, 2, 1, 4), new MoveEvent(2, 3, 2, 2),
                                new MoveEvent(2, 4, 2, 3), new MergeEvent(2, 3, 2, 2, 4)
                        }),

                new ShiftTestCase(
                        "merge move merge move merge",
                        Grid.New(new int[]{
                                0, 0, 0, 0,
                                0, 0, 0, 0,
                                0, 0, 0, 0,
                                2, 2, 4, 8,
                        }),
                        Grid.New(new int[]{
                                0, 0, 0, 0,
                                0, 0, 0, 0,
                                2, 0, 0, 0,
                                16, 0, 0, 0,
                        }),
                        new GridEvent[] {
                                new MergeEvent(4, 2, 4, 1, 4), new MoveEvent(4, 3, 4, 2),
                                new MergeEvent(4, 2, 4, 1, 8), new MoveEvent(4, 4, 4, 2),
                                new MergeEvent(4, 2, 4, 1, 16)
                        }),

        };
    }

    private ShiftTestCase[] getShiftUpTestCases() {
        return new ShiftTestCase[]{
                new ShiftTestCase(
                        "no move no merge",
                        Grid.New(new int[]{
                                2, 4, 0, 2,
                                0, 0, 0, 4,
                                0, 0, 0, 2,
                                0, 0, 0, 4,
                        }),
                        Grid.New(new int[]{
                                2, 4, 0, 2,
                                0, 0, 0, 4,
                                0, 0, 0, 2,
                                0, 0, 0, 4,
                        }),
                        new GridEvent[]{}
                ),

                new ShiftTestCase(
                        "one merge",
                        Grid.New(new int[]{
                                0, 2, 0, 0,
                                0, 2, 0, 0,
                                0, 0, 0, 0,
                                0, 0, 0, 0,
                        }),
                        Grid.New(new int[]{
                                0, 4, 0, 0,
                                0, 0, 0, 0,
                                2, 0, 0, 0,
                                0, 0, 0, 0,
                        }),
                        new GridEvent[]{
                                new MergeEvent(2, 2, 1, 2, 4)
                        }
                ),

                new ShiftTestCase(
                        "one move",
                        Grid.New(new int[]{
                                0, 0, 0, 0,
                                0, 0, 0, 0,
                                0, 0, 0, 0,
                                0, 0, 2, 0,
                        }),
                        Grid.New(new int[]{
                                0, 0, 2, 0,
                                0, 0, 0, 0,
                                2, 0, 0, 0,
                                0, 0, 0, 0,
                        }),
                        new GridEvent[]{
                                new MoveEvent(4, 3, 1, 3)
                        }
                )
        };
    }

    private ShiftTestCase[] getShiftRightTestCases() {
        return new ShiftTestCase[]{
                new ShiftTestCase(
                        "no move no merge",
                        Grid.New(new int[]{
                                0, 0, 0, 2,
                                0, 2, 8, 4,
                                0, 0, 0, 2,
                                0, 0, 0, 4,
                        }),
                        Grid.New(new int[]{
                                0, 0, 0, 2,
                                0, 2, 8, 4,
                                0, 0, 0, 2,
                                0, 0, 0, 4,
                        }),
                        new GridEvent[]{}
                ),

                new ShiftTestCase(
                        "one merge",
                        Grid.New(new int[]{
                                0, 0, 0, 0,
                                0, 0, 0, 0,
                                0, 0, 2, 2,
                                0, 0, 0, 0,
                        }),
                        Grid.New(new int[]{
                                0, 0, 0, 0,
                                0, 0, 0, 0,
                                2, 0, 0, 4,
                                0, 0, 0, 0,
                        }),
                        new GridEvent[]{
                                new MergeEvent(3, 3, 3, 4, 4)
                        }
                ),

                new ShiftTestCase(
                        "one move",
                        Grid.New(new int[]{
                                0, 0, 2, 0,
                                0, 0, 0, 0,
                                0, 0, 0, 0,
                                0, 0, 0, 0,
                        }),
                        Grid.New(new int[]{
                                0, 0, 0, 2,
                                0, 0, 0, 0,
                                2, 0, 0, 0,
                                0, 0, 0, 0,
                        }),
                        new GridEvent[]{
                                new MoveEvent(1, 3, 1, 4)
                        }
                )
        };
    }

    private ShiftTestCase[] getShiftDownTestCases() {
        return new ShiftTestCase[]{
                new ShiftTestCase(
                        "no move no merge",
                        Grid.New(new int[]{
                                0, 0, 0, 2,
                                0, 0, 0, 4,
                                0, 0, 0, 2,
                                4, 2, 0, 4,
                        }),
                        Grid.New(new int[]{
                                0, 0, 0, 2,
                                0, 0, 0, 4,
                                0, 0, 0, 2,
                                4, 2, 0, 4,
                        }),
                        new GridEvent[]{}
                ),

                new ShiftTestCase(
                        "one merge",
                        Grid.New(new int[]{
                                0, 0, 0, 0,
                                0, 0, 0, 0,
                                0, 2, 0, 0,
                                0, 2, 0, 0,
                        }),
                        Grid.New(new int[]{
                                0, 0, 0, 0,
                                0, 0, 0, 0,
                                2, 0, 0, 0,
                                0, 4, 0, 0,
                        }),
                        new GridEvent[]{
                                new MergeEvent(3, 2, 4, 2, 4)
                        }
                ),

                new ShiftTestCase(
                        "one move",
                        Grid.New(new int[]{
                                2, 0, 0, 0,
                                0, 0, 0, 0,
                                0, 0, 0, 0,
                                0, 0, 0, 0,
                        }),
                        Grid.New(new int[]{
                                0, 0, 0, 0,
                                0, 0, 0, 0,
                                2, 0, 0, 0,
                                2, 0, 0, 0,
                        }),
                        new GridEvent[]{
                                new MoveEvent(1, 1, 4, 1)
                        }
                )
        };
    }

    private static class LoggingGridEventListener implements Grid.EventListener {
        private List<GridEvent> mEventList = new ArrayList<>(4);

        @Override
        public void blockMoved(int startRow, int startColumn, int endRow, int endColumn) {
            mEventList.add(new MoveEvent(startRow, startColumn, endRow, endColumn));
        }

        @Override
        public void blocksMerged(int srcRow, int srcColumn, int dstRow, int dstColumn, int newValue) {
            mEventList.add(new MergeEvent(srcRow, srcColumn, dstRow, dstColumn, newValue));
        }

        @Override
        public void newBlock(int row, int column, int value) {
            // not testing new block event
        }

        List<GridEvent> getEvents() {
            return mEventList;
        }
    }

    private interface ShiftGridAction {
        Grid shift(Grid g, Random rand, LoggingGridEventListener listener);
    }

    public void testShiftLeft() {
        runTestCases(getShiftLeftTestCases(), new ShiftGridAction() {
            @Override
            public Grid shift(Grid g, Random rand, LoggingGridEventListener listener) {
                return g.shiftLeft(rand, listener);
            }
        });
    }

    public void testShiftUp() {
        runTestCases(getShiftUpTestCases(), new ShiftGridAction() {
            @Override
            public Grid shift(Grid g, Random rand, LoggingGridEventListener listener) {
                return g.shiftUp(rand, listener);
            }
        });
    }

    public void testShiftRight() {
        runTestCases(getShiftRightTestCases(), new ShiftGridAction() {
            @Override
            public Grid shift(Grid g, Random rand, LoggingGridEventListener listener) {
                return g.shiftRight(rand, listener);
            }
        });
    }

    public void testShiftDown() {
        runTestCases(getShiftDownTestCases(), new ShiftGridAction() {
            @Override
            public Grid shift(Grid g, Random rand, LoggingGridEventListener listener) {
                return g.shiftDown(rand, listener);
            }
        });
    }

    private void runTestCases(ShiftTestCase[] testCases, ShiftGridAction shiftAction) {
        for (ShiftTestCase testCase : testCases) {
            Random rand = new Random(SEED);
            LoggingGridEventListener listener = new LoggingGridEventListener();

            Assert.assertEquals(testCase.name + ": shifted Grid should equal expected shifted Grid",
                    testCase.after,
                    shiftAction.shift(testCase.before, rand, listener));

            List<GridEvent> actualEvents = listener.getEvents();

            if (!Arrays.equals(testCase.expectedEvents, actualEvents.toArray())) {
                StringBuilder failMsgBuilder = new StringBuilder(testCase.name)
                        .append(": should get correct event sequence\n")
                        .append("Expected events:\n");
                for (GridEvent e : testCase.expectedEvents) {
                    failMsgBuilder.append(e.toString()).append("\n");
                }
                failMsgBuilder.append("Actual events:\n");
                for (GridEvent e : actualEvents) {
                    failMsgBuilder.append(e.toString()).append("\n");
                }
                Assert.fail(failMsgBuilder.toString());
            }
        }
    }
}
