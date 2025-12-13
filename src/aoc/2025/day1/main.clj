;; You have a circular dial (like most safes) from 0 to 99.
;; -The end reaches the start and vice versa: 98,99,0 and 1,0,99
;; The dial starts at 50
;; The input tells you to go in which direction and how many numbers
;; -(Right gets larger, Left gets smaller)
;;
;; The password is the number of times that the resulting number is 0 after an operation.

(ns aoc.2025.day1.main
  (:require
   [clojure.string :as str])
  (:gen-class))

(defn parse-input
  "the input data converted to an array"
  [path]
  (->> (slurp path)
       (str/split-lines)))

(def input-test
  (parse-input "src/aoc/2025/day1/input.test.txt"))

(defn turn-dial
  "
  Starting from `current-val`, turn the dial in `dir` (L is decreasing and R is increasing)
  for the specified `steps`.
  The dial goes from 0 to 99 so 100 will be 0 again.
  "
  [current-val dir steps]
  (case dir
    \L (mod (- current-val (mod steps 100)) 100)
    \R  (mod (+ current-val (mod steps 100)) 100)
    (throw (ex-info
            "Wrong direction"
            {:dir dir
             :type :wrong-arg}))))

(defn run-instruction-with-history
  "
  Run the instructions from `input`, keeping the history of results after each
  instruction.
  "
  [input]

  (vec (reductions (fn [current-val instruction]
                     (let [dir (first instruction)
                           steps (Integer/parseInt (str/join (rest instruction)))]
                       (turn-dial current-val dir steps)))
                   50 ; initial value
                   input)))

(defn count-zeros
  "Count the number of zeroes in an array"
  [array]
  (count (filter zero? array)))

(defn print-help []
  (println "
           Usage: <input-path>

           <path>: The path to the puzzle's input file
           "))

(defn -main
  [& args]
  (when (nil? args)
    (print-help)
    (System/exit 1))
  (let [input-file (first args)
        input (parse-input input-file)
        dial-history (run-instruction-with-history input)
        zero-count (count-zeros dial-history)]
    (println
     "The number of times the dial landed on 0 after an instruction: "
     zero-count)))

;;;;;;;;;;;;;
;; TEST:
;;;;;;;;;;;;;

;; left tests
(assert (= (turn-dial 60 \L 80) 80))
(assert (= (turn-dial 0 \L 120) 80))
(assert (= (turn-dial 60 \L 1430) 30))
(assert (= (turn-dial 15 \L 14030) 85))

;; Right tests
(assert (= (turn-dial 60 \R 80) 40))
(assert (= (turn-dial 96 \R 80) 76))
(assert (= (turn-dial 0 \R 120) 20))
(assert (= (turn-dial 60 \R 1430) 90))
(assert (= (turn-dial 15 \R 1430) 45))

;; End values
(assert (= (count-zeros (run-instruction-with-history input-test)) 3))
