package com.badlogic.gdx.ai.tests.utility;

import com.badlogic.gdx.ai.GdxAI;
import com.badlogic.gdx.math.MathUtils;

/**
 * Created by felix on 11/6/2017.
 */
public class Dog {

    //float hunger;
    float thirst = 0f;
    //float bladder;
    public float energy = 100f;
    public float curiousity = 0f;
    public String name;
    public Dog (String name) {
        this.name = name;
    }

    public void update(float detla) {
        // Do something with the context.
        energy -= 0.35f;
        energy = MathUtils.clamp(energy, 0, 100);
        //hunger += 0.4f;
        thirst += 0.5f;
        thirst = MathUtils.clamp(thirst, 0, 100);
        //bladder += 0.5f;
        curiousity += 0.1f;
        curiousity = MathUtils.clamp(curiousity, 0, 100);
//        _context.Cleanliness -= 0.3f;
//        _context.Fitness -= 0.5f;

        log( "Energy:" + energy + "\nThirst:" + thirst + "\ncuriousity" + curiousity);

    }

    public float getEnergy() {
        return energy;
    }

    public void setEnergy(float energy) {
        this.energy = energy;
    }


//    public float getHunger() {
//        return hunger;
//    }
//
//    public void setHunger(float hunger) {
//        this.hunger =  hunger; //Math.max(0, Math.min(hunger, 1));
//    }

    public float getThirst() {
        return thirst;
    }

    public void setThirst(float thirst) {
        this.thirst = thirst; //Math.max(0, Math.min(thirst, 1));;
    }


    public void startWalking () {
        log("Let's find a nice tree");
    }
    public boolean stillWalking () {

        if (MathUtils.random(10) > 3) {
            log("MUMBLE MUMBLE - Still walking about");
            return true;
        }
        log("finished walking");
        return false;
    }


    public void log (String msg) {
        GdxAI.getLogger().info(name, msg);
    }
}
