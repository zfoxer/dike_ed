/*
 * Dike ED: Discrete-event simulator for medical emergency departments.
 * Copyright (C) 2021, 2022, 2023 by Constantine Kyriakopoulos
 * zfox@users.sourceforge.net
 * @version 0.4.0
 *
 * @section LICENSE
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package dike_ed;

//import org.json.simple.parser.ParseException;

import dike_ed.plugin.rres.RResFactory;

import java.io.IOException;
import java.util.*;

/**
 *  Dike is the main simulation engine for Emergency Departments (EDs) of hospitals. This is
 *  an experimental release.
 */
public class Dike
{
    /**
     *  Hardcoded version number
     */
    public static final String VERSION = "0.4.0";

    /**
     *  Predefined triage levels
     */
    enum TriageLevel
    {
        ONE, TWO, THREE, FOUR, FIVE
    }

    /**
     *  Number of executions. Results are the average of output values
     */
    public static int EXECUTIONS = 1;

    /**
     *  Mean arrivals per hour. Used by the distribution to produce interarrival patient times
     */
    public static int MEAN_ARRIVALS_PER_HOUR = 20;

    /**
     *  Total simulation time
     */
    public static int SIM_HOURS = 1;

    /**
     *  Beds in the ED
     */
    public static int BEDS = 5;

    /**
     *  Queue of incoming patients to ED
     */
    public static int PATIENT_QUEUE_SIZE = 100;

    /**
     *  Doctor resources
     */
    public static int DOCTORS = 4;

    /**
     *  Nurse resources
     */
    public static int NURSES = 4;

    /**
     *  Wardie resources
     */
    public static int WARDIES = 2;

    /**
     *  Lab resources
     */
    public static int LABS = 2;

    /**
     *  X-ray resources
     */
    public static int X_RAYS_STAFF = 2;

    /**
     *  Index of the active algorithm
     */
    public static int ALGO_INDEX = 0;

    /**
     *  Resource types/groups
     */
    public enum RESOURCE {DOCTOR, NURSE, WARDIE, LAB, X_RAY_STAFF}

    /**
     *  Predefined tasks and their resources and durations
     */
    public enum TASK
    {
        BED_ALLOC(Dike.RESOURCE.NURSE, 10), MEDICAL_ASS(Dike.RESOURCE.DOCTOR, 20),
        VITAL_SIGNS(Dike.RESOURCE.NURSE, 15), TAKE_BLOODS(Dike.RESOURCE.NURSE, 10),
        PATHOLOGY_TEST(Dike.RESOURCE.LAB, 60), REVIEW_DISCHARGE(Dike.RESOURCE.DOCTOR, 10),
        X_RAY(Dike.RESOURCE.X_RAY_STAFF, 30), PLASTERING(Dike.RESOURCE.DOCTOR, 30),
        ANAESTHETIC(Dike.RESOURCE.NURSE, 30), ANAESTHETIC_REC(Dike.RESOURCE.NURSE, 30),
        SUTURES(Dike.RESOURCE.DOCTOR, 30), DISCHARGE(Dike.RESOURCE.NURSE, 20),
        NURSE_TREATMENT(Dike.RESOURCE.NURSE, 20), PATIENT_NOTES(Dike.RESOURCE.DOCTOR, 20),
        ADMIT_IMPATIENT_UNIT(Dike.RESOURCE.DOCTOR, 10), TRANSFER_IMPATIENT_UNIT(Dike.RESOURCE.WARDIE, 30);

        /**
         *  The resource related to this task
         */
        public final Dike.RESOURCE resource;

        /**
         *  Task's duration
         */
        public final int duration;

        /**
         *  Typical constructor for the task
         */
        TASK(Dike.RESOURCE resource, int duration)
        {
            this.resource = resource;
            this.duration = duration;
        }
    }

    /**
     *  Pool containing all the events
     */
    private final EventPool eventPool = new EventPool();

    /**
     *  Queue for incoming patients
     */
    private final Vector<Patient> patientQueue = new Vector<>();

    /**
     *  Rejected patients due to a full queue
     */
    private final Vector<Patient> diedPatients = new Vector<>();

    /**
     *  Patients exiting the ED after fulfilling their treatment tasks
     */
    private final Vector<Patient> dischargedPatients = new Vector<>();

    /**
     *  Bed container for the ED
     */
    private final List<Bed> beds = new LinkedList<>();

    /**
     *  Doctor resource container for the ED
     */
    private final Vector<Resource> doctors = new Vector<>();

