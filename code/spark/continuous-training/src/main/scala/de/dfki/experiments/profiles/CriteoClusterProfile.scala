package de.dfki.experiments.profiles

/**
  * @author behrouz
  */
object CriteoClusterProfile extends Profile{
  override val INPUT_PATH = "hdfs://cloud-11:44000/user/behrouz/criteo/experiments/initial-training/day_0"
  override val STREAM_PATH = "hdfs://cloud-11:44000/user/behrouz/criteo/experiments/stream"
  override val EVALUATION_PATH = "prequential"
  override val RESULT_PATH = "/share/hadoop/behrouz/experiments/criteo-full/sampling"
  override val INITIAL_PIPELINE = "/share/hadoop/behrouz/experiments/criteo-full/sampling-mode-prequential/pipelines/init_500"
  override val DELIMITER = "\t"
  override val NUM_FEATURES = 3000
  override val NUM_ITERATIONS = 500
  override val SLACK = 10
  override val DAYS = "1,5"
  override val SAMPLE_SIZE = 1440
  override val DAY_DURATION = 1440
  override val PIPELINE_NAME = "criteo"
  override val REG_PARAM = 0.01
  override val PROFILE_NAME = "criteo-cluster"
}
