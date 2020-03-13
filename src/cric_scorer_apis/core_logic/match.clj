(ns cric-scorer-apis.core-logic.match)

(defn debug [x] (println x) x)

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
   :6s            0
   :4s            0
   :balls-played  0
   :balls-bowled  0
   :runs-conceded 0
   :maidens       0
   :wickets-taken 0
   :catches-taken 0
   :run-outs      0
   :stumpings     0
   :out?          false
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
  (-> {:action       :ACTION_CREATE_GAME
       :innings      :first
       :current-over []}
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

(defn total-balls-played [match-data team]
  (apply + (map :balls-played (get-in match-data [team :players]))))

(defn convert-to-overs [balls]
  {:overs (quot balls 6)
   :balls (rem balls 6)})

(defn overs-played-by-team [match-data team]
  (let [balls-played (total-balls-played match-data team)]
    (convert-to-overs balls-played)))

(defn runs-scored-by-team [match-data team]
  (apply + (map :runs (get-in match-data [team :players]))))

(defn wickets-fallen [match-data team]
  (apply + (filter :out? (get-in match-data [team :players]))))

(defn run-rate [runs balls]
  (if (zero? balls)
    0
    (-> runs
        (* 6)
        (/ balls)
        float)))

(defn calculate-strike-rate [player]
  (if (zero? (:balls-played player))
    0
    (-> (:runs player)
        (/ (:balls-played player))
        (* 100)
        int)))

(defn display-overs [{overs :overs balls :balls}]
  (str overs "." balls))

(defn bowling-stats [player]
  {:name    (:name player)
   :overs   (display-overs (convert-to-overs (:balls-bowled player)))
   :maidens (:maidens player)
   :runs    (:runs-conceded player)
   :wickets (:wickets-taken player)
   :economy (run-rate (:runs-conceded player) (:balls-bowled player))})


(defn team-stats [match-data team]
  {
   :name    (get-in match-data [team :name])
   :over    (display-overs (overs-played-by-team match-data team))
   :runs    (runs-scored-by-team match-data team)
   :wickets (wickets-fallen match-data team)
   })

(defn score-header [match-data]
  {:team1       (team-stats match-data :first-team)
   :team2       (team-stats match-data :second-team)
   :stats       {:CRR (run-rate (runs-scored-by-team match-data (:batting-team match-data))
                                (total-balls-played match-data (:batting-team match-data)))
                 :RRR 0}
   :total-overs (:total-overs match-data)})

(defn find-player-by-name [player-name players]
  (first (filter #(= (:name %) player-name) players)))

(defn batsman-stats [batsman]
  {:name  (:name batsman)
   :runs  (:runs batsman)
   :balls (:balls-played batsman)
   :4s    (:4s batsman)
   :6s    (:6s batsman)
   :SR    (calculate-strike-rate batsman)})

(defn current-batsmen-stats [match-data]
  (map #(-> (% match-data)
            (find-player-by-name (get-in match-data [(:batting-team match-data) :players]))
            batsman-stats) [:striker-batsman :non-striker-batsman]))

(defn current-bowler-stats [match-data]
  (bowling-stats (find-player-by-name (:bowler match-data) (get-in match-data [(get-opponent (:batting-team match-data)) :players]))))

(defn match-stats [match-data]
  {:score-header          (score-header match-data)
   :innings               (:innings match-data)
   :current-batsmen-stats (current-batsmen-stats match-data)
   :current-bowler-stats  (current-bowler-stats match-data)
   :current-over          (:current-over match-data)})