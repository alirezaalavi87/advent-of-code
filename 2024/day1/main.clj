#!/usr/bin/env clojure

;; The task is to get the total difference between the values of left column and right column of input
;; The task says the you should sort them first, and substract each column and add the total difference
;; but adding all of left col and adding all of right col and then substracting returns the same results
;; substracting each row unsorted also returns the same results (basic maths)

(ns main
  (:require [clojure.string :as str])
  (:gen-class))

; prepare input
(def input-data
  (->> (slurp "./input.txt");read data from file
       ;split by lines
       (str/split-lines)
       ;split each line into two strings separated by spaces
       (map #(str/split % #"   "))
       ;convert string to long int
       (map #(map parse-long %))
   ))

;; Calculate difference between two lists
;; METHOD: split columns(lists), sort each column and then substract, and add the absolute differences.

(def first-col-sorted
  (sort (map #(first %) input-data)))

(def second-col-sorted
  (sort (map #(last %) input-data)))

(def total-difference
  (apply +
         (map Math/abs
              (map - first-col-sorted second-col-sorted))))

; Calculate everything at once. sort each list once instead of twice
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

;; Calculate similarity score between two lists
;; HOW: multiply each number in left list by the times that it appeared in the right list.
;;then add them all up.
;; METHOD1: for x in sorted list1, iterate all items y in sorted list2 until y>x
;;and count how many times x=y  [O(n+m)]
;; METHOD2: create hash map that stores the frequency of each number in list2,
;;for each number, if number exists as key in the hash map, increment it's value,
;;if not, add it with count 1.
;;Iterate through list1, and check if number exists in frequency hash map, and add the value the
;;total similarity score. (no need for sorting) [O(n+m)]

(defn calculate-similarity-score [left-list right-list]
  (let [right-freqs (frequencies right-list)]
    ; Count how many times each item in left-list occured in right-list
    ;and calculate the similarity score
    (reduce
     (fn [score left-val]
       (let [count (get right-freqs left-val 0)]
         (+ score (* left-val count))))

     0
     left-list)))

(defn -main
  []
  (println "difference score:" total-difference-2)
  (println "similarity score:" (calculate-similarity-score first-col-sorted second-col-sorted)))

(-main)
