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

package com.badlogic.gdx.ai.steer.behaviors;

import com.badlogic.gdx.ai.GdxAI;
import com.badlogic.gdx.ai.Timepiece;
import com.badlogic.gdx.ai.steer.Limiter;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector;

/** {@code Wander} behavior is designed to produce a steering acceleration that will give the impression of a random walk through
 * the agent's environment. You'll often find it a useful ingredient when creating an agent's behavior.
 * <p>
 * There is a circle in front of the owner (where front is determined by its current facing direction) on which the target is
 * constrained. Each time the behavior is run, we move the target around the circle a little, by a random amount. Now there are 2
 * ways to implement wander behavior:
 * <ul>
 * <li>The owner seeks the target, using the {@link Seek} behavior, and performs a {@link LookWhereYouAreGoing} behavior to
 * correct its orientation.</li>
 * <li>The owner tries to face the target in each frame, using the {@link Face} behavior to align to the target, and applies full
 * linear acceleration in the direction of its current orientation.</li>
 * </ul>
 * In either case, the orientation of the owner is retained between calls (so smoothing the changes in orientation). The angles
 * that the edges of the circle subtend to the owner determine how fast it will turn. If the target is on one of these extreme
 * points, it will turn quickly. The target will twitch and jitter around the edge of the circle, but the owner's orientation will
 * change smoothly.
 * <p>
 * This implementation uses the second approach. However, if you manually align owner's orientation to its linear velocity on each
 * time step, {@link Face} behavior should not be used (which is the default case). On the other hand, if the owner has
 * independent facing you should explicitly call {@link #setFaceEnabled(boolean) setFaceEnabled(true)} before using Wander
 * behavior.
 * <p>
 * Note that this behavior internally calls the {@link Timepiece#getTime() GdxAI.getTimepiece().getTime()} method to get the
 * current AI time and make the {@link #wanderRate} FPS independent. This means that
 * <ul>
 * <li>if you forget to {@link Timepiece#update(float) update the timepiece} the wander orientation won't change.</li>
 * <li>ideally the timepiece should be always updated before this steering behavior runs.</li>
 * </ul>
 * <p>
 * This steering behavior can be used to produce a whole range of random motion, from very smooth undulating turns to wild
 * Strictly Ballroom type whirls and pirouettes depending on the size of the circle, its distance from the agent, and the amount
 * of random displacement each frame.
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @author davebaol */
public class Wander<T extends Vector<T>> extends Face<T> {

	/** The forward offset of the wander circle */
	protected float wanderOffset;

	/** The radius of the wander circle */
	protected float wanderRadius;

	/** The rate, expressed in radian per second, at which the wander orientation can change */
	protected float wanderRate;

	/** The last time the orientation of the wander target has been updated */
	protected float lastTime;

	/** The current orientation of the wander target */
	protected float wanderOrientation;

	/** The flag indicating whether to use {@link Face} behavior or not. This should be set to {@code true} when independent facing
	 * is used. */
	protected boolean faceEnabled;

	private T internalTargetPosition;
	private T wanderCenter;

	/** Creates a {@code Wander} behavior for the specified owner.
	 * @param owner the owner of this behavior. */
	public Wander (Steerable<T> owner) {
		super(owner);

		this.internalTargetPosition = newVector(owner);
		this.wanderCenter = newVector(owner);
	}

	@Override
	protected SteeringAcceleration<T> calculateRealSteering (SteeringAcceleration<T> steering) {
		// Update the wander orientation
		float now = GdxAI.getTimepiece().getTime();
		if (lastTime > 0) {
			float delta = now - lastTime;
			wanderOrientation += MathUtils.randomTriangular(wanderRate * delta);
		}
		lastTime = now;

		// Calculate the combined target orientation
		float targetOrientation = wanderOrientation + owner.getOrientation();

		// Calculate the center of the wander circle
		wanderCenter.set(owner.getPosition()).mulAdd(owner.angleToVector(steering.linear, owner.getOrientation()), wanderOffset);

		// Calculate the target location
		// Notice that we're using steering.linear as temporary vector
		internalTargetPosition.set(wanderCenter).mulAdd(owner.angleToVector(steering.linear, targetOrientation), wanderRadius);

		float maxLinearAcceleration = getActualLimiter().getMaxLinearAcceleration();

		if (faceEnabled) {
			// Delegate to face
			face(steering, internalTargetPosition);

			// Set the linear acceleration to be at full
			// acceleration in the direction of the orientation
			owner.angleToVector(steering.linear, owner.getOrientation()).scl(maxLinearAcceleration);
		} else {
			// Seek the internal target position
			steering.linear.set(internalTargetPosition).sub(owner.getPosition()).nor().scl(maxLinearAcceleration);

			// No angular acceleration
			steering.angular = 0;

		}

		return steering;
	}

