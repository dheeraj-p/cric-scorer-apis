(ns cric-scorer-apis.core
  (:use ring.adapter.jetty)
  (:require [compojure.core :refer :all]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [ring.middleware.cors :refer [wrap-cors]]
            [cric-scorer-apis.handlers :refer :all]))

(defonce match-data
         (atom
           {:action :ACTION_CREATE_GAME}))

(defn wrap-match-data [handler]
  (fn [request]
    (-> request
        (assoc :match-data match-data)
        handler)))

(defroutes app-routes
           (POST "/register-match" request (match-registration-handler request))
           (GET "/match-action" request (match-action-handler request)))

(def app
  (-> app-routes
      wrap-json-response
      wrap-match-data
      wrap-json-body
      (wrap-cors :access-control-allow-origin [#"http://localhost:3449"]
                 :access-control-allow-methods [:get :put :post :delete])))

(defn -main []
  (run-jetty app {:port 8000}))
