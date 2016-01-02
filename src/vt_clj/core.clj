(ns vt-clj.core
  (:import [id.co.veritrans.mdk.v1 VtGatewayConfigBuilder VtGatewayConfig VtGatewayFactory]
           [id.co.veritrans.mdk.v1.gateway.model.vtweb VtWebChargeRequest VtWebParam]
           [id.co.veritrans.mdk.v1.gateway.model TransactionDetails]
           [id.co.veritrans.mdk.v1.config EnvironmentType]))

(defn- gateway-config 
  "Configuring the gateway builder."
  [server-key client-key env]
  (let [vt-gateway-config-builder (VtGatewayConfigBuilder.)]
    (.setServerKey vt-gateway-config-builder server-key)
    (.setClientKey vt-gateway-config-builder client-key)
    (if (true? env)
      (.setEnvironmentType vt-gateway-config-builder (EnvironmentType/SANDBOX))
      (.setEnvironmentType vt-gateway-config-builder (EnvironmentType/PRODUCTION)))))

(defn- create-gateway-factory
  "Creating the gateway factory to obtain a reference to various Veritrans product instance."
  [gateway-builder-object]
  (let [vt-gateway-config (.createVtGatewayConfig gateway-builder-object)]
    (VtGatewayFactory. vt-gateway-config)))

(defn vt-web
  "Using VT-Web product. Default environment is sandbox."
  [server-key client-key transaction-id price & {:keys [env] :or {env true}}]
  (let [config (gateway-config server-key client-key env)
        factory (create-gateway-factory config)
        vt-web (.vtWeb factory)
        transaction-details (TransactionDetails. transaction-id (long price))
        vt-web-param (VtWebParam.)
        vt-web-charge-req (VtWebChargeRequest. transaction-details vt-web-param)]
    
    ;; Setup VT-Web parameter
    (.setVtWeb vt-web-charge-req vt-web-param)
    (.setCreditCardUse3dSecure (.getVtWeb vt-web-charge-req) true)
    
    ;; Get the response
    (let [vt-response (.charge vt-web vt-web-charge-req)]
      (if (= (.getStatusCode vt-response) "201")
        (.getRedirectUrl vt-response)
        "error"))))
