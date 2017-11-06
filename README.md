# stroom-expression

[![Build Status](https://travis-ci.org/gchq/stroom-expression.svg?branch=master)](https://travis-ci.org/gchq/stroom-expression)

This library provides Stroom with the functions for manipulating data on Stroom Dashboards.

Each function has a name, and some have additional aliases.

# Math Functions

## Add
```
arg1 + arg2
```
Or reduce the args by successive addition
```
add(args...)
```

## Subtract
```
arg1 - arg2
```
Or reduce the args by successive subtraction
```
subtract(args...)
```

## Multiply
Multiplies arg1 by arg2
```
arg1 * arg2
```
Or reduce the args by successive multiplication
```
multiply(args...)
```

## Divide
Divides arg1 by arg2
```
arg1 / arg2
```
Or reduce the args by successive division
```
divide(args...)
```

## Power
Raises arg1 to the power arg2
```
arg1 ^ arg2
```
Or reduce the args by successive raising to the power
```
power(args...)
```

## Negate
Multiplies arg1 by -1
```
negate(arg1)
```

## Equals
Evaluates if arg1 is equal to arg2
```
arg1 = arg2
equals(arg1, arg2)
```

## Greater Than
Evaluates if arg1 is greater than to arg2
```
arg1 > arg2
greaterThan(arg1, arg2)
```

## Less Than
Evaluates if arg1 is less than to arg2
```
arg1 < arg2
lessThan(arg1, arg2)
```

## Greater Than or Equal To
Evaluates if arg1 is greater than or equal to arg2
```
arg1 >= arg2
greaterThanOrEqualTo(arg1, arg2)
```

## Less Than or Equal To
Evaluates if arg1 is less than or equal to arg2
```
arg1 <= arg2
lessThanOrEqualTo(arg1, arg2)
```
## Random
Generates a random number between 0.0 and 1.0
```
random()
```

# Aggregation Functions
Determines the maximum value given in the args
## Max
```
max(args...)
```

## Min
Determines the minimum value given in the args
```
min(args...)
```

## Sum
Sums all the arguments together
```
sum(args...)
```

## Average
Takes an average value of the arguments
```
average(args...)
mean(args...)
```

# Rounding Functions

These functions require a value, and an optional decimal places.
If the decimal places are not given it will give you nearest whole number.


## Ceiling
```
ceiling(value, decimalPlaces<optional>)
```

## Floor
```
floor(value, decimalPlaces<optional>)
```

## Round
```
round(value, decimalPlaces<optional>)
```

## Ceiling Year/Month/Day/Hour/Minute/Second
```
ceilingYear(args...)
ceilingMonth(args...)
ceilingDay(args...)
ceilingHour(args...)
ceilingMinute(args...)
ceilingSecond(args...)
```

## Floor Year/Month/Day/Hour/Minute/Second
```
floorYear(args...)
floorMonth(args...)
floorDay(args...)
floorHour(args...)
floorMinute(args...)
floorSecond(args...)
```

## Round Year/Month/Day/Hour/Minute/Second
```
roundYear(args...)
roundMonth(args...)
roundDay(args...)
roundHour(args...)
roundMinute(args...)
roundSecond(args...)
```

# Counting Functions

## Count
```
count(args...)
```

## Count Groups
```
countGroups(args...)
```

# String Functions

## Replace
```
replace(args...)
```

## Concatenate
```
concat(args...)
```

## String Length
```
stringLength(args...)
```

## Upper Case
```
upperCase(args...)
```

## Lower Case
```
lowerCase(args...)
```

## Substring
```
substring(args...)
```

## Decode
```
decode(args...)
```

