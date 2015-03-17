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
public abstract class LongDistribution implements Distribution {

	@Override
	public int nextInt () {
		return (int)nextLong();
	}

	@Override
	public float nextFloat () {
		return (float)nextLong();
	}

	@Override
	public double nextDouble () {
		return (double)nextLong();
	}

	public static LongDistribution parse (String value) throws DistributionFormatException {
		StringTokenizer st = new StringTokenizer(value, ", \t\f");
		if (!st.hasMoreTokens()) throw new DistributionFormatException("Missing ditribution type");
		String type = st.nextToken();

		try {
			LongDistribution dist;
			if (type.equalsIgnoreCase("constant")) {
				dist = new ConstantLongDistribution(longToken(st));
			} else if (type.equalsIgnoreCase("uniform")) {
				long a1 = longToken(st);
				if (!st.hasMoreElements())
					dist = new UniformLongDistribution(a1);
				else
					dist = new UniformLongDistribution(a1, longToken(st));
			} else if (type.equalsIgnoreCase("triangular")) {
				long a1 = longToken(st);
				if (!st.hasMoreElements())
					dist = new TriangularLongDistribution(a1);
				else {
					long a2 = longToken(st);
					if (!st.hasMoreElements())
						dist = new TriangularLongDistribution(a1, a2);
					else
						dist = new TriangularLongDistribution(a1, a2, doubleToken(st));
				}
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

	private static long longToken (StringTokenizer st) {
		return Long.parseLong(st.nextToken());
	}

	private static double doubleToken (StringTokenizer st) {
		return Double.parseDouble(st.nextToken());
	}
}
