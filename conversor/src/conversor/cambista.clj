(ns conversor.cambista
  (:require [clj-http.client :as http-client]
            [cheshire.core :refer [parse-string]]))
            
(def chave (System/getenv "CHAVE_API"))
(def api-url "https://free.currconv.com/api/v7/convert")

(defn obter-cotacao [de para]
  (let [moedas (str de "_" para)]
    (-> (http-client/get api-url {:query-params {"q" moedas "apiKey" chave}})
        (:body)
        (parse-string)
        (get-in ["results" moedas "val"]))))
