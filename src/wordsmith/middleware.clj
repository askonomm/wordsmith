(ns wordsmith.middleware 
  (:require
   [wordsmith.data :as data]))

(def guard-handler
  ^{:response/status 200
    :response/type :redirect}
  (fn [_]
    {:to "/admin/login"
     :flash {:error "You must be logged in to view that page."}}))

(defn is-authenticated?
  [req handler]
  (if (and (-> req :cookie :token)
           (data/valid-auth-token? (-> req :cookie :token)))
    handler
    guard-handler))    
