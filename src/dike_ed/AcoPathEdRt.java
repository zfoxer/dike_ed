/*
 * Dike ED: Discrete-event simulator for medical emergency departments.
 * Copyright (C) 2021, 2022, 2023 by Constantine Kyriakopoulos
 * zfox@users.sourceforge.net
 * @version 0.3.0
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

/**
 *  Algorithm ACOPath-EDRT to allocate resources in realtime by using swarm intelligence.
 */
public class AcoPathEdRt extends Algorithm
{
    private Vector<Resource> doctors = null;
    private Vector<Resource> nurses = null;
    private Vector<Resource> wardies = null;
    private Vector<Resource> labs = null;
    private Vector<Resource> xRaysStaff = null;

    /**
     *  Constructor initialising the internal treatment paths
     */
    AcoPathEdRt()
    {
        initTreatPaths();
    }

    /**
     *  Sets the internal resources to use
     *
     *  @param doctors Doctor container resources
     *  @param nurses Nurse container resources
     *  @param wardies Wardie container resources
     *  @param labs Lab container resources
     *  @param xRaysStaff XRayStaff container resources
     */
    @Override
    public void setResources(Vector<Resource> doctors, Vector<Resource> nurses, Vector<Resource> wardies,
                             Vector<Resource> labs, Vector<Resource> xRaysStaff)
    {
        this.doctors = doctors;
        this.nurses = nurses;
        this.wardies = wardies;
        this.labs = labs;
        this.xRaysStaff = xRaysStaff;
    }

    /**
     *  Sets the internal resources to use from JSON representation
     *
     *  @param jsonRes JSON formatted resources
     */
    @Override
    public void setResourcesFromJSON(String jsonRes)
    {
    }

    /**
     *  Allocate medical tasks to a specific patient
     *
     *  @param patient Patient to allocate medical tasks to
     *  @param simTime Current simulation time
     *  @return int Duration of the allocation
     */
    @Override
    public int allocateTasks(Patient patient, int simTime)
    {
        //  Allocate in realtime
        allocateTasksRt(patient, simTime);
        acoOptimise();
        return getEstimatedBedTime(patient.getId());
    }

    /**
     *  Allocate medical tasks to a specific patient in realtime
     *
     *  @param patient Patient to allocate medical tasks to
     *  @param simTime Current simulation time
     *  @return int Duration of the allocation
     */
    public int allocateTasksRt(Patient patient, int simTime)
    {
        int retDuration = 0;
        List<Dike.TASK> treatPath = randomTreatPath();

        int startTime = simTime;
        for(Dike.TASK task : treatPath)
        {
            Resource res = pickupRes(task.resource);
            int timeToEnd = task.duration + res.setOccupied(patient.getId(), startTime, task.duration);
            retDuration += timeToEnd;
            startTime += timeToEnd;
        }

        patient.setTreatPath(treatPath);

        return retDuration;
    }

    /**
     *  Returns a picked-up resource from the group
     *
     *  @param resource The resource group
     *  @return Resource The resource
     */
    Resource pickupRes(Dike.RESOURCE resource)
    {
        //  Should always return a resource
        Vector<Resource> resTemp = switch(resource)
                {
                    case DOCTOR -> doctors;
                    case NURSE -> nurses;
                    case WARDIE -> wardies;
                    case LAB -> labs;
                    case X_RAY_STAFF -> xRaysStaff;
                    default -> throw new IllegalStateException("Unexpected value: " + resource.name());
                };

        int lessEndTime = Integer.MAX_VALUE;
        Resource retResource = null;
        for(Resource res : resTemp)
            if(res.lastOccupied() < lessEndTime)
            {
                lessEndTime = res.lastOccupied();
                retResource = res;
            }

        return retResource;
    }

    /**
     *  Returns a description of the algorithm
     *
     *  @return String A nice description
     */
    @Override
    public String description()
    {
        return "ACOPath EDRT: Realtime execution, experimental";
    }

