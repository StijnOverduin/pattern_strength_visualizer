//NOTICE: This file has been changed from the source



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
import android.widget.CheckBox;
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
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private PatternLockView mPatternLockView;

    private Map<String, PatternLockView.Dot> allNodes = new HashMap<>();
    private Map<PatternLockView.Dot, String> suggestionNodes = new HashMap<>();
    private Map<Pair<PatternLockView.Dot, PatternLockView.Dot>, ArrayList<Pair<PatternLockView.Dot, PatternLockView.Dot>>> corner = new HashMap<>();
    private ArrayList<PatternLockView.Dot> cornerNodes = new ArrayList<>();
    private boolean suggestionFunctionSwitch = true;
    private PatternLockViewListener mPatternLockViewListener = new PatternLockViewListener() {


        //Function that gets fired after the user touches the first node of the pattern.
        @Override
        public void onStarted() {
            Log.d(getClass().getName(), "Pattern drawing started");
            TextView strengthText = (TextView) findViewById(R.id.strengthText);
            strengthText.setText("");
            ProgressBar progressBar = (ProgressBar) findViewById(R.id.strengthMeter);
            progressBar.setProgress(0);
            TextView patternInfo = (TextView) findViewById(R.id.patternText);
            patternInfo.setText("");

        }

        //Function that updates everytime the users picks a new node.
        @Override
        public void onProgress(List<PatternLockView.Dot> progressPattern) {
            Log.d(getClass().getName(), "Pattern progress: " +
                    PatternLockUtils.patternToString(mPatternLockView, progressPattern));
            //TextView patternInfo = (TextView) findViewById(R.id.patternText);
            //patternInfo.setText("" + progressPattern);
        }


        //Function that fires when the user completes their pattern by letting go of the screen.
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onComplete(List<PatternLockView.Dot> pattern) {
            TextView patternInfo = (TextView) findViewById(R.id.patternText);
            Log.d(getClass().getName(), "Pattern complete: " +
                    PatternLockUtils.patternToString(mPatternLockView, pattern));

            //Refresh button that resets MainActivity
            Button button = (Button) findViewById(R.id.refreshBtn);
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = getIntent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    finish();
                    startActivity(intent);
                }
            });

            double strength = calculateStrength(pattern);

            if(pattern.size() > 3) {

                ProgressBar progressBar = (ProgressBar) findViewById(R.id.strengthMeter);
                TextView strengthText = (TextView) findViewById(R.id.strengthText);
                progressBar.setMax(14);

                if(strength < 5.5) {
                    progressBar.setProgressTintList(ColorStateList.valueOf(Color.RED));
                    strengthText.setText("WEAK");
                    progressBar.setProgress((int) strength);
                } else if(strength < 9) {
                    progressBar.setProgressTintList(ColorStateList.valueOf(Color.YELLOW));
                    strengthText.setText("MEDIUM");
                    progressBar.setProgress((int) strength);
                } else if(strength < 14) {
                    progressBar.setProgressTintList(ColorStateList.valueOf(Color.GREEN));
                    strengthText.setText("STRONG");
                    progressBar.setProgress((int) strength);
                } else {
                    progressBar.setProgressTintList(ColorStateList.valueOf(Color.GREEN));
                    progressBar.setProgress((int) strength);
                    strengthText.setText("VERY STRONG");
                }
            }

            CheckBox checkBox = (CheckBox) findViewById(R.id.checkBoxSuggestions);
            suggestionFunctionSwitch = checkBox.isChecked();

            //Suggestion only triggers when these constraints are met.
            if(pattern.size() > 3 && pattern.size() < 9 && strength < 12 && suggestionFunctionSwitch) {
                List<PatternLockView.Dot> reachable = reachableNodes(pattern);
                PatternLockView.Dot suggestion = giveSuggestion(pattern, reachable);
                patternInfo.setText("Suggested you add the: " + suggestionNodes.get(suggestion));
            }


        }

        @Override
        public void onCleared() {
            Log.d(getClass().getName(), "Pattern has been cleared");
        }
    };

    //Creates the MainActiviy and configures the settings for the patternLockView such as the number and color of the nodes.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        CheckBox checkBox = (CheckBox) findViewById(R.id.checkBoxSuggestions);
        checkBox.setChecked(true);
        makeNodeMap();
        makeSuggestionMap();
        makeCrossSectionsCorner();
        makeCrossSectionsSideMiddle();
        makeCrossSectionsMiddle();
        makeCornerList();
        mPatternLockView = (PatternLockView) findViewById(R.id.patter_lock_view);
        mPatternLockView.setDotCount(3);
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

    //Calculates a suggested node given the pattern and the reachable nodes.
    public PatternLockView.Dot giveSuggestion(List<PatternLockView.Dot> pattern, List<PatternLockView.Dot> reachable) {
        double highestStrength = 0;
        List<PatternLockView.Dot> sameStrength = new ArrayList<>();
        PatternLockView.Dot suggestedDot;
        for(PatternLockView.Dot node : reachable) {
            pattern.add(node);
            double newStrength = calculateStrength(pattern);
            if(highestStrength <= newStrength) {
                if (Math.abs(highestStrength - newStrength) <= 1) {
                    sameStrength.add(node);
                } else {
                    sameStrength.removeAll(sameStrength);
                    sameStrength.add(node);
                }
                highestStrength = newStrength;
            }
            pattern.remove(node);
        }

        int rnd = new Random().nextInt(sameStrength.size());
        suggestedDot = sameStrength.get(rnd);
        return suggestedDot;
    }


    //Calculates the reachable nodes given the pattern.
    public List<PatternLockView.Dot> reachableNodes(List<PatternLockView.Dot> pattern) {
        PatternLockView.Dot lastNode = pattern.get(pattern.size() - 1);
        Pair<PatternLockView.Dot, PatternLockView.Dot> oppositeLR = new Pair<>(allNodes.get("row1Col0"), allNodes.get("row1Col2")); //left and right middle node pair
        Pair<PatternLockView.Dot, PatternLockView.Dot> oppositeUD = new Pair<>(allNodes.get("rowCol1"), allNodes.get("row2Col1")); //upper and lower middle node pair
        Map<String, PatternLockView.Dot> nodes;
        List<PatternLockView.Dot> possibleNodeList = new ArrayList<>();
        if(cornerNodes.contains(lastNode)) { //is it a corner node?
            for(Map.Entry<String, PatternLockView.Dot> dot1 : allNodes.entrySet()) {
                if(!pattern.contains(dot1.getValue())) {
                    if(!cornerNodes.contains(dot1.getValue())) {
                        possibleNodeList.add(dot1.getValue());
                    } else {
                        if(lastNode.equals(allNodes.get("row0Col0"))) { //is it left upper corner?
                            nodes = allNodes;
                        } else if(lastNode.equals(allNodes.get("row2Col0"))) { // is it left bottom corner?
                            nodes = transformMap(allNodes, "h");  //transform all the nodes in the map horizontally
                        } else if(lastNode.equals(allNodes.get("row0Col2"))) { //is it right upper corner?
                            nodes = transformMap(allNodes, "v"); //transform all the nodes in the map vertically
                        } else {
                            nodes = transformMap(transformMap(allNodes, "h"), "v");//transform all the nodes in the map horizontally then vertically
                        }

                        //Checks for every corner if the conditions are met to reach the other corner.
                        if(dot1.getValue().equals(nodes.get("row2Col0")) && pattern.contains(nodes.get("row1Col0"))) {
                            possibleNodeList.add(dot1.getValue());
                        } else if(dot1.getValue().equals(nodes.get("row2Col2")) && pattern.contains(nodes.get("row1Col1"))) {
                            possibleNodeList.add(dot1.getValue());
                        } else if (dot1.getValue().equals(nodes.get("row0Col2")) && pattern.contains(nodes.get("row0Col1"))) {
                            possibleNodeList.add(dot1.getValue());
                        }
                    }
                }
            }

        } else if(lastNode == allNodes.get("row1Col1")) { //is it the middle node?
            for(Map.Entry<String, PatternLockView.Dot> dot1 : allNodes.entrySet()) {
                if(!pattern.contains(dot1.getValue())) { //check which nodes not in the pattern
                    possibleNodeList.add(dot1.getValue());
                }
            }

        } else { //nodes between corner nodes
                for (Map.Entry<String, PatternLockView.Dot> dot1 : allNodes.entrySet()) {
                    if (!pattern.contains(dot1.getValue())) {
                        //Check whether the conditions are met to reach the opposite node.
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


    //Loops over the entries of the given map and transforms them either horizontally or vertically
    public Map<String, PatternLockView.Dot> transformMap(Map<String, PatternLockView.Dot> m, String type) {
        Map<String, PatternLockView.Dot> transormedMap = new HashMap<>();
        Pair<PatternLockView.Dot, PatternLockView.Dot> p;
        Pair<PatternLockView.Dot, PatternLockView.Dot> tp;
        if(type.equals("h")) {
            for(Map.Entry<String, PatternLockView.Dot> entry : m.entrySet()) {
                p = new Pair<>(entry.getValue(), entry.getValue());
                tp = transformPairverhor(p, "h");
                transormedMap.put(entry.getKey(), tp.first);
            }
        } else {
            for(Map.Entry<String, PatternLockView.Dot> entry : m.entrySet()) {
                p = new Pair<>(entry.getValue(), entry.getValue());
                tp = transformPairverhor(p, "v");
                transormedMap.put(entry.getKey(), tp.first);
            }

        }

        return transormedMap;
    }

    //Calculates the strength of a pattern given all the metrics
    public double calculateStrength(List<PatternLockView.Dot> pattern) {
        int passCountStr = passedTwiceStraight(pattern);
        int passCountDia = passedTwiceDiagonal(pattern);
        int overlaps = passCountStr + passCountDia;
        double plen = patternLength(pattern);
        double nrDots = pattern.size();
        int crossCount = crossSections(pattern);
        double strength =  (nrDots / 2) + (plen/nrDots) + Math.pow(crossCount, 2) + Math.pow((overlaps), 2);

        return strength;
    }

    //Function to count the amount of straight overlaps is the pattern.
    public int passedTwiceStraight(List<PatternLockView.Dot> pattern) {
        int count = 0;
        for(int i = 0; i + 1 < pattern.size(); i++) {
            int resRow = pattern.get(i).getRow() - pattern.get(i + 1).getRow();
            int resCol = pattern.get(i).getColumn() - pattern.get(i + 1).getColumn();
            if (resRow == -2 || resRow == 2 ) { //difference of 2 in the row?
                if(pattern.get(i).getColumn() == pattern.get(i + 1).getColumn()) { //nodes in the same column?
                    count += 1;
                }
            }
            if (resCol == 2 || resCol == -2) {
                if(pattern.get(i).getRow() == pattern.get(i + 1).getRow()) {
                    count += 1;
                }
            }
        }
        return count;
    }


    //Function to count the number of diagonal overlaps.
    public int passedTwiceDiagonal(List<PatternLockView.Dot> pattern) {
        int count = 0;
        PatternLockView.Dot leftTop = this.allNodes.get("row0Col0");
        PatternLockView.Dot leftBot = this.allNodes.get("row2Col0");
        PatternLockView.Dot rightTop = this.allNodes.get("row0Col2");
        PatternLockView.Dot rightBot = this.allNodes.get("row2Col2");
        for (int i = 0; i + 1 < pattern.size(); i++) {
            if (pattern.get(i).equals(leftTop) && pattern.get(i + 1).equals(rightBot) || pattern.get(i).equals(rightBot) && pattern.get(i + 1).equals(leftTop)) { //checks whether two following nodes in the pattern are opposite corners
                count += 1;
            }
            if (pattern.get(i).equals(rightTop) && pattern.get(i + 1).equals(leftBot) || pattern.get(i).equals(leftBot) && pattern.get(i + 1).equals(rightTop)) { //checks whether two following nodes in the pattern are opposite corners
                count += 1;
            }
        }
        return count;
    }


    //Calculates the length of the given pattern.
    public double patternLength(List<PatternLockView.Dot> pattern) {
        double plen = 0;

        for(int i = 0; i + 1 < pattern.size(); i++) {
            if(pattern.get(i).getRow() == pattern.get(i + 1).getRow()) { //same row?
                plen += Math.abs(pattern.get(i).getColumn() - pattern.get(i + 1).getColumn()); //difference in column
            } else if(pattern.get(i).getColumn() == pattern.get(i + 1).getColumn()) { //same column?
                plen += Math.abs(pattern.get(i).getRow() - pattern.get(i + 1).getRow()); //difference in row
            } else if(Math.abs(pattern.get(i).getRow() - pattern.get(i + 1).getRow()) == 1) { //short diagonal
                plen += Math.sqrt(2);
            } else if(Math.abs(pattern.get(i).getRow() - pattern.get(i + 1).getRow()) == 1 && Math.abs(pattern.get(i).getColumn() - pattern.get(i + 1).getColumn()) == 2) { //long diagonal (difference of 1 in the row and 2 in the column
                plen += Math.sqrt(5);
            } else if (Math.abs(pattern.get(i).getRow() - pattern.get(i + 1).getRow()) == 2 && Math.abs(pattern.get(i).getColumn() - pattern.get(i + 1).getColumn()) == 1) { //long diagonal (difference of 2 in the row and 1 in the column
                plen += Math.sqrt(5);
            } else if (Math.abs(pattern.get(i).getRow() - pattern.get(i + 1).getRow()) == 2 && Math.abs(pattern.get(i).getColumn() - pattern.get(i + 1).getColumn()) == 2) {//diagonal from corner to corner
                plen += 2*Math.sqrt(2);
            }
        }

        return plen;
     }


     // Counting the amount of intersections in the pattern.
    public int crossSections(List<PatternLockView.Dot> pattern) {
       int count = 0;
       for(int i = 0; i + 1 < pattern.size(); i++) {
           for(int j = 0; j + 1 < pattern.size(); j++ ) {
               PatternLockView.Dot node1b = pattern.get(i);
               PatternLockView.Dot node1e = pattern.get(i + 1);
               PatternLockView.Dot node2b = pattern.get(j);
               PatternLockView.Dot node2e = pattern.get(j + 1);
               Pair<PatternLockView.Dot, PatternLockView.Dot> line1Pair = new Pair<>(node1b, node1e);
               Pair<PatternLockView.Dot, PatternLockView.Dot> line2Pair = new Pair<>(node2b, node2e);
               if(!equalPairsUndirected(line1Pair, line2Pair)) { //lines are not the same?
                   for(Map.Entry<Pair<PatternLockView.Dot, PatternLockView.Dot>, ArrayList<Pair<PatternLockView.Dot, PatternLockView.Dot>>> entry : corner.entrySet()) { //Loop through the map with all the unique lines that cause unique intersections.
                       //Series of if statements that check whether the line, be it a transformed one or not, is in the Map. If it is check if the second line is in the values of that entry.
                           if(equalPairsDirected(entry.getKey(), line1Pair)) {
                               if(pairInArray(entry.getValue(), line2Pair)) {
                                   count += 1;
                               }
                           } else if(equalPairsDirected(transformPairverhor(entry.getKey(), "v"), line1Pair)) { //Transform the line from the map vertically and check if the line from the pattern matches.
                               if (pairInArray(transformPairArray(entry.getValue(), "v"), line2Pair)) { //check if the second line from the pattern is in the values of that map entry.
                                   count += 1;
                               }
                           } else if(equalPairsDirected(transformPairverhor(entry.getKey(), "h"), line1Pair)) {
                               if(pairInArray(transformPairArray(entry.getValue(), "h"), line2Pair)) {
                                   count += 1;
                               }
                           } else if(equalPairsDirected(transformPairverhor(transformPairDiag(entry.getKey(), "lbtr"), "v"), line1Pair)) { //lbtr = left bot top right diagonal tranform.
                               if(pairInArray(transformPairArray(transformPairArray(entry.getValue(), "lbtr"), "v"), line2Pair)) {
                                   count += 1;
                               }
                           } else if(equalPairsDirected(transformPairverhor(transformPairDiag(entry.getKey(), "lbtr"), "h"), line1Pair)) {
                               if(pairInArray(transformPairArray(transformPairArray(entry.getValue(), "lbtr"), "h"), line2Pair)) {
                                   count += 1;
                               }
                           } else if(equalPairsDirected(transformPairDiag(transformPairDiag(entry.getKey(), "lbtr"), "rbtl"), line1Pair)) {
                               if(pairInArray(transformPairArray(transformPairArray(entry.getValue(), "lbtr"), "rbtl"), line2Pair)) {
                                   count += 1;
                               }
                           } else if(equalPairsDirected(transformPairDiag(entry.getKey(), "lbtr"), line1Pair)) {
                               if (pairInArray(transformPairArray(entry.getValue(), "lbtr"), line2Pair)) {
                                   count += 1;
                               }
                           } else if(equalPairsDirected(transformPairDiag(entry.getKey(), "rbtl"), line1Pair)) { //rbtl = right bot top left diagonal transform.
                               if(pairInArray(transformPairArray(entry.getValue(), "rbtl"), line2Pair)) {
                                   count += 1;
                               }
                           }
                       }

                   }
               }


           }

       count = count/2; //dividing by 2 because we counted every intersection twice.

       return count;
    }


    //Checks if 2 lines are the same disregarding direction.
    public boolean equalPairsUndirected(Pair<PatternLockView.Dot, PatternLockView.Dot> pairOne, Pair<PatternLockView.Dot, PatternLockView.Dot> pairTwo) {
        boolean result = false;

        if(pairOne.first.equals(pairTwo.first) && pairOne.second.equals(pairTwo.second) ) {
            result = true;
        } else if (pairOne.first.equals(pairTwo.second) && pairOne.second.equals(pairTwo.first)) {
            result = true;
        }

        return result;
    }


    //Checks if 2 lines are the same including direction
    public boolean equalPairsDirected(Pair<PatternLockView.Dot, PatternLockView.Dot> pairOne, Pair<PatternLockView.Dot, PatternLockView.Dot> pairTwo) {
        boolean result = false;

        if(pairOne.first.equals(pairTwo.first) && pairOne.second.equals(pairTwo.second) ) {
            result = true;
        }

        return result;
    }

    //Checks if the given pair is in the given array. Needs to disregard direction so equalPairsUndirected is used.
    public boolean pairInArray(ArrayList<Pair<PatternLockView.Dot, PatternLockView.Dot>> array, Pair<PatternLockView.Dot, PatternLockView.Dot> pair) {
        boolean result = false;

        for(Pair<PatternLockView.Dot, PatternLockView.Dot> pairs : array) {
            if(equalPairsUndirected(pairs, pair)) {
                result = true;
            }
        }

        return result;
    }


    //Transforms a pair array horizontally, vertically or diagonally depending on the given type.
    public ArrayList<Pair<PatternLockView.Dot, PatternLockView.Dot>> transformPairArray(ArrayList<Pair<PatternLockView.Dot, PatternLockView.Dot>> array, String type) {
        ArrayList<Pair<PatternLockView.Dot, PatternLockView.Dot>> transformed = new ArrayList<>();
        for(Pair<PatternLockView.Dot, PatternLockView.Dot> pair : array) {
            if(type.equals("h") || type.equals("v")) { //Either horizontal or diagonal tansform.
                Pair<PatternLockView.Dot, PatternLockView.Dot> transPair = transformPairverhor(pair, type);
                transformed.add(transPair);
            } else { //will be transformed diagonally
                Pair<PatternLockView.Dot, PatternLockView.Dot> transPair = transformPairDiag(pair, type);
                transformed.add(transPair);
            }

        }

        return transformed;
    }


    //Tranforms pairs (lines) either vertically or horizontally
    public Pair<PatternLockView.Dot, PatternLockView.Dot> transformPairverhor(Pair<PatternLockView.Dot, PatternLockView.Dot> pair, String type) {
        int firstn;
        int secondn;
        PatternLockView.Dot firstDot = pair.first;
        PatternLockView.Dot secondDot = pair.second;

        if(type.equals("v")) {
            firstn = pair.first.getColumn();
            secondn = pair.second.getColumn();

            // For both nodes in the pair checks whether the column is 2 or 0, if that is the case 0 becomes 2 and 2 becomes 0.
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

            // Same for vertical but now for the row.
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


    //Transforms a pair (line) diagonally.
    public Pair<PatternLockView.Dot, PatternLockView.Dot> transformPairDiag(Pair<PatternLockView.Dot, PatternLockView.Dot> pair, String type) {
        int firstCol = pair.first.getColumn();
        int secondCol = pair.second.getColumn();
        int firstRow = pair.first.getRow();
        int secondRow = pair.second.getRow();
        PatternLockView.Dot firstDot = pair.first;
        PatternLockView.Dot secondDot = pair.second;

        if(type.equals("lbtr")) { //ltbr = left bot top right

            // Differnt checks to determine what has to be changed for the node to be transformed.
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
        } else if(type.equals("rbtl")){ // rbtl = right bot top left
            // Same checks but now for the other diagonal.
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


    // Make a map with all the nodes in it.
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


    // Makes a map of the corners which makes it easier to check if a node is a corner node.
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


    //Adds the unigue lines of the corner to the map that are needed to calculate the number of unique intersections.
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
        Pair<PatternLockView.Dot, PatternLockView.Dot> option2cross8 = new Pair<>(allNodes.get("row1Col0"), allNodes.get("row1Col2"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option2cross9 = new Pair<>(allNodes.get("row2Col0"), allNodes.get("row0Col2"));
        ArrayList<Pair<PatternLockView.Dot, PatternLockView.Dot>> option2array = new ArrayList<>();
        option2array.add(option2cross1);
        option2array.add(option2cross2);
        option2array.add(option2cross3);
        option2array.add(option2cross4);
        option2array.add(option2cross5);
        option2array.add(option2cross6);
        option2array.add(option2cross7);
        option2array.add(option2cross8);
        option2array.add(option2cross9);
        corner.put(option2, option2array);

        Pair<PatternLockView.Dot, PatternLockView.Dot> option3 = new Pair<>(allNodes.get("row0Col0"), allNodes.get("row2Col2"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option3cross1 = new Pair<>(allNodes.get("row2Col1"), allNodes.get("row1Col2"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option3cross2 = new Pair<>(allNodes.get("row2Col1"), allNodes.get("row0Col2"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option3cross3 = new Pair<>(allNodes.get("row2Col0"), allNodes.get("row1Col2"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option3cross4 = new Pair<>(allNodes.get("row2Col0"), allNodes.get("row0Col1"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option3cross5 = new Pair<>(allNodes.get("row1Col0"), allNodes.get("row0Col1"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option3cross6 = new Pair<>(allNodes.get("row1Col0"), allNodes.get("row0Col2"));
        ArrayList<Pair<PatternLockView.Dot, PatternLockView.Dot>> option3array = new ArrayList<>();
        option3array.add(option3cross1);
        option3array.add(option3cross2);
        option3array.add(option3cross3);
        option3array.add(option3cross4);
        option3array.add(option3cross5);
        option3array.add(option3cross6);
        corner.put(option3, option3array);


    }

    //Adds the unique lines to the map that are needed to calculate the unique intersections of the nodes between the corner nodes.
    public void makeCrossSectionsSideMiddle() {
        Pair<PatternLockView.Dot, PatternLockView.Dot> option1 = new Pair<>(allNodes.get("row1Col0"), allNodes.get("row0Col1"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option1cross1 = new Pair<>(allNodes.get("row2Col1"), allNodes.get("row0Col0"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option1cross2 = new Pair<>(allNodes.get("row1Col1"), allNodes.get("row0Col0"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option1cross3 = new Pair<>(allNodes.get("row1Col2"), allNodes.get("row0Col0"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option1cross4 = new Pair<>(allNodes.get("row0Col0"), allNodes.get("row2Col2"));
        ArrayList<Pair<PatternLockView.Dot, PatternLockView.Dot>> option1array = new ArrayList<>();
        option1array.add(option1cross1);
        option1array.add(option1cross2);
        option1array.add(option1cross3);
        option1array.add(option1cross4);
        corner.put(option1, option1array);

        Pair<PatternLockView.Dot, PatternLockView.Dot> option2 = new Pair<>(allNodes.get("row1Col0"), allNodes.get("row1Col1"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option2cross1 = new Pair<>(allNodes.get("row2Col1"), allNodes.get("row0Col0"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option2cross2 = new Pair<>(allNodes.get("row2Col0"), allNodes.get("row0Col1"));
        ArrayList<Pair<PatternLockView.Dot, PatternLockView.Dot>> option2array = new ArrayList<>();
        option2array.add(option2cross1);
        option2array.add(option2cross2);
        corner.put(option2, option2array);

        Pair<PatternLockView.Dot, PatternLockView.Dot> option5 = new Pair<>(allNodes.get("row1Col0"), allNodes.get("row0Col2"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option5cross1 = new Pair<>(allNodes.get("row0Col0"), allNodes.get("row1Col2"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option5cross2 = new Pair<>(allNodes.get("row0Col0"), allNodes.get("row1Col1"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option5cross3 = new Pair<>(allNodes.get("row0Col0"), allNodes.get("row2Col1"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option5cross4 = new Pair<>(allNodes.get("row0Col1"), allNodes.get("row1Col2"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option5cross5 = new Pair<>(allNodes.get("row0Col1"), allNodes.get("row2Col2"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option5cross6 = new Pair<>(allNodes.get("row0Col1"), allNodes.get("row1Col1"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option5cross7 = new Pair<>(allNodes.get("row0Col1"), allNodes.get("row2Col0"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option5cross8 = new Pair<>(allNodes.get("row0Col0"), allNodes.get("row2Col2"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option5cross9 = new Pair<>(allNodes.get("row0Col1"), allNodes.get("row2Col1"));
        ArrayList<Pair<PatternLockView.Dot, PatternLockView.Dot>> option5array = new ArrayList<>();
        option5array.add(option5cross1);
        option5array.add(option5cross2);
        option5array.add(option5cross3);
        option5array.add(option5cross4);
        option5array.add(option5cross5);
        option5array.add(option5cross6);
        option5array.add(option5cross7);
        option5array.add(option5cross8);
        option5array.add(option5cross9);
        corner.put(option5, option5array);

        Pair<PatternLockView.Dot, PatternLockView.Dot> option6 = new Pair<>(allNodes.get("row1Col0"), allNodes.get("row1Col2"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option6cross1 = new Pair<>(allNodes.get("row0Col1"), allNodes.get("row2Col2"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option6cross2 = new Pair<>(allNodes.get("row0Col2"), allNodes.get("row2Col1"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option6cross3 = new Pair<>(allNodes.get("row0Col0"), allNodes.get("row2Col1"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option6cross4 = new Pair<>(allNodes.get("row0Col1"), allNodes.get("row2Col0"));
        ArrayList<Pair<PatternLockView.Dot, PatternLockView.Dot>> option6array = new ArrayList<>();
        option6array.add(option6cross1);
        option6array.add(option6cross2);
        option6array.add(option6cross3);
        option6array.add(option6cross4);
        corner.put(option6, option6array);


    }


    //Adds the unique lines to the map that are needed to calculate the unique intersections of the middle node.
    public void makeCrossSectionsMiddle() {
        Pair<PatternLockView.Dot, PatternLockView.Dot> option1 = new Pair<>(allNodes.get("row1Col1"), allNodes.get("row0Col0"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option1cross1 = new Pair<>(allNodes.get("row2Col0"), allNodes.get("row0Col1"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option1cross2 = new Pair<>(allNodes.get("row1Col0"), allNodes.get("row0Col1"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option1cross3 = new Pair<>(allNodes.get("row1Col0"), allNodes.get("row0Col2"));
        ArrayList<Pair<PatternLockView.Dot, PatternLockView.Dot>> option1array = new ArrayList<>();
        option1array.add(option1cross1);
        option1array.add(option1cross2);
        option1array.add(option1cross3);
        corner.put(option1, option1array);

        Pair<PatternLockView.Dot, PatternLockView.Dot> option2 = new Pair<>(allNodes.get("row1Col1"), allNodes.get("row1Col0"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option2cross1 = new Pair<>(allNodes.get("row2Col1"), allNodes.get("row0Col0"));
        Pair<PatternLockView.Dot, PatternLockView.Dot> option2cross2 = new Pair<>(allNodes.get("row2Col0"), allNodes.get("row0Col1"));
        ArrayList<Pair<PatternLockView.Dot, PatternLockView.Dot>> option2array = new ArrayList<>();
        option2array.add(option2cross1);
        option2array.add(option2cross2);
        corner.put(option2, option2array);
    }

    //Map to display the nodes in text for the suggestion function.
    public void makeSuggestionMap() {
        PatternLockView.Dot row0Col0 = new PatternLockView.Dot(0 ,0);
        PatternLockView.Dot row0Col1 = new PatternLockView.Dot(0 ,1);
        PatternLockView.Dot row0Col2 = new PatternLockView.Dot(0 ,2);
        PatternLockView.Dot row1Col0 = new PatternLockView.Dot(1 ,0);
        PatternLockView.Dot row1Col1 = new PatternLockView.Dot(1 ,1);
        PatternLockView.Dot row1Col2 = new PatternLockView.Dot(1 ,2);
        PatternLockView.Dot row2Col0 = new PatternLockView.Dot(2 ,0);
        PatternLockView.Dot row2Col1 = new PatternLockView.Dot(2 ,1);
        PatternLockView.Dot row2Col2 = new PatternLockView.Dot(2 ,2);

        this.suggestionNodes.put(row0Col0, "top left corner node");
        this.suggestionNodes.put(row0Col1, "top middle node" );
        this.suggestionNodes.put(row0Col2, "top right corner");
        this.suggestionNodes.put(row1Col0, "left middle node");
        this.suggestionNodes.put(row1Col1, "middle node");
        this.suggestionNodes.put(row1Col2, "right middle node");
        this.suggestionNodes.put(row2Col0, "bottom left corner node");
        this.suggestionNodes.put(row2Col1, "bottom middle node");
        this.suggestionNodes.put(row2Col2, "bottom right corner node");
    }

}
