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

package dike_ed;

/**
 *  A wardie resource type
 */
public class Wardie extends Resource
{
    private static int warCount = 0;

    /**
     *  Uses internal IDs to keep track of doctor resources
     */
    public Wardie()
    {
        super.id = ++warCount;
    }

    /**
     *  Describes this type of resource
     *
     *  @return String The description
     */
    @Override
    public String description()
    {
        return "Wardie";
    }
}
