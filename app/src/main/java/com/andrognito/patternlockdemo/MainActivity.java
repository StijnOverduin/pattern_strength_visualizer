package com.andrognito.patternlockdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.andrognito.patternlockview.utils.ResourceUtils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private PatternLockView mPatternLockView;

    private ArrayList<PatternLockView.Dot> corners;

    private PatternLockViewListener mPatternLockViewListener = new PatternLockViewListener() {
        @Override
        public void onStarted() {
            Log.d(getClass().getName(), "Pattern drawing started");
        }

        @Override
        public void onProgress(List<PatternLockView.Dot> progressPattern) {
            Log.d(getClass().getName(), "Pattern progress: " +
                    PatternLockUtils.patternToString(mPatternLockView, progressPattern));
            TextView patternInfo = (TextView) findViewById(R.id.patternText);
            patternInfo.setText("" + progressPattern);
            ProgressBar progressBar = (ProgressBar) findViewById(R.id.strengthMeter);
            TextView strengthText = (TextView) findViewById(R.id.strengthText);

//            if(progressPattern.size() <= 2) {
//                progressBar.setProgress(33);
//                progressBar.getProgressDrawable().setColorFilter(
//                        Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
//                strengthText.setText("WEAK");
//            } else if(progressPattern.size() <= 4) {
//                progressBar.setProgress(67);
//                progressBar.getProgressDrawable().setColorFilter(
//                        Color.YELLOW, android.graphics.PorterDuff.Mode.SRC_IN);
//                strengthText.setText("MEDIUM");
//            } else {
//                progressBar.setProgress(100);
//                progressBar.getProgressDrawable().setColorFilter(
//                        Color.GREEN, android.graphics.PorterDuff.Mode.SRC_IN);
//                strengthText.setText("STRONG");
//            }
        }

        @Override
        public void onComplete(List<PatternLockView.Dot> pattern) {
            Log.d(getClass().getName(), "Pattern complete: " +
                    PatternLockUtils.patternToString(mPatternLockView, pattern));
            Button button = (Button) findViewById(R.id.refreshBtn);
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = getIntent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    finish();
                    startActivity(intent);
                }
            });
            TextView debugInfo = (TextView) findViewById(R.id.debugText);
            int passCountStr = passedTwiceStraight(pattern);
            int passCountDia = passedTwiceDiagonal(pattern);
            int straightCount = straightLine(pattern);
            int diagLine = diagonalLine(pattern);
            debugInfo.setText("passCountStr = " + passCountStr + " straightCount = " + straightCount + " passCountDia = " + passCountDia + " DiagLine = " + diagLine);


        }

        @Override
        public void onCleared() {
            Log.d(getClass().getName(), "Pattern has been cleared");
        }
    };

    public int passedTwiceStraight(List<PatternLockView.Dot> progressPattern) {
        int count = 0;
        for(int i = 0; i + 1 < progressPattern.size(); i++) {
            int resRow = progressPattern.get(i).getRow() - progressPattern.get(i + 1).getRow();
            int resCol = progressPattern.get(i).getColumn() - progressPattern.get(i + 1).getColumn();
            if (resRow == -2 || resRow == 2 ) {
                if(progressPattern.get(i).getColumn() == progressPattern.get(i + 1).getColumn()) {
                    count += 1;
                }
            }
            if (resCol == 2 || resCol == -2) {
                if(progressPattern.get(i).getRow() == progressPattern.get(i + 1).getRow()) {
                    count += 1;
                }
            }
        }
        return count;
    }

    public int passedTwiceDiagonal(List<PatternLockView.Dot> progressPattern) {
        int count = 0;
        PatternLockView.Dot leftTop = new PatternLockView.Dot(0 ,0);
        PatternLockView.Dot leftBot = new PatternLockView.Dot(2 ,0);
        PatternLockView.Dot rightTop = new PatternLockView.Dot(0 ,2);
        PatternLockView.Dot rightBot = new PatternLockView.Dot(2 ,2);
        for (int i = 0; i + 1 < progressPattern.size(); i++) {
            if (progressPattern.get(i).equals(leftTop) && progressPattern.get(i + 1).equals(rightBot) || progressPattern.get(i).equals(rightBot) && progressPattern.get(i + 1).equals(leftTop)) {
                count += 1;
            }
            if (progressPattern.get(i).equals(rightTop) && progressPattern.get(i + 1).equals(leftBot) || progressPattern.get(i).equals(leftBot) && progressPattern.get(i + 1).equals(rightTop)) {
                count += 1;
            }
        }
        return count;
    }

    public int straightLine(List<PatternLockView.Dot> progressPattern) {
        int count = 0;
        for(int i = 0; i + 2 < progressPattern.size(); i++) {
            int rowSum = progressPattern.get(i).getRow() - progressPattern.get(i + 1).getRow() + progressPattern.get(i + 2).getRow();
            int colSum = progressPattern.get(i).getColumn() - progressPattern.get(i + 1).getColumn() + progressPattern.get(i + 2).getColumn();
            if (progressPattern.get(i).getColumn() == progressPattern.get(i + 1).getColumn() && progressPattern.get(i).getColumn() == progressPattern.get(i + 2).getColumn()) {
                if (rowSum == 1) {
                    count += 1;
                }
            } else if (progressPattern.get(i).getRow() == progressPattern.get(i + 1).getRow() && progressPattern.get(i).getRow() == progressPattern.get(i + 2).getRow()) {
                if (colSum == 1) {
                    count += 1;
                }
            }
        }

        return count;
    }

    public int diagonalLine(List<PatternLockView.Dot> progressPattern) {
        int count = 0;
        PatternLockView.Dot leftTop = new PatternLockView.Dot(0 ,0);
        PatternLockView.Dot leftBot = new PatternLockView.Dot(2 ,0);
        PatternLockView.Dot rightTop = new PatternLockView.Dot(0 ,2);
        PatternLockView.Dot rightBot = new PatternLockView.Dot(2 ,2);
        PatternLockView.Dot middle = new PatternLockView.Dot(1, 1);
        for (int i = 0; i + 2 < progressPattern.size(); i++) {
            if (progressPattern.get(i).equals(leftTop) && progressPattern.get(i + 1).equals(middle) && progressPattern.get(i + 2).equals(rightBot) || progressPattern.get(i).equals(rightBot) && progressPattern.get(i + 1).equals(middle) && progressPattern.get(i + 2).equals(leftTop)) {
                count += 1;
            }
            if (progressPattern.get(i).equals(rightTop) && progressPattern.get(i + 1).equals(middle) && progressPattern.get(i + 2).equals(leftBot)|| progressPattern.get(i).equals(leftBot) && progressPattern.get(i + 1).equals(middle) && progressPattern.get(i + 2).equals(rightTop)) {
                count += 1;
            }
        }
        return count;
    }

    public boolean crossed(List<PatternLockView.Dot> progressPattern) {
        boolean returnValue = false;



        return returnValue;
    }

