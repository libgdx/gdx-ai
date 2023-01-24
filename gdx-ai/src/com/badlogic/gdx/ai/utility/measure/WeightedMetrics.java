package com.badlogic.gdx.ai.utility.measure;

import com.badlogic.gdx.ai.utility.Utility;
import com.badlogic.gdx.math.MathUtils;

import java.util.List;

/**
 * Created by felix on 8/6/2017.
 */
/// <summary>
/// Calculates the l-p weighted metrics <see cref="https://en.wikipedia.org/wiki/Lp_space"/>.
/// </summary>
/// <seealso cref="T:Crystal.IMeasure" />
public class WeightedMetrics implements Measure {

    float oneOverP;
    float p;

    public WeightedMetrics(){
        setP(2f);
    }

    /// <summary>
    /// The minimum value for <see cref="T:Crystal.ConstrainedWeightedMetrics.PNorm"/>.
    /// </summary>
    public static final float PNormMin = 1.0f;

    /// <summary>

    public float getP() {
        return p;
    }

    public void setP(float p) {
        this.p = p;
        if(p> PNormMax){
           this.p = PNormMax;
        }

        if( p < PNormMin){
            this.p = PNormMin;
        }

        oneOverP = 1.0f / p;
    }

    /// The minimum value for <see cref="T:Crystal.ConstrainedWeightedMetrics.PNorm"/>.
    /// </summary>
    public static final float PNormMax = 10000.0f;
    static final double EPSILON = 0.00001;

    @Override
    public float calculate(List<Utility> elements) {
        int count = elements.size();
        if(count == 0)
            return 0.0f;

        float wsum = 0.0f;
        for(Utility utility : elements) {
            wsum += utility.getWeight();
        }


        if(MathUtils.isEqual(0,wsum)) {
            return 0;
        }


        //float[] vlist = new float[count];
        float sum = 0;
        for(int i = 0; i < elements.size();i++) {
            Utility utility = elements.get(i);
            float v = utility.getWeight() / wsum * (float)Math.pow(utility.getValue(), p);
            sum += v;
        }

        float res = (float)Math.pow(sum, oneOverP);

        return res;
    }
}
