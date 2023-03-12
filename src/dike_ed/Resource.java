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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 *  Base class for resources
 */
public abstract class Resource implements Cloneable
{
    protected int id = 0;

    /**
     *  Patient ID to timestamp for duration
     */
    private List<Pair<Integer, Pair<Integer, Integer>>> reserved = new LinkedList<>();

    /**
     *  Creates a new resource
     */
    public Resource() { }

    /**
     *  Returns resource's ID
     *
     *  @return int The ID
     */
    public int getId()
    {
        return id;
    }

    /**
     *  Returns the timestamp when occupation ends or ended
     *
     *  @return int Timestamp
     */
    public int lastOccupied()
    {
        if(reserved.size() == 0)
            return 0;

        return reserved.get(reserved.size() - 1).rhs().lhs() + reserved.get(reserved.size() - 1).rhs().rhs();
    }

    /**
     *  Occupies this resource for a specific duration
     *
     *  @param patientId The ID of the patient
     *  @param simTime Current simulation time
     *  @param duration The duration
     *  @return int The offset between simulation time and the new start time.
     */
    public int setOccupied(int patientId, int simTime, int duration)
    {
        //  It returns the offset between simTime and the new start time
        if(isOccupied(simTime))
        {
            //  Set occupation after the end
            Pair<Integer, Pair<Integer, Integer>> lastEntry = reserved.get(reserved.size() - 1);
            int newStartTime = lastEntry.rhs().lhs() + lastEntry.rhs().rhs();
            Pair<Integer, Integer> occupiedTimes = new Pair<>(newStartTime, duration);
            Pair<Integer, Pair<Integer, Integer>> occupied = new Pair<>(patientId, occupiedTimes);
            reserved.add(occupied);

            return newStartTime - simTime;
        }

        Pair<Integer, Integer> occupiedRange = new Pair<>(simTime, duration);
        reserved.add(new Pair<>(patientId, occupiedRange));

        return 0;
    }

    /**
     *  Returns this resource's reservations, i.e., patient ID to timestamp for duration
     *
     *  @return List<Pair<Integer, Pair<Integer, Integer>>> List of reservations
     */
    public List<Pair<Integer, Pair<Integer, Integer>>> getReservations()
    {
        return reserved;
    }

    /**
     *  Checks if this resource is occupied
     *
     *  @return boolean Indication
     */
    public boolean isOccupied(int simTime)
    {
        if(reserved.size() == 0)
            return false;

        Pair<Integer, Pair<Integer, Integer>> lastEntry = reserved.get(reserved.size() - 1);

        return lastEntry.rhs().lhs() + lastEntry.rhs().rhs() > simTime;
    }

    /**
     *  Returns the percentage of time it was occupied
     *
     *  @return double Percentage of utilisation
     */
    public double utilization()
    {
        if(reserved.size() == 0)
            return 0;

        Integer durations = 0;
        for(Pair<Integer, Pair<Integer, Integer>> pair : reserved)
            durations += (Integer)pair.rhs().rhs();

        Pair<Integer, Pair<Integer, Integer>> lastEntry = reserved.get(reserved.size() - 1);

        return durations.doubleValue() / (lastEntry.rhs().lhs() + lastEntry.rhs().rhs());
    }

    /**
     *  Describes this type of resource
     *
     *  @return String The description
     */
    abstract public String description();

    /**
     *  Clones this resource
     *
     *  @return Object The cloned object
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        Resource res = (Resource)super.clone();

        // Creating a deep copy
        res.id = id;
        res.reserved = new LinkedList<>();
        Iterator<Pair<Integer, Pair<Integer, Integer>>> it =  reserved.listIterator();
        while(it.hasNext())
            res.reserved.add(it.next());

        return res;
    }

    /**
     *  Clears the state of this resource
     */
    public void clear()
    {
        reserved = new LinkedList<>();
    }

    /**
     *  Removes a reservation at a specific timestamp
     *
     *  @param timestamp The timestamp
     */
    public Pair<Integer, Pair<Integer, Integer>> removeReservation(int timestamp)
    {
        Pair<Integer, Pair<Integer, Integer>> retReservation = null;

        Iterator<Pair<Integer, Pair<Integer, Integer>>> it =  reserved.listIterator();
        while(it.hasNext())
        {
            Pair<Integer, Pair<Integer, Integer>> nextVal = it.next();
            if(nextVal.rhs().lhs() == timestamp)
            {
                try
                {
                    retReservation = (Pair<Integer, Pair<Integer, Integer>>)nextVal.clone();
                }
                catch(CloneNotSupportedException ex)
                {
                    ex.printStackTrace();
                }

                it.remove();
                break;
            }
        }

        return retReservation;
    }
}
