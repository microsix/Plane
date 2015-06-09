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
import android.view.MotionEvent;
import android.view.View;


public class GameView extends View {

    private int startX = 0;
    private int startY = 0;
    private int GRID_WIDTH = GlobalData.getWidth()/12;
    private int GRID_MOD = GlobalData.getWidth()%12;
    private int GRID_NUM = 13;
    private Paint paint = null;

    private int[][] chess = new int[GRID_NUM][GRID_NUM];
    private int CHESS_BLACK = 1;//表示棋子的颜色，1代表黑色，2代表白色，0达标没有棋子
    private int CHESS_WHITE = 2;
    private int chess_flag = 0;//用于记录上一次下的棋子的颜色，1为黑色，2为白色，0是刚开始下棋,上一次没下棋子

    public GameView(Context context) {
        super(context);

        paint = new Paint();//实例化一个画笔
        paint.setAntiAlias(true);//设置画笔去锯齿，没有此语句，画的线或图片周围不圆滑

        // 为了不让棋盘的边界与屏幕的边界完全重合，需要让棋盘的边界离屏幕边界一定距离。
        if(GRID_MOD == 0){
            startX = GRID_WIDTH/2;
            startY = GRID_WIDTH/2;
            GRID_NUM--;
        }
        else{
            startX = GRID_MOD/2;
            startY = GRID_MOD/2;
        }
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
        for(int i=0;i<GRID_NUM;i++)
        {
            for(int j=0;j<GRID_NUM;j++)
            {
                if(chess[i][j] == CHESS_BLACK)
                {
                    paint.setColor(Color.BLACK);//黑色画笔，画黑棋
                    canvas.drawCircle(startX+i*GRID_WIDTH,startY+j*GRID_WIDTH ,GRID_WIDTH/2-3, paint);
                }
                if(chess[i][j] == CHESS_WHITE)
                {
                    paint.setColor(Color.WHITE);//白色画笔，画白棋
                    canvas.drawCircle(startX+i*GRID_WIDTH,startY+j*GRID_WIDTH ,GRID_WIDTH/2-3, paint);
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
            int index_x = Math.round((touchX-startX)/GRID_WIDTH);
            int index_y = Math.round((touchY-startY)/GRID_WIDTH);

            if(chess_flag == 0)
            {//此句表示在最开始下棋的时候每次都是黑棋先下
                chess[index_x][index_y] = CHESS_BLACK;
                chess_flag = CHESS_BLACK;

            }else if( chess_flag == CHESS_BLACK && chess[index_x][index_y] == 0)
            {
                chess[index_x][index_y] = CHESS_WHITE;
                chess_flag = CHESS_WHITE;
            }else if(chess_flag == CHESS_WHITE && chess[index_x][index_y] == 0)
            {
                chess[index_x][index_y] = CHESS_BLACK;
                chess_flag = CHESS_BLACK;
            }
        }

        invalidate();//点击完成后，通知重绘即再次执行onDraw方法
        return super.onTouchEvent(event);
    }

}





