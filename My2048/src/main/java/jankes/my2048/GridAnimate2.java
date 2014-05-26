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
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GridAnimate2 extends Activity {
    private static final String TAG = "My2048";

    private Random mRand;
    private Grid2 mGrid;
    private AnimatorSet mShiftAnimateUpdate;
    private boolean mContinueGameWinAnimation;
    private AnimatorSet mGameWinAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game2048);

        mRand = new Random(1000);

        // TODO: read game win state and grid from saved instance state
        mContinueGameWinAnimation = true;
        //mGrid = Grid2.New(mRand);
        mGrid = Grid2.New(new int[] {
                0, 0, 0, 0,
                2, 2, 0, 0,   // 0 2 2 4
                1024, 1024, 0, 0,   // 2 2 4 0
                4, 4, 2, 2,   // 8 2 2 4
        });

        LinearLayout container = (LinearLayout)findViewById(R.id.container);
        container.addView(new View2048(this));
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mShiftAnimateUpdate != null) {
            mShiftAnimateUpdate.end();
        }
        if (mGameWinAnimation != null) {
            mContinueGameWinAnimation = false;
            mGameWinAnimation.cancel();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGameWinAnimation != null) {
            mContinueGameWinAnimation = true;
            mGameWinAnimation.start();
        }
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
                Bitmap bitmap16 = BitmapFactory.decodeResource(getResources(), R.drawable.sixteen);
                mValueToBitmap.append(16, bitmap16);
                return bitmap16;
            } else if (blockValue == 32) {
                Bitmap bitmap32 = BitmapFactory.decodeResource(getResources(), R.drawable.thirtytwo);
                mValueToBitmap.append(32, bitmap32);
                return bitmap32;
            } else if (blockValue == 64) {
                Bitmap bitmap64 = BitmapFactory.decodeResource(getResources(), R.drawable.sixtyfour);
                mValueToBitmap.append(64, bitmap64);
                return bitmap64;
            } else if (blockValue == 128) {
                Bitmap bitmap128 = BitmapFactory.decodeResource(getResources(), R.drawable.onetwentyeight);
                mValueToBitmap.append(128, bitmap128);
                return bitmap128;
            } else if (blockValue == 256) {
                Bitmap bitmap256 = BitmapFactory.decodeResource(getResources(), R.drawable.twofiftysix);
                mValueToBitmap.append(256, bitmap256);
                return bitmap256;
            } else if (blockValue == 512) {
                Bitmap bitmap512 = BitmapFactory.decodeResource(getResources(), R.drawable.fivetwelve);
                mValueToBitmap.append(512, bitmap512);
                return bitmap512;
            } else if (blockValue == 1024) {
                Bitmap bitmap1024 = BitmapFactory.decodeResource(getResources(), R.drawable.tentwentyfour);
                mValueToBitmap.append(1024, bitmap1024);
                return bitmap1024;
            } else if (blockValue == 2048) {
                Bitmap bitmap2048 = BitmapFactory.decodeResource(getResources(), R.drawable.twentyfortyeight);
                mValueToBitmap.append(2048, bitmap2048);
                return bitmap2048;
            }
            else {
                String msg = "no bitmap for value " + blockValue;
                Log.e(TAG, msg);
                throw new RuntimeException(msg);
            }
        }
    }

    private class View2048 extends View implements ValueAnimator.AnimatorUpdateListener {
        private BlockBitmapManager mBlockBitmaps;
        private Block[] mBlocks;
        private Bitmap mWinBitmap;
        private GestureDetector mGestureDetector;

        public View2048(Context context) {
            super(context);
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
            mGestureDetector = new GestureDetector(context, new UpdateGestureListener());
        }

        private float rowToY(int row) {
            return (row - 1) * 150f;
        }

        private float columnToX(int col) {
            return (col - 1) * 150f;
        }

        private Block getBlock(int row, int column) {
            return mBlocks[(4 * row) + column - 5];
        }

        private void setBlock(int row, int column, Block block) {
            mBlocks[(4 * row) + column - 5] = block;
        }

        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            invalidate();
        }

        private class AnimateUpdateGridEventListener implements Grid2.EventListener {
            final AnimatorSet[] mAnimators = new AnimatorSet[4];
            final AnimatorSet.Builder[] mBuilders = new AnimatorSet.Builder[4];
            ObjectAnimator mNewAnimator;

            public void blockMoved(final int startRow, final int startColumn, final int endRow, final int endColumn) {
                Log.d(TAG, String.format("blockMoved: (%d, %d) to (%d, %d)", startRow, startColumn, endRow, endColumn));

                ObjectAnimator moveAnimator = createMoveAnimator(startRow, startColumn, endRow, endColumn);
                int index = getAnimatorIndex(startRow, startColumn, endRow);
                if (mAnimators[index] == null) {
                    mAnimators[index] = new AnimatorSet();
                    mBuilders[index] = mAnimators[index].play(moveAnimator);
                } else {
                    if (mBuilders[index] != null) {
                        mBuilders[index] = mBuilders[index].with(moveAnimator);
                    } else {
                        AnimatorSet next = new AnimatorSet();
                        mBuilders[index] = next.play(moveAnimator).after(mAnimators[index]);
                        mAnimators[index] = next;
                    }
                }
            }

            private ObjectAnimator createMoveAnimator(final int startRow, final int startColumn, final int endRow, final int endColumn) {
                final Block block = getBlock(startRow, startColumn);
                ObjectAnimator moveAnimator;
                if (startRow == endRow) {
                    moveAnimator = ObjectAnimator.ofFloat(block,"x", block.getX(), columnToX(endColumn));
                } else {
                    moveAnimator = ObjectAnimator.ofFloat(block, "y", block.getY(), rowToY(endRow));
                }
                moveAnimator.setDuration(1500);
                moveAnimator.addUpdateListener(View2048.this);
                moveAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        setBlock(endRow, endColumn, block);
                        setBlock(startRow, startColumn, null);
                    }
                });
                return moveAnimator;
            }

            @Override
            public void blocksMerged(final int srcRow, final int srcColumn, final int dstRow, final int dstColumn, int newValue) {
                Log.d(TAG, String.format("blocksMerged: (%d, %d) into (%d, %d) newValue = %d", srcRow, srcColumn, dstRow, dstColumn, newValue));

                ObjectAnimator mergeAnimator = createMergeAnimator(srcRow, srcColumn, dstRow, dstColumn, newValue);
                int index = getAnimatorIndex(srcRow, srcColumn, dstRow);
                if (mAnimators[index] == null) {
                    AnimatorSet mergePlayer = new AnimatorSet();
                    mergePlayer.play(mergeAnimator);
                    mAnimators[index] = mergePlayer;
                } else {
                    AnimatorSet next = new AnimatorSet();
                    next.play(mergeAnimator).after(mAnimators[index]);
                    mAnimators[index] = next;
                    mBuilders[index] = null;
                }
            }

            private ObjectAnimator createMergeAnimator(final int srcRow, final int srcColumn, final int dstRow, final int dstColumn, int newValue) {
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
                return mergeAnimator;
            }

            private int getAnimatorIndex(int row1, int column1, int row2) {
                if (row1 == row2) {
                    return row1 - 1;
                } else {
                    return column1 - 1;
                }
            }

            @Override
            public void newBlock(final int row, final int column, int value) {
                Log.d(TAG, String.format("newBlock: (%d, %d) --> %d", row, column, value));

                final Block newBlock = new Block(columnToX(column), rowToY(row), mBlockBitmaps.getBitmap(value));
                mNewAnimator = ObjectAnimator.ofFloat(newBlock,"scale", 0.5f, 1.0f);
                mNewAnimator.setDuration(500);
                mNewAnimator.addUpdateListener(View2048.this);
                mNewAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        setBlock(row, column, newBlock);
                    }
                });
            }

            public AnimatorSet createAnimateUpdatePlayer() {
                AnimatorSet createUpdate = new AnimatorSet();
                AnimatorSet moveMergePlayer = createMoveMergePlayer();
                if (mNewAnimator != null) {
                    if (moveMergePlayer.getChildAnimations().size() > 0) {
                        createUpdate.play(moveMergePlayer).before(mNewAnimator);
                    } else {
                        createUpdate.play(mNewAnimator);
                    }
                } else {
                    createUpdate.play(moveMergePlayer);
                }
                return createUpdate;
            }

            private AnimatorSet createMoveMergePlayer() {
                AnimatorSet moveMergePlayer = new AnimatorSet();
                AnimatorSet.Builder moveMergeBuilder = null;
                for (int i = 0; i < 4; i++) {
                    if (mAnimators[i] == null) {
                        continue;
                    }
                    if (moveMergeBuilder == null) {
                        moveMergeBuilder = moveMergePlayer.play(mAnimators[i]);
                    } else {
                        moveMergeBuilder = moveMergeBuilder.with(mAnimators[i]);
                    }
                }
                return moveMergePlayer;
            }
        }

        private class UpdateGestureListener extends GestureDetector.SimpleOnGestureListener {
            private static final float SHIFT_FLOOR = 500f;

            @Override
            public boolean onDown(MotionEvent event) {
                // If mShiftAnimateUpdate is null (no current shift animation) keep the touch
                // Otherwise already running shift animation, returns false to ignore touch
                return mShiftAnimateUpdate == null;
            }

            @Override
            public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
                if (mGameWinAnimation != null) {
                    return false;
                }

                AnimateUpdateGridEventListener listener = new AnimateUpdateGridEventListener();
                final Grid2 shifted = maybeShiftGrid(velocityX, velocityY, listener);
                if (shifted.equals(mGrid)) {
                    return false;
                }

                mShiftAnimateUpdate = listener.createAnimateUpdatePlayer();
                mShiftAnimateUpdate.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mGrid = shifted;
                        mShiftAnimateUpdate = null;

                        Log.d(TAG, "set new grid:");
                        Log.d(TAG, mGrid.toString());

                        if (mGrid.containsBlock(2048)) {
                            mGameWinAnimation = createGameWinAnimation();
                            mGameWinAnimation.start();
                        }
                    }
                });
                mShiftAnimateUpdate.start();
                return true;
            }

            private Grid2 maybeShiftGrid(float velocityX, float velocityY, Grid2.EventListener listener) {
                if (Math.abs(velocityX) < SHIFT_FLOOR && Math.abs(velocityY) < SHIFT_FLOOR) {
                    Log.d(TAG, String.format("no shift: velocityX = %f velocityY = %f", velocityX, velocityY));
                    return mGrid;
                }
                if (Math.abs(velocityX) > Math.abs(velocityY) && Math.abs(velocityX / velocityY) > 1.5f) {
                    if (velocityX < 0f) {
                        Log.d(TAG, "shift left");
                        return mGrid.shiftLeft(mRand, listener);
                    } else {
                        Log.d(TAG, "shift right");
                        return mGrid.shiftRight(mRand, listener);
                    }
                } else if (Math.abs(velocityY) > Math.abs(velocityX) && Math.abs(velocityY / velocityX) > 1.5f) {
                    if (velocityY < 0f) {
                        Log.d(TAG, "shift up");
                        return mGrid.shiftUp(mRand, listener);
                    } else {
                        Log.d(TAG, "shift down");
                        return mGrid.shiftDown(mRand, listener);
                    }
                } else {
                    Log.d(TAG, String.format("no shift: velocityX = %f velocityY = %f", velocityX, velocityY));
                    return mGrid;
                }
            }
        }

        private AnimatorSet createGameWinAnimation() {
            List<AnimatorSet> blockMoveAnimators = new ArrayList<AnimatorSet>(mBlocks.length);
            for (final Block block : mBlocks) {
                if (block == null) {
                    continue;
                }

                Animator.AnimatorListener moveEndListener = new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (mContinueGameWinAnimation) {
                            AnimatorSet nextMoveAnimator = createMoveToRandomXYAnimation(block, this);
                            nextMoveAnimator.start();
                        }
                    }
                };

                AnimatorSet moveAnimator = createMoveToRandomXYAnimation(block, moveEndListener);
                blockMoveAnimators.add(moveAnimator);
            }
            AnimatorSet allMoves = new AnimatorSet();
            allMoves.playTogether(blockMoveAnimators.toArray(new Animator[blockMoveAnimators.size()]));
            return allMoves;
        }

        private AnimatorSet createMoveToRandomXYAnimation(Block block, Animator.AnimatorListener listener) {
            AnimatorSet moveAnimator = new AnimatorSet();
            ObjectAnimator xMove = createMoveToRandomXAnimator(block);
            ObjectAnimator yMove = createMoveToRandomYAnimator(block);
            moveAnimator.playTogether(xMove, yMove);
            moveAnimator.addListener(listener);
            return moveAnimator;
        }

        private ObjectAnimator createMoveToRandomXAnimator(Block block) {
            float endX = (getWidth() - block.getBitmap().getWidth()) * mRand.nextFloat();
            ObjectAnimator xMove = ObjectAnimator.ofFloat(block, "x", block.getX(), endX);
            xMove.setDuration(2000L + mRand.nextInt(2000));
            xMove.addUpdateListener(this);
            return xMove;
        }

        private ObjectAnimator createMoveToRandomYAnimator(Block block) {
            float endY = (getHeight() - block.getBitmap().getHeight()) * mRand.nextFloat();
            ObjectAnimator yMove = ObjectAnimator.ofFloat(block, "y", block.getY(), endY);
            yMove.setDuration(2000L + mRand.nextInt(2000));
            yMove.addUpdateListener(this);
            return yMove;
        }

        @Override
        public boolean onTouchEvent(@NotNull MotionEvent event) {
            return mGestureDetector.onTouchEvent(event);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            Bitmap bitmap2048 = mBlockBitmaps.getBitmap(2048);
            Block block2048 = null;
            for (Block block : mBlocks) {
                if (block == null) {
                    continue;
                }

                // Save the first 2048 block and draw it last
                // This so it draws over everything in the game win animation
                if (block.getBitmap() == bitmap2048 && block2048 == null) {
                    block2048 = block;
                } else {
                    drawBlock(block, canvas);
                }
            }
            drawGameWinBitmap(canvas);
            if (block2048 != null) {
                drawBlock(block2048, canvas);
            }
        }

        private void drawBlock(Block block, Canvas canvas) {
            canvas.save();
            canvas.translate(block.getX(), block.getY());
            canvas.scale(block.getScale(), block.getScale());
            canvas.drawBitmap(block.getBitmap(), 0.0f, 0.0f, null);
            canvas.restore();
        }

        private void drawGameWinBitmap(Canvas canvas) {
            if (mGameWinAnimation == null) {
                return;
            }
            if (mWinBitmap == null) {
                mWinBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.win);
            }
            float left = (getWidth() - mWinBitmap.getWidth()) / 2f;
            float top = (getHeight() - mWinBitmap.getHeight()) / 2f;
            canvas.drawBitmap(mWinBitmap, left, top, null);
        }
    }
}
