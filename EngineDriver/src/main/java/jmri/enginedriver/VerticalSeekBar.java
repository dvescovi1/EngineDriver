package jmri.enginedriver;
/*
  VerticalSeekBar code based on code from de.eisfeldj.augendiagnose project, as referred to on StackOverflow
  https://github.com/jeisfeld/Augendiagnose/blob/master/AugendiagnoseIdea/augendiagnoseLib/src/main/java/de/jeisfeld/augendiagnoselib/components/VerticalSeekBar.java
*/

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

/**
 * Implementation of an easy vertical SeekBar, based on the normal SeekBar.
 */
@SuppressLint("AppCompatCustomView")
public class VerticalSeekBar extends SeekBar {
    // The angle by which the SeekBar view should be rotated.
    private static final int ROTATION_ANGLE = -90;

    private SharedPreferences prefs;
    protected int prefDisplaySpeedUnits = 100;
    protected boolean prefTickMarksOnSliders = true;
    public boolean tickMarksChecked = false;

    Paint tickPaint;

    protected int steps;
    protected int height;
    protected int width;
    protected int paddingLeft;
    protected int paddingRight;
    protected float gridLeft;
    protected float gridBottom;
//            protected float gridTop;
//            protected float gridRight;
    protected float gridMiddle;
    protected float tickSpacing;
    protected float sizeIncrease;
    protected float d;
    protected float l;
    protected float r;
    protected float j;


    // A change listener registrating start and stop of tracking. Need an own listener because the listener in SeekBar
    // is private.
    private OnSeekBarChangeListener mOnSeekBarChangeListener;

    private static final int TICK_TYPE_0_100 = 0;
    private static final int TICK_TYPE_0_100_0 = 1;
    int tickMarkType = 0;

    public boolean touchFromUser = false;

    public VerticalSeekBar(final Context context) {
        super(context);
    }

    public VerticalSeekBar(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        tickMarksChecked = false;

        tickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tickPaint.setColor(context.getResources().getColor(R.color.seekBarTickColor));
    }

    public VerticalSeekBar(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        tickMarksChecked = false;

        tickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tickPaint.setColor(context.getResources().getColor(R.color.seekBarTickColor));
    }


    @Override
    protected final void onSizeChanged(final int width, final int height, final int oldWidth, final int oldHeight) {
        super.onSizeChanged(height, width, oldHeight, oldWidth);
    }

    @Override
    protected final synchronized void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec);
        setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
    }

    public void setTickType(int requestedTickMarkType) {
        tickMarkType = requestedTickMarkType;
    }

    @Override
    protected final void onDraw(final Canvas c) {
        c.rotate(ROTATION_ANGLE);
        c.translate(-getHeight(), 0);

        if (!tickMarksChecked) {
            tickMarksChecked = true;
            prefTickMarksOnSliders = prefs.getBoolean("prefTickMarksOnSliders", getResources().getBoolean(R.bool.prefTickMarksOnSlidersDefaultValue));
            prefDisplaySpeedUnits = preferences.getIntPrefValue(prefs, "DisplaySpeedUnits", getResources().getString(R.string.prefDisplaySpeedUnitsDefaultValue));

            steps = prefDisplaySpeedUnits;
            if (steps >= 100) {
                steps = steps / 3;
            } else {
                if (steps < 28) {
                    steps = steps * 3;
                }
            }
        }
        if (prefTickMarksOnSliders) {
            height = getHeight();
            width = getWidth();
            paddingLeft = getPaddingLeft();
            paddingRight = getPaddingRight();
            gridLeft = 30;
//            gridTop = paddingRight;
//            gridRight = width - 30;
            gridMiddle = width / 2;

            switch (tickMarkType) {
                default:
                case TICK_TYPE_0_100:
                    gridBottom = height - paddingLeft;
                    tickSpacing = (paddingRight - gridBottom) / (steps - 1);
                    sizeIncrease = (gridMiddle - gridLeft - 30) / (steps * steps);

                    for (int i = -1; i < steps; i++) {
                        j = (steps - i);
                        d = gridBottom + i * tickSpacing;
                        l = gridMiddle - 10 - sizeIncrease * j * j;
                        r = gridMiddle + 10 + sizeIncrease * j * j;
                        c.drawLine(d, l, d, r, tickPaint);
                    }
                    break;
                case TICK_TYPE_0_100_0:
                    int tempSteps = steps/2;
                    gridBottom = height/2 - paddingLeft;
                    tickSpacing = (paddingRight - gridBottom) / (tempSteps - 1);
                    sizeIncrease = (gridMiddle - gridLeft - 30) / (tempSteps * tempSteps);

                    for (int i = -1; i < tempSteps; i++) {
                        j = (tempSteps - i);
                        d = gridBottom + (height/2) + (i * tickSpacing);
                        l = gridMiddle - 10 - (sizeIncrease) * j * j;
                        r = gridMiddle + 10 + (sizeIncrease) * j * j;
                        c.drawLine(d, l, d, r, tickPaint);
                    }

                    for (int i = -1; i < tempSteps; i++) {
                        j = (tempSteps - i);
                        d = gridBottom + ((tempSteps - i - 1) * tickSpacing);
                        l = gridMiddle - 10 - (sizeIncrease) * j * j;
                        r = gridMiddle + 10 + (sizeIncrease) * j * j;
                        c.drawLine(d, l, d, r, tickPaint);
                    }

                    break;

            }
        }

        super.onDraw(c);
    }

    @Override
    public final void setOnSeekBarChangeListener(final OnSeekBarChangeListener l) {
        mOnSeekBarChangeListener = l;
        super.setOnSeekBarChangeListener(l);
    }

    @Override
    public final boolean onTouchEvent(final MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }

        int progress;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                progress = getMax() - (int) (getMax() * event.getY() / getHeight());
                if (progress<0) {progress = 0;}
                if (progress>getMax()) {progress = getMax();}
                setSeekBarProgress(progress,true);
                mOnSeekBarChangeListener.onStartTrackingTouch(this);
                break;

            case MotionEvent.ACTION_MOVE:
                progress = getMax() - (int) (getMax() * event.getY() / getHeight());
                if (progress<0) {progress = 0;}
                if (progress>getMax()) {progress = getMax();}
                setSeekBarProgress(progress,true);
                break;

            case MotionEvent.ACTION_UP:
                progress = getMax() - (int) (getMax() * event.getY() / getHeight());
                if (progress<0) {progress = 0;}
                if (progress>getMax()) {progress = getMax();}
                setSeekBarProgress(progress,true);
                mOnSeekBarChangeListener.onStopTrackingTouch(this);
                break;

            case MotionEvent.ACTION_CANCEL:
                mOnSeekBarChangeListener.onStopTrackingTouch(this);
                break;

            default:
                break;
        }

        return true;
    }

    @Override
    public final void setProgress(final int progress) {
        setSeekBarProgress(progress, false);
    }

    private void setSeekBarProgress(int progress, final boolean fromUser) {
        touchFromUser = fromUser;

        if (progress != getProgress()) {
            super.setProgress(progress);
            if (mOnSeekBarChangeListener != null) {
                mOnSeekBarChangeListener.onProgressChanged(this, progress, fromUser);
            }
        }

        onSizeChanged(getWidth(), getHeight(), 0, 0);
    }
}