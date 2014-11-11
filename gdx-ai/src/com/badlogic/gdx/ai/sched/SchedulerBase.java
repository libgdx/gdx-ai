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

/** @author davebaol */
public abstract class SchedulerBase<T extends SchedulerBase.SchedulableRecord> implements Scheduler {

	/** The list of the scheduled tasks. */
	protected Array<T> schedulableRecords;

	protected Array<T> runList;

	protected IntArray phaseCounters;

	protected int dryRunFrames;

	/** Creates a {@code SchedulerBase}.
	 * @param dryRunFrames number of frames simulated by the dry run to calculate the phase when adding a schedulable via
	 *           {@link #addWithAutomaticPhasing(Schedulable, int)} */
	public SchedulerBase (int dryRunFrames) {
		this.schedulableRecords = new Array<T>();
		this.runList = new Array<T>();
		this.phaseCounters = new IntArray();
		this.dryRunFrames = dryRunFrames;
	}

	/** This method is invoked by {@link #addWithAutomaticPhasing(Schedulable, int)} and calculates the best phase based on the
	 * number of frames of the dry run. The optimal phase is guaranteed if the number of simulated frames is at least as large as
	 * the size of the least common multiple (LCM, see {@link ArithmeticUtils#lcmPositive(int, int)}) of all the frequency values
	 * used in the scheduler so far.
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
			int slot = frame % frequency;
			// Go through each task
			for (int i = 0; i < schedulableRecords.size; i++) {
				T record = schedulableRecords.get(i);
				// If it is due, count it
				if ((frame - record.phase) % record.frequency == 0) items[slot]++;
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

	/** A scheduled task.
	 * 
	 * @author davebaol */
	protected static class SchedulableRecord {
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
