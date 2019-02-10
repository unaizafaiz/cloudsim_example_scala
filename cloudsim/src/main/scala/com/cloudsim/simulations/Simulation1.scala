package com.cloudsim.simulations

import java.text.DecimalFormat
import java.util
import java.util.Calendar

import com.typesafe.config.{Config, ConfigFactory}
import org.cloudbus.cloudsim._
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.provisioners.{BwProvisionerSimple, PeProvisionerSimple, RamProvisionerSimple}
import org.slf4j.{Logger, LoggerFactory}

/**
  * Simulation 1
  * with 3 Datacenter, 1 broker, 24 vms and 20 cloudlets
  */
object Simulation1 {
  //Reading parameters from configuration file in src/main/resources
  val config: Config = ConfigFactory.load("simulation1.conf")
  //Logger SLf4j object creation
  val logger: Logger = LoggerFactory.getLogger(getClass)


  /**
    * Creates main() to run this example
    */
  def main(args: Array[String]): Unit = {
    logger.info("In main()")
      initialize()
  }


  def initialize(): Unit = {
    logger.info("In initialize() method")
    logger.info("Starting Simulation1...")
    try {

      // Initialize the CloudSim package
      logger.info("Initialising CloudSim...")

      val num_user = config.getInt("def.num_user")  // number of grid users
      val calendar = Calendar.getInstance
      val trace_flag = false // mean trace events

      // Initialize the CloudSim library
      CloudSim.init(num_user, calendar, trace_flag)


      //Create Datacenters
      //Datacenters are the resource providers in CloudSim. We need at list one of them to run a CloudSim simulation
      logger.info("Creating Datacenters")
      @SuppressWarnings(Array("unused")) val datacenter1 = createDatacenter("datacenter1")
      @SuppressWarnings(Array("unused")) val datacenter2 = createDatacenter("datacenter2")
      @SuppressWarnings(Array("unused")) val datacenter3 = createDatacenter("datacenter3")


      //Create Broker
      logger.info("Creating broker")
      val broker = createBroker("Broker1")
      val brokerId = broker.getId

      //Create VMs
      logger.info("Creating VM List for broker "+brokerId)
      val vms = config.getInt("def.vms")
      val vmlist = createVM(brokerId, vms)

      //Create cloudlets
      logger.info("Creating Cloudlet List for broker "+brokerId)
      val cloudlets = config.getInt("def.cloudlets")
      val cloudletList = createCloudlet(brokerId, cloudlets)


      //send the vms and cloudlets to broker
      logger.info("Submitting vmlist and clouslet list to "+brokerId)
      broker.submitVmList(vmlist)
      broker.submitCloudletList(cloudletList)

      // Start the simulation
      logger.info("Start simulation")
      CloudSim.startSimulation

      // Print results when simulation is over
      val newList = broker.getCloudletReceivedList
      printCloudletList(newList, brokerId)
      val utility = new Utility

      //Stop the simulation
      logger.info("Stop simulation")
      CloudSim.stopSimulation()

      logger.info("Simulation1 finished!")
    } catch {
      case e: Exception =>
        e.printStackTrace()
        logger.debug("The simulation has been terminated due to an unexpected error")
    }
  }

