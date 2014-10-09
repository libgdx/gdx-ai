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

/** @author davebaol */
public class BehaviorTreeLibraryManager {

	private static BehaviorTreeLibraryManager instance = new BehaviorTreeLibraryManager();

	protected BehaviorTreeLibrary library;

	private BehaviorTreeLibraryManager () {
	}

	public static BehaviorTreeLibraryManager getInstance () {
		return instance;
	}

	public void setLibrary (BehaviorTreeLibrary library) {
		this.library = library;
	}

	public BehaviorTree<?> createBehaviorTree (String treeReference) {
		return library.createBehaviorTree(treeReference);
	}

	public Node<?> createRootNode (String treeReference) {
		return library.createRootNode(treeReference);
	}

}
