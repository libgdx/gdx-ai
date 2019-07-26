package com.badlogic.gdx.ai.utility.consideration;

import com.badlogic.gdx.ai.utility.Utility;
import com.badlogic.gdx.ai.utility.measure.Measure;
import com.badlogic.gdx.ai.utility.measure.WeightedMetrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by felix on 8/6/2017.
 */

/// An aggregate of considerations. Although it is possible to create fully functioning
/// AIs using only <see cref="T:Crystal.IConsideration"/>s, this class allows for such
/// considerations to be combined. This is quite useful as it can reduce the complexity
/// of individual considerations allowing them to be seen as building blocks.
public class CompositeConsideration<E>  {
    List<Consideration<E>> considerations = new ArrayList<Consideration<E>>();
    List<Utility> considerationUtilities = new ArrayList<Utility>();

    Utility defaultUtility = new Utility(0,1);
    Utility utility;
    Measure measure;
    float weight = 1f;
    String name;

    public CompositeConsideration(){
        init();
    }

    void init() {
        weight = 1.0f;
        measure = new WeightedMetrics();
        utility = new Utility(0, weight, getRank());
    }

    public void addConsideration(Consideration c){
        considerations.add(c);
        considerationUtilities.add(new Utility(0,0));
    }

    public void consider(E context){
        if(considerations == null || considerations.isEmpty()) return;

        updateConsiderationUtilities(context);
        float mValue = measure.calculate(considerationUtilities);
        //utility = new Utility(mValue, weight, getRank());
        utility.setValue(mValue);
    }


    void updateConsiderationUtilities(E context) {
        for(int i = 0, count = considerations.size(); i < count; i++) {
            considerations.get(i).consider(context);
            considerationUtilities.set(i,considerations.get(i).utility);
        }
    }

    public Utility getDefaultUtility() {
        return defaultUtility;
    }

    public void setDefaultUtility(Utility defaultUtility) {
        this.defaultUtility = defaultUtility;
    }

    public Utility getUtility() {
        return utility;
    }

    public void setUtility(Utility utility) {
        this.utility = utility;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public Measure getMeasure() {
        return measure;
    }

    public void setMeasure(Measure measure) {
        this.measure = measure;
    }

    //returns max rank of all non zero considerations
    public float getRank() {
        return getMaxRank(considerationUtilities);
    }

    private float getMaxRank(List<Utility> elements) {
        if(elements == null || elements.isEmpty() ) return 0;

        Utility max = Collections.max(elements, new Comparator<Utility>() {
            @Override
            public int compare(Utility o1, Utility o2) {
                if(o1.getCombined() <=0) //o1 is less than because has no weight
                    return -1;

                if(o2.getCombined() <=0)//o1 is more than because has other no weight
                    return 1;

                return o1.getRank() < o2.getRank() ? -1 : o1.getRank() == o2.getRank() ? 0 : 1;
            }
        });

        return  max.getRank();
    }
}
