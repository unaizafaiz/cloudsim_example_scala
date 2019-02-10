package com.cloudsim.simulations

/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */


import java.text.DecimalFormat
import java.util
import java.util.Calendar

import com.typesafe.config.{Config, ConfigFactory}
import org.cloudbus.cloudsim._
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple
import org.slf4j.{Logger, LoggerFactory}



/**
  * Simulation 1
  * with 5 Datacenter, 2 brokers, 10 vms and 10 cloudlets
  */
object Simulation2 {
  val logger: Logger = LoggerFactory.getLogger(getClass)


  val config: Config = ConfigFactory.load("simulation2.conf")


  /**
    * Creates main() to run this example
    */
  def main(args: Array[String]): Unit = {
    logger.info("Starting Simulation2...")
    try {

      //Reading parameters from configuration file in src/main/resources


      // Initialize the CloudSim package.

      logger.info("Initialising CloudSim...")
      val num_user = config.getInt("def.num_user")  // number of grid users
      val calendar = Calendar.getInstance
      val trace_flag = false // mean trace events

      // Initialize the CloudSim librarysbt
      CloudSim.init(num_user, calendar, trace_flag)


      // Second step: Create Datacenters
      //Datacenters are the resource providers in CloudSim. We need at list one of them to run a CloudSim simulation
      logger.info("Creating Datacenters")
      val datacentercount = config.getInt("def.datacenter_count")
      val dccountrange = 1 to datacentercount
      val datacenterList = new util.LinkedList[Datacenter]
      dccountrange.foreach(i=>{
         val datacenter0 = createDatacenter("datacenter"+i)
         datacenterList.add(datacenter0)
      })

      //Create Broker
      logger.info("Creating Brokers")
      val broker = createBroker("Broker1")
      val brokerId = broker.getId

      val broker2 = createBroker("Broker2")
      val broker2Id = broker2.getId

      //Create VMs
      logger.info("Creating VM List for all brokers")
      val broker1_vms = config.getInt("def.broker1.vms")
      val vmlist = createVM(brokerId, broker1_vms)
      val broker2_vms = config.getInt("def.broker2.vms")
      val vmlist2 = createVM(broker2Id,broker2_vms)

      //Create cloudlets
      logger.info("Creating Cloudlet List for all brokers")
      val broker1_cloudlets = config.getInt("def.broker1.cloudlets")
      val cloudletList = createCloudlet(brokerId, broker1_cloudlets)
      val broker2_cloudlets = config.getInt("def.broker2.cloudlets")
      val cloudletList2 = createCloudlet(broker2Id, broker2_cloudlets)


      //send the vms and cloudlets to broker
      logger.info("Submit Vmlist and cloudlet to the Broker "+broker.getId)
      broker.submitVmList(vmlist)
      broker.submitCloudletList(cloudletList)

      logger.info("Submit Vmlist and cloudlet to the Broker "+broker.getId)
      broker2.submitVmList(vmlist2)
      broker2.submitCloudletList(cloudletList2)

      // Start the simulation
      logger.info("Starting cloud simulation")
      CloudSim.startSimulation

      // Print results when simulation is over
      val newList = broker.getCloudletReceivedList
      printCloudletList(newList, brokerId)
      val newList2 = broker2.getCloudletReceivedList
      printCloudletList(newList2, broker2Id)

      //Stop the simulation
      logger.info("Stopping cloud simulation")
      CloudSim.stopSimulation()

      logger.info("Simulation2 finished!")
    } catch {
      case e: Exception =>
        e.printStackTrace()
        logger.debug("The simulation has been terminated due to an unexpected error")
    }
  }

