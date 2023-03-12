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

/**
 *  Interface for algorithms to execute as plugins.
 */
public interface Algo
{
    /**
     *  Provides updated resource state.
     *
     *  @param jSonRes JSON text with resources.
     */
    void updResources(String jSonRes);

    /**
     *  Executes the resource allocation process.
     *
     *  @return String Allocated resources in JSON.
     */
    String run();

    /**
     *  Indicates if the allocation process finished.
     *
     *  @return boolean If false, allocation finished.
     */
    boolean repeat();

    /**
     *  Describes this algorithm implementation.
     *
     *  @return String A text description of the algo implementation.
     */
    String description();
}
