(ns cric-scorer-apis.core-logic.match)

(defn register-initial-match-data [match-data first-team second-team overs]
  (-> match-data
      (assoc :first-team first-team)
      (assoc :second-team second-team)
      (assoc :overs overs)
      (assoc :action :ACTION_SELECT_INITIAL_PLAYERS)))