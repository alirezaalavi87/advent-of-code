^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(ns aoc.2025.day2.main
  {:nextjournal.clerk/auto-expand-results? true
   :nextjournal.clerk/toc true}
  (:require
   [nextjournal.clerk :as clerk]
   [nextjournal.clerk.viewer :as viewer]
   [clojure.string :as str]
   [clojure.test :refer [deftest is testing run-tests]]
   [criterium.core :as c]))

;; FIXME these functions for configuring how clerk shows results are repeated in every notebook.
;; Make it a global config
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

;; # Day2: Gift Shop
; Product IDs are messed up! Detect the messed up IDs within the specified ranges.
; Invalid IDs are IDs that:
; 1. ID made *only* of some digits repeated *twice*. e.g. 55, 6464, 123123
;
; For example, if the specified range is 11-22, look for all invalid IDs from 11 to 22.
; That would be 11 and 22 (numbers repeated twice)
;
; - IDs are only positive integers.
; - IDs don't have leading zeros

; ## Utilities
(defn parse-input
  "Parse input to vector of vectors.
  Each vector defining the start and end of a range"
  [path]
  (map #(mapv read-string (str/split % #"-"))
       (-> (slurp path)
           str/trim
           (str/split #","))))

; Parsed test input
(def test-input
  (parse-input "src/aoc/2025/day2/input.test.txt"))

(defn num->digits
  "Number to seq of it's digits.
  Works only for valid numbers."
  [n]
  (->> n
       abs
       str
       (mapv (comp read-string str))))

(deftest num->digits-test
  (testing "Positive numbers"
    (is (= (num->digits 1) [1]))
    (is (= (num->digits 123) [1 2 3]))
    (is (= (num->digits 1234567899) [1 2 3 4 5 6 7 8 9 9])))
  (testing "Negative numbers"
    (is (= (num->digits -1) [1]))
    (is (= (num->digits -123) [1 2 3])))
  (testing "Edge cases"
    (is (= (num->digits 0) [0]))))

; ## Part 1
;
; 1. Get all invalid IDs
; 2. Sum of all invalid IDs
;
; Example input should result in `1227775554`.

; ### Approach
; In each range of numbers from n to m, check each number if **number is valid**
;
; To validate number:
;   - Is repeated twice? -> invalid
;     - To check this:
;       cut the number in half, if first half equals second half -> invalid
;   - else: valid

; ### Implementation

; `repeated-twice?` splits the number in two halves, and compares the two.
; If both halves are equal, than the number is repeated twice.
(defn repeated-twice?
  "Check if number is made of two repeated parts.
  Like 55, 123123"
  [n]
  (let [digits (num->digits n)
        digits-count (count digits)
        first-half (take (/ digits-count 2) digits)
        second-half (reverse (take (/ digits-count 2) (reverse digits)))]
    (if (odd? digits-count)
      false
      (= first-half second-half))))

; `valid-id?` is the main function for checking if an ID is valid or not.
; This being it's own function makes the logic slightly more understandable and
; flexible.
(defn invalid-id? [n]
  (repeated-twice? n))

(deftest invalid-id?-test
  (testing "Valid IDs"
    (is (false? (invalid-id? 0)))
    (is (false? (invalid-id? 789)))
    (is (false? (invalid-id? 999))))
  (testing "Invalid IDs"
    (is (true? (invalid-id? 123123)))
    (is (true? (invalid-id? 11)))))

; Get all invalid IDs from specified range.
(defn get-invalid-from-range
  "Get invalid IDs from specified range.
  Returns seq of invalid IDs."
  [[start end]]
  {:pre [(number? start)
         (number? end)]}
  (let [range-nums (range start (inc end))]
    (filter number? (map #(when (invalid-id? %) %) range-nums))))

(deftest get-invalid-from-range-test
  (is (= (get-invalid-from-range [11 22]) '(11 22)))
  (is (= (get-invalid-from-range [0 10]) ())))

; Final answer to part 1:
(defn sum-invalid-ids
  "Takes input of puzzle which, gets invalid IDs from each specified range and sums them.

  Example input: `([11 22] [349 500] [730 1203])`"
  [input]
  (let [invalid-ids (flatten (map get-invalid-from-range input))
        invalid-ids-sum (reduce + invalid-ids)]
    invalid-ids-sum))

; Now make sure we get correct results with test input for part 1
(assert (= 1227775554 (sum-invalid-ids test-input)))

; ### Optimization

; Performance for the real input is really slow. I think it can be fun optimizing it!\
; Parsing the input is very fast (0.5 ms) the rest is very slow.

; Using `clj-async-profiler` to profile the code and identify the bottlenecks, we see that `repeated-twice?`
; takes the most time. And `count` takes the most time within `repeated-twice?`.
(clerk/image "src/aoc/2025/day2/assets/profiling1.png")

; Let's optimize `repeated-twice?` by eliminating the two nested `reverse` calls
; by using `drop` instead and doing one less division.
(defn repeated-twice-opt?
  [^long n]
  (let [digits (num->digits n)
        len (count digits)
        half-len (quot len 2)
        first-half (take half-len digits)
        second-half (drop half-len digits)]
    (= first-half second-half)))

; Comparing different implementations of `repeated-twice?`
; ```clojure
; (def random-numbers
;   (repeatedly 10000 #(rand-int 100000000)))
;
; (c/quick-bench (map repeated-twice? random-numbers))
; (c/quick-bench (map repeated-twice-opt? random-numbers))
; ```
; 13.73ms vs. 11.32ms mean execution time after optimizations. That's ~18%.

; To optimize `num->digits?`
; - make sure `num->digits?` returns a vector instead of a list, with `mapv` instead of `map`,
;   that itself will bring enhancements.
; - We also see from our profiling result that `read-string` which is used throughout the code to
;   parse string numbers to number, is taking much time. So we will use `parse-long`
;   which is much better suited for this and has the same performance as `Integer/parseInt`.
; - Remove `abs` since we konw all our numbers are positive

(defn num->digits-opt
  [n]
  (->> n
       str
       (mapv (comp parse-long str))))

; Let's make `sum-invalid-ids` use our optimized version of functions and benchmark them.
;
; ```clojure
; (with-redefs-fn {#'repeated-twice? repeated-twice-opt?
;                  #'num->digits num->digits-opt}
;  #(c/quick-bench (sum-invalid-ids (parse-input "src/aoc/2025/day2/input.txt"))))
; ; =>
; ; (out) Evaluation count : 6 in 6 samples of 1 calls.
; ; (out)              Execution time mean : 1.239140 sec
; ; (out)     Execution time std-deviation : 43.134107 ms
; ; (out)    Execution time lower quantile : 1.194249 sec ( 2.5%)
; ; (out)    Execution time upper quantile : 1.294277 sec (97.5%)
; ; (out)                    Overhead used : 6.769222 ns
;
; (c/quick-bench (sum-invalid-ids (parse-input "src/aoc/2025/day2/input.txt")))
; ; =>
; ; (out) Evaluation count : 6 in 6 samples of 1 calls.
; ; (out)              Execution time mean : 9.713714 sec
; ; (out)     Execution time std-deviation : 127.283684 ms
; ; (out)    Execution time lower quantile : 9.555073 sec ( 2.5%)
; ; (out)    Execution time upper quantile : 9.872587 sec (97.5%)
; ; (out)                    Overhead used : 6.769222 ns
; ```
; Boy, Oh boy! We got ~87.3% increase in performance after our optimizations!\
; These are still cookie-cutter optimizations, tough. I think we can get much further
; with more in depth opts.

; Our profiling after the optimizations shows that now our most resource consuming
; part is the `mapv` function in `num->digits-opt`.
(clerk/image "src/aoc/2025/day2/assets/profiling2.png")

; Let's see how we can improve it.
;
; Now that I look at it, the previous `num->digits-opt` looks absolutely stupid.\
; There are two calls to `str` and there is no reason to use threading `->>` here.
; Just makes it confusing.
; - Convert `n` to string directly, feed it as `coll` to `mapv`.
; - Use `int` substract the ASCII offset to get the number.

(defn num->digits-opt2
  [^long n]
  (mapv #(- (int %) 48) (Long/toString n)))

; ```clojure
; (with-redefs-fn {#'repeated-twice? repeated-twice-opt?
;                  #'num->digits num->digits-opt2}
;  #(c/quick-bench (sum-invalid-ids (parse-input "src/aoc/2025/day2/input.txt"))))
; =>
; ; (out) Evaluation count : 6 in 6 samples of 1 calls.
; ; (out)              Execution time mean : 927.130309 ms
; ; (out)     Execution time std-deviation : 30.063344 ms
; ; (out)    Execution time lower quantile : 898.622286 ms ( 2.5%)
; ; (out)    Execution time upper quantile : 972.890815 ms (97.5%)
; ; (out)                    Overhead used : 6.691813 ns
; ; (out)
; ; (out) Found 1 outliers in 6 samples (16.6667 %)
; ; (out)   low-severe   1 (16.6667 %)
; ; (out)  Variance from outliers : 13.8889 % Variance is moderately inflated by outliers
; ```
; This make our total optimizations **~10.5x faster** than our initial solution.

; ## Part 2

; >Now, an ID is invalid if it is made only of some sequence of digits repeated at least twice.
;  So, 12341234 (1234 two times), 123123123 (123 three times), 1212121212 (12 five times),
;  and 1111111 (1 seven times) are all invalid IDs.

; ### Approach

; We only need to find repeated numbers. To do that
; first approach that comes to my mind is to divide number by groups of n digits,
; where $n \le \text{number length}$ and check in which $n$, all divided groups
; are equal.

; let our number be 112112112
;
; divide by groups of 1 (n=1) => [1 1 2 1 1 2 1 1 2]\
; groups are not equal.
;
; n=2 => [11 21 12 11 2]\
; groups are not equal
;
; n=3 => [112 112 112]\
; groups are equal ✅

; The groups should be strings of numbers so a gruop like "001" can still exist

; This is somewhat bruteforce but it works.

; ### Implementation

(defn num-split-n
  "Split digits of number to groups of 'n'.
  A group will be a string of digits to be able to represent groups such as '001' which is not a valid number."
  [number n]
  (loop [current-num (str number)
         groups []]
    (if (str/blank? current-num)
      groups
      (recur
       (str/join (second (split-at n (str/split current-num #""))))
       (conj groups (str/join (first (split-at n (str/split current-num #"")))))))))

(deftest num-split-n-test
  (is (= ["112" "112" "112"] (num-split-n 112112112 3)))
  (is (= ["1" "0" "0" "1"] (num-split-n 1001 1)))
  (is (= ["1" "2" "3" "4" "5"] (num-split-n 12345 1))))

(defn repeated-digits?
  "Return true if number is made of repeated digits."
  [number]
  (loop [number number
         n 1]
    (cond
      (> n (quot (count (num->digits-opt2 number)) 2))
      false
      (apply = (num-split-n number n)) ; All splitted groups are equal
      true
      :else (recur number (inc n)))))

(deftest repeated-digits?-test
  (testing "True cases"
    (is (true? (repeated-digits? 123123)))
    (is (true? (repeated-digits? 22)))
    (is (true? (repeated-digits? 111))))
  (testing "False cases"
    (is (false? (repeated-digits? 123124)))
    (is (false? (repeated-digits? 1231233)))
    (is (false? (repeated-digits? 1001)))
    (is (false? (repeated-digits? 12)))
    (is (false? (repeated-digits? 0)))
    (is (false? (repeated-digits? 1))))
  (testing "Invalid values"
    (is (false? (repeated-digits? -1)))
    (is (false? (repeated-digits? -123123)))))

; Compute final answer to part 2
(defn part-2 [input]
  (with-redefs-fn {#'repeated-twice? repeated-digits?
                   #'num->digits num->digits-opt2}
    #(sum-invalid-ids input)))

; Check if the answer for test input is correct:
(assert (= 4174379265 (part-2 test-input)))

; (time (part-2 (parse-input "src/aoc/2025/day2/input.txt"))) ; 50857215650

; ## Running all tests
(run-tests)
