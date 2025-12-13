// Read input by lines. pop the last empty split
const inputLines = Deno.readTextFileSync("./input-test.txt", "utf-8")
    .split("\n")
    .filter((l) => l.trim() != "");

for (let i = 0; i < inputLines.length; i++) {
    for (let j = 0; j < inputLines[i].length; j++) {
        if (inputLines[i][j] === "X") {
            //look "around" for M
            lookAroundFor("M", i, j, inputLines);
            // console.log(i, j);
        }
    }
}

/**
 * Look "around" the coords `i`,`j` fr the letter `s`
 * By "around it" I mean: if X is at input-lines[i][j] we should look at:
 * input-lines[i][j-1] [i][j+1] [i+1][j-1] [i+1][j] [i+1][j+1] [i-1][j-1] [i-1][j] [i-1][j+1]
 * @param {string} s
 * @param {number} i
 * @param {number} j
 * @param {string[]} input
 * @returns {boolean}
 */
function lookAroundFor(s, i, j, input) {
    let i2 = i == 0 ? 0 : i - 1;
    let j2 = j == 0 ? 0 : j - 1;

    for (i2; i2 <= i + 1; i2++) {
        if (i2 > input.length - 1) break;
        for (j2; j2 <= j + 1; j2++) {
            if (j2 > input[i].length - 1) break;
            console.log("in j2",i2, j2);
        }
    }
}