    /**
     *  Optimises the internal resource state using the ACO metaheuristic
     */
    private void acoOptimise()
    {
        for(Dike.RESOURCE resource : Dike.RESOURCE.values())
        {
            //  Traverses all locally stored resources
            Vector<Resource> resTemp = switch(resource)
                    {
                        case DOCTOR -> doctors;
                        case NURSE -> nurses;
                        case WARDIE -> wardies;
                        case LAB -> labs;
                        case X_RAY_STAFF -> xRaysStaff;
                        default -> throw new IllegalStateException("Unexpected value: " + resource.name());
                    };

            List<List<Integer>> log = new LinkedList<>();
            Vector<Resource> resTempClone = (Vector<Resource>)resTemp.clone();
            //  Graph representation of resources
            Map<Pair<Integer, Integer>, Double> edge2distance = acoEdges(resTempClone, log);

            if(countNodes(edge2distance) <= 3)
                return;
            else
                optGraph(edge2distance);

            int pathCount = resTempClone.size();
            List<Vector<Integer>> paths = new LinkedList<>();
            while(pathCount-- > 0)
            {
                if(countNodes(edge2distance) <= 2)
                    break;

                //  Create a new Ant System
                AntSystem as = new AntSystem(edge2distance, AntSystem.ANTS, AntSystem.ITERATIONS);
                //  Get the path
                Vector<Integer> path = as.path(0, countNodes(edge2distance) - 1);
                if(path.size() >= 3)
                {
                    paths.add(path);
                    removePath(path, edge2distance);
                }
            }

            switch(resource)
            {
                //  Convert the representation form back to initial
                case DOCTOR -> {doctors = reverseAcoEdges(paths, log, resTempClone);}
                case NURSE -> {nurses = reverseAcoEdges(paths, log, resTempClone);}
                case WARDIE -> {wardies = reverseAcoEdges(paths, log, resTempClone);}
                case LAB -> {labs = reverseAcoEdges(paths, log, resTempClone);}
                case X_RAY_STAFF -> {xRaysStaff = reverseAcoEdges(paths, log, resTempClone);}
            }
        }
    }

    /**
     *  Removes the path from the graph form of resources
     *
     *  @param path The node path
     *  @param edge2distance Graph edges with distances
     */
    private void removePath(Vector<Integer> path, Map<Pair<Integer, Integer>, Double> edge2distance)
    {
        if(path.size() <= 2)
            return;

        for(int i = 1; i < path.size() - 1; ++i)
        {
            Integer pathNode = path.elementAt(i);
            edge2distance.keySet().removeIf(integerIntegerPair -> pathNode == integerIntegerPair.rhs());
        }
        Integer lastNode = path.lastElement();
        Integer previousNode = path.elementAt(path.size() - 2);
        Iterator<Pair<Integer, Integer>> it = edge2distance.keySet().iterator();
        while (it.hasNext())
        {
            Pair<Integer, Integer> next = it.next();
            if (next.lhs() == previousNode && next.rhs() == lastNode)
            {
                it.remove();
                break;
            }
        }
    }

    /**
     *  Returns the last node
     *
     *  @param edge2distance Graph edges with distances
     *  @return int The last node
     */
    private int getLastNode(Map<Pair<Integer, Integer>, Double> edge2distance)
    {
        Set<Integer> nodes = new TreeSet<>();
        for(Pair<Integer, Integer> key : edge2distance.keySet())
        {
            nodes.add(key.lhs());
            nodes.add(key.rhs());
        }

        return ((Integer)((TreeSet)nodes).last());
    }

    /**
     *  Returns the first node
     *
     *  @param edge2distance Graph edges with distances
     *  @return int The first node
     */
    private int getFirstNode(Map<Pair<Integer, Integer>, Double> edge2distance)
    {
        Set<Integer> nodes = new TreeSet<>();
        for(Pair<Integer, Integer> key : edge2distance.keySet())
        {
            nodes.add(key.lhs());
            nodes.add(key.rhs());
        }

        return ((Integer)((TreeSet)nodes).first());
    }

