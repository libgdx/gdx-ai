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

package com.badlogic.gdx.ai.btree.parser;

import java.io.Reader;

import com.badlogic.gdx.Gdx;
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

/** {@link AssetLoader} for {@link BehaviorTree} instances. The BehaviorTree is loaded asynchronously.
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
		if (parameter == null)
			throw new IllegalArgumentException("BehaviorTreeParameter can not be null");
		if (parameter.blackboard == null)
			throw new IllegalArgumentException("BehaviorTreeParameter.blackboard can not be null");

		Reader reader = null;
		try {
			reader = Gdx.files.internal("data/dog.tree.xml").reader();
			BehaviorTreeParser parser = new BehaviorTreeParser(parameter.debug);
			this.behaviorTree = parser.parse(reader, parameter.blackboard);
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

	static public class BehaviorTreeParameter extends AssetLoaderParameters<BehaviorTree> {
		public final Object blackboard;
		public final int debug;

		public BehaviorTreeParameter (Object blackboard) {
			this(blackboard, BehaviorTreeParser.DEBUG_NONE);
		}

		public BehaviorTreeParameter (Object blackboard, int debug) {
			this.blackboard = blackboard;
			this.debug = debug;
		}
	}

}
