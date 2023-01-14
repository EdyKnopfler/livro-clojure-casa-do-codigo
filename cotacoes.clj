; Constantes (mapas)
(def moedas
   {:real {:cotacao 1.0 :simbolo "R$"}
    :yuan {:cotacao 2.15 :simbolo "Y"}
    :dolar {:cotacao 0.16 :simbolo "US$"}})

(def transacoes
   [{:valor 33.0 :tipo "despesa" :comentario "Almoço"
     :moeda :real :data "19/11"}
    {:valor 2700.0 :tipo "receita" :comentario "Bico"
     :moeda :real :data "01/12"}
    {:valor 29.0 :tipo "despesa" :comentario "Livro de Clojure"
     :moeda :real :data "03/13"}])
     
; Função
(defn valor-sinalizado [transacao]
   ; Duas formas de fazer (JS):
   ;    simbolo = moedas[transacao.moeda || REAL].simbolo
   ; função get-in ou desestruturação do mapa no let
   ;(let [simbolo (get-in moedas [(:moeda transacao :real) :simbolo])
   (let [{{simbolo :simbolo} (:moeda transacao :real)} moedas
         valor (:valor transacao)
         tipo (:tipo transacao)] 
     (if (= tipo "despesa")
         (str simbolo " -" valor)
         (str simbolo " +" valor))))
         
(defn data-valor [transacao]
   (str (:data transacao) " => " (valor-sinalizado transacao)))

(defn valor-ref-em-real [transacao]
   ;(let [cotacao (get-in moedas [(:moeda transacao) :cotacao])]
   (let [{{cotacao :cotacao} (:moeda transacao)} moedas]
   ;                                             ^^^^^^ impureza!
   ;                            moedas é definido fora da função.
      (/ (:valor transacao) cotacao)))
    
(defn convertido-para [moeda transacao]
   ;(let [cotacao (get-in moedas [moeda :cotacao])]
   (let [{{cotacao :cotacao} moeda} moedas]
      (assoc transacao :valor (* cotacao (valor-ref-em-real transacao))
                       :moeda moeda)))

; Aplicações parciais (currying no Haskell)
(def em-yuan (partial convertido-para :yuan))
(def em-dolar (partial convertido-para :dolar))

; Resumo das transações em dólar
; ("19/11 => US$ -5.28" "01/12 => US$ +432.0" "03/13 => US$ -4.64")
; com função anônima
(map (fn [t] (data-valor (em-dolar t))) transacoes)
; com composição gof(x) = g(f(x))
(map (comp data-valor em-dolar) transacoes)

; API
(clojure.string/join ", " (map (comp data-valor em-dolar) transacoes))

(defn resumo-em-dolar [transacao]
   ; -> macro "sequencial": resultado de uma aplicado como 1º parâmetro da próxima
   (-> (em-dolar transacao)
       (data-valor)))
       
(defn calcular [acumulado transacao]
   (if (= (:tipo transacao) "despesa")
       (- acumulado (:valor transacao))
       (+ acumulado (:valor transacao))))

(defn saldo
    ; sobrecarga de aridade (# de argumentos)
    ([transacoes]
        (saldo 0 transacoes))
    ([acumulado transacoes]
        (if (empty? transacoes)
            acumulado
            ; tail recursion elimination (recur): otimização feita quando a chamada recursiva é a última
            (recur (calcular acumulado (first transacoes))
                   (rest transacoes)))))

; Muitos loops/recursões podem ser substituídos por
(reduce calcular 0 transacoes)

(defn transacao-aleatoria []
    {:valor (* (rand-int 100001) 0.01M)
     :tipo (rand-nth ["despesa" "receita"])})
     
; sequência infiinita (preguiçosa)
(def transacoes-aleatorias (repeatedly transacao-aleatoria))
(take 2 transacoes-aleatorias)

; true!! o repeatedly mantém em memória os elementos já gerados
(= (take 5 transacoes-aleatorias) (take 5 transacoes-aleatorias))

(defn aleatorias
    ([quantidade]
        (aleatorias quantidade 1 (list (transacao-aleatoria))))
    ([quantidade quantas-ja-foram transacoes]
        ; criando uma sequência preguiçosa
        (lazy-seq
            (if (= quantas-ja-foram quantidade)
                transacoes
                (aleatorias quantidade 
                            (inc quantas-ja-foram)
                            (cons (transacao-aleatoria) transacoes))))))
                            
; sequência preguiçosa infinita
(defn aleatorias-inf []
    (lazy-seq
        (cons (transacao-aleatoria) (aleatorias-inf))))

; esta não guarda, sempre recomputa os primeiros a cada vez que é chamada :)
(take 3 (aleatorias-inf))
