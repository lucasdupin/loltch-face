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

    private static final float HOUR_STROKE_WIDTH = 5f;
    private static final float MINUTE_STROKE_WIDTH = 3f;
    private static final float SECOND_TICK_STROKE_WIDTH = 2f;
    private static final float CENTER_GAP_AND_CIRCLE_RADIUS = 4f;
    private static final int SHADOW_RADIUS = 6;

    private Paint mHourPaint;
    private Paint mMinutePaint;
    private Paint mSecondPaint;
    private Paint mTickAndCirclePaint;

    @Override
    public boolean updatesEverySecond() {
        return true;
    }

    @Override
    public void onCreate() {
        /* Set defaults for colors */
        int mWatchHandColor = Color.WHITE;
        int mWatchHandHighlightColor = Color.RED;
        int mWatchHandShadowColor = Color.BLACK;

        mHourPaint = new Paint();
        mHourPaint.setColor(mWatchHandColor);
        mHourPaint.setStrokeWidth(HOUR_STROKE_WIDTH);
        mHourPaint.setAntiAlias(true);
        mHourPaint.setStrokeCap(Paint.Cap.ROUND);
        mHourPaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);

        mMinutePaint = new Paint();
        mMinutePaint.setColor(mWatchHandColor);
        mMinutePaint.setStrokeWidth(MINUTE_STROKE_WIDTH);
        mMinutePaint.setAntiAlias(true);
        mMinutePaint.setStrokeCap(Paint.Cap.ROUND);
        mMinutePaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);

        mSecondPaint = new Paint();
        mSecondPaint.setColor(mWatchHandHighlightColor);
        mSecondPaint.setStrokeWidth(SECOND_TICK_STROKE_WIDTH);
        mSecondPaint.setAntiAlias(true);
        mSecondPaint.setStrokeCap(Paint.Cap.ROUND);
        mSecondPaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);

        mTickAndCirclePaint = new Paint();
        mTickAndCirclePaint.setColor(mWatchHandColor);
        mTickAndCirclePaint.setStrokeWidth(SECOND_TICK_STROKE_WIDTH);
        mTickAndCirclePaint.setAntiAlias(true);
        mTickAndCirclePaint.setStyle(Paint.Style.STROKE);
        mTickAndCirclePaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);

        mHourPaint.setAntiAlias(true);
        mMinutePaint.setAntiAlias(true);
        mSecondPaint.setAntiAlias(true);
        mTickAndCirclePaint.setAntiAlias(true);

        mHourPaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);
        mMinutePaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);
        mSecondPaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);
        mTickAndCirclePaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);
    }

    @Override
    public void onDraw(Canvas canvas, Rect bounds, float hour, float minute, float seconds) {

        float mCenterX = bounds.centerX();
        float mCenterY = bounds.centerY();

        /*
         * Calculate lengths of different hands based on watch screen size.
         */
        float mSecondHandLength = (float) (mCenterX * 0.875);
        float sMinuteHandLength = (float) (mCenterX * 0.75);
        float sHourHandLength = (float) (mCenterX * 0.5);

        // Background colors
        canvas.drawColor(Color.BLACK);

        /*
         * These calculations reflect the rotation in degrees per unit of time, e.g.,
         * 360 / 60 = 6 and 360 / 12 = 30.
         */
        final float secondsRotation = seconds * 6f;
        final float minutesRotation = minute * 6f;
        final float hourHandOffset = minute / 2f;
        final float hoursRotation = (hour * 30) + hourHandOffset;

        // Draw hands
        canvas.save();

        canvas.rotate(hoursRotation, mCenterX, mCenterY);
        canvas.drawLine(mCenterX, mCenterY - CENTER_GAP_AND_CIRCLE_RADIUS,
                mCenterX, mCenterY - sHourHandLength,
                mHourPaint);

        canvas.rotate(minutesRotation - hoursRotation, mCenterX, mCenterY);
        canvas.drawLine(mCenterX, mCenterY - CENTER_GAP_AND_CIRCLE_RADIUS,
                mCenterX, mCenterY - sMinuteHandLength,
                mMinutePaint);

        // Do not draw seconds hand on ambient mode
        if (!isAmbient) {
            canvas.rotate(secondsRotation - minutesRotation, mCenterX, mCenterY);
            canvas.drawLine(mCenterX, mCenterY - CENTER_GAP_AND_CIRCLE_RADIUS,
                    mCenterX, mCenterY - mSecondHandLength,
                    mSecondPaint);

        }

        // Small circle on top
        canvas.drawCircle(mCenterX, mCenterY, CENTER_GAP_AND_CIRCLE_RADIUS, mTickAndCirclePaint);

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
