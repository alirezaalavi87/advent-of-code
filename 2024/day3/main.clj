#!/usr/bin/env clojure

;; The goal of the input is to multiply numbers with this exact syntax: `mul(number, number)`
;; But since it is corrupted, there are other characters and invalid `mul` instructions.
;; Extract the correct `mul` instructions, from the text, do the multiplications, and add them all up.

;;;; Utility
(ns main
  (:require [clojure.string :as str])
  (:gen-class))

(defn extract-mul-instructions [s]
  (re-seq #"mul\([0-9]*,[0-9]*\)" s))

;; Takes string in form of "mul(int,int)" and produces list of (long, long)
(defn parse-instruction-nums [s]
  (when (string? s)
    (map #(Long/parseLong %) (re-seq #"\d+" s))))

(defn sum-results [results]
  (apply + results))

;;;;;;;;;;;;;;;;;
;;  PART 1     ;;
;;;;;;;;;;;;;;;;

;; Read content of file
; adds the implicit do() to the beginning, we'll see why in part2
(def input (str "do()" (slurp "2024/day3/input.txt")))

(def valid-muls
  (extract-mul-instructions input))

(def valid-muls-parsed
  (map parse-instruction-nums valid-muls))

;; Multiply each pair
(def mul-results
  (map #(apply * %) valid-muls-parsed))

;; Final result of part 1
(def sum-of-muls
  (sum-results mul-results))

;;;;;;;;;;;;;;;
;; PART 2    ;;
;;;;;;;;;;;;;;
;; Execute mul() instructions from beginning up to a don't() instruction. enable execution again after a do() instruction
;;and so on.
;;
;; Method:
;;- Add the implicit do() instruction to the beginning of the input
;;- split input by `don't()` instructions -> at the end of every split there will be a don't() instruction
;;- split each dont-split by do() and remove the first split -> the dont'() up to do() part is deleted and only do() parts remain
;;- calculate the remaining like part1

(defn split-donts [s]
  (str/split s #"don't\(\)"))

(defn split-dos [s]
  (str/split s #"do\(\)"))

(defn parse-valid-instructions-from-dont-split [s]
  "From a dont-split, meaning a split that ends in a don't() and before it there was a don't(),
  find and parse the valid instructions that where after do() instructions."
  (->> (split-dos s)
       rest
       (map extract-mul-instructions)
       flatten
       (map parse-instruction-nums)))

(def valid-instructions-parsed2
  (map parse-valid-instructions-from-dont-split (split-donts input)))

(def valid-muls-results2
  "Since valid-instructions-parsed2 is a nested list,
  ```
  (((1 2)(3 4))()((5 6)(7 8)))
  ```
  we will iterate twice
  On it's content. Once getting the inner list that contains the pairs and once
  multiplying the actual pairs"
  (flatten (map (fn [inner-list]
                  (map (fn [pair]
                         (apply * pair))
                       inner-list))
                valid-instructions-parsed2)))

;; Final result of part2
(def sum-of-muls2
  (sum-results valid-muls-results2))

(println "sum of part1:" sum-of-muls)
(println "sum of part2:" sum-of-muls2)
