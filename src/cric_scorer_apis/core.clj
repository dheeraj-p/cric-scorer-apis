(ns cric-scorer-apis.core
  (:use ring.adapter.jetty)
  (:require [compojure.core :refer :all]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [ring.middleware.cors :refer [wrap-cors]]
            [cric-scorer-apis.core-logic.match :as core-logic]))

(defonce match-data
         (atom
           {:action :ACTION_CREATE_GAME}))

;Handlers Start

(defn start-match-handler
  [{{first-team "first-team"
     second-team "second-team"
     overs "overs"} :body}]
  (swap! match-data core-logic/start-match first-team second-team overs))

;Handlers End


(defroutes app-routes
           (POST "/start-match" request {:status 200
                                         :body   (start-match-handler request)})
           (GET "/match-action" _ {:status 200
                                   :body   (select-keys @match-data [:action])}))

(def app
  (-> app-routes
      wrap-json-response
      wrap-json-body
      (wrap-cors :access-control-allow-origin [#"http://localhost:3449"]
                 :access-control-allow-methods [:get :put :post :delete])))

(defn -main []
  (run-jetty app {:port 8000}))
