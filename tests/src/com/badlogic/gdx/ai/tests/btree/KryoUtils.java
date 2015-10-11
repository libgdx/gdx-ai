
package com.badlogic.gdx.ai.tests.btree;

import org.objenesis.strategy.StdInstantiatorStrategy;

import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.utils.random.ConstantDoubleDistribution;
import com.badlogic.gdx.ai.utils.random.ConstantFloatDistribution;
import com.badlogic.gdx.ai.utils.random.ConstantIntegerDistribution;
import com.badlogic.gdx.ai.utils.random.ConstantLongDistribution;
import com.badlogic.gdx.ai.utils.random.Distribution;
import com.badlogic.gdx.ai.utils.random.GaussianDoubleDistribution;
import com.badlogic.gdx.ai.utils.random.GaussianFloatDistribution;
import com.badlogic.gdx.ai.utils.random.TriangularDoubleDistribution;
import com.badlogic.gdx.ai.utils.random.TriangularFloatDistribution;
import com.badlogic.gdx.ai.utils.random.TriangularIntegerDistribution;
import com.badlogic.gdx.ai.utils.random.TriangularLongDistribution;
import com.badlogic.gdx.ai.utils.random.UniformDoubleDistribution;
import com.badlogic.gdx.ai.utils.random.UniformFloatDistribution;
import com.badlogic.gdx.ai.utils.random.UniformIntegerDistribution;
import com.badlogic.gdx.ai.utils.random.UniformLongDistribution;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Kryo.DefaultInstantiatorStrategy;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.io.OutputChunked;

public final class KryoUtils {

	private static final Kryo kryo;
	private static final OutputChunked output = new OutputChunked();

	private KryoUtils () {
	}

