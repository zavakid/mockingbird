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
        // but caching need upper layer to do
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
        Stack<Stack<Fragment>> frameStack = new Stack<Stack<Fragment>>();
        Stack<Fragment> fragStack = new Stack<Fragment>();
        Stack<Character> operatorStack = new Stack<Character>();
        while (chars.remain()) {
            char c = chars.next();
            switch (c) {
                default:
                    buildDeafultFrag(fragStack, operatorStack, chars, c);
                    break;
                case '.':
                    buildDeafultFrag(fragStack, operatorStack, chars, State.ANY_CHARACTOR);
                    break;
                case '*':
                    buildStarFrag(fragStack, operatorStack, chars);
                    break;
                case '+':
                    buildPlusFrag(fragStack, operatorStack, chars);
                    break;
                case '?':
                    buildQuestionFrag(fragStack, operatorStack, chars);
                    break;
                case '(':
                    // add a new fragStack
                    frameStack.push(fragStack);
                    fragStack = new Stack<Fragment>();
                    break;
                case ')':
                    Fragment frag = join(fragStack);
                    fragStack = frameStack.pop();
                    catAndPush(fragStack, chars, frag);
                    break;
            }
        }

        assert fragStack.size() == 1;
        nfa = new NFA();
        nfa.setStartState(fragStack.get(0).getStart());
        return nfa;
    }

    private void buildDeafultFrag(Stack<Fragment> fragStack, Stack<Character> operatorStack, CharBuffer chars, Object c) {
        Fragment newFrag = Fragment.create();
        newFrag.getStart().addTransfer(c, newFrag.getEnd());
        catAndPush(fragStack, chars, newFrag);

    }

    private boolean canCat(Character c) {
        if (c == null) {
            return true;
        }
        switch (c) {
            case '*':
            case '+':
            case '?':
                return false;
            default:
                break;
        }
        return true;
    }

    private void buildStarFrag(Stack<Fragment> fragStack, Stack<Character> operatorStack, CharBuffer chars) {
        Fragment newFrag = fragStack.pop();
        newFrag = Fragment.starWrap(newFrag);
        catAndPush(fragStack, chars, newFrag);
    }

    private void buildPlusFrag(Stack<Fragment> fragStack, Stack<Character> operatorStack, CharBuffer chars) {
        Fragment newFrag = fragStack.pop();
        newFrag = Fragment.plusWrap(newFrag);
        catAndPush(fragStack, chars, newFrag);
    }

    private void buildQuestionFrag(Stack<Fragment> fragStack, Stack<Character> operatorStack, CharBuffer chars) {
        Fragment newFrag = fragStack.pop();
        newFrag = Fragment.questionWrap(newFrag);
        catAndPush(fragStack, chars, newFrag);
    }

    protected void catAndPush(Stack<Fragment> fragStack, CharBuffer chars, Fragment newFrag) {
        if (fragStack.size() != 0) {
            if (canCat(chars.lookAhead(1))) {
                Fragment pre = fragStack.pop();
                newFrag = pre.cat(newFrag);
            }
        }
        fragStack.push(newFrag);
    }

    private Fragment join(Stack<Fragment> fragStack) {
        Fragment start = fragStack.remove(0);
        if (fragStack.size() == 0) {
            return start;
        }

        for (Fragment fragment : fragStack) {
            start = start.cat(fragment);
        }
        return start;
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
