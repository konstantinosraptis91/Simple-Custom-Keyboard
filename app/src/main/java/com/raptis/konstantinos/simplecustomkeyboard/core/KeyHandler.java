package com.raptis.konstantinos.simplecustomkeyboard.core;

import android.util.Log;

import com.raptis.konstantinos.simplecustomkeyboard.util.DigraphType;
import com.raptis.konstantinos.simplecustomkeyboard.util.Key;
import com.raptis.konstantinos.simplecustomkeyboard.util.KeyObject;

import java.util.HashMap;

/**
 * Created by konstantinos on 17/4/2016.
 */
public class KeyHandler {

    // logcat id
    private static final String CM_LOG = "cmlog";
    private static final String kEY_LOG = "keylog";
    // variables
    public static final int BUFFER_SIZE = 15;
    private int index = 0;
    private KeyObject[] buffer = new KeyObject[BUFFER_SIZE];
    private int errorRateCounter = 0;
    public static HashMap<Integer, Key> keysMap;
    private static long currentTimeReleased, previousTimeReleased,
            currentTimePressed, previousTimePressed;
    private static KeyObject currentKey, previousKey;

    // constructor
    public KeyHandler() {
        keysMap = new HashMap<>();
        for (Key[] keyArray : KeyFactory.keys) {
            for (Key key : keyArray) {
                keysMap.put(key.getPrimaryCode(), key);
            }
        }
    }

    public void add(KeyObject keyObject) {
        // check if key primary code mach to delete button key primary code
        if (keyObject.getPrimaryCode() == Key.DELETE_BUTTON.getPrimaryCode()) {
            errorRateCounter++;
            return; // we dont want to buffer delete key
        }

        if (index >= BUFFER_SIZE) {
            buffer = new KeyObject[BUFFER_SIZE];
            index = 0;
        }
        buffer[index] = keyObject;
        index++;

        // check digraph type (only if key exists in keysMap)
        if (keysMap.containsKey(keyObject.getPrimaryCode())) {
            DigraphType digraphType = KeyFactory.getDigraphType(keyObject);
            keyObject.setDigraphType(digraphType);
        }

    }

    // index always point to the first available position in buffer
    public int getIndex() {
        return index;
    }

    // get buffer
    public KeyObject[] getBuffer() {
        return buffer;
    }

    // get error rate counter
    public int getErrorRateCounter() {
        return errorRateCounter;
    }

    // key pressed
    public void keyPressed(int primaryCode) {
        if (currentTimePressed == 0 && previousTimePressed == 0) {
            currentTimePressed = System.nanoTime();
            previousTimePressed = System.nanoTime();
        } else {
            currentTimePressed = System.nanoTime();
        }
    }

    // key released
    public void keyReleased(int primaryCode) {
        if (currentTimeReleased == 0 && previousTimeReleased == 0) {
            currentTimeReleased = System.nanoTime();
            previousTimeReleased = System.nanoTime();
            currentKey = new KeyObject(primaryCode, null, -1, currentTimePressed, currentTimeReleased);    // negative time in order to show
            previousKey = new KeyObject(primaryCode, null, -1, previousTimePressed, previousTimeReleased);   // that there is no previous key typed
        } else {
            currentTimeReleased = System.nanoTime();
            double timePassed = (double) ((currentTimeReleased - previousTimePressed) / 1000000);  // milliseconds
            previousTimePressed = currentTimePressed;
            currentKey = new KeyObject(primaryCode, previousKey, timePassed, currentTimePressed, currentTimeReleased);
            Log.i(CM_LOG, "Time passed : " + timePassed + " ms" + "\n");
            previousTimeReleased = currentTimeReleased;
            previousKey = currentKey;
        }
        // add key object to buffer
        add(currentKey);
        Log.i(kEY_LOG, currentKey.toString());
    }

}