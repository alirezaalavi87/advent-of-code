#!/usr/bin/env clojure

;; The task is to get the total difference between the values of left column and right column of input
;; The task says the you should sort them first, and substract each column and add the total difference
;; but adding all of left col and adding all of right col and then substracting returns the same results
;; substracting each row unsorted also returns the same results (basic maths)

(ns main
  (:require [clojure.string :as str])
  (:gen-class))

(defn parse-int [s]
  (Integer/parseInt s))

; prepare input:
; read data from file, split by lines, split each line into two strings separated by spaces,
; convert string to int
(def input-data
  (->> (slurp "./input.txt")
       (str/split-lines)
       (map #(str/split % #"   "))
       (map #(map parse-int %))))

;; METHOD: split columns(lists), sort each column and then substract, and add the absolute differences.

(def first-col-sorted
  (sort (map #(first %) input-data)))

(def second-col-sorted
  (sort (map #(last %) input-data)))

(def total-difference
  (apply +
         (map Math/abs
              (map - first-col-sorted second-col-sorted))))

;; Calculate everything at once. sort each list once instead of twice
(def total-difference-2
  (let [[first-col second-col]
        ;transpose data into two vectors, one containing all first-col elements
        ; and one containing second-col elements
        (apply map vector input-data)
        ;sort
        first-col-sorted (sort first-col)
        second-col-sorted (sort second-col)]
    ;add all differences together
    (apply +
           ;take the absolute value of each substraction, because we are looking
           ; for the difference between two cols
           (map Math/abs
                ;substract cols
                (map - first-col-sorted second-col-sorted)))))

(defn -main
  []
  (println total-difference-2))

(-main)
