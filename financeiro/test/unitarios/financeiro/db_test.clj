(ns financeiro.db-test
  (:require [midje.sweet :refer :all]
            [financeiro.db :refer :all]))

(facts "Guarda uma transação num átomo"
  (against-background [(before :facts (limpar))]
    (fact "a coleção de transações inicia vazia"
      (count (transacoes)) => 0)

    (fact "a transação é o primeiro registro"
      (registrar {:valor 7 :tipo "receita"}) => {:id 1 :valor 7 :tipo "receita"}
      (count (transacoes)) => 1)))

(facts "Calcula o saldo dada uma coleção de transações"
  (against-background [(before :facts (limpar))]
    (fact "saldo é positivo quando só tem receita"
      (registrar {:valor 1 :tipo "receita"})
      (registrar {:valor 10 :tipo "receita"})
      (registrar {:valor 100 :tipo "receita"})
      (registrar {:valor 1000 :tipo "receita"})
      (saldo) => 1111)

    (fact "Saldo é negativo quando só tem despesa"
      (registrar {:valor 2 :tipo "despesa"})
      (registrar {:valor 20 :tipo "despesa"})
      (registrar {:valor 200 :tipo "despesa"})
      (registrar {:valor 2000 :tipo "despesa"})
      (saldo) => -2222)

    (fact "saldo é a soma das receitas menos a soma das despesas"
      (registrar {:valor 2 :tipo "despesa"})
      (registrar {:valor 10 :tipo "receita"})
      (registrar {:valor 200 :tipo "despesa"})
      (registrar {:valor 1000 :tipo "receita"})
      (saldo) => 808)))

(facts "filtra transacoes por tipo"
  (def transacoes-aleatorias
    '({:valor 2 :tipo "despesa"}
      {:valor 10 :tipo "receita"}
      {:valor 200 :tipo "despesa"}
      {:valor 1000 :tipo "receita"}))

  (against-background [(before :facts [(limpar)
                                       (doseq [transacao transacoes-aleatorias]
                                         (registrar transacao))])]
    (fact "encontra apenas as receitas"
      (transacoes-do-tipo "receita") => '({:valor 10 :tipo "receita"} {:valor 1000 :tipo "receita"}))

    (fact "encontra apenas as despesas"
      (transacoes-do-tipo "despesa") => '({:valor 2 :tipo "despesa"} {:valor 200 :tipo "despesa"}))
  )

)
