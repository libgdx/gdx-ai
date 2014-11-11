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

import com.badlogic.gdx.ai.sched.PriorityScheduler.PrioritySchedulableRecord;
import com.badlogic.gdx.utils.TimeUtils;

/** A {@code PriorityScheduler} works like a {@link LoadBalancingScheduler} but allows different tasks to get a different share of
 * the available time by assigning a priority to each task. The higher the priority the longer the amount of the available time
 * dedicated to the corresponding task.
 * 
 * @author davebaol */
public class PriorityScheduler extends SchedulerBase<PrioritySchedulableRecord> {

	/** The current frame number */
	protected int frame;

	/** Creates a {@code PriorityScheduler}.
	 * @param dryRunFrames number of frames simulated by the dry run to calculate the phase when adding a schedulable via
	 *           {@link #addWithAutomaticPhasing(Schedulable, int)} and {@link #addWithAutomaticPhasing(Schedulable, int, float)} */
	public PriorityScheduler (int dryRunFrames) {
		super(dryRunFrames);
		this.frame = 0;
	}

	/** Executes scheduled tasks based on their frequency and phase. This method must be called once per frame.
	 * @param timeToRun the maximum time in nanoseconds this scheduler should run on the current frame. */
	@Override
	public void run (long timeToRun) {
		// Increment the frame number
		frame++;

		// Clear the list of tasks to run and their total priority
		runList.size = 0;
		float totalPriority = 0;

		// Go through each task
		for (int i = 0; i < schedulableRecords.size; i++) {
			PrioritySchedulableRecord record = schedulableRecords.get(i);
			// If it is due, schedule it
			if ((frame + record.phase) % record.frequency == 0) {
				runList.add(record);
				totalPriority += record.priority;
			}
		}

		// Keep track of the current time
		long lastTime = TimeUtils.nanoTime();

		// Find the number of tasks we need to run
		int numToRun = runList.size;

		// Go through the tasks to run
		for (int i = 0; i < numToRun; i++) {
			// Find the available time
			long currentTime = TimeUtils.nanoTime();
			timeToRun -= currentTime - lastTime;
			PrioritySchedulableRecord record = runList.get(i);
			long availableTime = (long)(timeToRun * record.priority / totalPriority);

			// Run the schedulable object
			record.schedulable.run(availableTime);

			// Store the current time
			lastTime = currentTime;
		}
	}

	/** Adds the {@code schedulable} to the list using the given {@code frequency}, priority 1 and a phase calculated by a dry run
	 * of the scheduler.
	 * @param schedulable the task to schedule
	 * @param frequency the frequency */
	@Override
	public void addWithAutomaticPhasing (Schedulable schedulable, int frequency) {
		addWithAutomaticPhasing(schedulable, frequency, 1f);
	}

	/** Adds the {@code schedulable} to the list using the given {@code frequency} and {@code priority} while the phase is
	 * calculated by a dry run of the scheduler.
	 * @param schedulable the task to schedule
	 * @param frequency the frequency
	 * @param priority the priority */
	public void addWithAutomaticPhasing (Schedulable schedulable, int frequency, float priority) {
		// Calculate the phase and add the schedulable to the list
		add(schedulable, frequency, calculatePhase(frequency), priority);
	}

	/** Adds the {@code schedulable} to the list using the given {@code frequency} and {@code phase} with priority 1.
	 * @param schedulable the task to schedule
	 * @param frequency the frequency
	 * @param phase the phase */
	@Override
	public void add (Schedulable schedulable, int frequency, int phase) {
		add(schedulable, frequency, phase, 1f);
	}

	/** Adds the {@code schedulable} to the list using the given {@code frequency}, {@code phase} and priority.
	 * @param schedulable the task to schedule
	 * @param frequency the frequency
	 * @param phase the phase
	 * @param priority the priority */
	public void add (Schedulable schedulable, int frequency, int phase, float priority) {
		// Compile the record and add it to the list
		schedulableRecords.add(new PrioritySchedulableRecord(schedulable, frequency, phase, priority));
	}

	/** A scheduled task with priority.
	 * 
	 * @author davebaol */
	static class PrioritySchedulableRecord extends com.badlogic.gdx.ai.sched.SchedulerBase.SchedulableRecord {
		float priority;

		PrioritySchedulableRecord (Schedulable schedulable, int frequency, int phase, float priority) {
			super(schedulable, frequency, phase);
			this.priority = priority;
		}
	}

}
