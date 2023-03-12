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
 *  An event for the discrete simulation process
 */
public final class Event implements Comparable<Event>
{
    private static int eventCount = 0;
    private int id = 0;
    /**
     *  Initiating timestamp for the event
     */
    private int startTimestamp = 0;
    /**
     *  Type of the event
     */
    private EventType eventType;
    private int bedId = 0;

    /**
     *  Available event types
     */
    public enum EventType
    {
        PATIENT_ARRIVAL, BED_AVAILABLE
    }

    /**
     *  Creates a new event with a specific type
     */
    public Event(EventType eventType)
    {
        id = ++eventCount;
        this.eventType = eventType;
    }

    /**
     *  Returns the event type
     *
     *  @return EventType The type
     */
    public EventType getType()
    {
        return eventType;
    }

    /**
     *  Returns the event ID
     *
     *  @return int The ID
     */
    public int getId()
    {
        return id;
    }

    /**
     *  Returns the event start time
     *
     *  @return int The start time
     */
    public int getStartTime()
    {
        return startTimestamp;
    }

    /**
     *  Sets the event start time
     *
     *  @param startTimestamp The starting timestamp
     */
    public void setStartTime(int startTimestamp)
    {
        this.startTimestamp = startTimestamp;
    }

    /**
     *  Sets the bed ID of this event
     *
     *  @param bedId The bed ID
     */
    public void setBedId(int bedId)
    {
        this.bedId = bedId;
    }

    /**
     *  Returns the bed ID of this event
     *
     * @return int The bed ID
     */
    public int getBedId()
    {
        return bedId;
    }

    /**
     *  Compares this event to the rhs instance
     *
     *  @param rhs An event to compare this event to
     *  @return int 0 if equal to rhs, -1 if precedes rhs, 1 otherwise
     */
    @Override
    public int compareTo(Event rhs)
    {
        if(startTimestamp == rhs.startTimestamp)
            return 0;
        else
            return Integer.compare(startTimestamp, rhs.startTimestamp);
    }

    /**
     *  Checks for equality by ID
     *
     *  @param rhs An event to compare this event to
     *  @return boolean True if equals
     */
    @Override
    public boolean equals(Object rhs)
    {
        if(!(rhs instanceof Event))
            return false;

        Event eventObj = (Event)rhs;

        return startTimestamp == eventObj.startTimestamp;
    }
}
