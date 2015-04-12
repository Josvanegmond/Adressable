package mindwave.apps.joozey.mindwavedemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;

/**
 * Created by mint on 11-4-15.
 */
public class MindSurface extends SurfaceView
{
    private Paint paint;
    public ArrayList[] bufferedLinesArray;

    public MindSurface(Context context) {
        super(context);
        init();
    }

    public MindSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MindSurface(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init()
    {
        paint = new Paint();
        paint.setColor( Color.RED);
        paint.setStrokeWidth(4);

        this.setBackgroundColor(Color.parseColor("#ff9999"));

        bufferedLinesArray = new ArrayList[8];

        for( int i = 0; i < bufferedLinesArray.length; i++ ) {
            bufferedLinesArray[i] = new ArrayList<Float>();
        }
    }

    @Override
    public void draw( Canvas canvas )
    {
        super.draw(canvas);

        String[] labels = {
                "delta",
                "highalpha",
                "lowalpha",
                "highbeta",
                "lowbeta",
                "midgamma",
                "lowgamma",
                "theta"
        };

        int[] colorTable = {
                Color.RED,
                Color.BLUE,
                Color.GREEN,
                Color.CYAN,
                Color.YELLOW,
                Color.GRAY,
                Color.MAGENTA,
                Color.WHITE
        };

        for( int i = 0; i < colorTable.length; i++ )
        {
            paint.setColor( colorTable[i] );
            canvas.drawText( labels[i], 20, getHeight() - 250 + i*20, paint );
        }

        paint.setColor(Color.BLACK);
        canvas.drawLine( 0f, 50 + (float)getHeight() * 0.5f, (float)getWidth(), 50 + (float)getHeight() * 0.5f, paint );

        for( int n = 0; n < bufferedLinesArray.length; n++ )
        {
            paint.setColor( colorTable[n] );
            ArrayList<Float> bufferedLines = bufferedLinesArray[n];
            for (int i = 0; i < bufferedLines.size() - 1; i++)
            {
                canvas.drawLine(i * (getWidth() / bufferedLines.size()), getHeight() * 0.5f + bufferedLines.get(i), (i + 1) * (getWidth() / bufferedLines.size()), getHeight() * 0.5f + bufferedLines.get(i + 1), paint);
            }
        }
    }

    public void repaint()
    {
        Canvas canvas = null;
        SurfaceHolder holder = this.getHolder();

        try {
            canvas = holder.lockCanvas();

            synchronized(holder) {
                postInvalidate();
            }
        }

        finally {
            if(canvas != null) {
                holder.unlockCanvasAndPost(canvas);
            }
        }
    }

    public void pushGraphData(int graphNumber, int msg) {
        pushGraphData( graphNumber, (float) msg );
    }

    public void pushGraphData(int graphNumber, double msg) {
        pushGraphData( graphNumber, (float) msg );
    }

    public void pushGraphData(int graphNumber, float msg) {

        float value = msg;
        bufferedLinesArray[graphNumber].add(value);
        if( bufferedLinesArray[graphNumber].size() > 50 ) { bufferedLinesArray[graphNumber].remove(0);}
    }
}
