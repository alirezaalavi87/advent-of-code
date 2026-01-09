^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(ns aoc.2025.day1.main
  {:nextjournal.clerk/auto-expand-results? true
   :nextjournal.clerk/toc true}
  (:require
   [nextjournal.clerk :as clerk]
   [nextjournal.clerk.viewer :as viewer]
   [clojure.string :as str]
   [criterium.core :as c]))

;; # Day1: Secret Enterance
;;
;; You have a circular dial (like most safes) that goes from 0 to 99.
;; The end reaches the start and vice versa: 98 -> 99 -> 0 and 1 -> 0-> 99\
;; The dial starts at 50\
;; The input tells you to go in which direction and how many numbers.\
;; (Right gets larger, Left gets smaller). We will call this an operation.

^{::clerk/visibility {:code :hide :result :hide}}
(defn defn? [cell]
  (some-> cell :result :nextjournal/value ::clerk/var-from-def deref fn?))
^{::clerk/visibility {:code :hide :result :hide}}
(def custom-cell-viewer
  (update viewer/cell-viewer
          :transform-fn comp
          (clerk/update-val (fn [cell]
                              (update-in cell [:settings ::clerk/visibility]
                                         #(if
                                           (defn? cell) {:result :hide}
                                           %))))))

^{::clerk/visibility {:code :hide :result :hide}
  ::clerk/no-cache true}
(clerk/add-viewers! [custom-cell-viewer])

;; ## Parsing input and mock input
(defn parse-input
  "Parse the input data to vector of strings"
  [path]
  (->> (slurp path)
       (str/split-lines)))

;; The test(mock) input
^{:nextjournal.clerk/visibility {:result :show}}
(def test-input
  (parse-input "src/aoc/2025/day1/input.test.txt"))

;; ## Helper functions
(defn turn-dial
  "Starting from `current-val`, turn the dial in `dir` (L is decreasing and R is increasing)
  for the specified `steps`.
  The dial goes from 0 to 99 so 100 will be 0 again.
  "
  [current-val dir steps]
  (case dir
    \L (mod (- current-val (mod steps 100)) 100)
    \R  (mod (+ current-val (mod steps 100)) 100)
    (throw (ex-info "Wrong direction"
                    {:dir dir
                     :type :wrong-arg}))))

(defn turn-dial-with-history
  "Run the instructions from `input`, keeping the history of results after each
  instruction.
  It keeps history of running `cmd` with the instructions list
  "
  [input]
  (vec (reductions (fn [current-val instruction]
                     (let [dir (first instruction)
                           steps (Integer/parseInt (str/join (rest instruction)))]
                       (turn-dial current-val dir steps)))
                   50 ; initial value
                   input)))

