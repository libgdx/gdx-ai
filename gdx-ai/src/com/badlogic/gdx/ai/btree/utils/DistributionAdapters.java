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

package com.badlogic.gdx.ai.btree.utils;

import java.util.StringTokenizer;

import com.badlogic.gdx.ai.utils.random.ConstantDoubleDistribution;
import com.badlogic.gdx.ai.utils.random.ConstantFloatDistribution;
import com.badlogic.gdx.ai.utils.random.ConstantIntegerDistribution;
import com.badlogic.gdx.ai.utils.random.ConstantLongDistribution;
import com.badlogic.gdx.ai.utils.random.Distribution;
import com.badlogic.gdx.ai.utils.random.DistributionFormatException;
import com.badlogic.gdx.ai.utils.random.DoubleDistribution;
import com.badlogic.gdx.ai.utils.random.FloatDistribution;
import com.badlogic.gdx.ai.utils.random.GaussianDoubleDistribution;
import com.badlogic.gdx.ai.utils.random.GaussianFloatDistribution;
import com.badlogic.gdx.ai.utils.random.IntegerDistribution;
import com.badlogic.gdx.ai.utils.random.LongDistribution;
import com.badlogic.gdx.ai.utils.random.TriangularDoubleDistribution;
import com.badlogic.gdx.ai.utils.random.TriangularFloatDistribution;
import com.badlogic.gdx.ai.utils.random.TriangularIntegerDistribution;
import com.badlogic.gdx.ai.utils.random.TriangularLongDistribution;
import com.badlogic.gdx.ai.utils.random.UniformDoubleDistribution;
import com.badlogic.gdx.ai.utils.random.UniformFloatDistribution;
import com.badlogic.gdx.ai.utils.random.UniformIntegerDistribution;
import com.badlogic.gdx.ai.utils.random.UniformLongDistribution;
import com.badlogic.gdx.utils.ObjectMap;

/** @author davebaol */
public class DistributionAdapters {

	public abstract static class Adapter<D extends Distribution> {
		final String category;
		final Class<?> type;

		public Adapter (String category, Class<?> type) {
			this.category = category;
			this.type = type;
		}

		public abstract D toDistribution (String[] args) throws DistributionFormatException;

		public abstract String[] toParameters (D distribution);

		public static double parseDouble (String v) {
			try {
				return Double.parseDouble(v);
			} catch (NumberFormatException nfe) {
				throw new DistributionFormatException("Not a double value: " + v, nfe);
			}
		}

		public static float parseFloat (String v) {
			try {
				return Float.parseFloat(v);
			} catch (NumberFormatException nfe) {
				throw new DistributionFormatException("Not a float value: " + v, nfe);
			}
		}

		public static int parseInteger (String v) {
			try {
				return Integer.parseInt(v);
			} catch (NumberFormatException nfe) {
				throw new DistributionFormatException("Not an int value: " + v, nfe);
			}
		}

		public static long parseLong (String v) {
			try {
				return Long.parseLong(v);
			} catch (NumberFormatException nfe) {
				throw new DistributionFormatException("Not a long value: " + v, nfe);
			}
		}

	}

	public abstract static class DoubleAdapter<D extends DoubleDistribution> extends Adapter<D> {
		public DoubleAdapter (String category) {
			super(category, DoubleDistribution.class);
		}
	}

	public abstract static class FloatAdapter<D extends FloatDistribution> extends Adapter<D> {
		public FloatAdapter (String category) {
			super(category, FloatDistribution.class);
		}
	}

	public abstract static class IntegerAdapter<D extends IntegerDistribution> extends Adapter<D> {
		public IntegerAdapter (String category) {
			super(category, IntegerDistribution.class);
		}
	}

	public abstract static class LongAdapter<D extends LongDistribution> extends Adapter<D> {
		public LongAdapter (String category) {
			super(category, LongDistribution.class);
		}
	}

