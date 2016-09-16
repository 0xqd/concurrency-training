(def philosophers-atom (atom (into [] (repeat 5 :thinking))))

(defn think [i]
  (do #_(prn "thinking")
      (Thread/sleep (rand 1000))))

(defn eat [i]
  (do (prn "eating" i)
      (Thread/sleep (rand 1000))))

(defn claim-chopsticks-atom [philosopher left right]
  (do (swap! philosophers-atom (fn [ps]
                                 (if (and (= (get ps left) :thinking)
                                          (= (get ps right) :thinking))
                                   (assoc ps philosopher :eating)
                                   ps)))
      (= (get @philosophers-atom philosopher) :eating)))

(defn release-chopsticks-atom [philosopher]
  (swap! philosophers-atom assoc philosopher :thinking))

(defn philosopher-atom-thread [n]
  (Thread.
   #(let [left  (mod (- n 1) 5)
          right (mod (+ n 1) 5)]
      (while true
        (think n)
        (when (claim-chopsticks-atom n left right) ;; acquire
          (eat n)
          ;; release
          (release-chopsticks-atom n))))))

(defn main-atom []
  (let [threads (map philosopher-atom-thread (range 5))]
    (doseq [thread threads] (.start thread))
    (doseq [thread threads] (.join thread))))

(main-atom)
