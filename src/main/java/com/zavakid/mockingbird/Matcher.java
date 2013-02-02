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
        ParseContext context = new ParseContext();

        if (pattern.startsWith("^")) {
            pattern = pattern.substring(1);
            nfa.setStrictStart(true);
        }

        if (pattern.endsWith("$")) {
            pattern = pattern.substring(0, pattern.length() - 1);
            nfa.setStrictEnd(true);
        }

        CharBuffer chars = new CharBuffer(pattern);
        Fragment frag = parseFragment(chars, context);

        nfa.setStartState(frag.getStart());
        return nfa;
    }

    protected Fragment parseFragment(CharBuffer chars, ParseContext context) {
        Stack<Fragment> fragStack = new Stack<Fragment>();
        while (chars.remain()) {
            char c = chars.next();
            switch (c) {
                default:
                    buildDeafultFrag(fragStack, chars, c, context);
                    break;
                case '.':
                    buildDeafultFrag(fragStack, chars, State.ANY_CHARACTOR, context);
                    break;
                case '*':
                    buildStarFrag(fragStack, chars, context);
                    break;
                case '+':
                    buildPlusFrag(fragStack, chars, context);
                    break;
                case '?':
                    buildQuestionFrag(fragStack, chars, context);
                    break;
                case '\\':
                    buildEscapeFrag(fragStack, chars, context);
                    break;
                case '(':
                    buildLeftBracketFrag(fragStack, chars, context);
                    break;
                case ')':
                    return merge(fragStack, context);
                case '|':
                    // in square bracket, | is a normal char
                    if (context.inLeftSquareBracket()) {
                        buildDeafultFrag(fragStack, chars, c, context);
                        break;
                    }
                    Fragment leftFrag = merge(fragStack, context);
                    fragStack = new Stack<Fragment>();
                    Fragment rightFrag = parseFragment(chars, context);
                    return leftFrag.union(rightFrag);
                case '[':
                    buildLeftSquarreBracketFrag(fragStack, chars, context);
                    break;
                case ']':
                    if (context.inLeftSquareBracket()) {
                        Fragment f = merge(fragStack, context);
                        context.leaveLeftSquarreBracket();
                        return f;
                    } else {
                        buildDeafultFrag(fragStack, chars, ']', context);
                    }
                    break;

            }
        }

        check(fragStack, context);
        return merge(fragStack, context);
    }

    private void check(Stack<Fragment> fragStack, ParseContext context) {
        if (context.inLeftSquareBracket()) {
            throw new IllegalArgumentException("the pattern has [ but no ]");
        }

    }

    private void buildDeafultFrag(Stack<Fragment> fragStack, CharBuffer chars, Object c, ParseContext context) {
        Fragment newFrag = Fragment.create();
        if (needConnect(chars, context)) {
            chars.next();
            Character end = chars.next();
            if (end.compareTo((Character) c) < 0) {
                throw new IllegalArgumentException("invalid range " + c + "-" + end);
            }
            for (char i = (Character) c; i <= end; i++) {
                newFrag.getStart().addTransfer(i, newFrag.getEnd());
            }
        } else {
            newFrag.getStart().addTransfer(c, newFrag.getEnd());
        }
        mergeAndPush(fragStack, chars, newFrag, context);

    }

    // match the strut like : [0-9]
    private boolean needConnect(CharBuffer chars, ParseContext context) {
        if (!context.inLeftSquareBracket()) {
            return false;
        }
        Character next1 = chars.lookAhead(1);
        Character next2 = chars.lookAhead(2);
        if ((next1 != null && '-' == next1) && (next2 != null & ']' != next2)) {
            return true;
        }
        return false;
    }

    // if not quantifier, we can merge
    private boolean canMerge(Character c) {
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

    private void buildStarFrag(Stack<Fragment> fragStack, CharBuffer chars, ParseContext context) {
        Fragment newFrag = fragStack.pop();
        newFrag = Fragment.starWrap(newFrag);
        mergeAndPush(fragStack, chars, newFrag, context);
    }

    private void buildPlusFrag(Stack<Fragment> fragStack, CharBuffer chars, ParseContext context) {
        Fragment newFrag = fragStack.pop();
        newFrag = Fragment.plusWrap(newFrag);
        mergeAndPush(fragStack, chars, newFrag, context);
    }

    private void buildQuestionFrag(Stack<Fragment> fragStack, CharBuffer chars, ParseContext context) {
        Fragment newFrag = fragStack.pop();
        newFrag = Fragment.questionWrap(newFrag);
        mergeAndPush(fragStack, chars, newFrag, context);
    }

    private void buildEscapeFrag(Stack<Fragment> fragStack, CharBuffer chars, ParseContext context) {
        buildDeafultFrag(fragStack, chars, chars.next(), context);
    }

    private void buildLeftBracketFrag(Stack<Fragment> fragStack, CharBuffer chars, ParseContext context) {
        Fragment newFrag = parseFragment(chars, context);
        mergeAndPush(fragStack, chars, newFrag, context);
    }

    private void buildLeftSquarreBracketFrag(Stack<Fragment> fragStack, CharBuffer chars, ParseContext context) {
        context.enterLeftSquarreBracket();
        Fragment newFrag = parseFragment(chars, context);
        mergeAndPush(fragStack, chars, newFrag, context);
    }

    protected void mergeAndPush(Stack<Fragment> fragStack, CharBuffer chars, Fragment newFrag, ParseContext context) {
        if (fragStack.size() != 0) {
            if (canMerge(chars.lookAhead(1))) {
                Fragment pre = fragStack.pop();
                if (context.canCat()) {
                    newFrag = pre.cat(newFrag);
                } else {
                    newFrag = pre.union(newFrag);
                }

            }
        }
        fragStack.push(newFrag);
    }

    private Fragment merge(Stack<Fragment> fragStack, ParseContext context) {
        Fragment start = fragStack.remove(0);
        if (fragStack.size() == 0) {
            return start;
        }

        for (Fragment fragment : fragStack) {
            if (context.canCat()) {
                start = start.cat(fragment);
            } else {
                start = start.union(fragment);
            }
        }
        return start;
    }

    private static class ParseContext {

        private int leftSquareBrackets = 0;

        public void enterLeftSquarreBracket() {
            leftSquareBrackets++;
        }

        public void leaveLeftSquarreBracket() {
            leftSquareBrackets--;
        }

        public boolean inLeftSquareBracket() {
            return leftSquareBrackets > 0;
        }

        public boolean canCat() {
            return leftSquareBrackets == 0;
        }
    }
}
