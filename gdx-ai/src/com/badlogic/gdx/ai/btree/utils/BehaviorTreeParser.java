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

import java.io.InputStream;
import java.io.Reader;

import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.annotation.TaskAttribute;
import com.badlogic.gdx.ai.btree.annotation.TaskConstraint;
import com.badlogic.gdx.ai.btree.branch.Parallel;
import com.badlogic.gdx.ai.btree.branch.RandomSelector;
import com.badlogic.gdx.ai.btree.branch.RandomSequence;
import com.badlogic.gdx.ai.btree.branch.Selector;
import com.badlogic.gdx.ai.btree.branch.Sequence;
import com.badlogic.gdx.ai.btree.decorator.AlwaysFail;
import com.badlogic.gdx.ai.btree.decorator.AlwaysSucceed;
import com.badlogic.gdx.ai.btree.decorator.Include;
import com.badlogic.gdx.ai.btree.decorator.Invert;
import com.badlogic.gdx.ai.btree.decorator.Random;
import com.badlogic.gdx.ai.btree.decorator.Repeat;
import com.badlogic.gdx.ai.btree.decorator.SemaphoreGuard;
import com.badlogic.gdx.ai.btree.decorator.UntilFail;
import com.badlogic.gdx.ai.btree.decorator.UntilSuccess;
import com.badlogic.gdx.ai.btree.leaf.Failure;
import com.badlogic.gdx.ai.btree.leaf.Success;
import com.badlogic.gdx.ai.btree.leaf.Wait;
import com.badlogic.gdx.ai.utils.random.Distribution;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entries;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.SerializationException;
import com.badlogic.gdx.utils.reflect.Annotation;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import com.badlogic.gdx.utils.reflect.ReflectionException;

/** A {@link BehaviorTree} parser.
 * 
 * @author davebaol */
public class BehaviorTreeParser<E> {

	public static final int DEBUG_NONE = 0;
	public static final int DEBUG_LOW = 1;
	public static final int DEBUG_HIGH = 2;

	public int debug;
	public DistributionAdapters distributionAdapters;

	private ConcreteBehaviorTreeReader<E> btReader;

	public BehaviorTreeParser () {
		this(DEBUG_NONE);
	}

	public BehaviorTreeParser (DistributionAdapters distributionAdapters) {
		this(distributionAdapters, DEBUG_NONE);
	}

	public BehaviorTreeParser (int debug) {
		this(new DistributionAdapters(), debug);
	}

	public BehaviorTreeParser (DistributionAdapters distributionAdapters, int debug) {
		this.distributionAdapters = distributionAdapters;
		this.debug = debug;
		btReader = new ConcreteBehaviorTreeReader<E>(this);
	}

	/** Parses the given string.
	 * @param string the string to parse
	 * @param object the blackboard object. It can be {@code null}.
	 * @return the behavior tree
	 * @throws SerializationException if the string cannot be successfully parsed. */
	public BehaviorTree<E> parse (String string, E object) {
		btReader.parse(string);
		return createBehaviorTree(btReader.root, object);
	}

	/** Parses the given input stream.
	 * @param input the input stream to parse
	 * @param object the blackboard object. It can be {@code null}.
	 * @return the behavior tree
	 * @throws SerializationException if the input stream cannot be successfully parsed. */
	public BehaviorTree<E> parse (InputStream input, E object) {
		btReader.parse(input);
		return createBehaviorTree(btReader.root, object);
	}

	/** Parses the given file.
	 * @param file the file to parse
	 * @param object the blackboard object. It can be {@code null}.
	 * @return the behavior tree
	 * @throws SerializationException if the file cannot be successfully parsed. */
	public BehaviorTree<E> parse (FileHandle file, E object) {
		btReader.parse(file);
		return createBehaviorTree(btReader.root, object);
	}

	/** Parses the given reader.
	 * @param reader the reader to parse
	 * @param object the blackboard object. It can be {@code null}.
	 * @return the behavior tree
	 * @throws SerializationException if the reader cannot be successfully parsed. */
	public BehaviorTree<E> parse (Reader reader, E object) {
		btReader.parse(reader);
		return createBehaviorTree(btReader.root, object);
	}

	protected BehaviorTree<E> createBehaviorTree (Task<E> root, E object) {
		if (debug > BehaviorTreeParser.DEBUG_LOW) printTree(root, 0);
		return new BehaviorTree<E>(root, object);
	}

	protected void printTree (Task<E> task, int indent) {
		for (int i = 0; i < indent; i++)
			System.out.print(' ');
		System.out.println(task.getClass().getSimpleName());
		for (int i = 0; i < task.getChildCount(); i++) {
			printTree(task.getChild(i), indent + 2);
		}
	}

	static class ConcreteBehaviorTreeReader<E> extends BehaviorTreeReader {

