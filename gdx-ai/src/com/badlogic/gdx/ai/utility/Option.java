package com.badlogic.gdx.ai.utility;

import com.badlogic.gdx.ai.utility.consideration.CompositeConsideration;
import com.badlogic.gdx.ai.utility.measure.WeightedMetrics;

/**
 * Created by felix on 8/6/2017.
 */
public class Option<E> extends CompositeConsideration<E>{

    Action<E> action;

    public Option(){
        init();
    }

    void init() {
        setWeight(1.0f);
        setMeasure(new WeightedMetrics());
    }

    public Action<E> getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public void consider(E context) {
        if(action.isInCooldown()) {
            setUtility(new Utility(0.0f, getWeight(), getRank()));
        }
        super.consider(context);
    }

}