//    public void makeCornerList() {
//        PatternLockView.Dot leftTop = new PatternLockView.Dot(0 ,0);
//        PatternLockView.Dot leftBot = new PatternLockView.Dot(2 ,0);
//        PatternLockView.Dot rightTop = new PatternLockView.Dot(0 ,2);
//        PatternLockView.Dot rightBot = new PatternLockView.Dot(2 ,2);
//        corners.add(leftBot);
//        corners.add(leftTop);
//        corners.add(rightBot);
//        corners.add(rightTop);
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        //makeCornerList();
        mPatternLockView = (PatternLockView) findViewById(R.id.patter_lock_view);
        mPatternLockView.setDotCount(3);
        //mPatternLockView.setDotNormalSize((int) ResourceUtils.getDimensionInPx(this, R.dimen.pattern_lock_dot_size));
        mPatternLockView.setDotSelectedSize((int) ResourceUtils.getDimensionInPx(this, R.dimen.pattern_lock_dot_selected_size));
        mPatternLockView.setPathWidth((int) ResourceUtils.getDimensionInPx(this, R.dimen.pattern_lock_path_width));
        mPatternLockView.setAspectRatioEnabled(true);
        mPatternLockView.setAspectRatio(PatternLockView.AspectRatio.ASPECT_RATIO_HEIGHT_BIAS);
        mPatternLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT);
        mPatternLockView.setDotAnimationDuration(150);
        mPatternLockView.setPathEndAnimationDuration(100);
        mPatternLockView.setCorrectStateColor(ResourceUtils.getColor(this, R.color.white));
        mPatternLockView.setInStealthMode(false);
        mPatternLockView.setTactileFeedbackEnabled(true);
        mPatternLockView.setInputEnabled(true);
        mPatternLockView.addPatternLockListener(mPatternLockViewListener);

    }
}
