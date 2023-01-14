(ns financeiro.handler-test
  (:require [midje.sweet :refer :all]
            [cheshire.core :as json]
            [ring.mock.request :as mock]
            [financeiro.handler :refer :all]
            [financeiro.db :as db]))

(facts "Dá um oi na rota raiz"
  (let [response (app (mock/request :get "/"))]
    (fact "o status da resposta é 200"
      (:status response) => 200)
    (fact "o texto do corpo é 'No ar!'"
      (:body response) => "No ar!")))

(facts "Rota inválida não existe"
  (let [response (app (mock/request :get "/invalid"))]
    (fact "o código de erro é 404"
      (:status response) => 404)
    (fact "o texto do corpo é 'Recurso não encontrado'"
      (:body response) => "Recurso não encontrado")))

(facts "Saldo inicial é 0"
  (against-background [(db/saldo) => 0])  ; mock
  (let [response (app (mock/request :get "/saldo"))]
    (fact "o status da resposta é 200"
      (:status response) => 200)
    (fact "o mime-type é 'application/json'"
      (get-in response [:headers "Content-Type"]) => "application/json; charset=utf-8")
    (fact "o corpo é um JSON com chave 'saldo' e valor 0"
      (json/parse-string (:body response) true) => {:saldo 0})))

(facts "Registra uma receita no valor de 10"
  ;mock
  (against-background (db/registrar {:valor 10 :tipo "receita"}) => {:id 1 :valor 10 :tipo "receita"})
    (let [response (app (-> (mock/request :post "/transacoes")
                            (mock/json-body {:valor 10 :tipo "receita"})))]
      (fact "o status da resposta é 201"
        (:status response) => 201)
      (fact "o texto do corpo é um JSON com o conteúdo enviado e um id"
        (:body response) => "{\"id\":1,\"valor\":10,\"tipo\":\"receita\"}")))

(facts "Existe rota para lidar com filtro de transação por tipo"
  (against-background [(db/transacoes-do-tipo "receita") => '({:id 1 :valor 2000 :tipo "receita"})
                       (db/transacoes-do-tipo "despesa") => '({:id 2 :valor 89 :tipo "despesa"})
                       (db/transacoes) => '({:id 1 :valor 2000 :tipo "receita"} {:id 2 :valor 89 :tipo "despesa"})]
    (fact "Filtro por receita"
      (let [response (app (mock/request :get "/receitas"))]
        (:status response) => 200
        (:body response) => (json/generate-string {:transacoes '({:id 1 :valor 2000 :tipo "receita"})})))

    (fact "Filtro por despesa"
      (let [response (app (mock/request :get "/despesas"))]
        (:status response) => 200
        (:body response) => (json/generate-string {:transacoes '({:id 2 :valor 89 :tipo "despesa"})})))

    (fact "Sem filtro"
      (let [response (app (mock/request :get "/transacoes"))]
        (:status response) => 200
        (:body response) => (json/generate-string {:transacoes '({:id 1 :valor 2000 :tipo "receita"}
                                                                 {:id 2 :valor 89 :tipo "despesa"})})))
  )
)