	/** Returns the forward offset of the wander circle. */
	public float getWanderOffset () {
		return wanderOffset;
	}

	/** Sets the forward offset of the wander circle.
	 * @return this behavior for chaining. */
	public Wander<T> setWanderOffset (float wanderOffset) {
		this.wanderOffset = wanderOffset;
		return this;
	}

	/** Returns the radius of the wander circle. */
	public float getWanderRadius () {
		return wanderRadius;
	}

	/** Sets the radius of the wander circle.
	 * @return this behavior for chaining. */
	public Wander<T> setWanderRadius (float wanderRadius) {
		this.wanderRadius = wanderRadius;
		return this;
	}

	/** Returns the rate, expressed in radian per second, at which the wander orientation can change. */
	public float getWanderRate () {
		return wanderRate;
	}

	/** Sets the rate, expressed in radian per second, at which the wander orientation can change.
	 * @return this behavior for chaining. */
	public Wander<T> setWanderRate (float wanderRate) {
		this.wanderRate = wanderRate;
		return this;
	}

	/** Returns the current orientation of the wander target. */
	public float getWanderOrientation () {
		return wanderOrientation;
	}

	/** Sets the current orientation of the wander target.
	 * @return this behavior for chaining. */
	public Wander<T> setWanderOrientation (float wanderOrientation) {
		this.wanderOrientation = wanderOrientation;
		return this;
	}

	/** Returns the flag indicating whether to use {@link Face} behavior or not. */
	public boolean isFaceEnabled () {
		return faceEnabled;
	}

	/** Sets the flag indicating whether to use {@link Face} behavior or not. This should be set to {@code true} when independent
	 * facing is used.
	 * @return this behavior for chaining. */
	public Wander<T> setFaceEnabled (boolean faceEnabled) {
		this.faceEnabled = faceEnabled;
		return this;
	}

	/** Returns the current position of the wander target. This method is useful for debug purpose. */
	public T getInternalTargetPosition () {
		return internalTargetPosition;
	}

	/** Returns the current center of the wander circle. This method is useful for debug purpose. */
	public T getWanderCenter () {
		return wanderCenter;
	}

	//
	// Setters overridden in order to fix the correct return type for chaining
	//

	@Override
	public Wander<T> setOwner (Steerable<T> owner) {
		this.owner = owner;
		return this;
	}

	@Override
	public Wander<T> setEnabled (boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	/** Sets the limiter of this steering behavior. The given limiter must at least take care of the maximum linear acceleration;
	 * additionally, if the flag {@code faceEnabled} is true, it must take care of the maximum angular speed and acceleration.
	 * @return this behavior for chaining. */
	@Override
	public Wander<T> setLimiter (Limiter limiter) {
		this.limiter = limiter;
		return this;
	}

	/** Sets the target to align to. Notice that this method is inherited from {@link ReachOrientation}, but is completely useless
	 * for {@code Wander} because owner's orientation is determined by the internal target, which is moving on the wander circle.
	 * @return this behavior for chaining. */
	@Override
	public Wander<T> setTarget (Location<T> target) {
		this.target = target;
		return this;
	}

	@Override
	public Wander<T> setAlignTolerance (float alignTolerance) {
		this.alignTolerance = alignTolerance;
		return this;
	}

	@Override
	public Wander<T> setDecelerationRadius (float decelerationRadius) {
		this.decelerationRadius = decelerationRadius;
		return this;
	}

	@Override
	public Wander<T> setTimeToTarget (float timeToTarget) {
		this.timeToTarget = timeToTarget;
		return this;
	}

}
