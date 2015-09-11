package com.badlogic.gdx.ai.tests.btree.dog;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;

/**
 * Created by DESK-W7 on 9/11/2015.
 */
public class PlayTask extends LeafTask<Dog> {

    public void start(){
        Dog dog = getObject();
        dog.brainLog("Lets play!");
    }

    @Override
    public void run () {
        Dog dog = getObject();
        dog.brainLog("-pant pant-");
        running();
    }

    @Override
    public void end(){
        Dog dog = getObject();
        dog.brainLog("No time to play");
    }

    @Override
    protected Task<Dog> copyTo(Task<Dog> task) {
        return task;
    }
}
