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

import java.util.List;

/**
 *  Patient representation class
 */
public class Patient
{
    private static int patientCount = 0;
    private int id = 0;
    private boolean inBed = false;
    private boolean inQueue = false;
    private int queuedTimestamp = 0;
    private Bed bed = null;
    /**
     *  Treatment path allocated to the patient instance
     */
    private List<Dike.TASK> treatPath = null;
    /**
     *  Simulation time to duration
     */
    private Pair<Integer, Integer> bedTimestamps = null;

    /**
     *  Creates a new patient instance with internal ID
     */
    public Patient()
    {
        id = ++patientCount;
    }

    /**
     *  Returns the ID of the patient
     *
     *  @return int The ID
     */
    public int getId()
    {
        return id;
    }

    /**
     *  Puts this patient into a bed
     *
     *  @param bed The bed
     *  @param simTime Current simulation time
     */
    public void setInBed(Bed bed, int simTime)
    {
        this.bed = bed;
        inBed = true;
        inQueue = false;
        bedTimestamps = new Pair<Integer, Integer>(simTime, 0);
    }

    /**
     *  Removes this patient from the bed
     *
     *  @param simTime Current simulation time
     */
    public void removeFromBed(int simTime)
    {
        this.bed = null;
        inBed = false;
        bedTimestamps = new Pair<Integer, Integer>(bedTimestamps.lhs(), simTime);
    }

    /**
     *  Checks if occupies a bed
     *
     *  @return boolean Indication if in bed
     */
    public boolean isInBed()
    {
        return inBed;
    }

    /**
     *  Checks if he is in a queue
     *
     *  @return boolean Indication if in queue
     */
    public boolean isInQueue()
    {
        return inQueue;
    }

    /**
     *  Gets the time between the start and end of treatment
     *
     *  @return int Treatment time
     */
    public int getBedTime()
    {
        return bedTimestamps != null ? bedTimestamps.rhs() - bedTimestamps.lhs() : 0;
    }

    /**
     *  Puts this patient in a queue
     *
     *  @param time Current simulation time
     */
    public void setInQueue(int time)
    {
        this.queuedTimestamp = time;
        inQueue = true;
    }

    /**
     *  Returns the time this patient was queued
     *
     *  @return int Time in queue
     */
    public int getQueuedTimestamp()
    {
        return queuedTimestamp;
    }

    /**
     *  Sets the treatment path
     *
     *  @param treatPath Treatment path
     */
    public void setTreatPath(List<Dike.TASK> treatPath)
    {
        this.treatPath = treatPath;
    }

    /**
     *  Returns patient's treatment path
     *
     *  @return List<Dike.TASK> List of tasks
     */
    public List<Dike.TASK> getTreatPath()
    {
        return treatPath;
    }
}
