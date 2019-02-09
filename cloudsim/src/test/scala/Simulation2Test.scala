import java.util.Calendar

import com.cloudsim.simulations.{Simulation1, Simulation2}
import org.cloudbus.cloudsim.core.CloudSim
import org.scalatest.junit.AssertionsForJUnit
import org.junit.{Before, Test}
import org.slf4j.{Logger, LoggerFactory}

class Simulation2Test extends AssertionsForJUnit  {

  val logger: Logger = LoggerFactory.getLogger(classOf[Simulation1Test])

  @Before def intialise() {
    val num_user = 1  // number of grid users
    val calendar = Calendar.getInstance
    val trace_flag = false // mean trace events

    // Initialize the CloudSim library
    CloudSim.init(num_user, calendar, trace_flag)
  }

  @Test def verifyCreateBroker(): Unit ={
    logger.info("Testing createBroker()")
    val broker = Simulation2.createBroker("Broker0")
    val expectedClassObject = "org.cloudbus.cloudsim.DatacenterBroker"
    val actualClassObject = broker.getClass+""
    //Asserting if the object returned is a Broker object and name of the object name is as specified
    assert(actualClassObject.contains(expectedClassObject) && broker.getName == "Broker0")
  }

  @Test def createDataCenter(): Unit = {
    logger.info("Testing createDataCenter()")
    val datacenter = Simulation2.createBroker("Datacenter_0")
    val expectedClassObject = "org.cloudbus.cloudsim.Datacenter"
    val actualClassObject = datacenter.getClass+""
    //Asserting if the object returned is a Datacenter object and name of the object is as specified
    assert(actualClassObject.contains(expectedClassObject) && datacenter.getName == "Datacenter_0")
  }

  @Test def verifyCreateVMReturnsIsEmpty(): Unit = {
    val broker = Simulation2.createBroker("Broker0")
    logger.info("Testing VM Creation returns empty list")
    val actualVMList = Simulation1.createVM(broker.getId,0)
    assert(actualVMList.isEmpty)
  }

  @Test def verifyCreateVMReturnsListNotEmpty(): Unit = {
    val broker = Simulation2.createBroker("Broker0")
    logger.info("Testing VM Creation returns list of specified size")
    val actualVMList = Simulation1.createVM(broker.getId,20)
    assert(actualVMList.size() == 20)
  }

  @Test def verifyCreateCloudletReturnsEmpty(): Unit = {
    val broker = Simulation2.createBroker("Broker0")
    logger.info("Testing createCloudlet returns an empty list")
    val actualCloudletlist = Simulation1.createCloudlet(broker.getId,0)
    assert(actualCloudletlist.isEmpty)
  }

  @Test def verifyCreateCloudletReturnsList(): Unit = {
    val broker = Simulation2.createBroker("Broker0")
    logger.info("Testing createCloudlet returns cloudlet list of specified size")
    val actualCloudletlist = Simulation1.createCloudlet(broker.getId,10)
    assert(actualCloudletlist.size == 10)
  }

}
