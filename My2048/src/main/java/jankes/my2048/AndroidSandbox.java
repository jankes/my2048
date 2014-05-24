package jankes.my2048;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import java.util.Random;

public class AndroidSandbox extends Activity {

    private static final String TAG = "My2048";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game2048);

        LinearLayout container = (LinearLayout)findViewById(R.id.container);

        container.addView(new View2048(this));
    }

    public class View2048 extends View implements ValueAnimator.AnimatorUpdateListener {

        private Bitmap mNumber;
        private float mX;
        private float mY;
        private float mScaleX;
        private float mScaleY;
        private int mUpdateCount;
        private int mDrawCount;

        public View2048(Context context) {
            super(context);

            mX = 0f;
            mY = 0f;
            mScaleX = 1f;
            mScaleY = 1f;
            mUpdateCount = 0;
            mDrawCount = 0;

            //mNumber = getResources().getDrawable(R.drawable.ic_launcher);

            mNumber = BitmapFactory.decodeResource(getResources(), R.drawable.two);

        }

        public void setMyX(float value) {
            mX = value;
        }

        public void setMyY(float value) {
            mY = value;
        }

        public void setMyScaleX(float value) {
            mScaleX = value;
        }

        public void setMyScaleY(float value) {
            mScaleY = value;
        }

        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            mUpdateCount += 1;
            invalidate();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() != MotionEvent.ACTION_DOWN &&
                    event.getAction() != MotionEvent.ACTION_MOVE) {
                return false;
            }

            mX = 0f;
            mY = 0f;
            mScaleX = 1f;
            mScaleY = 1f;
            mUpdateCount = 0;
            mDrawCount = 0;

            ValueAnimator slideAnim = ObjectAnimator.ofFloat(this, "myY", 0f, 100f);
            slideAnim.setDuration(5000);
            slideAnim.addUpdateListener(this);

            ValueAnimator scaleXAnim = ObjectAnimator.ofFloat(this, "myScaleX", 1f, 1.5f);
            scaleXAnim.setDuration(5000);
            scaleXAnim.addUpdateListener(this);

            ValueAnimator scaleYAnim = ObjectAnimator.ofFloat(this, "myScaleY", 1f, 1.5f);
            scaleYAnim.setDuration(5000);
            //scaleYAnim.addUpdateListener(this);

            AnimatorSet animation = new AnimatorSet();

            animation.playTogether(slideAnim, scaleXAnim, scaleYAnim);

            animation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    Log.d(TAG, "update count = " + mUpdateCount);
                    Log.d(TAG, "draw count = " + mDrawCount);
                }
            });
            animation.start();

            return true;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            mDrawCount += 1;
            canvas.save();
            canvas.translate(mX, mY);
            canvas.scale(mScaleX, mScaleY);
            canvas.drawBitmap(mNumber, mX, mY, null);
            canvas.restore();
        }
    }
}
