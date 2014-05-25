package jankes.my2048;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

public class GestureTest extends Activity {
    private static final String TAG = "My2048";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game2048);

        LinearLayout container = (LinearLayout)findViewById(R.id.container);
        container.addView(new GestureTestView(this));
    }

    private class GestureTestView extends View {
        GestureDetector mGestureDetector;
        LoggingGestureListener mGestureListener;

        public GestureTestView(Context context) {
            super(context);
            mGestureListener = new LoggingGestureListener();
            mGestureDetector = new GestureDetector(context, mGestureListener);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            //Log.d(TAG, "view onTouchEvent");
            return mGestureDetector.onTouchEvent(event);
        }

        private class LoggingGestureListener extends GestureDetector.SimpleOnGestureListener {
            @Override
            public boolean onDown(MotionEvent event) {
                //Log.d(TAG, "onDown: " + event.toString());
                return true;
            }

            @Override
            public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
                Log.d(TAG, String.format("onFling:\nvelocityX = %f\nvelocityY = %f\nevent1 = %s\nevent2 = %s\n",
                        velocityX, velocityY, event1.toString(), event2.toString()));
                return true;
            }
        }
    }
}
