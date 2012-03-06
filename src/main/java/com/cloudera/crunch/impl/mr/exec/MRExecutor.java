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
package com.cloudera.crunch.impl.mr.exec;

import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.mapreduce.lib.jobcontrol.ControlledJob;
import org.apache.hadoop.mapreduce.lib.jobcontrol.JobControl;

/**
 *
 *
 */
public class MRExecutor {

  private static final Log LOG = LogFactory.getLog(MRExecutor.class);

  private final JobControl control;

  public MRExecutor(Class<?> jarClass) {
    this.control = new JobControl(jarClass.toString());
  }

  public void addJob(CrunchJob job) {
    this.control.addJob(job);
  }

  public void execute() {
    try {
      Thread controlThread = new Thread(control);
      controlThread.start();
      while (!control.allFinished()) {
        Thread.sleep(1000);
      }
      control.stop();
    } catch (InterruptedException e) {
      LOG.info(e);
    }
    List<ControlledJob> failures = control.getFailedJobList();
    if (!failures.isEmpty()) {
      System.err.println(failures.size() + " job failure(s) occurred:");
      for (ControlledJob job : failures) {
        System.err.println(job.getJobName() + "(" + job.getJobID() + "): " + job.getMessage());
      }
    } else {
      for (ControlledJob controlledJob: this.control.getSuccessfulJobList()) {
        if (controlledJob instanceof CrunchJob) {
          try {
            ((CrunchJob)controlledJob).handleMultiPaths();
          } catch (IOException e) {
            LOG.error("Failed to handle output paths for " + controlledJob + ":", e);
          }
        }
      }
    }
  }
}
