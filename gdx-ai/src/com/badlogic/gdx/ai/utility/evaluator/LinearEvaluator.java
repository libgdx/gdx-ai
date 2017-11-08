package com.badlogic.gdx.ai.utility.evaluator;

import com.badlogic.gdx.ai.utility.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by felix on 8/4/2017.
 * The LinearEvaluator returns a normalized utility value based on a linear function.
 ///   <see href="https://www.desmos.com/calculator/esibujxim4">Power</see> for an interactive
 ///   plot.
 */
public class LinearEvaluator extends Evaluator {
    float dyOverDx;

    public LinearEvaluator(){
        super();
        init();
    }


    public LinearEvaluator(Vector2 ptA, Vector2 ptB) {
        super(ptA, ptB);
        init();
    }



    void init() {
        dyOverDx = (this.yb - this.ya) / (this.xb - this.xa);
    }
    @Override
    public float evaluate(float x) {
        float raw = this.ya + dyOverDx * (x - xa);
        return MathUtils.Clamp01(raw);
    }
}
