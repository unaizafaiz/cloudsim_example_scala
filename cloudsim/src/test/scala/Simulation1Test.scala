import java.util.Calendar

import com.cloudsim.simulations.Simulation1
import com.cloudsim.simulations.Simulation1.config
import org.cloudbus.cloudsim.DatacenterBroker
import org.cloudbus.cloudsim.core.CloudSim
import org.scalatest.junit.AssertionsForJUnit
import org.junit.{Before, Test}
import org.slf4j.{Logger, LoggerFactory}

class Simulation1Test extends AssertionsForJUnit  {

    val logger: Logger = LoggerFactory.getLogger(classOf[Simulation1Test])
    var broker: DatacenterBroker = _ //Using a mutable variable so that the same var can be used throughout the test

    @Before def intialise() {
        val num_user = 1  // number of grid users
        val calendar = Calendar.getInstance
        val trace_flag = false // mean trace events

        // Initialize the CloudSim library
        CloudSim.init(num_user, calendar, trace_flag)

        broker = Simulation1.createBroker("Broker0")
    }

    @Test def verifyCreateBroker(): Unit ={
        logger.info("Testing Create Broker")
        val expectedClassObject = "org.cloudbus.cloudsim.DatacenterBroker"
        val actualClassObject = broker.getClass+""
        assert(actualClassObject.contains(expectedClassObject))
    }

    @Test def verifyCreateVMReturnsIsEmpty(): Unit = {
        logger.info("Testing VM Creation returns empty list")
        val actualVMList = Simulation1.createVM(broker.getId,0)
        assert(actualVMList.isEmpty)
    }

    @Test def verifyCreateVMReturnsListNotEmpty(): Unit = {
        logger.info("Testing VM Creation returns list of specified size")
        val actualVMList = Simulation1.createVM(broker.getId,20)
        assert(actualVMList.size() == 20)
    }

    @Test def verifyCreateCloudletReturnsEmpty(): Unit = {
        logger.info("Testing createCloudlet returns an empty list")
        val actualCloudletlist = Simulation1.createCloudlet(broker.getId,0)
        assert(actualCloudletlist.isEmpty)
    }

    @Test def verifyCreateCloudletReturnsList(): Unit = {
        logger.info("Testing createCloudlet returns cloudlet list of specified size")
        val actualCloudletlist = Simulation1.createCloudlet(broker.getId,10)
        assert(actualCloudletlist.size == 10)
    }

}
