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

## Max
Determines the maximum value given in the args
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
These are aggregation functions

## Count
```
count()
```

## Count Groups
```
countGroups()
```

# String Functions

## Replace
1. A regex
2. The string to replace
3. The replacement string
```
replace(input, findThis, replaceWithThis)
```

Example
```
replace('this', 'is', 'at')

>> 'that'
```

## Concatenate
Appends all the arguments end to end in a single string
```
concat(args...)
```

Example
```
concat('this ', 'is ', 'how ', 'it ', 'works')
>> 'this is how it works'
```

## String Length
Takes the length of a string
```
stringLength(aString)
```

Example
```
stringLength('hello')
>> 5
```

## Upper Case
Converts the string to upper case
```
upperCase(aString)
```

Example
```
upperCase('Hello DeVeLoPER')
>> HELLO DEVELOPER
```

## Lower Case
Converts the string to lower case
```
lowerCase(aString)
```

Example
```
lowerCase('Hello DeVeLoPER')
>> hello developer
```

## Substring
Take a substring based on start/end index of letters
```
substring(aString, startIndex, endIndex)
```

Example
```
substring('this', 1, 2)
>> 'h'
```

## Decode
The arguments are split into 3 parts
1. The input value to test
2. Pairs of regex matchers with their respective output value
3. A default result, if the input doesn't match any of the regexes

```
decode(input, test1, result1, test2, result2, ... testN, resultN, otherwise)
```

It works much like a Java Switch/Case statement

Example
```
decode(${val}, 'red', 'rgb(255, 0, 0)', 'green', 'rgb(0, 255, 0)', 'blue', 'rgb(0, 0, 255)', 'rgb(255, 255, 255)')
${val}='blue'
> rgb(0, 0, 255)
${val}='green'
> rgb(0, 255, 0)
${val}='brown'
> rgb(255, 255, 255) // falls back to the 'otherwise' value
```

in Java, this would be equivalent to
```java

String decode(value) {
    switch(value) {
        case "red":
            return "rgb(255, 0, 0)"
        case "green":
            return "rgb(0, 255, 0)"
        case "blue":
            return "rgb(0, 0, 255)"
        default:
            return "rgb(255, 255, 255)"
    }
}

decode('red')
> 'rgb(255, 0, 0)'

```