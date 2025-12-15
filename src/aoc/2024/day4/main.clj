;; The goal is to find all instances of "XMAS" in the input.(count)
;;The word can be written in horizontal,vertical and diagonal, forwards and backwards.

;; Method: (brute force, basically)
;;Iterate over the input(keep it's geometry in mind), For each element:
;;  - Get the 3 elements to it's right
;;  - Get the 3 elements below it
;;  - Get the 3 elements diagonal to it (right-below)
;; Now we have 3 sets of 4 elements. We check if they match "XMAS" forwards or backwards, if yes, we add to the count

(ns day4.main
  (:require [clojure.string :as str])
  (:require [util.main :as util])
  (:gen-class))

(def input-lines
  (str/split (slurp "src/aoc/2024/day4/input-test.txt") #"\n"))

(def input-map
  "Map the grid points to their position"
  ;sort just for coherence
  (sort (util/grid->point-map input-lines)))
