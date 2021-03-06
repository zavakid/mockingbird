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

/**
 * @author Zava 2013-1-29 下午5:01:59
 * @since 0.0.1
 */
public class Fragment {

    private State start;
    private State end;

    public static Fragment create() {
        return new Fragment();
    }

    public Fragment(){
        start = State.createInitalState();
        end = State.createAcceptState();
    }

    public Fragment addState(Object o, State newState) {
        this.end.addTransfer(o, newState);
        this.end.canalAccept();
        newState.markAccept();
        this.end = newState;
        return this;
    }

    public Fragment cat(Fragment another) {
        this.end.addTransfer(Transfers.EPSILON, another.start);
        this.end.canalAccept();
        this.end = another.end;
        another.start.canalInital();
        return this;
    }

    /**
     * union by operator : |
     * 
     * @param another
     * @return
     */
    public Fragment union(Fragment another) {
        Fragment newFragment = create();
        newFragment.start.addTransfer(Transfers.EPSILON, this.start);
        this.start.canalInital();

        this.end.addTransfer(Transfers.EPSILON, newFragment.end);
        this.end.canalAccept();

        newFragment.start.addTransfer(Transfers.EPSILON, another.start);
        another.start.canalInital();

        another.end.addTransfer(Transfers.EPSILON, newFragment.end);
        another.end.canalAccept();

        return newFragment;
    }

    /**
     * question by operator : ?
     * 
     * @param another
     * @return
     */
    public static Fragment questionWrap(Fragment another) {
        Fragment newFragment = create();
        newFragment.start.addTransfer(Transfers.EPSILON, another.start);
        another.start.canalInital();

        another.end.addTransfer(Transfers.EPSILON, newFragment.end);
        another.end.canalAccept();

        newFragment.start.addTransfer(Transfers.EPSILON, newFragment.end);

        return newFragment;
    }

    /**
     * question by operator : *
     * 
     * @param another
     * @return
     */
    public static Fragment starWrap(Fragment another) {
        Fragment newFragment = create();
        newFragment.start.addTransfer(Transfers.EPSILON, another.start);
        another.start.canalInital();

        another.end.addTransfer(Transfers.EPSILON, newFragment.start);
        another.end.canalAccept();

        newFragment.start.addTransfer(Transfers.EPSILON, newFragment.end);

        return newFragment;
    }

    /**
     * question by operator : +
     * 
     * @param another
     * @return
     */
    public static Fragment plusWrap(Fragment another) {
        Fragment newFragment = create();
        newFragment.start.addTransfer(Transfers.EPSILON, another.start);
        another.start.canalInital();

        another.end.addTransfer(Transfers.EPSILON, newFragment.end);
        another.end.addTransfer(Transfers.EPSILON, newFragment.start);
        another.end.canalAccept();

        return newFragment;
    }

    // =========== setter & getter ===========
    public State getStart() {
        return start;
    }

    public State getEnd() {
        return end;
    }

    public void setStart(State start) {
        this.start = start;
    }

    public void setEnd(State end) {
        this.end = end;
    }

}
