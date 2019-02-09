package com.cloudsim.simulations

import org.cloudbus.cloudsim.{Cloudlet, Datacenter}

class Utility {

  def getExecutionTime(cloudlet: Cloudlet): Double ={
    var cost = 0.0
    val resources = cloudlet.getAllResourceId
    resources.foreach(resource => {
      val costPerSec = cloudlet.getCostPerSec(resource)
      cost += costPerSec * cloudlet.getActualCPUTime()
    })

    cost
  }

  /*def getDataCenterCost(datacenter: Datacenter): Double = {
    var cost = 0.0
    //cost for total storage
    cost += datacenter.
    cost
  }*/
}
