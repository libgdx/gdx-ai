package com.badlogic.gdx.ai.utility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by felix on 8/7/2017.
 */
public class UtilityAI<E> {
    List<Behaviour<E>> behaviours;
    List<Utility> behaviourUtilities;
    Selector selector;

    public UtilityAI(){
        init();
    }

    void init() {
        selector = new MaxUtilitySelector();
       // _behaviourMap = new Dictionary<string, Behaviour>();
        behaviours = new ArrayList<Behaviour<E>>();
        behaviourUtilities = new ArrayList<Utility>();
    }

    public boolean addBehaviour(Behaviour<E> behaviour){
        behaviours.add(behaviour); //todo dont add existing
        behaviourUtilities.add(new Utility(0f,0f));//todo not sure this usage

        return true;
    }


    /**
     * Selects one of the contained behaviours
     * Behavior in turn selected an option, which returns the action asociated with that option
     * @param context
     * @return
     */
    public Action select(E context){
        if(behaviours == null || behaviours.isEmpty())
            return null;

        if(behaviours.size() == 1)
            return behaviours.get(0).select(context);

        updateBehaviourUtilitites(context);
        return selectAction(context);
    }

    void updateBehaviourUtilitites(E context) {
        for(int i = 0, count = behaviours.size(); i < count; i++) {
            behaviours.get(i).consider(context);
            behaviourUtilities.set(i,behaviours.get(i).getUtility());
        }
    }

    Action selectAction(E context) {
        int idx = selector.select(behaviourUtilities);
        Behaviour selectedBehaviour = idx >= 0 ? behaviours.get(idx) : null;
        return selectedBehaviour.select(context);
    }
}
