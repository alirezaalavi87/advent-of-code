#!/usr/bin/env clojure

;; The task: Each row in input is a report and each value in each report is
;;a level. SAFE reports are ones that are either all increasing or all
;;decreasing, AND the difference between two adjacent levels are AT MOST 3
;; How many SAFE reports are in this input?

(ns main
  (:require [clojure.string :as str])
  (:gen-class))

;; extract each report into vector
(def input-reports
  (->> (slurp "./input.txt")
       (str/split-lines)
       (map #(str/split % #" "))
       (map #(map parse-long %))))

;;;;;;;;;;;;;;;;;
;     PART1     ;
;;;;;;;;;;;;;;;;

;; If all records are either increasing or decreasing
(defn monotonic-diffs? [diffs]
  (or (every? pos? diffs) (every? neg? diffs)))

;; If all diffs are within the specified bounds
(defn diffs-within-bounds? [diffs]
  (every? #(and (<= (abs %) 3) (>= (abs %) 1)) diffs))

;; Check if report is safe
;; 1. The levels are either all increasing or all decreasing.
;; 2. Any two adjacent levels differ by at least one and at most three.
(defn safe-report? [report]
  ; get the diff of 1st element with 2nd, 3rd with 4th and so on...
  (let [diffs (map #(- %2 %1) report (rest report))]
    (and (monotonic-diffs? diffs) (diffs-within-bounds? diffs))))

;; count the safe reports
(defn count-safe-reports [reports]
  (count (filter safe-report? reports)))

;;;;;;;;;;;;;;;;;
;     PART2     ;
;;;;;;;;;;;;;;;;
;; Part2 is the same as part1 but now we have tolerance for one bad level.

;; METHOD:
;; Check if report is safe like PART1
;; If report is unsafe, start removing each element. if it becomes safe by removing an element,
;;The report is safe. if not, the report is not safe
; (defn safe-report-with-removal? [report])

(println (count-safe-reports input-reports))
