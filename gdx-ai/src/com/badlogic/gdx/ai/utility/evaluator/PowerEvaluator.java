package com.badlogic.gdx.ai.utility.evaluator;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by felix on 8/7/2017.
 * The PowerEvaluator returns a normalized utility based on a power function that has
 ///   effectively 1 parameter ( 0 leq p le 10000 ) bounded by the box defined by PtA and PtB with
 ///   PtA.x being strictly less than PtB.x!
 ///   <see href="https://www.desmos.com/calculator/ok9nse8l3u">Power</see> for an interactive
 ///   plot.
 */
public class PowerEvaluator extends Evaluator {
    public static final float MinP = 0.0f;
    public static final float MaxP = 10000f;
    float dy;
    float p;

    /// <summary>
    ///   Initializes a new instance of the <see cref="T:Crystal.PowerEvaluator"/> class.
    ///   <see href="https://www.desmos.com/calculator/ok9nse8l3u">Power</see> for an interactive
    ///   plot.
    /// </summary>
//    public PowerEvaluator() {
//        p = 2.0f;
//        Initialize();
//    }

    /// <summary>
    ///   Initializes a new instance of the <see cref="T:Crystal.PowerEvaluator"/> class.
    ///   <see href="https://www.desmos.com/calculator/ok9nse8l3u">Power</see> for an interactive
    ///   plot.
    /// </summary>
    /// <param name="ptA">Point a.</param>
    /// <param name="ptB">Point b.</param>
//    public PowerEvaluator(Vector2 ptA, Vector2 ptB)  {
//        p = 2.0f;
//        Initialize();
//    }
    /// <summary>
    ///   Returns the utility for the specified value x.
    /// </summary>
    /// <param name="x">The x value.</param>
    public float evaluate(float x) {
        float cx = MathUtils.clamp(x,xa,xb);
        cx = dy * (float)Math.pow((cx - xa) / (xb - xa), p) + ya;
        return cx;
    }





    /// <summary>
    ///   Initializes a new instance of the <see cref="T:Crystal.PowerEvaluator"/> class.
    ///   <see href="https://www.desmos.com/calculator/ok9nse8l3u">Power</see> for an interactive
    ///   plot.
    /// </summary>
    /// <param name="ptA">Point a.</param>
    /// <param name="ptB">Point b.</param>
    /// <param name="power">Power.</param>
    public PowerEvaluator(Vector2 ptA, Vector2 ptB, float power) {
        super(ptA,ptB);
        p = MathUtils.clamp(power,MinP, MaxP);

        Initialize();
    }

    void Initialize() {
        dy = yb - ya;
    }



}