    /**
     *  Returns the resource container in its initial form
     *
     *  @param paths Node paths
     *  @param log Metadata to use for the conversion
     *  @param resources Resources
     *  @return Vector<Resource> Initial representation from the ACO graph
     */
    private Vector<Resource> reverseAcoEdges(List<Vector<Integer>> paths,
                                             List<List<Integer>> log, Vector<Resource> resources)
    {
        Vector<Resource> retResources = new Vector<>();

        for(Vector<Integer> path : paths)
        {
            if(path.size() <= 2)
                continue;

            int node = path.get(1);
            Resource resource = null;
            try
            {
                resource = (Resource)getResource(node, log, resources).clone();
            }
            catch(CloneNotSupportedException ex)
            {
                ex.printStackTrace();
            }

            if(path.size() > 3)
                resource.clear();

            for(int i = 2; i < path.size() - 1; ++i)
            {
                int pathNode = path.get(i);
                Resource nodeRes = getResource(pathNode, log, resources);
                Pair<Integer, Pair<Integer, Integer>> reservation =
                        nodeRes.removeReservation(getTimestamp(pathNode, log, resources));

                resource.setOccupied(reservation.lhs(), reservation.rhs().lhs(), reservation.rhs().rhs());
            }

            retResources.add(resource);
        }

        return retResources;
    }

    /**
     *  Returns the resource from a node
     *
     *  @param node Node number
     *  @param log Metadata to use for the conversion
     *  @param resources Resources
     *  @return Resource Resource from node
     */
    private Resource getResource(int node, List<List<Integer>> log, Vector<Resource> resources)
    {
        Resource retResource = null;

        for(List<Integer> row : log)
        {
            if(row.get(2) == node)
            {
                int resourceId = row.get(0);
                for(Resource res : resources)
                    if(res.getId() == resourceId)
                    {
                        retResource = res;
                        break;
                    }
            }
            if(retResource != null)
                break;
        }

        return retResource;
    }

    /**
     *  Returns the timestamp from a node
     *
     *  @param node Node number
     *  @param log Metadata to use for the conversion
     *  @param resources Resources
     *  @return int Resource from node
     */
    private int getTimestamp(int node, List<List<Integer>> log, Vector<Resource> resources)
    {
        int retTimestamp = 0;

        for(List<Integer> row : log)
        {
            if(row.get(2) == node)
            {
                retTimestamp = row.get(3);
                break;
            }
        }

        return retTimestamp;
    }

    /**
     *  Optimises the graph representation. Not required for runtime execution.
     *
     *  @param edge2distance ACO graph representation
     */
    private void optGraph(Map<Pair<Integer, Integer>, Double> edge2distance)
    {
    }

    /**
     *  Converts to an ACO-friendly representation
     *
     *  @param resources Initial resources
     *  @param log Metadata to store for a later conversion
     */
    private Map<Pair<Integer, Integer>, Double> acoEdges(Vector<Resource> resources, List<List<Integer>> log)
    {
        Map<Pair<Integer, Integer>, Double> retEdge2distance = new HashMap<>();

        int strNode = 0;
        int endNode = 1;
        for(Resource res : resources)
        {
            // Create edge2distance suitable to Ant System's topology input
            int timeStamp = 0;
            List<Pair<Integer, Pair<Integer, Integer>>> reservations = res.getReservations();
            if(reservations.size() == 0)
                continue;

            for(Pair<Integer, Pair<Integer, Integer>> reservation : reservations)
            {
                int gap = reservation.rhs().lhs() - timeStamp;
                retEdge2distance.put(new Pair<Integer, Integer>(strNode, endNode),
                        createWeight(gap, reservation.rhs().rhs()));
                List<Integer> logRowValues = new LinkedList<>();

                //  Resource ID, Patient ID, ACO node number, timestamp
                logRowValues.add(res.getId());
                logRowValues.add(reservation.lhs());
                logRowValues.add(endNode);
                logRowValues.add(reservation.rhs().lhs());
                log.add(logRowValues);

                ++endNode;
                strNode = endNode - 1;
                timeStamp = reservation.rhs().lhs() + reservation.rhs().rhs();
            }

            retEdge2distance.put(new Pair<Integer, Integer>(strNode, Integer.MAX_VALUE),
                    createWeight(0, 0));
            strNode = 0;
        }

        if(retEdge2distance.size() == 0)
        {
            retEdge2distance.put(new Pair<Integer, Integer>(0, Integer.MAX_VALUE),
                    createWeight(0, 0));
        }

        retEdge2distance = replaceLimit(retEdge2distance, Integer.MAX_VALUE);

        return retEdge2distance;
    }

