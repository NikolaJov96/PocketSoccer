package colm.example.pocketsoccer;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import colm.example.pocketsoccer.game_model.AppPreferences;
import colm.example.pocketsoccer.game_model.Game;
import colm.example.pocketsoccer.game_model.GameAssetManager;

public class GameView extends View {

    private static final int PACK_BORDER_WIDTH = 5;

    private Game game;

    private GameAssetManager gam;
    private AppPreferences ap;
    private Rect drawingRect;

    private Paint goalPaint;
    private Paint packPaint;
    private Paint timePaint;

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

    public int timer;
    public int leftSocre;
    public int rightScore;

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);

        game = Game.getGame();

        gam = GameAssetManager.getGameAssetManager();
        ap = AppPreferences.getAppPreferences();
        drawingRect = new Rect();

        goalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        goalPaint.setStyle(Paint.Style.STROKE);
        goalPaint.setStrokeWidth(20);
        goalPaint.setColor(0xff000000);

        packPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        packPaint.setStyle(Paint.Style.FILL);
        packPaint.setStrokeWidth(10);
        packPaint.setColor(0xff000000);

        timePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        timePaint.setTextSize(100);
        timePaint.setFakeBoldText(true);
        timePaint.setColor(0xff000000);

        initialized = false;

        packPosX = new int[6];
        packPosY = new int[6];
        packFlag = new int[6];
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            game.startMove((int)event.getX(), (int)event.getY());
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            game.endMove((int)event.getX(), (int)event.getY());
            performClick();
        }
        return true;
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

        drawingRect.left = leftSpacing;
        drawingRect.right = getWidth() - leftSpacing;
        drawingRect.top = topSpacing;
        drawingRect.bottom = getHeight() - topSpacing;
        canvas.drawBitmap(gam.getField(ap.getFieldId()), null, drawingRect, packPaint);
        canvas.drawRect(drawingRect, goalPaint);

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

        String timeStr = (timer / 60) + " : " + (timer % 60);
        canvas.drawText(timeStr, getWidth() / 2.0f, getHeight() * 0.1f, timePaint);
        canvas.drawText(Integer.toString(leftSocre), getWidth() * 1.0f / 4.0f, getHeight() * 0.1f, timePaint);
        canvas.drawText(Integer.toString(rightScore), getWidth() * 3.0f / 4.0f, getHeight() * 0.1f, timePaint);
    }
}
