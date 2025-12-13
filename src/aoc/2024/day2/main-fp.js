/*****************************
 *  Run file with `deno main.js`
 *  or use the repl like `deno repl --eval-file=main-fp.js`
 * ***************************/


const REPORTS_OK = [
    [1, 2, 4, 3, 5],
    [1, 3, 4, 7],
    [4, 1, 5, 6],
    [6, 2, 7, 9],
    [9, 7, 5, 2, 3]
];

const REPORTS_BAD = [
    [10, 11, 8, 7, 3],
    [6, 2, 7, 9, 8],
    [1, 3, 5, 4, 7, 7]
];

const inputReports = Deno.readTextFileSync('./input.txt', 'utf-8')
    .split('\n') // Split the content into lines
    .filter(line => line.trim() !== '') // Filter out empty lines
    .map(line => line.split(' ')) // Split each line into words
    .map(arr => arr.map(Number)); // Convert each word to a number

/**
* Get diff of every level with it's next level
* @param {number[]} arr
*/
const getDiffs = (arr) =>
    arr.slice(1).map((val, index) => val - arr[index]);

/**
 * Check if a given report is safe.
 *
 * For a report to be safe, it must:
 * - The levels are either all increasing or all decreasing.
 * - Any two adjacent levels differ by at least one and at most three.
 *
 * @param {number[]} report
 */
const isReportSafe = (report) => {
    const diffs = getDiffs(report);

    // if all records are not either increasing or decreasing
    const isMonotonic = diffs.every(x => x > 0) || diffs.every(x => x < 0);
    const isWithinBounds = diffs.every(d => Math.abs(d) >= 1 && Math.abs(d) <= 3);

    return isMonotonic && isWithinBounds;
};

/**
 * is the report safe if a level is removed?
 *
 * Complexity of this solution is O(n^m) where n is the length of the report and m is the number of
 * faults to tolerate +1
 *
 * @param {number[]} report
 * @returns {boolean}
 */
const isReportSafeWithRemoval = (report) => {
    // If the report is already safe
    if (isReportSafe(report)) {
        return true;
    }

    // Check by removing one element at a time
    return report.some((_, index) => {
        // Create a new report without the i-th element
        const reportNew = report.filter((_, i) => i !== index);
        // See if it is safe after the removal
        return isReportSafe(reportNew);
    });
}

/** @type {number} */
const safeReportsCount = inputReports.filter(isReportSafe).length

/** @type {number} */
const safeReportsWithRemovalCount = inputReports.filter(isReportSafeWithRemoval).length

console.info(safeReportsCount);
console.info(safeReportsWithRemovalCount);