	static {
		kryo = new Kryo();
		kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));

		kryo.register(ConstantDoubleDistribution.class, new DistributionSerializer<ConstantDoubleDistribution>() {
			@Override
			public void write (Kryo kryo, Output output, ConstantDoubleDistribution object) {
				output.writeDouble(object.getValue());
			}

			@Override
			public ConstantDoubleDistribution read (Kryo kryo, Input input, Class<ConstantDoubleDistribution> type) {
				return new ConstantDoubleDistribution(input.readDouble());
			}
		});
		kryo.register(ConstantFloatDistribution.class, new DistributionSerializer<ConstantFloatDistribution>() {
			@Override
			public void write (Kryo kryo, Output output, ConstantFloatDistribution object) {
				output.writeFloat(object.getValue());
			}

			@Override
			public ConstantFloatDistribution read (Kryo kryo, Input input, Class<ConstantFloatDistribution> type) {
				return new ConstantFloatDistribution(input.readFloat());
			}
		});
		kryo.register(ConstantIntegerDistribution.class, new DistributionSerializer<ConstantIntegerDistribution>() {
			@Override
			public void write (Kryo kryo, Output output, ConstantIntegerDistribution object) {
				output.writeInt(object.getValue());
			}

			@Override
			public ConstantIntegerDistribution read (Kryo kryo, Input input, Class<ConstantIntegerDistribution> type) {
				return new ConstantIntegerDistribution(input.readInt());
			}
		});
		kryo.register(ConstantLongDistribution.class, new DistributionSerializer<ConstantLongDistribution>() {
			@Override
			public void write (Kryo kryo, Output output, ConstantLongDistribution object) {
				output.writeLong(object.getValue());
			}

			@Override
			public ConstantLongDistribution read (Kryo kryo, Input input, Class<ConstantLongDistribution> type) {
				return new ConstantLongDistribution(input.readLong());
			}
		});
		kryo.register(GaussianDoubleDistribution.class, new DistributionSerializer<GaussianDoubleDistribution>() {
			@Override
			public void write (Kryo kryo, Output output, GaussianDoubleDistribution object) {
				output.writeDouble(object.getMean());
				output.writeDouble(object.getStandardDeviation());
			}

			@Override
			public GaussianDoubleDistribution read (Kryo kryo, Input input, Class<GaussianDoubleDistribution> type) {
				return new GaussianDoubleDistribution(input.readDouble(), input.readDouble());
			}
		});
		kryo.register(GaussianFloatDistribution.class, new DistributionSerializer<GaussianFloatDistribution>() {
			@Override
			public void write (Kryo kryo, Output output, GaussianFloatDistribution object) {
				output.writeFloat(object.getMean());
				output.writeFloat(object.getStandardDeviation());
			}

			@Override
			public GaussianFloatDistribution read (Kryo kryo, Input input, Class<GaussianFloatDistribution> type) {
				return new GaussianFloatDistribution(input.readFloat(), input.readFloat());
			}
		});
		kryo.register(TriangularDoubleDistribution.class, new DistributionSerializer<TriangularDoubleDistribution>() {
			@Override
			public void write (Kryo kryo, Output output, TriangularDoubleDistribution object) {
				output.writeDouble(object.getLow());
				output.writeDouble(object.getHigh());
				output.writeDouble(object.getMode());
			}

			@Override
			public TriangularDoubleDistribution read (Kryo kryo, Input input, Class<TriangularDoubleDistribution> type) {
				return new TriangularDoubleDistribution(input.readDouble(), input.readDouble(), input.readDouble());
			}
		});
		kryo.register(TriangularFloatDistribution.class, new DistributionSerializer<TriangularFloatDistribution>() {
			@Override
			public void write (Kryo kryo, Output output, TriangularFloatDistribution object) {
				output.writeFloat(object.getLow());
				output.writeFloat(object.getHigh());
				output.writeFloat(object.getMode());
			}

			@Override
			public TriangularFloatDistribution read (Kryo kryo, Input input, Class<TriangularFloatDistribution> type) {
				return new TriangularFloatDistribution(input.readFloat(), input.readFloat(), input.readFloat());
			}
		});
		kryo.register(TriangularIntegerDistribution.class, new DistributionSerializer<TriangularIntegerDistribution>() {
			@Override
			public void write (Kryo kryo, Output output, TriangularIntegerDistribution object) {
				output.writeInt(object.getLow());
				output.writeInt(object.getHigh());
				output.writeFloat(object.getMode());
			}

			@Override
			public TriangularIntegerDistribution read (Kryo kryo, Input input, Class<TriangularIntegerDistribution> type) {
				return new TriangularIntegerDistribution(input.readInt(), input.readInt(), input.readFloat());
			}
		});
		kryo.register(TriangularLongDistribution.class, new DistributionSerializer<TriangularLongDistribution>() {
			@Override
			public void write (Kryo kryo, Output output, TriangularLongDistribution object) {
				output.writeLong(object.getLow());
				output.writeLong(object.getHigh());
				output.writeDouble(object.getMode());
			}

			@Override
			public TriangularLongDistribution read (Kryo kryo, Input input, Class<TriangularLongDistribution> type) {
				return new TriangularLongDistribution(input.readLong(), input.readLong(), input.readDouble());
			}
		});
		kryo.register(UniformDoubleDistribution.class, new DistributionSerializer<UniformDoubleDistribution>() {
			@Override
			public void write (Kryo kryo, Output output, UniformDoubleDistribution object) {
				output.writeDouble(object.getLow());
				output.writeDouble(object.getHigh());
			}

			@Override
			public UniformDoubleDistribution read (Kryo kryo, Input input, Class<UniformDoubleDistribution> type) {
				return new UniformDoubleDistribution(input.readDouble(), input.readDouble());
			}
		});
		kryo.register(UniformFloatDistribution.class, new DistributionSerializer<UniformFloatDistribution>() {
			@Override
			public void write (Kryo kryo, Output output, UniformFloatDistribution object) {
				output.writeFloat(object.getLow());
				output.writeFloat(object.getHigh());
			}

			@Override
			public UniformFloatDistribution read (Kryo kryo, Input input, Class<UniformFloatDistribution> type) {
				return new UniformFloatDistribution(input.readFloat(), input.readFloat());
			}
		});
		kryo.register(UniformIntegerDistribution.class, new DistributionSerializer<UniformIntegerDistribution>() {
			@Override
			public void write (Kryo kryo, Output output, UniformIntegerDistribution object) {
				output.writeInt(object.getLow());
				output.writeInt(object.getHigh());
			}

			@Override
			public UniformIntegerDistribution read (Kryo kryo, Input input, Class<UniformIntegerDistribution> type) {
				return new UniformIntegerDistribution(input.readInt(), input.readInt());
			}
		});
		kryo.register(UniformLongDistribution.class, new DistributionSerializer<UniformLongDistribution>() {
			@Override
			public void write (Kryo kryo, Output output, UniformLongDistribution object) {
				output.writeLong(object.getLow());
				output.writeLong(object.getHigh());
			}

			@Override
			public UniformLongDistribution read (Kryo kryo, Input input, Class<UniformLongDistribution> type) {
				return new UniformLongDistribution(input.readLong(), input.readLong());
			}
		});

		kryo.register(BehaviorTree.class);
		// FieldSerializer fieldSerializer = new FieldSerializer(kryo, BehaviorTree.class);
		// fieldSerializer.removeField("object");
		// kryo.register(BehaviorTree.class, fieldSerializer);
	}

	public static void save (Object obj) {
		output.clear();
		kryo.writeObjectOrNull(output, obj, obj.getClass());
		// System.out.println(output.total());
	}

	public static <T> T load (Class<T> type) {
		Input input = new ByteBufferInput(output.getBuffer());
		return kryo.readObjectOrNull(input, type);
	}

	public static <T> T copy (T object) {
		return kryo.copy(object);
	}

	static abstract class DistributionSerializer<T extends Distribution> extends Serializer<T> {
		public DistributionSerializer () {
			super(false, true);
		}
	}
}
