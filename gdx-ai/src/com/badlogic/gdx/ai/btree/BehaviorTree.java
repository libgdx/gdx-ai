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

/** The behavior tree itself
 * 
 * @param <E> type of the blackboard nodes use to read or modify game state
 * 
 * @author implicit-invocation
 * @author davebaol */
public class BehaviorTree<E> extends Node<E> {

	private Node<E> rootNode;

	/** Creates a behavior tree with no root node and no blackboard object. Both the root node and the blackboard object must be set
	 * before running this behavior tree.
	 * 
	 * @param rootNode the root node of this tree */
	public BehaviorTree () {
		this(null, null);
	}

	/** Creates a behavior tree with a root node and no blackboard object. The blackboard object must be set before running this
	 * behavior tree.
	 * 
	 * @param rootNode the root node of this tree */
	public BehaviorTree (Node<E> rootNode) {
		this(rootNode, null);
	}

	/** Creates a behavior tree with a root node and a blackboard object
	 * 
	 * @param rootNode the root node of this tree
	 * @param object the blackboard */
	public BehaviorTree (Node<E> rootNode, E object) {
		this.rootNode = rootNode;
		this.object = object;
	}

	/** Sets the blackboard object.
	 * 
	 * @param object the new blackboard */
	public void setObject (E object) {
		this.object = object;
	}

	/** This method should be called when game entity needs to make decisions: call this in game loop or after a fixed time slice if
	 * the game is realtime, or on entity's turn if the game is turn-based */
	public void step () {
		if (runningNode != null) {
			runningNode.run(object);
		} else {
			rootNode.setControl(this);
			rootNode.object = object;
			rootNode.start(object);
			rootNode.run(object);
		}
	}

	@Override
	public int getChildCount () {
		return rootNode == null ? 0 : 1;
	}

	@Override
	public Node<E> getChild (int i) {
		return rootNode;
	}

	@Override
	public void run (E object) {
	}

	@Override
	protected Node<E> copyTo (Node<E> node) {
		BehaviorTree<E> tree = (BehaviorTree<E>)node;
		tree.rootNode = rootNode.cloneNode();

		return node;
	}

}
