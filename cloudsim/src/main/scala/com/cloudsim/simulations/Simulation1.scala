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
import com.typesafe.scalalogging.LazyLogging
import org.cloudbus.cloudsim._
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple
import org.slf4j.{Logger, LoggerFactory}



/**
  * Simulation 1
  * with xx Datacenter, xx brokers, xx vms and xx policy
  */
object Simulation1 extends LazyLogging{

  val config: Config = ConfigFactory.load("my_app.conf")


  /**
    * Creates main() to run this example
    */
  def main(args: Array[String]): Unit = {
    logger.info("Starting CloudSimExample6...")
    try {

      //Reading parameters from configuration file in src/main/resources


      // First step: Initialize the CloudSim package.


      val num_user = config.getInt("def.num_user")  // number of grid users
      val calendar = Calendar.getInstance
      val trace_flag = false // mean trace events

      // Initialize the CloudSim library
      CloudSim.init(num_user, calendar, trace_flag)


      // Second step: Create Datacenters
      //Datacenters are the resource providers in CloudSim. We need at list one of them to run a CloudSim simulation
      //@SuppressWarnings(Array("unused")) val datacenter0 = createDatacenter("Datacenter_0")
      @SuppressWarnings(Array("unused")) val datacenter1 = createDatacenter("Datacenter_1")


      //Third step: Create Broker
      val broker = createBroker
      val brokerId = broker.getId

      //Fourth step: Create VMs
      val vms = config.getInt("def.vms")
      val vmlist = createVM(brokerId, vms) //creating 20 vms

      //Fifth step: Create cloudlets
      val cloudlets = config.getInt("def.cloudlets")
      val cloudletList = createCloudlet(brokerId, cloudlets) // creating 40 cloudlets

      //send the vms and cloudlets to broker
      broker.submitVmList(vmlist)
      broker.submitCloudletList(cloudletList)

      // Sixth step: Starts the simulation
      CloudSim.startSimulation

      // Final step: Print results when simulation is over
      val newList = broker.getCloudletReceivedList
      printCloudletList(newList)

      //Stop the simulation
      CloudSim.stopSimulation()

      logger.info("CloudSimExample6 finished!")
    } catch {
      case e: Exception =>
        e.printStackTrace()
        Log.printLine("The simulation has been terminated due to an unexpected error")
    }
  }

