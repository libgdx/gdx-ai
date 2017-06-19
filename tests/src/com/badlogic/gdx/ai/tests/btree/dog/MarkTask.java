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

/** @author implicit-invocation
 * @author davebaol */
public class MarkTask extends LeafTask<Dog> {

	int i;

	@Override
	public void start () {
		i = 0;
		getObject().log("Dog lifts a leg and pee!");
	}

	@Override
	public Status execute () {
		Dog dog = getObject();
		Boolean result = dog.markATree(i++);
		if (result == null) {
			return Status.RUNNING;
		}
		return result ? Status.SUCCEEDED : Status.FAILED;
	}

	@Override
	protected Task<Dog> copyTo (Task<Dog> task) {
		return task;
	}

	@Override
	public void reset() {
		i = 0;
		super.reset();
	}
}
