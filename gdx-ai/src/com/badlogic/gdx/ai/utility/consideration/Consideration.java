package com.badlogic.gdx.ai.utility.consideration;

import com.badlogic.gdx.ai.utility.MathUtils;
import com.badlogic.gdx.ai.utility.Utility;
import com.badlogic.gdx.ai.utility.evaluator.Evaluator;

/**
 * Created by felix on 8/4/2017.
 */
public abstract class Consideration<E> {

    float rank = 0f;
    float weight = 1f;
    String name;
    protected Evaluator evaluator;
    protected Utility utility;


    public Consideration(String name, float rank){
        this.name = name;
        this.rank = rank;
    }


    public abstract void consider(E context);

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = MathUtils.Clamp01(weight);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Evaluator getEvaluator() {
        return evaluator;
    }

    public void setEvaluator(Evaluator evaluator) {
        this.evaluator = evaluator;
    }

    public float getRank() {
        return rank;
    }


}
