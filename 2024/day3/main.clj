#!/usr/bin/env clojure

;; The goal of the input is to multiply numbers with this exact syntax: `mul(number, number)`
;; But since it is corrupted, there are other characters and invalid `mul` instructions.
;; Extract the correct `mul` instructions, from the text, do the multiplications, and add them all up.

(ns main
  (:require [clojure.string :as str])
  (:gen-class))

(defn get-valid-instructions [s]
  (re-seq #"mul\([0-9]*,[0-9]*\)" s))

;; Takes string in form of "mul(int,int)" and produces list of (long, long)
(defn parse-numbers [s]
  (if (not (string? s))
    (throw (IllegalArgumentException. "input must be a string"))
    (map #(Long/parseLong %) (re-seq #"\d+" s))))

(defn remove-nils [l]
  (remove nil? l))

;; Read content of file
; adds the implicit do() to the beginning, we'll see why in part2
(def input (str "do()" (slurp "2024/day3/input.txt")))

(def valid-instructions
  (get-valid-instructions input))

(def valid-instructions-parsed
  (map parse-numbers valid-instructions))

;; Multiply each pair
(def valid-muls-results
  (map #(apply * %) valid-instructions-parsed))

(def sum-of-muls
  (apply + valid-muls-results))

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

(defn parse-valid-from-dont-split [s]
  ; Parse instructions into numbers
  (map #(parse-numbers %)
       (flatten
        (remove-nils
         ; Get the valid instructions from all the remaining splits
         (map
          #(get-valid-instructions %)
          ; Remove the first part of the split: the dont() instructions
          (rest
           (split-dos s)))))))

(def valid-instructions-parsed2
  (map parse-valid-from-dont-split (split-donts input)))

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

(def sum-of-muls2
  (apply + valid-muls-results2))
