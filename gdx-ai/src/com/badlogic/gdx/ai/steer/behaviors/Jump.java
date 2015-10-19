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
import com.badlogic.gdx.ai.steer.Limiter;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteerableAdapter;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.math.Vector;

/** First the {@code Jump} behavior calculates the linear velocity required to achieve the jump. If the calculated velocity doesn't
 * exceed the maximum linear velocity the jump is achievable; otherwise it's not. In either cases, the given callback gets
 * informed through the {@link JumpCallback#reportAchievability(boolean) reportAchievability} method. Also, if the jump is
 * achievable the run up phase begins and the {@code Jump} behavior will start to produce the linear acceleration required to match
 * the calculated velocity. Once the jump point and the linear velocity are reached with a precision within the given tolerance
 * the callback is told to jump through the {@link JumpCallback#takeoff(float, float) takeoff} method.
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @author davebaol */
public class Jump<T extends Vector<T>> extends MatchVelocity<T> {

	public static boolean DEBUG_ENABLED = false;

	/** The jump descriptor to use */
	protected JumpDescriptor<T> jumpDescriptor;

	/** The gravity vector to use. Notice that this behavior only supports gravity along a single axis, which must be the one
	 * returned by the {@link GravityComponentHandler#getComponent(Vector)} method. */
	protected T gravity;

	protected GravityComponentHandler<T> gravityComponentHandler;

	protected JumpCallback callback;

	protected float takeoffPositionTolerance;
	protected float takeoffVelocityTolerance;

	/** The maximum vertical component of jump velocity, where "vertical" stands for the axis where gravity operates. */
	protected float maxVerticalVelocity;

	/** Keeps track of whether the jump is achievable */
	private boolean isJumpAchievable;

	protected float airborneTime = 0;

	private JumpTarget<T> jumpTarget;
	private T planarVelocity;

	/** Creates a {@code Jump} behavior.
	 * @param owner the owner of this behavior
	 * @param jumpDescriptor the descriptor of the jump to make
	 * @param gravity the gravity vector
	 * @param gravityComponentHandler the handler giving access to the vertical axis
	 * @param callback the callback that gets informed about jump achievability and when to jump */
	public Jump (Steerable<T> owner, JumpDescriptor<T> jumpDescriptor, T gravity,
		GravityComponentHandler<T> gravityComponentHandler, JumpCallback callback) {
		super(owner);
		this.gravity = gravity;
		this.gravityComponentHandler = gravityComponentHandler;
		setJumpDescriptor(jumpDescriptor);
		this.callback = callback;

		this.jumpTarget = new JumpTarget<T>(owner);
		this.planarVelocity = newVector(owner);
	}

	@Override
	public SteeringAcceleration<T> calculateRealSteering (SteeringAcceleration<T> steering) {
		// Check if we have a trajectory, and create one if not.
		if (target == null) {
			target = calculateTarget();
			callback.reportAchievability(isJumpAchievable);
		}

		// If the trajectory is zero, return no steering acceleration
		if (!isJumpAchievable) return steering.setZero();

		// Check if the owner has reached target position and velocity with acceptable tolerance
		if (owner.getPosition().epsilonEquals(target.getPosition(), takeoffPositionTolerance)) {
			if (DEBUG_ENABLED) GdxAI.getLogger().info("Jump", "Good position!!!");
			if (owner.getLinearVelocity().epsilonEquals(target.getLinearVelocity(), takeoffVelocityTolerance)) {
				if (DEBUG_ENABLED) GdxAI.getLogger().info("Jump", "Good Velocity!!!");
				isJumpAchievable = false;
				// Perform the jump, and return no steering (the owner is airborne, no need to steer).
				callback.takeoff(maxVerticalVelocity, airborneTime);
				return steering.setZero();
			} else {
				if (DEBUG_ENABLED)
					GdxAI.getLogger().info("Jump",
						"Bad Velocity: Speed diff. = "
							+ planarVelocity.set(target.getLinearVelocity()).sub(owner.getLinearVelocity()).len() + ", diff = ("
							+ planarVelocity + ")");
			}
		}

		// Delegate to MatchVelocity
		return super.calculateRealSteering(steering);
	}

