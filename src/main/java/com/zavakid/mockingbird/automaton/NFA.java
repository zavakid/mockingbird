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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * a common NFA automaton
 * 
 * @author Zava 2013-1-28 下午10:00:41
 * @since 0.0.1
 */
public class NFA {

    private boolean           strictStart;
    private boolean           strictEnd;

    private State             startState;
    private Collection<State> currentState = new HashSet<State>();

    public NFA(){
        currentState = new LinkedList<State>();
    }

    public void moveStates(char ch) {
        Set<State> nextState = new HashSet<State>();
        for (State state : currentState) {
            Collection<State> nextAndClosure = state.getNextAndTheirClosure(ch);
            Collection<State> anyAndClosure = state.getNextAndTheirClosure(Transfers.ANY_CHARACTOR);
            nextState.addAll(nextAndClosure);
            nextState.addAll(anyAndClosure);
        }
        currentState = nextState;
    }

    public boolean inAcceptState() {
        for (State state : currentState) {
            if (state.isAccept()) {
                return true;
            }
        }
        return false;
    }

    public void reset() {
        currentState.clear();
        currentState.add(startState);
        currentState.addAll(startState.getClosure());
    }

    public State getStartState() {
        return startState;
    }

    public void setStartState(State startState) {
        this.startState = startState;
    }

    public boolean isStrictStart() {
        return strictStart;
    }

    public boolean isStrictEnd() {
        return strictEnd;
    }

    public void setStrictStart(boolean strictStart) {
        this.strictStart = strictStart;
    }

    public void setStrictEnd(boolean strictEnd) {
        this.strictEnd = strictEnd;
    }

}
