package com.badlogic.gdx.ai.utility;

import java.util.List;

/**
 * This selector returns the index of the Utility whose value is highest compared to any in the supllied list
 */
public class MaxUtilitySelector implements Selector{

    public int select(List<Utility> elements) {
        int count = elements.size();
        if(count == 0)
            return -1;
        if(count == 1)
            return 0;

        float maxUtil = 0f;
        int selIdx = -1;
        for(int i = 0; i < count; i++) {
            Utility el = elements.get(i);
            if(el.getCombined() > maxUtil) {
                maxUtil = el.getCombined();
                selIdx = i;
            }
        }

        return selIdx;
    }
}
