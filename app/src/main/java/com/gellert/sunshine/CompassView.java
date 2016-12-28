package com.gellert.sunshine;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;


public class CompassView extends View {
    private final static String TAG = "CompassView";

    private float direction = 0;
    private Paint p1;
    private Paint p2;

    private final static double conv = Math.PI / 180;

    public CompassView(Context context) {
        super(context);
        init();
    }

    public CompassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CompassView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        p1 = new Paint(Paint.ANTI_ALIAS_FLAG);
        p1.setARGB(255,38,168,244);

        p2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        p2.setARGB(255,2,136,209);
        p2.setStyle(Paint.Style.FILL);
        p2.setStrokeWidth(5);

        AccessibilityManager accessibilityManager =
                (AccessibilityManager) getContext().getSystemService(
                        Context.ACCESSIBILITY_SERVICE);
        if (accessibilityManager.isEnabled()) {
            sendAccessibilityEvent(
                    AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredW = 250;
        int desiredH = 250;

        int wSize = MeasureSpec.getSize(widthMeasureSpec);
        int wMode = MeasureSpec.getMode(widthMeasureSpec);
        int hSize = MeasureSpec.getSize(heightMeasureSpec);
        int hMode = MeasureSpec.getMode(heightMeasureSpec);

        int mWidth = wSize;
        int mHeight = hSize;

        if(wMode == MeasureSpec.EXACTLY){
            mWidth = wSize;
        } else if(wMode == MeasureSpec.AT_MOST){
            mWidth = Math.min(desiredW,wSize);
        } else{
            mWidth = desiredW;
        }

        if(hMode == MeasureSpec.EXACTLY){
            mHeight = hSize;
        } else if(hMode == MeasureSpec.AT_MOST){
            mHeight = Math.min(desiredH,hSize);
        } else{
            mHeight = desiredH;
        }

        setMeasuredDimension(mWidth,mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int w = canvas.getWidth();
        int h = canvas.getHeight();
        int m = Math.min(w/2,h/2);

        int r = m-15;
        double x,y;

        if(direction <= 90.0){
            x = r * Math.sin(direction * conv);
            y = -r * Math.cos(direction * conv);
            //Log.v(TAG,"Direction: " + direction + " case 1");
        } else if(direction <= 180.0){
            x = r * Math.cos((direction-90) * conv);
            y = r * Math.sin((direction-90) * conv);
            //Log.v(TAG,"Direction: " + direction + " case 2");
        } else if(direction <= 270.0){
            x = -r * Math.sin((direction-180) * conv);
            y = r * Math.cos((direction-180) * conv);
            //Log.v(TAG,"Direction: " + direction + " case 3");
        } else {
            x = -r * Math.cos((direction-270) * conv);
            y = -r * Math.sin((direction-270) * conv);
            //Log.v(TAG,"Direction: " + direction + " case 4");
        }

        x += w/2;
        y += h/2;

        canvas.drawCircle(w/2,h/2,m,p1);
        canvas.drawLine(w/2,h/2,
                (int)x,
                (int)y,
                p2);
    }

    public void setDirection(float direction) {
        this.direction = direction;
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        event.getText().add(direction + " degrees");
        return true;
    }
}
