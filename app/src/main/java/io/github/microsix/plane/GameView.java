package io.github.microsix.plane;

/**
 * Created by cai on 2015/6/8.
 */
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


public class GameView extends View {

    private final String TAG = "GameView";
    private int startX = 0;
    private int startY = 0;
    private int GRID_WIDTH = GlobalData.getWidth()/11;
    private int GRID_MOD = GlobalData.getWidth()%11;
    private int GRID_NUM = 11;
    private Paint paint = null;

    private int[][] chess = new int[GRID_NUM][GRID_NUM];
    private int CHESS_BLACK = 1;//表示棋子的颜色，1代表黑色，2代表白色，0达标没有棋子
    private int CHESS_WHITE = 2;
    private int CHESS_GRAY = 3;
    private int CHESS_RED = 4;
    private int CHESS_WHITE_SMALL = 5;
    private int chess_flag = 0;//用于记录上一次下的棋子的颜色，1为黑色，2为白色，0是刚开始下棋,上一次没下棋子

    private int whichPlane = 1;
    private boolean lock = false;
    private int []headX = new int[3];
    private int []headY = new int[3] ;

    public GameView(Context context) {
        super(context);

        paint = new Paint();//实例化一个画笔
        paint.setAntiAlias(true);//设置画笔去锯齿，没有此语句，画的线或图片周围不圆滑

        // 为了不让棋盘的边界与屏幕的边界完全重合，需要让棋盘的边界离屏幕边界一定距离。
        Log.d(TAG, "GlobalData.getWidth() = " + GlobalData.getWidth() + ", GlobalData.getWidth() = " + GlobalData.getWidth());

        startX = (GRID_MOD+GRID_WIDTH)/2;
        startY = (GRID_MOD+GRID_WIDTH)/2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(0xFF8B4726);//背景色
        paint.setColor(Color.BLACK);//画笔颜色
        for(int i=0;i<GRID_NUM;i++)
        {
            canvas.drawLine(startX, startY+i*GRID_WIDTH,startX+(GRID_NUM-1)*GRID_WIDTH , startY+i*GRID_WIDTH, paint);
            canvas.drawLine(startX+i*GRID_WIDTH, startY,startX+i*GRID_WIDTH , startY+(GRID_NUM-1)*GRID_WIDTH, paint);
        }
        //使用maskFilter实现棋子的滤镜效果，使之看起来更有立体感。
        float[] dire = new float[]{1,1,1};  //光线方向
        float light = 0.5f;   //光线强度
        float spe = 6;
        float blur = 3.5f;
        EmbossMaskFilter emboss=new EmbossMaskFilter(dire,light,spe,blur);
        paint.setMaskFilter(emboss);

        //绘制棋子
        for(int i=1;i<GRID_NUM;i++)
        {
            for(int j=1;j<GRID_NUM;j++)
            {
                if(chess[i][j] == CHESS_BLACK)
                {
                    paint.setColor(Color.BLACK);//黑色画笔，画黑棋
                    canvas.drawCircle(startX+i*GRID_WIDTH-GRID_WIDTH/2,startY+j*GRID_WIDTH-GRID_WIDTH/2,GRID_WIDTH/2-3, paint);
                }
                if(chess[i][j] == CHESS_WHITE)
                {
                    paint.setColor(Color.WHITE);//白色画笔，画白棋
                    canvas.drawCircle(startX+i*GRID_WIDTH-GRID_WIDTH/2,startY+j*GRID_WIDTH-GRID_WIDTH/2,GRID_WIDTH/2-3, paint);
                }
                if(chess[i][j] == CHESS_GRAY)
                {
                    paint.setColor(Color.GRAY);//黑色画笔，画黑棋
                    canvas.drawCircle(startX+i*GRID_WIDTH-GRID_WIDTH/2,startY+j*GRID_WIDTH-GRID_WIDTH/2,GRID_WIDTH/2-3, paint);
                }
                if(chess[i][j] == CHESS_RED)
                {
                    paint.setColor(Color.RED);//白色画笔，画白棋
                    //canvas.drawRect();
                    canvas.drawCircle(startX+i*GRID_WIDTH-GRID_WIDTH/2,startY+j*GRID_WIDTH-GRID_WIDTH/2,GRID_WIDTH/4, paint);
                }
                if(chess[i][j] == CHESS_WHITE_SMALL)
                {
                    paint.setColor(Color.WHITE);//白色画笔，画白棋
                    canvas.drawCircle(startX+i*GRID_WIDTH-GRID_WIDTH/2,startY+j*GRID_WIDTH-GRID_WIDTH/2,GRID_WIDTH/4, paint);
                }
            }
        }
    }

    //重写View的监听触摸事件的方法
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float touchX = event.getX();
        float touchY = event.getY();

