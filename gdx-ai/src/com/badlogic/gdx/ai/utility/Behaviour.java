package com.badlogic.gdx.ai.utility;

import com.badlogic.gdx.ai.utility.consideration.CompositeConsideration;
import com.badlogic.gdx.ai.utility.measure.Chebyshev;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by felix on 8/6/2017.
 */
/// <summary>
/// An AI behaviour is a set of <see cref="T:Crystal.IOption"/>s. The behaviour itself
/// is a composite consideration and therefore, unless this is the only behaviour in a
/// <see cref="T:Crystal.IUtilityAi"/>, it will be selected only if its considerations
/// "win" against competing behaviours in the AI.
/// </summary>
/// <seealso cref="T:Crystal.CompositeConsideration" />
/// <seealso cref="T:Crystal.IBehaviour" />
public class Behaviour<E> extends CompositeConsideration<E> {

    List<Option<E>> options = new ArrayList<Option<E>>();
    List<Utility> utilities = new ArrayList<Utility>();
    Selector selector;

    public Behaviour(String name){
        setName(name);
        init();
    }

    public void init(){
        setMeasure(new Chebyshev());
        selector = new MaxRankAndUtilitySelector();
    }


    public void addOption(Option option) {
        options.add(option);
        utilities.add(new Utility(0.0f, 1.0f));
    }

    /// <summary>
    ///   Selects the action for execution, given the specified context.
    /// </summary>
    /// <param name="context">The context.</param>
    /// <returns>The action to execute.</returns>
    public Action select(E context) {
        for(int i = 0, count = options.size(); i < count; i++) {
            options.get(i).consider(context);
            utilities.set(i,options.get(i).getUtility());
        }

        return selectAction();
    }

    protected Action selectAction(){
        int idx = selector.select(utilities);
        Option option = idx >= 0 ? options.get(idx) : null;
        return option != null ? option.action : null;
    }

}
