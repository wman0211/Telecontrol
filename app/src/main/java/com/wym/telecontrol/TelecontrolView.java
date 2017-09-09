package com.wym.telecontrol;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;


/**
 * 模仿遥控器控件
 * Created by wym on 2017/9/8.
 */

public class TelecontrolView extends ViewGroup {

    private boolean isPress = false;
    private byte pressDirection;
    private final static int CENTER_RADIUS = 100;
    private byte lastStatus = 'n';
    private byte lastPressDirection;

    public TelecontrolView(Context context) {
        super(context);
        init(context);
    }

    public TelecontrolView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TelecontrolView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setWillNotDraw(false);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String current;
                switch ((byte)view.getTag()) {
                    case 'l':
                        current = "left";
                        break;
                    case 't':
                        current = "top";
                        break;
                    case 'r':
                        current = "right";
                        break;
                    case 'b':
                        current = "bottom";
                        break;
                    case 'c':
                        current = "center";
                        break;
                    default:
                        current = "none";
                        break;

                }
                log("onclick: " + current);
            }
        });

    }

    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {

    }

    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        int width = getWidth();
        int height = getHeight();

        RectF rectRound = new RectF(0, 0, width, height);

//        paint.setColor(Color.BLUE);
//        canvas.drawOval(rectRound, paint);

        // left
        paint.setColor((isPress && pressDirection == 'l') ? Color.BLACK : Color.RED);
        canvas.drawArc(rectRound, -225, 90, true, paint);
        // top
        paint.setColor((isPress && pressDirection == 't') ? Color.BLACK : Color.BLUE);
        canvas.drawArc(rectRound, -135, 90, true, paint);
        // right
        paint.setColor((isPress && pressDirection == 'r') ? Color.BLACK : Color.RED);
        canvas.drawArc(rectRound, -45, 90, true, paint);
        // bottom
        paint.setColor((isPress && pressDirection == 'b') ? Color.BLACK : Color.BLUE);
        canvas.drawArc(rectRound, 45, 90, true, paint);
        // center
        paint.setColor((isPress && pressDirection == 'c') ? Color.BLACK : Color.YELLOW);
        canvas.drawCircle(width / 2, height / 2, CENTER_RADIUS, paint);


    }

    private void log(String str) {
        Log.e("control", str);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean isMoveDraw = lastStatus != 'm';
        judgeDirection(event);
        boolean isMoveChange = pressDirection != lastPressDirection;
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                lastStatus = 'u';
                isPress = false;
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                lastStatus = 'm';
//                手指移动触发
//                if (isMoveDraw)
                if (isMoveChange) {
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_DOWN:
                lastStatus = 'd';
                isPress = true;
                invalidate();
                break;
        }
        lastPressDirection = pressDirection;
        setTag(pressDirection);
        return super.onTouchEvent(event);
    }



    private void judgeDirection(MotionEvent event) {
        float[] centerPoint = new float[2];
        centerPoint[0] = getWidth() / 2;
        centerPoint[1] = getHeight() / 2;

        if (judgeCenter(event, centerPoint)) {
            pressDirection = 'c';
            return;
        }

        int rotation = getRotationBetweenLines(centerPoint[0], centerPoint[1], event.getX(), event.getY());
//      log("c1:" + centerPoint[0] + " c2:" + centerPoint[1] + " x1:" + event.getX() + " x2:" + event.getY() + "---角度" + rotation);
        if (rotation > 135 && rotation < 225) {
            pressDirection = 'b';
        } else if (rotation > 225 && rotation < 315) {
            pressDirection = 'l';
        } else if (rotation > 45 && rotation < 45 + 90) {
            pressDirection = 'r';
        } else if (rotation == 45 || rotation == 45 + 90 || rotation == 45 + 90 + 90 || rotation == 45 + 90 + 90 + 90) {
            pressDirection = 'n';
        } else {
            pressDirection = 't';
        }
    }

    private boolean judgeCenter(MotionEvent event, float[] centerPoint) {
        int distance = getDistance(centerPoint[0], centerPoint[1], event.getX(), event.getY());
//        log("距离: " + distance);
        return distance < CENTER_RADIUS;

    }

    /**
     * from: http://www.jianshu.com/p/33342476b5ae
     * @return rotation
     */
    public int getRotationBetweenLines(float centerX, float centerY, float xInView, float yInView) {
        double rotation = 0;

        double k1 = (double) (centerY - centerY) / (centerX * 2 - centerX);
        double k2 = (double) (yInView - centerY) / (xInView - centerX);
        double tmpDegree = Math.atan((Math.abs(k1 - k2)) / (1 + k1 * k2)) / Math.PI * 180;

        if (xInView > centerX && yInView < centerY) {  //第一象限
            rotation = 90 - tmpDegree;
        } else if (xInView > centerX && yInView > centerY) //第二象限
        {
            rotation = 90 + tmpDegree;
        } else if (xInView < centerX && yInView > centerY) { //第三象限
            rotation = 270 - tmpDegree;
        } else if (xInView < centerX && yInView < centerY) { //第四象限
            rotation = 270 + tmpDegree;
        } else if (xInView == centerX && yInView < centerY) {
            rotation = 0;
        } else if (xInView == centerX && yInView > centerY) {
            rotation = 180;
        }

        return (int) rotation;
    }

    public int getDistance(float centerX, float centerY, float xInView, float yInView) {
        return Math.abs((((int)xInView - (int)centerX) ^ 2) - (((int)yInView - (int)centerY) ^ 2));
    }


}















