package atomicstryker.ruins.common;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Criterion against which collections of strings can be tested. The conditions to be met in order to satisfy a
 * particular criterion are defined by an expression provided at the time of its creation.
 * <p>
 * An expression consists of a number of <i>elements</i>, or strings, to be found (or not) among collections compared
 * to it. Elements are case sensitive and must either consist entirely of alphanumeric characters and/or underscores or
 * be enclosed in quotes. Quoted elements may contain standard Java escape sequences. Elements are combined to form
 * expressions through the use of the following <i>operators</i>:
 * <ul>
 * <li>unary minus, representing complement ("-A" means any collection <i>not</i> containing A)</li>
 * <li>binary plus, representing intersection ("A+B" means any collection containing both A <i>and</i> B)</li>
 * <li>binary minus, representing relative complement ("A-B" means any collection containing A <i>and not</i> B)</li>
 * <li>binary comma, representing union ("A,B" means any collection containing either A <i>or</i> B, or both)</li>
 * </ul>
 * Unary plus ("+A") is also accepted as an operator for completeness, but it has no effect. The unary operators have
 * highest precedence, followed by the binary plus and minus operators, and finally the comma. Order of evaluation can
 * also be modified by using parentheses to enclose subexpressions. Whitespace is ignored, as is the number sign and
 * anything following it (unless enclosed in quotes).
 * <p>
 * Example: A criterion created from the expression <code>RED + (YELLOW, GREEN) - BLUE</code> is satisfied by any
 * collection of strings containing RED, either TELLOW or GREEN (or both), but not BLUE.
 * <p>
 * Optionally, a collection of elements may also be provided from which an expression must be constructed. If an
 * element in the expression does not appear in the list of valid elements, an exception is thrown (if strict
 * validation is enabled) or a warning is logged (if strict validation is not enabled).
 */
class RuinVennCriterion
{
    /**
     * Creates a RuinVennCriterion object from the specified expression. Elements (e.g. names, IDs, etc.) in the
     * expression are validated against the collection provided; any not appearing in the collection generate a
     * warning in the log file or, if strict validation is selected, throw a runtime exception.
     *
     * @param   expression          definition of criterion
     * @param   strict_validation   true if failed element validation is considered an error; false otherwise
     * @param   valid_elements      collection of valid elements
     * @return                      a new RuinVennCriterion object as defined by the expression
     */
    public static RuinVennCriterion parseExpression(@Nonnull String expression, boolean strict_validation, @Nonnull Collection<String> valid_elements)
    {
        Objects.requireNonNull(expression, "'expression' must not be null");
        Objects.requireNonNull(valid_elements, "'valid_elements' must not be null");

        return (new ExpressionParser()).parse(expression, strict_validation, valid_elements);
    }

    /**
     * Creates a RuinVennCriterion object from the specified expression. No element validation is performed.
     *
     * @param   expression  definition of criterion
     * @return              a new RuinVennCriterion object as defined by the expression
     */
    public static RuinVennCriterion parseExpression(@Nonnull String expression)
    {
        Objects.requireNonNull(expression, "'expression' must not be null");

        return (new ExpressionParser()).parse(expression, false, null);
    }

    /**
     * Tests whether the specified collection of elements meets this criterion.
     *
     * @param   elements    elements to be tested
     * @return              true if the criterion is met, false otherwise
     */
    public boolean isSatisfiedBy(@Nonnull Collection<String> elements)
    {
        Objects.requireNonNull(elements, "'elements' must not be null");

        return root_node_ != null && root_node_.isSatisfiedBy(elements);
    }

    /**
     * Tests whether the specified element meets this criterion.
     *
     * @param   element     element to be tested
     * @return              true if the criterion is met, false otherwise
     */
    public boolean isSatisfiedBy(@Nonnull String element)
    {
        Objects.requireNonNull(element, "'element' must not be null");

        // build a temporary set containing just this element
        //
        Set<String> elements = new HashSet<>();
        elements.add(element);

        return isSatisfiedBy(elements);
    }

