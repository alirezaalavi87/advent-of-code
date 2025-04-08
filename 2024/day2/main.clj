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

;; check each level in report:
;;1-is either ascending or all descending
;;2-no diff between two adjacent elements are bigger than 3
(defn safe-report? [report]
  ; get the diff of 1st element with 2nd, 3rd with 4th and so on...
  (let [diffs (map #(- %2 %1) report (rest report))]
    ; all diffs are positive AND the abs is smaller than 3
    ;OR all diffs are negative AND the abs is smaller than 3
    (or (and (every? pos? diffs) (every? #(<= (abs %) 3) diffs))
        (and (every? neg? diffs) (every? #(<= (abs %) 3) diffs)))))

;; count the safe reports
(defn count-safe-reports [reports]
  (count (filter safe-report? reports)))

(println (count-safe-reports input-reports))
