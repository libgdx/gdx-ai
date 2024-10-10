package com.badlogic.gdx.ai.tests.utility;

import com.badlogic.gdx.ai.GdxAI;
import com.badlogic.gdx.ai.utility.Action;

/**
 * Created by felix on 8/4/2017.
 */
public class RestAction extends Action<Dog> {
    public RestAction() {
        super("RestAction", 0);
    }

    private float energyIncrement = 4f;
    private float thirstIncrement = 4f;


    public float timeScale = 1;
    public float timeStart = 0;

    @Override
    protected void onStart(Dog actor) {
        timeStart = GdxAI.getTimepiece().getTime();

        takeANap(actor);
    }

    @Override
    protected void onUpdate(Dog dog) {
        if(dog.energy > 90) {
            endInSuccess(dog);
        }

        takeANap(dog);
    }

    @Override
    protected void onStop(Dog context) {
        GdxAI.getLogger().info(getName(),"RestAction ran for " +  (GdxAI.getTimepiece().getTime() - timeStart) + "seconds" );
    }

    private void takeANap(Dog actor) {

        float enegery = actor.energy;
        //float thirst = actor.getThirst();

        GdxAI.getLogger().info(getName(),"Resting... Energy before " + enegery );

        actor.energy = enegery + energyIncrement;

         GdxAI.getLogger().info(getName(),"Finish Resting... Energy after " + actor.energy );
    }
}
