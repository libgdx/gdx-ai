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

package com.badlogic.gdx.ai.tests.btree;

import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/** Base class for individual behavior tree tests.
 * 
 * @author davebaol */
public abstract class BehaviorTreeTestBase {

	private String name;
	private String description;

	public BehaviorTreeTestBase (String name) {
		this(name, null);
	}

	public BehaviorTreeTestBase (String name, String description) {
		this.name = name;
		this.description = description;
	}

	public abstract Actor createActor (Skin skin);

	public abstract void dispose ();

	public String getName () {
		return name;
	}

	public void setName (String name) {
		this.name = name;
	}

	public void setDescription (String description) {
		this.description = description;
	}

	public String getDescription () {
		return description;
	}

	public static Actor createTreeViewer (String name, BehaviorTree<?> tree, Skin skin) {
		return createTreeViewer(name, tree, skin, true);

	}

	public static Actor createTreeViewer (String name, BehaviorTree<?> tree, Skin skin, boolean scrollable) {
		@SuppressWarnings({"rawtypes", "unchecked"})
		BehaviorTreeViewer<?> btv = new BehaviorTreeViewer(tree, skin);
		btv.setName(name);

		return scrollable ? new ScrollPane(btv, skin) : btv;

	}
}
