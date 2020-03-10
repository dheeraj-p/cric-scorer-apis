(ns cric-scorer-apis.core-logic.match)

(defn start-match [match-data first-team second-team overs]
  (-> match-data
      (assoc :first-team first-team)
      (assoc :second-team second-team)
      (assoc :overs overs)))