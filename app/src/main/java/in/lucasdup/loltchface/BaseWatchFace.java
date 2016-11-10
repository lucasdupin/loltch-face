package in.lucasdup.loltchface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.view.SurfaceHolder;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public abstract class BaseWatchFace extends CanvasWatchFaceService {

    //
    // Abstract methods that can be overridden
    //

    /**
     * Should this watch repaint at every second?
     * @return true if yes
     */
    public abstract boolean updatesEverySecond();

    /**
     * Watch face creation hook. Initialize your paints and drawables in here
     */
    public abstract void onCreate();

    /**
     * Render your watch face in here. Remember to consider ambiente mode by reading
     * isAmbient
     *
     * @param canvas Canvas
     * @param bounds Window size
     * @param hour hours
     * @param minute minutes
     * @param second seconds
     */
    public abstract void onDraw(Canvas canvas, Rect bounds, float hour, float minute, float second);

    /**
     * Tap event handling
     * @param tapType TAP_TYPE_TOUCH, TAP_TYPE_TOUCH_CANCEL or TAP_TYPE_TAP
     * @param x x position
     * @param y y position
     * @param eventTime millis
     */
    public abstract void onTapCommand(int tapType, int x, int y, long eventTime);

    /**
     * Override to know when you enter or exit ambient mode
     * @param inAmbientMode true if ambient mode is on
     */
    public void onAmbientModeChanged(boolean inAmbientMode) {}

    protected boolean isAmbient;


    //
    // Base watch face implementation
    //

    private static final int MSG_UPDATE_TIME = 0;

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private static class EngineHandler extends Handler {
        private final WeakReference<BaseWatchFace.Engine> mWeakReference;

        private EngineHandler(BaseWatchFace.Engine reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            BaseWatchFace.Engine engine = mWeakReference.get();
            if (engine != null) {
                switch (msg.what) {
                    case MSG_UPDATE_TIME:
                        engine.handleUpdateTimeMessage();
                        break;
                }
            }
        }
    }

    private class Engine extends CanvasWatchFaceService.Engine {

        private final Rect mPeekCardBounds = new Rect();
        /* Handler to update the time once a second in interactive mode. */
        private final Handler mUpdateTimeHandler = new EngineHandler(this);
        private Calendar mCalendar;
        private final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mCalendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            }
        };
        private boolean mRegisteredTimeZoneReceiver = false;
//        private boolean mMuteMode;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(BaseWatchFace.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .setAcceptsTapEvents(true)
                    .build());

            mCalendar = Calendar.getInstance();

            BaseWatchFace.this.onCreate();
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            isAmbient = inAmbientMode;

            BaseWatchFace.this.onAmbientModeChanged(inAmbientMode);

            /* Check and trigger whether or not timer should be running (only in active mode). */
            updateTimer();
        }

        @Override
        public void onInterruptionFilterChanged(int interruptionFilter) {
            super.onInterruptionFilterChanged(interruptionFilter);
            boolean inMuteMode = (interruptionFilter == WatchFaceService.INTERRUPTION_FILTER_NONE);

            // TODO decide whether MUTE mode is relevant
//            /* Dim display in mute mode. */
//            if (mMuteMode != inMuteMode) {
//                mMuteMode = inMuteMode;
//                mHourPaint.setAlpha(inMuteMode ? 100 : 255);
//                mMinutePaint.setAlpha(inMuteMode ? 100 : 255);
//                mSecondPaint.setAlpha(inMuteMode ? 80 : 255);
//                invalidate();
//            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
        }

        /**
         * Captures tap event (and tap type). The {@link WatchFaceService#TAP_TYPE_TAP} case can be
         * used for implementing specific logic to handle the gesture.
         */
        @Override
        public void onTapCommand(int tapType, int x, int y, long eventTime) {
            BaseWatchFace.this.onTapCommand(tapType, x, y, eventTime);
            invalidate();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            long now = System.currentTimeMillis();
            mCalendar.setTimeInMillis(now);

            /*
             * These calculations reflect the rotation in degrees per unit of time, e.g.,
             * 360 / 60 = 6 and 360 / 12 = 30.
             */
            final float seconds =
                    (mCalendar.get(Calendar.SECOND) + mCalendar.get(Calendar.MILLISECOND) / 1000f);
            final float minutes = mCalendar.get(Calendar.MINUTE);
            final float hours = mCalendar.get(Calendar.HOUR);

            BaseWatchFace.this.onDraw(canvas, bounds, hours, minutes, seconds);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                registerReceiver();
                /* Update time zone in case it changed while we weren't visible. */
                mCalendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            } else {
                unregisterReceiver();
            }

            /* Check and trigger whether or not timer should be running (only in active mode). */
            updateTimer();
        }

        @Override
        public void onPeekCardPositionUpdate(Rect rect) {
            super.onPeekCardPositionUpdate(rect);
            mPeekCardBounds.set(rect);
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            BaseWatchFace.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            BaseWatchFace.this.unregisterReceiver(mTimeZoneReceiver);
        }

        /**
         * Starts/stops the {@link #mUpdateTimeHandler} timer based on the state of the watch face.
         */
        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer
         * should only run in active mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !isAmbient;
        }

        /**
         * Handle updating the time periodically in interactive mode.
         */
        private void handleUpdateTimeMessage() {
            invalidate();
            if (shouldTimerBeRunning()) {

                long updateInterval = updatesEverySecond() ?
                        TimeUnit.SECONDS.toMillis(1) :
                        TimeUnit.MINUTES.toMillis(1);

                long timeMs = System.currentTimeMillis();
                long delayMs = updateInterval
                        - (timeMs % updateInterval);
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
            }
        }
    }
}
