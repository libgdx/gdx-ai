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

package com.badlogic.gdx.ai.pfa;

import com.badlogic.gdx.math.Vector;

/** A request for interruptible path smoothing.
 * 
 * @param <N> Type of node
 * @param <V> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @author davebaol */
public class PathSmootherRequest<N, V extends Vector<V>> {

	public boolean isNew;
	public int outputIndex;
	public int inputIndex;
	public SmoothableGraphPath<N, V> path;

	/** Creates an empty {@code PathSmootherRequest} */
	public PathSmootherRequest () {
		isNew = true;
	}

	public void refresh (SmoothableGraphPath<N, V> path) {
		this.path = path;
		this.isNew = true;
	}

}
