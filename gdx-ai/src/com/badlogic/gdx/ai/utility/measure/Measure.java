package com.badlogic.gdx.ai.utility.measure;

import com.badlogic.gdx.ai.utility.Utility;

import java.util.List;

/**
 * Created by felix on 8/6/2017.
 */
public interface Measure {

    /// <summary>
    ///   Calculate the measure for the given set of elements.
    /// </summary>
    float calculate(List<Utility> elements);
}
