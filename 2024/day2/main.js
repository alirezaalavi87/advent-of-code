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

function countSafeReports() {
    return inputReports.filter(isReportSafe).length
}

function countSafeReportsWithRemoval() {
    return inputReports.filter(isReportSafeWithRemoval).length
}

/**
 * Check if a given report is safe.
 *
 * For a report to be safe, it must:
 * - The levels are either all increasing or all decreasing.
 * - Any two adjacent levels differ by at least one and at most three.
 *
 * @param {number[]} report
 */
function isReportSafe(report) {
    // calculate report[i+1] - report[i]
    const diffs = getDiffs(report);

    // if all records are not either increasing or decreasing
    if (!(diffs.every(x => x > 0) || diffs.every(x => x < 0))) {
        return false;
    }

    for (let i = 0; i < diffs.length; i++) {
        const d = diffs[i];

        if (Math.abs(d) > 3 || Math.abs(d) < 1) {
            return false;
        }
    }

    return true;
}

/**
 * is the report safe if a level is removed?
 *
 * Complexity of this solution is O(n^m) where n is the length of the report and m is the number of
 * faults to tolerate +1
 *
 * @param {number[]} report
 */
function isReportSafeWithRemoval(report) {
    // If the report is already safe
    if (isReportSafe(report)) {
        return true;
    }

    // check by removing one element at a time
    for (let i = 0; i < report.length; i++) {
        // Remove the i-th element from the report
        let reportNew = [...report];
        reportNew.splice(i, 1);
        // console.log(reportNew);
        // see if it is safe after the removal
        if (isReportSafe(reportNew)) {
            return true;
        }
    }

    return false;
}

/**
* Get diff of every level with it's next level
* @param {number[]} value
* @returns {number[]}
*/
function getDiffs(value) {
    let diffs = [];

    for (let i = 0; i < value.length - 1; i++) {
        diffs[i] = value[i + 1] - value[i];
    }

    return diffs
}
