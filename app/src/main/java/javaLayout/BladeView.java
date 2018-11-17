package javaLayout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

public class BladeView extends View{
    private OnItemClickListener mOnItemClickListener;
    String[] b = { "#", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K",
            "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X",
            "Y", "Z" };
    int choose = -1;
    Paint paint = new Paint();
    boolean showBkg = false;
    private PopupWindow mPopupWindow;
    private TextView mPopupText;
    private Handler handler = new Handler();

    //构造函数1：在代码中直接new一个BladeView实例的时候,会调用这个构造函数
    public BladeView(Context context) {
        super(context);
    }

    //构造函数2：在xml布局文件中调用BladeView的时候,会调用这个构造函数
    //在xml布局文件中调用BladeView,并且BladeView标签中还有自定义属性时,调用的还是这个构造函数
    public BladeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    //构造函数3：系统默认只会调用BladeView的前两个构造函数,
    // 至于第三个构造函数的调用,通常是我们自己在构造函数中主动调用的（例如,在第二个构造函数中调用第三个构造函数）
    public BladeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    public BladeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//        super(context, attrs, defStyleAttr, defStyleRes);
//    }
    @Override
    //将A-Z绘制到View中
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (showBkg) {
            //绘制画布背景
            canvas.drawColor(Color.parseColor("#00000000"));
        }
        int height = getHeight();//Return the height of your view
        int width = getWidth();//Return the width of your view
        int singleHeight = height / b.length;//每个字母的高度
        for (int i = 0; i < b.length; i++) {
            //Paint类包含关于如何绘制几何图形、文本和位图的样式和颜色信息
            paint.setColor(Color.parseColor("#ff2f2f2f"));//设置画笔的颜色
            paint.setTypeface(Typeface.DEFAULT_BOLD);//字体类型
            paint.setFakeBoldText(true);//设置粗体文字，注意设置在小字体上效果会非常差
            paint.setAntiAlias(true);//抗锯齿，使画笔更为圆滑
            if (i == choose) {//选中时的颜色
                paint.setColor(Color.parseColor("#3399ff"));//#3399ff一种蓝色
            }
            float xPos = width / 2 - paint.measureText(b[i]) / 2;//paint.measureText得到字符串的长度
            float yPos = singleHeight * i + singleHeight;
            canvas.drawText(b[i], xPos, yPos, paint);//参数：text:要绘制的文字；x：绘制的文字最左边的位置；y：绘制文字的基线位置（就是大概文字底部）；paint:用来做画的画笔
            paint.reset();// paint恢复为默认设置
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final int oldChoose = choose;
        final int c = (int) (event.getY() / getHeight() * b.length);

        switch (event.getAction()) {//getAction()
            // 返回动作类型
            case MotionEvent.ACTION_DOWN://手指的初次触摸
                showBkg = true;
                if (oldChoose != c) {
                    if (c > 0 && c < b.length) {
                        performItemClicked(c);
                        //Log.d("Letter",String.valueOf(c));
                        choose = c;
                        invalidate();//重绘制
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE://手指在屏幕上滑动
                if (oldChoose != c) {
                    if (c > 0 && c < b.length) {
                        performItemClicked(c);
                        choose = c;
                        invalidate();//重绘制
                    }
                }
                break;
            case MotionEvent.ACTION_UP://手指抬起
                showBkg = false;
                choose = -1;
                dismissPopup();
                invalidate();
                break;
        }
        return true;
    }

    //选中某字母，在屏幕中间产生一个灰色框显示这个字母
    private void showPopup(int item) {
        if (mPopupWindow == null) {
            handler.removeCallbacks(dismissRunnable);
            mPopupText = new TextView(getContext());//获取点到的内容
            mPopupText.setBackgroundColor(Color.GRAY);//灰
            mPopupText.setTextColor(Color.CYAN);//青色
            mPopupText.setTextSize(50);
            mPopupText.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
            mPopupWindow = new PopupWindow(mPopupText, 100, 100);
        }
        String text = "";
        if (item == 0) {
            text = "#";
        } else {
            text = Character.toString((char) ('A' + item - 1));
        }
        mPopupText.setText(text);
        if (mPopupWindow.isShowing()) {
            mPopupWindow.update();
        } else {
            mPopupWindow.showAtLocation(getRootView(),
                    Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
        }
    }

    private void dismissPopup() {
        handler.postDelayed(dismissRunnable, 800);
    }

    Runnable dismissRunnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            if (mPopupWindow != null) {
                mPopupWindow.dismiss();
            }
        }
    };

    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    private void performItemClicked(int item) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(b[item]);
            showPopup(item);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(String s);
    }

}
