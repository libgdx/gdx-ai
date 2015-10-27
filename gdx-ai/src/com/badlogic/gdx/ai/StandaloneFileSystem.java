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

package com.badlogic.gdx.ai;

import java.io.File;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** @author davebaol */
public class StandaloneFileSystem implements FileSystem {

	public StandaloneFileSystem () {
	}

	@Override
	public FileHandleResolver newResolver (final FileType fileType) {
		return new FileHandleResolver() {

			@Override
			public FileHandle resolve (String fileName) {
				return new DesktopFileHandle(fileName, fileType);
			}
		};
	}

	@Override
	public FileHandle newFileHandle (String fileName) {
		return new DesktopFileHandle(fileName, FileType.Absolute);
	}

	@Override
	public FileHandle newFileHandle (File file) {
		return new DesktopFileHandle(file, FileType.Absolute);
	}

	@Override
	public FileHandle newFileHandle (String fileName, FileType type) {
		return new DesktopFileHandle(fileName, type);
	}

	@Override
	public FileHandle newFileHandle (File file, FileType type) {
		return new DesktopFileHandle(file, type);
	}

	public static class DesktopFileHandle extends FileHandle {

		static public final String externalPath = System.getProperty("user.home") + File.separator;
		static public final String localPath = new File("").getAbsolutePath() + File.separator;

		public DesktopFileHandle (String fileName, FileType type) {
			super(fileName, type);
		}

		public DesktopFileHandle (File file, FileType type) {
			super(file, type);
		}

		public FileHandle child (String name) {
			if (file.getPath().length() == 0) return new DesktopFileHandle(new File(name), type);
			return new DesktopFileHandle(new File(file, name), type);
		}

		public FileHandle sibling (String name) {
			if (file.getPath().length() == 0) throw new GdxRuntimeException("Cannot get the sibling of the root.");
			return new DesktopFileHandle(new File(file.getParent(), name), type);
		}

		public FileHandle parent () {
			File parent = file.getParentFile();
			if (parent == null) {
				if (type == FileType.Absolute)
					parent = new File("/");
				else
					parent = new File("");
			}
			return new DesktopFileHandle(parent, type);
		}

		public File file () {
			if (type == FileType.External) return new File(DesktopFileHandle.externalPath, file.getPath());
			if (type == FileType.Local) return new File(DesktopFileHandle.localPath, file.getPath());
			return file;
		}
	}
}
