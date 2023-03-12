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
import java.util.Vector;

/**
 *  A pool of events for the discrete simulation process
 */
public class EventPool
{
    /**
     *  Event container storage
     */
    private Vector<Event> eventVec = new Vector<>();

    /**
     *  Inserts an event to this pool
     *
     *  @param event The event to be inserted
     */
    public void addEvent(Event event)
    {
        eventVec.add(event);
    }

    /**
     *  Pops the next event. It returns and then removes it.
     *
     *  @return Event The event
     */
    public Event popNextEvent()
    {
        if(eventVec.isEmpty())
            return null;

        //  Find the first oldest event and erase/return it
        Iterator<Event> it = eventVec.iterator();
        Event retEvent = eventVec.get(0);
        int startTime = retEvent.getStartTime();
        int index = 0; int removeIndex = 0;
        while(it.hasNext())
        {
            Event event = it.next();
            if(event.getStartTime() < startTime)
            {
                retEvent = event;
                startTime = event.getStartTime();
                removeIndex = index;
            }
            index++;
        }
        eventVec.remove(removeIndex);

        return retEvent;
    }

    /**
     *  Returns the size of this pool
     *
     *  @return int The size
     */
    public int size()
    {
        return eventVec.size();
    }

    /**
     *  Clears this pool
     */
    public void clear()
    {
        eventVec.clear();
    }
}
