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

import com.zavakid.mockingbird.automaton.Fragment;
import com.zavakid.mockingbird.automaton.NFA;
import com.zavakid.mockingbird.automaton.State;
import com.zavakid.mockingbird.common.CharBuffer;
import com.zavakid.mockingbird.common.Stack;

/**
 * NOT Thread safe!
 * 
 * @author Zava 2013-1-28 下午11:01:46
 * @since 0.0.1
 */
public class Matcher {

    private NFA nfa;

    public Matcher(String pattern){
        nfa = compile(pattern);
    }

    public boolean match(String str) {
        nfa.reset();
        for (char ch : str.toCharArray()) {
            nfa.moveStates(ch);
            // for $ at end of pattern
            if (!nfa.isStrictEnd()) {
                if (nfa.inAcceptState()) {
                    return true;
                }
            }
        }

        if (nfa.inAcceptState()) {
            return true;
        }

        // for ^ at start of pattern, no more change to match again
        if (nfa.isStrictStart()) {
            return false;
        }
        while (str.length() > 0) {
            str = str.substring(1);
            return match(str);
        }

        return false;

    }

    protected NFA compile(String pattern) {
        nfa = new NFA();

        if (pattern.startsWith("^")) {
            pattern = pattern.substring(1);
            nfa.setStrictStart(true);
        }

        if (pattern.endsWith("$")) {
            pattern = pattern.substring(0, pattern.length() - 1);
            nfa.setStrictEnd(true);
        }

        CharBuffer chars = new CharBuffer(pattern);
        Fragment frag = parseFragment(chars);

        nfa.setStartState(frag.getStart());
        return nfa;
    }

    protected Fragment parseFragment(CharBuffer chars) {
        Stack<Fragment> fragStack = new Stack<Fragment>();
        while (chars.remain()) {
            char c = chars.next();
            switch (c) {
                default:
                    buildDeafultFrag(fragStack, chars, c);
                    break;
                case '.':
                    buildDeafultFrag(fragStack, chars, State.ANY_CHARACTOR);
                    break;
                case '*':
                    buildStarFrag(fragStack, chars);
                    break;
                case '+':
                    buildPlusFrag(fragStack, chars);
                    break;
                case '?':
                    buildQuestionFrag(fragStack, chars);
                    break;
                case '\\':
                    buildEscapeFrag(fragStack, chars);
                    break;
                case '(':
                    buildLeftBracketFrag(fragStack, chars);
                    break;
                case ')':
                    return join(fragStack);
                case '|':
                    Fragment leftFrag = join(fragStack);
                    fragStack = new Stack<Fragment>();
                    Fragment rightFrag = parseFragment(chars);
                    return leftFrag.union(rightFrag);
            }
        }
        return join(fragStack);
    }

    private void buildDeafultFrag(Stack<Fragment> fragStack, CharBuffer chars, Object c) {
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

    private void buildStarFrag(Stack<Fragment> fragStack, CharBuffer chars) {
        Fragment newFrag = fragStack.pop();
        newFrag = Fragment.starWrap(newFrag);
        catAndPush(fragStack, chars, newFrag);
    }

    private void buildPlusFrag(Stack<Fragment> fragStack, CharBuffer chars) {
        Fragment newFrag = fragStack.pop();
        newFrag = Fragment.plusWrap(newFrag);
        catAndPush(fragStack, chars, newFrag);
    }

    private void buildQuestionFrag(Stack<Fragment> fragStack, CharBuffer chars) {
        Fragment newFrag = fragStack.pop();
        newFrag = Fragment.questionWrap(newFrag);
        catAndPush(fragStack, chars, newFrag);
    }

    private void buildEscapeFrag(Stack<Fragment> fragStack, CharBuffer chars) {
        buildDeafultFrag(fragStack, chars, chars.next());
    }

    private void buildLeftBracketFrag(Stack<Fragment> fragStack, CharBuffer chars) {
        Fragment newFrag = parseFragment(chars);
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
}
