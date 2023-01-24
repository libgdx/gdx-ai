package com.badlogic.gdx.ai.utility.measure;

import com.badlogic.gdx.ai.utility.Utility;
import com.badlogic.gdx.math.MathUtils;

import java.util.List;

/**
 * Created by felix on 8/6/2017.
 */
public class Chebyshev implements Measure {

    public float calculate(List<Utility> elements){
        float wsum = 0.0f;
        int count = elements.size();

        if(count == 0)
            return 0.0f;

        for(Utility el : elements) {
            wsum += el.getWeight();
        }

        if(MathUtils.isEqual(0,wsum))
            return 0.0f;

        float max = 0;
        for(Utility el : elements) {
            float temp = el.getValue() * (el.getWeight() / wsum);
            if(temp > max){
                max = temp;
            }
        }
        return max;
    }
}
