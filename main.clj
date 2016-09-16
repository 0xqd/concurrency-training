(def philosophers (into [] (repeatedly 5 #(ref :thinking))))

(defn claim-chopsticks [philosopher left right]
  (dosync
   (when (and (= (ensure left) :thinking) (= (ensure right) :thinking))
     (ref-set philosopher :eating))))

(defn release-chopsticks [philosopher]
  (dosync (ref-set philosopher :thinking)))

(defn think [i]
  (do #_(prn "thinking")
      (Thread/sleep (rand 1000))))

(defn eat [i]
  (do (prn "eating" i)
      (Thread/sleep (rand 1000))))

(defn philosopher-thread [n]
  (Thread.
   #(let [philosopher (philosophers n)
          left        (philosophers (mod (- n 1) 5))
          right       (philosophers (mod (+ n 1) 5))]
      (while true
        (think n)
        (when (claim-chopsticks philosopher left right)
          (eat n)
          (release-chopsticks philosopher))))))

(defn main []
  (let [threads (map philosopher-thread (range 5))]
    (doseq [thread threads] (.start thread))
    (doseq [thread threads] (.join thread))))

(main)
