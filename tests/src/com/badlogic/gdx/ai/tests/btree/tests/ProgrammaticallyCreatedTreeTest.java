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

import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.branch.Parallel;
import com.badlogic.gdx.ai.btree.branch.Selector;
import com.badlogic.gdx.ai.btree.branch.Sequence;
import com.badlogic.gdx.ai.btree.decorator.AlwaysFail;
import com.badlogic.gdx.ai.btree.decorator.Include;
import com.badlogic.gdx.ai.btree.utils.BehaviorTreeLibrary;
import com.badlogic.gdx.ai.btree.utils.BehaviorTreeLibraryManager;
import com.badlogic.gdx.ai.btree.utils.BehaviorTreeParser;
import com.badlogic.gdx.ai.tests.btree.BehaviorTreeTestBase;
import com.badlogic.gdx.ai.tests.btree.BehaviorTreeViewer;
import com.badlogic.gdx.ai.tests.btree.dog.BarkTask;
import com.badlogic.gdx.ai.tests.btree.dog.CareTask;
import com.badlogic.gdx.ai.tests.btree.dog.Dog;
import com.badlogic.gdx.ai.tests.btree.dog.MarkTask;
import com.badlogic.gdx.ai.tests.btree.dog.RestTask;
import com.badlogic.gdx.ai.tests.btree.dog.WalkTask;
import com.badlogic.gdx.ai.utils.random.TriangularIntegerDistribution;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/** A simple test to demonstrate subtree inclusion both eager (at clone-time) and lazy (at run-time) for programmatically created
 * behavior trees.
 * 
 * @author davebaol */
public class ProgrammaticallyCreatedTreeTest extends BehaviorTreeTestBase {

	private boolean lazy;

	public ProgrammaticallyCreatedTreeTest (boolean lazy) {
		super("Programmatically Created Tree" + (lazy ? " (lazy)" : ""));
		this.lazy = lazy;
	}

	@Override
	public Actor createActor (Skin skin) {
		BehaviorTreeLibraryManager libraryManager = BehaviorTreeLibraryManager.getInstance();
		BehaviorTreeLibrary library = new BehaviorTreeLibrary(BehaviorTreeParser.DEBUG_HIGH);
		registerDogBehavior(library);
		libraryManager.setLibrary(library);
		BehaviorTree<Dog> tree = libraryManager.createBehaviorTree("dog", new Dog("Buddy"));
		BehaviorTreeViewer<?> treeViewer = createTreeViewer(tree.getObject().name, tree, true, skin);

		return new ScrollPane(treeViewer, skin);
	}

	private void registerDogBehavior (BehaviorTreeLibrary library) {

		Include<Dog> include = new Include<Dog>();
		include.lazy = lazy;
		include.subtree = "dog.actual";
		BehaviorTree<Dog> includeBehavior = new BehaviorTree<Dog>(include);
		library.registerArchetypeTree("dog", includeBehavior);

		BehaviorTree<Dog> actualBehavior = new BehaviorTree<Dog>(createDogBehavior());
		library.registerArchetypeTree("dog.actual", actualBehavior);
	}

	private static Task<Dog> createDogBehavior () {
		Selector<Dog> selector = new Selector<Dog>();

		Parallel<Dog> parallel = new Parallel<Dog>();
		selector.addChild(parallel);

		CareTask care = new CareTask();
		care.urgentProb = 0.8f;
		parallel.addChild(care);
		parallel.addChild(new AlwaysFail<Dog>(new RestTask()));

		Sequence<Dog> sequence = new Sequence<Dog>();
		selector.addChild(sequence);

		BarkTask bark1 = new BarkTask();
		bark1.times = new TriangularIntegerDistribution(1, 5, 2);
		sequence.addChild(bark1);
		sequence.addChild(new WalkTask());
		sequence.addChild(new BarkTask());
		sequence.addChild(new MarkTask());

		return selector;
	}
}
