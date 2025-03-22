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

;; METHOD: sort each column and then substract, and add the absolute differences.

(def first-col-sorted
  (sort (map #(first %) input-data)))

(def second-col-sorted
  (sort (map #(last %) input-data)))

(def total-difference
  (apply +
         (map Math/abs
              (map - first-col-sorted second-col-sorted))))

(defn -main
  []
  (println total-difference))

(-main)
