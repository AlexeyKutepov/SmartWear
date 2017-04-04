package wear.smart.ru.smartwear.component;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import wear.smart.ru.smartwear.R;

/**
 * Отображение температуры одежды
 */

public class TempView extends View {

    private Paint mPaint;
    public Bitmap bmpRound;
    Context ctx;

    public TempView(Context context) {
        super(context);
        ctx = context;
        bmpRound = BitmapFactory.decodeResource(getResources(), R.drawable.in_temp);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(16);
        mPaint.setColor(0xFFFFFFFF);
        mPaint.setStyle(Paint.Style.FILL);
    }

    public TempView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        ctx = context;
        bmpRound = BitmapFactory.decodeResource(getResources(), R.drawable.in_temp);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(16);
        mPaint.setColor(0xFFFFFFFF);
        mPaint.setStyle(Paint.Style.FILL);
    }

    public TempView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ctx = context;
        bmpRound = BitmapFactory.decodeResource(getResources(), R.drawable.in_temp);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(16);
        mPaint.setColor(0xFFFFFFFF);
        mPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(bmpRound, 0, 0,null);
    }
}
