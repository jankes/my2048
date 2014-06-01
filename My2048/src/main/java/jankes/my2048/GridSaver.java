package jankes.my2048;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

class GridSaver {
    private static final String GRID_FILENAME = "grid";
    private Context mContext;

    public GridSaver (Context context) {
        mContext = context;
    }

    public void saveGrid(Grid grid) {
        int[] values = grid.getBlockValues();
        ByteBuffer outputBuffer = ByteBuffer.allocate(32);
        for (int i = 0; i < 16; i++) {
            outputBuffer.putShort((short)values[i]);
        }
        try {
            mContext.openFileOutput(GRID_FILENAME, Context.MODE_PRIVATE).write(outputBuffer.array());
        }
        catch (IOException e) {
            Log.e(Game2048.TAG, "IOException attempting to save grid");
        }
    }

    public Grid restoreGrid() {
        byte[] gridBytes = readGridBytes();
        if (gridBytes == null) {
            return null;
        }
        int[] blockValues = new int[16];
        ShortBuffer blockValueBuffer = ByteBuffer.wrap(gridBytes).asShortBuffer();
        for (int i = 0; i < 16; i++) {
            blockValues[i] = blockValueBuffer.get();
        }
        return Grid.New(blockValues);
    }

    private byte[] readGridBytes() {
        try {
            byte[] buffer = new byte[32];
            int readCount = mContext.openFileInput(GRID_FILENAME).read(buffer, 0, 32);
            if (readCount != 32) {
                Log.e(Game2048.TAG, "expected to read exactly 32 bytes for the stored grid, but instead read " + readCount);
                return null;
            }
            return buffer;
        }
        catch (IOException e) {
            return null;
        }
    }
}
