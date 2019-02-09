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

import com.clousdim.UtilizationModelNew
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

      // Initialize the CloudSim library
      CloudSim.init(num_user, calendar, trace_flag)


      // Second step: Create Datacenters
      //Datacenters are the resource providers in CloudSim. We need at list one of them to run a CloudSim simulation
      logger.info("Creating Datacenters")
      val datacentercount = config.getInt("def.datacenter_count")
      val dccountrange = 1 to datacentercount
      val datacenterList = new util.LinkedList[Datacenter]
      dccountrange.foreach(i=>{
         val datacenter0 = createDatacenter("Datacenter_"+i)
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
      val vms = config.getInt("def.vms")
      val vmlist = createVM(brokerId, vms)
      val vmlist2 = createVM(broker2Id,vms)

      //Create cloudlets
      logger.info("Creating Cloudlet List for all brokers")
      val cloudlets = config.getInt("def.cloudlets")
      val cloudletList = createCloudlet(brokerId, cloudlets)
      val cloudletList2 = createCloudlet(broker2Id, cloudlets)


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
      printCloudletList(newList)
      val newList2 = broker2.getCloudletReceivedList
      printCloudletList(newList2)

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
    val mips = config.getInt("datacenter.mips")
    val peType = config.getString("datacenter.peType")


    // Create PEs; for a quad-core machine, a list of 4 PEs is required:
    if(peType.equals("quad")){
      val id = 0 to 3
      id.foreach(id => peList1.add(new Pe(id, new PeProvisionerSimple(mips))))
    } else {
      val id = 0 to 1
      id.foreach(id => peList1.add(new Pe(id, new PeProvisionerSimple(mips))))
    }

    //Create hostList
    logger.info("Configuring hosts")
    val hostcount = config.getInt("datacenter.hostCount");
    val hostCountRange = 1 to hostcount //Initialising the range for number of hosts
    hostCountRange.foreach( i => {
      val hostname = "host"+i
      //4. Create Hosts with its id and list of PEs and add them to the list of machines
      val hostId = config.getInt("datacenter."+hostname+".hostID")
      val ram = config.getInt("datacenter."+hostname+".ram")
      //host memory (MB)
      val storage = config.getInt("datacenter."+hostname+".storage")
      //host storage
      val bw = config.getInt("datacenter."+hostname+".bw")


      hostList.add(new Host(hostId, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList1, new VmSchedulerSpaceShared(peList1)))
    })


    // Creating a Datacenter Characteristics object that stores the properties of a data center
    logger.info("Initialising Datacenter characteristics")
    val arch = config.getString("datacenter.characteristics.arch")
    // system architecture
    val os = config.getString("datacenter.characteristics.os")
    // operating system
    val vmm = config.getString("datacenter.characteristics.vmm")
    val time_zone = config.getDouble("datacenter.characteristics.time_zone")
    // time zone this resource located
    val cost = config.getDouble("datacenter.characteristics.cost")
    // the cost of using processing in this resource
    val costPerMem = config.getDouble("datacenter.characteristics.costPerMem")
    // the cost of using memory in this resource
    val costPerStorage = config.getDouble("datacenter.characteristics.costPerStorage")
    // the cost of using storage in this resource
    val costPerBw = config.getDouble("datacenter.characteristics.costPerBw")
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

    //cloudlet parameters
     logger.info("Initializing cloudlet parameters ... ")
     val length = config.getInt("cloudlet.length")
    val fileSize = config.getInt("cloudlet.fileSize")
    val outputSize = config.getInt("cloudlet.outputSize")
    val pesNumber = config.getInt("cloudlet.pesNumber")
    val utilizationModel = new UtilizationModelNew()


     val range = 0 until cloudlets
    range.foreach(id => {
      val cloudlet = new Cloudlet(id, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel)
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
  private def printCloudletList(list: util.List[_ <: Cloudlet]): Unit = {
    val size = list.size
    val indent = "    "
    Log.printLine()
    Log.printLine("========== OUTPUT ==========")
    Log.printLine("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID" + indent + indent + "Time" + indent + "Start Time" + indent + "Finish Time")
    val dft = new DecimalFormat("###.##")

    val listIterator = list.iterator()
    while (listIterator.hasNext){
      val cloudlet = listIterator.next()
      cloudlet.getAllResourceId
      Log.print(indent + cloudlet.getCloudletId + indent + indent)
      if (cloudlet.getCloudletStatus == Cloudlet.SUCCESS) {
        Log.print("SUCCESS")
        Log.printLine(indent + indent + cloudlet.getResourceId + indent + indent + indent + cloudlet.getVmId + indent + indent + indent + dft.format(cloudlet.getActualCPUTime) + indent + indent + dft.format(cloudlet.getExecStartTime) + indent + indent + indent + dft.format(cloudlet.getFinishTime))
      }
    }
  }
}
