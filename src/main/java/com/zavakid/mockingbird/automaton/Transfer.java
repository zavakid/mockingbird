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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * @author Zava 2013-2-4 下午1:47:44
 * @since 0.0.1
 */
public class Transfer {

    private Multimap<Object, State> innerMap;

    public Transfer(){
        this.innerMap = HashMultimap.create();
    }

    public Collection<State> get(Object object) {
        return innerMap.get(object);
    }

    public void put(Object o, State s) {
        innerMap.put(o, s);
    }

    // TODO, need implement toString for human reader
}