    /**
     * Returns true if the criterion was created with a blank expression. Such a criterion can not be satisfied by any
     * collection of elements.
     *
     * @return  true if criterion has no conditions to meet
     */
    public boolean isEmpty()
    {
        return root_node_ == null;
    }

    // diagnostic logger
    //
    private static final Logger LOGGER = LogManager.getLogger();

    // a criterion is represented as a binary tree of operator nodes and elements (leaf nodes); this is the root
    //
    private Node root_node_;

    // private constructor; build RuinVennCriterion objects by calling the public parseExpression() methods
    //
    private RuinVennCriterion(Node root_node)
    {
        root_node_ = root_node;
    }

    // a node of the binary node representing a criterion
    // determining whether a collection of elements meets the criterion is achieved by walking the tree of nodes
    //
    private interface Node
    {
        public boolean isSatisfiedBy(Collection<String> elements);
    }

    // an element (leaf) node simply contains a single element
    // a collection satisfies this node if and only if it includes that element
    //
    private static class ElementNode implements Node
    {
        private final String element_;

        public ElementNode(String element)
        {
            element_ = element;
        }

        @Override
        public boolean isSatisfiedBy(Collection<String> elements)
        {
            return elements.contains(element_);
        }
    }

    // a complement (NOT) node contains one other node
    // a collection satisfies this node if and only if it does not satisfy the node it contains
    //
    private static class ComplementNode implements Node
    {
        private final Node node_;

        public ComplementNode(Node node)
        {
            node_ = node;
        }

        @Override
        public boolean isSatisfiedBy(Collection<String> elements)
        {
            return !node_.isSatisfiedBy(elements);
        }
    }

    // an intersection (AND) node contains two other nodes
    // a collection satisfies this node if and only if it satisfies both of the nodes it contains
    //
    private static class IntersectionNode implements Node
    {
        private final Node left_node_;
        private final Node right_node_;

        public IntersectionNode(Node left_node, Node right_node)
        {
            left_node_ = left_node;
            right_node_ = right_node;
        }

        @Override
        public boolean isSatisfiedBy(Collection<String> elements)
        {
            return left_node_.isSatisfiedBy(elements) && right_node_.isSatisfiedBy(elements);
        }
    }

    // a union (OR) node contains two other nodes
    // a collection satisfies this node if and only if it satisfies at least one of the nodes it contains
    //
    private static class UnionNode implements Node
    {
        private final Node left_node_;
        private final Node right_node_;

        public UnionNode(Node left_node, Node right_node)
        {
            left_node_ = left_node;
            right_node_ = right_node;
        }

        @Override
        public boolean isSatisfiedBy(Collection<String> elements)
        {
            return left_node_.isSatisfiedBy(elements) || right_node_.isSatisfiedBy(elements);
        }
    }

    // utility for parsing expressions to create new criteria
    //
    private static class ExpressionParser
    {
        // public constructor; initializes expression parsing stacks
        //
        public ExpressionParser()
        {
            nodes_ = new ArrayDeque<>();
            operators_ = new ArrayDeque<>();
        }