	private static final ObjectMap<Class<?>, Adapter<?>> adapters = new ObjectMap<Class<?>, Adapter<?>>();
	static {
		//
		// Constant distributions
		//

		adapters.put(ConstantDoubleDistribution.class, new DoubleAdapter<ConstantDoubleDistribution>("constant") {

			@Override
			public ConstantDoubleDistribution toDistribution (String[] args) throws DistributionFormatException {
				if (args.length != 1) throw new DistributionFormatException();
				return new ConstantDoubleDistribution(parseDouble(args[0]));
			}

			@Override
			public String[] toParameters (ConstantDoubleDistribution distribution) {
				return new String[] {Double.toString(distribution.getValue())};
			}
		});

		adapters.put(ConstantFloatDistribution.class, new FloatAdapter<ConstantFloatDistribution>("constant") {

			@Override
			public ConstantFloatDistribution toDistribution (String[] args) throws DistributionFormatException {
				if (args.length != 1) throw new DistributionFormatException();
				return new ConstantFloatDistribution(parseFloat(args[0]));
			}

			@Override
			public String[] toParameters (ConstantFloatDistribution distribution) {
				return new String[] {Float.toString(distribution.getValue())};
			}
		});

		adapters.put(ConstantIntegerDistribution.class, new IntegerAdapter<ConstantIntegerDistribution>("constant") {

			@Override
			public ConstantIntegerDistribution toDistribution (String[] args) throws DistributionFormatException {
				if (args.length != 1) throw new DistributionFormatException();
				return new ConstantIntegerDistribution(parseInteger(args[0]));
			}

			@Override
			public String[] toParameters (ConstantIntegerDistribution distribution) {
				return new String[] {Integer.toString(distribution.getValue())};
			}
		});

		adapters.put(ConstantLongDistribution.class, new LongAdapter<ConstantLongDistribution>("constant") {

			@Override
			public ConstantLongDistribution toDistribution (String[] args) throws DistributionFormatException {
				if (args.length != 1) throw new DistributionFormatException();
				return new ConstantLongDistribution(parseLong(args[0]));
			}

			@Override
			public String[] toParameters (ConstantLongDistribution distribution) {
				return new String[] {Long.toString(distribution.getValue())};
			}
		});

		//
		// Gaussian distributions
		//

		adapters.put(GaussianDoubleDistribution.class, new DoubleAdapter<GaussianDoubleDistribution>("gaussian") {

			@Override
			public GaussianDoubleDistribution toDistribution (String[] args) throws DistributionFormatException {
				if (args.length != 2) throw new DistributionFormatException();
				return new GaussianDoubleDistribution(parseDouble(args[0]), parseDouble(args[1]));
			}

			@Override
			public String[] toParameters (GaussianDoubleDistribution distribution) {
				return new String[] {Double.toString(distribution.getMean()), Double.toString(distribution.getStandardDeviation())};
			}
		});

		adapters.put(GaussianFloatDistribution.class, new FloatAdapter<GaussianFloatDistribution>("gaussian") {

			@Override
			public GaussianFloatDistribution toDistribution (String[] args) throws DistributionFormatException {
				if (args.length != 2) throw new DistributionFormatException();
				return new GaussianFloatDistribution(parseFloat(args[0]), parseFloat(args[1]));
			}

			@Override
			public String[] toParameters (GaussianFloatDistribution distribution) {
				return new String[] {Float.toString(distribution.getMean()), Float.toString(distribution.getStandardDeviation())};
			}
		});

		//
		// Triangular distributions
		//

		adapters.put(TriangularDoubleDistribution.class, new DoubleAdapter<TriangularDoubleDistribution>("triangular") {

			@Override
			public TriangularDoubleDistribution toDistribution (String[] args) throws DistributionFormatException {
				switch (args.length) {
				case 1:
					return new TriangularDoubleDistribution(parseDouble(args[0]));
				case 2:
					return new TriangularDoubleDistribution(parseDouble(args[0]), parseDouble(args[1]));
				case 3:
					return new TriangularDoubleDistribution(parseDouble(args[0]), parseDouble(args[1]), parseDouble(args[2]));
				default:
					throw new DistributionFormatException();
				}
			}

			@Override
			public String[] toParameters (TriangularDoubleDistribution distribution) {
				return new String[] {Double.toString(distribution.getLow()), Double.toString(distribution.getHigh()),
					Double.toString(distribution.getMode())};
			}
		});

		adapters.put(TriangularFloatDistribution.class, new FloatAdapter<TriangularFloatDistribution>("triangular") {

			@Override
			public TriangularFloatDistribution toDistribution (String[] args) throws DistributionFormatException {
				switch (args.length) {
				case 1:
					return new TriangularFloatDistribution(parseFloat(args[0]));
				case 2:
					return new TriangularFloatDistribution(parseFloat(args[0]), parseFloat(args[1]));
				case 3:
					return new TriangularFloatDistribution(parseFloat(args[0]), parseFloat(args[1]), parseFloat(args[2]));
				default:
					throw new DistributionFormatException(
						"Wrong number of arguments in triangular distribution; expected one, two or three");
				}
			}

			@Override
			public String[] toParameters (TriangularFloatDistribution distribution) {
				return new String[] {Float.toString(distribution.getLow()), Float.toString(distribution.getHigh()),
					Float.toString(distribution.getMode())};
			}
		});

		adapters.put(TriangularIntegerDistribution.class, new IntegerAdapter<TriangularIntegerDistribution>("triangular") {

			@Override
			public TriangularIntegerDistribution toDistribution (String[] args) throws DistributionFormatException {
				switch (args.length) {
				case 1:
					return new TriangularIntegerDistribution(parseInteger(args[0]));
				case 2:
					return new TriangularIntegerDistribution(parseInteger(args[0]), parseInteger(args[1]));
				case 3:
					return new TriangularIntegerDistribution(parseInteger(args[0]), parseInteger(args[1]), Float.valueOf(args[2]));
				default:
					throw new DistributionFormatException();
				}
			}

			@Override
			public String[] toParameters (TriangularIntegerDistribution distribution) {
				return new String[] {Integer.toString(distribution.getLow()), Integer.toString(distribution.getHigh()),
					Float.toString(distribution.getMode())};
			}
		});

		adapters.put(TriangularLongDistribution.class, new LongAdapter<TriangularLongDistribution>("triangular") {

			@Override
			public TriangularLongDistribution toDistribution (String[] args) throws DistributionFormatException {
				switch (args.length) {
				case 1:
					return new TriangularLongDistribution(parseLong(args[0]));
				case 2:
					return new TriangularLongDistribution(parseLong(args[0]), parseLong(args[1]));
				case 3:
					return new TriangularLongDistribution(parseLong(args[0]), parseLong(args[1]), parseDouble(args[2]));
				default:
					throw new DistributionFormatException();
				}
			}

			@Override
			public String[] toParameters (TriangularLongDistribution distribution) {
				return new String[] {Long.toString(distribution.getLow()), Long.toString(distribution.getHigh()),
					Double.toString(distribution.getMode())};
			}
		});

		//
		// Uniform distributions
		//

		adapters.put(UniformDoubleDistribution.class, new DoubleAdapter<UniformDoubleDistribution>("uniform") {

			@Override
			public UniformDoubleDistribution toDistribution (String[] args) throws DistributionFormatException {
				switch (args.length) {
				case 1:
					return new UniformDoubleDistribution(parseDouble(args[0]));
				case 2:
					return new UniformDoubleDistribution(parseDouble(args[0]), parseDouble(args[1]));
				default:
					throw new DistributionFormatException();
				}
			}

			@Override
			public String[] toParameters (UniformDoubleDistribution distribution) {
				return new String[] {Double.toString(distribution.getLow()), Double.toString(distribution.getHigh())};
			}
		});

		adapters.put(UniformFloatDistribution.class, new FloatAdapter<UniformFloatDistribution>("uniform") {

			@Override
			public UniformFloatDistribution toDistribution (String[] args) throws DistributionFormatException {
				switch (args.length) {
				case 1:
					return new UniformFloatDistribution(parseFloat(args[0]));
				case 2:
					return new UniformFloatDistribution(parseFloat(args[0]), parseFloat(args[1]));
				default:
					throw new DistributionFormatException();
				}
			}

			@Override
			public String[] toParameters (UniformFloatDistribution distribution) {
				return new String[] {Float.toString(distribution.getLow()), Float.toString(distribution.getHigh())};
			}
		});

		adapters.put(UniformIntegerDistribution.class, new IntegerAdapter<UniformIntegerDistribution>("uniform") {

			@Override
			public UniformIntegerDistribution toDistribution (String[] args) throws DistributionFormatException {
				switch (args.length) {
				case 1:
					return new UniformIntegerDistribution(parseInteger(args[0]));
				case 2:
					return new UniformIntegerDistribution(parseInteger(args[0]), parseInteger(args[1]));
				default:
					throw new DistributionFormatException();
				}
			}

			@Override
			public String[] toParameters (UniformIntegerDistribution distribution) {
				return new String[] {Integer.toString(distribution.getLow()), Integer.toString(distribution.getHigh())};
			}
		});

		adapters.put(UniformLongDistribution.class, new LongAdapter<UniformLongDistribution>("uniform") {

			@Override
			public UniformLongDistribution toDistribution (String[] args) throws DistributionFormatException {
				switch (args.length) {
				case 1:
					return new UniformLongDistribution(parseLong(args[0]));
				case 2:
					return new UniformLongDistribution(parseLong(args[0]), parseLong(args[1]));
				default:
					throw new DistributionFormatException();
				}
			}

			@Override
			public String[] toParameters (UniformLongDistribution distribution) {
				return new String[] {Long.toString(distribution.getLow()), Long.toString(distribution.getHigh())};
			}
		});
	}

