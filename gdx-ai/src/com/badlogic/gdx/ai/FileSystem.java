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

/** The {@code FileSystem} interface exposes the underlying file system(s).
 * @author davebaol */
public interface FileSystem {

	public FileHandleResolver newResolver(FileType fileType);

	public FileHandle newFileHandle(String fileName);

	public FileHandle newFileHandle(File file);
	
	public FileHandle newFileHandle(String fileName, FileType type);

	public FileHandle newFileHandle(File file, FileType type);

}
