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
package com.cloudera.crunch.io.seq;

import org.apache.hadoop.fs.Path;

import com.cloudera.crunch.io.impl.ReadableSourcePathTargetImpl;
import com.cloudera.crunch.type.PType;

public class SeqFileSourceTarget<T> extends ReadableSourcePathTargetImpl<T> {
  public SeqFileSourceTarget(String path, PType<T> ptype) {
    this(new Path(path), ptype);
  }
  
  public SeqFileSourceTarget(Path path, PType<T> ptype) {
    super(new SeqFileSource<T>(path, ptype), new SeqFileTarget(path));
  }
}