	ObjectMap<Class<?>, Adapter<?>> map;
	ObjectMap<Class<?>, ObjectMap<String, Adapter<?>>> typeMap;

	public DistributionAdapters () {
		this.map = new ObjectMap<Class<?>, Adapter<?>>();
		this.typeMap = new ObjectMap<Class<?>, ObjectMap<String, Adapter<?>>>();
		for (ObjectMap.Entry<Class<?>, Adapter<?>> e : adapters.entries())
			add(e.key, e.value);
	}

	public final void add (Class<?> clazz, Adapter<?> adapter) {
		map.put(clazz, adapter);

		ObjectMap<String, Adapter<?>> m = typeMap.get(adapter.type);
		if (m == null) {
			m = new ObjectMap<String, Adapter<?>>();
			typeMap.put(adapter.type, m);
		}
		m.put(adapter.category, adapter);
	}

	@SuppressWarnings("unchecked")
	public <T extends Distribution> T toDistribution (String value, Class<T> clazz) throws DistributionFormatException {
		StringTokenizer st = new StringTokenizer(value, ", \t\f");
		if (!st.hasMoreTokens()) throw new DistributionFormatException("Missing ditribution type");
		String type = st.nextToken();
		ObjectMap<String, Adapter<?>> categories = typeMap.get(clazz);
		Adapter<T> converter = (Adapter<T>)categories.get(type);
		if (converter == null)
			throw new DistributionFormatException("Cannot create a '" + clazz.getSimpleName() + "' of type '" + type + "'");
		String[] args = new String[st.countTokens()];
		for (int i = 0; i < args.length; i++)
			args[i] = st.nextToken();
		return converter.toDistribution(args);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public String toString (Distribution distribution) {
		Adapter adapter = map.get(distribution.getClass());
		String args[] = adapter.toParameters(distribution);
		String out = adapter.category;
		for (String a : args)
			out += "," + a;
		return out;

	}

	public static void main (String[] args) {
		DistributionAdapters ds = new DistributionAdapters();
		Distribution d = ds.toDistribution("triangular,re", FloatDistribution.class);
		System.out.println(d);
		System.out.println(ds.toString(d));
	}
}
