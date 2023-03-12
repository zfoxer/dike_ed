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

/**
 *  An ED bed type
 */
public class Bed implements Comparable<Bed>
{
    public static final int NOT_OCCUPIED = -1;
    private static int bedCount = 0;
    private int id = 0;
    /**
     *  A patient reference this bed is related to
     */
    private Patient patient = null;

    /**
     *  Timestamp when the bed was occupied
     */
    private int tsOccupied = NOT_OCCUPIED;

    /**
     *  Timestamp the bed is to be available again
     */
    private int tsToFree = 0;

    /**
     *  A new bed with increasing ID is created
     */
    public Bed()
    {
        id = ++bedCount;
    }

    /**
     *  Returns this bed's ID
     *
     *  @return int The ID
     */
    public int getId()
    {
        return id;
    }

    /**
     *  Adds a patient to this bed
     *
     *  @param patient Patient instance
     *  @param simTime Current simulation time
     *  @param tsToFree Timestamp this bed will be available again
     */
    public void addPatient(Patient patient, int simTime, int tsToFree)
    {
        this.patient = patient;
        patient.setInBed(this, simTime);
        tsOccupied = simTime;
        this.tsToFree = tsToFree;
    }

    /**
     *  Removes the patient from this bed
     *
     *  @param simTime Current simulation time
     *  @return Patient The removed patient reference
     */
    public Patient removePatient(int simTime)
    {
        Patient retPatient = patient;
        patient = null;
        tsOccupied = NOT_OCCUPIED;
        tsToFree = simTime;

        return retPatient;
    }

    /**
     *  Checks the availability of this bed
     *
     *  @return boolean If available
     */
    public boolean available()
    {
        return patient == null && tsOccupied == NOT_OCCUPIED;
    }

    /**
     *  Checks the availability of this bed at a specific timestamp
     *
     *  @param timestamp Simulation time
     *  @return boolean If available
     */
    public boolean availableAt(int timestamp)
    {
        return tsToFree == timestamp;
    }

    /**
     *  Compares this bed to the rhs instance
     *
     *  @param rhs A bed to compare this bed to
     *  @return int 0 if equal to rhs, -1 if precedes rhs, 1 otherwise
     */
    @Override
    public int compareTo(Bed rhs)
    {
        if(tsToFree == rhs.tsToFree)
            return getId() < rhs.getId() ? -1 : 1;

        return tsToFree < rhs.tsToFree ? -1 : 1;
    }

    /**
     *  Checks for equality by ID
     *
     *  @param rhs A bed to compare this bed to
     *  @return boolean True if equals
     */
    @Override
    public boolean equals(Object rhs)
    {
        if(!(rhs instanceof Bed))
            return false;

        Bed bedObj = (Bed)rhs;

        return getId() == bedObj.getId();
    }
}
