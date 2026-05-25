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

(deftest repeated-twice?-test
  (testing "Valid IDs"
    (is (false? (repeated-twice? 0)))
    (is (false? (repeated-twice? 789)))
    (is (false? (repeated-twice? 999))))
  (testing "Invalid IDs"
    (is (true? (repeated-twice? 123123)))
    (is (true? (repeated-twice? 11)))))

; `invalid-id?` is the main function for checking if an ID is valid or not.
; This being it's own function makes the logic slightly more understandable and
; flexible.
(defn invalid-id? [n]
  (repeated-twice? n))

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
(defn repeated-twice?-opt
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
; (c/quick-bench (map repeated-twice?-opt random-numbers))
; ```
; 13.73ms vs. 11.32ms mean execution time after optimizations. That's \~18%.

; To optimize `num->digits?`
; - make sure `num->digits?` returns a vector instead of a list, with `mapv` instead of `map`,
;   that itself will bring enhancements.
; - We also see from our profiling result that `read-string` which is used throughout the code to
;   parse string numbers to number, is taking much time. So we will use `parse-long`
;   which is much better suited for this and has the same performance as `Integer/parseInt`.
; - Remove `abs` since we know all our numbers are positive

(defn num->digits-opt
  [n]
  (->> n
       str
       (mapv (comp parse-long str))))

; Let's make `sum-invalid-ids` use our optimized version of functions and benchmark them.
;
; ```clojure
; (with-redefs-fn {#'repeated-twice? repeated-twice?-opt
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
; Boy, Oh boy! We got \~87.3% increase in performance after our optimizations!\
; These are still cookie-cutter optimizations, tough. I think we can get much further
; with more in depth optimizations.

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
;
; (I found this way of converting number to digits in stackoverflow)

(defn num->digits-opt2
  [^long n]
  (mapv #(- (int %) 48) (Long/toString n)))

; UPDATE: My solution to part2 lead me to think how I can improve `num->digits-opt2`.\
; We don't really need to convert the digits to int. We can just compare strings or chars
; like we did in part 2.

(defn num->digits-opt3
  [^long n]
  (vec (str n)))

; ```clojure
; (c/quick-bench (num->digits-opt2 1234567890123456))
; ; (out) Evaluation count : 979176 in 6 samples of 163196 calls.
; ; (out)              Execution time mean : 638.355243 ns
; (c/quick-bench (num->digits-opt3 1234567890123456))
; ; (out) Evaluation count : 4139466 in 6 samples of 689911 calls.
; ; (out)              Execution time mean : 142.703027 ns
; ```

; That's a lot faster!

; ```clojure
; (with-redefs-fn {#'repeated-twice? repeated-twice?-opt
;                  #'num->digits num->digits-opt3}
;  #(c/quick-bench (sum-invalid-ids (parse-input "src/aoc/2025/day2/input.txt"))))
; =>
; ; (out) Evaluation count : 6 in 6 samples of 1 calls.
; ; (out)              Execution time mean : 572.113409 ms
; ```
; This make our total optimizations **\~17x faster** than our initial solution.

; ## Part 2

; >Now, an ID is invalid if it is made only of some sequence of digits repeated at least twice.
;  So, 12341234 (1234 two times), 123123123 (123 three times), 1212121212 (12 five times),
;  and 1111111 (1 seven times) are all invalid IDs.

; ### Approach

; We only need to find repeated numbers. To do that, the
; first approach that comes to my mind is to divide number by groups of $n$ digits,
; where $n \le \text{number length}$, and check if for some $n$, all divided groups
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

; ~The groups should be strings of numbers so a group like "001" can still exist.~\
; The groups should be sequence of numbers `(\1 \1 \2)` because there is no need to create
; strings from them which is a very costly process. In profiling, `str/join` took \~75% of our computation time.

; ### Implementation

(defn num-split-n
  "Split digits of number to groups of 'n'.
  A group will be a sequence of digits to be able to represent groups such as '001' which is not a valid number."
  [^long number ^long n]
  (loop [current-num-digits (num->digits number)
         groups []]
    (let [num-digits-split (vec (split-at n current-num-digits))]
      (if (empty? current-num-digits)
        groups
        (recur
         (second num-digits-split)
         (conj groups (first num-digits-split)))))))

(deftest num-split-n-test
  (is (= ['(1 1 2) '(1 1 2) '(1 1 2)] (num-split-n 112112112 3)))
  (is (= ['(1) '(0) '(0) '(1)] (num-split-n 1001 1)))
  (is (= ['(1) '(2) '(3) '(4) '(5)] (num-split-n 12345 1))))

(defn repeated-digits?
  "Return true if number is made of repeated digits."
  ([^long number]
   (loop [number number
          n 1]
     (cond
       (> n (quot (count (num->digits number)) 2))
       false
       (apply = (num-split-n number n)) ; All splitted groups are equal
       true
       :else (recur number (inc n))))))

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
    (is (false? (repeated-digits? 1)))))

; Compute final answer to part 2\
; We will use `sum-invalid-ids` but just use `repeated-digits?` instead of `repeated-twice?`
(defn part-2 [input]
  (with-redefs-fn {#'repeated-twice? repeated-digits?
                   #'num->digits num->digits-opt3}
    #(sum-invalid-ids input)))

; Check if the answer for test input is correct:
(assert (= 4174379265 (part-2 test-input)))

; ### Optimization

; We have already applied the optimizations from part 1.\
; Our slution is quite slow as of now. (\~15s execution time mean for full input).
;
; Let's profile `part-2` with the full input.
(clerk/image "src/aoc/2025/day2/assets/profp2-1.png")
(clerk/image "src/aoc/2025/day2/assets/profp2-1-zoom.png")

;; The main culprit is `num-split-n` within `repeated-digits?`.
;; Turns out, clojure has a function `partition-all` which does exactly what we want!\
;; Clojure's stdlib never ceases to amaze me! it has such amazing and useful functions!\
;; Also, we dont *need* the number split to be a vector, it can be a list since we just
;; want to check the equality of splits.

;; So we will optimize and rewrite `repeated-digits?`
;; - Gets rid of num-split-n entirely
;; - `partition-all` returns group seqs lazily; every? compares groups without expanding args.
;; - Avoids apply by doing a manual loop
;; - compute `digits`, `len`, `max-len` only once
;; - only check `n` that devides the length of the number

(defn repeated-digits?-opt
  "Return true if number is made of repeated digits."
  [^long number]
  (let [digits (num->digits number)
        len (count digits)
        max-n (quot len 2)]
    (loop [n 1]
      (cond
        ;; If our period is larger than half of length, then number cannot be
        ;; repeated in periods of n.
        (> n max-n) false
        (not (zero? (mod len n))) (recur (inc n)) ; skip non-divisors
        :else
        (let [groups (partition-all n digits)
              firstg (first groups)]
          (if (and firstg (every? #(= firstg %) (rest groups)))
            true
            (recur (inc n))))))))

;; Let's benchmark them

(with-out-str
  (c/quick-bench (with-redefs-fn {#'num->digits num->digits-opt3}
                   #(repeated-digits? 123456789012345678))))
(with-out-str
  (c/quick-bench (with-redefs-fn {#'num->digits num->digits-opt3}
                   #(repeated-digits?-opt 123456789012345678))))

;; This is \~57% faster

; ```clojure
; (c/quick-bench (part-2 (parse-input "src/aoc/2025/day2/input.txt")))
; ; =>
; ; (out) Evaluation count : 6 in 6 samples of 1 calls.
; ; (out)              Execution time mean : 14.848484 sec
; ; (out)     Execution time std-deviation : 81.032469 ms
; (c/quick-bench (with-redefs-fn
;                  {#'repeated-digits? repeated-digits?-opt
;                   #'num->digits num->digits-opt3}
;                  #(part-2 (parse-input "src/aoc/2025/day2/input.txt"))))
; ; =>
; ; (out) Evaluation count : 6 in 6 samples of 1 calls.
; ; (out)              Execution time mean : 6.660334 sec
; ; (out)     Execution time std-deviation : 164.315838 ms
; ```

;; Our total execution time of part-2 is down by \~55%.

;; For further optimization, I can't think of anything since my knowledge on Clojure is
;; pretty limited at the moment.\
;; So I ask ChatGPT5-mini for help.\
;; Key changes:
;; - convert digits once into a primitive int-array for fast indexed access (avoids boxed collections).
;; - avoid allocations from partition/all/seq; compare in-place by index.
;; - skip n values that do not divide length.
;; - use unchecked integer ops and simple loops to minimize boxing.

;; So I dug deep to understand why and how these changes benefit our performance.
;; - convert digits once into a primitive int-array for fast indexed access
;;    with `digits->int-array` (avoids boxed collections).
;; - Instead of `apply`ing `=` to our number splits, check equality of digits
;;    in-place by index (in `repeat-pattern?`). This avoids

;; In `repeated-digits?-opt` we used `partition-all` to split the sequence of our
;; number's digits into groups of n, and then using `every?` to see if all splits
;; are equal.\
;; Here's what this does:\
;; `partition-all` creates seq of coll, then loads the first n items of the seq
;; into memory at once with `doall`. It then recurses with the `nthrest` of the
;; collection and keeps adding the results together and then returns them in a `lazy-seq`.\
;; This creates many intermediary seqs and repeated traversals

;; In `repeated-digits?-opt2` we convert our digits into a primitive int-array
;; for fast indexed access (`digits->int-array`). This avoids boxed collections.\
;; Then, we check if our array of ints is repeated with `repeated-pattern?` for
;; groups of n.\
;; The difference here from previous implementation is that we will check equality of
;; ints in-place by their index, with unchecked comparisons. This enables unboxed primitive
;; operations for us instead of creating boxed values and seqs and traversing them.\
;; If our previous conditions did not hold, we will recurse `repeated-digits?-opt2`
;; with bigger n until `max-n`.

(defn- digits->int-array
  "Convert list of digits to array of int"
  [digits]
  (let [len (count digits)
        arr (int-array len)]
    (loop [i 0 d digits]
      (when (< i len)
        (aset-int arr i (int (first d)))
        (recur (inc i) (rest d))))
    arr))

(defn- repeat-pattern?
  "Return true if arr repeats with period n."
  [^ints arr ^long len ^long n]
  (loop [i n]
    (cond
      (>= i len) true
      (== (aget arr i) (aget arr (mod i n))) (recur (inc i))
      :else false)))

#_(repeat-pattern? (digits->int-array (num->digits-opt3 12341234)) 8 8)

;; Using a manual `loop` instead of `apply` for checking the equality,
(defn repeated-digits?-opt2
  "True if number is made of repeated digits. Single-threaded, allocation-minimal."
  [^long number]
  (let [digits (num->digits number)
        len (count digits)
        max-n (quot len 2)
        arr (digits->int-array digits)]
    (loop [n 1]
      (cond
        (> n max-n) false
        (not (zero? (mod len n))) (recur (inc n))
        (repeat-pattern? arr len n) true
        :else (recur (inc n))))))

;; Note it doesn't matter if `num->digits` returns digits as char representation or
;; ints, because we just want to compare. The actual value doesn't matter.
; ```clojure
; (c/quick-bench (with-redefs-fn
;                  {#'repeated-digits? repeated-digits?-opt2
;                   #'num->digits num->digits-opt3}
;                  #(part-2 (parse-input "src/aoc/2025/day2/input.txt"))))
; ; =>
; ; (out) Evaluation count : 6 in 6 samples of 1 calls.
; ; (out)              Execution time mean : 2.057931 sec
; ; (out)     Execution time std-deviation : 14.719844 ms
; ```
;; This is \~87% faster than our first implementation.\
;; Or in more click-baity words, **\~ x7.5 times faster!**

; ## Observations and lessons

; - Benchmarking results aren't consistent between different days. Same function with same
;   inputs can give varying results in terms of absolutes (Execution mean time for example).
;   But the proportions are almost the same. So relying on absolute times and values can be
;   misleading.
; - "TDD" isn't that great, but writing tests for your functions and having them
;   for checking your functions' functionality(!) really helps and is especially useful for
;   refactoring. Helps you refactor your code with more ease of mind.\
;   Also, I love the documentation aspect of tests. They serve as a kind of document
;   for how your function must behave.
; - Profiling\
;   This was my first time using clj-async-profiler.\
;   We have defined a `:profile` alias in `deps.edn` which loads `clj-async-profiler` and
;   some needed JVM options for profiling.\
;   So we will run it with `clj -A:profile`. Then, within the repl, Something like this can help us benchmark our code.
;   ```clojure
;   (require '[clj-async-profiler.core :as prof])
;   (require '[aoc.2025.day2.main :as main])
;   (prof/profile (my-function))
;   (prof/serve-ui 8080) ; serve the profiler UI on localhost:8080
;   ```

; ## Running all tests
(run-tests)
