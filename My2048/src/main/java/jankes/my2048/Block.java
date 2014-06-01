package jankes.my2048;


import android.graphics.Bitmap;

class Block {
    private float mX;
    private float mY;
    private float mScale;
    private Bitmap mBitmap;

    public Block(float x, float y, Bitmap bitmap) {
        mX = x;
        mY = y;
        mScale = 1f;
        mBitmap = bitmap;
    }

    public float getX() {
        return mX;
    }

    // Needed in order to use ValueAnimator for animation
    @SuppressWarnings("unused")
    public void setX(float value) {
        mX = value;
    }

    public float getY() {
        return mY;
    }

    // Needed in order to use ValueAnimator for animation
    @SuppressWarnings("unused")
    public void setY(float value) {
        mY = value;
    }

    public float getScale() {
        return mScale;
    }

    // Needed in order to use ValueAnimator for animation
    @SuppressWarnings("unused")
    public void setScale(float value) {
        mScale = value;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }
}
