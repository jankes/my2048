package jankes.my2048;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GridAnimate extends Activity{
    private static final String TAG = "My2048";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game2048);

        LinearLayout container = (LinearLayout)findViewById(R.id.container);
        container.addView(new View2048(this));
    }

    private static class Block {
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

        public void setX(float value) {
            mX = value;
        }

        public float getY() {
            return mY;
        }

        public void setY(float value) {
            mY = value;
        }

        public float getScale() {
            return mScale;
        }

        public void setScale(float value) {
            mScale = value;
        }

        public Bitmap getBitmap() {
            return mBitmap;
        }
    }

    private class BlockBitmapManager {
        SparseArray<Bitmap> mValueToBitmap;

        public BlockBitmapManager() {
            mValueToBitmap = new SparseArray<Bitmap>(11);

            Resources resources = getResources();
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
            if (blockValue == 8) {
                Bitmap bitmap8 = BitmapFactory.decodeResource(getResources(), R.drawable.eight);
                mValueToBitmap.append(8, bitmap8);
                return bitmap8;
            } else if (blockValue == 16) {

                Log.e(TAG, "no bitmap for 16 defined yet!");
                throw new RuntimeException("");
            } else {
                String msg = "no bitmap for value " + blockValue;
                Log.e(TAG, msg);
                throw new RuntimeException(msg);
            }
        }
    }

    public class View2048 extends View implements ValueAnimator.AnimatorUpdateListener {

        Random mRand;
        Grid mGrid;
        BlockBitmapManager mBlockBitmaps;
        Block[] mBlocks;
        boolean mShifting;

        public View2048(Context context) {
            super(context);
            mRand = new Random(1000);
            //mGrid = Grid.New(mRand);
            mGrid = Grid.New(new int[] {
                    0, 0, 0, 0,
                    0, 2, 2, 0,
                    0, 0, 0, 0,   // 2 2 4 0
                    0, 0, 0, 0,   // 8 2 2 4
            });
            mBlockBitmaps = new BlockBitmapManager();
            mBlocks = new Block[16];
            for (int row = 1; row <= 4; row++) {
                for (int col = 1; col <= 4; col++) {
                    int value = mGrid.get(row, col);
                    if (value != 0) {
                        setBlock(row, col,
                                new Block(columnToX(col), rowToY(row), mBlockBitmaps.getBitmap(value)));
                    }
                }
            }
            mShifting = false;
        }

        private float rowToY(int row) {
            return (row - 1) * 150f;
        }

        private float columnToX(int col) {
            return (col - 1) * 150f;
        }

        private Block getBlock(int row, int column) {
            return mBlocks[(column - 1) + (4 * (row - 1))];
        }

        private void setBlock(int row, int column, Block block) {
            mBlocks[(column - 1) + (4 * (row - 1))] = block;
        }

        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            invalidate();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() != MotionEvent.ACTION_DOWN &&
                    event.getAction() != MotionEvent.ACTION_MOVE) {
                return false;
            }
            if (mShifting) {
                return false;
            }

            Log.d(TAG, "Shifting grid ...");

            mShifting = true;

