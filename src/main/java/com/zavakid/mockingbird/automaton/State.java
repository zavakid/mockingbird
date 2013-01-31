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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * @author Zava 2013-1-28 下午9:55:46
 * @since 0.0.1
 */
public class State {

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

    private static int              STATE_COUNT   = 0;

    private int                     id;
    private boolean                 inital;
    private boolean                 accept;
    private Multimap<Object, State> transfers     = HashMultimap.create();

    private State(){
        id = STATE_COUNT++;
    }

    private State(boolean inital, boolean accept){
        id = STATE_COUNT++;
        this.inital = inital;
        this.accept = accept;
    }

    public static State createState() {
        return new State();
    }

    public static State createInitalState() {
        return new State(true, false);
    }

    public static State createAcceptState() {
        return new State(false, true);
    }

    public Set<State> getNextAndTheirClosure(Object object) {
        Collection<State> nextState = transfers.get(object);
        if (nextState.isEmpty()) {
            return Collections.emptySet();
        }
        Set<State> closures = new HashSet<State>();

        // find their closure
        for (State s : nextState) {
            if (!closures.contains(s)) {
                closures.add(s);
                closures.addAll(s.getClosure());
            }
        }

        return Collections.unmodifiableSet(closures);
    }

    public Set<State> getClosure() {
        Set<State> collected = new HashSet<State>();
        collectClosure(collected);
        return collected;
    }

    protected void collectClosure(Set<State> collected) {
        Collection<State> epsilonState = transfers.get(EPSILON);
        for (State state : epsilonState) {
            if (!collected.contains(state)) {
                collected.add(state);
                state.collectClosure(collected);
            }
        }
    }

    public void addTransfer(Object o, State s) {
        transfers.put(o, s);
    }

    public boolean isInital() {
        return inital;
    }

    public void markInital() {
        this.inital = true;
    }

    public void canalInital() {
        this.inital = false;
    }

    public boolean isAccept() {
        return accept;
    }

    public void markAccept() {
        this.accept = true;
    }

    public void canalAccept() {
        this.accept = false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("State [id=" + id + ", inital=" + inital + ", accept=" + accept
                                             + " Transfer : ");
        for (Object key : transfers.keySet()) {
            sb.append(System.getProperty("line.separator"));// TODO 需要封装此方式
            sb.append(key).append(" -> ");
            for (State state : transfers.get(key)) {
                sb.append(state.id).append(" , ");
            }
            sb.append(System.getProperty("line.separator"));// TODO 需要封装此方式
        }
        sb.append(" ]");
        return sb.toString();
    }
}
