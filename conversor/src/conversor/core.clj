(ns conversor.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [conversor.cambista :refer [obter-cotacao]])
  (:gen-class))
  
(def opcoes-do-programa
  [["-d" "--de moeda_base" "moeda base para conversão" :default "eur"]
   ["-p" "--para moeda_destino" "moeda a qual queremos saber o valor"]])
   
(defn- formatar [cotacao de para]
  (str "1 " de " equivale a " cotacao " em " para))
  
(defn -main
  [& args]
  (let [{:keys [de para]} (:options (parse-opts args opcoes-do-programa))]
    (-> (obter-cotacao de para)
        (formatar de para)
        (prn))))
