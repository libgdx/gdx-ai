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

package com.badlogic.gdx.ai.btree.leaf;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.annotation.TaskAttribute;
import com.badlogic.gdx.ai.utils.random.ConstantFloatDistribution;
import com.badlogic.gdx.ai.utils.random.FloatDistribution;
import com.badlogic.gdx.utils.TimeUtils;

/** {@code Wait} is a leaf that keeps running for the specified amount of time then succeeds.
 * 
 * @param <E> type of the blackboard object that tasks use to read or modify game state
 * 
 * @author davebaol */
public class Wait<E> extends LeafTask<E> {

	@TaskAttribute(required = true) public FloatDistribution seconds;

	private long startTime;
	private float timeout;

	/** Creates a {@code Wait} task that immediately succeeds. */
	public Wait () {
		this(ConstantFloatDistribution.ZERO);
	}

	/** Creates a {@code Wait} task running for the specified number of seconds.
	 * 
	 * @param seconds the number of seconds to wait for */
	public Wait (float seconds) {
		this(new ConstantFloatDistribution(seconds));
	}

	/** Creates a {@code Wait} task running for the specified number of seconds.
	 * 
	 * @param seconds the random distribution determining the number of seconds to wait for */
	public Wait (FloatDistribution seconds) {
		this.seconds = seconds;
	}

	@Override
	public void start () {
		timeout = seconds.nextFloat();
		startTime = TimeUtils.nanoTime();
	}

	@Override
	public void run () {
		if ((TimeUtils.nanoTime() - startTime) / 1000000000f < timeout)
			running();
		else
			success();
	}

	@Override
	protected Task<E> copyTo (Task<E> task) {
		((Wait<E>)task).seconds = seconds;
		return task;
	}

}
