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
 *  Typical pair record
 *
 *  @param lhs The left item
 *  @param rhs The right item
 *  @param <L> The type of the left item
 *  @param <R> The type of the right item
 */
record Pair<L, R>(L lhs, R rhs) implements Cloneable
{
    /**
     *  Asserts for the null case
     *
     *  @param lhs The left hand side
     *  @param rhs The right hand side
     */
    Pair
    {
        assert lhs != null;
        assert rhs != null;
    }

    /**
     *  Hashcode method
     *
     *  @return int The hash code
     */
    @Override
    public int hashCode()
    {
        return lhs.hashCode() ^ rhs.hashCode();
    }

    /**
     *  Checks for equality
     *
     *  @param obj The reference object with which to compare
     *  @return true If equals
     */
    @Override
    public boolean equals(Object obj)
    {
        if(!(obj instanceof Pair))
            return false;

        Pair pairObj = (Pair)obj;
        return this.lhs.equals(pairObj.lhs()) && this.rhs.equals(pairObj.rhs());
    }

    /**
     *  Clones this instance
     *
     *  @return Object Cloned object
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        Pair<L, R> res = (Pair<L, R>)super.clone();

        return res;
    }
}
