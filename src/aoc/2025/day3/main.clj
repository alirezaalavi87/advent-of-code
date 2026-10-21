^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(ns aoc.2025.day3.main
  {:nextjournal.clerk/toc true}
  (:require [nextjournal.clerk :as clerk]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing run-tests]]))

;; # Day3: Lobby
;; Each line of your input is a **battery band** where each digit corresponds to
;; to the "joltage" of a battery in that bank.
;; The joltage that the bank produces -> The number formed by the digits on the batteries that are turned on.
;;
;; For example in a bank `3539` if battery number 2 and 4 are turned on, the bank produces
;; 59 jolts.
;;
;; ## Utilities

;; Notice how we have to use BigInteger throughout because the actual input has numbers of 100 digits! so `Long` will not work here.
(defn num->digits
  [^BigInteger n]
  (mapv #(- (int %) 48) (BigInteger/.toString n)))

(defn digits->number
  "Turn a vector of numbers to a single number.
  Only works with valid, positive digits. (0 0 1 2) is an invalid input for example.
  E.x. (1 2 4) -> 124"
  [v]
  (BigInteger/new (apply str v)))

(deftest digits->number-test
  (is (= 893 (digits->number [893]))))

(defn parse-input
  "Parse input to vector of vectors.
  Each vector defining the joltages of batteries from a bank"
  [path]
  (mapv #(num->digits (BigInteger/new %))
        (-> (slurp path)
            (str/split #"\n"))))

(def test-input
  (parse-input "src/aoc/2025/day3/input.test.txt"))

(defn get-highest-num-in-vec
  "Takes vector of numbers, returns the Highest number with it's index as a vector"
  [v]
  (let [max-num (first (sort > v))
        max-num-i (.indexOf v max-num)]
    [max-num max-num-i]))

(deftest get-highest-num-in-vec-test
  (is (= [9 2] (get-highest-num-in-vec [1 2 9 4]))))

;; ## Part 1:
;; **Objective:** Find the highest possible output joltage.\
;; -> Find the largest possible joltage a bank can produce with 2 active batteries
;; -> Sum of all banks

;; To find the highest possible joltage that a bank can produce
;; with `n` active batteries:\
;; 1. Search for the largest number in bank (`a`) where `a` still has at least n-1 batteries ahead of it.\
;;    for example, let n be 2, and bank be 7809; `a` will be 8, not 9, because it has more than 1 batteries ahead of it.\
;;    If we don't do this the max-joltage of this bank will be 9 instead of 89
;; 2. Search for the largest number after `a`
;; 3. return them as a number
(defn bank-max-joltage
  "Takes a vector of numbers representing a battery bank,
  Returns a number representing the highest possible joltage of that bank
  with `n` batteries."
  [v n]
  (loop [v v
         padding (dec n)
         highest-jolts []]
    (if (and (< (count highest-jolts) n) (pos? (count v)))
      (let [;; The vector-(n-1) subvector to correctly get the highest possible number from sequence
            subv (subvec v 0 (- (count v) padding))
            [highest-jolt i] (get-highest-num-in-vec subv)]
        (recur (subvec v (inc i))
               (dec padding)
               (conj highest-jolts highest-jolt)))
      (digits->number highest-jolts))))

(deftest bank-max-joltage-test
  (is (= 95 (bank-max-joltage [1 2 3 3 2 9 5] 2)))
  (is (= 95 (bank-max-joltage [9 5 1 1 2 4 1 2] 2)))
  (is (= 89 (bank-max-joltage [8 1 1 1 1 9] 2)))
  (is (= 99 (bank-max-joltage [8 1 1 9 1 9] 2)))
  (is (= 9542 (bank-max-joltage [9 5 1 1 2 4 1 2] 4))))

;; Calculate the sum of all battery bank's highest joltage.
;;
;; We use `pmap` here so the computations are done in parallel for higher speed.\
;; The solution is already pretty fast. On the real input it took ~14ms on average benchmarks with `map` and
;; computing them in parallel with `pmap` lowered that to ~5ms.
(defn part-1 [input]
  (let [bank-max-joltage-two-batteries (fn [v] (bank-max-joltage v 2))]
    (apply + (pmap bank-max-joltage-two-batteries input))))

;; Check if the answer for test input is correct (357)
(assert (= 357 (part-1 test-input)))

;; Great! part 1 done.

#_(run-tests)