	/** Works out the trajectory calculation. */
	private Steerable<T> calculateTarget () {
		this.jumpTarget.position = jumpDescriptor.takeoffPosition;
		this.airborneTime = calculateAirborneTimeAndVelocity(jumpTarget.linearVelocity, jumpDescriptor, getActualLimiter()
			.getMaxLinearSpeed());
		this.isJumpAchievable = airborneTime >= 0;
		return jumpTarget;
	}

	/** Returns the airborne time and sets the {@code outVelocity} vector to the airborne planar velocity required to achieve the
	 * jump. If the jump is not achievable -1 is returned and the {@code outVelocity} vector remains unchanged.
	 * <p>
	 * Be aware that you should avoid using unlimited or very high max velocity, because this might produce a time of flight close
	 * to 0. Actually, the motion equation for T has 2 solutions and Jump always try to use the fastest time.
	 * @param outVelocity the output vector where the airborne planar velocity is calculated
	 * @param jumpDescriptor the jump descriptor
	 * @param maxLinearSpeed the maximum linear speed that can be used to achieve the jump
	 * @return the time of flight or -1 if the jump is not achievable using the given max linear speed. */
	public float calculateAirborneTimeAndVelocity (T outVelocity, JumpDescriptor<T> jumpDescriptor, float maxLinearSpeed) {
		float g = gravityComponentHandler.getComponent(gravity);

		// Calculate the first jump time, see time of flight at http://hyperphysics.phy-astr.gsu.edu/hbase/traj.html
		// Notice that the equation has 2 solutions. We'd ideally like to achieve the jump in the fastest time
		// possible, so we want to use the smaller of the two values. However, this time value might give us
		// an impossible launch velocity (required speed greater than the max), so we need to check and
		// use the higher value if necessary.
		float sqrtTerm = (float)Math.sqrt(2f * g * gravityComponentHandler.getComponent(jumpDescriptor.delta)
			+ maxVerticalVelocity * maxVerticalVelocity);
		float time = (-maxVerticalVelocity + sqrtTerm) / g;
		if (DEBUG_ENABLED) GdxAI.getLogger().info("Jump", "1st jump time = " + time);

		// Check if we can use it
		if (!checkAirborneTimeAndCalculateVelocity(outVelocity, time, jumpDescriptor, maxLinearSpeed)) {
			// Otherwise try the other time
			time = (-maxVerticalVelocity - sqrtTerm) / g;
			if (DEBUG_ENABLED) GdxAI.getLogger().info("Jump", "2nd jump time = " + time);
			if (!checkAirborneTimeAndCalculateVelocity(outVelocity, time, jumpDescriptor, maxLinearSpeed)) {
				return -1f; // Unachievable jump
			}
		}
		return time; // Achievable jump
	}

	private boolean checkAirborneTimeAndCalculateVelocity (T outVelocity, float time, JumpDescriptor<T> jumpDescriptor,
		float maxLinearSpeed) {
		// Calculate the planar velocity
		planarVelocity.set(jumpDescriptor.delta).scl(1f / time);
		gravityComponentHandler.setComponent(planarVelocity, 0f);

		// Check the planar linear speed
		if (planarVelocity.len2() < maxLinearSpeed * maxLinearSpeed) {
			// We have a valid solution, so store it by merging vertical and non-vertical axes
			float verticalValue = gravityComponentHandler.getComponent(outVelocity);
			gravityComponentHandler.setComponent(outVelocity.set(planarVelocity), verticalValue);
			if (DEBUG_ENABLED)
				GdxAI.getLogger().info("Jump", "targetLinearVelocity = " + outVelocity + "; targetLinearSpeed = " + outVelocity.len());
			return true;
		}
		return false;
	}

	/** Returns the jump descriptor. */
	public JumpDescriptor<T> getJumpDescriptor () {
		return jumpDescriptor;
	}