(defn print-help []
  (println "
           Usage: <input-path>

           <input-path>: The path to the puzzle's input file
           "))

;; ## Part 1
;; The password is the number of times that the resulting number is 0 after an operation.
;; - Keep the result after every operation
;; - Count how many times it has landed on 0

(defn count-zeros
  "Count the number of zeros in an array"
  [array]
  (count (filter zero? array)))

(defn part-1 [input]
  (count-zeros (turn-dial-with-history input)))

;; Let's see the result with our test input\
;; Test the result (`nil` means correct)
(assert (= (part-1 test-input) 3))

(c/quick-bench (part-1 (parse-input "src/aoc/2025/day1/input.txt")))
;; ### Benchmarking
;; We'll use the [criterium](https://github.com/hugoduncan/criterium) library for benchmarking.
;; The commands are run in the REPL
;; Benchmark with test data:
;; ```clojure
;; (require '[criterium.core :as c])
;; (c/quick-bench (part-1 test-input))
;; (count test-input) ; 10
;; ```
;; Results:
;; ```
;; Evaluation count : 195798 in 6 samples of 32633 calls.
;; Execution time mean : 3.628046 µs
;; Execution time std-deviation : 699.498474 ns
;; Execution time lower quantile : 3.065177 µs ( 2.5%)
;; Execution time upper quantile : 4.401143 µs (97.5%)
;; Overhead used : 6.574883 ns
;; ```
;; Benchmark with actual input:
;; ```clojure
;; (require '[criterium.core :as c])
;; (c/quick-bench (part-1 (parse-input "src/aoc/2025/day1/input.txt")))
;; (count (parse-input "src/aoc/2025/day1/input.txt")) ; 4446
;; ```
;; Results:
;; ```
;; Evaluation count : 222 in 6 samples of 37 calls.
;; Execution time mean : 3.395790 ms
;; Execution time std-deviation : 686.534780 µs
;; Execution time lower quantile : 2.705494 ms ( 2.5%)
;; Execution time upper quantile : 4.372686 ms (97.5%)
;; Overhead used : 6.574883 ns
;; ```
;;
;; #### Input size vs. performance
;;
;; We will grow the size of the input and measure the execution time (MET)
#_(require '[criterium.core :as c])

; ```clojure
; (def full-input
;   (parse-input "src/aoc/2025/day1/input.txt"))
;
; ^{:nextjournal.clerk/visibility {:result :show}}
; (def input-sizes
;   "Generate smaple input sizes (how much of the original input to take)"
;   (filter #(< % (count full-input)) (take 10 (iterate #(+ % 500) 1))))
;
; (defn get-n-first-operations [n]
;   (take n full-input))
;
; (defn benchmark-with-input [f i]
;   (c/quick-benchmark* #(f i) nil))
; ```
;; Benchmark funtion `f` for every input size sample in `input-sizes`
; ```clojure
; (defn generate-performance-data [f input-sizes]
;   (mapv (fn [n]
;           (let [input (get-n-first-operations n)
;                 benchmark-result (benchmark-with-input f input)]
;             {:input-size n
;              :mean-execution-time (first (:mean benchmark-result))}))
;         input-sizes))
; ```
; ```clojure
; (defn visualize-performance-trend [performance-data]
;   (let [input-sizes (mapv :input-size performance-data)
;         execution-times (mapv :mean-execution-time performance-data)]
;     (clerk/plotly
;      {:data [{:type "scatter"
;               :mode "lines+markers"
;               :x input-sizes
;               :y execution-times
;               :name "Execution Time"}]
;       :layout {:title "Function Performance vs. Input Size"
;                :xaxis {:title "Input Size"
;                        :type "linear"}
;                :yaxis {:title "Mean Execution Time (seconds)"
;                        :type "linear"}}})))
;
; (def performance-data
;   (generate-performance-data part-1 input-sizes))
; ```
;
;; Now visualize the rate of Mean Execution Time in relation to the input size.
;; ```clojure
;; (visualize-performance-trend performance-data)
;; ```

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/image "src/aoc/2025/day1/performance-benchmark-trend.png")

;; We can see that the mean execution time(MET) grows almost linearly.
;; - For input size 501 it took 299(us).
;; - For input size 4001 it took 0.0016(ms).
;; - The input grew by ~8 while the MET grew by ~5.3.
;;
;; This is pretty good performance.

;; ### Unit Tests
;; Turn dial left
(assert (= (turn-dial 60 \L 80) 80))
(assert (= (turn-dial 0 \L 120) 80))
(assert (= (turn-dial 15 \L 14030) 85))
(assert (= (turn-dial 1 \L 1) 0))
;; Turn dial right
(assert (= (turn-dial 60 \R 80) 40))
(assert (= (turn-dial 96 \R 80) 76))
(assert (= (turn-dial 0 \R 120) 20))
(assert (= (turn-dial 60 \R 1430) 90))
(assert (= (turn-dial 99 \R 1) 0))

; ## Part 2

; ### Approach

; count the number of times any operation causes the dial to point at 0,
; regardless of whether it happens **during a rotation** or **at the end of one**.\
; Count each full rotation. that's a zero. (The dial will point to 0 once in each full rotation)
; 1. each full rotation WILL pass 0, hence pointing at it at some point
; 2. at the end of rotation, the dial MIGHT land on a 0, that's another 0
; 3. sum the count of how many times the dial pointed to 0 for each operation.
; 4. keep count of how many `points to zero` you get for each operation
; 5. sum `points to zero` of all operations together

; 40 - 45 = -5 < 0
; => 1 zero pass
(turn-dial 40 \L 45)
; 2 (200) + 1 (50 - 70 = -20) = 3 zero pass
(turn-dial 50 \L 270)

; 99 - 40 = 49 \
; 49 - 90 = -41 < 0 \
; 1 zero pass
(turn-dial 40 \R 90)
; 99 - 40 = 59 \
; 59 - 45 = 14 > 0 \
; => 0 zero pass
(turn-dial 40 \R 45)
; 99 - 99 = 0 \
; 0 - 2 = -2 < 0 \
; => 1 zero passes
(turn-dial 99 \R 2)
; 99 - 50 = 49 \
; 49 - 70 = 21 > 0 \
; => 2 (200) +  1 zero pass
(turn-dial 50 \R 270)
; 99 - 50 = 49 \
; 49 - 20 = 29 > 0 \
; 0 zero pass
(turn-dial 50 \R 20)

; This was a bit hard to wrap my head around to be honest.

; ### Implementation

(defn count-points-to-zero
  "Count how many times The dial points on 0 in an operation.
  Each full rotation (100 steps) will surely pass 0.
  So count how many full roations we have, Then, see if the remaining steps pass the zero:
  - For left turns: if (current-val - steps) < 0
  - For right turns: if ((99 - current-val) - steps) < 0"
  [current-val dir steps]
  (let [full-rotations (quot steps 100)
        remaining-steps (mod steps 100)]
    (case dir
      \L (+ full-rotations
            ;; If the current-val is 0, it can only point to 0 with full rotations
            ;; FIXME the logic for checking 0 current-val seems hacky
            (if (and (<= (- current-val remaining-steps) 0) (not= current-val 0))
              1
              0))
      \R (+ full-rotations
            (if (< (- (- 99 current-val) remaining-steps) 0)
              1
              0))
      (throw (ex-info "Wrong direction"
                      {:dir dir
                       :type IllegalArgumentException})))))

(defn count-points-to-zero-history
  "Run every instruction. get the initial value from `starting-values` of corresponding index.

  `starting-values` is a vector of the calculated starting values of each operation, after running
  the previous operation.

  Returns a seq, whom values correspond to the number of times the dial has pointed to zero
  during the operation."
  [starting-values input]
  (map
   (fn
     [current-val instruction]
     (let [dir (first instruction)
           steps (Integer/parseInt (str/join (rest instruction)))]
       (count-points-to-zero current-val dir steps))) starting-values input))

(defn sum-points-to-zero [input starting-values]
  (apply + (count-points-to-zero-history starting-values input)))

(defn part-2 [input]
  (sum-points-to-zero input (turn-dial-with-history input)))

;; Let's see the result of part 2 with our test-input
(assert (= (part-2 test-input) 6))

; ### Benchmark

; The performance seems pretty good too. Almost instantaneous result
; on my old tiny HP Prodesk with intel i5 6500T CPU.\
; Note that benchmarks for part1 are from my laptop with much faster RAM and i7 9750H CPU.
; The benchmarks for part1 on this small boi give a mean time of 3ms.
;
; TODO: run benchmarks for this part also with laptop

; ```clojure
; (c/quick-bench (part-2 (parse-input "src/aoc/2025/day1/input.txt")))
; ```
; Results:
; ```
; (out) Evaluation count : 126 in 6 samples of 21 calls.
; (out)              Execution time mean : 5.179176 ms
; (out)     Execution time std-deviation : 257.478597 µs
; (out)    Execution time lower quantile : 4.930603 ms ( 2.5%)
; (out)    Execution time upper quantile : 5.471735 ms (97.5%)
; (out)                    Overhead used : 11.240677 ns
; ```

; ### Unit tests

;; Left Tests
(assert (= (count-points-to-zero 0 \L 350) 3))
(assert (= (count-points-to-zero 20 \L 350) 4))
(assert (= (count-points-to-zero 20 \L 310) 3))
; note that the dial *passes* 0 three times and lands on 0. that is 4 times
(assert (= (count-points-to-zero 20 \L 320) 4))

;; Right tests
(assert (= (count-points-to-zero 0 \R 350) 3))
(assert (= (count-points-to-zero 20 \R 380) 4))
(assert (= (count-points-to-zero 20 \R 279) 2))
