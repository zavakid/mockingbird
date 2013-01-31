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

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author Zava 2013-1-28 下午11:19:24
 * @since 0.0.1
 */
@RunWith(Parameterized.class)
public class MatcherTest {

    @Parameters
    public static List<Object[]> params() {
        return Arrays.asList(new Object[][] {
                new Object[] { "abc", new String[] { "abc" } },
                new Object[] { "a.*", new String[] { "abcde" } },
                new Object[] { ".", new String[] { "f" } },
                new Object[] { ".", new String[] { "f" } },
                new Object[] { ".*", new String[] { "sdfajlakjf", "ab" } },
                new Object[] { "a*", new String[] { "a", "", "aaaaa" } },
                new Object[] { "a+", new String[] { "a" } },
                new Object[] { "a?", new String[] { "", "a" } },
                new Object[] { "b.a?", new String[] { "bx", "bza" } },
                new Object[] { "b*a?", new String[] { "bbbbbbbbbbbb", "bbbbbbbbbbbba" } },
                new Object[] {
                        "(xy)*(abc)+",
                        new String[] { "xyabc", "abc", "abcabc", "xyabc", "xyabcabc", "xyxyxyxyabc", "xyxyxyxyabcabc",
                                "xyxyxyxyabcabcabc" } },

                new Object[] { "(xy)*(abc)?",
                        new String[] { "", "xy", "xyxy", "xyxyxy", "", "xyabc", "xyxyabc", "xyxyxyabc" } },

        });
    }

    private String   pattern;
    private String[] testStrs;

    public MatcherTest(String pattern, String[] testStrs){
        this.pattern = pattern;
        this.testStrs = testStrs;
    }

    @Test
    public void test() {
        MatcherWithFrag matcher = new MatcherWithFrag(pattern);
        for (String testStr : testStrs) {
            Assert.assertTrue(String.format("pattern [ %s ] not matche string [ %s ]", pattern, testStr),
                              matcher.match(testStr));
        }
    }
}