        // launch the expression parsing state machine
        // parsing is performed via a variation of the shunting yard algorithm
        //
        public RuinVennCriterion parse(String expression, boolean strict_validation, Collection<String> valid_elements)
        {
            // initialize state machine
            //
            LOGGER.debug("parsing Venn criterion expression '{}'", expression);

            if (valid_elements != null)
            {
                LOGGER.debug("strict element validation enforcement '{}'", strict_validation);
                LOGGER.debug("element validation against collection '{}'", valid_elements);
            }
            else
            {
                LOGGER.debug("no element validation");
            }

            Node root_node = null;

            LOGGER.debug("starting expression parser state machine");
            State state = State.EXPECT_ELEMENT;
            Matcher matcher = WHITESPACE_PATTERN.matcher(expression);
            int start = 0;
            final int end = expression.length();
            boolean warning_throttle = false;

            // loop until the entire expression has been processed
            //
            for (matcher.region(start, end).lookingAt(); (start = matcher.end()) < end; matcher.region(start, end).usePattern(WHITESPACE_PATTERN).lookingAt())
            {
                // extract the next field from the expression, depending on the current parser state
                //
                LOGGER.debug("looking for next expression field in state '{}' at index {}", state, matcher.start());

                switch (state)
                {
                case EXPECT_ELEMENT:
                {
                    // EXPECT_ELEMENT state applies initially, after an operator, or at the start of a subexpression
                    // elements, unary operators, and subexpression starts are valid in this state

                    if (matcher.region(start, end).usePattern(ELEMENT_PATTERN).lookingAt())
                    {
                        // if an element occurs, push it onto the node stack and advance the state
                        // unescape the element if it is "quoted" to include otherwise-invalid chcracters

                        start = matcher.end();
                        final String element = matcher.group(1) != null ? matcher.group(1) : StringEscapeUtils.unescapeJava(matcher.group(2));
                        LOGGER.debug("encountered element '{}'", element);

                        if (valid_elements != null && !valid_elements.contains(element))
                        {
                            if (strict_validation)
                            {
                                throw new RuntimeException(String.format("invalid element '%s' encountered", element));
                            }

                            // limit warnings to one per expression so as not to spam log
                            //
                            if (!warning_throttle)
                            {
                                LOGGER.warn("invalid element '{}' encountered in criterion expression '{}'", element, expression);
                                warning_throttle = true;
                            }
                        }

                        LOGGER.debug("pushing element '{}' onto node stack", element);
                        nodes_.push(new ElementNode(element));
                        state = State.EXPECT_OPERATOR;
                    }
                    else if (matcher.region(start, end).usePattern(OPERATOR_PATTERN).lookingAt())
                    {
                        // if an operator occurs, push it onto the operator stack and maintain the state
                        // accepted operator symbols in this state are: + -
                        // also accepted are subexpression starts, indicated by the symbol: (

                        start = matcher.end();
                        final String operator = matcher.group(1);
                        LOGGER.debug("encountered operator symbol '{}'", operator);

                        switch (operator)
                        {
                        case "+":
                        {
                            // unary plus is valid, but does nothing
                            //
                            LOGGER.debug("ignoring unary + operator symbol (no-op)");
                            break;
                        }
                        case "-":
                        {
                            // unary minus (NOT)
                            //
                            pushOperator(Operator.ABSOLUTE_COMPLEMENT);
                            break;
                        }
                        case "(":
                        {
                            // left parenthesis starts a new subexpression
                            //
                            LOGGER.debug("pushing subexpression onto operator stack");
                            operators_.push(Operator.SUBEXPRESSION_START);
                            break;
                        }
                        default:
                            throw new RuntimeException(String.format("element expected, invalid operator '%s' encountered", operator));
                        }
                    }
                    else
                    {
                        throw new RuntimeException("element expected, unrecognized characters encountered");
                    }

                    break;
                }
                case EXPECT_OPERATOR:
                {
                    // EXPECT_OPERATOR state applies after an element or subexpression
                    // only binary operators and subexpression ends are valid in this state

                    if (matcher.region(start, end).usePattern(OPERATOR_PATTERN).lookingAt())
                    {
                        // if an operator occurs, push it onto the operator stack and advance the state
                        // accepted operator symbols in this state are: , + -
                        // also accepted are subexpression ends (with no state advance), indicated by the symbol: )

                        start = matcher.end();
                        final String operator = matcher.group(1);
                        LOGGER.debug("encountered operator symbol '{}'", operator);

                        switch (operator)
                        {
                        case ",":
                        {
                            // binary comma (OR)
                            //
                            pushOperator(Operator.UNION);
                            state = State.EXPECT_ELEMENT;
                            break;
                        }
                        case "+":
                        {
                            // binary plus (AND)
                            //
                            pushOperator(Operator.INTERSECTION);
                            state = State.EXPECT_ELEMENT;
                            break;
                        }
                        case "-":
                        {
                            // binary minus (AND NOT)
                            //
                            pushOperator(Operator.RELATIVE_COMPLEMENT);
                            state = State.EXPECT_ELEMENT;
                            break;
                        }
                        case ")":
                        {
                            // right parenthesis ends a subexpression
                            //
                            LOGGER.debug("popping subexpression from operator stack");

                            // pop all operators off the stack until the most recent subexpression start is reached
                            //
                            while (!operators_.isEmpty() && operators_.peek() != Operator.SUBEXPRESSION_START)
                            {
                                processOperator(operators_.pop());
                            }

                            if (operators_.isEmpty())
                            {
                                throw new RuntimeException("unbalanced right parenthesis in expression");
                            }

                            // pop and discard the subexpression start
                            //
                            operators_.pop();
                            break;
                        }
                        default:
                            throw new RuntimeException(String.format("invalid operator '%s' encountered", operator));
                        }
                    }
                    else
                    {
                        throw new RuntimeException("operator expected, unrecognized characters encountered");
                    }

                    break;
                }
                default:
                    throw new AssertionError(String.format("unrecognized parser state '%s'", state));
                }
            }

            // end of expression processing
            //
            LOGGER.debug("end of expression encountered while in state '{}'", state);

            switch (state)
            {
            case EXPECT_ELEMENT:
            {
                // the final state should only be EXPECT_ELEMENT if the expression was empty
                //
                if (!nodes_.isEmpty() || !operators_.isEmpty())
                {
                    throw new RuntimeException("expression incomplete");
                }

                // create a criterion that, much like my mother-in-law, is never satisfied
                //
                LOGGER.debug("creating degenerate (empty) criteron object");
                break;
            }
            case EXPECT_OPERATOR:
            {
                // the typical final state is EXPECT_OPERATOR
                // pop and process any remaining operators from the stack, and (hopefully) only the root node remains
                //
                LOGGER.debug("popping all operators from stack");

                while (!operators_.isEmpty())
                {
                    final Operator operator = operators_.pop();

                    if (operator == Operator.SUBEXPRESSION_START)
                    {
                        throw new RuntimeException("unbalanced left parenthesis in expression");
                    }

                    processOperator(operator);
                }

                if (nodes_.isEmpty())
                {
                    throw new AssertionError("no nodes remaining on final stack");
                }
                else if (nodes_.size() > 1)
                {
                    throw new AssertionError("unresolved nodes remaining on final stack");
                }

                LOGGER.debug("creating criteron object from fully resolved node tree");
                root_node = nodes_.pop();
                break;
            }
            default:
                throw new AssertionError(String.format("unrecognized parser end state '%s'", state));
            }

            // make a new criterion from the constructed root node
            //
            return new RuinVennCriterion(root_node);
        }

