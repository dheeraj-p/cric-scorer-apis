(ns cric-scorer-apis.handlers
  (:require [cric-scorer-apis.core-logic.match :as core-logic]))

(defn create-response
  ([body] {:status 200 :body body})
  ([headers body] {:headers headers :body body :status 200})
  ([status-code headers body] {:status status-code :headers headers :body body}))

;Handlers Start

(defn match-registration-handler
  [{{first-team  "first-team"
     second-team "second-team"
     overs       "overs"} :body
    match-data            :match-data}]
  (-> (swap! match-data core-logic/register-initial-match-data first-team second-team overs)
      create-response))

(defn match-action-handler [{match-data :match-data}]
  (-> @match-data
      (select-keys [:action])
      create-response))

(defn register-initial-players-handler
  [{match-data :match-data
    {striker "striker"
     non-striker "non-striker"
     bowler "bowler"} :body}]
  (-> match-data
      (swap! core-logic/register-initial-players striker non-striker bowler)
      create-response))