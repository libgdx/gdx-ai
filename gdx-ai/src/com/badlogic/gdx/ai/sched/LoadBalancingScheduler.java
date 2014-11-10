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

import com.badlogic.gdx.ai.utils.ArithmeticUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.TimeUtils;

/** A {@code LoadBalancingScheduler} understands the time it has to run and distributes this time among the tasks that need to be
 * run. This scheduler splits the time it is given according to the number of tasks that must be run on this frame. To adjust for
 * small errors in the running time of tasks, this scheduler recalculates the time it has left after each task is run. This way an
 * overrunning task will reduce the time that is given to others run in the same frame.
 * <p>
 * The scheduler takes tasks, each one having a frequency and a phase that determine when it should be run.
 * <ul>
 * <li><b>Frequency:</b> On each time frame, the scheduler is called to manage the whole AI budget. It decides which behaviors
 * need to be run and calls them. This is done by keeping count of the number of frames passed. This is incremented each time the
 * scheduler is called. It is easy to test if each behavior should be run by checking if the frame count is evenly divisible by
 * the frequency. On its own, this approach suffers from clumping: some frames with no tasks being run, and other frames with
 * several tasks sharing the budget. Picking frequencies that are relatively prime makes the clash points less frequent but
 * doesn't eliminate them. To solve the problem, we use the phase.</li>
 * <li><b>Phase:</b> The phase doesn't change the frequency but offsets when the task will be called. However, calculating good
 * phase values to avoid spikes can be difficult. It is not intuitively clear whether a particular set of frequency and phase
 * values will lead to a regular spike or not. That's why this scheduler supports automatic phasing. When a new behavior is added
 * to the scheduler, with a frequency of {@code f}, we perform a dry run of the scheduler for a fixed number of frames into the
 * future. Rather than executing behaviors in this dry run, we simply count how many would be executed. We find the frame with the
 * least number of running tasks. The phase value for the task is set to the number of frames ahead at which this minimum occurs.
 * The fixed number of frames is normally a manually set value found by experimentation. Ideally, it would be the least common
 * multiple (LCM) of all the frequency values used in the scheduler, see {@link ArithmeticUtils#lcmPositive(int, int)}. Typically,
 * however, this is a large number and would slow the algorithm unnecessarily (for frequencies of 2, 3, 5, 7, and 11, for example,
 * we have an LCM of 2310). Despite being a good approach in practice, it has a theoretical chance that it will still produce
 * heavy spikes, if the lookahead isn't at least as large as the size of the LCM.</li>
 * </ul>
 * 
 * @author davebaol */
public class LoadBalancingScheduler implements Scheduler {

	/** The list of the scheduled tasks. */
	protected Array<SchedulableRecord> schedulableRecords;

	protected Array<SchedulableRecord> runList;

	protected IntArray phaseCounters;

	protected int dryRunFrames;

	/** The current frame number */
	protected int frame;

	/** Creates a {@code LoadBalancingScheduler}.
	 * @param dryRunFrames number of frames simulated by the dry run to calculate the phase when adding a schedulable via
	 *           {@link #add(Schedulable, int)} */
	public LoadBalancingScheduler (int dryRunFrames) {
		this.schedulableRecords = new Array<SchedulableRecord>();
		this.runList = new Array<SchedulableRecord>();
		this.phaseCounters = new IntArray();
		this.dryRunFrames = dryRunFrames;
		this.frame = 0;
	}

	/** Adds the {@code schedulable} to the list using the given {@code frequency} and a phase calculated by a dry run of the
	 * scheduler.
	 * @param schedulable the task to schedule
	 * @param frequency the frequency */
	@Override
	public void add (Schedulable schedulable, int frequency) {
		// Calculate the phase and add the schedulable to the list
		add(schedulable, frequency, calculatePhase(frequency));
	}

	@Override
	public void add (Schedulable schedulable, int frequency, int phase) {
		// Compile the record and add it to the list
		schedulableRecords.add(new SchedulableRecord(schedulable, frequency, phase));
	}

	/** Executes scheduled tasks based on their frequency and phase. This method must be called once per frame.
	 * @param timeToRun the maximum time in nanoseconds this scheduler should run on the current frame. */
	@Override
	public void run (long timeToRun) {
		// Increment the frame number
		frame++;

		// Clear the list of behaviors to run
		runList.size = 0;

		// Go through each behavior
		for (int i = 0; i < schedulableRecords.size; i++) {
			SchedulableRecord record = schedulableRecords.get(i);
			// If it is due, schedule it
			if ((frame + record.phase) % record.frequency == 0) runList.add(record);
		}

		// Keep track of the current time
		long lastTime = TimeUtils.nanoTime();

		// Find the number of behaviors we need to run
		int numToRun = runList.size;

		// Go through the behaviors to run
		for (int i = 0; i < numToRun; i++) {
			// Find the available time
			long currentTime = TimeUtils.nanoTime();
			timeToRun -= currentTime - lastTime;
			long availableTime = timeToRun / (numToRun - i);

			// Run the schedulable object
			runList.get(i).schedulable.run(availableTime);

			// Store the current time
			lastTime = currentTime;
		}
	}

	/** This method is invoked by {@link #add(Schedulable, int)} and calculates the best phase based on the number of frames of the
	 * dry run. The optimal phase is guaranteed if the number of simulated frames is at least as large as the size of the least
	 * common multiple (LCM, see {@link ArithmeticUtils#lcmPositive(int, int)}) of all the frequency values used in the scheduler
	 * so far.
	 * @param frequency the frequency of the skedulable task to add
	 * @return the best phase based on the length of the dry run. */
	protected int calculatePhase (int frequency) {
		if (frequency > phaseCounters.size) phaseCounters.ensureCapacity(frequency - phaseCounters.size);

		int[] items = phaseCounters.items;

		// Reset counters
		phaseCounters.size = frequency;
		for (int i = 0; i < frequency; i++)
			items[i] = 0;

		// Perform a dry run
		for (int frame = 0; frame < dryRunFrames; frame++) {
			int xxx = frame % frequency;
			// Go through each behavior
			for (int i = 0; i < schedulableRecords.size; i++) {
				SchedulableRecord record = schedulableRecords.get(i);
				// If it is due, count it
				if ((frame - record.phase) % record.frequency == 0) items[xxx]++;
			}
		}

		int minValue = Integer.MAX_VALUE;
		int minValueAt = -1;
		for (int i = 0; i < frequency; i++) {
			if (items[i] < minValue) {
				minValue = items[i];
				minValueAt = i;
			}
		}

		// Return the phase
		return minValueAt;
	}

	/** A scheduled behavior.
	 * 
	 * @author davebaol */
	static class SchedulableRecord {
		Schedulable schedulable;
		int frequency;
		int phase;

		SchedulableRecord (Schedulable schedulable, int frequency, int phase) {
			this.schedulable = schedulable;
			this.frequency = frequency;
			this.phase = phase;
		}
	}
}
