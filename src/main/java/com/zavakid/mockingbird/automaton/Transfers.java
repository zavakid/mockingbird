/*
   Copyright 2013 Zava (http://www.zavakid.com)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.zavakid.mockingbird.automaton;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * @author Zava 2013-2-4 下午1:47:44
 * @since 0.0.1
 */
public class Transfers {

    public static Object            EPSILON       = new Object() {

                                                      @Override
                                                      public String toString() {
                                                          return "epsilon";
                                                      }
                                                  };

    public static Object            ANY_CHARACTOR = new Object() {

                                                      @Override
                                                      public String toString() {
                                                          return ".";
                                                      }
                                                  };

    private Multimap<Object, State> innerMap;
    private Multimap<State, Object> negateMap;
    private boolean                 hasNegate     = false;

    public Transfers(){
        this.innerMap = HashMultimap.create();
    }

    public Collection<State> get(Object object) {
        Collection<State> result = innerMap.get(object);

        // . in negate mode is just a noram char
        if (!hasNegate || EPSILON == object || ANY_CHARACTOR == object) {
            return result;
        }

        // for negate case, e.g. [^abc]
        Set<State> negateStates = negateMap.keySet();
        for (State state : negateStates) {

            if (result.contains(state)) {
                continue;
            }

            Collection<Object> negateObjects = negateMap.get(state);
            if (!negateObjects.contains(object)) {
                result.add(state);
            }
        }

        return result;

    }

    public void put(Object o, State s) {
        innerMap.put(o, s);
    }

    /**
     * for negate used, not thread safe<br/>
     * e.g. [^ab|c] , need to add negate of a,b,c,|,c to the transfer
     * 
     * @param s
     * @param objects
     */
    public void putNegate(State s, Object... objects) {
        if (!hasNegate) {
            hasNegate = true;
            negateMap = HashMultimap.create();
        }
        for (Object o : objects) {
            negateMap.put(s, o);
        }
    }

    // TODO, need implement toString for human reader
}
