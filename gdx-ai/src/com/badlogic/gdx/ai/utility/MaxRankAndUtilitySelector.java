package com.badlogic.gdx.ai.utility;

import com.badlogic.gdx.ai.GdxAI;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * This selector returns the index of the Utility whose value is highest compared to any in the supllied list
 */
public class MaxRankAndUtilitySelector implements Selector{

    public int select(List<Utility> elements) {

        float maxRank = getMaxRank(elements);

        int count = elements.size();
        if(count == 0)
            return -1;
        if(count == 1)
            return 0;

        float maxUtil = 0f;
        int selIdx = -1;
        for(int i = 0; i < count; i++) {
            Utility el = elements.get(i);

            //GdxAI.getLogger().info("MaxRankAndUtilitySelector", el.getClass().getSimpleName() + " combined utility is : " + el.getCombined());

            if((el.getRank() >= maxRank) && el.getCombined() > maxUtil) {
                maxUtil = el.getCombined();
                selIdx = i;

                GdxAI.getLogger().info("MaxRankAndUtilitySelector", "selected with max utility: " + el.getClass().getSimpleName());
            }
        }



        return selIdx;
    }

    private float getMaxRank(List<Utility> elements) {
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

        //GdxAI.getLogger().info("MaxRankAndUtilitySelector", "max rank: " + max.getRank() );

        return  max.getRank();
    }
}
