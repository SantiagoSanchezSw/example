(ns invoice-item)

(defn- discount-factor [{:invoice-item/keys [discount-rate]
                         :or                {discount-rate 0}}]
  (- 1 (/ discount-rate 100.0)))


(defn subtotal
  [{:keys [precise-quantity precise-price discount-rate]
    :as                item
    :or                {discount-rate 0}}]
  (if (and (number? precise-quantity) (number? precise-price) (number? discount-rate))
    (* precise-price precise-quantity (discount-factor item))
    0.0))


