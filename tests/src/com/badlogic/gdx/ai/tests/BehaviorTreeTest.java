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

package com.badlogic.gdx.ai.tests;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.parser.xml.XmlParser;
import com.badlogic.gdx.ai.tests.btree.Dog;
import com.badlogic.gdx.ai.tests.utils.GdxAiTest;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;

/** A simple test to demonstrate behavior tree
 * 
 * @author implicit-invocation */
public class BehaviorTreeTest extends GdxAiTest {

	public static void main (String[] argv) {
		launch(new BehaviorTreeTest());
	}

	private BehaviorTree<Dog> dogBehaviorTree;
	private float elapsedTime;

	@Override
	public void create () {
		elapsedTime = 0;

// String treeData = Gdx.files.internal("data/dog.tree").readString();
// dogBehaviorTree = new BehaviorTree<Dog>(treeData, new Dog());

		InputStream in = Gdx.files.internal("data/dog.tree.xml").read();
		InputStreamReader reader = new InputStreamReader(in);
		XmlParser<Dog> parser = new XmlParser<Dog>();
		try {
			dogBehaviorTree = parser.parser(reader, new Dog());
		} catch (IOException e) {
			throw new GdxRuntimeException(e);
		} finally {
			StreamUtils.closeQuietly(reader);
		}

	}

	@Override
	public void render () {
		elapsedTime += Gdx.graphics.getRawDeltaTime();

		if (elapsedTime > 0.8f) {
			dogBehaviorTree.step();
			elapsedTime = 0;
		}
	}
}
