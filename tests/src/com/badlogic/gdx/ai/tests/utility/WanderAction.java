package com.badlogic.gdx.ai.tests.utility;

import com.badlogic.gdx.ai.GdxAI;
import com.badlogic.gdx.ai.utility.Action;

/**
 * Created by felix on 8/4/2017.
 */
public class WanderAction extends Action<Dog> {

    public WanderAction() {
        super("WanderAction", 0);
    }
    private float curiousityDelta = 1f;
    private float energyDelta = 5f;

    float timeStart = 0;

    @Override
    protected void onStart(Dog actor) {
        timeStart = GdxAI.getTimepiece().getTime();
        actor.startWalking();

    }

    @Override
    protected void onUpdate(Dog dog) {
        GdxAI.getLogger().info(getName(),"+++ WanderAction update...");

        if (dog.stillWalking()) {
            //dog.log("Walking ...");

            float curiousity = dog.curiousity;
            float energy = dog.energy;

            dog.curiousity -= curiousityDelta;
            dog.energy -= energyDelta;

            GdxAI.getLogger().info(getName(),"Wander... Curoiusity before " + curiousity + " , after " + dog.curiousity);
            GdxAI.getLogger().info(getName(),"Wander... Energy before " + energy + " , after " + dog.energy);

        } else {
            endInSuccess(dog);
        }

    }

    @Override
    protected void onStop(Dog context) {
        GdxAI.getLogger().info(getName(),"WanderAction ran for " +  (GdxAI.getTimepiece().getTime() - timeStart) + "seconds" );
    }
}
