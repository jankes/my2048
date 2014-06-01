package jankes.my2048;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.SparseArray;

class BlockBitmapManager {
    private Context mContext;
    private SparseArray<Bitmap> mValueToBitmap;

    public BlockBitmapManager(Context context) {
        mContext = context;
        mValueToBitmap = new SparseArray<>(11);

        Resources resources = context.getResources();
        Bitmap bitmap2 = BitmapFactory.decodeResource(resources, R.drawable.two);
        mValueToBitmap.append(2, bitmap2);

        Bitmap bitmap4 = BitmapFactory.decodeResource(resources, R.drawable.four);
        mValueToBitmap.append(4, bitmap4);
    }

    public Bitmap getBitmap(int blockValue) {
        Bitmap bitmap = mValueToBitmap.get(blockValue, null);
        if (bitmap != null) {
            return bitmap;
        }
        Resources resources = mContext.getResources();
        if (blockValue == 8) {
            Bitmap bitmap8 = BitmapFactory.decodeResource(resources, R.drawable.eight);
            mValueToBitmap.append(8, bitmap8);
            return bitmap8;
        } else if (blockValue == 16) {
            Bitmap bitmap16 = BitmapFactory.decodeResource(resources, R.drawable.sixteen);
            mValueToBitmap.append(16, bitmap16);
            return bitmap16;
        } else if (blockValue == 32) {
            Bitmap bitmap32 = BitmapFactory.decodeResource(resources, R.drawable.thirtytwo);
            mValueToBitmap.append(32, bitmap32);
            return bitmap32;
        } else if (blockValue == 64) {
            Bitmap bitmap64 = BitmapFactory.decodeResource(resources, R.drawable.sixtyfour);
            mValueToBitmap.append(64, bitmap64);
            return bitmap64;
        } else if (blockValue == 128) {
            Bitmap bitmap128 = BitmapFactory.decodeResource(resources, R.drawable.onetwentyeight);
            mValueToBitmap.append(128, bitmap128);
            return bitmap128;
        } else if (blockValue == 256) {
            Bitmap bitmap256 = BitmapFactory.decodeResource(resources, R.drawable.twofiftysix);
            mValueToBitmap.append(256, bitmap256);
            return bitmap256;
        } else if (blockValue == 512) {
            Bitmap bitmap512 = BitmapFactory.decodeResource(resources, R.drawable.fivetwelve);
            mValueToBitmap.append(512, bitmap512);
            return bitmap512;
        } else if (blockValue == 1024) {
            Bitmap bitmap1024 = BitmapFactory.decodeResource(resources, R.drawable.tentwentyfour);
            mValueToBitmap.append(1024, bitmap1024);
            return bitmap1024;
        } else if (blockValue == 2048) {
            Bitmap bitmap2048 = BitmapFactory.decodeResource(resources, R.drawable.twentyfortyeight);
            mValueToBitmap.append(2048, bitmap2048);
            return bitmap2048;
        }
        else {
            String msg = "no bitmap for value " + blockValue;
            Log.e(Game2048.TAG, msg);
            throw new RuntimeException(msg);
        }
    }
}
