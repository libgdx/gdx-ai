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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.AbsoluteFileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.ClasspathFileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.ExternalFileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.LocalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;

/** @author davebaol */
public class GdxFileSystem implements FileSystem {

	public GdxFileSystem () {
	}

	@Override
	public FileHandleResolver newResolver (FileType fileType) {
		switch (fileType) {
		case Absolute:
			return new AbsoluteFileHandleResolver();
		case Classpath:
			return new ClasspathFileHandleResolver();
		case External:
			return new ExternalFileHandleResolver();
		case Internal:
			return new InternalFileHandleResolver();
		case Local:
			return new LocalFileHandleResolver();
		}
		return null; // Should never happen
	}

	@Override
	public FileHandle newFileHandle (String fileName) {
		return Gdx.files.absolute(fileName);
	}

	@Override
	public FileHandle newFileHandle (File file) {
		return Gdx.files.absolute(file.getAbsolutePath());
	}

	@Override
	public FileHandle newFileHandle (String fileName, FileType type) {
		return Gdx.files.getFileHandle(fileName, type);
	}

	@Override
	public FileHandle newFileHandle (File file, FileType type) {
		return Gdx.files.getFileHandle(file.getPath(), type);
	}

}