		private static final ObjectMap<String, String> DEFAULT_IMPORTS = new ObjectMap<String, String>();
		static {
			Class<?>[] classes = new Class<?>[] {// @off - disable libgdx formatter
				AlwaysFail.class,
				AlwaysSucceed.class,
				Failure.class,
				Include.class,
				Invert.class,
				Parallel.class,
				Random.class,
				RandomSelector.class,
				RandomSequence.class,
				Repeat.class,
				Selector.class,
				SemaphoreGuard.class,
				Sequence.class,
				Success.class,
				UntilFail.class,
				UntilSuccess.class,
				Wait.class
			}; // @on - enable libgdx formatter
			for (Class<?> c : classes) {
				String fqcn = c.getName();
				String cn = c.getSimpleName();
				String alias = Character.toLowerCase(cn.charAt(0)) + (cn.length() > 1 ? cn.substring(1) : "");
				DEFAULT_IMPORTS.put(alias, fqcn);
			}
		}

		private static final int TAG_NONE = -1;
		private static final int TAG_IMPORT = 0;
		private static final int TAG_ROOT = 1;

		private static final String[] STATEMENTS = new String[] {"import", "root"};

		BehaviorTreeParser<E> btParser;

		ObjectMap<String, String> userImports = new ObjectMap<String, String>();

		ObjectMap<Class<?>, Metadata> metadataCache = new ObjectMap<Class<?>, Metadata>();

		Task<E> root;
		Array<StackedTask<E>> stack = new Array<StackedTask<E>>();
		ObjectSet<String> encounteredAttributes = new ObjectSet<String>();
		int tagType;
		boolean isTask;
		int currentDepth;
		StackedTask<E> prevTask;
		int step;
		int rootIndent;

		ConcreteBehaviorTreeReader (BehaviorTreeParser<E> btParser) {
			this.btParser = btParser;
		}

		@Override
		public void parse (char[] data, int offset, int length) {
			debug = btParser.debug > BehaviorTreeParser.DEBUG_NONE;
			tagType = TAG_NONE;
			isTask = false;
			userImports.clear();
			root = null;
			prevTask = null;
			currentDepth = -1;
			step = 1;
			stack.clear();
			encounteredAttributes.clear();
			super.parse(data, offset, length);

			// Pop all task from the stack and check their minimum number of children
			popAndCheckMinChildren(0);

			if (root == null) throw new GdxRuntimeException("The tree must have at least the root task");
		}

		@Override
		protected void startStatement (int indent, String name) {
			if (btParser.debug > BehaviorTreeParser.DEBUG_LOW)
				System.out.println(lineNumber + ": <" + indent + "> task name '" + name + "'");
			if (tagType == TAG_ROOT)
				openTask(indent, name);
			else {
				boolean validStatement = openTag(name);
				if (!validStatement) {
					if (btParser.debug > BehaviorTreeParser.DEBUG_LOW) {
						System.out.println("validStatement: " + validStatement);
						System.out.println("getImport(name): " + getImport(name));
					}
					if (getImport(name) != null) {
						// root statement is optional
						tagType = TAG_ROOT;
						openTask(indent, name);
						return;
					}
					throw new GdxRuntimeException("Unknown tag '" + name + "'");
				}
			}
		}

		@Override
		protected void attribute (String name, Object value) {
			if (btParser.debug > BehaviorTreeParser.DEBUG_LOW)
				System.out.println(lineNumber + ": attribute '" + name + " : " + value + "'");
			if (isTask) {
				if (!attributeTask(name, value)) throw new GdxRuntimeException(prevTask.name + ": unknown attribute '" + name + "'");
			} else {
				if (!attributeTag(name, value))
					throw new GdxRuntimeException(STATEMENTS[tagType] + ": unknown attribute '" + name + "'");
			}
		}

		private boolean attributeTask (String name, Object value) {
			AttrInfo ai = prevTask.metadata.attributes.get(name);
			if (ai == null) return false;
			boolean isNew = encounteredAttributes.add(name);
			if (!isNew) throw new GdxRuntimeException(prevTask.name + ": attribute '" + name + "' specified more than once");
			Field attributeField = getField(prevTask.task.getClass(), ai.fieldName);
			setField(attributeField, prevTask.task, value);
			return true;
		}

		private Field getField (Class<?> clazz, String name) {
			try {
				return ClassReflection.getField(clazz, name);
			} catch (ReflectionException e) {
				throw new GdxRuntimeException(e);
			}
		}

		private void setField (Field field, Task<E> task, Object value) {
			field.setAccessible(true);
			Object valueObject = castValue(field, value);
			try {
				field.set(task, valueObject);
			} catch (ReflectionException e) {
				throw new GdxRuntimeException(e);
			}
		}