    /**
     *  Counts the ACO graph nodes
     *
     *  @param acoEdges ACO graph representation
     *  @return int The counter
     */
    private int countNodes(Map<Pair<Integer, Integer>, Double> acoEdges)
    {
        Set<Integer> nodes = new TreeSet<>();
        for(Map.Entry<Pair<Integer, Integer>, Double> entry : acoEdges.entrySet())
        {
            nodes.add(entry.getKey().lhs());
            nodes.add(entry.getKey().rhs());
        }

        return nodes.size();
    }

    /**
     *  Replaces the given limit with the last node
     *
     *  @param acoEdges ACO graph representation
     *  @param limit The limit
     *  @return Map<Pair<Integer, Integer>, Double> Graph with the new limit
     */
    private Map<Pair<Integer, Integer>, Double> replaceLimit(Map<Pair<Integer, Integer>, Double> acoEdges, int limit)
    {
        int nextLimit = countNodes(acoEdges) - 1;

        Map<Pair<Integer, Integer>, Double> nextACoEdges = new HashMap<>();
        for(Map.Entry<Pair<Integer, Integer>, Double> entry : acoEdges.entrySet())
        {
            Pair<Integer, Integer> nextPair = null;
            if(entry.getKey().lhs() == limit)
                nextPair = new Pair<>(nextLimit, entry.getKey().rhs());
            if(entry.getKey().rhs() == limit)
                nextPair = new Pair<>(entry.getKey().lhs(), nextLimit);

            if(nextPair != null)
                nextACoEdges.put(nextPair, entry.getValue());
            else
                nextACoEdges.put(entry.getKey(), entry.getValue());
        }

        return nextACoEdges;
    }

    /**
     *  Creates a weight value
     *
     *  @param gap Gap between tasks
     *  @param duration Processing time
     *  @return double The weight
     */
    private double createWeight(int gap, int duration)
    {
        //  Edge weight: Gap plus dest processing time, for now
        return gap + duration > 0 ? gap + duration : 2;
    }

    /**
     *  Retuens the estimated bed time
     *
     *  @param patientId Specific patient
     *  @return int ED time for the patient
     */
    private int getEstimatedBedTime(int patientId)
    {
        int lowMark = Integer.MAX_VALUE;
        int highMark = Integer.MIN_VALUE;

        for(Dike.RESOURCE resource : Dike.RESOURCE.values())
        {
            Vector<Resource> resIndex = switch(resource)
                    {
                        case DOCTOR -> doctors;
                        case NURSE -> nurses;
                        case WARDIE -> wardies;
                        case LAB -> labs;
                        case X_RAY_STAFF -> xRaysStaff;
                        default -> throw new IllegalStateException("Unexpected value: " + resource.name());
                    };

            for(Resource res : resIndex)
            {
                List<Pair<Integer, Pair<Integer, Integer>>> reservations = res.getReservations();
                for(Pair<Integer, Pair<Integer, Integer>> entry : reservations)
                    if(entry.lhs() == patientId)
                    {
                        if(entry.rhs().lhs() < lowMark)
                            lowMark = entry.rhs().lhs();
                        if(entry.rhs().lhs() + entry.rhs().rhs() > highMark)
                            highMark = entry.rhs().lhs() + entry.rhs().rhs();
                    }
            }
        }

        return highMark - lowMark;
    }
}
