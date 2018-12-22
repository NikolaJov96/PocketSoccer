package colm.example.pocketsoccer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import colm.example.pocketsoccer.game_model.GameAssetManager;

public class GameView extends View {

    class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
    }
    GestureDetector detector = new GestureDetector(GameView.this.getContext(), new GestureListener());

    private static final int PACK_BORDER_WIDTH = 5;

    private GameAssetManager gam;
    private Rect drawingRect;

    private Paint goalPaint;
    private Paint packPaint;

    public boolean initialized;

    public int leftSpacing;
    public int effectiveWidth;
    public int topSpacing;
    public int effectiveHeight;

    public int packPosX[];
    public int packPosY[];
    public int packFlag[];
    public int ballPosX;
    public int ballPosY;
    public int goalHeight;
    public int goalWidth;
    public int packRadius;
    public int ballRadius;

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);

        gam = GameAssetManager.getGameAssetManager();
        drawingRect = new Rect();

        goalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        goalPaint.setStyle(Paint.Style.STROKE);
        goalPaint.setStrokeWidth(10);
        goalPaint.setColor(0xff000000);

        packPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        packPaint.setStyle(Paint.Style.FILL);
        packPaint.setStrokeWidth(10);
        packPaint.setColor(0xff000000);

        initialized = false;

        packPosX = new int[6];
        packPosY = new int[6];
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

        int goalTopY = (getHeight() - goalHeight) / 2;
        int goalBottomY = getHeight() - goalTopY;
        canvas.drawLine(leftSpacing, goalTopY, leftSpacing + goalWidth, goalTopY, goalPaint);
        canvas.drawLine(getWidth() - leftSpacing, goalTopY, getWidth() - leftSpacing - goalWidth, goalTopY, goalPaint);
        canvas.drawLine(leftSpacing, goalBottomY, leftSpacing + goalWidth, goalBottomY, goalPaint);
        canvas.drawLine(getWidth() - leftSpacing, goalBottomY, getWidth() - leftSpacing - goalWidth, goalBottomY, goalPaint);

        for (int i = 0; i < 6; i++) {
            drawingRect.left = packPosX[i] - packRadius + PACK_BORDER_WIDTH;
            drawingRect.right = packPosX[i] + packRadius - PACK_BORDER_WIDTH;
            drawingRect.top = packPosY[i] - packRadius + PACK_BORDER_WIDTH;
            drawingRect.bottom = packPosY[i] + packRadius - PACK_BORDER_WIDTH;
            canvas.drawCircle(packPosX[i], packPosY[i], packRadius, packPaint);
            canvas.drawBitmap(gam.getFlag(packFlag[i]), null, drawingRect, packPaint);
        }

        drawingRect.left = ballPosX - ballRadius + PACK_BORDER_WIDTH;
        drawingRect.right = ballPosX + ballRadius - PACK_BORDER_WIDTH;
        drawingRect.top = ballPosY - ballRadius + PACK_BORDER_WIDTH;
        drawingRect.bottom = ballPosY + ballRadius - PACK_BORDER_WIDTH;
        canvas.drawCircle(ballPosX, ballPosY, ballRadius, packPaint);
        canvas.drawBitmap(gam.getBall(), null, drawingRect, packPaint);
    }
}