        // parser node and operator stacks
        //
        private Deque<Node> nodes_;
        private Deque<Operator> operators_;

        // there are four operators (and one pseudo-operator):
        // - ABSOLUTE_COMPLEMENT (aka NOT), represented by a unary prefix minus
        // - INTERSECTION (aka AND), represented by a binary infix plus
        // - RELATIVE_COMPLEMENT (aka AND NOT), represented by a binary infix minus
        // - UNION (aka OR), represented by a binary infix comma
        // each operator has a precedence, defining the order of evaluation (i.e., from lowest level to highest)
        // the subexpression start marker (left parenthesis) acts as a special operator of highest level precedence
        //
        private enum Operator
        {
            ABSOLUTE_COMPLEMENT(Precedence.COMPLEMENT),
            INTERSECTION(Precedence.INTERSECTION),
            RELATIVE_COMPLEMENT(Precedence.INTERSECTION),
            UNION(Precedence.UNION),
            SUBEXPRESSION_START(Precedence.NONE);

            // compare precedence of this operator to that of another
            //
            public boolean precedes(Operator other_operator)
            {
                return precedence_.precedes(other_operator.precedence_);
            }

            // precedence defines order of operator evaluation and associativity (left vs. right)
            // by convention, highest precedence is level 1, then level 2, and so on
            //
            private enum Precedence
            {
                COMPLEMENT(3, false),
                INTERSECTION(14, true),
                UNION(15, true),
                NONE(Integer.MAX_VALUE, false);

