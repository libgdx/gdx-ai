/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.ai.tests.btree.tests;

import java.io.Reader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.utils.BehaviorTreeParser;
import com.badlogic.gdx.ai.tests.BehaviorTreeTests;
import com.badlogic.gdx.ai.tests.btree.BehaviorTreeTestBase;
import com.badlogic.gdx.ai.tests.btree.dog.Dog;
import com.badlogic.gdx.ai.utils.NonBlockingSemaphoreRepository;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.StreamUtils;

/** A simple test to demonstrate behavior tree
 * 
 * @author davebaol */
public class SemaphoreGuardTest extends BehaviorTreeTestBase {

	private BehaviorTree<Dog> buddyBehaviorTree;
	private BehaviorTree<Dog> snoopyBehaviorTree;
	private float elapsedTime;
	private int step;

	public SemaphoreGuardTest (BehaviorTreeTests container) {
		super(container, "Semaphore Guard");
	}

	@Override
	public void create (Table table) {
		elapsedTime = 0;
		step = 0;

		Reader reader = null;
		try {
			// Parse Buddy's tree
			reader = Gdx.files.internal("data/dogSemaphore.tree").reader();
			BehaviorTreeParser<Dog> parser = new BehaviorTreeParser<Dog>(BehaviorTreeParser.DEBUG_HIGH);
			buddyBehaviorTree = parser.parse(reader, new Dog("Buddy"));

			// Clone Buddy's tree for Snoopy
			snoopyBehaviorTree = (BehaviorTree<Dog>)buddyBehaviorTree.cloneTask();
			snoopyBehaviorTree.setObject(new Dog("Snoopy"));

			// Create the semaphore
			NonBlockingSemaphoreRepository.clear();
			NonBlockingSemaphoreRepository.addSemaphore("dogSemaphore", 1);
			
			System.out.println();
		} finally {
			StreamUtils.closeQuietly(reader);
		}
	}

	@Override
	public void render () {
		elapsedTime += Gdx.graphics.getRawDeltaTime();

		if (elapsedTime > 0.8f) {
			System.out.println("Step: " + (++step));
			buddyBehaviorTree.step();
			snoopyBehaviorTree.step();
			elapsedTime = 0;
		}
	}

	@Override
	public void dispose () {
	}
}
