package com.jokerpeng.demo.danmudemo;

import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.Random;

import master.flame.danmaku.controller.DrawHandler;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.ui.widget.DanmakuView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView mImageView;
    private DanmakuView mDanmakuView;
    private LinearLayout mLinearLayout;
    private Button mButton;
    private EditText mEditText;

    private boolean showDanmaku;
    private DanmakuContext danmakuContext;
    private BaseDanmakuParser parser = new BaseDanmakuParser() {//弹幕的解析器
        @Override
        protected IDanmakus parse() {
            return new Danmakus();
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        initAction();
    }

    private void initAction() {
        mDanmakuView.setOnClickListener(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && Build.VERSION.SDK_INT >= 19){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE);
        }
    }

    private void initData() {
        mDanmakuView.enableDanmakuDrawingCache(true);
        mDanmakuView.setCallback(new DrawHandler.Callback() {
            @Override
            public void prepared() {
                showDanmaku = true;
                mDanmakuView.start();
                generateSomeDanmaku();
            }

            @Override
            public void updateTimer(DanmakuTimer timer) {

            }

            @Override
            public void danmakuShown(BaseDanmaku danmaku) {

            }

            @Override
            public void drawingFinished() {

            }
        });
        danmakuContext = DanmakuContext.create();//创建danmakuContext实例，DanmakuContext可以用于对弹幕的各种全局配置进行设定
        mDanmakuView.prepare(parser,danmakuContext);
    }

    private void initView() {
        mImageView = (ImageView) findViewById(R.id.iv);
        mDanmakuView = (DanmakuView) findViewById(R.id.dv);
        mLinearLayout = (LinearLayout) findViewById(R.id.ll);
        mButton = (Button) findViewById(R.id.btn);
        mEditText = (EditText) findViewById(R.id.et);
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {//对系统全局的UI变化进行了监听，保证程序一直可以处于沉浸式模式。
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if(visibility == View.SYSTEM_UI_FLAG_VISIBLE){
                    onWindowFocusChanged(true);
                }
            }
        });
    }


    /**
     * 随机生成一些弹幕内容以供测试
     */
    private void generateSomeDanmaku() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(showDanmaku) {
                    int time = new Random().nextInt(300);
                    String content = "" + time + time;
                    addDanmaku(content, false);
                    try {
                        Thread.sleep(time);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }


    /**
     * 向弹幕View中添加一条弹幕
     * @param content
     *          弹幕的具体内容
     * @param  withBorder
     *          弹幕是否有边框
     */
    private void addDanmaku(String content, boolean withBorder) {
        BaseDanmaku danmaku = danmakuContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);//创建一条从右向左的弹幕
        danmaku.text = content;
        danmaku.padding = 5;
        danmaku.textSize = sp2px(20);
        danmaku.textColor = Color.WHITE;
        danmaku.setTime(mDanmakuView.getCurrentTime());
        if (withBorder) {//是否有边框
            danmaku.borderColor = Color.GREEN;
        }
        mDanmakuView.addDanmaku(danmaku);
    }

    /**
     * sp转px的方法。
     */
    private int sp2px(float spValue){
        final float fontScale = getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }


    @Override
    protected void onPause() {
        super.onPause();
        mDanmakuView.pause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mDanmakuView.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        showDanmaku = false;
        if(mDanmakuView != null){
            mDanmakuView.release();
            mDanmakuView = null;
        }
    }

    @Override
    public void onClick(View v) {
        if(null != v){
            switch (v.getId()){
                case R.id.dv://点击屏幕显示弹幕输入框
                    if (mLinearLayout.getVisibility() == View.GONE){
                        mLinearLayout.setVisibility(View.VISIBLE);
                    }else{
                        mLinearLayout.setVisibility(View.GONE);
                    }
                break;
                case R.id.btn://发送弹幕
                    String content = mEditText.getText().toString();
                    if (!TextUtils.isEmpty(content)){
                        addDanmaku(content,true);
                        mEditText.setText("");
                    }
                break;
            }
        }
    }
}
