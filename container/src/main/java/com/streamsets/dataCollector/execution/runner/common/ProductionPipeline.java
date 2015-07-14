/**
 * (c) 2014 StreamSets, Inc. All rights reserved. May not
 * be copied, modified, or distributed in whole or part without
 * written consent of StreamSets, Inc.
 */
package com.streamsets.dataCollector.execution.runner.common;

import com.streamsets.dataCollector.execution.PipelineStatus;
import com.streamsets.dataCollector.execution.StateListener;
import com.streamsets.pipeline.api.Record;
import com.streamsets.pipeline.api.StageException;
import com.streamsets.pipeline.api.impl.ErrorMessage;
import com.streamsets.pipeline.config.PipelineConfiguration;
import com.streamsets.pipeline.main.RuntimeInfo;
import com.streamsets.pipeline.metrics.MetricsConfigurator;
import com.streamsets.pipeline.runner.Pipeline;
import com.streamsets.pipeline.runner.PipelineRuntimeException;
import com.streamsets.pipeline.runner.production.ProductionSourceOffsetTracker;
import com.streamsets.pipeline.runner.production.ThreadHealthReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class ProductionPipeline {

  private static final Logger LOG = LoggerFactory.getLogger(ProductionPipeline.class);

  private final RuntimeInfo runtimeInfo;
  private final PipelineConfiguration pipelineConf;
  private final Pipeline pipeline;
  private final ProductionPipelineRunner pipelineRunner;
  private StateListener stateListener;

  public ProductionPipeline(RuntimeInfo runtimeInfo, PipelineConfiguration pipelineConf, Pipeline pipeline) {
    this.runtimeInfo = runtimeInfo;
    this.pipelineConf = pipelineConf;
    this.pipeline = pipeline;
    this.pipelineRunner =  (ProductionPipelineRunner)pipeline.getRunner();
  }

  public StateListener getStatusListener() {
    return this.stateListener;
  }

  public void registerStatusListener(StateListener stateListener) {
    this.stateListener = stateListener;
  }

  private void stateChanged(PipelineStatus pipelineStatus, String message, Map<String, Object> attributes) throws PipelineRuntimeException {
    if (stateListener!=null) {
      stateListener.stateChanged(pipelineStatus, message, attributes);
    }

  }

  public void run() throws StageException, PipelineRuntimeException {
    MetricsConfigurator.registerJmxMetrics(runtimeInfo.getMetrics());
    try {
      try {
        pipeline.init();
      } catch (Exception e) {
        if (!wasStopped()) {
          LOG.error("Pipeline failed to start: {} ", e.getMessage(), e);
          stateChanged(PipelineStatus.START_ERROR, null, null);
        }
        pipeline.destroy();
        throw e;
      }
      boolean finishing = false;
      boolean running_error = true;
      try {
        stateChanged(PipelineStatus.RUNNING, null, null);
        pipeline.run();
        if (!wasStopped()) {
          stateChanged(PipelineStatus.FINISHING, null, null);
          finishing = true;
        }
      } catch (Exception e) {
        if (!wasStopped()) {
          LOG.error("Pipeline failed while running: {} ", e.getMessage(), e);
          stateChanged(PipelineStatus.RUNNING_ERROR, null, null);
          running_error = true;
        }
        throw e;
      } finally {
        pipeline.destroy();
        if (finishing) {
          stateChanged(PipelineStatus.FINISHED, null, null);
        } else if (running_error) {
          stateChanged(PipelineStatus.RUN_ERROR, null, null);
        } //neither of above condition happens if pipeline was stopped
      }
    } finally {
      MetricsConfigurator.cleanUpJmxMetrics();
    }
  }

  public PipelineConfiguration getPipelineConf() {
    return pipelineConf;
  }

  public Pipeline getPipeline() {
    return this.pipeline;
  }

  public void stop() {
    pipelineRunner.stop();
  }

  public boolean wasStopped() {
    return pipelineRunner.wasStopped();
  }

  public String getCommittedOffset() {
    return pipelineRunner.getCommittedOffset();
  }

  public void captureSnapshot(String snapshotName, int batchSize, int batches) {
    pipelineRunner.capture(snapshotName, batchSize, batches);
  }

  public void setOffset(String offset) {
    ProductionSourceOffsetTracker offsetTracker = (ProductionSourceOffsetTracker) pipelineRunner.getOffSetTracker();
    offsetTracker.setOffset(offset);
    offsetTracker.commitOffset();
  }

  public List<Record> getErrorRecords(String instanceName, int size) {
    return pipelineRunner.getErrorRecords(instanceName, size);
  }

  public List<ErrorMessage> getErrorMessages(String instanceName, int size) {
    return pipelineRunner.getErrorMessages(instanceName, size);
  }

  public long getLastBatchTime() {
    return pipelineRunner.getOffSetTracker().getLastBatchTime();
  }

  public void setThreadHealthReporter(ThreadHealthReporter threadHealthReporter) {
    pipelineRunner.setThreadHealthReporter(threadHealthReporter);
  }
}