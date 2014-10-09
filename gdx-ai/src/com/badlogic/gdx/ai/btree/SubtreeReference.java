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

/** A {@code SubtreeReference} ...
 * 
 * @param <E> type of the blackboard nodes use to read or modify game state
 * 
 * @author davebaol */
public class SubtreeReference<E> extends Task<E> {

	public static final Metadata METADATA = new Metadata("reference");

	/** The name of the subtree we're referencing to. */
	public String reference;

	public SubtreeReference () {
	}

	public SubtreeReference (String reference) {
		this.reference = reference;
	}

	public String getReference () {
		return reference;
	}

	public void setReference (String reference) {
		this.reference = reference;
	}

	/** A {@code SubtreeReference} should never run. This method always throw an {@code UnsupportedOperationException}
	 * 
	 * @param object blackboard
	 * @exception UnsupportedOperationException always */
	@Override
	public void run (E object) {
		throw new UnsupportedOperationException("A " + SubtreeReference.class.getSimpleName() + " isn't meant to be run!");
	}

	/** Returns a clone of the referenced BehaviorTree. */
	@SuppressWarnings("unchecked")
	public Node<E> cloneNode () {
		BehaviorTreeLibraryManager libraryManager = BehaviorTreeLibraryManager.getInstance();
		return (Node<E>)libraryManager.createRootNode(reference);
	}

	/** Always throw a {@link NodeCloneException} because a @ SubtreeReference} should never be copied.
	 * @param node the node to be filled
	 * @return the given node for chaining
	 * @throws NodeCloneException always. */
	@Override
	protected Node<E> copyTo (Node<E> node) {
		throw new NodeCloneException(getClass().getSimpleName() + " should never be copied.");
	}

}