    /**
     *  Nurse resource container for the ED
     */
    private final Vector<Resource> nurses = new Vector<>();

    /**
     *  Wardie resource container for the ED
     */
    private final Vector<Resource> wardies = new Vector<>();

    /**
     *  Lab resource container for the ED
     */
    private final Vector<Resource> labs = new Vector<>();

    /**
     *  X-ray resource container for the ED
     */
    private final Vector<Resource> xRaysStaff = new Vector<>();

    /**
     *  Available algorithms in here
     */
    private Vector<dike_ed.plugin_api.Algo> algos = new Vector<>();

    /**
     *  Number of queued patients
     */
    private int queuedPatients = 0;

    /**
     *  Number of patient arrivals
     */
    private int patientArrivals = 0;

    /**
     *  Dike constructor that initialises the simulation, executes the scheduler
     *  and prints the stats
     */
    public Dike()
    {
        try
        {
            init();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }

        eventHandler();
    }

    /**
     *  Main initialisation point from a JSON representation. Configures the simulation parameters
     *  according the input file.
     *
     *  @param stateJson The state in JSON
     *  @throws IOException
     *  //@throws ParseException
     */
    private void init(String stateJson) throws IOException//, ParseException
    {
    }

    /**
     *  Main initialisation point. Calls sub-init processes.
     *
     *  @throws IOException
     *  //@throws ParseException
     */
    private void init() throws IOException//, ParseException
    {
        clear();
        try
        {
            initResourses();
            initDefaultBeds();
            initEventPool(MEAN_ARRIVALS_PER_HOUR, SIM_HOURS);
            initAlgos();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     *  Initialises the event pool. Events can be either patient arrival or availability of a bed.
     *
     *  @param hourArrivals Mean arrivals per hour
     *  @param hours Total simulation time in hours
     */
    private void initEventPool(int hourArrivals, int hours)
    {
        eventPool.clear();
        Vector<Integer> arr = arrivals(hourArrivals, hours);
        for(int min : arr)
            scheduleEvent(min, Event.EventType.PATIENT_ARRIVAL);
    }

    /**
     *  Beds are initialised. Their number is hard-coded.
     */
    private void initDefaultBeds()
    {
        for(int i = 0; i < BEDS; ++i)
            beds.add(new Bed());
        Collections.sort(beds);
    }

    /**
     *  Beds are initialised. Their number is entered as a parameter.
     *
     *  @param bedCount Number of beds
     */
    private void initDefaultBeds(int bedCount)
    {
        for(int i = 0; i < bedCount; ++i)
            this.beds.add(new Bed());
        Collections.sort(beds);
    }

    /**
     *  Initialises resources based on their hard-coded numbers
     */
    private void initResourses()
    {
        for(int i = 0; i < DOCTORS; ++i)
            doctors.add(new Doctor());

        for(int i = 0; i < NURSES; ++i)
            nurses.add(new Nurse());

        for(int i = 0; i < WARDIES; ++i)
            wardies.add(new Wardie());

        for(int i = 0; i < LABS; ++i)
            labs.add(new Lab());

        for(int i = 0; i < X_RAYS_STAFF; ++i)
            xRaysStaff.add(new XRayStaff());
    }

    /**
     *  Initialises resources according to the input numbers
     *
     *  @param docs Number of doctors
     *  @param nurs Number of nurses
     *  @param wards Number of wardies
     *  @param lbs Number of labs
     *  @param xrays Number of x-ray staff
     */
    private void initResourses(int docs, int nurs, int wards, int lbs, int xrays)
    {
        for(int i = 0; i < docs; ++i)
            doctors.add(new Doctor());

        for(int i = 0; i < nurs; ++i)
            nurses.add(new Nurse());

        for(int i = 0; i < wards; ++i)
            wardies.add(new Wardie());

        for(int i = 0; i < lbs; ++i)
            labs.add(new Lab());

        for(int i = 0; i < xrays; ++i)
            xRaysStaff.add(new XRayStaff());
    }

    /**
     *  Initialises the task allocation algorithms
     */
    private void initAlgos()
    {
        algos.add(new RResFactory().createAlgo());

        for(dike_ed.plugin_api.Algo algo : algos)
            algo.setResources(doctors, nurses, wardies, labs, xRaysStaff);
    }

    /**
     *  Returns a patient from the queue. The one with the highest weight, i.e., longest
     *  queue-stay time, multiplied by his triage priority.
     *
     *  @param currentTime Current simulation time
     *  @return Patient The requested patient
     */
    private Patient patientFromQueue(int currentTime)
    {
        if(patientQueue.size() == 0)
            return null;

        Patient retPatient = null;
        int maxWeight = Integer.MIN_VALUE;
        for(Patient patient : patientQueue)
        {
            int weight = (currentTime - patient.getQueuedTimestamp()) * triagePriority();
            if(weight > maxWeight)
            {
                maxWeight = weight;
                retPatient = patient;
            }
        }
        patientQueue.remove(retPatient);

        return retPatient;
    }

    /**
     *  Checks if at least a bed is available
     *
     *  @return boolean Indication of availability
     */
    private boolean bedAvailable()
    {
        for(Bed bed : beds)
            if(bed.available())
                return true;

        return false;
    }

    /**
     *  Returns the first available bed according to its availability time span
     *
     *  @return Bed The requested bed
     */
    private Bed firstAvailableBed()
    {
        //  Requires sorted beds according to longest availability
        Collections.sort(beds);
        for(Bed bed : beds)
            if(bed.available())
                return bed;

        return null;
    }

    /**
     *  Goes through all the event in the pool and handles them
     */
    private void eventHandler()
    {
        while(eventPool.size() > 0)
        {
            Event event = eventPool.popNextEvent();
            if(event == null)
                return;
            int simTime = event.getStartTime();

            switch(event.getType())
            {
                case PATIENT_ARRIVAL ->
                {
                    handlePatientArrival(simTime);
                }
                case BED_AVAILABLE ->
                {
                    handleAvailableBed(simTime, event.getBedId());
                }
                default -> throw new IllegalStateException("Unexpected value: " + event.getType());
            }
        }
    }

    /**
     *  Schedules an event of the given type at given simulation time
     *
     *  @param timeStamp Simulation timestamp to schedule the event
     *  @param type Type of the event
     */
    private void scheduleEvent(int timeStamp, Event.EventType type)
    {
        Event event = new Event(type);
        event.setStartTime(timeStamp);
        eventPool.addEvent(event);
    }

    /**
     *  Schedules a bed event
     *
     *  @param timeStamp Simulation timestamp to schedule the event
     *  @param bedId Bed ID
     */
    private void scheduleBedEvent(int timeStamp, int bedId)
    {
        Event event = new Event(Event.EventType.BED_AVAILABLE);
        event.setBedId(bedId);
        event.setStartTime(timeStamp);
        eventPool.addEvent(event);
    }

    /**
     *  Handles a patient arrival at given timestamp
     *
     *  @param simTime The current simulation time
     */
    private void handlePatientArrival(int simTime)
    {
        Patient patient = new Patient();
        patientArrivals++;

        if(bedAvailable())
        {
            int totalDuration = algos.elementAt(ALGO_INDEX).allocateTasks(patient, simTime);
            Bed bed = firstAvailableBed();
            bed.addPatient(patient, simTime, simTime + totalDuration);
            Collections.sort(beds);

            scheduleBedEvent(simTime + totalDuration, bed.getId());
        }
        else if(patientQueue.size() < PATIENT_QUEUE_SIZE)
        {
            patient.setInQueue(simTime);
            patientQueue.add(patient);
            queuedPatients++;
        }
        else
        {
            // Patient died, RIP
            diedPatients.add(patient);
        }
    }

    /**
     *  Handles the event when a bed becomes available
     *
     *  @param simTime The current simulation time
     *  @param bedId The ID of the available bed
     */
    private void handleAvailableBed(int simTime, int bedId)
    {
        Bed eventBed = null;
        for(Bed tmpBed : beds)
            if(tmpBed.getId() == bedId)
            {
                eventBed = tmpBed;
                break;
            }

        Patient eventPatient = eventBed.removePatient(simTime);
        eventPatient.removeFromBed(simTime);
        dischargedPatients.add(eventPatient);

        Patient patient = patientFromQueue(simTime);
        if(patient == null)
            return;
        int totalDuration = algos.elementAt(ALGO_INDEX).allocateTasks(patient, simTime);
        Bed bed = firstAvailableBed();
        bed.addPatient(patient, simTime, simTime + totalDuration);
        Collections.sort(beds);
        scheduleBedEvent(simTime + totalDuration, bed.getId());
    }

    /**
     *  Produces a triage priority number
     *
     *  @return int The priority number
     */
    private int triagePriority()
    {
        //  Uniform distribution to produce the triage level
        return (int)(TriageLevel.values().length * Math.random()) + 1;
    }

    /**
     *  Donald Knuth's Poisson process
     *
     *  @param mean The mean value of the process
     *  @return int The next produced random value
     */
    private int nextPoissonValue(double mean)
    {
        Random r = new Random();
        double l = Math.exp(-mean);
        int k = 0;
        double p = 1.0;
        do
        {
            p *= r.nextDouble();
            k++;
        }
        while(p > l);

        return k - 1;
    }

    /**
     *  Creates a sequence of patient arrival timestamps using a Poisson distribution
     *
     *  @param meanHourArrivals The mean arrivals per hour
     *  @param hours Simulation hours
     *  @return Vector<Integer> A container with the produced values
     */
    private Vector<Integer> arrivals(int meanHourArrivals, int hours)
    {
        Vector<Integer> retTimes = new Vector<>();    //  Timestamp in minutes
        double avgTimeDistance = 60 / (double)meanHourArrivals;
        int time = 0;

        while(time < hours * 60)
        {
            retTimes.add(time);
            time += nextPoissonValue(avgTimeDistance);
        }

        return retTimes;
    }

    /**
     *  Clears the whole internal state
     */
    private void clear()
    {
        eventPool.clear();
        patientQueue.clear();
        diedPatients.clear();
        dischargedPatients.clear();
        beds.clear();
        doctors.clear();
        nurses.clear();
        wardies.clear();
        labs.clear();
        xRaysStaff.clear();
        queuedPatients = patientArrivals = 0;
    }

    /**
     *  Some pretty good and simple stats
     */
    public String stats()
    {
        StringBuffer sb = new StringBuffer();

        sb.append("Arrived patients: " + patientArrivals + '\n');
        sb.append("Queued patients: " + queuedPatients + '\n');
        sb.append("Died patients: " + diedPatients.size() + '\n');
        sb.append("Discharged patients: " + dischargedPatients.size() + '\n');
        sb.append("Average time in beds: " + avgPatientBedTime() + '\n');

        return sb.toString();
    }

    /**
     *  Returns the utilisation of resources as a String
     *
     *  @return String The utilisation value
     */
    public String utilisation()
    {
        StringBuffer sb = new StringBuffer();

        for(RESOURCE resource : RESOURCE.values())
            sb.append(resource.name() + " utilisation: " + utilisation(resource) + '\n');

        return sb.toString();
    }

    /**
     *  Returns the average utilisation for a specific type of resources
     *
     *  @param resource The resource type
     *  @return double The utilisation value
     */
    private double utilisation(RESOURCE resource)
    {
        double utilSum = 0;

        Vector<Resource> resTemp = switch(resource)
                {
                    case DOCTOR -> doctors;
                    case NURSE -> nurses;
                    case WARDIE -> wardies;
                    case LAB -> labs;
                    case X_RAY_STAFF -> xRaysStaff;
                    default -> throw new IllegalStateException("Unexpected value: " + resource.name());
                };

        for(Resource res : resTemp)
            utilSum += res.utilization();

        return utilSum / resTemp.size();
    }

    /**
     *  Return the average patient ED-stay value. The whole duration of the treatment tasks, including
     *  gaps in between.
     *
     *  @return double The average patient bed-stay time
     */
    public double avgPatientBedTime()
    {
        double sum = 0;
        for(Patient patient : dischargedPatients)
            sum+= patient.getBedTime();

        return sum / dischargedPatients.size();
    }

    /**
     *  Provides programme usage details
     *
     *  @return String Usage details
     */
    private static String usage()
    {
        return "Dike-ED " + VERSION
                + "\n(C) 2021-2023 by Constantine Kyriakopoulos"
                + "\nReleased under GNU GPL v2"
                + "\n\nUsage: java -jar dike.jar ...\n";
    }

    /**
     *  Executes the simulation for a predefined number of runs
     */
    private static void executeCL(String... args)
    {
        System.out.println(usage());
        System.out.println("Command line interface not yet implemented");
        System.out.println("Using default configuration" + '\n');

        int i = 0;
        double avgBedTime = 0;
        while(i++ < EXECUTIONS)
        {
            Dike dike = new Dike();
            avgBedTime += dike.avgPatientBedTime();
            System.out.print(dike.utilisation());
            System.out.print(dike.stats());
        }
        avgBedTime /= EXECUTIONS;
        System.out.println("Average patient bedtime after " + EXECUTIONS + " executions: " + avgBedTime);
    }

    /**
     *  Programmes main entry point
     *
     *  @param args Command-line arguments
     */
    public static void main(String... args)
    {
        //  Execution either with UI or command line
        if(args.length == 0)
            DikeUI.main();
        else
            executeCL(args);
    }
}
