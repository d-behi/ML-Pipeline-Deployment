package de.dfki.core.sampling

/**
  * Simple random sampling from the entire history
  *
  * @author behrouz
  */
class SimpleRandomSampler(rate: Double = 0.1) extends Sampler(rate = rate) {

  override def sampleIndices(indices: List[Int]) = {
    indices.filter(_ => rand.nextDouble < rate)
  }

  override def name = "entire_history"
}
