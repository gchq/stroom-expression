/*
 * Copyright 2017 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package stroom.dashboard.expression.v1;

import com.caucho.hessian.io.Hessian2Output;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.text.ParseException;

public class TestExpressionParser {
    private final ExpressionParser parser = new ExpressionParser(new FunctionFactory(), new ParamFactory());

    @Test
    public void testBasic() throws ParseException {
        test("${val}");
        test("min(${val})");
        test("max(${val})");
        test("sum(${val})");
        test("min(round(${val}, 4))");
        test("min(roundDay(${val}))");
        test("min(roundMinute(${val}))");
        test("ceiling(${val})");
        test("floor(${val})");
        test("ceiling(floor(min(roundMinute(${val}))))");
        test("ceiling(floor(min(round(${val}))))");
        test("max(${val})-min(${val})");
        test("max(${val})/count()");
        test("round(${val})/(min(${val})+max(${val}))");
        test("concat('this is', 'it')");
        test("concat('it''s a string', 'with a quote')");
        test("'it''s a string'");
        test("stringLength('it''s a string')");
        test("upperCase('it''s a string')");
        test("lowerCase('it''s a string')");
        test("substring('Hello', 0, 1)");
        test("equals(${val}, ${val})");
        test("greaterThan(1, 0)");
        test("lessThan(1, 0)");
        test("greaterThanOrEqualTo(1, 0)");
        test("lessThanOrEqualTo(1, 0)");
        test("1=0");
        test("decode('fred', 'fr.+', 'freda', 'freddy')");
        test("extractHostFromUri('http://www.example.com:1234/this/is/a/path')");
    }

    private void test(final String expression) throws ParseException {
        final Expression exp = createExpression(expression);
        System.out.println(exp.toString());
    }

    @Test
    public void testMin1() throws ParseException {
        final Generator gen = createGenerator("min(${val})");

        gen.set(getVal(300D));
        gen.set(getVal(180D));

        Val out = gen.eval();
        Assert.assertEquals(180D, out.toDouble(), 0);

        gen.set(getVal(500D));

        out = gen.eval();
        Assert.assertEquals(180D, out.toDouble(), 0);

        gen.set(getVal(600D));
        gen.set(getVal(13D));
        gen.set(getVal(99.3D));
        gen.set(getVal(87D));

        out = gen.eval();
        Assert.assertEquals(13D, out.toDouble(), 0);
    }

    private Val[] getVal(final String... str) {
        final Val[] result = new Val[str.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = ValString.create(str[i]);
        }
        return result;
    }

    private Val[] getVal(final double... d) {
        final Val[] result = new Val[d.length];
        for (int i = 0; i < d.length; i++) {
            result[i] = ValDouble.create(d[i]);
        }
        return result;
    }

    @Test
    public void testMinUngrouped2() throws ParseException {
        final Generator gen = createGenerator("min(${val}, 100, 30, 8)");

        gen.set(getVal(300D));

        final Val out = gen.eval();
        Assert.assertEquals(8D, out.toDouble(), 0);
    }

    @Test
    public void testMinGrouped2() throws ParseException {
        final Generator gen = createGenerator("min(min(${val}), 100, 30, 8)");

        gen.set(getVal(300D));
        gen.set(getVal(180D));

        final Val out = gen.eval();
        Assert.assertEquals(8D, out.toDouble(), 0);
    }

    @Test
    public void testMin3() throws ParseException {
        final Generator gen = createGenerator("min(min(${val}), 100, 30, 8, count(), 55)");

        gen.set(getVal(300D));
        gen.set(getVal(180D));

        Val out = gen.eval();
        Assert.assertEquals(2D, out.toDouble(), 0);

        gen.set(getVal(300D));
        gen.set(getVal(180D));

        out = gen.eval();
        Assert.assertEquals(4D, out.toDouble(), 0);
    }

    @Test
    public void testMax1() throws ParseException {
        final Generator gen = createGenerator("max(${val})");

        gen.set(getVal(300D));
        gen.set(getVal(180D));

        Val out = gen.eval();
        Assert.assertEquals(300D, out.toDouble(), 0);

        gen.set(getVal(500D));

        out = gen.eval();
        Assert.assertEquals(500D, out.toDouble(), 0);

        gen.set(getVal(600D));
        gen.set(getVal(13D));
        gen.set(getVal(99.3D));
        gen.set(getVal(87D));

        out = gen.eval();
        Assert.assertEquals(600D, out.toDouble(), 0);
    }

    @Test
    public void testMaxUngrouped2() throws ParseException {
        final Generator gen = createGenerator("max(${val}, 100, 30, 8)");

        gen.set(getVal(10D));

        final Val out = gen.eval();
        Assert.assertEquals(100D, out.toDouble(), 0);
    }

    @Test
    public void testMaxGrouped2() throws ParseException {
        final Generator gen = createGenerator("max(max(${val}), 100, 30, 8)");

        gen.set(getVal(10D));
        gen.set(getVal(40D));

        final Val out = gen.eval();
        Assert.assertEquals(100D, out.toDouble(), 0);
    }

    @Test
    public void testMax3() throws ParseException {
        final Generator gen = createGenerator("max(max(${val}), count())");

        gen.set(getVal(3D));
        gen.set(getVal(2D));

        Val out = gen.eval();
        Assert.assertEquals(3D, out.toDouble(), 0);

        gen.set(getVal(1D));
        gen.set(getVal(1D));

        out = gen.eval();
        Assert.assertEquals(4D, out.toDouble(), 0);
    }

    @Test
    public void testSum() throws ParseException {
        // This is a bad usage of functions as ${val} will produce the last set
        // value when we evaluate the sum. As we are effectively grouping and we
        // don't have any control over the order that cell values are inserted
        // we will end up with indeterminate behaviour.
        final Generator gen = createGenerator("sum(${val}, count())");

        gen.set(getVal(3D));
        gen.set(getVal(2D));

        Val out = gen.eval();
        Assert.assertEquals(4D, out.toDouble(), 0);

        gen.set(getVal(1D));
        gen.set(getVal(1D));

        out = gen.eval();
        Assert.assertEquals(5D, out.toDouble(), 0);
    }

    @Test
    public void testSumOfSum() throws ParseException {
        final Generator gen = createGenerator("sum(sum(${val}), count())");

        gen.set(getVal(3D));
        gen.set(getVal(2D));

        Val out = gen.eval();
        Assert.assertEquals(7D, out.toDouble(), 0);

        gen.set(getVal(1D));
        gen.set(getVal(1D));

        out = gen.eval();
        Assert.assertEquals(11D, out.toDouble(), 0);
    }

    @Test
    public void testAverageUngrouped() throws ParseException {
        // This is a bad usage of functions as ${val} will produce the last set
        // value when we evaluate the sum. As we are effectively grouping and we
        // don't have any control over the order that cell values are inserted
        // we will end up with indeterminate behaviour.
        final Generator gen = createGenerator("average(${val}, count())");

        gen.set(getVal(3D));
        gen.set(getVal(4D));

        Val out = gen.eval();
        Assert.assertEquals(3D, out.toDouble(), 0);

        gen.set(getVal(1D));
        gen.set(getVal(8D));

        out = gen.eval();
        Assert.assertEquals(6D, out.toDouble(), 0);
    }

    @Test
    public void testAverageGrouped() throws ParseException {
        final Generator gen = createGenerator("average(${val})");

        gen.set(getVal(3D));
        gen.set(getVal(4D));

        Val out = gen.eval();
        Assert.assertEquals(3.5D, out.toDouble(), 0);

        gen.set(getVal(1D));
        gen.set(getVal(8D));

        out = gen.eval();
        Assert.assertEquals(4D, out.toDouble(), 0);
    }

    @Test
    public void testMatch1() throws ParseException {
        final Generator gen = createGenerator("match('this', 'this')");

        final Val out = gen.eval();
        Assert.assertTrue(out.toBoolean());
    }

    @Test
    public void testMatch2() throws ParseException {
        final Generator gen = createGenerator("match('this', 'that')");

        final Val out = gen.eval();
        Assert.assertFalse(out.toBoolean());
    }

    @Test
    public void testMatch3() throws ParseException {
        final Generator gen = createGenerator("match(${val}, 'this')");

        gen.set(getVal("this"));

        final Val out = gen.eval();
        Assert.assertTrue(out.toBoolean());
    }

    @Test
    public void testMatch4() throws ParseException {
        final Generator gen = createGenerator("match(${val}, 'that')");

        gen.set(getVal("this"));

        final Val out = gen.eval();
        Assert.assertFalse(out.toBoolean());
    }

    @Test
    public void testTrue() throws ParseException {
        final Generator gen = createGenerator("true()");

        final Val out = gen.eval();
        Assert.assertTrue(out.toBoolean());
    }

    @Test
    public void testFalse() throws ParseException {
        final Generator gen = createGenerator("false()");

        final Val out = gen.eval();
        Assert.assertFalse(out.toBoolean());
    }

    @Test
    public void testNull() throws ParseException {
        final Generator gen = createGenerator("null()");

        final Val out = gen.eval();
        Assert.assertTrue(out instanceof ValNull);
    }

    @Test
    public void testErr() throws ParseException {
        final Generator gen = createGenerator("err()");

        final Val out = gen.eval();
        Assert.assertTrue(out instanceof ValErr);
    }

    @Test
    public void testNotTrue() throws ParseException {
        final Generator gen = createGenerator("not(true())");

        final Val out = gen.eval();
        Assert.assertFalse(out.toBoolean());
    }

    @Test
    public void testNotFalse() throws ParseException {
        final Generator gen = createGenerator("not(false())");

        final Val out = gen.eval();
        Assert.assertTrue(out.toBoolean());
    }

    @Test
    public void testIf1() throws ParseException {
        final Generator gen = createGenerator("if(true(), 'this', 'that')");

        final Val out = gen.eval();
        Assert.assertEquals("this", out.toString());
    }

    @Test
    public void testIf2() throws ParseException {
        final Generator gen = createGenerator("if(false(), 'this', 'that')");

        final Val out = gen.eval();
        Assert.assertEquals("that", out.toString());
    }

    @Test
    public void testIf3() throws ParseException {
        final Generator gen = createGenerator("if(${val}, 'this', 'that')");

        gen.set(getVal("true"));

        final Val out = gen.eval();
        Assert.assertEquals("this", out.toString());
    }

    @Test
    public void testIf4() throws ParseException {
        final Generator gen = createGenerator("if(${val}, 'this', 'that')");

        gen.set(getVal("false"));

        final Val out = gen.eval();
        Assert.assertEquals("that", out.toString());
    }

    @Test
    public void testIf5() throws ParseException {
        final Generator gen = createGenerator("if(match(${val}, 'foo'), 'this', 'that')");

        gen.set(getVal("foo"));

        final Val out = gen.eval();
        Assert.assertEquals("this", out.toString());
    }

    @Test
    public void testIf6() throws ParseException {
        final Generator gen = createGenerator("if(match(${val}, 'foo'), 'this', 'that')");

        gen.set(getVal("bar"));

        final Val out = gen.eval();
        Assert.assertEquals("that", out.toString());
    }

    @Test
    public void testNotIf() throws ParseException {
        final Generator gen = createGenerator("if(not(${val}), 'this', 'that')");

        gen.set(getVal("false"));

        final Val out = gen.eval();
        Assert.assertEquals("this", out.toString());
    }

    @Test
    public void testIf_nullHandling() throws ParseException {
        final Generator gen = createGenerator("if(${val}=null(), true(), false())");

        gen.set(new Val[]{ValNull.INSTANCE});

        final Val out = gen.eval();
        Assert.assertEquals(ValBoolean.TRUE, out);
    }

    @Test
    public void testReplace1() throws ParseException {
        final Generator gen = createGenerator("replace('this', 'is', 'at')");

        gen.set(getVal(3D));

        final Val out = gen.eval();
        Assert.assertEquals("that", out.toString());
    }

    @Test
    public void testReplace2() throws ParseException {
        final Generator gen = createGenerator("replace(${val}, 'is', 'at')");

        gen.set(getVal("this"));

        final Val out = gen.eval();
        Assert.assertEquals("that", out.toString());
    }

    @Test
    public void testConcat1() throws ParseException {
        final Generator gen = createGenerator("concat('this', ' is ', 'it')");

        gen.set(getVal(3D));

        final Val out = gen.eval();
        Assert.assertEquals("this is it", out.toString());
    }

    @Test
    public void testConcat2() throws ParseException {
        final Generator gen = createGenerator("concat(${val}, ' is ', 'it')");

        gen.set(getVal("this"));

        final Val out = gen.eval();
        Assert.assertEquals("this is it", out.toString());
    }

    @Test
    public void testStringLength1() throws ParseException {
        final Generator gen = createGenerator("stringLength(${val})");

        gen.set(getVal("this"));

        final Val out = gen.eval();
        Assert.assertEquals(4D, out.toDouble(), 0);
    }

    @Test
    public void testSubstring1() throws ParseException {
        final Generator gen = createGenerator("substring(${val}, 1, 2)");

        gen.set(getVal("this"));

        final Val out = gen.eval();
        Assert.assertEquals("h", out.toString());
    }

    @Test
    public void testSubstring3() throws ParseException {
        final Generator gen = createGenerator("substring(${val}, 2, 99)");

        gen.set(getVal("his"));

        final Val out = gen.eval();
        Assert.assertEquals("s", out.toString());
    }

    @Test
    public void testSubstring4() throws ParseException {
        final Generator gen = createGenerator("substring(${val}, 1+1, 99-1)");

        gen.set(getVal("his"));

        final Val out = gen.eval();
        Assert.assertEquals("s", out.toString());
    }

    @Test
    public void testSubstring5() throws ParseException {
        final Generator gen = createGenerator("substring(${val}, 2+5, 99-1)");

        gen.set(getVal("his"));

        final Val out = gen.eval();
        Assert.assertEquals("", out.toString());
    }

    @Test
    public void testSubstringBefore1() throws ParseException {
        final Generator gen = createGenerator("substringBefore(${val}, '-')");

        gen.set(getVal("aa-bb"));

        final Val out = gen.eval();
        Assert.assertEquals("aa", out.toString());
    }

    @Test
    public void testSubstringBefore2() throws ParseException {
        final Generator gen = createGenerator("substringBefore(${val}, 'a')");

        gen.set(getVal("aa-bb"));

        final Val out = gen.eval();
        Assert.assertEquals("", out.toString());
    }

    @Test
    public void testSubstringBefore3() throws ParseException {
        final Generator gen = createGenerator("substringBefore(${val}, 'b')");

        gen.set(getVal("aa-bb"));

        final Val out = gen.eval();
        Assert.assertEquals("aa-", out.toString());
    }

    @Test
    public void testSubstringBefore4() throws ParseException {
        final Generator gen = createGenerator("substringBefore(${val}, 'q')");

        gen.set(getVal("aa-bb"));

        final Val out = gen.eval();
        Assert.assertEquals("", out.toString());
    }

    @Test
    public void testSubstringAfter1() throws ParseException {
        final Generator gen = createGenerator("substringAfter(${val}, '-')");

        gen.set(getVal("aa-bb"));

        final Val out = gen.eval();
        Assert.assertEquals("bb", out.toString());
    }

    @Test
    public void testSubstringAfter2() throws ParseException {
        final Generator gen = createGenerator("substringAfter(${val}, 'a')");

        gen.set(getVal("aa-bb"));

        final Val out = gen.eval();
        Assert.assertEquals("a-bb", out.toString());
    }

    @Test
    public void testSubstringAfter3() throws ParseException {
        final Generator gen = createGenerator("substringAfter(${val}, 'b')");

        gen.set(getVal("aa-bb"));

        final Val out = gen.eval();
        Assert.assertEquals("b", out.toString());
    }

    @Test
    public void testSubstringAfter4() throws ParseException {
        final Generator gen = createGenerator("substringAfter(${val}, 'q')");

        gen.set(getVal("aa-bb"));

        final Val out = gen.eval();
        Assert.assertEquals("", out.toString());
    }

    @Test
    public void testIndexOf() throws ParseException {
        final Generator gen = createGenerator("indexOf(${val}, '-')");

        gen.set(getVal("aa-bb"));

        final Val out = gen.eval();
        Assert.assertEquals(2, out.toInteger().intValue());
    }

    @Test
    public void testIndexOf1() throws ParseException {
        final Generator gen = createGenerator("substring(${val}, indexOf(${val}, '-'), stringLength(${val}))");

        gen.set(getVal("aa-bb"));

        final Val out = gen.eval();
        Assert.assertEquals("-bb", out.toString());
    }

    @Test
    public void testIndexOf2() throws ParseException {
        final Generator gen = createGenerator("substring(${val}, indexOf(${val}, 'a'), stringLength(${val}))");

        gen.set(getVal("aa-bb"));

        final Val out = gen.eval();
        Assert.assertEquals("aa-bb", out.toString());
    }

    @Test
    public void testIndexOf3() throws ParseException {
        final Generator gen = createGenerator("substring(${val}, indexOf(${val}, 'b'), stringLength(${val}))");

        gen.set(getVal("aa-bb"));

        final Val out = gen.eval();
        Assert.assertEquals("bb", out.toString());
    }

    @Test
    public void testIndexOf4() throws ParseException {
        final Generator gen = createGenerator("substring(${val}, indexOf(${val}, 'q'), stringLength(${val}))");

        gen.set(getVal("aa-bb"));

        final Val out = gen.eval();
        Assert.assertEquals("", out.toString());
    }

    @Test
    public void testLastIndexOf1() throws ParseException {
        final Generator gen = createGenerator("substring(${val}, lastIndexOf(${val}, '-'), stringLength(${val}))");

        gen.set(getVal("aa-bb"));

        final Val out = gen.eval();
        Assert.assertEquals("-bb", out.toString());
    }

    @Test
    public void testLastIndexOf2() throws ParseException {
        final Generator gen = createGenerator("substring(${val}, lastIndexOf(${val}, 'a'), stringLength(${val}))");

        gen.set(getVal("aa-bb"));

        final Val out = gen.eval();
        Assert.assertEquals("a-bb", out.toString());
    }

    @Test
    public void testLastIndexOf3() throws ParseException {
        final Generator gen = createGenerator("substring(${val}, lastIndexOf(${val}, 'b'), stringLength(${val}))");

        gen.set(getVal("aa-bb"));

        final Val out = gen.eval();
        Assert.assertEquals("b", out.toString());
    }

    @Test
    public void testLastIndexOf4() throws ParseException {
        final Generator gen = createGenerator("substring(${val}, lastIndexOf(${val}, 'q'), stringLength(${val}))");

        gen.set(getVal("aa-bb"));

        final Val out = gen.eval();
        Assert.assertEquals("", out.toString());
    }

    @Test
    public void testDecode1() throws ParseException {
        final Generator gen = createGenerator("decode(${val}, 'hullo', 'hello', 'goodbye')");

        gen.set(getVal("hullo"));

        final Val out = gen.eval();
        Assert.assertEquals("hello", out.toString());
    }

    @Test
    public void testDecode2() throws ParseException {
        final Generator gen = createGenerator("decode(${val}, 'h.+o', 'hello', 'goodbye')");

        gen.set(getVal("hullo"));

        final Val out = gen.eval();
        Assert.assertEquals("hello", out.toString());
    }

    @Test
    public void testInclude1() throws ParseException {
        final Generator gen = createGenerator("include(${val}, 'this', 'that')");
        gen.set(getVal("this"));
        final Val out = gen.eval();
        Assert.assertEquals("this", out.toString());
    }

    @Test
    public void testInclude2() throws ParseException {
        final Generator gen = createGenerator("include(${val}, 'this', 'that')");
        gen.set(getVal("that"));
        final Val out = gen.eval();
        Assert.assertEquals("that", out.toString());
    }

    @Test
    public void testInclude3() throws ParseException {
        final Generator gen = createGenerator("include(${val}, 'this', 'that')");
        gen.set(getVal("other"));
        final Val out = gen.eval();
        Assert.assertNull(out.toString());
    }

    @Test
    public void testExclude1() throws ParseException {
        final Generator gen = createGenerator("exclude(${val}, 'this', 'that')");
        gen.set(getVal("this"));
        final Val out = gen.eval();
        Assert.assertNull(out.toString());
    }

    @Test
    public void testExclude2() throws ParseException {
        final Generator gen = createGenerator("exclude(${val}, 'this', 'that')");
        gen.set(getVal("that"));
        final Val out = gen.eval();
        Assert.assertNull(out.toString());
    }

    @Test
    public void testExclude3() throws ParseException {
        final Generator gen = createGenerator("exclude(${val}, 'this', 'that')");
        gen.set(getVal("other"));
        final Val out = gen.eval();
        Assert.assertEquals("other", out.toString());
    }

    @Test
    public void testEquals1() throws ParseException {
        final Generator gen = createGenerator("equals(${val}, 'plop')");

        gen.set(getVal("plop"));

        final Val out = gen.eval();
        Assert.assertEquals("true", out.toString());
    }

    @Test
    public void testEquals2() throws ParseException {
        final Generator gen = createGenerator("equals(${val}, ${val})");

        gen.set(getVal("plop"));

        final Val out = gen.eval();
        Assert.assertEquals("true", out.toString());
    }

    @Test
    public void testEquals3() throws ParseException {
        final Generator gen = createGenerator("equals(${val}, 'plip')");

        gen.set(getVal("plop"));

        final Val out = gen.eval();
        Assert.assertEquals("false", out.toString());
    }

    @Test
    public void testEquals4() throws ParseException {
        final Generator gen = createGenerator2("equals(${val1}, ${val2})");

        gen.set(getVal("plop", "plip"));

        final Val out = gen.eval();
        Assert.assertEquals("false", out.toString());
    }

    @Test
    public void testEquals5() throws ParseException {
        final Generator gen = createGenerator2("equals(${val1}, ${val2})");

        gen.set(getVal("plop", "plop"));

        final Val out = gen.eval();
        Assert.assertEquals("true", out.toString());
    }

    @Test
    public void testEquals6() throws ParseException {
        final Generator gen = createGenerator2("${val1}=${val2}");

        gen.set(getVal("plop", "plop"));

        final Val out = gen.eval();
        Assert.assertEquals("true", out.toString());
    }

    @Test
    public void testLessThan1() throws ParseException {
        final Generator gen = createGenerator2("lessThan(1, 0)");

        final Val out = gen.eval();
        Assert.assertEquals("false", out.toString());
    }

    @Test
    public void testLessThan2() throws ParseException {
        final Generator gen = createGenerator2("lessThan(1, 1)");

        final Val out = gen.eval();
        Assert.assertEquals("false", out.toString());
    }

    @Test
    public void testLessThan3() throws ParseException {
        final Generator gen = createGenerator2("lessThan(${val1}, ${val2})");

        gen.set(getVal(1D, 2D));

        final Val out = gen.eval();
        Assert.assertEquals("true", out.toString());
    }

    @Test
    public void testLessThan4() throws ParseException {
        final Generator gen = createGenerator2("lessThan(${val1}, ${val2})");

        gen.set(getVal("fred", "fred"));

        final Val out = gen.eval();
        Assert.assertEquals("false", out.toString());
    }

    @Test
    public void testLessThan5() throws ParseException {
        final Generator gen = createGenerator2("lessThan(${val1}, ${val2})");

        gen.set(getVal("fred", "fred1"));

        final Val out = gen.eval();
        Assert.assertEquals("true", out.toString());
    }

    @Test
    public void testLessThan6() throws ParseException {
        final Generator gen = createGenerator2("lessThan(${val1}, ${val2})");

        gen.set(getVal("fred1", "fred"));

        final Val out = gen.eval();
        Assert.assertEquals("false", out.toString());
    }

    @Test
    public void testLessThanOrEqualTo1() throws ParseException {
        final Generator gen = createGenerator2("lessThanOrEqualTo(1, 0)");

        final Val out = gen.eval();
        Assert.assertEquals("false", out.toString());
    }

    @Test
    public void testLessThanOrEqualTo2() throws ParseException {
        final Generator gen = createGenerator2("lessThanOrEqualTo(1, 1)");

        final Val out = gen.eval();
        Assert.assertEquals("true", out.toString());
    }

    @Test
    public void testLessThanOrEqualTo3() throws ParseException {
        final Generator gen = createGenerator2("lessThanOrEqualTo(${val1}, ${val2})");

        gen.set(getVal(1D, 2D));

        final Val out = gen.eval();
        Assert.assertEquals("true", out.toString());
    }

    @Test
    public void testLessThanOrEqualTo3_mk2() throws ParseException {
        final Generator gen = createGenerator2("(${val1}<=${val2})");

        gen.set(getVal(1D, 2D));

        final Val out = gen.eval();
        Assert.assertEquals("true", out.toString());
    }

    @Test
    public void testLessThanOrEqualTo4() throws ParseException {
        final Generator gen = createGenerator2("lessThanOrEqualTo(${val1}, ${val2})");

        gen.set(getVal("fred", "fred"));

        final Val out = gen.eval();
        Assert.assertEquals("true", out.toString());
    }

    @Test
    public void testLessThanOrEqualTo5() throws ParseException {
        final Generator gen = createGenerator2("lessThanOrEqualTo(${val1}, ${val2})");

        gen.set(getVal("fred", "fred1"));

        final Val out = gen.eval();
        Assert.assertEquals("true", out.toString());
    }

    @Test
    public void testLessThanOrEqualTo6() throws ParseException {
        final Generator gen = createGenerator2("lessThanOrEqualTo(${val1}, ${val2})");

        gen.set(getVal("fred1", "fred"));

        final Val out = gen.eval();
        Assert.assertEquals("false", out.toString());
    }

    @Test
    public void testGreaterThanOrEqualTo1() throws ParseException {
        final Generator gen = createGenerator2("greaterThanOrEqualTo(${val1}, ${val2})");

        gen.set(getVal(2D, 1D));

        final Val out = gen.eval();
        Assert.assertEquals("true", out.toString());
    }

    @Test
    public void testGreaterThanOrEqualTo1_mk2() throws ParseException {
        final Generator gen = createGenerator2("(${val1}>=${val2})");

        gen.set(getVal(2D, 1D));

        final Val out = gen.eval();
        Assert.assertEquals("true", out.toString());
    }

    @Test
    public void testSubstring2() throws ParseException {
        final Generator gen = createGenerator("substring(${val}, 0, 99)");

        gen.set(getVal("this"));

        final Val out = gen.eval();
        Assert.assertEquals("this", out.toString());
    }

    @Test
    public void testHash1() throws ParseException {
        final Generator gen = createGenerator("hash(${val})");

        gen.set(getVal("test"));

        final Val out = gen.eval();
        Assert.assertEquals("9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08", out.toString());
    }

    @Test
    public void testHash2() throws ParseException {
        final Generator gen = createGenerator("hash(${val}, 'SHA-512')");

        gen.set(getVal("test"));

        final Val out = gen.eval();
        Assert.assertEquals("ee26b0dd4af7e749aa1a8ee3c10ae9923f618980772e473f8819a5d4940e0db27ac185f8a0e1d5f84f88bc887fd67b143732c304cc5fa9ad8e6f57f50028a8ff", out.toString());
    }

    @Test
    public void testHash3() throws ParseException {
        final Generator gen = createGenerator("hash(${val}, 'SHA-512', 'mysalt')");

        gen.set(getVal("test"));

        final Val out = gen.eval();
        Assert.assertEquals("af2910d4d8acf3fcf9683d3ca4425327cb1b4b48bc690f566e27b0e0144c17af82066cf6af14d3a30312ed9df671e0e24b1c66ed3973d1a7836899d75c4d6bb8", out.toString());
    }

    @Test
    public void testCount() throws ParseException {
        final Generator gen = createGenerator("count()");

        gen.set(getVal(122D));
        gen.set(getVal(133D));

        Val out = gen.eval();
        Assert.assertEquals(2D, out.toDouble(), 0);

        gen.set(getVal(11D));
        gen.set(getVal(122D));

        out = gen.eval();
        Assert.assertEquals(4D, out.toDouble(), 0);
    }

    @Test
    public void testCountUnique() throws ParseException {
        final Generator gen = createGenerator("countUnique(${val})");

        gen.set(getVal(122D));
        gen.set(getVal(133D));

        Val out = gen.eval();
        Assert.assertEquals(2D, out.toDouble(), 0);

        gen.set(getVal(11D));
        gen.set(getVal(122D));

        out = gen.eval();
        Assert.assertEquals(3D, out.toDouble(), 0);
    }

    @Test
    public void testCountUniqueStaticValue() throws ParseException {
        final Generator gen = createGenerator("countUnique('test')");

        gen.set(getVal(122D));
        gen.set(getVal(133D));

        Val out = gen.eval();
        Assert.assertEquals(1D, out.toDouble(), 0);

        gen.set(getVal(11D));
        gen.set(getVal(122D));

        out = gen.eval();
        Assert.assertEquals(1D, out.toDouble(), 0);
    }

    @Test
    public void testAdd1() throws ParseException {
        final Generator gen = createGenerator("3+4");

        gen.set(getVal(1D));

        final Val out = gen.eval();
        Assert.assertEquals(7D, out.toDouble(), 0);
    }

    @Test
    public void testAdd2() throws ParseException {
        final Generator gen = createGenerator("3+4+5");

        gen.set(getVal(1D));

        final Val out = gen.eval();
        Assert.assertEquals(12D, out.toDouble(), 0);
    }

    @Test
    public void testAdd3() throws ParseException {
        final Generator gen = createGenerator("2+count()");

        gen.set(getVal(1D));
        gen.set(getVal(1D));

        Val out = gen.eval();
        Assert.assertEquals(4D, out.toDouble(), 0);

        gen.set(getVal(1D));
        gen.set(getVal(1D));

        out = gen.eval();
        Assert.assertEquals(6D, out.toDouble(), 0);
    }

    @Test
    public void testSubtract1() throws ParseException {
        final Generator gen = createGenerator("3-4");

        gen.set(getVal(1D));

        final Val out = gen.eval();
        Assert.assertEquals(-1D, out.toDouble(), 0);
    }

    @Test
    public void testSubtract2() throws ParseException {
        final Generator gen = createGenerator("2-count()");

        gen.set(getVal(1D));
        gen.set(getVal(1D));

        Val out = gen.eval();
        Assert.assertEquals(0D, out.toDouble(), 0);

        gen.set(getVal(1D));
        gen.set(getVal(1D));

        out = gen.eval();
        Assert.assertEquals(-2D, out.toDouble(), 0);
    }

    @Test
    public void testMultiply1() throws ParseException {
        final Generator gen = createGenerator("3*4");

        gen.set(getVal(1D));

        final Val out = gen.eval();
        Assert.assertEquals(12D, out.toDouble(), 0);
    }

    @Test
    public void testMultiply2() throws ParseException {
        final Generator gen = createGenerator("2*count()");

        gen.set(getVal(1D));
        gen.set(getVal(1D));

        Val out = gen.eval();
        Assert.assertEquals(4D, out.toDouble(), 0);

        gen.set(getVal(1D));
        gen.set(getVal(1D));

        out = gen.eval();
        Assert.assertEquals(8D, out.toDouble(), 0);
    }

    @Test
    public void testDivide1() throws ParseException {
        final Generator gen = createGenerator("8/4");

        gen.set(getVal(1D));

        final Val out = gen.eval();
        Assert.assertEquals(2D, out.toDouble(), 0);
    }

    @Test
    public void testDivide2() throws ParseException {
        final Generator gen = createGenerator("8/count()");

        gen.set(getVal(1D));
        gen.set(getVal(1D));

        Val out = gen.eval();
        Assert.assertEquals(4D, out.toDouble(), 0);

        gen.set(getVal(1D));
        gen.set(getVal(1D));

        out = gen.eval();
        Assert.assertEquals(2D, out.toDouble(), 0);
    }

    @Test
    public void testFloorNum1() throws ParseException {
        final Generator gen = createGenerator("floor(8.4234)");

        gen.set(getVal(1D));

        final Val out = gen.eval();
        Assert.assertEquals(8D, out.toDouble(), 0);
    }

    @Test
    public void testFloorNum2() throws ParseException {
        final Generator gen = createGenerator("floor(8.5234)");

        gen.set(getVal(1D));

        final Val out = gen.eval();
        Assert.assertEquals(8D, out.toDouble(), 0);
    }

    @Test
    public void testFloorNum3() throws ParseException {
        final Generator gen = createGenerator("floor(${val})");

        gen.set(getVal(1.34D));

        final Val out = gen.eval();
        Assert.assertEquals(1D, out.toDouble(), 0);
    }

    @Test
    public void testFloorNum4() throws ParseException {
        final Generator gen = createGenerator("floor(${val}+count())");

        gen.set(getVal(1.34D));
        gen.set(getVal(1.8655D));

        final Val out = gen.eval();
        Assert.assertEquals(3D, out.toDouble(), 0);
    }

    @Test
    public void testFloorNum5() throws ParseException {
        final Generator gen = createGenerator("floor(${val}+count(), 1)");

        gen.set(getVal(1.34D));
        gen.set(getVal(1.8655D));

        final Val out = gen.eval();
        Assert.assertEquals(3.8D, out.toDouble(), 0);
    }

    @Test
    public void testFloorNum6() throws ParseException {
        final Generator gen = createGenerator("floor(${val}+count(), 2)");

        gen.set(getVal(1.34D));
        gen.set(getVal(1.8655D));

        final Val out = gen.eval();
        Assert.assertEquals(3.86D, out.toDouble(), 0);
    }

    @Test
    public void testCeilNum1() throws ParseException {
        final Generator gen = createGenerator("ceiling(8.4234)");

        gen.set(getVal(1D));

        final Val out = gen.eval();
        Assert.assertEquals(9D, out.toDouble(), 0);
    }

    @Test
    public void testCeilNum2() throws ParseException {
        final Generator gen = createGenerator("ceiling(8.5234)");

        gen.set(getVal(1D));

        final Val out = gen.eval();
        Assert.assertEquals(9D, out.toDouble(), 0);
    }

    @Test
    public void testCeilNum3() throws ParseException {
        final Generator gen = createGenerator("ceiling(${val})");

        gen.set(getVal(1.34D));

        final Val out = gen.eval();
        Assert.assertEquals(2D, out.toDouble(), 0);
    }

    @Test
    public void testCeilNum4() throws ParseException {
        final Generator gen = createGenerator("ceiling(${val}+count())");

        gen.set(getVal(1.34D));
        gen.set(getVal(1.8655D));

        final Val out = gen.eval();
        Assert.assertEquals(4D, out.toDouble(), 0);
    }

    @Test
    public void testCeilNum5() throws ParseException {
        final Generator gen = createGenerator("ceiling(${val}+count(), 1)");

        gen.set(getVal(1.34D));
        gen.set(getVal(1.8655D));

        final Val out = gen.eval();
        Assert.assertEquals(3.9D, out.toDouble(), 0);
    }

    @Test
    public void testCeilNum6() throws ParseException {
        final Generator gen = createGenerator("ceiling(${val}+count(), 2)");

        gen.set(getVal(1.34D));
        gen.set(getVal(1.8655D));

        final Val out = gen.eval();
        Assert.assertEquals(3.87D, out.toDouble(), 0);
    }

    @Test
    public void testRoundNum1() throws ParseException {
        final Generator gen = createGenerator("round(8.4234)");

        gen.set(getVal(1D));

        final Val out = gen.eval();
        Assert.assertEquals(8D, out.toDouble(), 0);
    }

    @Test
    public void testRoundNum2() throws ParseException {
        final Generator gen = createGenerator("round(8.5234)");

        gen.set(getVal(1D));

        final Val out = gen.eval();
        Assert.assertEquals(9D, out.toDouble(), 0);
    }

    @Test
    public void testRoundNum3() throws ParseException {
        final Generator gen = createGenerator("round(${val})");

        gen.set(getVal(1.34D));

        final Val out = gen.eval();
        Assert.assertEquals(1D, out.toDouble(), 0);
    }

    @Test
    public void testRoundNum4() throws ParseException {
        final Generator gen = createGenerator("round(${val}+count())");

        gen.set(getVal(1.34D));
        gen.set(getVal(1.8655D));

        final Val out = gen.eval();
        Assert.assertEquals(4D, out.toDouble(), 0);
    }

    @Test
    public void testRoundNum5() throws ParseException {
        final Generator gen = createGenerator("round(${val}+count(), 1)");

        gen.set(getVal(1.34D));
        gen.set(getVal(1.8655D));

        final Val out = gen.eval();
        Assert.assertEquals(3.9D, out.toDouble(), 0);
    }

    @Test
    public void testRoundNum6() throws ParseException {
        final Generator gen = createGenerator("round(${val}+count(), 2)");

        gen.set(getVal(1.34D));
        gen.set(getVal(1.8655D));

        final Val out = gen.eval();
        Assert.assertEquals(3.87D, out.toDouble(), 0);
    }

    @Test
    public void testTime() throws ParseException {
        testTime("floorSecond", "2014-02-22T12:12:12.888Z", "2014-02-22T12:12:12.000Z");
        testTime("floorMinute", "2014-02-22T12:12:12.888Z", "2014-02-22T12:12:00.000Z");
        testTime("floorHour", "2014-02-22T12:12:12.888Z", "2014-02-22T12:00:00.000Z");
        testTime("floorDay", "2014-02-22T12:12:12.888Z", "2014-02-22T00:00:00.000Z");
        testTime("floorMonth", "2014-02-22T12:12:12.888Z", "2014-02-01T00:00:00.000Z");
        testTime("floorYear", "2014-02-22T12:12:12.888Z", "2014-01-01T00:00:00.000Z");

        testTime("ceilingSecond", "2014-02-22T12:12:12.888Z", "2014-02-22T12:12:13.000Z");
        testTime("ceilingMinute", "2014-02-22T12:12:12.888Z", "2014-02-22T12:13:00.000Z");
        testTime("ceilingHour", "2014-02-22T12:12:12.888Z", "2014-02-22T13:00:00.000Z");
        testTime("ceilingDay", "2014-02-22T12:12:12.888Z", "2014-02-23T00:00:00.000Z");
        testTime("ceilingMonth", "2014-02-22T12:12:12.888Z", "2014-03-01T00:00:00.000Z");
        testTime("ceilingYear", "2014-02-22T12:12:12.888Z", "2015-01-01T00:00:00.000Z");

        testTime("roundSecond", "2014-02-22T12:12:12.888Z", "2014-02-22T12:12:13.000Z");
        testTime("roundMinute", "2014-02-22T12:12:12.888Z", "2014-02-22T12:12:00.000Z");
        testTime("roundHour", "2014-02-22T12:12:12.888Z", "2014-02-22T12:00:00.000Z");
        testTime("roundDay", "2014-02-22T12:12:12.888Z", "2014-02-23T00:00:00.000Z");
        testTime("roundMonth", "2014-02-22T12:12:12.888Z", "2014-03-01T00:00:00.000Z");
        testTime("roundYear", "2014-02-22T12:12:12.888Z", "2014-01-01T00:00:00.000Z");
    }

    private void testTime(final String function, final String in, final String expected) throws ParseException {
        final double expectedMs = DateUtil.parseNormalDateTimeString(expected);
        final String expression = function + "(${val})";
        final Generator gen = createGenerator(expression);

        gen.set(getVal(in));
        final Val out = gen.eval();
        Assert.assertEquals(expectedMs, out.toDouble(), 0);
    }

    @Test
    public void testBODMAS1() throws ParseException {
        final Generator gen = createGenerator("4+4/2+2");

        final Val out = gen.eval();

        // Non BODMAS would evaluate as 6 or even 4 - BODMAS should be 8.
        Assert.assertEquals(8D, out.toDouble(), 0);
    }

    @Test
    public void testBODMAS2() throws ParseException {
        final Generator gen = createGenerator("(4+4)/2+2");

        final Val out = gen.eval();

        // Non BODMAS would evaluate as 6 or even 4 - BODMAS should be 6.
        Assert.assertEquals(6D, out.toDouble(), 0);
    }

    @Test
    public void testBODMAS3() throws ParseException {
        final Generator gen = createGenerator("(4+4)/(2+2)");

        final Val out = gen.eval();

        // Non BODMAS would evaluate as 6 or even 4 - BODMAS should be 2.
        Assert.assertEquals(2D, out.toDouble(), 0);
    }

    @Test
    public void testBODMAS4() throws ParseException {
        final Generator gen = createGenerator("4+4/2+2*3");

        final Val out = gen.eval();

        // Non BODMAS would evaluate as 18 - BODMAS should be 12.
        Assert.assertEquals(12D, out.toDouble(), 0);
    }

    @Test
    public void testExtractAuthorityFromUri() throws ParseException {
        final Generator gen = createGenerator("extractAuthorityFromUri(${val})");

        gen.set(getVal("http://www.example.com:1234/this/is/a/path"));
        Val out = gen.eval();
        Assert.assertEquals("www.example.com:1234", out.toString());
    }

    @Test
    public void testExtractFragmentFromUri() throws ParseException {
        final Generator gen = createGenerator("extractFragmentFromUri(${val})");

        gen.set(getVal("http://www.example.com:1234/this/is/a/path#frag"));
        Val out = gen.eval();
        Assert.assertEquals("frag", out.toString());
    }

    @Test
    public void testExtractHostFromUri() throws ParseException {
        final Generator gen = createGenerator("extractHostFromUri(${val})");

        gen.set(getVal("http://www.example.com:1234/this/is/a/path"));
        Val out = gen.eval();
        Assert.assertEquals("www.example.com", out.toString());
    }

    @Test
    public void testExtractPathFromUri() throws ParseException {
        final Generator gen = createGenerator("extractPathFromUri(${val})");

        gen.set(getVal("http://www.example.com:1234/this/is/a/path"));
        Val out = gen.eval();
        Assert.assertEquals("/this/is/a/path", out.toString());
    }

    @Test
    public void testExtractPortFromUri() throws ParseException {
        final Generator gen = createGenerator("extractPortFromUri(${val})");

        gen.set(getVal("http://www.example.com:1234/this/is/a/path"));
        Val out = gen.eval();
        Assert.assertEquals("1234", out.toString());
    }

    @Test
    public void testExtractQueryFromUri() throws ParseException {
        final Generator gen = createGenerator("extractQueryFromUri(${val})");

        gen.set(getVal("http://www.example.com:1234/this/is/a/path?this=that&foo=bar"));
        Val out = gen.eval();
        Assert.assertEquals("this=that&foo=bar", out.toString());
    }

    @Test
    public void testExtractSchemeFromUri() throws ParseException {
        final Generator gen = createGenerator("extractSchemeFromUri(${val})");

        gen.set(getVal("http://www.example.com:1234/this/is/a/path"));
        Val out = gen.eval();
        Assert.assertEquals("http", out.toString());
    }

    @Test
    public void testExtractSchemeSpecificPartFromUri() throws ParseException {
        final Generator gen = createGenerator("extractSchemeSpecificPartFromUri(${val})");

        gen.set(getVal("http://www.example.com:1234/this/is/a/path"));
        Val out = gen.eval();
        Assert.assertEquals("//www.example.com:1234/this/is/a/path", out.toString());
    }

    @Test
    public void testExtractUserInfoFromUri() throws ParseException {
        final Generator gen = createGenerator("extractUserInfoFromUri(${val})");

        gen.set(getVal("http://john:doe@example.com:81/"));
        Val out = gen.eval();
        Assert.assertEquals("john:doe", out.toString());
    }

    @Test
    public void testParseDate1() throws ParseException {
        final Generator gen = createGenerator("parseDate(${val})");

        gen.set(getVal("2014-02-22T12:12:12.888Z"));
        Val out = gen.eval();
        Assert.assertEquals(1393071132888L, out.toLong().longValue());
    }

    @Test
    public void testParseDate2() throws ParseException {
        final Generator gen = createGenerator("parseDate(${val}, 'yyyy MM dd')");

        gen.set(getVal("2014 02 22"));
        Val out = gen.eval();
        Assert.assertEquals(1393027200000L, out.toLong().longValue());
    }

    @Test
    public void testParseDate3() throws ParseException {
        final Generator gen = createGenerator("parseDate(${val}, 'yyyy MM dd', '+0400')");

        gen.set(getVal("2014 02 22"));
        Val out = gen.eval();
        Assert.assertEquals(1393012800000L, out.toLong().longValue());
    }

    @Test
    public void testFormatDate1() throws ParseException {
        final Generator gen = createGenerator("formatDate(${val})");

        gen.set(getVal(1393071132888L));
        Val out = gen.eval();
        Assert.assertEquals("2014-02-22T12:12:12.888Z", out.toString());
    }

    @Test
    public void testFormatDate2() throws ParseException {
        final Generator gen = createGenerator("formatDate(${val}, 'yyyy MM dd')");

        gen.set(getVal(1393071132888L));
        Val out = gen.eval();
        Assert.assertEquals("2014 02 22", out.toString());
    }

    @Test
    public void testFormatDate3() throws ParseException {
        final Generator gen = createGenerator("formatDate(${val}, 'yyyy MM dd', '+1200')");

        gen.set(getVal(1393071132888L));
        Val out = gen.eval();
        Assert.assertEquals("2014 02 23", out.toString());
    }

    @Test
    public void testVariance1() throws ParseException {
        final Generator gen = createGenerator("variance(600, 470, 170, 430, 300)");

        Val out = gen.eval();
        Assert.assertEquals(21704D, out.toDouble(), 0);
    }

    @Test
    public void testVariance2() throws ParseException {
        final Generator gen = createGenerator("variance(${val})");

        gen.set(getVal(600));
        gen.set(getVal(470));
        gen.set(getVal(170));
        gen.set(getVal(430));
        gen.set(getVal(300));

        Val out = gen.eval();
        Assert.assertEquals(21704D, out.toDouble(), 0);
    }

    @Test
    public void testStDev1() throws ParseException {
        final Generator gen = createGenerator("round(stDev(600, 470, 170, 430, 300))");

        Val out = gen.eval();
        Assert.assertEquals(147, out.toDouble(), 0);
    }

    @Test
    public void testStDev2() throws ParseException {
        final Generator gen = createGenerator("round(stDev(${val}))");

        gen.set(getVal(600));
        gen.set(getVal(470));
        gen.set(getVal(170));
        gen.set(getVal(430));
        gen.set(getVal(300));

        Val out = gen.eval();
        Assert.assertEquals(147, out.toDouble(), 0);
    }

    @Test
    public void testToBoolean1() throws ParseException {
        final Generator gen = createGenerator("toBoolean('true')");
        Assert.assertEquals(ValBoolean.TRUE, gen.eval());
    }

    @Test
    public void testToBoolean2() throws ParseException {
        final Generator gen = createGenerator("toBoolean(${val})");
        gen.set(getVal("true"));
        Assert.assertEquals(ValBoolean.TRUE, gen.eval());
    }

    @Test
    public void testToDouble1() throws ParseException {
        final Generator gen = createGenerator("toDouble('100')");
        Assert.assertEquals(ValDouble.create(100), gen.eval());
    }

    @Test
    public void testToDouble2() throws ParseException {
        final Generator gen = createGenerator("toDouble(${val})");
        gen.set(getVal("100"));
        Assert.assertEquals(ValDouble.create(100), gen.eval());
    }

    @Test
    public void testToInteger1() throws ParseException {
        final Generator gen = createGenerator("toInteger('100')");
        Assert.assertEquals(ValInteger.create(100), gen.eval());
    }

    @Test
    public void testToInteger2() throws ParseException {
        final Generator gen = createGenerator("toInteger(${val})");
        gen.set(getVal("100"));
        Assert.assertEquals(ValInteger.create(100), gen.eval());
    }

    @Test
    public void testToLong1() throws ParseException {
        final Generator gen = createGenerator("toLong('100')");
        Assert.assertEquals(ValLong.create(100), gen.eval());
    }

    @Test
    public void testToLong2() throws ParseException {
        final Generator gen = createGenerator("toLong(${val})");
        gen.set(getVal("100"));
        Assert.assertEquals(ValLong.create(100), gen.eval());
    }

    @Test
    public void testToString1() throws ParseException {
        final Generator gen = createGenerator("toString('100')");
        Assert.assertEquals(ValString.create("100"), gen.eval());
    }

    @Test
    public void testToString2() throws ParseException {
        final Generator gen = createGenerator("toString(${val})");
        gen.set(getVal("100"));
        Assert.assertEquals(ValString.create("100"), gen.eval());
    }

    private Generator createGenerator(final String expression) throws ParseException {
        final Expression exp = createExpression(expression);
        final Generator gen = exp.createGenerator();
        testSerialisation(gen);
        return gen;
    }

    private Expression createExpression(final String expression) throws ParseException {
        final FieldIndexMap fieldIndexMap = new FieldIndexMap();
        fieldIndexMap.create("val", true);

        final Expression exp = parser.parse(fieldIndexMap, expression);
        final String actual = exp.toString();
        Assert.assertEquals(expression, actual);

        testSerialisation(exp);
        return exp;
    }

    private Generator createGenerator2(final String expression) throws ParseException {
        final Expression exp = createExpression2(expression);
        final Generator gen = exp.createGenerator();
        testSerialisation(gen);
        return gen;
    }

    private Expression createExpression2(final String expression) throws ParseException {
        final FieldIndexMap fieldIndexMap = new FieldIndexMap();
        fieldIndexMap.create("val1", true);
        fieldIndexMap.create("val2", true);

        final Expression exp = parser.parse(fieldIndexMap, expression);
        final String actual = exp.toString();
        Assert.assertEquals(expression, actual);

        testSerialisation(exp);
        return exp;
    }

    private void testSerialisation(final Object object) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Hessian2Output out = new Hessian2Output(baos);
            out.writeObject(object);
            out.close();
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
