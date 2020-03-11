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

(defn match-action-handler [request]
  (-> request
      :match-data
      deref
      (select-keys [:action])
      create-response))