/**
 * Copyright (c) 2011, Cloudera, Inc. All Rights Reserved.
 *
 * Cloudera, Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"). You may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * This software is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the
 * License.
 */
package com.cloudera.crunch.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;

import com.google.common.collect.UnmodifiableIterator;

public class CompositePathIterable<T> implements Iterable<T> {

  private final FileStatus[] stati;
  private final FileSystem fs;
  private final FileReaderFactory<T> readerFactory;

  private static final PathFilter FILTER = new PathFilter() {
	@Override
	public boolean accept(Path path) {
	  return !path.getName().startsWith("_");
	}
  };

  public static <S> Iterable<S> create(FileSystem fs, Path path, FileReaderFactory<S> readerFactory) throws IOException {
    FileStatus[] stati;
    try {
      stati = fs.listStatus(path, FILTER);
    } catch (FileNotFoundException fnfe) {
      stati = new FileStatus[0];
    }
	if (stati.length == 0) {
	  throw new IOException("No files found to materialize at: " + path);
	}
	return new CompositePathIterable<S>(stati, fs, readerFactory);
  }

  private CompositePathIterable(FileStatus[] stati, FileSystem fs, FileReaderFactory<T> readerFactory) {
	this.stati = stati;
	this.fs = fs;
	this.readerFactory = readerFactory;
  }

  @Override
  public Iterator<T> iterator() {
	return new UnmodifiableIterator<T>() {
	  private int index = 0;
	  private Iterator<T> iter = readerFactory.read(fs, stati[index++].getPath());

	  @Override
	  public boolean hasNext() {
		if (!iter.hasNext()) {
		  while (index < stati.length) {
       	    iter = readerFactory.read(fs, stati[index++].getPath());
			if (iter.hasNext()) {
			  return true;
			}
		  }
		  return false;
		}
		return true;
	  }

	  @Override
	  public T next() {
		return iter.next();
	  }
	};
  }
}
