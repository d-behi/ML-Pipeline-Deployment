package de.dfki.experiments

import java.nio.file.{Files, Paths}

import de.dfki.core.sampling._
import de.dfki.deployment.ContinuousDeploymentQualityAnalysis
import de.dfki.experiments.profiles.Profile
import de.dfki.ml.optimization.updater.SquaredL2UpdaterWithAdam
import de.dfki.ml.pipelines.Pipeline
import de.dfki.ml.pipelines.criteo.CriteoPipeline
import de.dfki.ml.pipelines.urlrep.URLRepPipeline
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @author behrouz
  */
object SamplingModes extends Experiment {

  object DefaultProfile extends Profile {
    val INPUT_PATH = "data/url-reputation/processed/initial-training/day_0"
    val STREAM_PATH = "data/url-reputation/processed/stream"
    val EVALUATION_PATH = "prequential"
    val RESULT_PATH = "../../../experiment-results/url-reputation/sampling"
    val INITIAL_PIPELINE = "data/url-reputation/pipelines/sampling-mode/pipeline-3000"
    val DELIMITER = ","
    // URL FEATURE SIZE
    // val NUM_FEATURES = 3231961
    val NUM_FEATURES = 3000
    val NUM_ITERATIONS = 2000
    val SLACK = 5
    // 44 no error all 4400 rows are ok
    // 45 error but 3900 rows are only ok
    val DAYS = "1,120"
    val SAMPLING_RATE = 0.1
    val DAY_DURATION = 100
    val PIPELINE_NAME = "url-rep"
    val REG_PARAM = 0.001
    override val SAMPLE_SIZE = 100
    override val PROFILE_NAME = "default"
  }

  def main(args: Array[String]): Unit = {
    val params = getParams(args, DefaultProfile)

    val conf = new SparkConf().setAppName("Sampling Mode Experiment")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)

    val ssc = new StreamingContext(conf, Seconds(1))
    val data = ssc.sparkContext.textFile(params.inputPath)

    // continuously trained with a uniform sample of the historical data
    val uniformPipeline = getPipeline(ssc.sparkContext,
      params.delimiter,
      params.numFeatures,
      params.numIterations,
      params.regParam,
      data,
      params.pipelineName,
      params.initialPipeline)

    new ContinuousDeploymentQualityAnalysis(history = params.inputPath,
      streamBase = params.streamPath,
      evaluation = s"${params.evaluationPath}",
      resultPath = s"${params.resultPath}/continuous",
      daysToProcess = params.days,
      slack = params.slack,
      sampler = new UniformSampler(size = params.dayDuration)).deploy(ssc, uniformPipeline)

    // continuously trained with a window based sample of the historical data
    val windowBased = getPipeline(ssc.sparkContext,
      params.delimiter,
      params.numFeatures,
      params.numIterations,
      params.regParam,
      data,
      params.pipelineName,
      params.initialPipeline)

    new ContinuousDeploymentQualityAnalysis(history = params.inputPath,
      streamBase = params.streamPath,
      evaluation = s"${params.evaluationPath}",
      resultPath = s"${params.resultPath}/continuous",
      daysToProcess = params.days,
      slack = params.slack,
      sampler = new WindowBasedSampler(size = params.dayDuration, window = params.dayDuration * 10)).deploy(ssc, windowBased)

    // continuously trained with a time based sample of the historical data
    val timeBasedFix = getPipeline(ssc.sparkContext,
      params.delimiter,
      params.numFeatures,
      params.numIterations,
      params.regParam,
      data,
      params.pipelineName,
      params.initialPipeline)

    new ContinuousDeploymentQualityAnalysis(history = params.inputPath,
      streamBase = params.streamPath,
      evaluation = s"${params.evaluationPath}",
      resultPath = s"${params.resultPath}/continuous",
      daysToProcess = params.days,
      slack = params.slack,
      sampler = new TimeBasedSampler(size = params.dayDuration)).deploy(ssc, timeBasedFix)
  }
}
