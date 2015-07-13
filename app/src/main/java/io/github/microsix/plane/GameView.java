package io.github.microsix.plane;

/**
 * Created by cai on 2015/6/8.
 */
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class GameView extends View {

    private final String TAG = "GameView";
    private int startX = 0;
    private int startY = 0;
    private int GRID_WIDTH = GlobalData.getWidth()/(10+1);
    private int GRID_MOD = GlobalData.getWidth()%(10+1);
    private int GRID_NUM = 11;
    private Paint paint = null;

    private int[][] chess = new int[GRID_NUM][GRID_NUM];
    private int[][] gameData = new int[GRID_NUM][GRID_NUM];
    private final int CHESS_BLACK = 3;// point color
    private final int CHESS_WHITE = 1;
    private final int CHESS_GRAY = 2;
    private final int CHESS_RED = 4;
    private final int GAME_START = 5;

    private final int H_START = 0;
    private final int V_START = 8;
    private final int SIDE_START = 16;

    private int whichPlane = CHESS_WHITE;
    private boolean lock = false;
    private boolean building = true;
    private boolean finish = false;

    private int destroyNumber = 0;
    private int stepNumber = 0;

    private int []headX = new int[4];
    private int []headY = new int[4] ;

    private TextView tv_state;
    private Button button_clear;
    private Button button_exercise;
    private Button button_offline;
    private Button button_online;

    Context context;

    public GameView(Context context) {
        super(context);
        this.context = context;

        paint = new Paint();//实例化一个画笔
        paint.setAntiAlias(true);//设置画笔去锯齿，没有此语句，画的线或图片周围不圆滑

        Log.d(TAG, "GlobalData.getWidth() = " + GlobalData.getWidth() + ", GlobalData.getWidth() = " + GlobalData.getWidth());

        // offset avoid no distance between line and screen edge
        startX = (GRID_MOD+GRID_WIDTH)/2;
        startY = (GRID_MOD+GRID_WIDTH)/2;

        initActivityView();
    }

    private void initActivityView() {
        tv_state = (TextView)((PlaneMainActivity)context).findViewById(R.id.tv_state);
        button_clear = (Button)((PlaneMainActivity)context).findViewById(R.id.button_clear);
        button_exercise = (Button)((PlaneMainActivity)context).findViewById(R.id.button_exercise);
        button_offline = (Button)((PlaneMainActivity)context).findViewById(R.id.button_offline);
        button_online = (Button)((PlaneMainActivity)context).findViewById(R.id.button_online);

        button_clear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "button_clear");
                for(int i=1;i<GRID_NUM;i++) {
                    for(int j=1;j<GRID_NUM;j++) {
                        chess[i][j] = 0;
                    }
                }
                lock = false;
                building = true;
                button_offline.setEnabled(false);
                button_online.setEnabled(false);
                button_exercise.setEnabled(true);
                whichPlane = CHESS_WHITE;
                finish = false;
                destroyNumber = 0;
                stepNumber = 0;
                tv_state.setText("All cleared");
                invalidate();
            }
        });

        button_exercise.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "button_exercise");
                tv_state.setText("Sorry, it's still wait for building");
                randomGame();
            }
        });

        button_offline.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startOfflineGame();
            }
        });

        button_online.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_state.setText("Sorry, it's still wait for building");
            }
        });
    }

    private void startOfflineGame() {
        for(int i=1;i<GRID_NUM;i++) {
            for(int j=1;j<GRID_NUM;j++) {
                gameData[i][j] = chess[i][j];
                chess[i][j] = GAME_START;
            }
        }
        button_offline.setEnabled(false);
        button_online.setEnabled(false);
        button_exercise.setEnabled(false);
        button_clear.setEnabled(false);
        tv_state.setText("Enjoy the game");
        invalidate();
    }

    private void randomGame() {
        int randomX, randomY;

        while (building) {
            randomX = (int)(Math.random()*9) + 1;
            randomY = (int)(Math.random()*9) + 1;
            Log.d(TAG, randomX + " " + randomY);
            process(randomX, randomY, whichPlane);
        }

        startOfflineGame();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(0xFF8B4726);//background color
        paint.setColor(Color.BLACK);//line color
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

        //draw point
        for(int i=1;i<GRID_NUM;i++)
        {
            for(int j=1;j<GRID_NUM;j++)
            {
                if(chess[i][j] == CHESS_BLACK)
                {
                    paint.setColor(Color.BLACK);//draw black point
                    canvas.drawCircle(startX+i*GRID_WIDTH-GRID_WIDTH/2,startY+j*GRID_WIDTH-GRID_WIDTH/2,GRID_WIDTH/2-3, paint);
                }
                if(chess[i][j] == CHESS_WHITE)
                {
                    paint.setColor(Color.WHITE);//draw white point
                    canvas.drawCircle(startX+i*GRID_WIDTH-GRID_WIDTH/2,startY+j*GRID_WIDTH-GRID_WIDTH/2,GRID_WIDTH/2-3, paint);
                }
                if(chess[i][j] == CHESS_GRAY)
                {
                    paint.setColor(Color.GRAY);//draw gray point
                    canvas.drawCircle(startX+i*GRID_WIDTH-GRID_WIDTH/2,startY+j*GRID_WIDTH-GRID_WIDTH/2,GRID_WIDTH/2-3, paint);
                }
                if(chess[i][j] == CHESS_RED)
                {
                    paint.setColor(Color.RED);//draw red point
                    canvas.drawCircle(startX+i*GRID_WIDTH-GRID_WIDTH/2,startY+j*GRID_WIDTH-GRID_WIDTH/2,GRID_WIDTH/4, paint);
                }
                if(chess[i][j] == GAME_START)
                {
                    paint.setColor(Color.YELLOW);
                    canvas.drawRect(startX+(i-1)*GRID_WIDTH+0.1f*GRID_WIDTH, startY+(j-1)*GRID_WIDTH+0.1f*GRID_WIDTH, startX+i*GRID_WIDTH-0.1f*GRID_WIDTH, startY+j*GRID_WIDTH-0.1f*GRID_WIDTH, paint);
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        if(touchX < startX || touchX>startX+(GRID_NUM-1)*GRID_WIDTH
                || touchY < startY || touchY>startY+(GRID_NUM-1)*GRID_WIDTH){
            //Outside table, blank
        } else {
            int index_x = (int)Math.ceil((touchX-startX)/GRID_WIDTH);
            int index_y = (int)Math.ceil((touchY-startY)/GRID_WIDTH);

            process(index_x, index_y, whichPlane);
        }

        invalidate();//notify to call onDraw function
        return super.onTouchEvent(event);
    }

    private void process(int index_x, int index_y, int chess_color) {

        if (!finish && building) {
            if (chess[index_x][index_y] == 0) {
                Log.d(TAG, "0");

                if (lock) {
                    removeHead(headX[chess_color], headY[chess_color]);
                    lock = false;
                }

                if (!lock) {
                    chess[index_x][index_y] = chess_color;

                    int tempX, tempY;
                    for (int n = 3; n >= 0; n--) {
                        tempX = getPoint(SIDE_START + n, index_x, index_y, 0, 0).x;
                        tempY = getPoint(SIDE_START + n, index_x, index_y, 0, 0).y;
                        if (assign(tempX, tempY, CHESS_RED)) {
                            checkPlane(index_x, index_y, tempX - index_x, tempY - index_y);
                        }
                    }

                    headX[chess_color] = index_x;
                    headY[chess_color] = index_y;

                    lock = true;
                }

            } else if (chess[index_x][index_y] == chess_color) {
                Log.d(TAG, " CHESS COLOR = " + chess_color);

                removeHead(index_x, index_y);
                lock = false;

            } else if (chess[index_x][index_y] == CHESS_RED) {
                Log.d(TAG, "CHESS_RED");
                assignPlane(headX[chess_color], headY[chess_color],
                        index_x - headX[chess_color], index_y - headY[chess_color], chess_color);
                if (++whichPlane <= 3) {
                    lock = false;
                } else {
                    building = false;
                    tv_state.setText("You can start game now");
                    button_online.setEnabled(true);
                    button_offline.setEnabled(true);
                    button_exercise.setEnabled(false);
                }
            }
        } else if (!finish) {  //not building, start game
            if (chess[index_x][index_y] == GAME_START) {
                stepNumber++;
                if (gameData[index_x][index_y] != 0) {
                    chess[index_x][index_y] = CHESS_BLACK;
                } else {
                    chess[index_x][index_y] = 0;
                }
                for (int i = CHESS_WHITE; i <= CHESS_BLACK; i++) {
                    if (index_x == headX[i] && index_y == headY[i]) {
                        tv_state.setText( ++destroyNumber + " plane is destroy");
                    }
                }
                if (destroyNumber == 3) {
                    tv_state.setText("Congratulations! You win the game with " + stepNumber + "steps");
                    finish = true;
                    button_clear.setEnabled(true);
                }
            }
        }
    }

    private void removeHead(int index_x, int index_y) {
        chess[index_x][index_y] = 0;

        for (int n = 3; n >= 0; n--){
            assign(getPoint(SIDE_START + n, index_x, index_y, 0, 0).x,
                    getPoint(SIDE_START + n, index_x, index_y, 0, 0).y, 0);
        }
    }

    private boolean assign(int index_x, int index_y, int chess_value) {
        if (index_x >= 1 && index_x <= 10 && index_y >=1 && index_y <=10
                && (chess[index_x][index_y] == 0 || chess[index_x][index_y] == CHESS_RED)) {
            chess[index_x][index_y] = chess_value;
            return true;
        } else {
            return  false;
        }
    }

    private boolean check(int index_x, int index_y) {
        if (index_x >= 1 && index_x <= 10 && index_y >=1 && index_y <=10
                && chess[index_x][index_y] == 0) {
            return true;
        } else {
            return  false;
        }
    }

    private Point getPoint (int i, int index_x, int index_y, int x, int y) {
        Point point = new Point();

        switch (i) {
            // H_START is 0, means createPlane in the horizontal direction
            case H_START:   point.set(index_x + x,   index_y - 1); break;
            case H_START+1: point.set(index_x + x,   index_y - 2); break;
            case H_START+2: point.set(index_x + x,   index_y + 1); break;
            case H_START+3: point.set(index_x + x,   index_y + 2); break;
            case H_START+4: point.set(index_x + 2*x, index_y); break;
            case H_START+5: point.set(index_x + 3*x, index_y); break;
            case H_START+6: point.set(index_x + 3*x, index_y - 1); break;
            case H_START+7: point.set(index_x + 3*x, index_y + 1); break;

            // V_START is 8
            case V_START:   point.set(index_x - 1, index_y + y); break;
            case V_START+1: point.set(index_x - 2, index_y + y); break;
            case V_START+2: point.set(index_x + 1, index_y + y); break;
            case V_START+3: point.set(index_x + 2, index_y + y); break;
            case V_START+4: point.set(index_x,     index_y + 2*y); break;
            case V_START+5: point.set(index_x,     index_y + 3*y); break;
            case V_START+6: point.set(index_x - 1, index_y + 3*y); break;
            case V_START+7: point.set(index_x + 1, index_y + 3*y); break;

            //SIDE_START is 16, means up/down/right/left point of Head point
            case SIDE_START:   point.set(index_x + 1, index_y); break;
            case SIDE_START+1: point.set(index_x - 1, index_y); break;
            case SIDE_START+2: point.set(index_x,     index_y + 1); break;
            case SIDE_START+3: point.set(index_x,     index_y - 1); break;

            default: //blank
        }

        return point;
    }

    private void assignPlane(int index_x, int index_y, int x, int y, int chess_value) {
        Log.d(TAG, "createPlane");

        int start;
        if(x != 0) {    //Horizon
            start = H_START;
        } else {        //Vertical
            start = V_START;
        }

        //assignPlane
        for (int n = 7; n >= 0; n--){
            chess[getPoint(start + n, index_x, index_y, x, y).x][getPoint(start + n, index_x, index_y, x, y).y] = chess_value;
        }

        //resetRedPoint
        for (int n = 3; n >= 0; n--){
            assign(getPoint(SIDE_START + n, index_x, index_y, x, y).x, getPoint(SIDE_START + n, index_x, index_y, x, y).y, 0);
        }

        chess[index_x + x][index_y + y] = chess_value;
    }

    private void checkPlane(int index_x, int index_y, int x, int y) {
        int start;
        if (x != 0) {    //Horizon
            start = H_START;
        } else {        //Vertical
            start = V_START;
        }

        //checkPlane
        for (int n = 7; n >= 0; n--){
            if (!check(getPoint(start + n, index_x, index_y, x, y).x, getPoint(start + n, index_x, index_y, x, y).y)) {
                chess[index_x + x][index_y + y] = 0;
                break;
            }
        }
    }
}





