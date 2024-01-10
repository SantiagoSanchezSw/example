(ns example
  (:require
    [clojure.test :refer :all]
    [clojure.edn :as edn]
    [clojure.data.json :as json]
    [clojure.spec.alpha :as s]
    [invoice-spec :as invoice-spec]
    [invoice-item :as invoice-item]))

;; FULL FIRST EXERCISE
(defn invoice-data
  "Get the invoice data with a specific file that have retention rate with value 1 or tax rate with value 19",
  [file-path]
  (def invoice (edn/read-string (slurp file-path)))

  (def retention-rates (->> invoice
                            :invoice/items
                            (filter #(and (= 1 (get-in % [:retentionable/retentions 0 :retention/rate]))
                                          (not= 19 (get-in % [:taxable/taxes 0 :tax/rate])
                                                )))))

  (def tax-rates (->> invoice
                      :invoice/items
                      (filter #(and (not= 1 (get-in % [:retentionable/retentions 0 :retention/rate]))
                                    (= 19 (get-in % [:taxable/taxes 0 :tax/rate])
                                       )))))

  (println retention-rates tax-rates)
  )
(invoice-data "../files/invoice.edn")


;; FULL SECOND EXERCISE
;; I HAVE SOME ISSUES WITH THIS EXCERSICE EXACTLY
(defn validate-invoice [json-data]
  ;; create the invoice spec obeject
  (def spec-item {
                  :invoice/issue-date (get-in json-data [:invoice :issue_date])
                  :invoice/customer (get-in json-data [:invoice :customer])
                  :invoice/items (get-in json-data [:invoice :items])
                  }
    )
  (if (s/valid? ::invoice-spec/invoice spec-item )
    (print "THE SPEC IS CORRECT" json-data)
    (println (s/explain ::invoice-spec/invoice json-data))
    ))

(defn read-json-file [file-path]
  (let [json-data (json/read-str (slurp file-path) :key-fn keyword)]
    (validate-invoice json-data)))

(def file-path  "../invoice.json")
(def validated-invoice (read-json-file file-path))



;; FULL THIRT EXERCISE
(deftest test-subtotal
  (testing "subtotal function with valid input"
    (is (= 50.0 (invoice-item/subtotal {:precise-quantity 10 :precise-price 5 :discount-rate 20}))))

  (testing "subtotal function with zero discount-rate"
    (is (= 50.0 (invoice-item/subtotal {:precise-quantity 10 :precise-price 5 :discount-rate 0}))))

  (testing "subtotal function with missing discount-rate (defaults to zero)"
    (is (= 50.0 (invoice-item/subtotal {:precise-quantity 10 :precise-price 5}))))

  (testing "subtotal function with non-numeric discount-rate (defaults to zero)"
    (is (= 0.0 (invoice-item/subtotal {:precise-quantity 10 :precise-price 5 :discount-rate "invalid"}))))

  (testing "subtotal function with negative discount-rate (defaults to zero)"
    (is (= 50.0 (invoice-item/subtotal {:precise-quantity 10 :precise-price 5 :discount-rate -10}))))

  (testing "subtotal function with floating-point discount-rate"
    (is (= 50.0 (invoice-item/subtotal {:precise-quantity 10 :precise-price 5 :discount-rate 20.5}))))

  (testing "subtotal function with missing precise-quantity (defaults to zero)"
    (is (= 0.0 (invoice-item/subtotal {:precise-price 5 :discount-rate 20}))))

  (testing "subtotal function with missing precise-price (defaults to zero)"
    (is (= 0.0 (invoice-item/subtotal {:precise-quantity 10 :discount-rate 20}))))

  (testing "subtotal function with missing precise-quantity and precise-price (defaults to zero)"
    (is (= 0.0 (invoice-item/subtotal {:discount-rate 20}))))
)

(run-test test-subtotal)
