#!/usr/bin/env clojure

;; The goal of the input is to multiply numbers with this exact syntax: `mul(number, number)`
;; But since it is corrupted, there are other characters and invalid `mul` instructions.
;; Extract the correct `mul` instructions, from the text, do the multiplications, and add them all up.

(ns main
  (:gen-class))

;; Read content of file
(def input (slurp "2024/day3/input.txt"))

(def valid-instructions
  (re-seq #"mul\([0-9]*,[0-9]*\)" input))

;; Takes string v in form of "mul(int,int)" and produces list of (long, long)
(defn parse-numbers [v]
  (map #(Long/parseLong %) (re-seq #"\d+" v)))

(def valid-instructions-parsed
  (map parse-numbers valid-instructions))

(def multiplication-values
  (map #(apply * %) valid-instructions-parsed))

(def mulitiplications-sum
  (apply + multiplication-values))
