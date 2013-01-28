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
import java.util.Stack;

import com.zavakid.mockingbird.common.CharBuffer;

/**
 * @author Zava 2013-1-28 下午10:00:41
 * @since 0.0.1
 */
public class NFA {

    private State             initalState;
    private Collection<State> currentState = new HashSet<State>();

    private NFA(){
        initalState = State.createInitalState();
        currentState = new LinkedList<State>();
    }

    public static NFA complie(String pattern) {
        CharBuffer chars = new CharBuffer(pattern);
        NFA nfa = new NFA();
        State tail = nfa.initalState;
        Stack<State> stack = new Stack<State>();

        while (chars.remain()) {
            State newState;
            char c = chars.next();
            switch (c) {
                default:
                    newState = buildDefaultState(tail, stack, chars, c);
                    break;
                case '.':
                    newState = buildDotState(tail, stack, chars, c);
                    break;
                case '*':
                    newState = buildStarState(tail, stack, chars, c);
                    break;
            }
            tail = newState;
        }

        tail.markAccept();
        return nfa;
    }

    /**
     * thread not safe
     * 
     * @param string
     * @return
     */
    public boolean match(String string) {
        currentState.clear();
        currentState.add(initalState);
        for (char ch : string.toCharArray()) {
            moveStates(ch);
        }

        return inAcceptStates();

    }

    private boolean inAcceptStates() {
        for (State state : currentState) {
            if (state.isAccept()) {
                return true;
            }
        }
        return false;
    }

    private void moveStates(char ch) {
        Set<State> nextState = new HashSet<State>();
        for (State state : currentState) {
            Collection<State> nextAndClosure = state.getNextAndClosure(ch);
            Collection<State> anyAndClosure = state.getNextAndClosure(State.ANY_CHARACTOR);
            nextState.addAll(nextAndClosure);
            nextState.addAll(anyAndClosure);
        }
        currentState = nextState;
    }

    private static State buildDotState(State tail, Stack<State> stack, CharBuffer chars, char c) {
        State newState = State.createState();
        tail.addTransfer(State.ANY_CHARACTOR, newState);
        stack.push(tail);
        return newState;
    }

    private static State buildDefaultState(State tail, Stack<State> stack, CharBuffer chars, char c) {
        State newState = State.createState();
        tail.addTransfer(c, newState);
        stack.push(tail);
        return newState;
    }

    private static State buildStarState(State tail, Stack<State> stack, CharBuffer chars, char c) {
        State prev = stack.pop();
        tail.addTransfer(State.EPSILON, prev);
        tail.addTransfer(chars.lookbefore(1), tail);
        return prev;
    }

}
