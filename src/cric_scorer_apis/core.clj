(ns cric-scorer-apis.core
  (:use ring.adapter.jetty)
  (:require [compojure.core :refer :all]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [ring.middleware.cors :refer [wrap-cors]]))

(defonce match-data
         (atom
           {:first-team  nil
            :second-team nil}))

(defn start-match [{{first-team "first-team" second-team "second-team"} :body}]
  (swap! match-data #(-> %
                         (assoc :first-team first-team)
                         (assoc :second-team second-team))))

(defroutes app-routes
           (POST "/start-match" request {:status 200
                                         :body   (start-match request)}))

(def app
  (-> app-routes
      wrap-json-response
      wrap-json-body
      (wrap-cors :access-control-allow-origin [#"http://localhost:3449"]
                 :access-control-allow-methods [:get :put :post :delete])))

(defn -main []
  (run-jetty app {:port 8000}))
