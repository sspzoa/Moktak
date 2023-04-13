package com.example.moktak;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    public int strike_cnt = 0;
    public int remain_time;
    public int remain_touch;
    public int i = 0;
    public int vis = 0;
    public int invis = 0;

    private SoundPool mSoundPool;
    private int mSoundId;
    private Vibrator vibrator;
    private SharedPreferences sharedPref;
    private ImageView moktak;
    private TextView strike_view;
    private RelativeLayout background;
    private TextView auto;
    private ImageView backlight;
    private Animation animScale;
    private Animation animFadeIn;
    private Animation animFadeOut;
    private Handler mHandler;
    private Runnable mRunnable;
    private Runnable nRunnable;
    private VibrationEffect vibe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initAnimations();
        initSoundPool();
        initHandlers();

        strike_cnt = sharedPref.getInt("key", 0);
        strike_view.setText("Strike: " + strike_cnt);

        remain_time = -1;
        remain_touch = 3;

        mHandler.post(mRunnable);

        background.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                onTouchBackground();
                return false;
            }
        });

        auto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAutoClick();
            }
        });
    }

    private void initViews() {
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        moktak = findViewById(R.id.moktak);
        strike_view = findViewById(R.id.strike);
        background = findViewById(R.id.background);
        auto = findViewById(R.id.auto);
        backlight = findViewById(R.id.backlight);
    }

    private void initAnimations() {
        animScale = AnimationUtils.loadAnimation(this, R.anim.anim_scale);
        animFadeIn = AnimationUtils.loadAnimation(this, R.anim.anim_fadein);
        animFadeOut = AnimationUtils.loadAnimation(this, R.anim.anim_fadeout);
    }

    private void initSoundPool() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mSoundPool = new SoundPool.Builder()
                    .setMaxStreams(1)
                    .build();
        } else {
            mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 1);
        }

        mSoundId = mSoundPool.load(getApplicationContext(), R.raw.moktak_sound, 1);
    }

    private void initHandlers() {
        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                updateState();
                mHandler.postDelayed(this, 1000);
            }
        };
        nRunnable = new Runnable() {
            @Override
            public void run() {
                onTouchBackground();
                mHandler.postDelayed(this, 1000);
            }
        };
    }

    private void updateState() {
        if (remain_time > 0) {
            remain_time--;
        } else if (remain_time == 0) {
            invis++;
            if (invis == 1) {
                System.out.println(invis);
                backlight.startAnimation(animFadeOut);
                backlight.setVisibility(View.INVISIBLE);
                vis = 0;
            }
        }

        if (remain_touch == 0) {
            vis++;
            if (vis == 1) {
                backlight.startAnimation(animFadeIn);
                backlight.setVisibility(View.VISIBLE);
                invis = 0;
            }
        }
        remain_touch = 3;
    }

    private void onTouchBackground() {
        moktak.startAnimation(animScale);

        strike_cnt++;
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("key", strike_cnt);
        editor.apply();

        remain_time = 1;
        if (remain_touch > 0) {
            remain_touch--;
        }
        vibe = VibrationEffect.createOneShot(40, 255);
        vibrator.vibrate(vibe);
        strike_view.setText("Strike: " + strike_cnt);
        mSoundPool.play(mSoundId, 1, 1, 1, 0, 1);
    }

    private void onAutoClick() {
        VibrationEffect vibe = VibrationEffect.createOneShot(50, 255);
        vibrator.vibrate(vibe);

        i++;
        if (i % 2 != 0) {
            mHandler.post(nRunnable);
            System.out.println(i);

            auto.setText("Auto (ON)");
            auto.setTextColor(Color.parseColor("#B8E6E1"));
        } else {
            mHandler.removeCallbacks(nRunnable);

            auto.setText("Auto (OFF)");
            auto.setTextColor(Color.parseColor("#FFFFFF"));
        }
    }
}
