# CS 441 - Spring 2019 - Homework 1

#####By Unaiza Faiz (ufaiz2@uic.edu)

The program deals with Cloud center simulation using Cloudsim platform. Cloud computing datacenters are created and jobs are executed on them. We define the number of brokers in a Simulation, Virtual Machines, Cloudlets (Application) for each simulation. Datacenter Broker policy is defined using the out-of-the-box methods in Cloudsim. 

Two Simulations have been created which model FaaS(Function as a Service) behaviour. We model an on demand behavior, where the users pay as they go. Our cloudlet image size reflects this modelling with size of 10000 to 40000 instructions per second and small file size. For example, getting an image from the cloud to view on a website.

Each simulation also displays the total application (cloudlet) cost and the cost of all the cloudlets run by that broker. This is calculated in the following way:
    - Since cloudlets can run on multiple resource we get the costPerSec for each resource
    - Find the cost for each resource * actualCPUTime
    - Then find the sum total across all resources 

The overall cost endured by the broker to execute all cloudlets is calculated as a sum total of each cloudlet cost. 

#### Simulation1 

The first cloud model is build with the following structure:

* Brokers = 1
* Datacenters = 3 with following characteristics for each data center
     - 2 hosts of 8GB RAM, 100GB storage, 1 mbps bandwidth
     - Each host is a quad core
     - Each host use VM Space Shared Policy 
* Virtual Machines = 24 each with following characteristics
     - 2 GB RAM
     - 2 CPUs with 1000 mips speed
     - 1000 kbps bandwidth
     - 10GB storage
* Cloudlets = 20 with following properties
    - 40000 instructions per second
    - 300 KB filesize
    - 300 KB output file size
    - 1 CPU
    - Each cloudlet uses Utilization model full

We demonstrate space sharing of VMs by having 2 CPUs in each VM and the cloudlet requiring only 1 CPU to complete its execution. The output displays how each cloudlet is shared by 2 VMs (2 different cloudlets share the same VM id) since we use VM Space shared policy in our host.

#### Simulation2

The second simulation is build on the following architecture: 

* Brokers = 2 each running 20 VM, 20 cloudlets and 40 VM, 20 cloudlet
* Datacenters = 4 with following characteristics:
     - 2 Datacenters with following specs:
        - quad core
        - 4 hosts of 16GB RAM, 100GB storage, 1 mbps bandwidth
        - Each host use VM Time Shared Policy 
     - 2 Datacenters with following specs:
             - dual core
             - 4 hosts of 8GB RAM, 100GB storage, 1 mbps bandwidth
             - Each host use VM Time Shared Policy 
* Virtual Machines each with following characteristics
     - 2 GB RAM
     - 2 CPUs with 1000 mips speed
     - 1000 kbps bandwidth
     - 10GB storage
* Cloudlets = 20 with following properties
    - 10000 instructions per second -  this is varied in the createVm() method by using scala.util.Random class to simulate real world scenario of applications of varied length running
    - 300 KB filesize
    - 300 KB output file size
    - 1 CPU
    - Each cloudlet uses Utilization model full

We observe that the broker1 utilizes most of the datacenter resources to create VMs as a result datacenter2 is able to create only 3 VM. But the cloudlets continue execution on these VMs with ease, though using the same execution time per second. Each cloudlet is assigned vm based on time sharing policy. So once the cloudlet finishes execution another cloudlet application uses this resource.

## Steps to Run  

1. Clone the project to your local system using git clone git@bitbucket.org:unaizafaiz/unaiza_faiz_hw1.git
2. On IntelliJ, open the project by selecting the folder ../unaiza_faiz_hw1/cloudsim
3. Open sbt shell -
    ````
    sbt> compile
    sbt> run
   
4. The console will prompt to select the simulation class
5. Enter '1' to run Simulation1 
5. Repeat step 3 to 5 and at the prompt enter '2' to run Simulation 2

The output of the simulation contains all the logging statement demonstration the cloud sim setup. And finally prints the output showing the cloudlet ID, status of cloudlet execution, datacenter on which the cloudlet was run, VM Id of the execution, Actual CPU Time taken for executing the cloudlet, start and end time and the total cost of the cloudlet execution.
### Prerequisites

1. IntelliJ 
2. Java 8

## Running the tests

1. To run the test case on the sbt console type "test"
2. OR in IntelliJ navigate to src/test/scala to run the testcase

Each simulation object has a corresponding test file that tests for createBroker(), createDatacenter, createVM() and createCloudlets()



