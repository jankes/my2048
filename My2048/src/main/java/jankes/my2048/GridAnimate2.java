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

import java.util.Random;

public class GridAnimate2 extends Activity {
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
                    2, 2, 0, 0,   // 0 2 2 4
                    0, 0, 0, 0,   // 2 2 4 0
                    4, 4, 2, 2,   // 8 2 2 4
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

            final AnimatorSet[] animators = new AnimatorSet[4];
            final AnimatorSet.Builder[] builders = new AnimatorSet.Builder[4];

            final ObjectAnimator[] newAnimator = new ObjectAnimator[1];

            final Grid shifted = mGrid.shiftLeft(mRand, new Grid.EventListener() {
                @Override
                public void blockMoved(final int startRow, final int startColumn, final int endRow, final int endColumn) {
                    Log.d(TAG, String.format("blockMoved: (%d, %d) to (%d, %d", startRow, startColumn, endRow, endColumn));

                    final Block block = getBlock(startRow, startColumn);
                    ObjectAnimator moveAnimator = ObjectAnimator.ofFloat(block,"x", block.getX(), columnToX(endColumn));
                    moveAnimator.setDuration(2000);
                    moveAnimator.addUpdateListener(View2048.this);
                    moveAnimator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            setBlock(endRow, endColumn, block);
                            setBlock(startRow, startColumn, null);
                        }
                    });

                    int index = endRow - 1;
                    if (animators[index] == null) {
                        animators[index] = new AnimatorSet();
                        builders[index] = animators[index].play(moveAnimator);
                    } else {
                        if (builders[index] != null) {
                            builders[index] = builders[index].with(moveAnimator);
                        } else {
                            AnimatorSet next = new AnimatorSet();
                            builders[index] = next.play(moveAnimator).after(animators[index]);
                            animators[index] = next;
                        }
                    }
                }

                @Override
                public void blocksMerged(final int srcRow, final int srcColumn, final int dstRow, final int dstColumn, int newValue) {
                    Log.d(TAG, String.format("blocksMerged: (%d, %d) into (%d, %d)", srcRow, srcColumn, dstRow, dstColumn));

                    final Block newBlock = new Block(columnToX(dstColumn), rowToY(dstRow), mBlockBitmaps.getBitmap(newValue));
                    ObjectAnimator mergeAnimator = ObjectAnimator.ofFloat(newBlock, "scale", 1.0f, 1.25f);
                    mergeAnimator.setDuration(500);
                    mergeAnimator.setRepeatMode(ValueAnimator.REVERSE);
                    mergeAnimator.setRepeatCount(1);
                    mergeAnimator.addUpdateListener(View2048.this);
                    mergeAnimator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            setBlock(srcRow, srcColumn, null);
                            setBlock(dstRow, dstColumn, newBlock);
                        }
                    });

                    int index = dstRow - 1;
                    if (animators[index] == null) {
                        AnimatorSet mergePlayer = new AnimatorSet();
                        mergePlayer.play(mergeAnimator);
                        animators[index] = mergePlayer;
                    } else {
                        AnimatorSet next = new AnimatorSet();
                        next.play(mergeAnimator).after(animators[index]);
                        animators[index] = next;
                        builders[index] = null;
                    }
                }

                @Override
                public void newBlock(final int row, final int column, int value) {
                    Log.d(TAG, String.format("newBlock: (%d, %d) --> %d", row, column, value));

                    final Block newBlock = new Block(columnToX(column), rowToY(row), mBlockBitmaps.getBitmap(value));
                    newAnimator[0] = ObjectAnimator.ofFloat(newBlock,"scale", 0.5f, 1.0f);
                    newAnimator[0].setDuration(1000);
                    newAnimator[0].addUpdateListener(View2048.this);
                    newAnimator[0].addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            setBlock(row, column, newBlock);
                        }
                    });
                }
            });

            //
            // can check shifted grid here for win/loss before kicking off any animations
            //

            AnimatorSet moveMergePlayer = new AnimatorSet();
            AnimatorSet.Builder moveMergeBuilder = null;
            for (int i = 0; i < 4; i++) {
                if (animators[i] == null) {
                    continue;
                }
                if (moveMergeBuilder == null) {
                    moveMergeBuilder = moveMergePlayer.play(animators[i]);
                } else {
                    moveMergeBuilder = moveMergeBuilder.with(animators[i]);
                }
            }

            AnimatorSet all = new AnimatorSet();
            if (moveMergeBuilder != null) {
                all.play(newAnimator[0]).after(moveMergePlayer);
            } else {
                all.play(newAnimator[0]);
            }
            all.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mShifting = false;
                    mGrid = shifted;

                    Log.d(TAG, "set new grid:");
                    Log.d(TAG, mGrid.toString());
                }
            });
            all.start();

            return true;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            for(Block block : mBlocks) {
                if (block == null) {
                    continue;
                }
                canvas.save();
                canvas.translate(block.getX(), block.getY());
                canvas.scale(block.getScale(), block.getScale());
                canvas.drawBitmap(block.getBitmap(), 0.0f, 0.0f, null);
                canvas.restore();
            }
        }
    }
}