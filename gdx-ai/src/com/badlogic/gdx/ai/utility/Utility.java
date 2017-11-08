package com.badlogic.gdx.ai.utility;

/**
 * Created by felix on 8/6/2017.
 */
public class Utility {
    float weight;
    float value;
    float rank = 0f;

    public Utility(float value, float weight, float rank){
        this.weight = MathUtils.Clamp01(weight);
        this.value = MathUtils.Clamp01(value);
        this.rank = rank;
    }

    public Utility(float value, float weight){
        this(value, weight,0f);
    }

    public float getWeight() {
        return  MathUtils.Clamp01(weight);
    }

     public void setWeight(float weight) {
         this.weight = weight;
    }

    public float getValue() {
        return MathUtils.Clamp01(value);
    }

    public void setValue(float value) {
        this.value = value;
    }

    public float getCombined(){
        return value * weight;
    }

    public float getRank() {
        return rank;
    }



}
