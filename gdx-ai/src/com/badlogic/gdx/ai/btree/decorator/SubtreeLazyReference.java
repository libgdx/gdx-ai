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

package com.badlogic.gdx.ai.btree.decorator;

import com.badlogic.gdx.ai.btree.BehaviorTreeLibraryManager;
import com.badlogic.gdx.ai.btree.Decorator;
import com.badlogic.gdx.ai.btree.Metadata;
import com.badlogic.gdx.ai.btree.Node;

/** A {@code SubtreeLookupDecorator} ...
 * 
 * @param <E> type of the blackboard nodes use to read or modify game state
 * 
 * @author davebaol
 * @author implicit-invocation */
public class SubtreeLazyReference<E> extends Decorator<E> {

	public static final Metadata METADATA = new Metadata("reference");

	/** The name of the subtree we're referencing to. */
	public String reference;

	public SubtreeLazyReference () {
	}

	public SubtreeLazyReference (String reference) {
		this.reference = reference;
	}

	public String getReference () {
		return reference;
	}

	public void getReference (String referenceName) {
		this.reference = referenceName;
	}

	public SubtreeLazyReference (Node<E> node) {
		super(node);
	}

	/** The first call of this method lazily sets its child to the referenced subtree created through the
	 * {@link BehaviorTreeLibraryManager}, then invoke the same method of the superclass. Subsequent calls invoke the same method
	 * of the superclass directly since the child has already been set.
	 * 
	 * @param control the parent node */
	@SuppressWarnings("unchecked")
	@Override
	public void setControl (Node<E> control) {
		if (node == null) {
			BehaviorTreeLibraryManager libraryManager = BehaviorTreeLibraryManager.getInstance();
			node = (Node<E>)libraryManager.createRootNode(reference);
		}
		super.setControl(control);
	}

	@Override
	protected Node<E> copyTo (Node<E> node) {
		if (node == null) super.copyTo(node);

		SubtreeLazyReference<E> include = (SubtreeLazyReference<E>)node;
		include.reference = reference;

		return node;
	}

}
