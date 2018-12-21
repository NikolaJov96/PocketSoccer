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

    private Paint rectPaint;

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);

        rectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rectPaint.setStyle(Paint.Style.FILL);
        rectPaint.setColor(0xffff0000);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = detector.onTouchEvent(event);
        if (!result) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                Toast.makeText(getContext(), "view clicked", Toast.LENGTH_SHORT).show();
                result = true;
            }
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int sideLen = Math.min(getWidth(), getHeight()) / 2;

        canvas.drawRect(sideLen / 2, sideLen / 2, sideLen * 3 / 2, sideLen * 3 / 2, rectPaint);
    }
}
