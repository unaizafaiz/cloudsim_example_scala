package com.cloudsim.simulations

import java.util
import scala.collection.JavaConversions._
import org.cloudbus.cloudsim.{Cloudlet}

/**
  * Class to define utility functions
  */
class Utility {

  /**
    * Find the total expenditure of broker to execute all classes
    * @param listCloudletCost
    * @return cost
    */
  def getTotalApplicationsCost(listCloudletCost: util.ArrayList[Double]): Double = {
    val cost =listCloudletCost.fold(0.0)(_+_)
    cost
  }

  /**
    * Find the execution cost of a cloudlet
    * @param cloudlet
    * @return cost
    */


  def getExecutionTime(cloudlet: Cloudlet): Double ={
    val listCost = new util.ArrayList[Double]()
    val resources = cloudlet.getAllResourceId
    resources.foreach(resource => {
      val costPerSec = cloudlet.getCostPerSec(resource)
      listCost.add(costPerSec * cloudlet.getActualCPUTime())
    })
    val cost = listCost.fold(0.0)(_+_)
    cost
  }
}