//            final AnimatorSet[] animators = new AnimatorSet[4];
//
//            final AnimatorSet.Builder[] builders = new AnimatorSet.Builder[4];
//
//            mGrid.shiftLeft(mRand, new Grid.EventListener() {
//                @Override
//                public void blockMoved(final int startRow, final int startColumn, final int endRow, final int endColumn) {
//                    Log.d(TAG, "blockMoved");
//                    final Block block = getBlock(startRow, startColumn);
//                    int index = endRow - 1;
//                    ObjectAnimator moveAnimator = ObjectAnimator.ofFloat(block,"x", block.getX(), columnToX(endColumn));
//                    moveAnimator.setDuration(2000);
//                    moveAnimator.addUpdateListener(View2048.this);
//                    moveAnimator.addListener(new AnimatorListenerAdapter() {
//                        @Override
//                        public void onAnimationEnd(Animator animation) {
//                            Log.d(TAG, String.format("move animation end: (%d, %d) (%d, %d)", startRow, startColumn, endRow, endColumn));
//                            setBlock(endRow, endColumn, block);
//                            setBlock(startRow, startColumn, null);
//                        }
//                    });
//                    if (animators[index] == null) {
//                        animators[index] = new AnimatorSet();
//                        builders[index] = animators[index].play(moveAnimator);
//                    } else {
//                        builders[index] = builders[index].with(moveAnimator);
//                    }
//                }
//
//                @Override
//                public void blocksMerged(final int srcRow, final int srcColumn, final int dstRow, final int dstColumn, int newValue) {
//                    Log.d(TAG, "blocksMerged");
//
//                    final Block newBlock = new Block(columnToX(dstColumn), rowToY(dstRow), mBlockBitmaps.getBitmap(newValue));
//                    int index = dstRow - 1;
//                    ObjectAnimator mergeAnimator = ObjectAnimator.ofFloat(newBlock, "scale", newBlock.getScale(), 1.25f);
//                    mergeAnimator.setDuration(500);
//                    mergeAnimator.setRepeatMode(ValueAnimator.REVERSE);
//                    mergeAnimator.setRepeatCount(1);
//                    mergeAnimator.addUpdateListener(View2048.this);
//                    mergeAnimator.addListener(new AnimatorListenerAdapter() {
//                        @Override
//                        public void onAnimationStart(Animator animation) {
//                            Log.d(TAG, String.format("merge animation start: (%d, %d) (%d, %d)", srcRow, srcColumn, dstRow, dstColumn));
//                            setBlock(srcRow, srcColumn, null);
//                            setBlock(dstRow, dstColumn, newBlock);
//                        }
//                    });
//                    if (animators[index] == null) {
//                        animators[index] = new AnimatorSet();
//                        builders[index] = animators[index].play(mergeAnimator);
//                    } else {
//
//                        // TODO: was working here
//                        //       Event firing:
//                        //
//                        //       figure out how to get:
//                        //       move end (2, 2) (2, 1)
//                        //       move end (2, 3) (2, 2)
//                        //       merge start (2, 2) (2, 1)
//                        //
//                        //       currently getting
//                        //       move end: (2, 2) (2, 1)
//                        //       merge start: (2, 2) (2, 1)
//                        //       move end: (2, 3) (2, 2)
//
//                        // play(move1).with(move2).before(merge)
//
//                        builders[index] = builders[index].before(mergeAnimator);
//                    }
//
//                }
//
//                @Override
//                public void newBlock(int row, int column, int value) {
//
//                }
//            });




            // Move 1
            ObjectAnimator moveAnimator1;
            {
                final int startRow = 2;
                final int startColumn = 2;
                final int endRow = 2;
                final int endColumn = 1;
                final Block block = getBlock(startRow, startColumn);
                moveAnimator1 = ObjectAnimator.ofFloat(block, "x", block.getX(), columnToX(endColumn));
                moveAnimator1.setDuration(2000);
                moveAnimator1.addUpdateListener(View2048.this);
                moveAnimator1.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        Log.d(TAG, String.format("move animation end: (%d, %d) (%d, %d)", startRow, startColumn, endRow, endColumn));
                        setBlock(endRow, endColumn, block);
                        setBlock(startRow, startColumn, null);
                    }
                });
            }

            // Move 2
            ObjectAnimator moveAnimator2;
            {
                final int startRow = 2;
                final int startColumn = 3;
                final int endRow = 2;
                final int endColumn = 2;
                final Block block = getBlock(startRow, startColumn);
                moveAnimator2 = ObjectAnimator.ofFloat(block, "x", block.getX(), columnToX(endColumn));
                moveAnimator2.setDuration(2000);
                moveAnimator2.addUpdateListener(View2048.this);
                moveAnimator2.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        Log.d(TAG, String.format("move animation end: (%d, %d) (%d, %d)", startRow, startColumn, endRow, endColumn));
                        setBlock(endRow, endColumn, block);
                        setBlock(startRow, startColumn, null);
                    }
                });
            }

            // Merge
            ObjectAnimator mergeAnimator;
            {
                final int srcRow = 2;
                final int srcColumn = 2;
                final int dstRow = 2;
                final int dstColumn = 1;
                final int newValue = 4;
                final Block newBlock = new Block(columnToX(dstColumn), rowToY(dstRow), mBlockBitmaps.getBitmap(newValue));
                mergeAnimator = ObjectAnimator.ofFloat(newBlock, "scale", newBlock.getScale(), 1.25f);
                mergeAnimator.setDuration(500);
                mergeAnimator.setRepeatMode(ValueAnimator.REVERSE);
                mergeAnimator.setRepeatCount(1);
                mergeAnimator.addUpdateListener(View2048.this);
                mergeAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        Log.d(TAG, String.format("merge animation start: (%d, %d) (%d, %d)", srcRow, srcColumn, dstRow, dstColumn));
                        setBlock(srcRow, srcColumn, null);
                        setBlock(dstRow, dstColumn, newBlock);
                    }
                });
            }

            //AnimatorSet animatorSet = new AnimatorSet();
            //animatorSet.play(moveAnimator1).with(moveAnimator2).before(mergeAnimator);
            //animatorSet.play(mergeAnimator).before(moveAnimator1).with(moveAnimator2);
            //animatorSet.play(moveAnimator2).with(moveAnimator1).before(mergeAnimator);
            //animatorSet.start();

            AnimatorSet moveSet = new AnimatorSet();
            moveSet.play(moveAnimator1).with(moveAnimator2);

            AnimatorSet mergeSet = new AnimatorSet();
            mergeSet.play(mergeAnimator);

            AnimatorSet all = new AnimatorSet();
            all.play(mergeAnimator).after(moveSet);
            //all.play(moveSet).before(mergeAnimator);
            all.start();

//            AnimatorSet shiftAnim = new AnimatorSet();
//            AnimatorSet.Builder builder = null;
//            for (int i = 0; i < 4; i++) {
//                if (animators[i] == null) {
//                    continue;
//                }
//                if (builder == null) {
//                    builder = shiftAnim.play(animators[i]);
//                } else {
//                    builder = builder.with(animators[i]);
//                }
//            }
//            if (builder != null) {
//                shiftAnim.addListener(new AnimatorListenerAdapter() {
//                    @Override
//                    public void onAnimationEnd(Animator animation) {
//                        mShifting = false;
//                    }
//                });
//                shiftAnim.start();
//            }

            return true;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            for(Block block : mBlocks) {
                if (block == null) {
                    continue;
                }
                canvas.save();
                canvas.scale(block.getScale(), block.getScale());
                canvas.drawBitmap(block.getBitmap(), block.getX(), block.getY(), null);
                canvas.restore();
            }
        }
    }
}
