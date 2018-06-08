package com.andrognito.patternlockdemo;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private PatternLockView mPatternLockView;

    private Map<String, PatternLockView.Dot> allNodes = new HashMap<>();
    private Map<Pair<PatternLockView.Dot, PatternLockView.Dot>, ArrayList<Pair<PatternLockView.Dot, PatternLockView.Dot>>> corner = new HashMap<>();
    private ArrayList<PatternLockView.Dot> cornerNodes = new ArrayList<>();

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

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
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
            int nrDots = pattern.size();
            double strength = calculateStrength(pattern);

            ProgressBar progressBar = (ProgressBar) findViewById(R.id.strengthMeter);
            TextView strengthText = (TextView) findViewById(R.id.strengthText);
            strength = (strength / 46) * 100;

            if(strength < 40) {
                progressBar.setProgressTintList(ColorStateList.valueOf(Color.RED));
                strengthText.setText("WEAK");
                progressBar.setProgress((int) strength);
            } else if(strength < 60) {
                progressBar.setProgressTintList(ColorStateList.valueOf(Color.YELLOW));
                strengthText.setText("MEDIUM");
                progressBar.setProgress((int) strength);
            } else {
                progressBar.setProgressTintList(ColorStateList.valueOf(Color.GREEN));
                progressBar.setProgress((int) strength);
                strengthText.setText("STRONG");
            }
            debugInfo.setText("" + strength);
            //debugInfo.setText("passCountStr = " + passCountStr + " passCountDia = " + passCountDia + " length = " + plen + " nrDots = " + nrDots + " starting Dot = " + startingDot + " crossCount = " + crossCount);

            if(nrDots < 9) {
                PatternLockView.Dot suggestion = giveSuggestion(pattern, strength);
                debugInfo.setText("" + possibleNodes(pattern));
            }


        }

        @Override
        public void onCleared() {
            Log.d(getClass().getName(), "Pattern has been cleared");
        }
    };

    public PatternLockView.Dot giveSuggestion(List<PatternLockView.Dot> pattern, double currentStrength) {
        for(Map.Entry<String, PatternLockView.Dot> dot1 : allNodes.entrySet()) {

        }

        return allNodes.get("row1COl2");
    }

    public List<PatternLockView.Dot> possibleNodes(List<PatternLockView.Dot> pattern) {
        PatternLockView.Dot lastNode = pattern.get(pattern.size() - 1);
        Pair<PatternLockView.Dot, PatternLockView.Dot> oppositeLR = new Pair<>(allNodes.get("row1Col0"), allNodes.get("row1Col2"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> oppositeUD = new Pair<>(allNodes.get("rowCol1"), allNodes.get("row2Col1"));
        List<PatternLockView.Dot> possibleNodeList = new ArrayList<>();
        if(cornerNodes.contains(lastNode)) {
            for(Map.Entry<String, PatternLockView.Dot> dot1 : allNodes.entrySet()) {
                if(!pattern.contains(dot1.getValue())) {
                    if(!cornerNodes.contains(dot1.getValue())) {
                        possibleNodeList.add(dot1.getValue());
                    } else {

                    }
                }
            }

        } else if(lastNode == allNodes.get("row1Col1")) {
            for(Map.Entry<String, PatternLockView.Dot> dot1 : allNodes.entrySet()) {
                if(!pattern.contains(dot1.getValue())) {
                    possibleNodeList.add(dot1.getValue());
                }
            }

        } else {
                for (Map.Entry<String, PatternLockView.Dot> dot1 : allNodes.entrySet()) {
                    if (!pattern.contains(dot1.getValue())) {
                        if((lastNode.equals(oppositeLR.first) && dot1.getValue().equals(oppositeLR.second)) || (lastNode.equals(oppositeLR.second) && dot1.getValue().equals(oppositeLR.first)) ||
                                (lastNode.equals(oppositeUD.first) && dot1.getValue().equals(oppositeUD.second)) || (lastNode.equals(oppositeUD.second) && dot1.getValue().equals(oppositeUD.first))) {
                            if(pattern.contains(allNodes.get("row1Col1"))) {
                                possibleNodeList.add(dot1.getValue());
                            }
                        } else {
                            possibleNodeList.add(dot1.getValue());
                        }

                    }
                }
            }
            return possibleNodeList;
    }

    public double calculateStrength(List<PatternLockView.Dot> pattern) {
        int passCountStr = passedTwiceStraight(pattern);
        int passCountDia = passedTwiceDiagonal(pattern);
        double plen = patternLength(pattern);
        int nrDots = pattern.size();
        int crossCount = crossSections(pattern);
        double strength =  nrDots * (Math.log(plen + crossCount + passCountDia + passCountStr) / Math.log(2.0));

        return strength;
    }

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
        PatternLockView.Dot leftTop = this.allNodes.get("row0Col0");
        PatternLockView.Dot leftBot = this.allNodes.get("row2Col0");
        PatternLockView.Dot rightTop = this.allNodes.get("row0Col2");
        PatternLockView.Dot rightBot = this.allNodes.get("row2Col2");
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
        PatternLockView.Dot leftTop = this.allNodes.get("row0Col0");
        PatternLockView.Dot leftBot = this.allNodes.get("row2Col0");
        PatternLockView.Dot rightTop = this.allNodes.get("row0Col2");
        PatternLockView.Dot rightBot = this.allNodes.get("row2Col2");
        PatternLockView.Dot middle = this.allNodes.get("row1Col1");
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

    public double patternLength(List<PatternLockView.Dot> progressPattern) {
        double plen = 0;

        for(int i = 0; i + 1 < progressPattern.size(); i++) {
            if(progressPattern.get(i).getRow() == progressPattern.get(i + 1).getRow()) {
                plen += Math.abs(progressPattern.get(i).getColumn() - progressPattern.get(i + 1).getColumn());
            } else if(progressPattern.get(i).getColumn() == progressPattern.get(i + 1).getColumn()) {
                plen += Math.abs(progressPattern.get(i).getRow() - progressPattern.get(i + 1).getRow());
            } else if(Math.abs(progressPattern.get(i).getRow() - progressPattern.get(i + 1).getRow()) == 1) {
                plen += Math.sqrt(2);
            } else if(Math.abs(progressPattern.get(i).getRow() - progressPattern.get(i + 1).getRow()) == 1 && Math.abs(progressPattern.get(i).getColumn() - progressPattern.get(i + 1).getColumn()) == 2) {
                plen += Math.sqrt(5);
            } else if (Math.abs(progressPattern.get(i).getRow() - progressPattern.get(i + 1).getRow()) == 2 && Math.abs(progressPattern.get(i).getColumn() - progressPattern.get(i + 1).getColumn()) == 1) {
                plen += Math.sqrt(5);
            } else if (Math.abs(progressPattern.get(i).getRow() - progressPattern.get(i + 1).getRow()) == 2 && Math.abs(progressPattern.get(i).getColumn() - progressPattern.get(i + 1).getColumn()) == 2) {
                plen += 2*Math.sqrt(2);
            }
        }

        return plen;
     }

    public int crossSections(List<PatternLockView.Dot> progressPattern) {
       int count = 0;
        TextView debugInfo = (TextView) findViewById(R.id.debugText);
       for(int i = 0; i + 1 < progressPattern.size(); i++) {
           for(int j = 0; j + 1 < progressPattern.size(); j++ ) {
               PatternLockView.Dot node1b = progressPattern.get(i);
               PatternLockView.Dot node1e = progressPattern.get(i + 1);
               PatternLockView.Dot node2b = progressPattern.get(j);
               PatternLockView.Dot node2e = progressPattern.get(j + 1);
               Pair<PatternLockView.Dot, PatternLockView.Dot> line1Pair = new Pair<>(node1b, node1e);
               Pair<PatternLockView.Dot, PatternLockView.Dot> line2Pair = new Pair<>(node2b, node2e);
               if(!equalPairs(line1Pair, line2Pair)) {
                   for(Map.Entry<Pair<PatternLockView.Dot, PatternLockView.Dot>, ArrayList<Pair<PatternLockView.Dot, PatternLockView.Dot>>> entry : corner.entrySet()) {
                       if(cornerNodes.contains(entry.getKey().first) || cornerNodes.contains((entry.getKey().second))) {
                           if(equalPairs(entry.getKey(), line1Pair)) {
                               debugInfo.setText("hello1" + entry.getKey() + " array : " + entry.getValue());
                               if(pairInArray(entry.getValue(), line2Pair)) {
                                   count += 1;
                               }
                           } else if(equalPairs(transformPairverhor(entry.getKey(), "v"), line1Pair)) {
                               debugInfo.setText(debugInfo.getText() + "hello2");
                               if (pairInArray(transformPairArray(entry.getValue(), "v"), line2Pair)) {
                                   debugInfo.setText(debugInfo.getText() + "hello2");
                                   count += 1;
                               }
                           } else if(equalPairs(transformPairverhor(entry.getKey(), "h"), line1Pair)) {
                               debugInfo.setText(debugInfo.getText() + "hello2");
                               if(pairInArray(transformPairArray(entry.getValue(), "h"), line2Pair)) {
                                   debugInfo.setText(debugInfo.getText() + "hello3");
                                   count += 1;
                               }
                           } else if(equalPairs(transformPairverhor(transformPairverhor(entry.getKey(), "h"), "v"), line1Pair)) {
                               debugInfo.setText(debugInfo.getText() + "hello2");
                               if(pairInArray(transformPairArray(transformPairArray(entry.getValue(), "h"), "v"), line2Pair)) {
                                   debugInfo.setText(debugInfo.getText() + "hello4");
                                   count += 1;
                               }
                           }
                       } else {
                           if(equalPairs(entry.getKey(), line1Pair)) {
                               if (pairInArray(entry.getValue(), line2Pair)) {
                                   debugInfo.setText(debugInfo.getText() + "hello5");
                                   count += 1;
                               }
                           }
//                           } else if(equalPairs(transformPairDiag(entry.getKey(), "lbtr"), line1Pair)) {
//                               if (pairInArray(transformPairArray(entry.getValue(), "lbtr"), line2Pair)) {
//                                   debugInfo.setText(debugInfo.getText() + "hello6");
//                                   count += 1;
//                               }
//                           } else if(equalPairs(transformPairDiag(entry.getKey(), "rbtl"), line1Pair)) {
//                               if(pairInArray(transformPairArray(entry.getValue(), "rbtl"), line2Pair)) {
//                                   debugInfo.setText(debugInfo.getText() + "hello7");
//                                   count += 1;
//                               }
//                           } else if(equalPairs(transformPairverhor(entry.getKey(), "v"), line1Pair)) {
//                               //debugInfo.setText("" + entry.getValue() + "Transformed: " + transformPairArray(entry.getValue(), "v"));
//                               if(pairInArray(transformPairArray(entry.getValue(), "v"), line2Pair)) {
//                                   debugInfo.setText(debugInfo.getText() + "hello8");
//                                   count += 1;
//                               }
//                           }
                       }

                   }
               }


           }

       }

       count = count/2;

       return count;
    }

    public boolean equalPairs(Pair<PatternLockView.Dot, PatternLockView.Dot> pairOne, Pair<PatternLockView.Dot, PatternLockView.Dot> pairTwo) {
        boolean result = false;

        if(pairOne.first.equals(pairTwo.first) && pairOne.second.equals(pairTwo.second) ) {
            result = true;
        } else if (pairOne.first.equals(pairTwo.second) && pairOne.second.equals(pairTwo.first)) {
            result = true;
        }

        return result;
    }

    public boolean pairInArray(ArrayList<Pair<PatternLockView.Dot, PatternLockView.Dot>> array, Pair<PatternLockView.Dot, PatternLockView.Dot> pair) {
        boolean result = false;

        for(Pair<PatternLockView.Dot, PatternLockView.Dot> pairs : array) {
            if(equalPairs(pairs, pair)) {
                result = true;
            }
        }

        return result;
    }


    public ArrayList<Pair<PatternLockView.Dot, PatternLockView.Dot>> transformPairArray(ArrayList<Pair<PatternLockView.Dot, PatternLockView.Dot>> array, String type) {
        ArrayList<Pair<PatternLockView.Dot, PatternLockView.Dot>> transformed = new ArrayList<>();
        for(Pair<PatternLockView.Dot, PatternLockView.Dot> pair : array) {
            if(type.equals("h") || type.equals("v")) {
                Pair<PatternLockView.Dot, PatternLockView.Dot> transPair = transformPairverhor(pair, type);
                transformed.add(transPair);
            } else {
                Pair<PatternLockView.Dot, PatternLockView.Dot> transPair = transformPairDiag(pair, type);
                transformed.add(transPair);
            }

        }

        return transformed;
    }

    public Pair<PatternLockView.Dot, PatternLockView.Dot> transformPairverhor(Pair<PatternLockView.Dot, PatternLockView.Dot> pair, String type) {
        int firstn;
        int secondn;
        PatternLockView.Dot firstDot = pair.first;
        PatternLockView.Dot secondDot = pair.second;

        if(type.equals("v")) {
            firstn = pair.first.getColumn();
            secondn = pair.second.getColumn();
            if(firstn == 2 || firstn == 0) {
                if (firstn == 2) {
                    firstDot = new PatternLockView.Dot(pair.first.getRow(), 0);
                } else {
                    firstDot = new PatternLockView.Dot(pair.first.getRow(), 2);
                }
            }
            if(secondn == 2 || secondn == 0) {
                if (secondn == 2) {
                    secondDot = new PatternLockView.Dot(pair.second.getRow(), 0);
                } else {
                    secondDot = new PatternLockView.Dot(pair.second.getRow(), 2);
                }
            }
        } else if (type.equals("h")){
            firstn = pair.first.getRow();
            secondn = pair.second.getRow();
            if (firstn == 2 || firstn == 0) {
                if (firstn == 2) {
                    firstDot = new PatternLockView.Dot(0, pair.first.getColumn());
                } else {
                    firstDot = new PatternLockView.Dot(2, pair.first.getColumn());
                }
            }
            if (secondn == 2 || secondn == 0) {
                if (secondn == 2) {
                    secondDot = new PatternLockView.Dot(0, pair.second.getColumn());
                } else {
                    secondDot = new PatternLockView.Dot(2, pair.second.getColumn());
                }
            }
        }

        Pair<PatternLockView.Dot, PatternLockView.Dot> transformed = new Pair<>(firstDot, secondDot);
        return transformed;
    }

    public Pair<PatternLockView.Dot, PatternLockView.Dot> transformPairDiag(Pair<PatternLockView.Dot, PatternLockView.Dot> pair, String type) {
        int firstCol = pair.first.getColumn();
        int secondCol = pair.second.getColumn();
        int firstRow = pair.first.getRow();
        int secondRow = pair.second.getRow();
        PatternLockView.Dot firstDot = pair.first;
        PatternLockView.Dot secondDot = pair.second;

        if(type.equals("lbtr")) {
            if (firstRow == 1 && firstCol == 0 || firstRow == 0 && firstCol == 1 ) {
                firstDot = new PatternLockView.Dot(firstRow + 1, firstCol + 1);
            } else if (firstRow == 0 && firstCol == 0){
                firstDot = new PatternLockView.Dot(2, 2);
            } else if (firstRow == 1 && firstCol == 2 || firstRow == 2 && firstCol == 1 ) {
                firstDot = new PatternLockView.Dot(firstRow - 1, firstCol - 1);
            } else if(firstRow == 2 && firstCol == 2 ){
                firstDot = new PatternLockView.Dot(0, 0);
            }

            if (secondRow == 1 && secondCol == 0 || secondRow == 0 && secondCol == 1 ) {
                secondDot = new PatternLockView.Dot(secondRow + 1, secondCol + 1);
            } else if (secondRow == 0 && secondCol == 0){
                secondDot = new PatternLockView.Dot(2, 2);
            } else if (secondRow == 1 && secondCol == 2 || secondRow == 2 && secondCol == 1 ) {
                secondDot = new PatternLockView.Dot(secondRow - 1, secondCol - 1);
            } else if(secondRow == 2 && secondCol == 2 ){
                secondDot = new PatternLockView.Dot(0, 0);
            }
        } else if(type.equals("rbtl")){
            if (firstRow == 0 && firstCol == 1 || firstRow == 1 && firstCol == 2) {
                firstDot = new PatternLockView.Dot(firstRow + 1, firstCol - 1);
            } else if (firstRow == 0 && firstCol == 2){
                firstDot = new PatternLockView.Dot(2, 0);
            } else if (firstRow == 2 && firstCol == 1 || firstRow == 1 && firstCol == 0) {
                firstDot = new PatternLockView.Dot(firstRow - 1, firstCol + 1);
            } else if(firstRow == 2 && firstCol == 0){
                firstDot = new PatternLockView.Dot(0, 2);
            }

            if (secondRow == 0 && secondCol == 1 || secondRow == 1 && secondCol == 2) {
                secondDot = new PatternLockView.Dot(secondRow + 1, secondCol - 1);
            } else if (secondRow == 0 && secondCol == 2){
                secondDot = new PatternLockView.Dot(2, 0);
            } else if (secondRow == 2 && secondCol == 1 || secondRow == 1 && secondCol == 0) {
                secondDot = new PatternLockView.Dot(secondRow - 1, secondCol + 1);
            } else if(secondRow == 2 && secondCol == 0){
                secondDot = new PatternLockView.Dot(0, 2);
            }
        }

        Pair<PatternLockView.Dot, PatternLockView.Dot> transformed = new Pair<>(firstDot, secondDot);
        return transformed;
    }

    public void makeNodeMap() {
        PatternLockView.Dot row0Col0 = new PatternLockView.Dot(0 ,0);
        PatternLockView.Dot row0Col1 = new PatternLockView.Dot(0 ,1);
        PatternLockView.Dot row0Col2 = new PatternLockView.Dot(0 ,2);
        PatternLockView.Dot row1Col0 = new PatternLockView.Dot(1 ,0);
        PatternLockView.Dot row1Col1 = new PatternLockView.Dot(1 ,1);
        PatternLockView.Dot row1Col2 = new PatternLockView.Dot(1 ,2);
        PatternLockView.Dot row2Col0 = new PatternLockView.Dot(2 ,0);
        PatternLockView.Dot row2Col1 = new PatternLockView.Dot(2 ,1);
        PatternLockView.Dot row2Col2 = new PatternLockView.Dot(2 ,2);

        this.allNodes.put("row0Col0", row0Col0);
        this.allNodes.put("row0Col1", row0Col1);
        this.allNodes.put("row0Col2", row0Col2);
        this.allNodes.put("row1Col0", row1Col0);
        this.allNodes.put("row1Col1", row1Col1);
        this.allNodes.put("row1Col2", row1Col2);
        this.allNodes.put("row2Col0", row2Col0);
        this.allNodes.put("row2Col1", row2Col1);
        this.allNodes.put("row2Col2", row2Col2);
    }

    public void makeCornerList () {
        PatternLockView.Dot row0Col0 = new PatternLockView.Dot(0 ,0);
        PatternLockView.Dot row2Col0 = new PatternLockView.Dot(2 ,0);
        PatternLockView.Dot row0Col2 = new PatternLockView.Dot(0 ,2);
        PatternLockView.Dot row2Col2 = new PatternLockView.Dot(2 ,2);
        cornerNodes.add(row0Col0);
        cornerNodes.add(row2Col0);
        cornerNodes.add(row0Col2);
        cornerNodes.add(row2Col2);
    }

    public void makeCrossSectionsCorner() {
        Pair<PatternLockView.Dot, PatternLockView.Dot> option1 = new Pair<>(allNodes.get("row0Col0"), allNodes.get("row1Col1"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option1cross1 = new Pair<>(allNodes.get("row2Col0"), allNodes.get("row0Col1"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option1cross2 = new Pair<>(allNodes.get("row1Col0"), allNodes.get("row0Col1"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option1cross3 = new Pair<>(allNodes.get("row1Col0"), allNodes.get("row0Col2"));
        ArrayList<Pair<PatternLockView.Dot, PatternLockView.Dot>> option1array = new ArrayList<>();
        option1array.add(option1cross1);
        option1array.add(option1cross2);
        option1array.add(option1cross3);
        corner.put(option1, option1array);

        Pair<PatternLockView.Dot, PatternLockView.Dot> option2 = new Pair<>(allNodes.get("row0Col0"), allNodes.get("row2Col1"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option2cross1 = new Pair<>(allNodes.get("row2Col0"), allNodes.get("row0Col1"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option2cross2 = new Pair<>(allNodes.get("row2Col0"), allNodes.get("row1Col1"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option2cross3 = new Pair<>(allNodes.get("row2Col0"), allNodes.get("row1Col2"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option2cross4 = new Pair<>(allNodes.get("row1Col0"), allNodes.get("row0Col1"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option2cross5 = new Pair<>(allNodes.get("row1Col0"), allNodes.get("row0Col2"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option2cross6 = new Pair<>(allNodes.get("row2Col2"), allNodes.get("row1Col0"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option2cross7 = new Pair<>(allNodes.get("row1Col1"), allNodes.get("row1Col0"));
        ArrayList<Pair<PatternLockView.Dot, PatternLockView.Dot>> option2array = new ArrayList<>();
        option2array.add(option2cross1);
        option2array.add(option2cross2);
        option2array.add(option2cross3);
        option2array.add(option2cross4);
        option2array.add(option2cross5);
        option2array.add(option2cross6);
        option2array.add(option2cross7);
        corner.put(option2, option2array);

        Pair<PatternLockView.Dot, PatternLockView.Dot> option3 = new Pair<>(allNodes.get("row0Col0"), allNodes.get("row1Col2"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option3cross1 = new Pair<>(allNodes.get("row1Col0"), allNodes.get("row0Col1"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option3cross2 = new Pair<>(allNodes.get("row1Col0"), allNodes.get("row0Col2"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option3cross3 = new Pair<>(allNodes.get("row2Col0"), allNodes.get("row0Col1"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option3cross4 = new Pair<>(allNodes.get("row2Col2"), allNodes.get("row0Col1"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option3cross5 = new Pair<>(allNodes.get("row2Col1"), allNodes.get("row0Col2"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option3cross6 = new Pair<>(allNodes.get("row1Col1"), allNodes.get("row0Col2"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option3cross7 = new Pair<>(allNodes.get("row1Col1"), allNodes.get("row0Col1"));
        ArrayList<Pair<PatternLockView.Dot, PatternLockView.Dot>> option3array = new ArrayList<>();
        option3array.add(option3cross1);
        option3array.add(option3cross2);
        option3array.add(option3cross3);
        option3array.add(option3cross4);
        option3array.add(option3cross5);
        option3array.add(option3cross6);
        option3array.add(option3cross7);
        corner.put(option3, option3array);

//        Pair<PatternLockView.Dot, PatternLockView.Dot> option4 = new Pair<>(allNodes.get("row0Col0"), allNodes.get("row2Col2"));
//        Pair<PatternLockView.Dot, PatternLockView.Dot> option4cross1 = new Pair<>(allNodes.get("row2Col1"), allNodes.get("row1Col2"));
//        Pair<PatternLockView.Dot, PatternLockView.Dot> option4cross2 = new Pair<>(allNodes.get("row2Col1"), allNodes.get("row0Col2"));
//        Pair<PatternLockView.Dot, PatternLockView.Dot> option4cross3 = new Pair<>(allNodes.get("row2Col0"), allNodes.get("row1Col2"));
//        ArrayList<Pair<PatternLockView.Dot, PatternLockView.Dot>> option4array = new ArrayList<>();
//        option4array.add(option4cross1);
//        option4array.add(option4cross2);
//        option4array.add(option4cross3);
//        corner.put(option4, option4array);


    }

    public void makeCrossSectionsSideMiddle() {
        Pair<PatternLockView.Dot, PatternLockView.Dot> option1 = new Pair<>(allNodes.get("row1Col0"), allNodes.get("row0Col1"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option1cross1 = new Pair<>(allNodes.get("row2Col1"), allNodes.get("row0Col0"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option1cross2 = new Pair<>(allNodes.get("row1Col1"), allNodes.get("row0Col0"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option1cross3 = new Pair<>(allNodes.get("row1Col2"), allNodes.get("row0Col0"));
        //Pair<PatternLockView.Dot, PatternLockView.Dot> option1cross4 = new Pair<>(allNodes.get("row2Col2"), allNodes.get("row0Col0"));
        ArrayList<Pair<PatternLockView.Dot, PatternLockView.Dot>> option1array = new ArrayList<>();
        option1array.add(option1cross1);
        option1array.add(option1cross2);
        option1array.add(option1cross3);
        //option1array.add(option1cross4);
        corner.put(option1, option1array);

        Pair<PatternLockView.Dot, PatternLockView.Dot> option2 = new Pair<>(allNodes.get("row1Col0"), allNodes.get("row1Col1"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option2cross1 = new Pair<>(allNodes.get("row2Col1"), allNodes.get("row0Col0"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option2cross2 = new Pair<>(allNodes.get("row2Col0"), allNodes.get("row0Col1"));
        ArrayList<Pair<PatternLockView.Dot, PatternLockView.Dot>> option2array = new ArrayList<>();
        option2array.add(option2cross1);
        option2array.add(option2cross2);
        corner.put(option2, option2array);


        Pair<PatternLockView.Dot, PatternLockView.Dot> option3 = new Pair<>(allNodes.get("row1Col2"), allNodes.get("row2Col1"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option3cross1 = new Pair<>(allNodes.get("row2Col2"), allNodes.get("row1Col1"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option3cross2 = new Pair<>(allNodes.get("row2Col2"), allNodes.get("row0Col1"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option3cross3 = new Pair<>(allNodes.get("row2Col2"), allNodes.get("row1Col0"));
        //Pair<PatternLockView.Dot, PatternLockView.Dot> option3cross4 = new Pair<>(allNodes.get("row2Col2"), allNodes.get("row0Col0"));
        ArrayList<Pair<PatternLockView.Dot, PatternLockView.Dot>> option3array = new ArrayList<>();
        option3array.add(option3cross1);
        option3array.add(option3cross2);
        option3array.add(option3cross3);
        //option3array.add(option3cross4);
        corner.put(option3, option3array);

        Pair<PatternLockView.Dot, PatternLockView.Dot> option4 = new Pair<>(allNodes.get("row1Col0"), allNodes.get("row2Col1"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option4cross1 = new Pair<>(allNodes.get("row2Col0"), allNodes.get("row1Col1"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option4cross2 = new Pair<>(allNodes.get("row2Col0"), allNodes.get("row0Col1"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option4cross3 = new Pair<>(allNodes.get("row2Col0"), allNodes.get("row1Col2"));
        //Pair<PatternLockView.Dot, PatternLockView.Dot> option4cross4 = new Pair<>(allNodes.get("row0Col2"), allNodes.get("row2Col0"));
        ArrayList<Pair<PatternLockView.Dot, PatternLockView.Dot>> option4array = new ArrayList<>();
        option4array.add(option4cross1);
        option4array.add(option4cross2);
        option4array.add(option4cross3);
        //option4array.add(option4cross4);
        corner.put(option4, option4array);

        Pair<PatternLockView.Dot, PatternLockView.Dot> option5 = new Pair<>(allNodes.get("row1Col2"), allNodes.get("row0Col1"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option5cross1 = new Pair<>(allNodes.get("row0Col2"), allNodes.get("row2Col1"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option5cross2 = new Pair<>(allNodes.get("row0Col2"), allNodes.get("row1Col1"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option5cross3 = new Pair<>(allNodes.get("row0Col2"), allNodes.get("row1Col0"));
        //Pair<PatternLockView.Dot, PatternLockView.Dot> option5cross4 = new Pair<>(allNodes.get("row2Col0"), allNodes.get("row0Col2"));
        ArrayList<Pair<PatternLockView.Dot, PatternLockView.Dot>> option5array = new ArrayList<>();
        option5array.add(option5cross1);
        option5array.add(option5cross2);
        option5array.add(option5cross3);
        //option5array.add(option5cross4);
        corner.put(option5, option5array);

        Pair<PatternLockView.Dot, PatternLockView.Dot> option6 = new Pair<>(allNodes.get("row2Col1"), allNodes.get("row1Col1"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option6cross1 = new Pair<>(allNodes.get("row2Col0"), allNodes.get("row1Col2"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option6cross2 = new Pair<>(allNodes.get("row1Col0"), allNodes.get("row2Col2"));
        ArrayList<Pair<PatternLockView.Dot, PatternLockView.Dot>> option6array = new ArrayList<>();
        option6array.add(option6cross1);
        option6array.add(option6cross2);
        corner.put(option6, option6array);

        Pair<PatternLockView.Dot, PatternLockView.Dot> option7 = new Pair<>(allNodes.get("row1Col2"), allNodes.get("row1Col1"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option7cross1 = new Pair<>(allNodes.get("row0Col2"), allNodes.get("row2Col1"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option7cross2 = new Pair<>(allNodes.get("row2Col2"), allNodes.get("row0Col1"));
        ArrayList<Pair<PatternLockView.Dot, PatternLockView.Dot>> option7array = new ArrayList<>();
        option7array.add(option7cross1);
        option7array.add(option7cross2);
        corner.put(option7, option7array);

        Pair<PatternLockView.Dot, PatternLockView.Dot> option8 = new Pair<>(allNodes.get("row0Col1"), allNodes.get("row1Col1"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option8cross1 = new Pair<>(allNodes.get("row0Col0"), allNodes.get("row1Col2"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option8cross2 = new Pair<>(allNodes.get("row1Col0"), allNodes.get("row0Col2"));
        ArrayList<Pair<PatternLockView.Dot, PatternLockView.Dot>> option8array = new ArrayList<>();
        option8array.add(option8cross1);
        option8array.add(option8cross2);
        corner.put(option8, option8array);



//        Pair<PatternLockView.Dot, PatternLockView.Dot> option4 = new Pair<>(allNodes.get("row1Col0"), allNodes.get("row2Col2"));
//        Pair<PatternLockView.Dot, PatternLockView.Dot> option4cross1 = new Pair<>(allNodes.get("row2Col0"), allNodes.get("row0Col1"));
//        Pair<PatternLockView.Dot, PatternLockView.Dot> option4cross2 = new Pair<>(allNodes.get("row2Col0"), allNodes.get("row1Col1"));
//        Pair<PatternLockView.Dot, PatternLockView.Dot> option4cross3 = new Pair<>(allNodes.get("row2Col0"), allNodes.get("row1Col2"));
//        Pair<PatternLockView.Dot, PatternLockView.Dot> option4cross4 = new Pair<>(allNodes.get("row2Col1"), allNodes.get("row1Col2"));
//        Pair<PatternLockView.Dot, PatternLockView.Dot> option4cross5 = new Pair<>(allNodes.get("row2Col1"), allNodes.get("row0Col2"));
//        Pair<PatternLockView.Dot, PatternLockView.Dot> option4cross6 = new Pair<>(allNodes.get("row2Col1"), allNodes.get("row0Col0"));
//        Pair<PatternLockView.Dot, PatternLockView.Dot> option4cross7 = new Pair<>(allNodes.get("row2Col1"), allNodes.get("row1Col1"));
//        ArrayList<Pair<PatternLockView.Dot, PatternLockView.Dot>> option4array = new ArrayList<>();
//        option4array.add(option4cross1);
//        option4array.add(option4cross2);
//        option4array.add(option4cross3);
//        option4array.add(option4cross4);
//        option4array.add(option4cross5);
//        option4array.add(option4cross6);
//        option4array.add(option4cross7);
//        corner.put(option4, option4array);
//
//        Pair<PatternLockView.Dot, PatternLockView.Dot> option5 = new Pair<>(allNodes.get("row1Col0"), allNodes.get("row0Col2"));
//        Pair<PatternLockView.Dot, PatternLockView.Dot> option5cross1 = new Pair<>(allNodes.get("row0Col0"), allNodes.get("row1Col2"));
//        Pair<PatternLockView.Dot, PatternLockView.Dot> option5cross2 = new Pair<>(allNodes.get("row0Col0"), allNodes.get("row1Col1"));
//        Pair<PatternLockView.Dot, PatternLockView.Dot> option5cross3 = new Pair<>(allNodes.get("row0Col0"), allNodes.get("row2Col1"));
//        Pair<PatternLockView.Dot, PatternLockView.Dot> option5cross4 = new Pair<>(allNodes.get("row0Col1"), allNodes.get("row1Col2"));
//        Pair<PatternLockView.Dot, PatternLockView.Dot> option5cross5 = new Pair<>(allNodes.get("row0Col1"), allNodes.get("row2Col2"));
//        Pair<PatternLockView.Dot, PatternLockView.Dot> option5cross6 = new Pair<>(allNodes.get("row0Col1"), allNodes.get("row1Col1"));
//        Pair<PatternLockView.Dot, PatternLockView.Dot> option5cross7 = new Pair<>(allNodes.get("row0Col1"), allNodes.get("row2Col0"));
//        ArrayList<Pair<PatternLockView.Dot, PatternLockView.Dot>> option5array = new ArrayList<>();
//        option5array.add(option5cross1);
//        option5array.add(option5cross2);
//        option5array.add(option5cross3);
//        option5array.add(option5cross4);
//        option5array.add(option5cross5);
//        option5array.add(option5cross6);
//        option5array.add(option5cross7);
//        corner.put(option5, option5array);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        makeNodeMap();
        makeCrossSectionsCorner();
        makeCrossSectionsSideMiddle();
        makeCornerList();
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