        if(touchX < startX || touchX>startX+(GRID_NUM-1)*GRID_WIDTH || touchY < startY || touchY>startY+(GRID_NUM-1)*GRID_WIDTH)
        {//点击到棋盘以外的位置

        }
        else
        {
            //根据点击的位置，从而获知在棋盘上的哪个位置，即是数组的脚标
            int index_x = (int)Math.ceil((touchX-startX)/GRID_WIDTH);
            int index_y = (int)Math.ceil((touchY-startY)/GRID_WIDTH);

            switch (whichPlane) {
                case 1:
                    if (chess[index_x][index_y] == 0 && !lock ) {
                        Log.d(TAG, "0");

                        chess[index_x][index_y] = CHESS_WHITE;

                        if (assign(index_x+1, index_y, CHESS_RED)) {
                            checkPlane(index_x, index_y, 1, 0);
                        }
                        if (assign(index_x-1, index_y, CHESS_RED)) {
                            checkPlane(index_x, index_y, -1, 0);
                        }
                        if (assign(index_x, index_y+1, CHESS_RED)) {
                            checkPlane(index_x, index_y, 0, 1);
                        }
                        if (assign(index_x, index_y-1, CHESS_RED)) {
                            checkPlane(index_x, index_y, 0, -1);
                        }

                        headX[0] = index_x;
                        headY[0] = index_y;

                        lock = true;

                    } else if (chess[index_x][index_y] == CHESS_WHITE) {
                        Log.d(TAG, "CHESS_WHITE");
                        chess[index_x][index_y] = 0;
                        if(index_x + 1 <= 10) {
                            chess[index_x+1][index_y] = 0;
                        }
                        if(index_x - 1 >= 1) {
                            chess[index_x-1][index_y] = 0;
                        }
                        if(index_y + 1 <= 10) {
                            chess[index_x][index_y+1] = 0;
                        }
                        if(index_y - 1 >= 1) {
                            chess[index_x][index_y-1] = 0;
                        }
                        lock = false;
                    } else if (chess[index_x][index_y] == CHESS_RED) {
                        Log.d(TAG, "CHESS_RED");
                        createPlane(headX[0], headY[0], index_x - headX[0], index_y - headY[0], CHESS_WHITE);  //waste much time
                    }
                    break;
                case 2:

                    break;
                case 3:

                    break;
            }
        }

        invalidate();//点击完成后，通知重绘即再次执行onDraw方法
        return super.onTouchEvent(event);
    }

    private boolean assign(int index_x, int index_y, int chess_value) {
        if (index_x >= 1 && index_x <= 10 && index_y >=1 && index_y <=10) {
            chess[index_x][index_y] = chess_value;
            return true;
        } else {
            if (chess_value == 0 ) {   //for reset chess[][]
                return true;
            }
            return  false;
        }
    }

    private boolean check(int index_x, int index_y) {
        if (index_x >= 1 && index_x <= 10 && index_y >=1 && index_y <=10) {
            return true;
        } else {
            return  false;
        }
    }

    private void createPlane(int index_x, int index_y, int x, int y, int chess_value) {
        Log.d(TAG, "createPlane");
        if(y == 0) {
            Log.d(TAG, "y=0");
            if (assign(index_x + x, index_y - 1, chess_value)
                    && assign(index_x + x, index_y - 2, chess_value)
                    && assign(index_x + x, index_y + 1, chess_value)
                    && assign(index_x + x, index_y + 2, chess_value)
                    && assign(index_x + 2*x, index_y, chess_value)
                    && assign(index_x + 3*x, index_y, chess_value)
                    && assign(index_x + 3*x, index_y - 1, chess_value)
                    && assign(index_x + 3*x, index_y + 1, chess_value)){
                assign(index_x + 1, index_y, 0);
                assign(index_x - 1, index_y, 0);
                assign(index_x, index_y + 1, 0);
                assign(index_x, index_y - 1, 0);
                assign(index_x + x, index_y + y, chess_value);
            } else {
                Log.d(TAG, "reset");
                createPlane(index_x, index_y, x, y, chess_value);
            }
        } else {   //x = 0
            Log.d(TAG, "x=0");
            if (assign(index_x - 1, index_y + y, chess_value)
                    && assign(index_x - 2, index_y + y, chess_value)
                    && assign(index_x + 1, index_y + y, chess_value)
                    && assign(index_x + 2, index_y + y, chess_value)
                    && assign(index_x, index_y + 2*y, chess_value)
                    && assign(index_x, index_y + 3*y, chess_value)
                    && assign(index_x - 1, index_y + 3*y, chess_value)
                    && assign(index_x + 1, index_y + 3*y, chess_value)){
                assign(index_x + 1, index_y, 0);
                assign(index_x - 1, index_y, 0);
                assign(index_x, index_y + 1, 0);
                assign(index_x, index_y - 1, 0);
                assign(index_x + x, index_y + y, chess_value);
            } else {
                Log.d(TAG, "reset");
                createPlane(index_x, index_y, x, y, 0);
            }
        }
    }

    private void checkPlane(int index_x, int index_y, int x, int y) {
        if(y == 0) {
            if (check(index_x + x, index_y - 1)
                    && check(index_x + x, index_y - 2)
                    && check(index_x + x, index_y + 1)
                    && check(index_x + x, index_y + 2)
                    && check(index_x + 2*x, index_y)
                    && check(index_x + 3*x, index_y)
                    && check(index_x + 3*x, index_y - 1)
                    && check(index_x + 3*x, index_y + 1))
            {
                ////nothing to do
            } else {
                assign(index_x + x , index_y + y, 0);
            }
        } else {   //x = 0
            if (check(index_x - 1, index_y + y)
                    && check(index_x - 2, index_y + y)
                    && check(index_x + 1, index_y + y)
                    && check(index_x + 2, index_y + y)
                    && check(index_x, index_y + 2*y)
                    && check(index_x, index_y + 3*y)
                    && check(index_x - 1, index_y + 3*y)
                    && check(index_x + 1, index_y - 3*y)){
                ////nothing to do
            } else {
                assign(index_x + x, index_y + y, 0);
            }
        }
    }

}





