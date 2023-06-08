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

package dike_ed.spi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public abstract class Algo
{
    /**
     *  Predefined treatment paths. Each path is a sequence of tasks.
     */
    protected List<dike_ed.Dike.TASK> treatPath1, treatPath21, treatPath22, treatPath31, treatPath32, treatPath41,
            treatPath42, treatPath43;

    /**
     *  Ability to set the local resources from vector containers.
     *
     *  @param doctors A container with doctor resources.
     *  @param nurses A container with nurse resources.
     *  @param wardies A container with wardie resources.
     *  @param labs A container with lab resources.
     *  @param xRayStaff A container with XRay staff resources.
     */
    public abstract void setResources(Vector<dike_ed.Resource> doctors, Vector<dike_ed.Resource> nurses, Vector<dike_ed.Resource> wardies,
                                      Vector<dike_ed.Resource> labs, Vector<dike_ed.Resource> xRayStaff);

    /**
     *  Ability to set the local resources from a JSON file.
     *
     *  @param jsonRes The JSON file containing the resources.
     */
    public abstract void setResourcesFromJSON(String jsonRes);

    /**
     *  Allocates a sequence of tasks to a patient.
     *
     *  @param patient The patient to allocate tasks to.
     *  @param simTime Current simulation time.
     *  @return The duration of the task sequence.
     */
    public abstract int allocateTasks(dike_ed.Patient patient, int simTime);

    /**
     *  Allocates a sequence of tasks to a patient.
     *
     *  @return The description of this algorithm.
     */
    public abstract String description();

    /**
     *  Initialises the available sequence of tasks for patients.
     */
    protected void initTreatPaths()
    {
        //  Hardcoded treatment paths.
        treatPath1 = Arrays.asList(dike_ed.Dike.TASK.BED_ALLOC, dike_ed.Dike.TASK.MEDICAL_ASS, dike_ed.Dike.TASK.VITAL_SIGNS, dike_ed.Dike.TASK.REVIEW_DISCHARGE);
        treatPath21 = Arrays.asList(dike_ed.Dike.TASK.BED_ALLOC, dike_ed.Dike.TASK.MEDICAL_ASS, dike_ed.Dike.TASK.X_RAY, dike_ed.Dike.TASK.PLASTERING,
                dike_ed.Dike.TASK.ANAESTHETIC_REC, dike_ed.Dike.TASK.REVIEW_DISCHARGE);
        treatPath22 = Arrays.asList(dike_ed.Dike.TASK.BED_ALLOC, dike_ed.Dike.TASK.MEDICAL_ASS, dike_ed.Dike.TASK.X_RAY, dike_ed.Dike.TASK.ANAESTHETIC,
                dike_ed.Dike.TASK.ANAESTHETIC_REC, dike_ed.Dike.TASK.REVIEW_DISCHARGE);
        treatPath31 = Arrays.asList(dike_ed.Dike.TASK.BED_ALLOC, dike_ed.Dike.TASK.MEDICAL_ASS, dike_ed.Dike.TASK.SUTURES, dike_ed.Dike.TASK.DISCHARGE);
        treatPath32 = Arrays.asList(dike_ed.Dike.TASK.BED_ALLOC, dike_ed.Dike.TASK.MEDICAL_ASS, dike_ed.Dike.TASK.NURSE_TREATMENT, dike_ed.Dike.TASK.DISCHARGE);
        treatPath41 = Arrays.asList(dike_ed.Dike.TASK.BED_ALLOC, dike_ed.Dike.TASK.MEDICAL_ASS, dike_ed.Dike.TASK.PATIENT_NOTES,
                dike_ed.Dike.TASK.ADMIT_IMPATIENT_UNIT, dike_ed.Dike.TASK.TRANSFER_IMPATIENT_UNIT);
        treatPath42 = Arrays.asList(dike_ed.Dike.TASK.BED_ALLOC, dike_ed.Dike.TASK.MEDICAL_ASS, dike_ed.Dike.TASK.TAKE_BLOODS, dike_ed.Dike.TASK.PATHOLOGY_TEST,
                dike_ed.Dike.TASK.ADMIT_IMPATIENT_UNIT, dike_ed.Dike.TASK.TRANSFER_IMPATIENT_UNIT);
        treatPath43 = Arrays.asList(dike_ed.Dike.TASK.BED_ALLOC, dike_ed.Dike.TASK.VITAL_SIGNS, dike_ed.Dike.TASK.PATIENT_NOTES,
                dike_ed.Dike.TASK.ADMIT_IMPATIENT_UNIT, dike_ed.Dike.TASK.TRANSFER_IMPATIENT_UNIT);
    }

    /**
     *  Picks up a uniformly random treatment path.
     *
     *  @return The treatment path.
     */
    protected List<dike_ed.Dike.TASK> randomTreatPath()
    {
        List<List<dike_ed.Dike.TASK>> tasks = new ArrayList<>(Arrays.asList(treatPath1, treatPath21, treatPath22, treatPath31,
                treatPath32, treatPath41, treatPath42, treatPath43));

        return (List<dike_ed.Dike.TASK>)tasks.toArray()[(int)(tasks.size() * Math.random())];
    }
}
