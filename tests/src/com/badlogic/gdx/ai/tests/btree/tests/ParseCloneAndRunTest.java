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
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.StreamUtils;

/** A simple test to demonstrate behavior tree cloning capabilities.
 * 
 * @author davebaol */
public class ParseCloneAndRunTest extends BehaviorTreeTestBase {

	private BehaviorTree<Dog> dogBehaviorTree;
	private float elapsedTime;

	public ParseCloneAndRunTest (BehaviorTreeTests container) {
		super(container, "Parse, Clone and Run");
	}

	@Override
	public void create (Table table) {
		elapsedTime = 0;

		BehaviorTree<Dog> dogBehaviorTreeArchetype = null;

		Reader reader = null;
		try {
			reader = Gdx.files.internal("data/dog.tree").reader();
			BehaviorTreeParser<Dog> parser = new BehaviorTreeParser<Dog>(BehaviorTreeParser.DEBUG_NONE);
			dogBehaviorTreeArchetype = parser.parse(reader, null);
		} finally {
			StreamUtils.closeQuietly(reader);
		}
		if (dogBehaviorTreeArchetype != null) {
			dogBehaviorTree = (BehaviorTree<Dog>)dogBehaviorTreeArchetype.cloneTask();
			dogBehaviorTree.setObject(new Dog("Cloned Buddy"));
		}
	}

	@Override
	public void render () {
		elapsedTime += Gdx.graphics.getRawDeltaTime();

		if (elapsedTime > 0.8f) {
			dogBehaviorTree.step();
			elapsedTime = 0;
		}
	}

	@Override
	public void dispose () {
	}

}
