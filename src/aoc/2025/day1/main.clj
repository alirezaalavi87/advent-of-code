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

;; ## Part 1
;; The password is the number of times that the resulting number is 0 after an operation.
;; - Keep the result after every operation
;; - Count how many times it has landed on 0
(defn part-1 [input]
  (count-zeros (run-instruction-with-history input)))

;; Let's see the result with our test input
^{:nextjournal.clerk/visibility {:result :show}}
(part-1 test-input)

;; Test the result (`nil` means correct)
^{:nextjournal.clerk/visibility {:result :show}}
(assert (= (part-1 test-input) 3))

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

; ## Part 2
; count the number of times any click causes the dial to point at 0, regardless of whether it happens during a rotation or at the end of one.
; Count each full rotation. that's a zero.
; - during the turning, each full rotation is a 0
; - at the end, the dial might land on a 0, that's another 0
; - sum these as the count of `zero-passes` for each operation.
; - keep count of how many `zero-passes` you get for each operation
; - sum `zero-passes` of all operations together

;; ## Unit Tests
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

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(defn -main [& args]
  (when (nil? args)
    (print-help)
    (System/exit 1))
  (let [input-file (first args)
        input (parse-input input-file)]
    [(part-1 input)]))

