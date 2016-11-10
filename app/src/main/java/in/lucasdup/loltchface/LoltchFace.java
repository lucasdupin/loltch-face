package in.lucasdup.loltchface;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.widget.Toast;

/**
 * LOL watch face
 * Created by lucasdupin on 11/9/16.
 */

public class LoltchFace extends BaseWatchFace {

    private static final float STROKE_WIDTH = 5f;
    private static final float HAND_SIZE = 35f;
    private static final float X_OFFSET = 70f;

    private Paint mHandPaint;
    private Paint mOPaint;

    @Override
    public boolean updatesEverySecond() {
        return true;
    }

    @Override
    public void onCreate() {
        mHandPaint = new Paint();
        mHandPaint.setColor(Color.WHITE);
        mHandPaint.setStrokeWidth(STROKE_WIDTH);
        mHandPaint.setAntiAlias(true);
        mHandPaint.setStrokeCap(Paint.Cap.ROUND);

        mOPaint = new Paint();
        mOPaint.setColor(Color.WHITE);
        mOPaint.setStrokeWidth(STROKE_WIDTH);
        mOPaint.setAntiAlias(true);
        mOPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    public void onDraw(Canvas canvas, Rect bounds, float hour, float minute, float seconds) {

        hour = 3;
        minute = 0;

        float centerX = bounds.centerX();
        float centerY = bounds.centerY();

        // Background color
        canvas.drawColor(Color.BLACK);

        // Hands
        drawHands(canvas, hour, minute, centerX + X_OFFSET, centerY + HAND_SIZE / 2f);
        drawHands(canvas, hour, minute, centerX - X_OFFSET, centerY + HAND_SIZE / 2f);

        // Small circle on top
        canvas.drawCircle(centerX, centerY, HAND_SIZE / 2f, mOPaint);

    }

    private void drawHands(Canvas canvas, float hour, float minute, float x, float y) {
        canvas.save();

        /*
         * These calculations reflect the rotation in degrees per unit of time, e.g.,
         * 360 / 60 = 6 and 360 / 12 = 30.
         */
        final float minutesRotation = minute * 6f;
        final float hourHandOffset = minute / 2f;
        final float hoursRotation = (hour * 30) + hourHandOffset;

        // Hours
        canvas.rotate(hoursRotation, x, y);
        canvas.drawLine(x, y, x, y - HAND_SIZE * 0.6f, mHandPaint);
        // Minutes
        canvas.rotate(minutesRotation - hoursRotation, x, y);
        canvas.drawLine(x, y, x, y - HAND_SIZE, mHandPaint);

        canvas.restore();
    }

    @Override
    public void onTapCommand(int tapType, int x, int y, long eventTime) {
        switch (tapType) {
            case TAP_TYPE_TOUCH:
                // The user has started touching the screen.
                break;
            case TAP_TYPE_TOUCH_CANCEL:
                // The user has started a different gesture or otherwise cancelled the tap.
                break;
            case TAP_TYPE_TAP:
                // The user has completed the tap gesture.
                // TODO: Add code to handle the tap gesture.
                Toast.makeText(getApplicationContext(), R.string.message, Toast.LENGTH_SHORT)
                        .show();
                break;
        }
    }
}
