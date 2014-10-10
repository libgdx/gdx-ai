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

package com.badlogic.gdx.ai.btree;

import com.badlogic.gdx.utils.SerializationException;

/** @author davebaol */
public class BehaviorTreeLibraryManager {

	private static BehaviorTreeLibraryManager instance = new BehaviorTreeLibraryManager();

	protected BehaviorTreeLibrary library;

	private BehaviorTreeLibraryManager () {
	}

	public static BehaviorTreeLibraryManager getInstance () {
		return instance;
	}

	public BehaviorTreeLibrary getLibrary () {
		return library;
	}

	public void setLibrary (BehaviorTreeLibrary library) {
		this.library = library;
	}

	/** Creates the root node of {@link BehaviorTree} for the specified reference.
	 * @param treeReference the tree identifier, typically a path
	 * @return the root node of the tree cloned from the archetype.
	 * @throws SerializationException if the reference cannot be successfully parsed.
	 * @throws NodeCloneException if the archetype cannot be successfully parsed. */
	public <T> Node<T> createRootNode (String treeReference) {
		return library.createRootNode(treeReference);
	}

	/** Creates the {@link BehaviorTree} for the specified reference.
	 * @param treeReference the tree identifier, typically a path
	 * @return the tree cloned from the archetype.
	 * @throws SerializationException if the reference cannot be successfully parsed.
	 * @throws NodeCloneException if the archetype cannot be successfully parsed. */
	public <T> BehaviorTree<T> createBehaviorTree (String treeReference) {
		return library.createBehaviorTree(treeReference);
	}

	/** Creates the {@link BehaviorTree} for the specified reference and blackboard object.
	 * @param treeReference the tree identifier, typically a path
	 * @param blackboard the blackboard object (it can be {@code null}).
	 * @return the tree cloned from the archetype.
	 * @throws SerializationException if the reference cannot be successfully parsed.
	 * @throws NodeCloneException if the archetype cannot be successfully parsed. */
	public <T> BehaviorTree<T> createBehaviorTree (String treeReference, T blackboard) {
		return library.createBehaviorTree(treeReference, blackboard);
	}

}
