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
public class NotMatcherTest {

    @Parameters
    public static List<Object[]> params() {
        return Arrays.asList(new Object[][] { new Object[] { "abcd", new String[] { "abc" } },
                new Object[] { "a.+", new String[] { "bbcdea" } }, new Object[] { "ba?", new String[] { "a", "" } },
                new Object[] { "abc", new String[] { " ", "a", "ab", "cba", "ba" } },
                new Object[] { "a.*", new String[] { "", "b", "." } }, new Object[] { ".", new String[] { "" } },
                new Object[] { "a+", new String[] { "", "b" } }, new Object[] { "b.a?", new String[] { "b" } },
                new Object[] { "^(xy)*(abc)+$", new String[] { "x", "y", "ab", "bc", "ababbc", "xyxabbc", "xyx" } },
                new Object[] { "^(xy)+(abc)+$", new String[] { "abc", "xy", } },
                new Object[] { "(xy)+(abc)+", new String[] { "abc", "xy", } },
                new Object[] { "(xy)+(abc)+", new String[] { "xabc", "xya", } },
                new Object[] { "^ab$", new String[] { "abc", "cab" } },
                new Object[] { "x[abc]y", new String[] { "xabcy", "xy" } },
                new Object[] { "xabc]y", new String[] { "xay", "xby" } },
                new Object[] { "a[x0-9|y]", new String[] { "a1", "ax", "ay", "a9", "a0", "a|" } },

        });
    }

    private String   pattern;
    private String[] testStrs;

    public NotMatcherTest(String pattern, String[] testStrs){
        this.pattern = pattern;
        this.testStrs = testStrs;
    }

    @Test
    public void test() {
        Matcher matcher = new Matcher(pattern);
        for (String testStr : testStrs) {
            Assert.assertFalse(String.format("pattern [  %s  ] is matche string [  %s  ]", pattern, testStr),
                               matcher.match(testStr));
        }
    }
}
