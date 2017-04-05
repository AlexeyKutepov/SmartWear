package wear.smart.ru.smartwear.component;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import wear.smart.ru.smartwear.R;

/**
 * Отображение температуры одежды
 */

public class TempView extends View {

    private TextPaint textPaint;
    public Bitmap bmpRound;
    private String text = "0.0";

    public TempView(Context context) {
        super(context);
        bmpRound = BitmapFactory.decodeResource(getResources(), R.drawable.in_temp);
        createTextPaint();
    }

    public TempView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        bmpRound = BitmapFactory.decodeResource(getResources(), R.drawable.in_temp);
        createTextPaint();
    }

    public TempView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        bmpRound = BitmapFactory.decodeResource(getResources(), R.drawable.in_temp);
        createTextPaint();
    }

    /**
     * Создаём TextPaint
     */
    private void createTextPaint() {
        textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(24 * getResources().getDisplayMetrics().density);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(getResources().getColor(R.color.colorBlue500));
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(bmpRound, 0, 0,null);
        canvas.drawText(text, 96, 110, textPaint);

    }
}
