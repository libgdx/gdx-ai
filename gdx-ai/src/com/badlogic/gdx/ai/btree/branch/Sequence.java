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

package com.badlogic.gdx.ai.btree.branch;

import com.badlogic.gdx.ai.btree.BranchNode;
import com.badlogic.gdx.ai.btree.Node;
import com.badlogic.gdx.utils.Array;

/** A sequence is a branch node that runs every children nodes until one of them fails. If a child node succeeds, the selector will
 * start and run the next child node.
 * 
 * @param <E> type of the blackboard nodes use to read or modify game state
 * 
 * @author implicit-invocation */
public class Sequence<E> extends BranchNode<E> {

	public Sequence () {
		super(new Array<Node<E>>());
	}

	public Sequence (Array<Node<E>> nodes) {
		super(nodes);
	}

	public Sequence (Node<E>... nodes) {
		super(new Array<Node<E>>(nodes));
	}

	@Override
	public void childSuccess (Node<E> runningNode) {
		super.childSuccess(runningNode);
		this.actualTask++;
		if (actualTask < children.size) {
			run(this.object);
		} else {
			success();
		}
	}

	@Override
	public void childFail (Node<E> runningNode) {
		fail();
	}

}
