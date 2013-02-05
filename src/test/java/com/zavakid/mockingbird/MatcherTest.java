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

                new Object[] { "a|b", new String[] { "a", "b", "ac", "bc" } },
                new Object[] { "ab|xy", new String[] { "ab", "xy" } },
                new Object[] { "(ab|xy)zz", new String[] { "abzz", "xyzz" } },
                new Object[] { "(ab?|xy)zz", new String[] { "azz", "abzz", "xyzz" } },
                new Object[] { "(ab|xy)?zz", new String[] { "zz", "abzz", "xyzz" } },
                new Object[] { "(ab|xy)*zz", new String[] { "zz", "abababzz", "xyxyxyzz", "abxyabxyxyabzz" } },
                new Object[] { "ab|cd|de", new String[] { "ab", "cd", "de" } },
                new Object[] { "(x|y)+", new String[] { "x", "yxy", "xxyxyxyxyyyyyxxxxx" } },
                new Object[] { "((a|b)(x|y))?", new String[] { "", "ax", "bx", "ay", "by" } },
                new Object[] { "((a|b)?(x|y))?", new String[] { "", "ax", "bx", "ay", "by", "x", "y" } },
                new Object[] { "a\\|b", new String[] { "a|b" } }, new Object[] { "a\\*", new String[] { "a*" } },
                new Object[] { "h(ello|appy) hippo", new String[] { "hello there, happy hippox" } },
                new Object[] { "abc", new String[] { "abcd" } },
                new Object[] { "^(xy)+(abc)+$", new String[] { "xyabc", "xyxyabcabc", } },
                new Object[] { "^ab$", new String[] { "ab" } }, new Object[] { "^ab(ab)+$", new String[] { "abab" } },
                new Object[] { "x(.+)+x", new String[] { "==xxx==========================" } },
                new Object[] { "to(nite|knite|night)", new String[] { "hot tonic tonight!" } },
                new Object[] { "[abc]", new String[] { "a" } }, new Object[] { "x[abc]y", new String[] { "xby" } },
                new Object[] { "x[abc]+y", new String[] { "xbcaaaabcccby", "xay" } },
                new Object[] { "x[(abc)(h)(ij)]+y", new String[] { "xay" } },
                new Object[] { "xy]xy", new String[] { "xy]xy" } }, new Object[] { "0-9", new String[] { "0-9" } },
                new Object[] { "a[x0-9y]", new String[] { "a1", "ax", "ay", "a9", "a0" } },
                new Object[] { "a[x0-9|y]", new String[] { "a1", "ax", "ay", "a9", "a0", "a|" } },
                new Object[] { "a[^0-9]z", new String[] { "aaz", "a$z" } },
                new Object[] { "a[^0-9a-z]z", new String[] { "aAz", "aZz" } },
                new Object[] { "a[^\\d]z", new String[] { "aAz", "aZz", "a\\z", "adz" } },
                new Object[] { "a\\dz", new String[] { "a1z", "a0z" } }, });
    }

    private String   pattern;
    private String[] testStrs;

    public MatcherTest(String pattern, String[] testStrs){
        this.pattern = pattern;
        this.testStrs = testStrs;
    }

    @Test
    public void test() {
        Matcher matcher = new Matcher(pattern);
        for (String testStr : testStrs) {
            Assert.assertTrue(String.format("pattern [ %s ] not matche string [ %s ]", pattern, testStr),
                              matcher.match(testStr));
        }
    }
}
