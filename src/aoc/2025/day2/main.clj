^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(ns aoc.2025.day2.main
  {:nextjournal.clerk/auto-expand-results? true
   :nextjournal.clerk/toc true}
  (:require
   [nextjournal.clerk :as clerk]
   [nextjournal.clerk.viewer :as viewer]
   [clojure.string :as str]
   [clojure.test :refer [deftest is testing]]))

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
       (map (comp read-string str))))

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
(defn valid-id? [n]
  (cond
    (repeated-twice? n) false
    :else true))

(deftest valid-id?-test
  (testing "Valid IDs"
    (is (true? (valid-id? 0)))
    (is (true? (valid-id? 789)))
    (is (true? (valid-id? 999))))
  (testing "Invalid IDs"
    (is (false? (valid-id? 123123)))
    (is (false? (valid-id? 11)))))

; Get all invalid IDs from specified range.
(defn get-invalid-from-range
  "Get invalid IDs from specified range.
  Returns seq of invalid IDs."
  [[start end]]
  {:pre [(number? start)
         (number? end)]}
  (let  [range-nums (range start (inc end))]
    (filter number? (map #(if (not (valid-id? %)) % nil) range-nums))))

(deftest get-invalid-from-range-test
  (is (= (get-invalid-from-range [11 22]) [11 22]))
  (is (= () (get-invalid-from-range [0 10]))))

; Final answer to part 1:
(defn sum-invalid-ids
  "Takes input of puzzle which is of type [:seq [:vec [:number]]],
  gets invalid IDs from each specified range and sums them."
  [input]
  (let [invalid-ids (filter not-empty (map get-invalid-from-range input))
        invalid-ids-sum (apply + (map #(apply + %) invalid-ids))]
    invalid-ids-sum))

; Now make sure we get correct results with test input for part 1
(assert (= 1227775554 (sum-invalid-ids test-input)))

; Performance for the real input is really slow. I think it can be fun optimizing it!
; ```clojure
; (time (sum-invalid-ids (parse-input "src/aoc/2025/day2/input.txt")))
; ;=> (out) "Elapsed time: 15928.037254 msecs"
; ```
; Parsing the input is very fast (0.5 ms) the rest is very slow.

; ## Part 2