	/** Sets the jump descriptor to use.
	 * @param jumpDescriptor the jump descriptor to set
	 * @return this behavior for chaining. */
	public Jump<T> setJumpDescriptor (JumpDescriptor<T> jumpDescriptor) {
		this.jumpDescriptor = jumpDescriptor;
		this.target = null;
		this.isJumpAchievable = false;
		return this;
	}

	/** Returns the gravity vector. */
	public T getGravity () {
		return gravity;
	}

	/** Sets the gravity vector.
	 * @param gravity the gravity to set
	 * @return this behavior for chaining. */
	public Jump<T> setGravity (T gravity) {
		this.gravity = gravity;
		return this;
	}

	/** Returns the maximum vertical component of jump velocity, where "vertical" stands for the axis where gravity operates. */
	public float getMaxVerticalVelocity () {
		return maxVerticalVelocity;
	}

	/** Sets the maximum vertical component of jump velocity, where "vertical" stands for the axis where gravity operates.
	 * @param maxVerticalVelocity the maximum vertical velocity to set
	 * @return this behavior for chaining. */
	public Jump<T> setMaxVerticalVelocity (float maxVerticalVelocity) {
		this.maxVerticalVelocity = maxVerticalVelocity;
		return this;
	}

	/** Returns the tolerance used to check if the owner has reached the takeoff location. */
	public float getTakeoffPositionTolerance () {
		return takeoffPositionTolerance;
	}

	/** Sets the tolerance used to check if the owner has reached the takeoff location.
	 * @param takeoffPositionTolerance the takeoff position tolerance to set
	 * @return this behavior for chaining. */
	public Jump<T> setTakeoffPositionTolerance (float takeoffPositionTolerance) {
		this.takeoffPositionTolerance = takeoffPositionTolerance;
		return this;
	}

	/** Returns the tolerance used to check if the owner has reached the takeoff velocity. */
	public float getTakeoffVelocityTolerance () {
		return takeoffVelocityTolerance;
	}

	/** Sets the tolerance used to check if the owner has reached the takeoff velocity.
	 * @param takeoffVelocityTolerance the takeoff velocity tolerance to set
	 * @return this behavior for chaining. */
	public Jump<T> setTakeoffVelocityTolerance (float takeoffVelocityTolerance) {
		this.takeoffVelocityTolerance = takeoffVelocityTolerance;
		return this;
	}

	/** Sets the the tolerance used to check if the owner has reached the takeoff location with the required velocity.
	 * @param takeoffTolerance the takeoff tolerance for both position and velocity
	 * @return this behavior for chaining. */
	public Jump<T> setTakeoffTolerance (float takeoffTolerance) {
		setTakeoffPositionTolerance(takeoffTolerance);
		setTakeoffVelocityTolerance(takeoffTolerance);
		return this;
	}

	//
	// Setters overridden in order to fix the correct return type for chaining
	//

	@Override
	public Jump<T> setOwner (Steerable<T> owner) {
		this.owner = owner;
		return this;
	}

