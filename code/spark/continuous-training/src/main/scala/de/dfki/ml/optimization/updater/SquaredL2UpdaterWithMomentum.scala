package de.dfki.ml.optimization.updater

import breeze.linalg.{DenseVector => BDV, Vector => BV, axpy => brzAxpy, norm => brzNorm}
import de.dfki.ml.LinearAlgebra.{asBreeze, fromBreeze}
import org.apache.spark.mllib.linalg.Vector

/**
  * Implements the learning adaptation with momentum
  *
  * @see http://sebastianruder.com/optimizing-gradient-descent/index.html#momentum
  *      for more detailed information
  * @param gamma fraction of previous update vector
  */
class SquaredL2UpdaterWithMomentum(var gamma: Double = 0.9) extends Updater {

  var updateVector: BV[Double] = _

  override var iterCounter = 1

  def withUpdateVector(vector: BV[Double]): this.type = {
    updateVector = vector
    this
  }

  override def compute(weightsOld: Vector,
                       gradient: Vector,
                       stepSize: Double,
                       iter: Int) = {

    var brzWeights = asBreeze(weightsOld)
    val brzGradient = asBreeze(gradient)
    val thisIterStepSize = stepSize / math.sqrt(iterCounter)
    val size = brzGradient.size
    if (updateVector == null) {
      updateVector = BDV.zeros[Double](weightsOld.size)
    }
    // adjust the size of the accumulators
    // used when the size of the model is dynamically changing
    updateVector = adjustSize(updateVector, size)
    // break momentum update vector formula: v = v * gamma + learningRate * gradient
    // into to parts
    // part1 : v = v * gamma
    updateVector = updateVector * gamma + thisIterStepSize * brzGradient
    // part 2: v = v + learningRate * gradient
    //brzAxpy(thisIterStepSize, asBreeze(gradient), updateVector)

    logger.info(s"current step-size ($thisIterStepSize)")

    // w' = w - v
    brzAxpy(-1.0, updateVector, brzWeights)

    iterCounter = iterCounter + 1

    fromBreeze(brzWeights)
  }

  override def name = "momentum"

  override def toString = {
    s"${this.getClass.getCanonicalName}, updateVector($updateVector), gamma($gamma)"
  }
}
