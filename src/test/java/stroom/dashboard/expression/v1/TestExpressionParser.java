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

import org.junit.Assert;
import org.junit.Test;

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
        final Expression exp = createExpression("min(${val})");
        final Generator generator = exp.createGenerator();

        generator.set(getVal(300D));
        generator.set(getVal(180D));

        Var out = generator.eval();
        Assert.assertEquals(180D, out.toDouble(), 0);

        generator.set(getVal(500D));

        out = generator.eval();
        Assert.assertEquals(180D, out.toDouble(), 0);

        generator.set(getVal(600D));
        generator.set(getVal(13D));
        generator.set(getVal(99.3D));
        generator.set(getVal(87D));

        out = generator.eval();
        Assert.assertEquals(13D, out.toDouble(), 0);
    }

    private Var[] getVal(final String... str) {
        final Var[] result = new Var[str.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = new VarString(str[i]);
        }
        return result;
    }

    private Var[] getVal(final double... d) {
        final Var[] result = new Var[d.length];
        for (int i = 0; i < d.length; i++) {
            result[i] = new VarDouble(d[i]);
        }
        return result;
    }

    @Test
    public void testMinUngrouped2() throws ParseException {
        final Expression exp = createExpression("min(${val}, 100, 30, 8)");
        final Generator generator = exp.createGenerator();

        generator.set(getVal(300D));

        final Var out = generator.eval();
        Assert.assertEquals(8D, out.toDouble(), 0);
    }

    @Test
    public void testMinGrouped2() throws ParseException {
        final Expression exp = createExpression("min(min(${val}), 100, 30, 8)");
        final Generator generator = exp.createGenerator();

        generator.set(getVal(300D));
        generator.set(getVal(180D));

        final Var out = generator.eval();
        Assert.assertEquals(8D, out.toDouble(), 0);
    }

    @Test
    public void testMin3() throws ParseException {
        final Expression exp = createExpression("min(min(${val}), 100, 30, 8, count(), 55)");
        final Generator generator = exp.createGenerator();

        generator.set(getVal(300D));
        generator.set(getVal(180D));

        Var out = generator.eval();
        Assert.assertEquals(2D, out.toDouble(), 0);

        generator.set(getVal(300D));
        generator.set(getVal(180D));

        out = generator.eval();
        Assert.assertEquals(4D, out.toDouble(), 0);
    }

    @Test
    public void testMax1() throws ParseException {
        final Expression exp = createExpression("max(${val})");
        final Generator generator = exp.createGenerator();

        generator.set(getVal(300D));
        generator.set(getVal(180D));

        Var out = generator.eval();
        Assert.assertEquals(300D, out.toDouble(), 0);

        generator.set(getVal(500D));

        out = generator.eval();
        Assert.assertEquals(500D, out.toDouble(), 0);

        generator.set(getVal(600D));
        generator.set(getVal(13D));
        generator.set(getVal(99.3D));
        generator.set(getVal(87D));

        out = generator.eval();
        Assert.assertEquals(600D, out.toDouble(), 0);
    }

    @Test
    public void testMaxUngrouped2() throws ParseException {
        final Expression exp = createExpression("max(${val}, 100, 30, 8)");
        final Generator generator = exp.createGenerator();

        generator.set(getVal(10D));

        final Var out = generator.eval();
        Assert.assertEquals(100D, out.toDouble(), 0);
    }

    @Test
    public void testMaxGrouped2() throws ParseException {
        final Expression exp = createExpression("max(max(${val}), 100, 30, 8)");
        final Generator generator = exp.createGenerator();

        generator.set(getVal(10D));
        generator.set(getVal(40D));

        final Var out = generator.eval();
        Assert.assertEquals(100D, out.toDouble(), 0);
    }

    @Test
    public void testMax3() throws ParseException {
        final Expression exp = createExpression("max(max(${val}), count())");
        final Generator generator = exp.createGenerator();

        generator.set(getVal(3D));
        generator.set(getVal(2D));

        Var out = generator.eval();
        Assert.assertEquals(3D, out.toDouble(), 0);

        generator.set(getVal(1D));
        generator.set(getVal(1D));

        out = generator.eval();
        Assert.assertEquals(4D, out.toDouble(), 0);
    }

    @Test
    public void testSum() throws ParseException {
        // This is a bad usage of functions as ${val} will produce the last set
        // value when we evaluate the sum. As we are effectively grouping and we
        // don't have any control over the order that cell values are inserted
        // we will end up with indeterminate behaviour.
        final Expression exp = createExpression("sum(${val}, count())");
        final Generator generator = exp.createGenerator();

        generator.set(getVal(3D));
        generator.set(getVal(2D));

        Var out = generator.eval();
        Assert.assertEquals(4D, out.toDouble(), 0);

        generator.set(getVal(1D));
        generator.set(getVal(1D));

        out = generator.eval();
        Assert.assertEquals(5D, out.toDouble(), 0);
    }

    @Test
    public void testSumOfSum() throws ParseException {
        final Expression exp = createExpression("sum(sum(${val}), count())");
        final Generator generator = exp.createGenerator();

        generator.set(getVal(3D));
        generator.set(getVal(2D));

        Var out = generator.eval();
        Assert.assertEquals(7D, out.toDouble(), 0);

        generator.set(getVal(1D));
        generator.set(getVal(1D));

        out = generator.eval();
        Assert.assertEquals(11D, out.toDouble(), 0);
    }

    @Test
    public void testAverageUngrouped() throws ParseException {
        // This is a bad usage of functions as ${val} will produce the last set
        // value when we evaluate the sum. As we are effectively grouping and we
        // don't have any control over the order that cell values are inserted
        // we will end up with indeterminate behaviour.
        final Expression exp = createExpression("average(${val}, count())");
        final Generator generator = exp.createGenerator();

        generator.set(getVal(3D));
        generator.set(getVal(4D));

        Var out = generator.eval();
        Assert.assertEquals(3D, out.toDouble(), 0);

        generator.set(getVal(1D));
        generator.set(getVal(8D));

        out = generator.eval();
        Assert.assertEquals(6D, out.toDouble(), 0);
    }

    @Test
    public void testAverageGrouped() throws ParseException {
        final Expression exp = createExpression("average(${val})");
        final Generator generator = exp.createGenerator();

        generator.set(getVal(3D));
        generator.set(getVal(4D));

        Var out = generator.eval();
        Assert.assertEquals(3.5D, out.toDouble(), 0);

        generator.set(getVal(1D));
        generator.set(getVal(8D));

        out = generator.eval();
        Assert.assertEquals(4D, out.toDouble(), 0);
    }

    @Test
    public void testReplace1() throws ParseException {
        final Expression exp = createExpression("replace('this', 'is', 'at')");
        final Generator generator = exp.createGenerator();

        generator.set(getVal(3D));

        final Var out = generator.eval();
        Assert.assertEquals("that", out.toString());
    }

    @Test
    public void testReplace2() throws ParseException {
        final Expression exp = createExpression("replace(${val}, 'is', 'at')");
        final Generator generator = exp.createGenerator();

        generator.set(getVal("this"));

        final Var out = generator.eval();
        Assert.assertEquals("that", out.toString());
    }

    @Test
    public void testConcat1() throws ParseException {
        final Expression exp = createExpression("concat('this', ' is ', 'it')");
        final Generator generator = exp.createGenerator();

        generator.set(getVal(3D));

        final Var out = generator.eval();
        Assert.assertEquals("this is it", out.toString());
    }

    @Test
    public void testConcat2() throws ParseException {
        final Expression exp = createExpression("concat(${val}, ' is ', 'it')");
        final Generator generator = exp.createGenerator();

        generator.set(getVal("this"));

        final Var out = generator.eval();
        Assert.assertEquals("this is it", out.toString());
    }

    @Test
    public void testStringLength1() throws ParseException {
        final Expression exp = createExpression("stringLength(${val})");
        final Generator generator = exp.createGenerator();

        generator.set(getVal("this"));

        final Var out = generator.eval();
        Assert.assertEquals(4D, out.toDouble(), 0);
    }

    @Test
    public void testSubstring1() throws ParseException {
        final Expression exp = createExpression("substring(${val}, 1, 2)");
        final Generator generator = exp.createGenerator();

        generator.set(getVal("this"));

        final Var out = generator.eval();
        Assert.assertEquals("h", out.toString());
    }

    @Test
    public void testSubstring3() throws ParseException {
        final Expression exp = createExpression("substring(${val}, 2, 99)");
        final Generator generator = exp.createGenerator();

        generator.set(getVal("his"));

        final Var out = generator.eval();
        Assert.assertEquals("s", out.toString());
    }

    @Test
    public void testSubstring4() throws ParseException {
        final Expression exp = createExpression("substring(${val}, 1+1, 99-1)");
        final Generator generator = exp.createGenerator();

        generator.set(getVal("his"));

        final Var out = generator.eval();
        Assert.assertEquals("s", out.toString());
    }

    @Test
    public void testSubstring5() throws ParseException {
        final Expression exp = createExpression("substring(${val}, 2+5, 99-1)");
        final Generator generator = exp.createGenerator();

        generator.set(getVal("his"));

        final Var out = generator.eval();
        Assert.assertEquals("", out.toString());
    }

    @Test
    public void testSubstringBefore1() throws ParseException {
        final Expression exp = createExpression("substringBefore(${val}, '-')");
        final Generator generator = exp.createGenerator();

        generator.set(getVal("aa-bb"));

        final Var out = generator.eval();
        Assert.assertEquals("aa", out.toString());
    }


    @Test
    public void testSubstringBefore2() throws ParseException {
        final Expression exp = createExpression("substringBefore(${val}, 'a')");
        final Generator generator = exp.createGenerator();

        generator.set(getVal("aa-bb"));

        final Var out = generator.eval();
        Assert.assertEquals("", out.toString());
    }

    @Test
    public void testSubstringBefore3() throws ParseException {
        final Expression exp = createExpression("substringBefore(${val}, 'b')");
        final Generator generator = exp.createGenerator();

        generator.set(getVal("aa-bb"));

        final Var out = generator.eval();
        Assert.assertEquals("aa-", out.toString());
    }

    @Test
    public void testSubstringBefore4() throws ParseException {
        final Expression exp = createExpression("substringBefore(${val}, 'q')");
        final Generator generator = exp.createGenerator();

        generator.set(getVal("aa-bb"));

        final Var out = generator.eval();
        Assert.assertEquals("", out.toString());
    }

    @Test
    public void testSubstringAfter1() throws ParseException {
        final Expression exp = createExpression("substringAfter(${val}, '-')");
        final Generator generator = exp.createGenerator();

        generator.set(getVal("aa-bb"));

        final Var out = generator.eval();
        Assert.assertEquals("bb", out.toString());
    }

    @Test
    public void testSubstringAfter2() throws ParseException {
        final Expression exp = createExpression("substringAfter(${val}, 'a')");
        final Generator generator = exp.createGenerator();

        generator.set(getVal("aa-bb"));

        final Var out = generator.eval();
        Assert.assertEquals("a-bb", out.toString());
    }

    @Test
    public void testSubstringAfter3() throws ParseException {
        final Expression exp = createExpression("substringAfter(${val}, 'b')");
        final Generator generator = exp.createGenerator();

        generator.set(getVal("aa-bb"));

        final Var out = generator.eval();
        Assert.assertEquals("b", out.toString());
    }

    @Test
    public void testSubstringAfter4() throws ParseException {
        final Expression exp = createExpression("substringAfter(${val}, 'q')");
        final Generator generator = exp.createGenerator();

        generator.set(getVal("aa-bb"));

        final Var out = generator.eval();
        Assert.assertEquals("", out.toString());
    }

    @Test
    public void testIndexOf() throws ParseException {
        final Expression exp = createExpression("indexOf(${val}, '-')");
        final Generator generator = exp.createGenerator();

        generator.set(getVal("aa-bb"));

        final Var out = generator.eval();
        Assert.assertEquals(2, out.toInteger().intValue());
    }

    @Test
    public void testIndexOf1() throws ParseException {
        final Expression exp = createExpression("substring(${val}, indexOf(${val}, '-'), stringLength(${val}))");
        final Generator generator = exp.createGenerator();

        generator.set(getVal("aa-bb"));

        final Var out = generator.eval();
        Assert.assertEquals("-bb", out.toString());
    }

    @Test
    public void testIndexOf2() throws ParseException {
        final Expression exp = createExpression("substring(${val}, indexOf(${val}, 'a'), stringLength(${val}))");
        final Generator generator = exp.createGenerator();

        generator.set(getVal("aa-bb"));

        final Var out = generator.eval();
        Assert.assertEquals("aa-bb", out.toString());
    }

    @Test
    public void testIndexOf3() throws ParseException {
        final Expression exp = createExpression("substring(${val}, indexOf(${val}, 'b'), stringLength(${val}))");
        final Generator generator = exp.createGenerator();

        generator.set(getVal("aa-bb"));

        final Var out = generator.eval();
        Assert.assertEquals("bb", out.toString());
    }

    @Test
    public void testIndexOf4() throws ParseException {
        final Expression exp = createExpression("substring(${val}, indexOf(${val}, 'q'), stringLength(${val}))");
        final Generator generator = exp.createGenerator();

        generator.set(getVal("aa-bb"));

        final Var out = generator.eval();
        Assert.assertEquals("", out.toString());
    }

    @Test
    public void testLastIndexOf1() throws ParseException {
        final Expression exp = createExpression("substring(${val}, lastIndexOf(${val}, '-'), stringLength(${val}))");
        final Generator generator = exp.createGenerator();

        generator.set(getVal("aa-bb"));

        final Var out = generator.eval();
        Assert.assertEquals("-bb", out.toString());
    }

    @Test
    public void testLastIndexOf2() throws ParseException {
        final Expression exp = createExpression("substring(${val}, lastIndexOf(${val}, 'a'), stringLength(${val}))");
        final Generator generator = exp.createGenerator();

        generator.set(getVal("aa-bb"));

        final Var out = generator.eval();
        Assert.assertEquals("a-bb", out.toString());
    }

    @Test
    public void testLastIndexOf3() throws ParseException {
        final Expression exp = createExpression("substring(${val}, lastIndexOf(${val}, 'b'), stringLength(${val}))");
        final Generator generator = exp.createGenerator();

        generator.set(getVal("aa-bb"));

        final Var out = generator.eval();
        Assert.assertEquals("b", out.toString());
    }

    @Test
    public void testLastIndexOf4() throws ParseException {
        final Expression exp = createExpression("substring(${val}, lastIndexOf(${val}, 'q'), stringLength(${val}))");
        final Generator generator = exp.createGenerator();

        generator.set(getVal("aa-bb"));

        final Var out = generator.eval();
        Assert.assertEquals("", out.toString());
    }

    @Test
    public void testDecode1() throws ParseException {
        final Expression exp = createExpression("decode(${val}, 'hullo', 'hello', 'goodbye')");
        final Generator generator = exp.createGenerator();

        generator.set(getVal("hullo"));

        final Var out = generator.eval();
        Assert.assertEquals("hello", out.toString());
    }

    @Test
    public void testDecode2() throws ParseException {
        final Expression exp = createExpression("decode(${val}, 'h.+o', 'hello', 'goodbye')");
        final Generator generator = exp.createGenerator();

        generator.set(getVal("hullo"));

        final Var out = generator.eval();
        Assert.assertEquals("hello", out.toString());
    }

    @Test
    public void testEquals1() throws ParseException {
        final Expression exp = createExpression("equals(${val}, 'plop')");
        final Generator generator = exp.createGenerator();

        generator.set(getVal("plop"));

        final Var out = generator.eval();
        Assert.assertEquals("true", out.toString());
    }

    @Test
    public void testEquals2() throws ParseException {
        final Expression exp = createExpression("equals(${val}, ${val})");
        final Generator generator = exp.createGenerator();

        generator.set(getVal("plop"));

        final Var out = generator.eval();
        Assert.assertEquals("true", out.toString());
    }

    @Test
    public void testEquals3() throws ParseException {
        final Expression exp = createExpression("equals(${val}, 'plip')");
        final Generator generator = exp.createGenerator();

        generator.set(getVal("plop"));

        final Var out = generator.eval();
        Assert.assertEquals("false", out.toString());
    }

    @Test
    public void testEquals4() throws ParseException {
        final Expression exp = createExpression2("equals(${val1}, ${val2})");
        final Generator generator = exp.createGenerator();

        generator.set(getVal("plop", "plip"));

        final Var out = generator.eval();
        Assert.assertEquals("false", out.toString());
    }

    @Test
    public void testEquals5() throws ParseException {
        final Expression exp = createExpression2("equals(${val1}, ${val2})");
        final Generator generator = exp.createGenerator();

        generator.set(getVal("plop", "plop"));

        final Var out = generator.eval();
        Assert.assertEquals("true", out.toString());
    }

    @Test
    public void testEquals6() throws ParseException {
        final Expression exp = createExpression2("${val1}=${val2}");
        final Generator generator = exp.createGenerator();

        generator.set(getVal("plop", "plop"));

        final Var out = generator.eval();
        Assert.assertEquals("true", out.toString());
    }

    @Test
    public void testLessThan1() throws ParseException {
        final Expression exp = createExpression2("lessThan(1, 0)");
        final Generator generator = exp.createGenerator();

        final Var out = generator.eval();
        Assert.assertEquals("false", out.toString());
    }

    @Test
    public void testLessThan2() throws ParseException {
        final Expression exp = createExpression2("lessThan(1, 1)");
        final Generator generator = exp.createGenerator();

        final Var out = generator.eval();
        Assert.assertEquals("false", out.toString());
    }

    @Test
    public void testLessThan3() throws ParseException {
        final Expression exp = createExpression2("lessThan(${val1}, ${val2})");
        final Generator generator = exp.createGenerator();

        generator.set(getVal(1D, 2D));

        final Var out = generator.eval();
        Assert.assertEquals("true", out.toString());
    }

    @Test
    public void testLessThan4() throws ParseException {
        final Expression exp = createExpression2("lessThan(${val1}, ${val2})");
        final Generator generator = exp.createGenerator();

        generator.set(getVal("fred", "fred"));

        final Var out = generator.eval();
        Assert.assertEquals("false", out.toString());
    }

    @Test
    public void testLessThan5() throws ParseException {
        final Expression exp = createExpression2("lessThan(${val1}, ${val2})");
        final Generator generator = exp.createGenerator();

        generator.set(getVal("fred", "fred1"));

        final Var out = generator.eval();
        Assert.assertEquals("true", out.toString());
    }

    @Test
    public void testLessThan6() throws ParseException {
        final Expression exp = createExpression2("lessThan(${val1}, ${val2})");
        final Generator generator = exp.createGenerator();

        generator.set(getVal("fred1", "fred"));

        final Var out = generator.eval();
        Assert.assertEquals("false", out.toString());
    }

    @Test
    public void testLessThanOrEqualTo1() throws ParseException {
        final Expression exp = createExpression2("lessThanOrEqualTo(1, 0)");
        final Generator generator = exp.createGenerator();

        final Var out = generator.eval();
        Assert.assertEquals("false", out.toString());
    }

    @Test
    public void testLessThanOrEqualTo2() throws ParseException {
        final Expression exp = createExpression2("lessThanOrEqualTo(1, 1)");
        final Generator generator = exp.createGenerator();

        final Var out = generator.eval();
        Assert.assertEquals("true", out.toString());
    }

    @Test
    public void testLessThanOrEqualTo3() throws ParseException {
        final Expression exp = createExpression2("lessThanOrEqualTo(${val1}, ${val2})");
        final Generator generator = exp.createGenerator();

        generator.set(getVal(1D, 2D));

        final Var out = generator.eval();
        Assert.assertEquals("true", out.toString());
    }

    @Test
    public void testLessThanOrEqualTo4() throws ParseException {
        final Expression exp = createExpression2("lessThanOrEqualTo(${val1}, ${val2})");
        final Generator generator = exp.createGenerator();

        generator.set(getVal("fred", "fred"));

        final Var out = generator.eval();
        Assert.assertEquals("true", out.toString());
    }

    @Test
    public void testLessThanOrEqualTo5() throws ParseException {
        final Expression exp = createExpression2("lessThanOrEqualTo(${val1}, ${val2})");
        final Generator generator = exp.createGenerator();

        generator.set(getVal("fred", "fred1"));

        final Var out = generator.eval();
        Assert.assertEquals("true", out.toString());
    }

    @Test
    public void testLessThanOrEqualTo6() throws ParseException {
        final Expression exp = createExpression2("lessThanOrEqualTo(${val1}, ${val2})");
        final Generator generator = exp.createGenerator();

        generator.set(getVal("fred1", "fred"));

        final Var out = generator.eval();
        Assert.assertEquals("false", out.toString());
    }

    @Test
    public void testSubstring2() throws ParseException {
        final Expression exp = createExpression("substring(${val}, 0, 99)");
        final Generator generator = exp.createGenerator();

        generator.set(getVal("this"));

        final Var out = generator.eval();
        Assert.assertEquals("this", out.toString());
    }

    @Test
    public void testCount() throws ParseException {
        final Expression exp = createExpression("count()");
        final Generator generator = exp.createGenerator();

        generator.set(getVal(122D));
        generator.set(getVal(133D));

        Var out = generator.eval();
        Assert.assertEquals(2D, out.toDouble(), 0);

        generator.set(getVal(11D));
        generator.set(getVal(122D));

        out = generator.eval();
        Assert.assertEquals(4D, out.toDouble(), 0);
    }

    @Test
    public void testCountUnique() throws ParseException {
        final Expression exp = createExpression("countUnique(${val})");
        final Generator generator = exp.createGenerator();

        generator.set(getVal(122D));
        generator.set(getVal(133D));

        Var out = generator.eval();
        Assert.assertEquals(2D, out.toDouble(), 0);

        generator.set(getVal(11D));
        generator.set(getVal(122D));

        out = generator.eval();
        Assert.assertEquals(3D, out.toDouble(), 0);
    }

    @Test
    public void testCountUniqueStaticValue() throws ParseException {
        final Expression exp = createExpression("countUnique('test')");
        final Generator generator = exp.createGenerator();

        generator.set(getVal(122D));
        generator.set(getVal(133D));

        Var out = generator.eval();
        Assert.assertEquals(1D, out.toDouble(), 0);

        generator.set(getVal(11D));
        generator.set(getVal(122D));

        out = generator.eval();
        Assert.assertEquals(1D, out.toDouble(), 0);
    }

    @Test
    public void testAdd1() throws ParseException {
        final Expression exp = createExpression("3+4");
        final Generator generator = exp.createGenerator();

        generator.set(getVal(1D));

        final Var out = generator.eval();
        Assert.assertEquals(7D, out.toDouble(), 0);
    }

    @Test
    public void testAdd2() throws ParseException {
        final Expression exp = createExpression("3+4+5");
        final Generator generator = exp.createGenerator();

        generator.set(getVal(1D));

        final Var out = generator.eval();
        Assert.assertEquals(12D, out.toDouble(), 0);
    }

    @Test
    public void testAdd3() throws ParseException {
        final Expression exp = createExpression("2+count()");
        final Generator generator = exp.createGenerator();

        generator.set(getVal(1D));
        generator.set(getVal(1D));

        Var out = generator.eval();
        Assert.assertEquals(4D, out.toDouble(), 0);

        generator.set(getVal(1D));
        generator.set(getVal(1D));

        out = generator.eval();
        Assert.assertEquals(6D, out.toDouble(), 0);
    }

    @Test
    public void testSubtract1() throws ParseException {
        final Expression exp = createExpression("3-4");
        final Generator generator = exp.createGenerator();

        generator.set(getVal(1D));

        final Var out = generator.eval();
        Assert.assertEquals(-1D, out.toDouble(), 0);
    }

    @Test
    public void testSubtract2() throws ParseException {
        final Expression exp = createExpression("2-count()");
        final Generator generator = exp.createGenerator();

        generator.set(getVal(1D));
        generator.set(getVal(1D));

        Var out = generator.eval();
        Assert.assertEquals(0D, out.toDouble(), 0);

        generator.set(getVal(1D));
        generator.set(getVal(1D));

        out = generator.eval();
        Assert.assertEquals(-2D, out.toDouble(), 0);
    }

    @Test
    public void testMultiply1() throws ParseException {
        final Expression exp = createExpression("3*4");
        final Generator generator = exp.createGenerator();

        generator.set(getVal(1D));

        final Var out = generator.eval();
        Assert.assertEquals(12D, out.toDouble(), 0);
    }

    @Test
    public void testMultiply2() throws ParseException {
        final Expression exp = createExpression("2*count()");
        final Generator generator = exp.createGenerator();

        generator.set(getVal(1D));
        generator.set(getVal(1D));

        Var out = generator.eval();
        Assert.assertEquals(4D, out.toDouble(), 0);

        generator.set(getVal(1D));
        generator.set(getVal(1D));

        out = generator.eval();
        Assert.assertEquals(8D, out.toDouble(), 0);
    }

    @Test
    public void testDivide1() throws ParseException {
        final Expression exp = createExpression("8/4");
        final Generator generator = exp.createGenerator();

        generator.set(getVal(1D));

        final Var out = generator.eval();
        Assert.assertEquals(2D, out.toDouble(), 0);
    }

    @Test
    public void testDivide2() throws ParseException {
        final Expression exp = createExpression("8/count()");
        final Generator generator = exp.createGenerator();

        generator.set(getVal(1D));
        generator.set(getVal(1D));

        Var out = generator.eval();
        Assert.assertEquals(4D, out.toDouble(), 0);

        generator.set(getVal(1D));
        generator.set(getVal(1D));

        out = generator.eval();
        Assert.assertEquals(2D, out.toDouble(), 0);
    }

    @Test
    public void testFloorNum1() throws ParseException {
        final Expression exp = createExpression("floor(8.4234)");
        final Generator generator = exp.createGenerator();

        generator.set(getVal(1D));

        final Var out = generator.eval();
        Assert.assertEquals(8D, out.toDouble(), 0);
    }

    @Test
    public void testFloorNum2() throws ParseException {
        final Expression exp = createExpression("floor(8.5234)");
        final Generator generator = exp.createGenerator();

        generator.set(getVal(1D));

        final Var out = generator.eval();
        Assert.assertEquals(8D, out.toDouble(), 0);
    }

    @Test
    public void testFloorNum3() throws ParseException {
        final Expression exp = createExpression("floor(${val})");
        final Generator generator = exp.createGenerator();

        generator.set(getVal(1.34D));

        final Var out = generator.eval();
        Assert.assertEquals(1D, out.toDouble(), 0);
    }

    @Test
    public void testFloorNum4() throws ParseException {
        final Expression exp = createExpression("floor(${val}+count())");
        final Generator generator = exp.createGenerator();

        generator.set(getVal(1.34D));
        generator.set(getVal(1.8655D));

        final Var out = generator.eval();
        Assert.assertEquals(3D, out.toDouble(), 0);
    }

    @Test
    public void testFloorNum5() throws ParseException {
        final Expression exp = createExpression("floor(${val}+count(), 1)");
        final Generator generator = exp.createGenerator();

        generator.set(getVal(1.34D));
        generator.set(getVal(1.8655D));

        final Var out = generator.eval();
        Assert.assertEquals(3.8D, out.toDouble(), 0);
    }

    @Test
    public void testFloorNum6() throws ParseException {
        final Expression exp = createExpression("floor(${val}+count(), 2)");
        final Generator generator = exp.createGenerator();

        generator.set(getVal(1.34D));
        generator.set(getVal(1.8655D));

        final Var out = generator.eval();
        Assert.assertEquals(3.86D, out.toDouble(), 0);
    }

    @Test
    public void testCeilNum1() throws ParseException {
        final Expression exp = createExpression("ceiling(8.4234)");
        final Generator generator = exp.createGenerator();

        generator.set(getVal(1D));

        final Var out = generator.eval();
        Assert.assertEquals(9D, out.toDouble(), 0);
    }

    @Test
    public void testCeilNum2() throws ParseException {
        final Expression exp = createExpression("ceiling(8.5234)");
        final Generator generator = exp.createGenerator();

        generator.set(getVal(1D));

        final Var out = generator.eval();
        Assert.assertEquals(9D, out.toDouble(), 0);
    }

    @Test
    public void testCeilNum3() throws ParseException {
        final Expression exp = createExpression("ceiling(${val})");
        final Generator generator = exp.createGenerator();

        generator.set(getVal(1.34D));

        final Var out = generator.eval();
        Assert.assertEquals(2D, out.toDouble(), 0);
    }

    @Test
    public void testCeilNum4() throws ParseException {
        final Expression exp = createExpression("ceiling(${val}+count())");
        final Generator generator = exp.createGenerator();

        generator.set(getVal(1.34D));
        generator.set(getVal(1.8655D));

        final Var out = generator.eval();
        Assert.assertEquals(4D, out.toDouble(), 0);
    }

    @Test
    public void testCeilNum5() throws ParseException {
        final Expression exp = createExpression("ceiling(${val}+count(), 1)");
        final Generator generator = exp.createGenerator();

        generator.set(getVal(1.34D));
        generator.set(getVal(1.8655D));

        final Var out = generator.eval();
        Assert.assertEquals(3.9D, out.toDouble(), 0);
    }

    @Test
    public void testCeilNum6() throws ParseException {
        final Expression exp = createExpression("ceiling(${val}+count(), 2)");
        final Generator generator = exp.createGenerator();

        generator.set(getVal(1.34D));
        generator.set(getVal(1.8655D));

        final Var out = generator.eval();
        Assert.assertEquals(3.87D, out.toDouble(), 0);
    }

    @Test
    public void testRoundNum1() throws ParseException {
        final Expression exp = createExpression("round(8.4234)");
        final Generator generator = exp.createGenerator();

        generator.set(getVal(1D));

        final Var out = generator.eval();
        Assert.assertEquals(8D, out.toDouble(), 0);
    }

    @Test
    public void testRoundNum2() throws ParseException {
        final Expression exp = createExpression("round(8.5234)");
        final Generator generator = exp.createGenerator();

        generator.set(getVal(1D));

        final Var out = generator.eval();
        Assert.assertEquals(9D, out.toDouble(), 0);
    }

    @Test
    public void testRoundNum3() throws ParseException {
        final Expression exp = createExpression("round(${val})");
        final Generator generator = exp.createGenerator();

        generator.set(getVal(1.34D));

        final Var out = generator.eval();
        Assert.assertEquals(1D, out.toDouble(), 0);
    }

    @Test
    public void testRoundNum4() throws ParseException {
        final Expression exp = createExpression("round(${val}+count())");
        final Generator generator = exp.createGenerator();

        generator.set(getVal(1.34D));
        generator.set(getVal(1.8655D));

        final Var out = generator.eval();
        Assert.assertEquals(4D, out.toDouble(), 0);
    }

    @Test
    public void testRoundNum5() throws ParseException {
        final Expression exp = createExpression("round(${val}+count(), 1)");
        final Generator generator = exp.createGenerator();

        generator.set(getVal(1.34D));
        generator.set(getVal(1.8655D));

        final Var out = generator.eval();
        Assert.assertEquals(3.9D, out.toDouble(), 0);
    }

    @Test
    public void testRoundNum6() throws ParseException {
        final Expression exp = createExpression("round(${val}+count(), 2)");
        final Generator generator = exp.createGenerator();

        generator.set(getVal(1.34D));
        generator.set(getVal(1.8655D));

        final Var out = generator.eval();
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
        final Expression exp = createExpression(expression);
        final Generator generator = exp.createGenerator();
        generator.set(getVal(in));
        final Var out = generator.eval();
        Assert.assertEquals(expectedMs, out.toDouble(), 0);
    }

    @Test
    public void testBODMAS1() throws ParseException {
        final Expression exp = createExpression("4+4/2+2");
        final Generator generator = exp.createGenerator();
        final Var out = generator.eval();

        // Non BODMAS would evaluate as 6 or even 4 - BODMAS should be 8.
        Assert.assertEquals(8D, out.toDouble(), 0);
    }

    @Test
    public void testBODMAS2() throws ParseException {
        final Expression exp = createExpression("(4+4)/2+2");
        final Generator generator = exp.createGenerator();
        final Var out = generator.eval();

        // Non BODMAS would evaluate as 6 or even 4 - BODMAS should be 6.
        Assert.assertEquals(6D, out.toDouble(), 0);
    }

    @Test
    public void testBODMAS3() throws ParseException {
        final Expression exp = createExpression("(4+4)/(2+2)");
        final Generator generator = exp.createGenerator();
        final Var out = generator.eval();

        // Non BODMAS would evaluate as 6 or even 4 - BODMAS should be 2.
        Assert.assertEquals(2D, out.toDouble(), 0);
    }

    @Test
    public void testBODMAS4() throws ParseException {
        final Expression exp = createExpression("4+4/2+2*3");
        final Generator generator = exp.createGenerator();
        final Var out = generator.eval();

        // Non BODMAS would evaluate as 18 - BODMAS should be 12.
        Assert.assertEquals(12D, out.toDouble(), 0);
    }

    @Test
    public void testExtractAuthorityFromUri() throws ParseException {
        final Expression exp = createExpression("extractAuthorityFromUri(${val})");
        final Generator generator = exp.createGenerator();

        generator.set(getVal("http://www.example.com:1234/this/is/a/path"));
        Var out = generator.eval();
        Assert.assertEquals("www.example.com:1234", out.toString());
    }

    @Test
    public void testExtractFragmentFromUri() throws ParseException {
        final Expression exp = createExpression("extractFragmentFromUri(${val})");
        final Generator generator = exp.createGenerator();

        generator.set(getVal("http://www.example.com:1234/this/is/a/path#frag"));
        Var out = generator.eval();
        Assert.assertEquals("frag", out.toString());
    }

    @Test
    public void testExtractHostFromUri() throws ParseException {
        final Expression exp = createExpression("extractHostFromUri(${val})");
        final Generator generator = exp.createGenerator();

        generator.set(getVal("http://www.example.com:1234/this/is/a/path"));
        Var out = generator.eval();
        Assert.assertEquals("www.example.com", out.toString());
    }

    @Test
    public void testExtractPathFromUri() throws ParseException {
        final Expression exp = createExpression("extractPathFromUri(${val})");
        final Generator generator = exp.createGenerator();

        generator.set(getVal("http://www.example.com:1234/this/is/a/path"));
        Var out = generator.eval();
        Assert.assertEquals("/this/is/a/path", out.toString());
    }

    @Test
    public void testExtractPortFromUri() throws ParseException {
        final Expression exp = createExpression("extractPortFromUri(${val})");
        final Generator generator = exp.createGenerator();

        generator.set(getVal("http://www.example.com:1234/this/is/a/path"));
        Var out = generator.eval();
        Assert.assertEquals("1234", out.toString());
    }

    @Test
    public void testExtractQueryFromUri() throws ParseException {
        final Expression exp = createExpression("extractQueryFromUri(${val})");
        final Generator generator = exp.createGenerator();

        generator.set(getVal("http://www.example.com:1234/this/is/a/path?this=that&foo=bar"));
        Var out = generator.eval();
        Assert.assertEquals("this=that&foo=bar", out.toString());
    }

    @Test
    public void testExtractSchemeFromUri() throws ParseException {
        final Expression exp = createExpression("extractSchemeFromUri(${val})");
        final Generator generator = exp.createGenerator();

        generator.set(getVal("http://www.example.com:1234/this/is/a/path"));
        Var out = generator.eval();
        Assert.assertEquals("http", out.toString());
    }

    @Test
    public void testExtractSchemeSpecificPartFromUri() throws ParseException {
        final Expression exp = createExpression("extractSchemeSpecificPartFromUri(${val})");
        final Generator generator = exp.createGenerator();

        generator.set(getVal("http://www.example.com:1234/this/is/a/path"));
        Var out = generator.eval();
        Assert.assertEquals("//www.example.com:1234/this/is/a/path", out.toString());
    }

    @Test
    public void testExtractUserInfoFromUri() throws ParseException {
        final Expression exp = createExpression("extractUserInfoFromUri(${val})");
        final Generator generator = exp.createGenerator();

        generator.set(getVal("http://john:doe@example.com:81/"));
        Var out = generator.eval();
        Assert.assertEquals("john:doe", out.toString());
    }

    @Test
    public void testParseDate1() throws ParseException {
        final Expression exp = createExpression("parseDate(${val})");
        final Generator generator = exp.createGenerator();

        generator.set(getVal("2014-02-22T12:12:12.888Z"));
        Var out = generator.eval();
        Assert.assertEquals(1393071132888L, out.toLong().longValue());
    }

    @Test
    public void testParseDate2() throws ParseException {
        final Expression exp = createExpression("parseDate(${val}, 'yyyy MM dd')");
        final Generator generator = exp.createGenerator();

        generator.set(getVal("2014 02 22"));
        Var out = generator.eval();
        Assert.assertEquals(1393027200000L, out.toLong().longValue());
    }

    @Test
    public void testParseDate3() throws ParseException {
        final Expression exp = createExpression("parseDate(${val}, 'yyyy MM dd', '+0400')");
        final Generator generator = exp.createGenerator();

        generator.set(getVal("2014 02 22"));
        Var out = generator.eval();
        Assert.assertEquals(1393012800000L, out.toLong().longValue());
    }

    private Expression createExpression(final String expression) throws ParseException {
        final FieldIndexMap fieldIndexMap = new FieldIndexMap();
        fieldIndexMap.create("val", true);

        final Expression exp = parser.parse(fieldIndexMap, expression);
        final String actual = exp.toString();
        Assert.assertEquals(expression, actual);
        return exp;
    }

    private Expression createExpression2(final String expression) throws ParseException {
        final FieldIndexMap fieldIndexMap = new FieldIndexMap();
        fieldIndexMap.create("val1", true);
        fieldIndexMap.create("val2", true);

        final Expression exp = parser.parse(fieldIndexMap, expression);
        final String actual = exp.toString();
        Assert.assertEquals(expression, actual);
        return exp;
    }
}
