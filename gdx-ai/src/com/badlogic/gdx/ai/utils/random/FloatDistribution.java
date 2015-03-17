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

package com.badlogic.gdx.ai.utils.random;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/** @author davebaol */
public abstract class FloatDistribution implements Distribution {

	@Override
	public int nextInt () {
		return (int)nextFloat();
	}

	@Override
	public long nextLong () {
		return (long)nextFloat();
	}

	@Override
	public double nextDouble () {
		return (double)nextFloat();
	}

	public static FloatDistribution parse (String value) throws DistributionFormatException {
		StringTokenizer st = new StringTokenizer(value, ", \t\f");
		if (!st.hasMoreTokens()) throw new DistributionFormatException("Missing ditribution type");
		String type = st.nextToken();

		try {
			FloatDistribution dist;
			if (type.equalsIgnoreCase("constant")) {
				dist = new ConstantFloatDistribution(floatToken(st));
			} else if (type.equalsIgnoreCase("uniform")) {
				float a1 = floatToken(st);
				if (!st.hasMoreElements())
					dist = new UniformFloatDistribution(a1);
				else
					dist = new UniformFloatDistribution(a1, floatToken(st));
			} else if (type.equalsIgnoreCase("triangular")) {
				float a1 = floatToken(st);
				if (!st.hasMoreElements())
					dist = new TriangularFloatDistribution(a1);
				else {
					float a2 = floatToken(st);
					if (!st.hasMoreElements())
						dist = new TriangularFloatDistribution(a1, a2);
					else
						dist = new TriangularFloatDistribution(a1, a2, floatToken(st));
				}
			} else if (type.equalsIgnoreCase("gaussian")) {
				dist = new GaussianFloatDistribution(floatToken(st), floatToken(st));
			} else {
				throw new DistributionFormatException("Unknown distribution '" + type + "'");
			}

			if (st.hasMoreTokens()) throw new DistributionFormatException("Too many arguments in distribution '" + type + "'");

			return dist;
		} catch (NumberFormatException nfe) {
			throw new DistributionFormatException("Illegal argument in in distribution '" + type + "'", nfe);
		} catch (NoSuchElementException nsee) {
			throw new DistributionFormatException("Missing argument in distribution '" + type + "'");
		}
	}

	private static float floatToken (StringTokenizer st) {
		return Float.parseFloat(st.nextToken());
	}
}
