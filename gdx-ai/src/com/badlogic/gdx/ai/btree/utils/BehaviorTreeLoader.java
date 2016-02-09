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

package com.badlogic.gdx.ai.btree.utils;

import java.io.Reader;

import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AssetLoader;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.StreamUtils;

/** {@link AssetLoader} for {@link BehaviorTree} instances. The behavior tree is loaded asynchronously.
 * 
 * @author davebaol */
@SuppressWarnings("rawtypes")
public class BehaviorTreeLoader extends AsynchronousAssetLoader<BehaviorTree, BehaviorTreeLoader.BehaviorTreeParameter> {

	public BehaviorTreeLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	BehaviorTree behaviorTree;

	@SuppressWarnings("unchecked")
	@Override
	public void loadAsync (AssetManager manager, String fileName, FileHandle file, BehaviorTreeParameter parameter) {
		this.behaviorTree = null;

		Object blackboard = null;
		BehaviorTreeParser parser = null;
		if (parameter != null) {
			blackboard = parameter.blackboard;
			parser = parameter.parser;
		}

		if (parser == null) parser = new BehaviorTreeParser();

		Reader reader = null;
		try {
			reader = file.reader();
			this.behaviorTree = parser.parse(reader, blackboard);
		} finally {
			StreamUtils.closeQuietly(reader);
		}
	}

	@Override
	public BehaviorTree loadSync (AssetManager manager, String fileName, FileHandle file, BehaviorTreeParameter parameter) {
		BehaviorTree bundle = this.behaviorTree;
		this.behaviorTree = null;
		return bundle;
	}

	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, FileHandle file, BehaviorTreeParameter parameter) {
		return null;
	}

	public static class BehaviorTreeParameter extends AssetLoaderParameters<BehaviorTree> {
		public final Object blackboard;
		public final BehaviorTreeParser parser;

		public BehaviorTreeParameter () {
			this(null);
		}

		public BehaviorTreeParameter (Object blackboard) {
			this(blackboard, null);
		}

		public BehaviorTreeParameter (Object blackboard, BehaviorTreeParser parser) {
			this.blackboard = blackboard;
			this.parser = parser;
		}
	}

}
