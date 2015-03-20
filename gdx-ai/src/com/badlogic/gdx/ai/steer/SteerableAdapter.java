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

package com.badlogic.gdx.ai.steer;

import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.Vector;

/** An adapter class for {@link Steerable}. You can derive from this and only override what you are interested in. For example,
 * this comes in handy when you have to create on the fly a target for a particular behavior.
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @author davebaol */
public class SteerableAdapter<T extends Vector<T>> implements Steerable<T> {

	@Override
	public float getZeroLinearSpeedThreshold () {
		return 0.001f;
	}

	@Override
	public void setZeroLinearSpeedThreshold (float value) {
	}

	@Override
	public float getMaxLinearSpeed () {
		return 0;
	}

	@Override
	public void setMaxLinearSpeed (float maxLinearSpeed) {
	}

	@Override
	public float getMaxLinearAcceleration () {
		return 0;
	}

	@Override
	public void setMaxLinearAcceleration (float maxLinearAcceleration) {
	}

	@Override
	public float getMaxAngularSpeed () {
		return 0;
	}

	@Override
	public void setMaxAngularSpeed (float maxAngularSpeed) {
	}

	@Override
	public float getMaxAngularAcceleration () {
		return 0;
	}

	@Override
	public void setMaxAngularAcceleration (float maxAngularAcceleration) {
	}

	@Override
	public T getPosition () {
		return null;
	}

	@Override
	public float getOrientation () {
		return 0;
	}

	@Override
	public void setOrientation (float orientation) {
	}

	@Override
	public T getLinearVelocity () {
		return null;
	}

	@Override
	public float getAngularVelocity () {
		return 0;
	}

	@Override
	public float getBoundingRadius () {
		return 0;
	}

	@Override
	public boolean isTagged () {
		return false;
	}

	@Override
	public void setTagged (boolean tagged) {
	}

	@Override
	public Location<T> newLocation () {
		return null;
	}

	@Override
	public float vectorToAngle (T vector) {
		return 0;
	}

	@Override
	public T angleToVector (T outVector, float angle) {
		return null;
	}

}