		private Object castValue (Field field, Object value) {
			Class<?> type = field.getType();
			Object ret = null;
			if (value instanceof Number) {
				Number numberValue = (Number)value;
				if (type == int.class || type == Integer.class)
					ret = numberValue.intValue();
				else if (type == float.class || type == Float.class)
					ret = numberValue.floatValue();
				else if (type == long.class || type == Long.class)
					ret = numberValue.longValue();
				else if (type == double.class || type == Double.class)
					ret = numberValue.doubleValue();
				else if (type == short.class || type == Short.class)
					ret = numberValue.shortValue();
				else if (type == byte.class || type == Byte.class)
					ret = numberValue.byteValue();
				else if (ClassReflection.isAssignableFrom(Distribution.class, type)) {
					@SuppressWarnings("unchecked")
					Class<Distribution> distributionType = (Class<Distribution>)type;
					ret = btParser.distributionAdapters.toDistribution("constant," + numberValue, distributionType);
				}
			} else if (value instanceof Boolean) {
				if (type == boolean.class || type == Boolean.class) ret = value;
			} else if (value instanceof String) {
				String stringValue = (String)value;
				if (type == String.class)
					ret = value;
				else if (type == char.class || type == Character.class) {
					if (stringValue.length() != 1) throw new GdxRuntimeException("Invalid character '" + value + "'");
					ret = Character.valueOf(stringValue.charAt(0));
				} else if (ClassReflection.isAssignableFrom(Distribution.class, type)) {
					@SuppressWarnings("unchecked")
					Class<Distribution> distributionType = (Class<Distribution>)type;
					ret = btParser.distributionAdapters.toDistribution(stringValue, distributionType);
				} else if (ClassReflection.isAssignableFrom(Enum.class, type)) {
					Enum<?>[] constants = (Enum<?>[])type.getEnumConstants();
					for (int i = 0, n = constants.length; i < n; i++) {
						Enum<?> e = constants[i];
						if (e.name().equalsIgnoreCase(stringValue)) {
							ret = e;
							break;
						}
					}
				}
			}
			if (ret == null) throwAttributeTypeException(prevTask.name, field.getName(), type.getSimpleName());
			return ret;
		}

		private boolean attributeTag (String name, Object value) {
			if (tagType == TAG_IMPORT) {
				if (value instanceof String)
					addImport(name, (String)value);
				else
					throwAttributeTypeException(STATEMENTS[tagType], name, "String");
			}
			return true;
		}

		private void throwAttributeTypeException (String statement, String name, String expectedType) {
			throw new GdxRuntimeException(statement + ": attribute '" + name + "' must be of type " + expectedType);
		}

		@Override
		protected void endStatement () {
			if (isTask) {
				isTask = (stack.size != 0);
				if (isTask) {
					checkRequiredAttributes(prevTask);
					encounteredAttributes.clear();
				}
			} else {
// if (tagType == TAG_IMPORT) {
// addImport(importTask, importAs);
// }
// // Reset the tag type to the parent
// if (tagType != TAG_NONE) {
// tagType = TAGS[tagType].parentIndex;
// }
			}
		}

		private void addImport (String alias, String task) {
			if (task == null) throw new GdxRuntimeException("import: missing task class name.");
			if (alias == null) {
				Class<?> clazz = null;
				try {
					clazz = ClassReflection.forName(task);
				} catch (ReflectionException e) {
					throw new GdxRuntimeException("import: class not found '" + task + "'");
				}
				alias = clazz.getSimpleName();
			}
			String className = getImport(alias);
			if (className != null) throw new GdxRuntimeException("import: alias '" + alias + "' previously defined already.");
			userImports.put(alias, task);
		}

		private String getImport (String as) {
			String className = DEFAULT_IMPORTS.get(as);
			return className != null ? className : userImports.get(as);
		}

		private boolean openTag (String name) {
			for (int i = 0; i < STATEMENTS.length; i++) {
				String tag = STATEMENTS[i];
				if (name.equals(tag)) {
					tagType = i;
					return true;
				}
			}
			return false;
		}

		private void openTask (int indent, String name) {
			isTask = true;
			String className = getImport(name);
			if (className == null) className = name;
			try {
				@SuppressWarnings("unchecked")
				Task<E> task = (Task<E>)ClassReflection.newInstance(ClassReflection.forName(className));

				if (prevTask == null) {
					root = task;
					rootIndent = indent;
					indent = 0;
				} else {
					indent -= rootIndent;
					if (prevTask.task == root) {
						step = indent;
					}
					if (indent > currentDepth) {
						stack.add(prevTask); // push
					} else if (indent <= currentDepth) {
						// Pop tasks from the stack based on indentation
						// and check their minimum number of children
						int i = (currentDepth - indent) / step;
						popAndCheckMinChildren(stack.size - i);
					}

					// Check the max number of children of the parent
					StackedTask<E> stackedParent = stack.peek();
					int maxChildren = stackedParent.metadata.maxChildren;
					if (stackedParent.task.getChildCount() >= maxChildren)
						throw new GdxRuntimeException(stackedParent.name + ": max number of children exceeded ("
							+ (stackedParent.task.getChildCount() + 1) + " > " + maxChildren + ")");

					// Add child task to the parent
					stackedParent.task.addChild(task);
				}
				prevTask = createStackedTask(name, task);
				currentDepth = indent;
			} catch (ReflectionException e) {
				throw new GdxRuntimeException("Cannot parse behavior tree!!!", e);
			}
		}

