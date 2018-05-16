package stroom.dashboard.expression.v1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

class Evaluator {
    private final List<java.util.function.Function<Val[], Optional<Val>>> evaluationFunctions;

    static final Optional<Val> OPT_TRUE_VALUE = Optional.of(ValBoolean.TRUE);
    static final Optional<Val> OPT_FALSE_VALUE = Optional.of(ValBoolean.FALSE);
    static final Optional<Val> OPT_NULL_VALUE = Optional.of(ValNull.INSTANCE);

    // for using in logging and ValErr messages
    private final String name;

    private Evaluator(final List<java.util.function.Function<Val[], Optional<Val>>> evaluationFunctions,
                      final String name) {
        this.evaluationFunctions = evaluationFunctions;
        this.name = name;
    }

    static EvaluationBuilder builder(final String name) {
        return new EvaluationBuilder(name);
    }

    /**
     * Evaluates the value of each childGenerator, then passes those values to each of the
     * evaluation functions. The evaluation functions are applied in the order in which they
     * were added to the builder. As soon as one of the functions returns a populated {@link Optional}
     * the evaluate method will return the contents of that {@link Optional} or a ValErr if it
     * is empty.
     *
     * @param childGenerators An array of child generators
     * @return The value resulting from applying all the evaluation functions.
     */
    Val evaluate(final Generator... childGenerators) {
        final Val[] values = new Val[childGenerators.length];
        for (int i = 0; i < childGenerators.length; i++) {
            try {
                values[i] = childGenerators[i].eval();
            } catch (RuntimeException e) {
                values[i] = ValErr.create("Error evaluating generator" +
                        childGenerators[i].getClass().getCanonicalName() + ": " + e.getMessage());
            }
        }

        Optional<Val> retVal = Optional.empty();
        int i = 0;
        for (Function<Val[], Optional<Val>> func : evaluationFunctions) {
            if (!retVal.isPresent()) {
                // func return no value so try next one
                try {
                    retVal = func.apply(values);
                } catch (RuntimeException e) {
                    retVal = Optional.of(ValErr.create(String.format("Error applying evaluation function %s to %s: %s",
                            i, Arrays.toString(values), e.getMessage())));
                }
            } else {
                // got a value so break out
                break;
            }
            i++;
        }
        return retVal.orElse(ValErr.create(String.format("No value after %s evaluation functions", evaluationFunctions.size())));
    }

    static class EvaluationBuilder {
        private final String name;
        private final List<Function<Val[], Optional<Val>>> evaluationFunctions = new ArrayList<>();

        private EvaluationBuilder(final String name) {
            this.name = name;
        }

        /**
         * Add a custom evaluation function that maps an array of values from the child generators to
         * an optional Val
         */
        EvaluationBuilder addEvaluationFunction(Function<Val[], Optional<Val>> evaluationFunction) {
            evaluationFunctions.add(Objects.requireNonNull(evaluationFunction));
            return this;
        }

        /**
         * Add an evaluation function that will return a ValErr if any of the values from the child generators
         * evaluate to ValErr
         */
        EvaluationBuilder addReturnErrorOnFirstErrorValue() {
            evaluationFunctions.add(EvaluationBuilder::addReturnErrorOnFirstErrorValue);
            return this;
        }

        /**
         * Add an evaluation function that will return a ValErr if any of the values from the child generators
         * evaluate to ValNull
         */
        EvaluationBuilder addReturnErrorOnFirstNullValue() {
            evaluationFunctions.add(EvaluationBuilder::addReturnErrorOnFirstNullValue);
            return this;
        }

        /**
         * Adds an evaluation function that will attempt to convert the single child generator's value to a string
         * and then apply the string mapping function to it.
         * @param stringMapper A function to map a string to another string. If allowNullStringValue is true, the
         *                     function should support a null input. A null output will ultimately result in a ValNull.
         * @param allowNullStringValue If true values that cannot be converted to a string will be passed to the
         *                             mapper as a null, else a ValErr will be returned.
         * @param mapNullValues If true null strings will be passed to the stringMapper
         * @return The result of stringMapper as a ValString, a ValNull or a ValErr.
         */
        EvaluationBuilder addStringMapper(final Function<String, String> stringMapper,
                                          final boolean allowNullStringValue,
                                          final boolean mapNullValues) {
            evaluationFunctions.add(values -> {
                if (values.length > 1) {
                    throw new IllegalArgumentException(
                            String.format("Expecting only one value for this function, received %s",
                                    Arrays.toString(values)));
                }

                final String valStr = values[0].toString();


                if (!allowNullStringValue && valStr == null) {
                    return Optional.of(ValErr.create("Unable to convert argument to string in function " + name));
                }

                try {
                    String resultStr = mapNullValues ? stringMapper.apply(valStr) : null;
                    if (resultStr == null) {
                        return Evaluator.OPT_NULL_VALUE;
                    } else {
                        return Optional.of(ValString.create(resultStr));
                    }
                } catch (RuntimeException e) {
                    return Optional.of(ValErr.create(
                            String.format("Error applying string mapping function %s to %s, %s",
                                    name, valStr, e.getMessage())));
                }
            });
            return this;
        }

        Evaluator build() {
            return new Evaluator(evaluationFunctions, name);
        }

        private static Optional<Val> addReturnErrorOnFirstErrorValue(Val... values) {
            Optional<Val> result = Optional.empty();
            for (Val val : values) {
                if (Objects.requireNonNull(val) instanceof ValErr) {
                    result = Optional.of(val);
                    break;
                }
            }
            return result;
        }

        private static Optional<Val> addReturnErrorOnFirstNullValue(Val... values) {
            Optional<Val> result = Optional.empty();
            for (Val val : values) {
                if (Objects.requireNonNull(val) instanceof ValNull) {
                    result = Optional.of(ValErr.create("All values must be non-null " + Arrays.toString(values)));
                    break;
                }
            }
            return result;
        }
    }
}
