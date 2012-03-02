package com.cloudera.crunch.io;

import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.lib.output.CrunchMultipleOutputs;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import com.cloudera.crunch.type.PType;

public abstract class PathTargetImpl implements PathTarget {

  private final Path path;
  private final Class<OutputFormat> outputFormatClass;
  private final Class keyClass;
  private final Class valueClass;

  public PathTargetImpl(String path, Class<OutputFormat> outputFormatClass,
	  Class keyClass, Class valueClass) {
	this(new Path(path), outputFormatClass, keyClass, valueClass);
  }

  public PathTargetImpl(Path path, Class<OutputFormat> outputFormatClass,
	  Class keyClass, Class valueClass) {
	this.path = path;
	this.outputFormatClass = outputFormatClass;
	this.keyClass = keyClass;
	this.valueClass = valueClass;
  }

  @Override
  public void configureForMapReduce(Job job, PType<?> ptype, Path outputPath,
	  String name) {
    try {
      FileOutputFormat.setOutputPath(job, outputPath);
    } catch (IOException e) {
      throw new RuntimeException("failed to set output path to " + outputPath, e);
    }
    if (name == null) {
      job.setOutputFormatClass(outputFormatClass);
      job.setOutputKeyClass(keyClass);
      job.setOutputValueClass(valueClass);
    } else {
      CrunchMultipleOutputs.addNamedOutput(job, name, outputFormatClass,
          keyClass, valueClass);
    }
  }

  @Override
  public Path getPath() {
	return path;
  }
}
