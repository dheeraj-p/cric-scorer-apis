(ns cric-scorer-apis.core
  (:use ring.adapter.jetty)
  (:require [compojure.core :refer :all]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [ring.middleware.cors :refer [wrap-cors]]
            [cric-scorer-apis.handlers :refer :all]
            [cric-scorer-apis.core-logic.match :refer [initialize-match-data]]))

(defonce match-data
         (atom (initialize-match-data)))

;Middlerwares Start

(defn wrap-match-data [handler]
  (fn [request]
    (-> request
        (assoc :match-data match-data)
        handler)))

(defn wrap-logger [handler]
  (fn [request]
    (let [response (handler request)]
      (println "Request------------------------------------\n" request "-----------------------------------\nResponse" response)
      response)))

;Middlewares End

(defroutes app-routes
           (POST "/register-match" request (match-registration-handler request))
           (POST "/register-initial-players" request (register-initial-players-handler request))
           (GET "/match-action" request (match-action-handler request)))

(def app
  (-> app-routes
      wrap-json-response
      wrap-match-data
      wrap-logger
      wrap-json-body
      (wrap-cors :access-control-allow-origin [#"http://localhost:3449"]
                 :access-control-allow-methods [:get :put :post :delete])))

(defn -main []
  (run-jetty app {:port 8000}))
