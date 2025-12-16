;; Helper Functions of AoC

(ns aoc.util.main
  (:require [clojure.data.int-map :as i]))

;; Thanks to narimiran/aoc2024
(defn grid->point-map
  "Converts a 2D grid (vector of vectors) into a map of points based on a predicate.
   The keys can be unique integers (if a multiplier is provided) or coordinate vectors.

   Parameters:
   - v: The 2D grid vector.
   - pred: A predicate function to filter characters (default: identity).
   - mult: An optional multiplier for generating unique keys."

  ;; Overload for a single argument: grid vector.
  ([v] (grid->point-map v identity nil))
  ;; Overload for two arguments: grid vector and a predicate function.
  ([v pred] (grid->point-map v pred nil))
  ;; Main function with three arguments: grid vector, predicate function, and an optional multiplier.
  ([v pred mult]
   ;; Constructs a map by filtering and transforming the grid based on the predicate.
   (into (if mult (i/int-map) {})
         (for [[^long y line] (map-indexed vector v) ;; Iterate over each row with its index.
               [^long x char] (map-indexed vector line) ;; Iterate over each character in the row with its index.
               :when (pred char)] ;; Filter characters based on the provided predicate.
           (if mult
             ;; If a multiplier is provided, create a unique key based on the row and column indices.
             [(+ (* y ^long mult) x) char]
             ;; If no multiplier, use the coordinates as the key
             [[x y] char])))))

