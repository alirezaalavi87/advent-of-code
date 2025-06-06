/**
 * The goal of the input is to multiply numbers with this exact syntax: `mul(number, number)`
 * But since it is corrupted, there are other characters and invalid `mul` instructions.
 * Extract the correct `mul` instructions, from the text, do the multiplications, and add them all up.
 */

/**
 * Method:
 * 1. create a sort of syntax parser to only extract correct sequences (finite automata)
 *  - m -> u -> l -> (-> [0-9]*-> ,-> [0-9]*-> )
 *  - Can use regex: /mul\([0-9]*,[0-9]*\)/gm
 * 2. Feed the extracted instructions straight to the program (call the `mul` functions within the program)
 * 3. Get sum of all multiplications
 */

const input = Deno.readTextFileSync('./input.txt', 'utf-8')

const regexValidInstruction = /mul\([0-9]*,[0-9]*\)/gm;

const validInstructions = input.match(regexValidInstruction);

/**
 * Multiplication function to match the existing instruction in the input
 * @param {number} x
 * @param {number} y
 */
const mul = (x, y) => x * y;

/**
 * Evaluate all valid instructions as a js function, and then take sum of all multiplications
 */
const sumOfMul = validInstructions.map(v => eval(v)).reduce((sum, x) => sum + x, 0)
