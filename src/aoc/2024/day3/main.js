/*
 * The goal of the input is to multiply numbers with this exact syntax: `mul(number, number)`
 * But since it is corrupted, there are other characters and invalid `mul` instructions.
 * Extract the correct `mul` instructions, from the text, do the multiplications, and add them all up.
 */

/*
 * Method:
 * 1. create a sort of syntax parser to only extract correct sequences (finite automata)
 *  - m -> u -> l -> ( -> [0-9]* -> , -> [0-9]* -> )
 *  - Can use regex: /mul\([0-9]*,[0-9]*\)/gm
 * 2. Feed the extracted instructions straight to the program (call the `mul` functions within the program)
 * 3. Get sum of all multiplications
 */

/**
 * Multiplication function to match the existing instruction in the input
 * @param {number} x
 * @param {number} y
 */
const mul = (x, y) => x * y;

const input = Deno.readTextFileSync("./input.txt", "utf-8");

const regexValidInstruction = /mul\([0-9]*,[0-9]*\)/gm;

const validInstructions = input.match(regexValidInstruction);

/**
 * Evaluate all valid instructions as a js function, and then take sum of all multiplications
 */
const sumOfMul = validInstructions.map((v) => eval(v)).reduce(
    (sum, x) => sum + x,
    0,
);
console.log(sumOfMul);

/*******************
 * PART TWO:
 *
 * - The do() instruction enables future mul instructions.
 * - The don't() instruction disables future mul instructions.
 *
 * Only the most recent do() or don't() instruction applies. At the beginning of the program, mul instructions are enabled.
 ******************/

/*
 * Method:
 * 1. Remove the `don't()` blocks frm the input
 *  - remove from every `don't()` until the `do()`
 * 2. Repeat the steps in part 1, now that all `don't()` blocks are removed
 */

/**
 * At the end of every split, There has been a don't() instruction
 */
const inputSplitByDont = input.split("don't()");

// Remove each split's beginning up to the first do(), except for the first one [0]
// Because those will be the sections between don't() and do() which we want to remove.
const inputWithoutdont = inputSplitByDont.map((s) => {
    const i = s.indexOf("do()");
    return s.substring(i);
}).concat(inputSplitByDont[0]);

const validInstructions2 = inputWithoutdont.map((s) =>
    s.match(regexValidInstruction)
).flat();

const sumOfMul2 = validInstructions2.map((v) => eval(v)).reduce(
    (sum, x) => sum + x,
    0,
);

console.log(sumOfMul2);
