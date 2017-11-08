package com.badlogic.gdx.ai.utility.evaluator;

import com.badlogic.gdx.math.Vector2;

/**
 * Created by felix on 8/4/2017.
 */
public abstract class Evaluator {

    float xa;
    float xb;
    float ya;
    float yb;

    protected Evaluator() {
        init(0.0f, 0.0f, 1.0f, 1.0f);
    }

    protected Evaluator(Vector2 ptA, Vector2 ptB) {
        init(ptA.x, ptA.y, ptB.x, ptB.y);
    }

    void init(float xA, float yA, float xB, float yB) {

        //todo
// if(CrMath.AeqB(xA, xB))
//            throw new EvaluatorDxZeroException();
//        if(xA > xB)
//            throw new EvaluatorXaGreaterThanXbException();

        xa = xA;
        xb = xB;
        ya = Math.max(0, Math.min(yA, 1));
        yb = Math.max(0, Math.min(yB, 1));
    }

    public abstract float evaluate(float x);
}
