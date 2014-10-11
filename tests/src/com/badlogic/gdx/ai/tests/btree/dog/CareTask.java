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

import com.badlogic.gdx.ai.btree.Metadata;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.LeafTask;

/** @author implicit-invocation
 * @author davebaol */
public class CareTask extends LeafTask<Dog> {

	public static final Metadata METADATA = new Metadata(LeafTask.METADATA, "urgentProb");

	public float urgentProb = 0.8f;

	@Override
	public void run (Dog dog) {
		if (Math.random() < urgentProb) {
			success();
		} else {
			dog.brainLog("It's leaking out!!!");
			dog.setUrgent(true);
			success();
		}
	}

	@Override
	protected Task<Dog> copyTo (Task<Dog> task) {
		CareTask care = (CareTask)task;
		care.urgentProb = urgentProb;

		return task;
	}

}