  def createDatacenter(name: String):Datacenter = {
    logger.info("Inside createDatacenter() .... ")
    // create a list to store Machines
    val hostList = new util.ArrayList[Host]


    // Creating PEs
    logger.info("Creating PE list as specified")
    val peList1 = new util.ArrayList[Pe]
    val mips = config.getInt(name+".mips")
    val peType = config.getString(name+".peType")


    // Create PEs; for a quad-core machine, a list of 4 PEs is required:
    if(peType.equals("quad")){
      logger.info("creating PEs")
      val id = 0 to 3
      id.foreach(id => peList1.add(new Pe(id, new PeProvisionerSimple(mips))))
    } else {
      logger.info("creating PEs")
      val id = 0 to 1
      id.foreach(id => peList1.add(new Pe(id, new PeProvisionerSimple(mips))))
    }

    //Create hostList
    logger.info("Configuring hosts")
    val hostcount = config.getInt(name+".hostCount");
    val hostCountRange = 1 to hostcount //Initialising the range for number of hosts
    hostCountRange.foreach( i => {
      val hostname = "host"+i
      //4. Create Hosts with its id and list of PEs and add them to the list of machines
      val hostId = config.getInt(name+"."+hostname+".hostID")
      val ram = config.getInt(name+"."+hostname+".ram")
      //host memory (MB)
      val storage = config.getInt(name+"."+hostname+".storage")
      //host storage
      val bw = config.getInt(name+"."+hostname+".bw")


      hostList.add(new Host(hostId, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList1, new VmSchedulerTimeShared(peList1)))
    })


    // Creating a Datacenter Characteristics object that stores the properties of a data center
    logger.info("Initialising Datacenter characteristics")
    val arch = config.getString(name+".characteristics.arch")
    // system architecture
    val os = config.getString(name+".characteristics.os")
    // operating system
    val vmm = config.getString(name+".characteristics.vmm")
    val time_zone = config.getDouble(name+".characteristics.time_zone")
    // time zone this resource located
    val cost = config.getDouble(name+".characteristics.cost")
    // the cost of using processing in this resource
    val costPerMem = config.getDouble(name+".characteristics.costPerMem")
    // the cost of using memory in this resource
    val costPerStorage = config.getDouble(name+".characteristics.costPerStorage")
    // the cost of using storage in this resource
    val costPerBw = config.getDouble(name+".characteristics.costPerBw")
    // the cost of using bw in this resource
    val storageList = new util.LinkedList[Storage]

    val characteristics = new DatacenterCharacteristics(arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw)

    // Creating a PowerDatacenter object.
    try {
      logger.info("Creating datacenter object")
      val datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0)
      logger.info("Datacenter created with ID = "+datacenter.getId)
      datacenter
    }catch {
      case e: Exception =>
        e.printStackTrace()
        logger.debug("Data center creation termined due to error")
        return null
    }
  }

  /**
    * Create Datacenter Broker objects
    * @param name name of the datacenter broker
    * @return
    */
  def createBroker(name: String): DatacenterBroker = {
    try {
      //Create Broker object
      logger.info("Creating datacenter broker...")
      val broker = new DatacenterBroker(name)
      logger.info("Datacenter broker "+broker.getName+" created with id = "+broker.getId)
      return broker
    } catch {
      case e: Exception =>
        logger.debug(e.printStackTrace()+"")
        logger.debug("Data center creation termined due to error")
        return null
    }
  }

  /**
    * Create vms
    * @param userId
    * @param vms: number of vms to be created
    * @return vm list
    */
   def createVM(userId: Int, vms: Int) = { //Creates a container to store VMs. This list is passed to the broker later
   logger.info("Inside createVM() method ... ")
    val list = new util.LinkedList[Vm]
     logger.info("Initializing VM parameters")
    //VM Parameters
    val size = config.getInt("vm.size")
    //image size (MB)
    val ram = config.getInt("vm.ram")
    //vm memory (MB)
    val mips = config.getInt("vm.mips")
    val bw = config.getInt("vm.bw")
    val pesNumber = config.getInt("vm.pesNumber")
    //number of cpus
    val vmm = config.getString("vm.vmm")
    //VMM name
    //create VMs
    logger.info("Create VMS .... ")
    val range = 0 until vms
    for( i <- range){
      val vm  = new Vm(i, userId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerSpaceShared)
      logger.info("VM created with id = "+vm.getId+" for broker ID "+vm.getUserId)
      list.add(vm)
    }
    list
  }

  /**
    * Create cloudlets
    * @param userId
    * @param cloudlets  number of cloudlets to be created
    * @return Cloudlet list
    */

   def createCloudlet(userId: Int, cloudlets: Int) = {
     logger.info("Inside createCloudlet() method ... ")

     // Creates a container to store Cloudlets
    val list = new util.LinkedList[Cloudlet]

     //Creating a random variable to build cloudlets of variable length
     val rand = scala.util.Random

    //cloudlet parameters
     logger.info("Initializing cloudlet parameters ... ")
     val length = config.getInt("cloudlet.length")
    val fileSize = config.getInt("cloudlet.fileSize")
    val outputSize = config.getInt("cloudlet.outputSize")
    val pesNumber = config.getInt("cloudlet.pesNumber")
    val utilizationModel = new UtilizationModelFull()


     val range = 0 until cloudlets
    range.foreach(id => {
      val cloudlet = new Cloudlet(id, length+rand.nextInt(100), pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel)
      // setting the owner of these Cloudlets
      cloudlet.setUserId(userId)
      logger.info("Cloudlet "+cloudlet.getCloudletId+" created for broker "+cloudlet.getUserId)
      list.add(cloudlet)
    })
    list
  }

  /**
    * Prints the Cloudlet objects
    *
    * @param list list of Cloudlets
    */
  private def printCloudletList(list: util.List[_ <: Cloudlet], brokerId:Int): Unit = {
    val size = list.size
    val indent = "    "
    Log.printLine()
    Log.printLine("========== OUTPUT ==========")
    Log.printLine("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID" + indent + indent + "Time" + indent + "Start Time" + indent + "Finish Time"+indent+indent+"Total cost")
    val dft = new DecimalFormat("###.##")

    val utility = new Utility
    val listIterator = list.iterator()
    val listCloudletCost = new util.ArrayList[Double]()
    while (listIterator.hasNext){
      val cloudlet = listIterator.next()
      cloudlet.getAllResourceId
      Log.print(indent + cloudlet.getCloudletId + indent + indent)
      if (cloudlet.getCloudletStatus == Cloudlet.SUCCESS) {
        val totalCost = utility.getExecutionTime(cloudlet)
        Log.print("SUCCESS")
        Log.printLine(indent + indent + cloudlet.getResourceId + indent + indent + indent + cloudlet.getVmId + indent + indent + indent + dft.format(cloudlet.getActualCPUTime) + indent + indent + dft.format(cloudlet.getExecStartTime) + indent + indent + indent + dft.format(cloudlet.getFinishTime)+indent + indent + indent + dft.format(totalCost))
        listCloudletCost.add(totalCost)
      }
    }
    println("Broker"+brokerId+" expenditure = "+utility.getTotalApplicationsCost(listCloudletCost))

  }
}
