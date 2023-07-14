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

package dike_ed.plugin.rres;

import dike_ed.Resource;
import dike_ed.plugin_api.*;

import java.util.List;
import java.util.Vector;

/**
 *  Algorithm to allocate resources by picking up uniformly random resources
 */
public class RandomResourceAlgo extends Algo
{
    protected Vector<Resource> doctors = null;
    protected Vector<Resource> nurses = null;
    protected Vector<Resource> wardies = null;
    protected Vector<Resource> labs = null;
    protected Vector<Resource> xRaysStaff = null;

    /**
     *  Constructor initialising the internal treatment paths
     */
    public RandomResourceAlgo()
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
    public int allocateTasks(dike_ed.Patient patient, int simTime)
    {
        int retDuration = 0;
        List<dike_ed.Dike.TASK> treatPath = randomTreatPath();

        int startTime = simTime;
        for(dike_ed.Dike.TASK task : treatPath)
        {
            Resource res = randomRes(task.resource);
            int timeToEnd = task.duration + res.setOccupied(patient.getId(), startTime, task.duration);
            retDuration += timeToEnd;
            startTime += timeToEnd;
        }

        patient.setTreatPath(treatPath);

        return retDuration;
    }

    /**
     *  Returns the random resource from the group
     *
     *  @param resource The resource group
     *  @return Resource The random resource
     */
    protected Resource randomRes(dike_ed.Dike.RESOURCE resource)
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

        return resTemp.elementAt((int)(resTemp.size() * Math.random()));
    }

    /**
     *  Returns a description of the algorithm
     *
     *  @return String A nice description
     */
    @Override
    public String description()
    {
        return "Random Resource First";
    }
}
