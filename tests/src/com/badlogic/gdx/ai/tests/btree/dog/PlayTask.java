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

package com.badlogic.gdx.ai.tests.btree.dog;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;

public class PlayTask extends LeafTask<Dog> {

	public void start () {
		Dog dog = getObject();
		dog.brainLog("WOW - Lets play!");
	}

	@Override
	public Status execute () {
		Dog dog = getObject();
		dog.brainLog("PANT PANT - So fun");
		return Status.RUNNING;
	}

	@Override
	public void end () {
		Dog dog = getObject();
		dog.brainLog("SIC - No time to play :(");
	}

	@Override
	protected Task<Dog> copyTo (Task<Dog> task) {
		return task;
	}
}
