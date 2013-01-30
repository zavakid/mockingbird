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
package com.zavakid.mockingbird;

import java.util.Stack;

import com.zavakid.mockingbird.automaton.Fragment;
import com.zavakid.mockingbird.automaton.NFA;
import com.zavakid.mockingbird.automaton.State;
import com.zavakid.mockingbird.common.CharBuffer;

/**
 * NOT Thread safe!
 * 
 * @author Zava 2013-1-28 下午11:01:46
 * @since 0.0.1
 */
public class MatcherWithFrag {

    private NFA nfa;

    public MatcherWithFrag(String pattern){
        nfa = compile(pattern);
        // may be we can add cache for some hot pattern
    }

    public boolean match(String string) {
        nfa.reset();
        for (char ch : string.toCharArray()) {
            nfa.moveStates(ch);
        }
        return nfa.inAcceptState();
    }

    protected NFA compile(String pattern) {
        CharBuffer chars = new CharBuffer(pattern);
        Stack<Fragment> fragStack = new Stack<Fragment>();
        Stack<Character> operatorStack = new Stack<Character>();
        Fragment currentFrag = null;
        while (chars.remain()) {
            char c = chars.next();
            switch (c) {
                default:
                    currentFrag = buildDeafultFrag(currentFrag, fragStack, operatorStack, c);
                    break;
                case '.':
                    currentFrag = buildDotFrag(currentFrag, fragStack, operatorStack, c);
                    break;
                case '*':
                    currentFrag = buildStartFrag(currentFrag, fragStack, operatorStack, c);
                    break;
                case '+':
                    currentFrag = buildPlusFrag(currentFrag, fragStack, operatorStack, c);
                    break;
                case '?':
                    currentFrag = buildQuestionFrag(currentFrag, fragStack, operatorStack, c);
                    break;
            }
        }

        if (currentFrag == null) {
            throw new IllegalArgumentException("the pattern : " + pattern + " complied failed");
        }

        nfa = new NFA();
        nfa.setStartState(currentFrag.getStart());
        return nfa;
    }

    private Fragment buildDeafultFrag(Fragment currentFrag, Stack<Fragment> fragStack, Stack<Character> operatorStack,
                                      char c) {
        if (currentFrag == null) {
            currentFrag = Fragment.create();
            currentFrag.getStart().addTransfer(c, currentFrag.getEnd());
            return currentFrag;
        }
        currentFrag.cat(c, State.createState());
        return currentFrag;
    }

    private Fragment buildDotFrag(Fragment currentFrag, Stack<Fragment> fragStack, Stack<Character> operatorStack,
                                  char c) {
        if (currentFrag == null) {
            currentFrag = Fragment.create();
            currentFrag.getStart().addTransfer(State.ANY_CHARACTOR, currentFrag.getEnd());
            return currentFrag;
        }
        currentFrag.cat(State.ANY_CHARACTOR, State.createState());
        return currentFrag;
    }

    private Fragment buildStartFrag(Fragment currentFrag, Stack<Fragment> fragStack, Stack<Character> operatorStack,
                                    char c) {
        if (currentFrag == null) {
            throw new IllegalArgumentException(" * must have a frag before it ");
        }
        Fragment newFragment = currentFrag.start(currentFrag);
        return newFragment;
    }

    private Fragment buildPlusFrag(Fragment currentFrag, Stack<Fragment> fragStack, Stack<Character> operatorStack,
                                   char c) {
        if (currentFrag == null) {
            throw new IllegalArgumentException(" + must have a frag before it ");
        }
        Fragment newFragment = currentFrag.plus(currentFrag);
        return newFragment;
    }

    private Fragment buildQuestionFrag(Fragment currentFrag, Stack<Fragment> fragStack, Stack<Character> operatorStack,
                                       char c) {
        if (currentFrag == null) {
            throw new IllegalArgumentException(" ? must have a frag before it ");
        }
        Fragment newFragment = currentFrag.question(currentFrag);
        return newFragment;
    }

    private static Object convertMetaIfNeccessary(char c) {
        switch (c) {
            case '.':
                return State.ANY_CHARACTOR;
            default:
                return c;
        }
    }
}
