(defn fizzbuzz [numero]
  (cond (zero? (mod numero 15)) "fizzbuzz"
        (zero? (mod numero 5)) "buzz"
        (zero? (mod numero 3)) "fizz"
        :else numero))
        
(defn fb [max]
  (if (zero? max)
    ""
    (str (fb (- max 1)) " " (fizzbuzz max))))
