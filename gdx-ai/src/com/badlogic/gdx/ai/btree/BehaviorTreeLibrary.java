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

import com.badlogic.gdx.ai.btree.parser.BehaviorTreeParser;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.SerializationException;

public class BehaviorTreeLibrary {

	protected ObjectMap<String, BehaviorTree<?>> repository;

	protected AssetManager assetManager;
	protected FileHandleResolver resolver;
	protected BehaviorTreeParser<?> parser;

	public BehaviorTreeLibrary () {
		this(BehaviorTreeParser.DEBUG_NONE);
	}

	public BehaviorTreeLibrary (int parserDebugLevel) {
		this(new InternalFileHandleResolver(), parserDebugLevel);
	}

	public BehaviorTreeLibrary (FileHandleResolver resolver) {
		this(resolver, BehaviorTreeParser.DEBUG_NONE);
	}

	public BehaviorTreeLibrary (FileHandleResolver resolver, int parserDebugLevel) {
		this(resolver, null, parserDebugLevel);
	}

	public BehaviorTreeLibrary (AssetManager assetManager) {
		this(assetManager, BehaviorTreeParser.DEBUG_NONE);
	}

	public BehaviorTreeLibrary (AssetManager assetManager, int parserDebugLevel) {
		this(null, assetManager, parserDebugLevel);
	}

	@SuppressWarnings("rawtypes")
	private BehaviorTreeLibrary (FileHandleResolver resolver, AssetManager assetManager, int parserDebugLevel) {
		this.resolver = resolver;
		this.assetManager = assetManager;
		this.repository = new ObjectMap<String, BehaviorTree<?>>();
		this.parser = new BehaviorTreeParser(parserDebugLevel);
	}

	/** Creates the root node of {@link BehaviorTree} from the specified reference.
	 * @param treeReference the tree identifier, typically a path
	 * @return the root node of the tree cloned from the archetype.
	 * @throws SerializationException if the reference cannot be successfully parsed.
	 * @throws NodeCloneException if the archetype cannot be successfully parsed. */
	public Node<?> createRootNode (String treeReference) {
		return retrieveArchetypeTree(treeReference).getChild(0).cloneNode();
	}

	/** Creates the {@link BehaviorTree} from the specified reference.
	 * @param treeReference the tree identifier, typically a path
	 * @return the tree cloned from the archetype.
	 * @throws SerializationException if the reference cannot be successfully parsed.
	 * @throws NodeCloneException if the archetype cannot be successfully parsed. */
	public BehaviorTree<?> createBehaviorTree (String treeReference) {
		return (BehaviorTree<?>)retrieveArchetypeTree(treeReference).cloneNode();
	}

	/** Retrieves the archetype tree from the library. If the library doesn't contain the archetype tree it is loaded and added to
	 * the library.
	 * @param treeReference the tree identifier, typically a path
	 * @return the archetype tree.
	 * @throws SerializationException if the reference cannot be successfully parsed. */
	protected BehaviorTree<?> retrieveArchetypeTree (String treeReference) {
		BehaviorTree<?> archetypeTree = repository.get(treeReference);
		if (archetypeTree == null) {
			if (assetManager != null) {
				// TODO: fix me!!!
// archetypeTree = assetManager.load(name, BehaviorTree.class, null);
				repository.put(treeReference, archetypeTree);
				return null;
			}
			archetypeTree = parser.parse(resolver.resolve(treeReference), null);
			repository.put(treeReference, archetypeTree);
		}
		return archetypeTree;
	}

}
