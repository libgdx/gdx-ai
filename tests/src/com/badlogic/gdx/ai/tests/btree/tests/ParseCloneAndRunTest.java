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
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.TaskCloner;
import com.badlogic.gdx.ai.btree.utils.BehaviorTreeParser;
import com.badlogic.gdx.ai.tests.btree.BehaviorTreeTestBase;
import com.badlogic.gdx.ai.tests.btree.BehaviorTreeViewer;
import com.badlogic.gdx.ai.tests.btree.KryoUtils;
import com.badlogic.gdx.ai.tests.btree.dog.Dog;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.StreamUtils;

/** A simple test to demonstrate behavior tree cloning capabilities.
 * 
 * @author davebaol */
public class ParseCloneAndRunTest extends BehaviorTreeTestBase {

	boolean useKryo;

	public ParseCloneAndRunTest (boolean useKryo) {
		super("Parse, Clone and Run (" + (useKryo ? "Kryo" : "no") + " cloner)");
		this.useKryo = useKryo;
	}

	@Override
	public Actor createActor (Skin skin) {
		Reader reader = null;
		try {
			// Parse
			reader = Gdx.files.internal("data/dog.tree").reader();
			BehaviorTreeParser<Dog> parser = new BehaviorTreeParser<Dog>(BehaviorTreeParser.DEBUG_NONE);
			BehaviorTree<Dog> treeArchetype = parser.parse(reader, null);

			// Clone
			Task.TASK_CLONER = !useKryo ? null : new TaskCloner() {
				@Override
				public <T> Task<T> cloneTask (Task<T> task) {
					return KryoUtils.copy(task);
				}
			};
			BehaviorTree<Dog> tree = (BehaviorTree<Dog>)treeArchetype.cloneTask();
			tree.setObject(new Dog("Cloned Buddy"));
			BehaviorTreeViewer<?> treeViewer = createTreeViewer(tree.getObject().name, tree, true, skin);

			return new ScrollPane(treeViewer, skin);
		} finally {
			StreamUtils.closeQuietly(reader);
		}
	}

	@Override
	public void dispose () {
		super.dispose();
		Task.TASK_CLONER = null;
	}

}
