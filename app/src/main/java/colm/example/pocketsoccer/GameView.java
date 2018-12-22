package colm.example.pocketsoccer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class GameView extends View {

    class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
    }
    GestureDetector detector = new GestureDetector(GameView.this.getContext(), new GestureListener());

    private Paint goalPaint;
    private Paint playerPaint[];
    private Paint ballPaint;

    public boolean initialized;

    public int leftSpacing;
    public int effectiveWidth;
    public int topSpacing;
    public int effectiveHeight;

    public float packPosX[];
    public float packPosY[];
    public int packFlag[];
    public float ballPosX;
    public float ballPosY;
    public float goalHeight;
    public float goalWidth;
    public float packRadius;
    public float ballRadius;

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);

        goalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        goalPaint.setStyle(Paint.Style.STROKE);
        goalPaint.setStrokeWidth(10);
        goalPaint.setColor(0xff000000);

        playerPaint = new Paint[2];
        playerPaint[0] = new Paint(Paint.ANTI_ALIAS_FLAG);
        playerPaint[0].setStyle(Paint.Style.FILL);
        playerPaint[0].setStrokeWidth(10);
        playerPaint[0].setColor(0xffff0000);
        playerPaint[1] = new Paint(Paint.ANTI_ALIAS_FLAG);
        playerPaint[1].setStyle(Paint.Style.FILL);
        playerPaint[1].setStrokeWidth(10);
        playerPaint[1].setColor(0xff00ff00);

        ballPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ballPaint.setStyle(Paint.Style.FILL);
        ballPaint.setStrokeWidth(10);
        ballPaint.setColor(0xff0000ff);

        initialized = false;

        packPosX = new float[6];
        packPosY = new float[6];
        packFlag = new int[6];
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = detector.onTouchEvent(event);
        if (!result) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                Toast.makeText(getContext(), "view clicked", Toast.LENGTH_SHORT).show();
                performClick();
                result = true;
            }
        }
        return result;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!initialized) {
            return;
        }

        canvas.drawRect(leftSpacing, topSpacing, getWidth() - leftSpacing, getHeight() - topSpacing, goalPaint);

        int goalTopY = (int)(getHeight() - goalHeight) / 2;
        int goalBottomY = getHeight() - goalTopY;
        canvas.drawLine(leftSpacing, goalTopY, leftSpacing + goalWidth, goalTopY, goalPaint);
        canvas.drawLine(getWidth() - leftSpacing, goalTopY, getWidth() - leftSpacing - goalWidth, goalTopY, goalPaint);
        canvas.drawLine(leftSpacing, goalBottomY, leftSpacing + goalWidth, goalBottomY, goalPaint);
        canvas.drawLine(getWidth() - leftSpacing, goalBottomY, getWidth() - leftSpacing - goalWidth, goalBottomY, goalPaint);

        for (int i = 0; i < 6; i++) {
            canvas.drawCircle(packPosX[i], packPosY[i], packRadius, playerPaint[i / 3]);
        }
        canvas.drawCircle(ballPosX, ballPosY, ballRadius, ballPaint);
    }
}
