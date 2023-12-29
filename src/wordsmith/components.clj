(ns wordsmith.components
  (:require
   [hiccup.page :as hpage]))

(defn auth-container
  "Renders a container for authentication pages."
  [title & content]
  (hpage/html5
   [:head
    [:title title]
    [:link {:rel "stylesheet"
            :href "/css/auth.css"}]]
   [:body
    [:div.auth-container
     content]]))

(def *csrf (atom nil))

(defn csrf []
  (let [csrf (str (random-uuid))]
    (reset! *csrf csrf)
    [:input {:type "hidden"
             :name "_csrf"
             :value csrf}]))


