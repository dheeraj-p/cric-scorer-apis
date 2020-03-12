(ns cric-scorer-apis.core-logic.match)

(defn register-initial-match-data [match-data first-team second-team overs]
  (-> match-data
      (assoc-in [:first-team :name] first-team)
      (assoc-in [:second-team :name] second-team)
      (assoc :total-overs overs
             :batting-team :first-team
             :action :ACTION_SELECT_INITIAL_PLAYERS)))

(defn create-player [name]
  {:name          name
   :runs          0
   :balls-played  0
   :overs-bowled  0
   :wickets-taken 0
   :catches-taken 0
   :run-outs      0
   :stumpings     0
   })

(defn get-opponent [team]
  (if (= team :first-team) :second-team team))

(defn initialize-team-data [match-data team]
  (-> match-data
      (assoc-in [team :players] [])
      (assoc-in [team :extras :wides] 0)
      (assoc-in [team :extras :no-balls] 0)
      (assoc-in [team :extras :byes] 0)))

(defn initialize-match-data []
  (-> {:action :ACTION_CREATE_GAME}
      (initialize-team-data :first-team)
      (initialize-team-data :second-team)))

(defn add-player-to-team [match-data player team]
  (update-in match-data [team :players] conj (create-player player)))

(defn add-player-to-batting-team [match-data player]
  (add-player-to-team match-data player (:batting-team match-data)))

(defn add-player-to-bowling-team [match-data player]
  (add-player-to-team match-data player (get-opponent (:batting-team match-data))))

(defn set-striker-batsman [match-data player]
  (-> match-data
      (assoc :striker-batsman player)
      (add-player-to-batting-team player)))

(defn set-non-striker-batsman [match-data player]
  (-> match-data
      (assoc :non-striker-batsman player)
      (add-player-to-batting-team player)))

(defn set-bowler [match-data player]
  (-> match-data
      (assoc :bowler player)
      (add-player-to-bowling-team player)))

(defn register-initial-players [match-data striker non-striker bowler]
  (-> match-data
      (set-striker-batsman striker)
      (set-non-striker-batsman non-striker)
      (set-bowler bowler)
      (assoc :action :ACTION_PLAY)))
