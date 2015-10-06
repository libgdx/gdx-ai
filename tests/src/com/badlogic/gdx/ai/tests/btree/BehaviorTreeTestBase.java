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

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/** Base class for individual behavior tree tests.
 * 
 * @author davebaol */
public abstract class BehaviorTreeTestBase {

	public String testName;

	public BehaviorTreeTestBase (String testName) {
		this.testName = testName;
	}

	public String getDescription () {
		return null;
	}

	public abstract Actor createActor (Skin skin);
	
	public abstract void dispose ();

}
