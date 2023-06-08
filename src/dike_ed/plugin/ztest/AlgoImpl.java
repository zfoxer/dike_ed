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

package dike_ed.plugin.ztest;

import dike_ed.Resource;
import dike_ed.spi.*;

import java.util.Vector;

/**
 *  A test algorithm implementation.
 */
public class AlgoImpl extends Algo
{
    /**
     *  Ability to set the local resources from vector containers.
     *
     *  @param doctors A container with doctor resources.
     *  @param nurses A container with nurse resources.
     *  @param wardies A container with wardie resources.
     *  @param labs A container with lab resources.
     *  @param xRayStaff A container with XRay staff resources.
     */
    public void setResources(Vector<Resource> doctors, Vector<dike_ed.Resource> nurses, Vector<dike_ed.Resource> wardies,
                                      Vector<dike_ed.Resource> labs, Vector<dike_ed.Resource> xRayStaff)
    {

    }

    /**
     *  Ability to set the local resources from a JSON file.
     *
     *  @param jsonRes The JSON file containing the resources.
     */
    public void setResourcesFromJSON(String jsonRes)
    {

    }

    /**
     *  Allocates a sequence of tasks to a patient.
     *
     *  @param patient The patient to allocate tasks to.
     *  @param simTime Current simulation time.
     *  @return The duration of the task sequence.
     */
    public int allocateTasks(dike_ed.Patient patient, int simTime)
    {
        return 0;
    }

    /**
     *  Describes this algorithm implementation.
     *
     *  @return String A text description of the algo implementation.
     */
    @Override
    public String description() {
        return null;
    }
}
