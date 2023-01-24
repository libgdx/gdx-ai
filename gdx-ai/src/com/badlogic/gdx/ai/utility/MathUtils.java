package com.badlogic.gdx.ai.utility;

/**
 * Created by felix on 8/4/2017.
 */
public class MathUtils {

    public static float Clamp01(float x){
       return Math.max(0, Math.min(x, 1));
    }
}
