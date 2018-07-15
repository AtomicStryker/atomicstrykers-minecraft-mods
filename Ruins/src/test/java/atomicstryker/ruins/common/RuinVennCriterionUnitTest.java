package atomicstryker.ruins.common;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class RuinVennCriterionUnitTest
{
    @Test
    public void testCriterion()
    {
        Set<String> elements = new HashSet<>();
        elements.add("ALPHA");
        elements.add("BETA");
        elements.add("GAMMA");
        elements.add("DELTA");
        elements.add("EPSILON");
        elements.add(")\till-\"behaved-\\entry, ((");
        elements.add("special");

        // basic single-element expressions

        {
            final String expression = "BETA";
            assertTrue("test 01", RuinVennCriterion.parseExpression(expression).isSatisfiedBy(elements));
        }

        {
            final String expression = "WUMPUS";
            assertFalse("test 02", RuinVennCriterion.parseExpression(expression).isSatisfiedBy(elements));
        }

        // quoted elements

        {
            final String expression = "\")\\till-\\\"behaved-\\\\entry, ((\"";
            assertTrue("test 03", RuinVennCriterion.parseExpression(expression).isSatisfiedBy(elements));
        }

        {
            final String expression = "\")\\tall-\\\"behived-\\\\entry, ((\"";
            assertFalse("test 04", RuinVennCriterion.parseExpression(expression).isSatisfiedBy(elements));
        }

        // unary minus (NOT)

        {
            final String expression = "-DELTA";
            assertFalse("test 05", RuinVennCriterion.parseExpression(expression).isSatisfiedBy(elements));
        }

        {
            final String expression = "-WUMPUS";
            assertTrue("test 06", RuinVennCriterion.parseExpression(expression).isSatisfiedBy(elements));
        }

        // binary plus (AND)

        {
            final String expression = "GAMMA+EPSILON";
            assertTrue("test 07", RuinVennCriterion.parseExpression(expression).isSatisfiedBy(elements));
        }

        {
            final String expression = "GAMMA+SNEEZY";
            assertFalse("test 08", RuinVennCriterion.parseExpression(expression).isSatisfiedBy(elements));
        }

        {
            final String expression = "GRUMPY+EPSILON";
            assertFalse("test 09", RuinVennCriterion.parseExpression(expression).isSatisfiedBy(elements));
        }

        {
            final String expression = "BASHFUL+DOC";
            assertFalse("test 10", RuinVennCriterion.parseExpression(expression).isSatisfiedBy(elements));
        }

        // binary minus (AND NOT)

        {
            final String expression = "GAMMA-EPSILON";
            assertFalse("test 11", RuinVennCriterion.parseExpression(expression).isSatisfiedBy(elements));
        }

        {
            final String expression = "GAMMA-SNEEZY";
            assertTrue("test 12", RuinVennCriterion.parseExpression(expression).isSatisfiedBy(elements));
        }

        {
            final String expression = "GRUMPY-EPSILON";
            assertFalse("test 13", RuinVennCriterion.parseExpression(expression).isSatisfiedBy(elements));
        }

        {
            final String expression = "BASHFUL-DOC";
            assertFalse("test 14", RuinVennCriterion.parseExpression(expression).isSatisfiedBy(elements));
        }

        // binary comma (OR)

        {
            final String expression = "GAMMA,EPSILON";
            assertTrue("test 15", RuinVennCriterion.parseExpression(expression).isSatisfiedBy(elements));
        }

        {
            final String expression = "GAMMA,SNEEZY";
            assertTrue("test 16", RuinVennCriterion.parseExpression(expression).isSatisfiedBy(elements));
        }

        {
            final String expression = "GRUMPY,EPSILON";
            assertTrue("test 17", RuinVennCriterion.parseExpression(expression).isSatisfiedBy(elements));
        }

        {
            final String expression = "BASHFUL,DOC";
            assertFalse("test 18", RuinVennCriterion.parseExpression(expression).isSatisfiedBy(elements));
        }

        // stacked unary operators--legal, but bad form

        {
            final String expression = "+-+GAMMA-+-EPSILON";
            assertFalse("test 19", RuinVennCriterion.parseExpression(expression).isSatisfiedBy(elements));
        }

        {
            final String expression = "+-+GAMMA-+-SNEEZY";
            assertFalse("test 20", RuinVennCriterion.parseExpression(expression).isSatisfiedBy(elements));
        }

        {
            final String expression = "+-+GRUMPY-+-EPSILON";
            assertTrue("test 21", RuinVennCriterion.parseExpression(expression).isSatisfiedBy(elements));
        }

        {
            final String expression = "+-+BASHFUL-+-DOC";
            assertFalse("test 22", RuinVennCriterion.parseExpression(expression).isSatisfiedBy(elements));
        }

        // operator precedence

        {
            final String expression = "ALPHA+BETA,GAMMA";
            assertTrue("test 23", RuinVennCriterion.parseExpression(expression).isSatisfiedBy(elements));
        }

        {
            final String expression = "ALPHA+BETA,BLUE";
            assertTrue("test 24", RuinVennCriterion.parseExpression(expression).isSatisfiedBy(elements));
        }

        {
            final String expression = "ALPHA+YELLOW,GAMMA";
            assertTrue("test 25", RuinVennCriterion.parseExpression(expression).isSatisfiedBy(elements));
        }

        {
            final String expression = "ALPHA+YELLOW,BLUE";
            assertFalse("test 26", RuinVennCriterion.parseExpression(expression).isSatisfiedBy(elements));
        }

        {
            final String expression = "RED+BETA,GAMMA";
            assertTrue("test 27", RuinVennCriterion.parseExpression(expression).isSatisfiedBy(elements));
        }

        {
            final String expression = "RED+BETA,BLUE";
            assertFalse("test 28", RuinVennCriterion.parseExpression(expression).isSatisfiedBy(elements));
        }

        {
            final String expression = "RED+YELLOW,GAMMA";
            assertTrue("test 29", RuinVennCriterion.parseExpression(expression).isSatisfiedBy(elements));
        }

        {
            final String expression = "RED+YELLOW,BLUE";
            assertFalse("test 30", RuinVennCriterion.parseExpression(expression).isSatisfiedBy(elements));
        }

        {
            final String expression = "ALPHA,BETA+GAMMA";
            assertTrue("test 31", RuinVennCriterion.parseExpression(expression).isSatisfiedBy(elements));
        }

        {
            final String expression = "ALPHA,BETA+BLUE";
            assertTrue("test 32", RuinVennCriterion.parseExpression(expression).isSatisfiedBy(elements));
        }

        {
            final String expression = "ALPHA,YELLOW+GAMMA";
            assertTrue("test 33", RuinVennCriterion.parseExpression(expression).isSatisfiedBy(elements));
        }

        {
            final String expression = "ALPHA,YELLOW+BLUE";
            assertTrue("test 34", RuinVennCriterion.parseExpression(expression).isSatisfiedBy(elements));
        }

        {
            final String expression = "RED,BETA+GAMMA";
            assertTrue("test 35", RuinVennCriterion.parseExpression(expression).isSatisfiedBy(elements));
        }

        {
            final String expression = "RED,BETA+BLUE";
            assertFalse("test 36", RuinVennCriterion.parseExpression(expression).isSatisfiedBy(elements));
        }

        {
            final String expression = "RED,YELLOW+GAMMA";
            assertFalse("test 37", RuinVennCriterion.parseExpression(expression).isSatisfiedBy(elements));
        }

        {
            final String expression = "RED,YELLOW+BLUE";
            assertFalse("test 38", RuinVennCriterion.parseExpression(expression).isSatisfiedBy(elements));
        }

        // parentheses

        {
            final String expression = "(ALPHA,BETA)+GAMMA";
            assertTrue("test 39", RuinVennCriterion.parseExpression(expression).isSatisfiedBy(elements));
        }

        {
            final String expression = "(ALPHA,BETA)+BLUE";
            assertFalse("test 40", RuinVennCriterion.parseExpression(expression).isSatisfiedBy(elements));
        }

        {
            final String expression = "(ALPHA,YELLOW)+GAMMA";
            assertTrue("test 41", RuinVennCriterion.parseExpression(expression).isSatisfiedBy(elements));
        }

        {
            final String expression = "(ALPHA,YELLOW)+BLUE";
            assertFalse("test 42", RuinVennCriterion.parseExpression(expression).isSatisfiedBy(elements));
        }

        {
            final String expression = "(RED,BETA)+GAMMA";
            assertTrue("test 43", RuinVennCriterion.parseExpression(expression).isSatisfiedBy(elements));
        }

        {
            final String expression = "(RED,BETA)+BLUE";
            assertFalse("test 44", RuinVennCriterion.parseExpression(expression).isSatisfiedBy(elements));
        }

        {
            final String expression = "(RED,YELLOW)+GAMMA";
            assertFalse("test 45", RuinVennCriterion.parseExpression(expression).isSatisfiedBy(elements));
        }

        {
            final String expression = "(RED,YELLOW)+BLUE";
            assertFalse("test 46", RuinVennCriterion.parseExpression(expression).isSatisfiedBy(elements));
        }

        // spaces and comments

        {
            final String expression = "  (  ALPHA  ,  BETA  )  +  GAMMA  #  xyz";
            assertTrue("test 47", RuinVennCriterion.parseExpression(expression).isSatisfiedBy(elements));
        }

        {
            final String expression = "  (  ALPHA  ,  BETA  )  +  BLUE  #  xyz";
            assertFalse("test 48", RuinVennCriterion.parseExpression(expression).isSatisfiedBy(elements));
        }

        // error cases

        {
            try
            {
                final String expression = "(UNBALANCED+LEFT";
                RuinVennCriterion.parseExpression(expression);
                fail();
            }
            catch (RuntimeException exception)
            {
                System.out.println(String.format("test 49 expected exception: %s", exception));
            }
        }

        {
            try
            {
                final String expression = "UNBALANCED+RIGHT)";
                RuinVennCriterion.parseExpression(expression);
                fail();
            }
            catch (RuntimeException exception)
            {
                System.out.println(String.format("test 50 expected exception: %s", exception));
            }
        }

        {
            try
            {
                final String expression = "UNRECOGNIZED$CHARACTERS";
                RuinVennCriterion.parseExpression(expression);
                fail();
            }
            catch (RuntimeException exception)
            {
                System.out.println(String.format("test 51 expected exception: %s", exception));
            }
        }

        {
            try
            {
                final String expression = "BAD+,OPERATOR";
                RuinVennCriterion.parseExpression(expression);
                fail();
            }
            catch (RuntimeException exception)
            {
                System.out.println(String.format("test 52 expected exception: %s", exception));
            }
        }

        {
            try
            {
                Set<String> valid_elements = new HashSet<>();
                valid_elements.add("ALPHA");
                valid_elements.add("GAMMA");

                final String expression = "ALPHA,BETA,GAMMA";
                RuinVennCriterion.parseExpression(expression, true, valid_elements);
                fail();
            }
            catch (RuntimeException exception)
            {
                System.out.println(String.format("test 53 expected exception: %s", exception));
            }
        }

        {
            try
            {
                System.out.println("test 54 expected warning:");

                Set<String> valid_elements = new HashSet<>();
                valid_elements.add("ALPHA");
                valid_elements.add("GAMMA");

                final String expression = "ALPHA,BETA,GAMMA";
                RuinVennCriterion.parseExpression(expression, false, valid_elements);
            }
            catch (RuntimeException exception)
            {
                fail();
            }
        }

        // empty expressions

        {
            final String expression = "";
            assertFalse("test 55", RuinVennCriterion.parseExpression(expression).isSatisfiedBy(elements));
            assertTrue("test 56", RuinVennCriterion.parseExpression(expression).isEmpty());
        }

        {
            final String expression = "special";
            assertTrue("test 57", RuinVennCriterion.parseExpression(expression).isSatisfiedBy(elements));
            assertFalse("test 58", RuinVennCriterion.parseExpression(expression).isEmpty());
        }

        // gratuitous bonus cases

        {
            final String expression = "(LAMBDA, \")\\till-\\\"behaved-\\\\entry, ((\") - (HELLO, KITTY)";
            assertTrue("test 59", RuinVennCriterion.parseExpression(expression).isSatisfiedBy(elements));
        }

        {
            final String expression = "((((((((((++++++(++(++(BETA)))------(--(--DELTA))))))))))))";
            assertTrue("test 60", RuinVennCriterion.parseExpression(expression).isSatisfiedBy(elements));
        }
    }
}
