package com.badlogic.gdx.ai.tests.btree.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.utils.BehaviorTreeParser;
import com.badlogic.gdx.ai.tests.BehaviorTreeTests;
import com.badlogic.gdx.ai.tests.btree.BehaviorTreeTestBase;
import com.badlogic.gdx.ai.tests.btree.dog.Dog;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.StreamUtils;

import java.io.Reader;

/**
 * Created by DESK-W7 on 9/11/2015.
 */
public class ParallelShortCircuitTest extends BehaviorTreeTestBase {

    private BehaviorTree<Dog> dogBehaviorTree;
    private float elapsedTime;
    private int step;

    public ParallelShortCircuitTest (BehaviorTreeTests container) {
        super(container, "Parallel Short Circuit");
    }

    @Override
    public void create (Table table) {
        elapsedTime = 0;
        step = 0;

        Reader reader = null;
        try {
            reader = Gdx.files.internal("data/dogParallel.tree").reader();
            BehaviorTreeParser<Dog> parser = new BehaviorTreeParser<Dog>(BehaviorTreeParser.DEBUG_NONE);
            dogBehaviorTree = parser.parse(reader, new Dog("Snowball"));
        } finally {
            StreamUtils.closeQuietly(reader);
        }
    }

    @Override
    public void render () {
        elapsedTime += Gdx.graphics.getRawDeltaTime();

        if (elapsedTime > 0.8f) {
            System.out.println("Step: " + (++step));
            dogBehaviorTree.step();
            elapsedTime = 0;
        }
    }

    @Override
    public void dispose () {
    }
}