   def createDatacenter(name: String) = {

     logger.info("Inside createDatacenter() .... ")

     // create a list to store Machines
    val hostList = new util.ArrayList[Host]


    // create a list to store these PEs before creatin a Machine.
    logger.info("Creating 2 PE list for two quad cores ")
     val peList1 = new util.ArrayList[Pe]
     val mips = config.getInt(name+".mips")

    // 3. Create PEs and add these into the list.
    //for a quad-core machine, a list of 4 PEs is required:
    peList1.add(new Pe(0, new PeProvisionerSimple(mips))) // need to store Pe id and MIPS Rating
    peList1.add(new Pe(1, new PeProvisionerSimple(mips)))
    peList1.add(new Pe(2, new PeProvisionerSimple(mips)))
    peList1.add(new Pe(3, new PeProvisionerSimple(mips)))

    val peList2 = new util.ArrayList[Pe]
    peList2.add(new Pe(0, new PeProvisionerSimple(mips)))
    peList2.add(new Pe(1, new PeProvisionerSimple(mips)))
    peList2.add(new Pe(2, new PeProvisionerSimple(mips)))
    peList2.add(new Pe(3, new PeProvisionerSimple(mips)))


    //4. Create Hosts with its id and list of PEs and add them to the list of machines
    logger.info("Creating hosts")
    val hostId = config.getInt(name+".host1.hostID")
    val ram = config.getInt(name+".host1.ram")
    //host memory (MB)
    val storage = config.getInt(name+".host1.storage")
    //host storage
    val bw = config.getInt(name+".host1.bw")
    hostList.add(new Host(hostId, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList1, new VmSchedulerSpaceShared(peList1))) // This is our first machine

    //4. Create Hosts with its id and list of PEs and add them to the list of machines
    val host2Id = config.getInt(name+".host2.hostID")
    val host2ram = config.getInt(name+".host2.ram") //host memory (MB)
    val host2storage = config.getInt(name+".host2.storage") //host storage
    val host2bw = config.getInt(name+".host2.bw")
    hostList.add(new Host(host2Id, new RamProvisionerSimple(host2ram), new BwProvisionerSimple(host2bw), host2storage, peList2, new VmSchedulerSpaceShared(peList2))) // Second machine

    // Creating a Datacenter Characteristics object that stores the properties of a data center
    logger.info("Configuring Datacenter Characteristics")
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
      val datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0)
      logger.info("Datacenter "+datacenter.getName+" created with id "+datacenter.getId)
      datacenter
    }catch {
      case e: Exception =>
        e.printStackTrace()
        logger.debug("Datacenter creation terminated due to error")
    }
  }

  //We strongly encourage users to develop their own broker policies, to submit vms and cloudlets according
  //to the specific rules of the simulated scenario
  def createBroker(name: String): DatacenterBroker = {
    try {
      val broker = new DatacenterBroker(name)
      logger.info("Datacenter "+broker.getName+" created with id "+broker.getId)
      return broker
    } catch {
      case e: Exception =>
        e.printStackTrace()
        logger.debug("Broker creation terminated due to error")
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
    logger.info("Inside createVM() method...")
    logger.info("Initialising VM Parameters")
    val list = new util.LinkedList[Vm]
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
    val range = 0 until vms
    for( i <- range){
      val vm = new Vm(i, userId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerSpaceShared)
      list.add(vm)
      logger.info("VM "+vm.getId+" created for broker "+vm.getUserId)
    }
    list
  }

  /**
    * Create cloudlets
    * @param userId
    * @param cloudlets  number of cloudlets to be created
    * @return Cloudlet list
    */

   def createCloudlet(userId: Int, cloudlets: Int) = { // Creates a container to store Cloudlets
     logger.info("Inside createCloudlet() method ... ")

     val list = new util.LinkedList[Cloudlet]

    //cloudlet parameters
     logger.info("Initializing cloudlet parameters ... ")

     val length = config.getInt("cloudlet.length")
    val fileSize = config.getInt("cloudlet.fileSize")
    val outputSize = config.getInt("cloudlet.outputSize")
    val pesNumber = config.getInt("cloudlet.pesNumber")
    val utilizationModel = new UtilizationModelFull()
    val cloudlet = new Array[Cloudlet](cloudlets)
    val range = 0 until cloudlets
    range.foreach(id => {

      cloudlet(id) = new Cloudlet(id, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel)
      // setting the owner of these Cloudlets
      cloudlet(id).setUserId(userId)
      logger.info("VM "+cloudlet(id).getCloudletId+" created for broker "+cloudlet(id).getUserId)
      list.add(cloudlet(id))
    })
    list
  }

  /**
    * Prints the Cloudlet objects
    *
    * @param list list of Cloudlets
    */
  private def printCloudletList(list: util.List[_ <: Cloudlet], brokerId: Int): Unit = {
    logger.info("Printing cloudlet output")
    val size = list.size
    val indent = "    "
    Log.printLine()
    Log.printLine("========== OUTPUT ==========")
    Log.printLine("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID" + indent + indent + "Time" + indent + "Start Time" + indent + "Finish Time"+indent+indent+"Total cost")
    val dft = new DecimalFormat("###.##")

    val cloudletCost = new util.ArrayList[Double]()
    val listIterator = list.iterator()
    val listCloudletCost = new util.ArrayList[Double]()
    val utility = new Utility
    while (listIterator.hasNext){
      val cloudlet = listIterator.next()
      cloudlet.getAllResourceId
      Log.print(indent + cloudlet.getCloudletId + indent + indent)
      if (cloudlet.getCloudletStatus == Cloudlet.SUCCESS) {
        val totalCost = utility.getExecutionTime(cloudlet)
        listCloudletCost.add(totalCost)
        Log.print("SUCCESS")
        Log.printLine(indent + indent + cloudlet.getResourceId + indent + indent + indent + cloudlet.getVmId + indent + indent + indent + dft.format(cloudlet.getActualCPUTime) + indent + indent + dft.format(cloudlet.getExecStartTime) + indent + indent + indent + dft.format(cloudlet.getFinishTime)+indent + indent + indent + dft.format(totalCost))
      }
    }
    println("Broker"+brokerId+" expenditure = "+utility.getTotalApplicationsCost(listCloudletCost))

  }
}