                // compare this precedence with another
                // lower level wins; if tied, this one wins if left associative, or loses if right associative
                //
                public boolean precedes(Precedence other_precedence)
                {
                    return level_ < other_precedence.level_ || level_ == other_precedence.level_ && left_associative_;
                }

                // precedence level and associativity
                //
                private final int level_;
                private final boolean left_associative_;

                private Precedence(int level, boolean left_associative)
                {
                    level_ = level;
                    left_associative_ = left_associative;
                }
            }

            // operator precedence
            //
            private final Precedence precedence_;

            private Operator(Precedence precedence)
            {
                precedence_ = precedence;
            }
        }

        // parser states
        //
        private enum State { EXPECT_ELEMENT, EXPECT_OPERATOR }

        // expression field parsing regex patterns
        //
        private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s*+(?:#.*+)?+");
        private static final Pattern ELEMENT_PATTERN = Pattern.compile("(?:(\\w++)|\"((?:[^\\\\\"]++|\\\\[tbnrf'\"\\\\])*+)\")");
        private static final Pattern OPERATOR_PATTERN = Pattern.compile("([(),+-])");

        // push an operator onto the stack
        // first, though, any higher precedence operators on top of the stack must be popped off and processed
        //
        private void pushOperator(Operator operator)
        {
            LOGGER.debug("pushing operator '{}' onto operator stack", operator);

            while (!operators_.isEmpty() && operators_.peek().precedes(operator))
            {
                LOGGER.debug("removing higher precedence operator from stack");
                processOperator(operators_.pop());
            }

            operators_.push(operator);
        }

        // process an operator
        // pop nodes from the stack, combine them as appropriate to construct a new node, and push that onto the stack
        //
        private void processOperator(Operator operator)
        {
            LOGGER.debug("processing operator '{}'", operator);

            switch (operator)
            {
            case ABSOLUTE_COMPLEMENT:
            {
                // replace the top node of the stack with a complement node built from it
                //
                if (nodes_.size() < 1)
                {
                    throw new AssertionError(String.format("insufficient operands for unary operator '%s'", operator.toString()));
                }

                final Node node = nodes_.pop();
                nodes_.push(new ComplementNode(node));
                break;
            }
            case INTERSECTION:
            {
                // replace the top two nodes of the stack with an intersection node built from them
                //
                if (nodes_.size() < 2)
                {
                    throw new AssertionError(String.format("insufficient operands for binary operator '%s'", operator.toString()));
                }

                final Node right_node = nodes_.pop();
                final Node left_node = nodes_.pop();
                nodes_.push(new IntersectionNode(left_node, right_node));
                break;
            }
            case RELATIVE_COMPLEMENT:
            {
                // replace the top two nodes of the stack with an intersection node built from the first and the
                // complement of the second
                //
                if (nodes_.size() < 2)
                {
                    throw new AssertionError(String.format("insufficient operands for binary operator '%s'", operator.toString()));
                }

                final Node right_node = nodes_.pop();
                final Node left_node = nodes_.pop();
                nodes_.push(new IntersectionNode(left_node, new ComplementNode(right_node)));
                break;
            }
            case UNION:
            {
                // replace the top two nodes of the stack with an union node built from them
                //
                if (nodes_.size() < 2)
                {
                    throw new AssertionError(String.format("insufficient operands for binary operator '%s'", operator.toString()));
                }

                final Node right_node = nodes_.pop();
                final Node left_node = nodes_.pop();
                nodes_.push(new UnionNode(left_node, right_node));
                break;
            }
            default:
                throw new AssertionError(String.format("no processing defined for operator '%s'", operator.toString()));
            }
        }
    }
}
