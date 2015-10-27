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

package com.badlogic.gdx.ai;

/** @author davebaol */
public interface Timepiece {

	/** Returns the time accumulated up to the current frame in seconds. */
	public float getTime ();

	/** Returns the time span between the current frame and the last frame in seconds. */
	public float getDeltaTime ();

	/** Updates this timepiece with the given delta time.
	 * @param deltaTime the time in seconds since the last frame. */
	public void update (float deltaTime);

}