  private def createDatacenter(name: String) = { // Here are the steps needed to create a PowerDatacenter:
    // 1. We need to create a list to store one or more Machines
    val hostList = new util.ArrayList[Host]


    // 2. A Machine contains one or more PEs or CPUs/Cores. Therefore, should
    //    create a list to store these PEs before creating
    //    a Machine.
    val peList1 = new util.ArrayList[Pe]
    val mips = config.getInt("datacenter.mips")

    // 3. Create PEs and add these into the list.
    //for a quad-core machine, a list of 4 PEs is required:
    peList1.add(new Pe(0, new PeProvisionerSimple(mips))) // need to store Pe id and MIPS Rating
    peList1.add(new Pe(1, new PeProvisionerSimple(mips)))
    peList1.add(new Pe(2, new PeProvisionerSimple(mips)))
    peList1.add(new Pe(3, new PeProvisionerSimple(mips)))

    //Another list, for a dual-core machine
    val peList2 = new util.ArrayList[Pe]
    peList2.add(new Pe(0, new PeProvisionerSimple(mips)))
    peList2.add(new Pe(1, new PeProvisionerSimple(mips)))


    //4. Create Hosts with its id and list of PEs and add them to the list of machines
    var hostId = config.getInt("datacenter.host1.hostID")
    val ram = config.getInt("datacenter.host1.ram")
    //host memory (MB)
    val storage = config.getInt("datacenter.host1.storage")
    //host storage
    val bw = config.getInt("datacenter.host1.bw")
    hostList.add(new Host(hostId, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList1, new VmSchedulerTimeShared(peList1))) // This is our first machine


    //4. Create Hosts with its id and list of PEs and add them to the list of machines
    var host2Id = config.getInt("datacenter.host2.hostID")
    val host2ram = config.getInt("datacenter.host2.ram") //host memory (MB)
    val host2storage = config.getInt("datacenter.host2.storage") //host storage
    val host2bw = config.getInt("datacenter.host2.bw")
    hostList.add(new Host(host2Id, new RamProvisionerSimple(host2ram), new BwProvisionerSimple(host2bw), host2storage, peList2, new VmSchedulerTimeShared(peList2))) // Second machine

    //To create a host with a space-shared allocation policy for PEs to VMs:
    //hostList.add(
    //		new Host(
    //			hostId,
    //			new CpuProvisionerSimple(peList1),
    //			new RamProvisionerSimple(ram),
    //			new BwProvisionerSimple(bw),
    //			storage,
    //			new VmSchedulerSpaceShared(peList1)
    //		)
    //	);
    //To create a host with a oportunistic space-shared allocation policy for PEs to VMs:
    //hostList.add(
    //		new Host(
    //			hostId,
    //			new CpuProvisionerSimple(peList1),
    //			new RamProvisionerSimple(ram),
    //			new BwProvisionerSimple(bw),
    //			storage,
    //			new VmSchedulerOportunisticSpaceShared(peList1)
    //		)
    //	);


    // 5. Create a DatacenterCharacteristics object that stores the
    //    properties of a data center: architecture, OS, list of
    //    Machines, allocation policy: time- or space-shared, time zone
    //    and its price (G$/Pe time unit).
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
    //we are not adding SAN devices by now
    val characteristics = new DatacenterCharacteristics(arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw)

    // 6. Finally, we need to create a PowerDatacenter object.

    try {
      val datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0)
      datacenter
    }catch {
      case e: Exception =>
        e.printStackTrace()
    }
  }

  //We strongly encourage users to develop their own broker policies, to submit vms and cloudlets according
  //to the specific rules of the simulated scenario
  private def createBroker: DatacenterBroker = {
    try {
      val broker = new DatacenterBroker("Broker")
      return broker
    } catch {
      case e: Exception =>
        e.printStackTrace()
        return null
    }
  }

  /**
    * Create vms
    * @param userId
    * @param vms: number of vms to be created
    * @return vm list
    */
  private def createVM(userId: Int, vms: Int) = { //Creates a container to store VMs. This list is passed to the broker later
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
    val vm = new Array[Vm](vms)
    var i = 0
    while ( {
      i < vms
    }) {
      vm(i) = new Vm(i, userId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerSpaceShared)

      //for creating a VM with a space shared scheduling policy for cloudlets:
      //vm[i] = Vm(i, userId, mips, pesNumber, ram, bw, size, priority, vmm, new CloudletSchedulerSpaceShared());

      list.add(vm(i))

      {
        i += 1; i - 1
      }
    }
    list
  }

  /**
    * Create cloudlets
    * @param userId
    * @param cloudlets  number of cloudlets to be created
    * @return Cloudlet list
    */

  private def createCloudlet(userId: Int, cloudlets: Int) = { // Creates a container to store Cloudlets
    val list = new util.LinkedList[Cloudlet]

    //cloudlet parameters
    val length = config.getInt("cloudlet.length")
    val fileSize = config.getInt("cloudlet.fileSize")
    val outputSize = config.getInt("cloudlet.outputSize")
    val pesNumber = config.getInt("cloudlet.pesNumber")
    val utilizationModel = new UtilizationModelFull()
    val cloudlet = new Array[Cloudlet](cloudlets)
    var i = 0
    while ( {
      i < cloudlets
    }) {
      cloudlet(i) = new Cloudlet(i, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel)
      // setting the owner of these Cloudlets
      cloudlet(i).setUserId(userId)
      list.add(cloudlet(i))

      {
        i += 1; i - 1
      }
    }
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
    var i = 0
    while ( {
      i < size
    }) {
      val cloudlet = list.get(i)
      Log.print(indent + cloudlet.getCloudletId + indent + indent)
      if (cloudlet.getCloudletStatus == Cloudlet.SUCCESS) {
        Log.print("SUCCESS")
        Log.printLine(indent + indent + cloudlet.getResourceId + indent + indent + indent + cloudlet.getVmId + indent + indent + indent + dft.format(cloudlet.getActualCPUTime) + indent + indent + dft.format(cloudlet.getExecStartTime) + indent + indent + indent + dft.format(cloudlet.getFinishTime))
      }

      {
        i += 1; i - 1
      }
    }
  }
}
