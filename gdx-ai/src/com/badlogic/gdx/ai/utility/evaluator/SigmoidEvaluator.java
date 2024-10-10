package com.badlogic.gdx.ai.utility.evaluator;

import com.badlogic.gdx.math.Vector2;

/**
 * Created by felix on 8/6/2017.
 *
 *  The SigmoidEvaluator returns a normalized utility based on a sigmoid function that has
 ///   effectively 1 parameter ( -0.99999f leq k leq 0.99999f ) bounded by the box defined by PtA and PtB with
 ///   PtA.x being strictly less than PtB.x!
 ///   <see href="https://www.desmos.com/calculator/u4qmty3ffk">Parametrized Sigmoid</see> for an interactive
 ///   plot.
 */
public class SigmoidEvaluator extends Evaluator {

    float dyOverTwo;
    float k;
    float oneMinusK;
    float twoOverDx;
    float xMean;
    float yMean;


    public SigmoidEvaluator(){
        k = -0.6f;
        init();
    }

    public SigmoidEvaluator(Vector2 ptA, Vector2 ptB, float k){
        super(ptA, ptB);

        if(k < minK){
            k = minK;
        }
        if(k > maxK){
            k = maxK;
        }
        this.k = k;
        init();
    }

    void init() {
        twoOverDx = Math.abs(2.0f / (xb - xa));
        xMean = (xa + xb) / 2.0f;
        yMean = (ya + yb) / 2.0f;
        dyOverTwo = (yb - ya) / 2.0f;
        oneMinusK = 1.0f - k;
    }



    @Override
    public float evaluate(float x) {

        //clamp to min
        if(x < xa){
            x = xa;
        }

        //clamp to max
        if(x > xb){
            x = xb;
        }

        float cxMinusXMean = x - xMean;
        float num = twoOverDx * cxMinusXMean * oneMinusK;
        float den = k * (1 - 2 * Math.abs(twoOverDx * cxMinusXMean)) + 1;
        float val = dyOverTwo * (num / den) + yMean;
        return val;


    }

    final float minK = -0.99999f;
    final float maxK = 0.99999f;
}
