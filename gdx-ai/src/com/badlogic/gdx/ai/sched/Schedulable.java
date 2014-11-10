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

package com.badlogic.gdx.ai.sched;

/** Anything that can be scheduled by a {@link Scheduler} must implement this interface.
 * 
 * @author davebaol */
public interface Schedulable {

	/** Method invoked by the {@link Scheduler} when this schedulable needs to be run.
	 * @param nanoTimeToRun the maximum time in nanoseconds this scheduler should run on the current frame. */
	public void run (long nanoTimeToRun);
}