		private void popAndCheckMinChildren (int upToFloor) {
			// Check the minimum number of children in prevTask
			if (prevTask != null) checkMinChildren(prevTask);

			// Check the minimum number of children while popping up to the specified floor
			while (stack.size > upToFloor) {
				StackedTask<E> stackedTask = stack.pop();
				checkMinChildren(stackedTask);
			}
		}

		private void checkMinChildren (StackedTask<E> stackedTask) {
			// Check the minimum number of children
			int minChildren = stackedTask.metadata.minChildren;
			if (stackedTask.task.getChildCount() < minChildren)
				throw new GdxRuntimeException(stackedTask.name + ": not enough children (" + stackedTask.task.getChildCount() + " < "
					+ minChildren + ")");
		}

		private void checkRequiredAttributes (StackedTask<E> stackedTask) {
			// Check the minimum number of children
			Entries<String, AttrInfo> entries = stackedTask.metadata.attributes.iterator();
			while (entries.hasNext()) {
				Entry<String, AttrInfo> entry = entries.next();
				if (entry.value.required && !encounteredAttributes.contains(entry.key))
					throw new GdxRuntimeException(stackedTask.name + ": missing required attribute '" + entry.key + "'");
			}
		}

		private StackedTask<E> createStackedTask (String name, Task<E> task) {
			Metadata metadata = findMetadata(task.getClass());
			if (metadata == null)
				throw new GdxRuntimeException(name + ": @TaskConstraint annotation not found in '" + task.getClass().getSimpleName()
					+ "' class hierarchy");
			return new StackedTask<E>(name, task, metadata);
		}

		private Metadata findMetadata (Class<?> clazz) {
			Metadata metadata = metadataCache.get(clazz);
			if (metadata == null) {
				Annotation tca = ClassReflection.getAnnotation(clazz, TaskConstraint.class);
				if (tca != null) {
					TaskConstraint taskConstraint = tca.getAnnotation(TaskConstraint.class);
					ObjectMap<String, AttrInfo> taskAttributes = new ObjectMap<String, AttrInfo>();
					Field[] fields = ClassReflection.getFields(clazz);
					for (Field f : fields) {
						Annotation a = f.getDeclaredAnnotation(TaskAttribute.class);
						if (a != null) {
							AttrInfo ai = new AttrInfo(f.getName(), a.getAnnotation(TaskAttribute.class));
							taskAttributes.put(ai.name, ai);
						}
					}
					metadata = new Metadata(taskConstraint.minChildren(), taskConstraint.maxChildren(), taskAttributes);
					metadataCache.put(clazz, metadata);
				}
			}
			return metadata;
		}

		private static class StackedTask<E> {
			String name;
			Task<E> task;
			Metadata metadata;

			StackedTask (String name, Task<E> task, Metadata metadata) {
				this.name = name;
				this.task = task;
				this.metadata = metadata;
			}
		}

		private static class Metadata {
			int minChildren;
			int maxChildren;
			ObjectMap<String, AttrInfo> attributes;

			/** Creates a {@code Metadata} for a task accepting from {@code minChildren} to {@code maxChildren} children and the given
			 * attributes.
			 * @param minChildren the minimum number of children (defaults to 0 if negative)
			 * @param maxChildren the maximum number of children (defaults to {@link Integer.MAX_VALUE} if negative)
			 * @param attributes the attributes */
			Metadata (int minChildren, int maxChildren, ObjectMap<String, AttrInfo> attributes) {
				this.minChildren = minChildren < 0 ? 0 : minChildren;
				this.maxChildren = maxChildren < 0 ? Integer.MAX_VALUE : maxChildren;
				this.attributes = attributes;
			}
		}

		private static class AttrInfo {
			String name;
			String fieldName;
			boolean required;

			AttrInfo (String fieldName, TaskAttribute annotation) {
				this(annotation.name(), fieldName, annotation.required());
			}

			AttrInfo (String name, String fieldName, boolean required) {
				this.name = name == null || name.length() == 0 ? fieldName : name;
				this.fieldName = fieldName;
				this.required = required;
			}
		}
	}
}