	@Override
	public Jump<T> setEnabled (boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	/** Sets the limiter of this steering behavior. The given limiter must at least take care of the maximum linear acceleration and
	 * speed.
	 * @return this behavior for chaining. */
	@Override
	public Jump<T> setLimiter (Limiter limiter) {
		this.limiter = limiter;
		return this;
	}

	/** Sets the target whose velocity should be matched. Notice that this method is inherited from {@link MatchVelocity}. Usually
	 * with {@code Jump} you should never call it because a specialized internal target has already been created implicitly.
	 * @param target the target to set
	 * @return this behavior for chaining. */
	@Override
	public Jump<T> setTarget (Steerable<T> target) {
		this.target = target;
		return this;
	}

	@Override
	public Jump<T> setTimeToTarget (float timeToTarget) {
		this.timeToTarget = timeToTarget;
		return this;
	}

	//
	// Nested classes and interfaces
	//

	private static class JumpTarget<T extends Vector<T>> extends SteerableAdapter<T> {

		T position;
		T linearVelocity;

		public JumpTarget (Steerable<T> other) {
			this.position = null;
			this.linearVelocity = other.getPosition().cpy().setZero();
		}

		@Override
		public T getPosition () {
			return position;
		}

		@Override
		public T getLinearVelocity () {
			return linearVelocity;
		}
	}

	/** A {@code JumpDescriptor} contains jump information like the take-off and the landing position.
	 * 
	 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
	 * 
	 * @author davebaol */
	public static class JumpDescriptor<T extends Vector<T>> {
		/** The position of the takeoff pad */
		public T takeoffPosition;

		/** The position of the landing pad */
		public T landingPosition;

		/** The change in position from takeoff to landing. This is calculated from the other values. */
		public T delta;

		/** Creates a {@code JumpDescriptor} with the given takeoff and landing positions.
		 * @param takeoffPosition the position of the takeoff pad
		 * @param landingPosition the position of the landing pad */
		public JumpDescriptor (T takeoffPosition, T landingPosition) {
			this.takeoffPosition = takeoffPosition;
			this.landingPosition = landingPosition;
			this.delta = landingPosition.cpy();
			set(takeoffPosition, landingPosition);
		}

		/** Sets this {@code JumpDescriptor} from the given takeoff and landing positions.
		 * @param takeoffPosition the position of the takeoff pad
		 * @param landingPosition the position of the landing pad */
		public void set (T takeoffPosition, T landingPosition) {
			this.takeoffPosition.set(takeoffPosition);
			this.landingPosition.set(landingPosition);
			this.delta.set(landingPosition).sub(takeoffPosition);
		}
	}

	/** A {@code GravityComponentHandler} is aware of the axis along which the gravity acts.
	 * 
	 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
	 * 
	 * @author davebaol */
	public interface GravityComponentHandler<T extends Vector<T>> {

		/** Returns the component of the given vector along which the gravity operates.
		 * <p>
		 * Assuming a 3D coordinate system where the gravity is acting along the y-axis, this method will be implemented as follows:
		 * 
		 * <pre>
		 * public float getComponent (Vector3 vector) {
		 * 	return vector.y;
		 * }
		 * </pre>
		 * 
		 * Of course, the equivalent 2D implementation will use Vector2 instead of Vector3.
		 * @param vector the vector
		 * @return the value of the component affected by gravity. */
		public float getComponent (T vector);

		/** Sets the component of the given vector along which the gravity operates.
		 * <p>
		 * Assuming a 3D coordinate system where the gravity is acting along the y-axis, this method will be implemented as follows:
		 * 
		 * <pre>
		 * public void setComponent (Vector3 vector, float value) {
		 * 	vector.y = value;
		 * }
		 * </pre>
		 * 
		 * Of course, the equivalent 2D implementation will use Vector2 instead of Vector3.
		 * @param vector the vector
		 * @param value the value of the component affected by gravity */
		public void setComponent (T vector, float value);
	}

	/** The {@code JumpCallback} allows you to know whether a jump is achievable and when to jump.
	 * 
	 * @author davebaol */
	public interface JumpCallback {

		/** Reports whether the jump is achievable or not.
		 * <p>
		 * A jump is not achievable when the character's maximum linear velocity is not enough, in which case the jump behavior
		 * won't produce any acceleration; you might want to use pathfinding to plan a new path.
		 * <p>
		 * If the jump is achievable the run up phase will start immediately and the character will try to match the target velocity
		 * toward the takeoff point. This is the right moment to start the run up animation, if needed.
		 * @param achievable whether the jump is achievable or not. */
		public void reportAchievability (boolean achievable);

		/** This method is called to notify that both the position and velocity of the character are good enough to jump.
		 * @param maxVerticalVelocity the velocity to set along the vertical axis to achieve the jump
		 * @param time the duration of the jump */
		public void takeoff (float maxVerticalVelocity, float time);

	}
}
